package com.ayush.common.transactions

interface AutoDetectedTransactionWriter {
    suspend fun write(input: AutoDetectedTransactionInput): Result<Unit>
}

data class AutoDetectedTransactionInput(
    val amount: Double,
    val type: String,
    val categoryId: String?,
    val merchant: String?,
    val date: Long,
    val note: String?
)