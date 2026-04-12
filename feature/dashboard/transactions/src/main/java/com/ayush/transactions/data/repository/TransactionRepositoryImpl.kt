package com.ayush.transactions.data.repository

import com.ayush.database.dao.CategoryDao
import com.ayush.database.dao.TransactionDao
import com.ayush.database.data.DefaultCategories
import com.ayush.database.data.TransactionEntity
import com.ayush.transactions.data.remote.SupabaseTransactionDataSource
import com.ayush.transactions.data.remote.SupabaseTransactionDto
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
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
    private val supabaseDataSource: SupabaseTransactionDataSource,
    private val supabaseClient: SupabaseClient,
) : TransactionRepository {

    override fun getAllTransactions(): Flow<List<Transaction>> {
        return transactionDao.getAllTransactions().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun searchTransactions(query: String): Flow<List<Transaction>> {
        return transactionDao.searchTransactions(query).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getFilteredTransactions(
        startDate: Long,
        endDate: Long,
        type: TransactionType?,
        categoryId: Long?,
        minAmount: Double?,
        maxAmount: Double?,
    ): Flow<List<Transaction>> {
        return transactionDao.getFilteredTransactions(
            startDate = startDate,
            endDate = endDate,
            type = type?.value,
            categoryId = categoryId,
            minAmount = minAmount,
            maxAmount = maxAmount,
        ).map { list -> list.map { it.toDomain() } }
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
        recurrenceType: String?,
    ): Long = withContext(Dispatchers.IO) {
        val userId = currentUserId()
        val localId = transactionDao.insert(
            TransactionEntity(
                amount = amount,
                type = type.value,
                categoryId = categoryId,
                note = note,
                date = date,
                isRecurring = isRecurring,
                recurrenceType = recurrenceType,
                userId = userId,
            )
        )

        if (userId.isNotEmpty()) {
            try {
                val categoryName = categoryId?.let { categoryDao.getCategoryById(it)?.name }
                val dto = supabaseDataSource.insert(
                    SupabaseTransactionDto(
                        userId = userId,
                        amount = amount,
                        type = type.value,
                        categoryName = categoryName,
                        note = note,
                        date = date,
                        isRecurring = isRecurring,
                        recurrenceType = recurrenceType,
                        createdAt = System.currentTimeMillis(),
                    )
                )
                transactionDao.update(
                    transactionDao.getTransactionById(localId)!!.copy(remoteId = dto.id)
                )
            } catch (e: Exception) {
                Timber.e(e, "Failed to sync new transaction to Supabase")
            }
        }

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
        recurrenceType: String?,
    ) {
        withContext(Dispatchers.IO) {
            val existing = transactionDao.getTransactionById(id) ?: return@withContext
            transactionDao.update(
                existing.copy(
                    amount = amount,
                    type = type.value,
                    categoryId = categoryId,
                    note = note,
                    date = date,
                    isRecurring = isRecurring,
                    recurrenceType = recurrenceType,
                )
            )
            existing.remoteId?.let { remoteId ->
                try {
                    val userId = currentUserId()
                    if (userId.isNotEmpty()) {
                        val categoryName = categoryId?.let { categoryDao.getCategoryById(it)?.name }
                        supabaseDataSource.update(
                            remoteId = remoteId,
                            dto = SupabaseTransactionDto(
                                userId = userId,
                                amount = amount,
                                type = type.value,
                                categoryName = categoryName,
                                note = note,
                                date = date,
                                isRecurring = isRecurring,
                                recurrenceType = recurrenceType,
                                createdAt = existing.createdAt,
                            ),
                        )
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Failed to sync updated transaction to Supabase")
                }
            }
        }
    }

    override suspend fun deleteTransaction(id: Long) = withContext(Dispatchers.IO) {
        val entity = transactionDao.getTransactionById(id)
        entity?.remoteId?.let { remoteId ->
            try {
                supabaseDataSource.delete(remoteId)
            } catch (e: Exception) {
                Timber.e(e, "Failed to delete transaction from Supabase")
            }
        }
        transactionDao.deleteById(id)
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

    override suspend fun syncFromRemote() = withContext(Dispatchers.IO) {
        val userId = currentUserId().takeIf { it.isNotEmpty() } ?: return@withContext
        try {
            val remoteTransactions = supabaseDataSource.fetchAllForUser(userId)
            remoteTransactions.forEach { dto ->
                val remoteId = dto.id ?: return@forEach
                if (transactionDao.getByRemoteId(remoteId) == null) {
                    val category = dto.categoryName?.let { categoryDao.getCategoryByName(it) }
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
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to sync transactions from Supabase")
        }
    }

    private fun currentUserId(): String {
        return supabaseClient.auth.currentSessionOrNull()?.user?.id ?: ""
    }
}
