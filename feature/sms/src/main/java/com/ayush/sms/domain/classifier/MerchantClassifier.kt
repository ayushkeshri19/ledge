package com.ayush.sms.domain.classifier

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MerchantClassifier @Inject constructor() {
    private val mapping = listOf(
        listOf("swiggy", "zomato", "eatfit") to "FOOD",
        listOf("amazon", "flipkart", "meesho") to "SHOPPING",
        listOf("uber", "ola", "rapido") to "TRANSPORT",
        listOf("netflix", "spotify", "prime", "jiohotstar") to "ENTERTAINMENT",
        listOf("apollo", "medplus", "pharmeasy", "1mg") to "HEALTH",
        listOf("bescom", "tata power", "jio", "airtel", "wbsedcl") to "BILLS"
    )

    fun classify(merchant: String?): Classification {
        if (merchant.isNullOrBlank()) return Classification(null, 0.0f)
        val lower = merchant.lowercase()
        for ((keywords, category) in mapping) {
            if (keywords.any { it == lower }) return Classification(category, 1.0f)
            if (keywords.any { it in lower }) return Classification(category, 0.6f)
        }
        return Classification(null, 0.0f)
    }
}

data class Classification(val categoryId: String?, val classifierConfidence: Float)