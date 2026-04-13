package com.ayush.home.domain.repository

import com.ayush.common.models.User
import com.ayush.database.data.CategorySpendTuple
import com.ayush.database.data.TransactionWithCategory

interface HomeRepository {
    suspend fun getCurrentUser(): User?
    suspend fun getTotalIncome(startDate: Long, endDate: Long): Double
    suspend fun getTotalExpense(startDate: Long, endDate: Long): Double
    suspend fun getExpensesByCategory(startDate: Long, endDate: Long): List<CategorySpendTuple>
    suspend fun getRecentTransactions(limit: Int): List<TransactionWithCategory>
}
