package com.ayush.home.domain.usecase

import com.ayush.home.domain.models.CategorySpend
import com.ayush.home.domain.models.TimePeriod
import com.ayush.home.domain.repository.HomeRepository
import com.ayush.ui.utils.hexToColor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetCategorySpendingUseCase @Inject constructor(
    private val repository: HomeRepository,
) {
    operator fun invoke(period: TimePeriod): Flow<List<CategorySpend>> {
        val (start, end) = period.dateRange()
        return repository.observeExpensesByCategory(start, end).map { tuples ->
            val totalExpense = tuples.sumOf { it.totalAmount }
            if (totalExpense == 0.0) emptyList()
            else tuples.map { tuple ->
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
}
