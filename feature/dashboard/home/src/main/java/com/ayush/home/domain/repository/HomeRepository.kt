package com.ayush.home.domain.repository

import com.ayush.common.models.User
import com.ayush.database.data.CategorySpendTuple
import com.ayush.database.data.TransactionWithCategory
import kotlinx.coroutines.flow.Flow

interface HomeRepository {
    suspend fun getCurrentUser(): User?
    fun observeTotalIncome(startDate: Long, endDate: Long): Flow<Double>
    fun observeTotalExpense(startDate: Long, endDate: Long): Flow<Double>
    fun observeExpensesByCategory(startDate: Long, endDate: Long): Flow<List<CategorySpendTuple>>
    fun observeRecentTransactions(limit: Int): Flow<List<TransactionWithCategory>>
}
