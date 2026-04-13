package com.ayush.transactions.domain.usecase

import androidx.paging.PagingData
import com.ayush.transactions.domain.models.Transaction
import com.ayush.transactions.domain.models.TransactionType
import com.ayush.transactions.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTransactionsUseCase @Inject constructor(
    private val repository: TransactionRepository,
) {
    operator fun invoke(): Flow<PagingData<Transaction>> = repository.getAllTransactions()

    fun search(query: String): Flow<PagingData<Transaction>> = repository.searchTransactions(query)

    fun filter(
        startDate: Long,
        endDate: Long,
        type: TransactionType? = null,
        categoryId: Long? = null,
    ): Flow<PagingData<Transaction>> = repository.getFilteredTransactions(
        startDate = startDate,
        endDate = endDate,
        type = type,
        categoryId = categoryId,
    )
}
