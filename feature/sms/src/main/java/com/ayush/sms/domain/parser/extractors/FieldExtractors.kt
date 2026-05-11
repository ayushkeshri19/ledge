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
    private val withCurrency = Regex(
        """(?:Rs\.?|INR|₹)\s?([0-9,]+(?:\.[0-9]{1,2})?)""",
        RegexOption.IGNORE_CASE
    )
    private val verbPrefixed = Regex(
        """(?:debited|credited|paid|spent|received|sent)\s+(?:by|with|for)\s+([0-9,]+(?:\.[0-9]{1,2})?)""",
        RegexOption.IGNORE_CASE
    )

    override fun extract(body: String): Double? {
        val raw = withCurrency.find(body)?.groupValues?.get(1)
            ?: verbPrefixed.find(body)?.groupValues?.get(1)
            ?: return null
        return raw.replace(",", "").toDoubleOrNull()
    }
}

object StandardTypeExtractor : TypeExtractor {
    private val debitVerbs = listOf(
        "debited", "spent", "paid", "withdrawn", "purchase", "sent", "using", "used"
    )
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