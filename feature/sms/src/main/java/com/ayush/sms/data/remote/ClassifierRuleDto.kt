package com.ayush.sms.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ClassifierRuleDto(
    val id: String,
    val keyword: String,
    @SerialName("category_slug") val categorySlug: String,
    @SerialName("match_type") val matchType: String,
    val enabled: Boolean,
    val priority: Int,
    val source: String
)
