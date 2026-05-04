package com.ayush.sms.domain.parser

sealed interface ParseResult {
    data class Success(val parsedTransaction: ParsedTransaction) : ParseResult
    data class Miss(val reason: ParseFailureReason) : ParseResult

    data object Rejected : ParseResult
}

enum class ParseFailureReason {
    NO_RULE_MATCHED, MISSING_AMOUNT, MISSING_TYPE, EXCEPTION;
}