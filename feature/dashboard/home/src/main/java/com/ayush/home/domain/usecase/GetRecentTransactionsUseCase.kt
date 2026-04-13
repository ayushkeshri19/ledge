package com.ayush.home.domain.usecase

import com.ayush.home.domain.models.RecentTransaction
import com.ayush.home.domain.repository.HomeRepository
import com.ayush.ui.utils.hexToColor
import javax.inject.Inject

class GetRecentTransactionsUseCase @Inject constructor(
    private val repository: HomeRepository,
) {
    suspend operator fun invoke(limit: Int = 5): List<RecentTransaction> {
        return repository.getRecentTransactions(limit).map { twc ->
            RecentTransaction(
                id = twc.transaction.id,
                amount = twc.transaction.amount,
                isExpense = twc.transaction.type == "expense",
                note = twc.transaction.note,
                categoryName = twc.category?.name,
                categoryColor = twc.category?.colorHex?.let { hexToColor(it) },
                date = twc.transaction.date,
            )
        }
    }
}
