package com.ayush.transactions.domain

import com.ayush.common.result.ApiResult
import com.ayush.common.transactions.AutoDetectedTransactionInput
import com.ayush.common.transactions.AutoDetectedTransactionWriter
import com.ayush.common.transactions.AutoDetectedType
import com.ayush.transactions.domain.models.TransactionType
import com.ayush.transactions.domain.usecase.AddTransactionParams
import com.ayush.transactions.domain.usecase.AddTransactionUseCase
import javax.inject.Inject

class AutoDetectedTransactionWriterImpl @Inject constructor(
    private val addTransactionUseCase: AddTransactionUseCase
) : AutoDetectedTransactionWriter {
    override suspend fun write(input: AutoDetectedTransactionInput): Result<Unit> = runCatching {
        val result = addTransactionUseCase(
            AddTransactionParams(
                amount = input.amount,
                type = input.type.toDomain(),
                categoryId = input.categoryId,
                note = input.note?.takeIf { it.isNotBlank() }
                    ?: input.merchant?.takeIf { it.isNotBlank() }
                    ?: "Auto-detected",
                date = input.date,
                isAutoDetected = true
            )
        )
        if (result is ApiResult.Error) error(result.message)
    }

    private fun AutoDetectedType.toDomain(): TransactionType = when (this) {
        AutoDetectedType.DEBIT -> TransactionType.EXPENSE
        AutoDetectedType.CREDIT -> TransactionType.INCOME
    }
}
