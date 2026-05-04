package com.ayush.sms.domain.parser

import com.ayush.sms.domain.parser.rules.Guards
import javax.inject.Inject

class SMSParser @Inject constructor(
    private val rules: List<ParserRule>
) {
    fun parse(sender: String, body: String, smsTimestamp: Long): ParseResult {
        if (Guards.isOtpOrPromo(body)) return ParseResult.Rejected

        val matchingRule = rules.firstOrNull { rule ->
            rule.senderPattern.containsMatchIn(sender) &&
                    rule.bodyMustContain.all { it.containsMatchIn(body) }
        } ?: return if (Guards.looksFinancial(body)) {
            ParseResult.Miss(ParseFailureReason.NO_RULE_MATCHED)
        } else ParseResult.Rejected

        return try {
            val amount = matchingRule.amountExtractor.extract(body)
                ?: return ParseResult.Miss(ParseFailureReason.MISSING_AMOUNT)
            val type = matchingRule.typeExtractor.extract(body)
                ?: return ParseResult.Miss(ParseFailureReason.MISSING_TYPE)
            val merchant = matchingRule.merchantExtractor.extract(body)
            val acct = matchingRule.accountExtractor.extract(body)

            ParseResult.Success(
                ParsedTransaction(
                    amount = amount,
                    type = type,
                    merchant = merchant,
                    accountLastFour = acct,
                    smsTimestamp = smsTimestamp,
                    rawSnippet = body.take(120),
                    sender = sender,
                    parserConfidence = matchingRule.baseConfidence
                )
            )
        } catch (t: Throwable) {
            ParseResult.Miss(ParseFailureReason.EXCEPTION)
        }
    }
}