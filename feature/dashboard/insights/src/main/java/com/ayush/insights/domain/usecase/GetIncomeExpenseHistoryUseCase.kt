package com.ayush.insights.domain.usecase

import com.ayush.insights.domain.models.IncomeExpenseBucket
import com.ayush.insights.domain.repository.InsightsRepository
import com.ayush.insights.utils.bucketIncomeExpenseByMonth
import com.ayush.insights.utils.computeRange
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetIncomeExpenseHistoryUseCase @Inject constructor(
    private val insightsRepository: InsightsRepository
) {
    operator fun invoke(monthsBack: Int = 6): Flow<List<IncomeExpenseBucket>> {
        val (start, end) = computeRange(monthsBack)
        return insightsRepository.observeTransactions(start, end).map { txns ->
            bucketIncomeExpenseByMonth(txns, monthsBack)
        }
    }
}
