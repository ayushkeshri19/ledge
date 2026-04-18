package com.ayush.insights.data.repository

import com.ayush.database.dao.TransactionDao
import com.ayush.database.data.CategorySpendTuple
import com.ayush.database.data.TransactionWithCategory
import com.ayush.insights.domain.repository.InsightsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InsightsRepositoryImpl @Inject constructor(
    private val transactionDao: TransactionDao,
) : InsightsRepository {

    override fun observeExpensesByCategory(
        startDate: Long,
        endDate: Long,
    ): Flow<List<CategorySpendTuple>> =
        transactionDao.observeExpensesByCategory(startDate, endDate)

    override fun observeTransactions(
        startDate: Long,
        endLong: Long
    ): Flow<List<TransactionWithCategory>> {
        return transactionDao.getTransactionsByDateRange(startDate, endLong)
    }
}
