package com.ayush.sms.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ayush.common.utils.formatAmount
import com.ayush.sms.domain.classifier.SmsCategorySlugs
import com.ayush.sms.domain.parser.PendingTransaction
import com.ayush.sms.domain.parser.TransactionType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val dateFormat = SimpleDateFormat("d MMM, h:mm a", Locale.getDefault())


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

    fun toDomain(): PendingTransaction {
        return PendingTransaction(
            id = id,
            amount = amount,
            type = TransactionType.valueOf(type),
            merchant = merchant?.takeIf { it.isNotBlank() } ?: "Unknown merchant",
            suggestedCategoryId = suggestedCategoryId,
            accountLastFour = accountLastFour,
            smsTimestamp = smsTimestamp,
            rawSnippet = rawSnippet,
            sender = sender,
            finalConfidence = finalConfidence,
            state = PendingTransaction.State.valueOf(state),
            amountFormatted = "₹${formatAmount(amount)}",
            categoryLabel = SmsCategorySlugs.nameFor(suggestedCategoryId),
            accountLabel = accountLastFour?.let { "•• $it" },
            dateFormatted = dateFormat.format(Date(smsTimestamp))
        )
    }
}