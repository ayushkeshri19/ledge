package com.ayush.sms.domain.classifier

object SmsCategorySlugs {
    val displayNames: Map<String, String> = mapOf(
        "FOOD" to "Food & Dining",
        "TRANSPORT" to "Transport",
        "ENTERTAINMENT" to "Entertainment",
        "SHOPPING" to "Shopping",
        "HEALTH" to "Healthcare",
        "BILLS" to "Utilities",
        "HOUSING" to "Housing",
        "SUBSCRIPTIONS" to "Subscriptions",
        "INVESTMENTS" to "Investments",
        "EDUCATION" to "Education",
        "PERSONAL_CARE" to "Personal Care",
        "INSURANCE" to "Insurance",
        "GIFTS" to "Gifts & Donations"
    )

    fun nameFor(slug: String?): String? = slug?.let { displayNames[it] }
}
