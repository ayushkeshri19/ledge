package com.ayush.sms.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_transactions")
data class PendingTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amount: Double,
    val type: String,
    val merchant: String?,
    val suggestedCategoryId: String?,
    val accountLastFour: String?,
    val smsTimestamp: Long,
    val rawSnippet: String,
    val sender: String,
    val finalConfidence: Float,
    val state: String = State.PENDING.name,
    val createdAt: Long = System.currentTimeMillis()
) {
    enum class State { PENDING, CONFIRMED, DISMISSED }
}