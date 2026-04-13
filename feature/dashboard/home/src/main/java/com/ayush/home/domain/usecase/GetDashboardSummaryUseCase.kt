package com.ayush.home.domain.usecase

import com.ayush.home.domain.models.DashboardSummary
import com.ayush.home.domain.models.TimePeriod
import com.ayush.home.domain.repository.HomeRepository
import javax.inject.Inject

class GetDashboardSummaryUseCase @Inject constructor(
    private val homeRepository: HomeRepository
) {
    suspend operator fun invoke(period: TimePeriod): DashboardSummary {
        val (start, end) = period.dateRange()
        return DashboardSummary(
            totalIncome = homeRepository.getTotalIncome(start, end),
            totalExpense = homeRepository.getTotalExpense(start, end)
        )
    }
}