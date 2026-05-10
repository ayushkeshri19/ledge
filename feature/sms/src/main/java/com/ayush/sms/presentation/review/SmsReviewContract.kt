package com.ayush.sms.presentation.review

import androidx.compose.runtime.Stable
import com.ayush.sms.domain.parser.PendingTransaction

@Stable
data class SmsReviewState(
    val items: List<PendingTransaction> = emptyList(),
    val pendingActions: Map<Long, PendingAction> = emptyMap(),
    val isLoading: Boolean = true
) {
    val visibleItems: List<PendingTransaction>
        get() = items.filterNot { it.id in pendingActions.keys }

    val canBulkAct: Boolean
        get() = visibleItems.isNotEmpty()
}

enum class PendingAction { CONFIRM, DISMISS }

sealed interface SmsReviewEvent {
    data class Confirm(val id: Long) : SmsReviewEvent
    data class Dismiss(val id: Long) : SmsReviewEvent
    data class Edit(val id: Long) : SmsReviewEvent
    data class Undo(val ids: Set<Long>) : SmsReviewEvent
    data object ConfirmAll : SmsReviewEvent
    data object DismissAll : SmsReviewEvent
}

sealed interface SmsReviewSideEffect {
    data class ShowSingleUndo(val id: Long, val action: PendingAction) : SmsReviewSideEffect
    data class ShowBulkUndo(val ids: Set<Long>, val action: PendingAction) : SmsReviewSideEffect
    data class ShowError(val message: String) : SmsReviewSideEffect
    data class NavigateToEdit(val pendingId: Long) : SmsReviewSideEffect
}