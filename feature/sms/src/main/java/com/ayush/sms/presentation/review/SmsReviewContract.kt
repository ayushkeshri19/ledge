package com.ayush.sms.presentation.review

import androidx.compose.runtime.Stable
import com.ayush.common.models.Category
import com.ayush.sms.domain.parser.PendingTransaction
import com.ayush.sms.domain.parser.TransactionType

@Stable
data class SmsReviewState(
    val items: List<PendingTransaction> = emptyList(),
    val pendingActions: Map<Long, PendingAction> = emptyMap(),
    val isLoading: Boolean = true,
    val categories: List<Category> = emptyList(),
    val editingPendingId: Long? = null
) {
    val visibleItems: List<PendingTransaction>
        get() = items.filterNot { it.id in pendingActions.keys }

    val canBulkAct: Boolean
        get() = visibleItems.isNotEmpty()

    val editingItem: PendingTransaction?
        get() = editingPendingId?.let { id -> items.firstOrNull { it.id == id } }
}

enum class PendingAction { CONFIRM, DISMISS }

sealed interface SmsReviewEvent {
    data class Confirm(val id: Long) : SmsReviewEvent
    data class Dismiss(val id: Long) : SmsReviewEvent
    data class Edit(val id: Long) : SmsReviewEvent
    data object EditDismissed : SmsReviewEvent
    data class ConfirmEdit(
        val id: Long,
        val amount: Double,
        val type: TransactionType,
        val categoryId: Long?,
        val note: String,
        val date: Long
    ) : SmsReviewEvent

    data class Undo(val ids: Set<Long>) : SmsReviewEvent
    data object ConfirmAll : SmsReviewEvent
    data object DismissAll : SmsReviewEvent
}

sealed interface SmsReviewSideEffect {
    data class ShowSingleUndo(val id: Long, val action: PendingAction) : SmsReviewSideEffect
    data class ShowBulkUndo(val ids: Set<Long>, val action: PendingAction) : SmsReviewSideEffect
    data class ShowError(val message: String) : SmsReviewSideEffect
}