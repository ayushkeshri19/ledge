package com.ayush.sms.domain.parser.rules

import com.ayush.sms.domain.parser.ParserRule
import com.ayush.sms.domain.parser.extractors.MerchantExtractor
import com.ayush.sms.domain.parser.extractors.MerchantExtractors

val HdfcRule = ParserRule(
    name = "HDFC",
    senderPattern = Regex("""HDFCBK""", RegexOption.IGNORE_CASE),
    bodyMustContain = listOf(Regex("""hdfc""", RegexOption.IGNORE_CASE)),
    merchantExtractor = MerchantExtractor { body ->
        MerchantExtractors.toThenOn().extract(body)
            ?: MerchantExtractors.atThenOn().extract(body)
    }
)

val IciciRule = ParserRule(
    name = "ICICI",
    senderPattern = Regex("""ICICIB""", RegexOption.IGNORE_CASE),
    bodyMustContain = listOf(Regex("""icici""", RegexOption.IGNORE_CASE)),
    merchantExtractor = MerchantExtractor { body ->
        MerchantExtractors.semicolonCredited().extract(body)
            ?: MerchantExtractors.toThenOn().extract(body)
            ?: MerchantExtractors.atThenOn().extract(body)
            ?: MerchantExtractors.upiVpa().extract(body)
    }
)

val SbiCardRule = ParserRule(
    name = "SBI_CARD",
    senderPattern = Regex("""SBICRD""", RegexOption.IGNORE_CASE),
    bodyMustContain = listOf(Regex("""sbi.*card""", RegexOption.IGNORE_CASE)),
    merchantExtractor = MerchantExtractors.atThenOn()
)

val SbiRule = ParserRule(
    name = "SBI",
    senderPattern = Regex("""SBI(?:INB|PSG|UPI)?""", RegexOption.IGNORE_CASE),
    bodyMustContain = listOf(Regex("""sbi""", RegexOption.IGNORE_CASE)),
    merchantExtractor = MerchantExtractor { body ->
        MerchantExtractors.trfToRefno().extract(body)
            ?: MerchantExtractors.atThenOn().extract(body)
    }
)

val AxisRule = ParserRule(
    name = "AXIS",
    senderPattern = Regex("""AXISBK""", RegexOption.IGNORE_CASE),
    bodyMustContain = listOf(Regex("""axis""", RegexOption.IGNORE_CASE)),
    merchantExtractor = MerchantExtractor { body ->
        MerchantExtractors.commaDelimitedAfterDate().extract(body)
            ?: MerchantExtractors.atThenOn().extract(body)
            ?: MerchantExtractors.forMerchant().extract(body)
    }
)

val KotakRule = ParserRule(
    name = "KOTAK",
    senderPattern = Regex("""KOTAKB""", RegexOption.IGNORE_CASE),
    bodyMustContain = listOf(Regex("""kotak""", RegexOption.IGNORE_CASE)),
    merchantExtractor = MerchantExtractor { body ->
        MerchantExtractors.toThenOn().extract(body)
            ?: MerchantExtractors.atThenOn().extract(body)
    }
)

val PnbRule = ParserRule(
    name = "PNB",
    senderPattern = Regex("""PNBSMS""", RegexOption.IGNORE_CASE),
    bodyMustContain = listOf(Regex("""pnb""", RegexOption.IGNORE_CASE)),
    merchantExtractor = MerchantExtractor { body ->
        MerchantExtractors.toThenOn().extract(body)
            ?: MerchantExtractors.atThenOn().extract(body)
    }
)

val BobRule = ParserRule(
    name = "BOB",
    senderPattern = Regex("""BOB(?:SMS|TXN)""", RegexOption.IGNORE_CASE),
    bodyMustContain = listOf(Regex("""baroda|bob""", RegexOption.IGNORE_CASE)),
    merchantExtractor = MerchantExtractor { body ->
        MerchantExtractors.toThenOn().extract(body)
            ?: MerchantExtractors.atThenOn().extract(body)
    }
)

val YesBankRule = ParserRule(
    name = "YESBANK",
    senderPattern = Regex("""YESBNK""", RegexOption.IGNORE_CASE),
    bodyMustContain = listOf(Regex("""yes\s?bank""", RegexOption.IGNORE_CASE)),
    merchantExtractor = MerchantExtractor { body ->
        MerchantExtractors.forMerchant().extract(body)
            ?: MerchantExtractors.toThenOn().extract(body)
            ?: MerchantExtractors.atThenOn().extract(body)
    }
)

val IndusIndRule = ParserRule(
    name = "INDUSIND",
    senderPattern = Regex("""INDBNK|INDUSB""", RegexOption.IGNORE_CASE),
    bodyMustContain = listOf(Regex("""indusind""", RegexOption.IGNORE_CASE)),
    merchantExtractor = MerchantExtractor { body ->
        MerchantExtractors.toThenOn().extract(body)
            ?: MerchantExtractors.atThenOn().extract(body)
    }
)

val CanaraRule = ParserRule(
    name = "CANARA",
    senderPattern = Regex("""CANBNK""", RegexOption.IGNORE_CASE),
    bodyMustContain = listOf(Regex("""canara""", RegexOption.IGNORE_CASE)),
    merchantExtractor = MerchantExtractor { body ->
        MerchantExtractors.toThenOn().extract(body)
            ?: MerchantExtractors.atThenOn().extract(body)
    }
)

val UpiCreditRule = ParserRule(
    name = "UPI_CREDIT",
    senderPattern = Regex(""".*"""),
    bodyMustContain = listOf(
        Regex("""received|credited""", RegexOption.IGNORE_CASE),
        Regex("""upi""", RegexOption.IGNORE_CASE)
    ),
    merchantExtractor = MerchantExtractor { body ->
        MerchantExtractors.upiVpa().extract(body)
            ?: Regex("""from\s+([A-Z0-9 .\-]{3,40}?)(?:\s+via|\s+on|\.|,)""", RegexOption.IGNORE_CASE)
                .find(body)?.groupValues?.get(1)?.trim()
    },
    baseConfidence = 0.85f
)

val UpiDebitRule = ParserRule(
    name = "UPI_DEBIT",
    senderPattern = Regex(""".*"""),
    bodyMustContain = listOf(
        Regex("""debited|paid""", RegexOption.IGNORE_CASE),
        Regex("""upi""", RegexOption.IGNORE_CASE)
    ),
    merchantExtractor = MerchantExtractor { body ->
        MerchantExtractors.upiVpa().extract(body)
            ?: MerchantExtractors.toThenOn().extract(body)
    },
    baseConfidence = 0.85f
)