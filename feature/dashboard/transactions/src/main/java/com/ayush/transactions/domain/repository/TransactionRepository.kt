package com.ayush.transactions.domain.repository

import com.ayush.transactions.domain.models.Category
import com.ayush.transactions.domain.models.Transaction
import com.ayush.transactions.domain.models.TransactionType
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getAllTransactions(): Flow<List<Transaction>>
    fun searchTransactions(query: String): Flow<List<Transaction>>
    fun getFilteredTransactions(
        startDate: Long,
        endDate: Long,
        type: TransactionType? = null,
        categoryId: Long? = null,
        minAmount: Double? = null,
        maxAmount: Double? = null,
    ): Flow<List<Transaction>>

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
}
