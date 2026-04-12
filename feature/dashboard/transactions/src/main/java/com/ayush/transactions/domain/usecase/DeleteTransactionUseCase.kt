package com.ayush.transactions.domain.usecase

import com.ayush.common.result.ApiResult
import com.ayush.transactions.domain.repository.TransactionRepository
import javax.inject.Inject

class DeleteTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository,
) {
    suspend operator fun invoke(id: Long): ApiResult<Unit> {
        return try {
            repository.deleteTransaction(id)
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error("Failed to delete transaction", e)
        }
    }
}
