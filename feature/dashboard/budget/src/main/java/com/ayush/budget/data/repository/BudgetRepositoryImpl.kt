package com.ayush.budget.data.repository

import com.ayush.budget.data.remote.SupabaseBudgetDataSource
import com.ayush.budget.data.remote.SupabaseBudgetDto
import com.ayush.budget.domain.models.Budget
import com.ayush.budget.domain.models.BudgetWithSpent
import com.ayush.budget.domain.repository.BudgetRepository
import com.ayush.common.utils.endOfDay
import com.ayush.common.utils.startOfDay
import com.ayush.database.dao.BudgetDao
import com.ayush.database.dao.CategoryDao
import com.ayush.database.dao.TransactionDao
import com.ayush.database.data.BudgetEntity
import com.ayush.database.data.SyncStatus
import com.ayush.ui.utils.hexToColor
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import timber.log.Timber
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class BudgetRepositoryImpl @Inject constructor(
    private val budgetDao: BudgetDao,
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val supabaseClient: SupabaseClient,
    private val supabaseDataSource: SupabaseBudgetDataSource,
) : BudgetRepository {

    override fun observeAllBudgetsWithSpent(): Flow<List<BudgetWithSpent>> {
        val (monthStart, monthEnd) = currentMonthRange()

        return budgetDao.getAllBudgets().flatMapLatest { budgets ->
            if (budgets.isEmpty()) return@flatMapLatest flowOf(emptyList())

            combine(
                transactionDao.observeTotalByTypeAndDateRange("expense", monthStart, monthEnd),
                transactionDao.observeExpensesByCategory(monthStart, monthEnd),
            ) { totalExpense, categorySpending ->
                budgets.map { entity ->
                    val spent = if (entity.categoryId == null) {
                        totalExpense ?: 0.0
                    } else {
                        categorySpending
                            .find { it.categoryId == entity.categoryId }
                            ?.totalAmount ?: 0.0
                    }

                    val category = entity.categoryId?.let { categoryDao.getCategoryById(it) }

                    BudgetWithSpent(
                        budget = Budget(
                            id = entity.id,
                            categoryId = entity.categoryId,
                            categoryName = if (entity.categoryId == null) "Overall" else category?.name,
                            categoryColor = category?.colorHex?.let { hexToColor(it) },
                            amount = entity.amount,
                            warningThreshold = entity.warningThreshold,
                        ),
                        spent = spent,
                    )
                }
            }
        }
    }

    override suspend fun saveBudget(categoryId: Long?, amount: Double, warningThreshold: Int) {
        val userId = currentUserId()

        val existing = if (categoryId == null) {
            budgetDao.getOverallBudget()
        } else {
            budgetDao.getBudgetByCategory(categoryId)
        }

        val syncStatus = if (existing != null) {
            if (existing.syncStatus == SyncStatus.PENDING_CREATE) SyncStatus.PENDING_CREATE
            else SyncStatus.PENDING_UPDATE
        } else {
            SyncStatus.PENDING_CREATE
        }

        budgetDao.upsert(
            BudgetEntity(
                id = existing?.id ?: 0,
                categoryId = categoryId,
                amount = amount,
                warningThreshold = warningThreshold,
                userId = userId,
                remoteId = existing?.remoteId,
                syncStatus = syncStatus,
            )
        )

        try {
            syncPendingBudgets()
        } catch (e: Exception) {
            Timber.d(e, "Immediate budget sync failed, will retry later")
        }
    }

    override suspend fun deleteBudget(id: Long) {
        val entity = budgetDao.getAllBudgetsSnapshot().find { it.id == id }
        if (entity == null) {
            budgetDao.deleteById(id)
            return
        }

        if (entity.syncStatus == SyncStatus.PENDING_CREATE) {
            budgetDao.deleteById(id)
        } else {
            budgetDao.updateSyncStatus(id, SyncStatus.PENDING_DELETE)
            try {
                syncPendingBudgets()
            } catch (e: Exception) {
                Timber.d(e, "Immediate budget sync failed, will retry later")
            }
        }
    }

    override suspend fun syncFromRemote() {
        val userId = currentUserId()
        if (userId.isEmpty()) return

        try {
            val remoteBudgets = supabaseDataSource.fetchAllForUser(userId)
            remoteBudgets.forEach { dto ->
                val remoteId = dto.id ?: return@forEach
                if (budgetDao.getByRemoteId(remoteId) == null) {
                    val category = dto.categoryName?.let { categoryDao.getCategoryByName(it) }
                    budgetDao.upsert(
                        BudgetEntity(
                            categoryId = category?.id,
                            amount = dto.amount,
                            warningThreshold = dto.warningThreshold,
                            userId = dto.userId,
                            remoteId = remoteId,
                            syncStatus = SyncStatus.SYNCED,
                            createdAt = dto.createdAt,
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Budget syncFromRemote: FAILED")
        }

        try {
            syncPendingBudgets()
        } catch (e: Exception) {
            Timber.d(e, "Budget syncPending after remote: FAILED")
        }
    }

    private suspend fun syncPendingBudgets() {
        val pending = budgetDao.getPendingSyncBudgets()

        pending.forEach { entity ->
            try {
                when (entity.syncStatus) {
                    SyncStatus.PENDING_CREATE -> {
                        val categoryName = entity.categoryId?.let {
                            categoryDao.getCategoryById(it)?.name
                        }
                        val dto = SupabaseBudgetDto(
                            userId = entity.userId,
                            categoryName = categoryName,
                            amount = entity.amount,
                            warningThreshold = entity.warningThreshold,
                            createdAt = entity.createdAt,
                        )
                        val result = supabaseDataSource.insert(dto)
                        budgetDao.upsert(
                            entity.copy(
                                remoteId = result.id,
                                syncStatus = SyncStatus.SYNCED,
                            )
                        )
                    }

                    SyncStatus.PENDING_UPDATE -> {
                        val remoteId = entity.remoteId ?: return@forEach
                        val categoryName = entity.categoryId?.let {
                            categoryDao.getCategoryById(it)?.name
                        }
                        val dto = SupabaseBudgetDto(
                            userId = entity.userId,
                            categoryName = categoryName,
                            amount = entity.amount,
                            warningThreshold = entity.warningThreshold,
                            createdAt = entity.createdAt,
                        )
                        supabaseDataSource.update(remoteId, dto)
                        budgetDao.updateSyncStatus(entity.id, SyncStatus.SYNCED)
                    }

                    SyncStatus.PENDING_DELETE -> {
                        entity.remoteId?.let { supabaseDataSource.delete(it) }
                        budgetDao.deleteById(entity.id)
                    }

                    SyncStatus.SYNCED -> {
                        /** No-op */
                    }
                }
            } catch (e: Exception) {
                Timber.d(e, "Failed to sync budget ${entity.id}")
            }
        }
    }

    private fun currentUserId(): String {
        return supabaseClient.auth.currentSessionOrNull()?.user?.id ?: ""
    }

    private fun currentMonthRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.startOfDay()
        val start = cal.timeInMillis
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.endOfDay()
        return Pair(start, cal.timeInMillis)
    }
}
