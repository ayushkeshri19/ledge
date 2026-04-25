package com.ayush.transactions.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.ayush.common.notification.BudgetNotificationHelper
import com.ayush.common.utils.Workers
import com.ayush.common.utils.endOfDay
import com.ayush.common.utils.startOfDay
import com.ayush.database.dao.BudgetDao
import com.ayush.database.dao.CategoryDao
import com.ayush.database.dao.TransactionDao
import com.ayush.database.data.DefaultCategories
import com.ayush.database.data.SyncStatus
import com.ayush.database.data.TransactionEntity
import com.ayush.transactions.data.remote.SupabaseTransactionDataSource
import com.ayush.transactions.data.remote.SupabaseTransactionDto
import com.ayush.transactions.data.sync.TransactionSyncWorker
import com.ayush.transactions.domain.models.Category
import com.ayush.transactions.domain.models.RecurrenceType
import com.ayush.transactions.domain.models.Transaction
import com.ayush.transactions.domain.models.TransactionType
import com.ayush.transactions.domain.models.toDomain
import com.ayush.transactions.domain.repository.TransactionRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val budgetDao: BudgetDao,
    private val supabaseDataSource: SupabaseTransactionDataSource,
    private val supabaseClient: SupabaseClient,
    private val workManager: WorkManager,
    private val budgetNotificationHelper: BudgetNotificationHelper,
) : TransactionRepository {

    companion object {
        private val PAGING_CONFIG = PagingConfig(
            pageSize = 20,
            prefetchDistance = 10,
            enablePlaceholders = false
        )
    }

    override fun getAllTransactions(): Flow<PagingData<Transaction>> {
        return Pager(
            config = PAGING_CONFIG,
            pagingSourceFactory = { transactionDao.getAllTransactions() }
        ).flow.map { pagingData -> pagingData.map { it.toDomain() } }
    }

    override fun searchTransactions(query: String): Flow<PagingData<Transaction>> {
        return Pager(
            config = PAGING_CONFIG,
            pagingSourceFactory = { transactionDao.searchTransactions(query) }
        ).flow.map { pagingData -> pagingData.map { it.toDomain() } }
    }

    override fun getFilteredTransactions(
        startDate: Long,
        endDate: Long,
        type: TransactionType?,
        categoryId: Long?,
        minAmount: Double?,
        maxAmount: Double?
    ): Flow<PagingData<Transaction>> {
        return Pager(
            config = PAGING_CONFIG,
            pagingSourceFactory = {
                transactionDao.getFilteredTransactions(
                    startDate = startDate,
                    endDate = endDate,
                    type = type?.value,
                    categoryId = categoryId,
                    minAmount = minAmount,
                    maxAmount = maxAmount,
                )
            },
        ).flow.map { pagingData -> pagingData.map { it.toDomain() } }
    }

    override suspend fun getTransactionById(id: Long): Transaction? {
        return withContext(Dispatchers.IO) {
            transactionDao.getTransactionById(id)?.let { entity ->
                val category = entity.categoryId?.let { categoryDao.getCategoryById(it) }
                Transaction(
                    id = entity.id,
                    amount = entity.amount,
                    type = TransactionType.fromValue(entity.type),
                    category = category?.toDomain(),
                    note = entity.note,
                    date = entity.date,
                    isRecurring = entity.isRecurring,
                    recurrenceType = RecurrenceType.fromValue(entity.recurrenceType),
                )
            }
        }
    }

    override suspend fun addTransaction(
        amount: Double,
        type: TransactionType,
        categoryId: Long?,
        note: String,
        date: Long,
        isRecurring: Boolean,
        recurrenceType: String?
    ): Long = withContext(Dispatchers.IO) {
        val localId = transactionDao.insert(
            TransactionEntity(
                amount = amount,
                type = type.value,
                categoryId = categoryId,
                note = note,
                date = date,
                isRecurring = isRecurring,
                recurrenceType = recurrenceType,
                userId = currentUserId().orEmpty(),
                syncStatus = SyncStatus.PENDING_CREATE,
            )
        )
        enqueueSync()
        checkBudgetAlerts()
        localId
    }

    override suspend fun updateTransaction(
        id: Long,
        amount: Double,
        type: TransactionType,
        categoryId: Long?,
        note: String,
        date: Long,
        isRecurring: Boolean,
        recurrenceType: String?
    ) {
        withContext(Dispatchers.IO) {
            val existing = transactionDao.getTransactionById(id) ?: return@withContext
            val newSyncStatus = if (existing.syncStatus == SyncStatus.PENDING_CREATE) {
                SyncStatus.PENDING_CREATE
            } else {
                SyncStatus.PENDING_UPDATE
            }
            transactionDao.update(
                existing.copy(
                    amount = amount,
                    type = type.value,
                    categoryId = categoryId,
                    note = note,
                    date = date,
                    isRecurring = isRecurring,
                    recurrenceType = recurrenceType,
                    syncStatus = newSyncStatus,
                )
            )
            enqueueSync()
            checkBudgetAlerts()
        }
    }

    override suspend fun deleteTransaction(id: Long) = withContext(Dispatchers.IO) {
        val entity = transactionDao.getTransactionById(id)
        if (entity == null) {
            transactionDao.deleteById(id)
            return@withContext
        }
        if (entity.syncStatus == SyncStatus.PENDING_CREATE) {
            transactionDao.deleteById(id)
        } else {
            transactionDao.updateSyncStatus(id, SyncStatus.PENDING_DELETE)
            enqueueSync()
        }
    }

    override fun getAllCategories(): Flow<List<Category>> {
        return categoryDao.getAllCategories().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun ensureDefaultCategories() = withContext(Dispatchers.IO) {
        if (categoryDao.count() == 0) {
            categoryDao.insertAll(DefaultCategories.entries)
        }
    }

    override suspend fun getPendingSync(): List<TransactionEntity> = withContext(Dispatchers.IO) {
        transactionDao.getPendingSyncTransactions()
    }

    override suspend fun currentUserId(): String? = withContext(Dispatchers.IO) {
        supabaseClient.auth.currentSessionOrNull()?.user?.id
    }

    override suspend fun pushCreate(transaction: TransactionEntity, userId: String) = withContext(Dispatchers.IO) {
        val categoryName = transaction.categoryId?.let { categoryDao.getCategoryById(it)?.name }
        val parentRemoteId = transaction.parentId?.let { transactionDao.getTransactionById(it)?.remoteId }
        val dto = SupabaseTransactionDto(
            userId = userId,
            amount = transaction.amount,
            type = transaction.type,
            categoryName = categoryName,
            note = transaction.note,
            date = transaction.date,
            isRecurring = transaction.isRecurring,
            recurrenceType = transaction.recurrenceType,
            createdAt = transaction.createdAt,
            parentRemoteId = parentRemoteId,
            lastExecutedDate = transaction.lastExecutedDate
        )
        val response = supabaseDataSource.insert(dto)
        transactionDao.update(
            transaction.copy(
                remoteId = response.id,
                syncStatus = SyncStatus.SYNCED,
            )
        )
    }

    override suspend fun pushUpdate(transaction: TransactionEntity, userId: String) = withContext(Dispatchers.IO) {
        val remoteId = transaction.remoteId ?: return@withContext
        val categoryName = transaction.categoryId?.let { categoryDao.getCategoryById(it)?.name }
        val parentRemoteId = transaction.parentId?.let { transactionDao.getTransactionById(it)?.remoteId }
        val dto = SupabaseTransactionDto(
            userId = userId,
            amount = transaction.amount,
            type = transaction.type,
            categoryName = categoryName,
            note = transaction.note,
            date = transaction.date,
            isRecurring = transaction.isRecurring,
            recurrenceType = transaction.recurrenceType,
            createdAt = transaction.createdAt,
            parentRemoteId = parentRemoteId,
            lastExecutedDate = transaction.lastExecutedDate
        )
        supabaseDataSource.update(remoteId, dto)
        transactionDao.updateSyncStatus(transaction.id, SyncStatus.SYNCED)
    }

    override suspend fun pushDelete(transaction: TransactionEntity) = withContext(Dispatchers.IO) {
        transaction.remoteId?.let { remoteId -> supabaseDataSource.delete(remoteId) }
        transactionDao.deleteById(transaction.id)
    }

    override suspend fun syncFromRemote() = withContext(Dispatchers.IO) {
        val userId = currentUserId().orEmpty()
        if (userId.isEmpty()) {
            return@withContext
        }
        try {
            val remoteTransactions = supabaseDataSource
                .fetchAllForUser(userId)
                .sortedBy { it.createdAt }
            remoteTransactions.forEach { dto ->
                val remoteId = dto.id ?: return@forEach
                if (transactionDao.getByRemoteId(remoteId) == null) {
                    val category = dto.categoryName?.let { categoryDao.getCategoryByName(it) }
                    val localParentId = dto.parentRemoteId?.let {
                        transactionDao.getByRemoteId(it)?.id
                    }
                    transactionDao.insert(
                        TransactionEntity(
                            amount = dto.amount,
                            type = dto.type,
                            categoryId = category?.id,
                            note = dto.note,
                            date = dto.date,
                            isRecurring = dto.isRecurring,
                            recurrenceType = dto.recurrenceType,
                            createdAt = dto.createdAt,
                            remoteId = remoteId,
                            userId = dto.userId,
                            syncStatus = SyncStatus.SYNCED,
                            parentId = localParentId,
                            lastExecutedDate = dto.lastExecutedDate
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "syncFromRemote: FAILED")
        }
    }

    override suspend fun getRecurringTransactions(): List<Transaction> {
        return withContext(Dispatchers.IO) {
            transactionDao.getRecurringTransactions()
                .map { it.toDomain() }
        }
    }

    override suspend fun updateLastExecutedDate(id: Long, date: Long) {
        withContext(Dispatchers.IO) {
            val existing = transactionDao.getTransactionById(id) ?: return@withContext
            val newSyncStatus = if (existing.syncStatus == SyncStatus.PENDING_CREATE) {
                SyncStatus.PENDING_CREATE
            } else {
                SyncStatus.PENDING_UPDATE
            }
            transactionDao.update(
                existing.copy(
                    lastExecutedDate = date,
                    syncStatus = newSyncStatus
                )
            )
            enqueueSync()
        }
    }

    override suspend fun createRecurringInstance(
        template: Transaction,
        date: Long
    ) {
        transactionDao.insert(
            TransactionEntity(
                amount = template.amount,
                type = template.type.value,
                categoryId = template.category?.id,
                note = template.note,
                date = date,
                isRecurring = false,
                recurrenceType = null,
                parentId = template.id,
                userId = currentUserId().orEmpty(),
                syncStatus = SyncStatus.PENDING_CREATE
            )
        )
        enqueueSync()
        checkBudgetAlerts()

    }

    override suspend fun stopRecurringSeries(templateId: Long) {
        withContext(Dispatchers.IO) {
            val existing = transactionDao.getTransactionById(templateId) ?: return@withContext
            val newSyncStatus = if (existing.syncStatus == SyncStatus.PENDING_CREATE) {
                SyncStatus.PENDING_CREATE
            } else {
                SyncStatus.PENDING_UPDATE
            }
            transactionDao.update(
                existing.copy(
                    isRecurring = false,
                    recurrenceType = null,
                    syncStatus = newSyncStatus
                )
            )
            enqueueSync()
        }
    }

    override suspend fun recurringInstanceExists(parentId: Long, date: Long): Boolean {
        return withContext(Dispatchers.IO) {
            transactionDao.countRecurringInstanceByParentAndDate(parentId, date) > 0
        }
    }

    private suspend fun checkBudgetAlerts() {
        try {
            val (monthStart, monthEnd) = currentMonthRange()
            val budgets = budgetDao.getAllBudgetsSnapshot()
            if (budgets.isEmpty()) return

            val categorySpending = transactionDao.getExpensesByCategory(monthStart, monthEnd)
            val totalSpent = transactionDao.getTotalByTypeAndDateRange("expense", monthStart, monthEnd) ?: 0.0

            budgets.forEach { budget ->
                val spent = if (budget.categoryId == null) {
                    totalSpent
                } else {
                    categorySpending.find { it.categoryId == budget.categoryId }?.totalAmount ?: 0.0
                }

                val ratio = if (budget.amount > 0) spent / budget.amount else 0.0
                val threshold = budget.warningThreshold / 100.0
                val categoryName = budget.categoryId?.let { categoryDao.getCategoryById(it)?.name }

                when {
                    ratio >= 1.0 -> {
                        if (!budget.exceededNotified) {
                            budgetNotificationHelper.notifyExceeded(
                                budgetId = budget.id,
                                categoryName = categoryName,
                                overBy = spent - budget.amount,
                                limit = budget.amount,
                            )
                            budgetDao.updateExceededNotified(budget.id, true)
                        }
                    }

                    ratio >= threshold -> {
                        if (!budget.warningNotified) {
                            budgetNotificationHelper.notifyWarning(
                                budgetId = budget.id,
                                categoryName = categoryName,
                                thresholdPercent = budget.warningThreshold,
                                spent = spent,
                                limit = budget.amount,
                            )
                            budgetDao.updateWarningNotified(budget.id, true)
                        }
                        if (budget.exceededNotified) {
                            budgetDao.updateExceededNotified(budget.id, false)
                        }
                    }

                    else -> {
                        if (budget.warningNotified) budgetDao.updateWarningNotified(budget.id, false)
                        if (budget.exceededNotified) budgetDao.updateExceededNotified(budget.id, false)
                    }
                }
            }
        } catch (e: Exception) {
            Timber.d(e, "checkBudgetAlerts failed")
        }
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

    private fun enqueueSync() {
        val request = OneTimeWorkRequestBuilder<TransactionSyncWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        workManager.enqueueUniqueWork(
            Workers.TRANSACTION_SYNC,
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            request
        )
    }
}
