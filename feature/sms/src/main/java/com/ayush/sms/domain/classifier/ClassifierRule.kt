package com.ayush.sms.domain.classifier

data class ClassifierRule(
    val keyword: String,
    val categorySlug: String,
    val matchType: MatchType,
    val priority: Int,
    val source: Source
)

enum class MatchType {
    SUBSTRING, EXACT;

    companion object {
        fun fromValue(value: String): MatchType =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: SUBSTRING
    }
}

enum class Source {
    CURATED, AGGREGATOR_DEFAULT, USER_CORRECTION;

    companion object {
        fun fromValue(value: String): Source =
            entries.firstOrNull { it.name.equals(value, ignoreCase = true) } ?: CURATED
    }
}
