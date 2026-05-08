package com.ayush.sms.domain.parser.rules

object Guards {
    private val otpTokens = Regex("""\b(otp|password|verification\s?code|verify)\b""", RegexOption.IGNORE_CASE)
    private val currencyTokens = Regex("""(?:Rs\.?|INR|₹)""", RegexOption.IGNORE_CASE)
    private val verbTokens = Regex("""\b(debited|credited|spent|received|paid|withdrawn)\b""", RegexOption.IGNORE_CASE)

    fun isOtpOrPromo(body: String): Boolean = otpTokens.containsMatchIn(body)
    fun looksFinancial(body: String): Boolean = verbTokens.containsMatchIn(body) && currencyTokens.containsMatchIn(body)
    fun hasCurrencyToken(body: String): Boolean = currencyTokens.containsMatchIn(body)
    fun hasVerbToken(body: String): Boolean = verbTokens.containsMatchIn(body)
}