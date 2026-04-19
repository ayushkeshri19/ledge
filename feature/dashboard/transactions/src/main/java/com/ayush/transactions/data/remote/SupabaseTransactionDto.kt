package com.ayush.transactions.data.remote

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class SupabaseTransactionDto(
    @EncodeDefault(EncodeDefault.Mode.NEVER) val id: String? = null,
    @SerialName("user_id") val userId: String,
    val amount: Double,
    val type: String,
    @SerialName("category_name") val categoryName: String? = null,
    val note: String,
    val date: Long,
    @SerialName("is_recurring") val isRecurring: Boolean = false,
    @SerialName("recurrence_type") val recurrenceType: String? = null,
    @SerialName("created_at") val createdAt: Long,
    @SerialName("parent_remote_id") val parentRemoteId: String? = null,
    @SerialName("last_executed_date") val lastExecutedDate: Long? = null
)
