package com.ayush.home.domain.usecase

import com.ayush.home.domain.models.CategorySpend
import com.ayush.home.domain.models.TimePeriod
import com.ayush.home.domain.repository.HomeRepository
import com.ayush.ui.utils.hexToColor
import javax.inject.Inject

class GetCategorySpendingUseCase @Inject constructor(
    private val repository: HomeRepository,
) {
    suspend operator fun invoke(period: TimePeriod): List<CategorySpend> {
        val (start, end) = period.dateRange()
        val tuples = repository.getExpensesByCategory(start, end)
        val totalExpense = tuples.sumOf { it.totalAmount }
        if (totalExpense == 0.0) return emptyList()

        return tuples.map { tuple ->
            CategorySpend(
                categoryId = tuple.categoryId,
                categoryName = tuple.categoryName ?: "Uncategorized",
                color = tuple.categoryColorHex?.let { hexToColor(it) }
                    ?: hexToColor("#D1D8E0"),
                amount = tuple.totalAmount,
                percentage = (tuple.totalAmount / totalExpense).toFloat(),
            )
        }
    }
}
