package com.ayush.home.domain.usecase

import com.ayush.home.domain.models.RecentTransaction
import com.ayush.home.domain.repository.HomeRepository
import com.ayush.ui.utils.hexToColor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetRecentTransactionsUseCase @Inject constructor(
    private val repository: HomeRepository,
) {
    operator fun invoke(limit: Int = 5): Flow<List<RecentTransaction>> {
        return repository.observeRecentTransactions(limit).map { list ->
            list.map { twc ->
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
}
