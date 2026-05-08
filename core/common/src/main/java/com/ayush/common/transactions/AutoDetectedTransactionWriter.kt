package com.ayush.common.transactions

interface AutoDetectedTransactionWriter {
    suspend fun write(input: AutoDetectedTransactionInput): Result<Unit>
}

data class AutoDetectedTransactionInput(
    val amount: Double,
    val type: AutoDetectedType,
    val categoryId: Long?,
    val merchant: String?,
    val date: Long,
    val note: String?
)

enum class AutoDetectedType { DEBIT, CREDIT }
