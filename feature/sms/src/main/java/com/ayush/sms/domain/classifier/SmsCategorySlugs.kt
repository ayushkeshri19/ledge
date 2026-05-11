package com.ayush.sms.domain.classifier

object SmsCategorySlugs {
    val displayNames: Map<String, String> = mapOf(
        "FOOD" to "Food & Dining",
        "TRANSPORT" to "Transport",
        "ENTERTAINMENT" to "Entertainment",
        "SHOPPING" to "Shopping",
        "HEALTH" to "Healthcare",
        "BILLS" to "Utilities"
    )

    fun nameFor(slug: String?): String? = slug?.let { displayNames[it] }
}
