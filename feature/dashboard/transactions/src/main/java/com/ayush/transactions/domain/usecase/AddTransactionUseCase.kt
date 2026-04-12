package com.ayush.transactions.domain.usecase

import com.ayush.common.result.ApiResult
import com.ayush.transactions.domain.models.TransactionType
import com.ayush.transactions.domain.repository.TransactionRepository
import javax.inject.Inject

class AddTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository,
) {
    suspend operator fun invoke(
        amount: Double,
        type: TransactionType,
        categoryId: Long?,
        note: String,
        date: Long,
        isRecurring: Boolean = false,
        recurrenceType: String? = null,
    ): ApiResult<Long> {
        if (amount <= 0) return ApiResult.Error("Amount must be greater than 0")
        if (note.isBlank()) return ApiResult.Error("Note cannot be empty")

        return try {
            val id = repository.addTransaction(
                amount = amount,
                type = type,
                categoryId = categoryId,
                note = note,
                date = date,
                isRecurring = isRecurring,
                recurrenceType = recurrenceType,
            )
            ApiResult.Success(id)
        } catch (e: Exception) {
            ApiResult.Error("Failed to add transaction", e)
        }
    }
}
