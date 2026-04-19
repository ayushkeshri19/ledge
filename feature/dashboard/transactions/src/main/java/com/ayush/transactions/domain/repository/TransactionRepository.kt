package com.ayush.transactions.domain.repository

import androidx.paging.PagingData
import com.ayush.database.data.TransactionEntity
import com.ayush.transactions.domain.models.Category
import com.ayush.transactions.domain.models.Transaction
import com.ayush.transactions.domain.models.TransactionType
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getAllTransactions(): Flow<PagingData<Transaction>>
    fun searchTransactions(query: String): Flow<PagingData<Transaction>>
    fun getFilteredTransactions(
        startDate: Long,
        endDate: Long,
        type: TransactionType? = null,
        categoryId: Long? = null,
        minAmount: Double? = null,
        maxAmount: Double? = null,
    ): Flow<PagingData<Transaction>>
    suspend fun getTransactionById(id: Long): Transaction?
    suspend fun addTransaction(
        amount: Double,
        type: TransactionType,
        categoryId: Long?,
        note: String,
        date: Long,
        isRecurring: Boolean = false,
        recurrenceType: String? = null,
    ): Long
    suspend fun updateTransaction(
        id: Long,
        amount: Double,
        type: TransactionType,
        categoryId: Long?,
        note: String,
        date: Long,
        isRecurring: Boolean = false,
        recurrenceType: String? = null,
    )
    suspend fun deleteTransaction(id: Long)
    fun getAllCategories(): Flow<List<Category>>
    suspend fun ensureDefaultCategories()
    suspend fun syncFromRemote()
    suspend fun getPendingSync(): List<TransactionEntity>
    suspend fun currentUserId(): String?
    suspend fun pushCreate(transaction: TransactionEntity, userId: String)
    suspend fun pushUpdate(transaction: TransactionEntity, userId: String)
    suspend fun pushDelete(transaction: TransactionEntity)
    suspend fun getRecurringTransactions(): List<Transaction>
    suspend fun createRecurringInstance(template: Transaction, date: Long)
    suspend fun updateLastExecutedDate(id: Long, date: Long)
    suspend fun stopRecurringSeries(templateId: Long)
}
