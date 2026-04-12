package com.ayush.transactions.domain.usecase

import com.ayush.common.result.ApiResult
import com.ayush.transactions.domain.models.TransactionType
import com.ayush.transactions.domain.repository.TransactionRepository
import javax.inject.Inject

class UpdateTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository,
) {
    suspend operator fun invoke(
        id: Long,
        amount: Double,
        type: TransactionType,
        categoryId: Long?,
        note: String,
        date: Long,
        isRecurring: Boolean = false,
        recurrenceType: String? = null,
    ): ApiResult<Unit> {
        if (amount <= 0) return ApiResult.Error("Amount must be greater than 0")
        if (note.isBlank()) return ApiResult.Error("Note cannot be empty")

        return try {
            repository.updateTransaction(
                id = id,
                amount = amount,
                type = type,
                categoryId = categoryId,
                note = note,
                date = date,
                isRecurring = isRecurring,
                recurrenceType = recurrenceType,
            )
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error("Failed to update transaction", e)
        }
    }
}
