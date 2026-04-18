package com.ayush.insights.domain.repository

import com.ayush.database.data.CategorySpendTuple
import com.ayush.database.data.TransactionWithCategory
import kotlinx.coroutines.flow.Flow

interface InsightsRepository {
    fun observeExpensesByCategory(startDate: Long, endDate: Long): Flow<List<CategorySpendTuple>>
    fun observeTransactions(startDate: Long, endLong: Long): Flow<List<TransactionWithCategory>>
}
