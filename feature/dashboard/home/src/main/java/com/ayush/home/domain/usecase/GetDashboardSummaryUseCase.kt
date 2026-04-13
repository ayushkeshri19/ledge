package com.ayush.home.domain.usecase

import com.ayush.home.domain.models.DashboardSummary
import com.ayush.home.domain.models.TimePeriod
import com.ayush.home.domain.repository.HomeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetDashboardSummaryUseCase @Inject constructor(
    private val repository: HomeRepository,
) {
    operator fun invoke(period: TimePeriod): Flow<DashboardSummary> {
        val (start, end) = period.dateRange()
        return combine(
            repository.observeTotalIncome(start, end),
            repository.observeTotalExpense(start, end),
        ) { income, expense ->
            DashboardSummary(totalIncome = income, totalExpense = expense)
        }
    }
}
