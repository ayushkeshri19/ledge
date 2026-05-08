package com.ayush.transactions.domain.usecase

import com.ayush.common.result.ApiResult
import com.ayush.transactions.domain.models.TransactionType
import com.ayush.transactions.domain.repository.TransactionRepository
import javax.inject.Inject

class AddTransactionUseCase @Inject constructor(
    private val repository: TransactionRepository
) {
    suspend operator fun invoke(
        params: AddTransactionParams
    ): ApiResult<Long> {
        if (params.amount <= 0) return ApiResult.Error("Amount must be greater than 0")
        if (params.note.isBlank()) return ApiResult.Error("Note cannot be empty")

        return try {
            val id = repository.addTransaction(
                amount = params.amount,
                type = params.type,
                categoryId = params.categoryId,
                note = params.note,
                date = params.date,
                isRecurring = params.isRecurring,
                recurrenceType = params.recurrenceType,
                isAutoDetected = params.isAutoDetected
            )
            ApiResult.Success(id)
        } catch (e: Exception) {
            ApiResult.Error("Failed to add transaction", e)
        }
    }
}

data class AddTransactionParams(
    val amount: Double,
    val type: TransactionType,
    val categoryId: Long?,
    val note: String,
    val date: Long,
    val isRecurring: Boolean = false,
    val recurrenceType: String? = null,
    val isAutoDetected: Boolean = false
)
