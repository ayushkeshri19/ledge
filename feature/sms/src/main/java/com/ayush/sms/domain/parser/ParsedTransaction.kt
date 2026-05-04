package com.ayush.sms.domain.parser

data class ParsedTransaction(
    val amount: Double,
    val merchant: String?,
    val parserConfidence: Float,
    val accountLastFour: String?,
    val type: TransactionType,
    val smsTimestamp: Long,
    val rawSnippet: String,
    val sender: String
)

enum class TransactionType {
    DEBIT, CREDIT;
}