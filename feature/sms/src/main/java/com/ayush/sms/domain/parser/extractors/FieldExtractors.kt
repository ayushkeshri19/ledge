package com.ayush.sms.domain.parser.extractors

import com.ayush.sms.domain.parser.TransactionType

fun interface AmountExtractor {
    fun extract(body: String): Double?
}

fun interface TypeExtractor {
    fun extract(body: String): TransactionType?
}

fun interface MerchantExtractor {
    fun extract(body: String): String?
}

fun interface AccountExtractor {
    fun extract(body: String): String?
}

object StandardAmountExtractor : AmountExtractor {
    private val regex = Regex("""(?:Rs\.?|INR|₹)\s?([0-9,]+(?:\.[0-9]{1,2})?)""", RegexOption.IGNORE_CASE)
    override fun extract(body: String): Double? =
        regex.find(body)?.groupValues?.get(1)?.replace(",", "")?.toDoubleOrNull()
}

object StandardTypeExtractor : TypeExtractor {
    private val debitVerbs = listOf("debited", "spent", "paid", "withdrawn", "purchase")
    private val creditVerbs = listOf("credited", "received", "deposited", "refund")
    override fun extract(body: String): TransactionType? {
        val lower = body.lowercase()
        return when {
            debitVerbs.any { it in lower } -> TransactionType.DEBIT
            creditVerbs.any { it in lower } -> TransactionType.CREDIT
            else -> null
        }
    }
}

object StandardAccountExtractor : AccountExtractor {
    private val regex = Regex(
        """(?:a/c|acct|account|card|cc)\s?(?:no\.?\s?)?(?:ending\s?)?\*{0,4}([0-9X*]{4,})""",
        RegexOption.IGNORE_CASE
    )
    override fun extract(body: String): String? =
        regex.find(body)?.groupValues?.get(1)?.takeLast(4)
}