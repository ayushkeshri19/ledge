package com.ayush.budget.data.remote

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class SupabaseBudgetDto(
    @EncodeDefault(EncodeDefault.Mode.NEVER) val id: String? = null,
    @SerialName("user_id") val userId: String,
    @SerialName("category_name") val categoryName: String? = null,
    val amount: Double,
    @SerialName("warning_threshold") val warningThreshold: Int,
    @SerialName("created_at") val createdAt: Long,
)
