package com.ayush.sms.domain.parser

import com.ayush.sms.data.local.PendingTransactionEntity

data class PendingTransaction(
    val id: Long,
    val amount: Double,
    val type: TransactionType,
    val merchant: String?,
    val suggestedCategoryId: String?,
    val accountLastFour: String?,
    val smsTimestamp: Long,
    val rawSnippet: String,
    val sender: String,
    val finalConfidence: Float,
    val state: State,
    val amountFormatted: String = "",
    val categoryLabel: String? = null,
    val accountLabel: String? = null,
    val dateFormatted: String = ""
) {
    enum class State { PENDING, CONFIRMED, DISMISSED }

    fun toEntity(): PendingTransactionEntity {
        return PendingTransactionEntity(
            id = id,
            amount = amount,
            type = type.name,
            merchant = merchant,
            suggestedCategoryId = suggestedCategoryId,
            accountLastFour = accountLastFour,
            smsTimestamp = smsTimestamp,
            rawSnippet = rawSnippet,
            sender = sender,
            finalConfidence = finalConfidence,
            state = state.name
        )
    }
}
