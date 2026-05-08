package com.ayush.sms.domain.parser.extractors

object MerchantExtractors {

    fun toThenOn(): MerchantExtractor = MerchantExtractor { body ->
        Regex("""\bto\s+([A-Z0-9 .\-&*/]{3,60}?)\s+on\s+\d""", RegexOption.IGNORE_CASE)
            .find(body)?.groupValues?.get(1)?.trim()
    }

    fun atThenOn(): MerchantExtractor = MerchantExtractor { body ->
        Regex(
            """\bat\s+([A-Z0-9 .\-&*/]{3,60}?)(?:\s+on|\s+ref|\.|,|$)""",
            RegexOption.IGNORE_CASE
        ).find(body)?.groupValues?.get(1)?.trim()
    }

    fun trfToRefno(): MerchantExtractor = MerchantExtractor { body ->
        Regex("""\btrf\s+to\s+([A-Z0-9 .\-&*/]{3,60}?)\s+(?:refno|ref)""", RegexOption.IGNORE_CASE)
            .find(body)?.groupValues?.get(1)?.trim()
    }

    fun commaDelimitedAfterDate(): MerchantExtractor = MerchantExtractor { body ->
        Regex("""\bon\s+[\d\-/A-Za-z]+\s*,\s*([A-Z0-9 .\-&*/]{3,60}?)\s*,""", RegexOption.IGNORE_CASE)
            .find(body)?.groupValues?.get(1)?.trim()
    }

    fun semicolonCredited(): MerchantExtractor = MerchantExtractor { body ->
        Regex(""";\s+([A-Z0-9 .\-&*/]{3,60}?)\s+credited""", RegexOption.IGNORE_CASE)
            .find(body)?.groupValues?.get(1)?.trim()
    }

    fun forMerchant(): MerchantExtractor = MerchantExtractor { body ->
        Regex("""\bfor\s+([A-Z0-9 .\-&*/]{3,60}?)(?:\.|,|$)""", RegexOption.IGNORE_CASE)
            .find(body)?.groupValues?.get(1)?.trim()
    }

    fun upiVpa(): MerchantExtractor = MerchantExtractor { body ->
        Regex("""(?:to|from)\s+([\w.\-]+@[\w.\-]+)""", RegexOption.IGNORE_CASE)
            .find(body)?.groupValues?.get(1)?.trim()
    }
}
