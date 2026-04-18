package com.ayush.insights.domain.usecase

import com.ayush.common.models.TimePeriod
import com.ayush.insights.domain.models.SpendBucket
import com.ayush.insights.domain.repository.InsightsRepository
import com.ayush.insights.utils.bucketSpendByDayOfWeek
import com.ayush.insights.utils.bucketSpendByMonth
import com.ayush.insights.utils.bucketSpendByWeekOfMonth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetSpendTimeSeriesUseCase @Inject constructor(
    private val insightsRepository: InsightsRepository
) {
    operator fun invoke(period: TimePeriod): Flow<List<SpendBucket>> {
        val (start, end) = period.dateRange()
        return insightsRepository.observeTransactions(start, end).map { txns ->
            val expenses = txns.filter { it.transaction.type == "expense" }
            when (period) {
                TimePeriod.WEEK -> bucketSpendByDayOfWeek(expenses)
                TimePeriod.MONTH -> bucketSpendByWeekOfMonth(expenses, start)
                TimePeriod.YEAR -> bucketSpendByMonth(expenses)
            }
        }
    }
}
