package com.ayush.transactions.data.repository

import com.ayush.database.dao.CategoryDao
import com.ayush.database.dao.TransactionDao
import com.ayush.database.data.DefaultCategories
import com.ayush.database.data.TransactionEntity
import com.ayush.transactions.domain.models.Category
import com.ayush.transactions.domain.models.Transaction
import com.ayush.transactions.domain.models.TransactionType
import com.ayush.transactions.domain.models.toDomain
import com.ayush.transactions.domain.repository.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao,
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
                    recurrenceType = com.ayush.transactions.domain.models.RecurrenceType.fromValue(entity.recurrenceType),
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
        transactionDao.insert(
            TransactionEntity(
                amount = amount,
                type = type.value,
                categoryId = categoryId,
                note = note,
                date = date,
                isRecurring = isRecurring,
                recurrenceType = recurrenceType,
            )
        )
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
    ) = withContext(Dispatchers.IO) {
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
    }

    override suspend fun deleteTransaction(id: Long) = withContext(Dispatchers.IO) {
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
}
