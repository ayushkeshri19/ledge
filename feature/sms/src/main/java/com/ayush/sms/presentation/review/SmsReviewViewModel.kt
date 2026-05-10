package com.ayush.sms.presentation.review

import androidx.lifecycle.viewModelScope
import com.ayush.sms.domain.usecase.ConfirmPendingTransactionUseCase
import com.ayush.sms.domain.usecase.DismissPendingTransactionUseCase
import com.ayush.sms.domain.usecase.ObservePendingTransactionsUseCase
import com.ayush.ui.base.BaseMviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val UNDO_WINDOW_MS: Long = 4_500L

@HiltViewModel
class SmsReviewViewModel @Inject constructor(
    observePendingUseCase: ObservePendingTransactionsUseCase,
    private val confirmUseCase: ConfirmPendingTransactionUseCase,
    private val dismissUseCase: DismissPendingTransactionUseCase
) : BaseMviViewModel<SmsReviewEvent, SmsReviewState, SmsReviewSideEffect>(
    initialState = SmsReviewState()
) {

    private val pendingJobs = mutableMapOf<Set<Long>, Job>()

    init {
        observePendingUseCase()
            .onEach { txns ->
                setState { copy(items = txns, isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    override fun onEvent(event: SmsReviewEvent) {
        when (event) {
            is SmsReviewEvent.Confirm -> scheduleSingle(id = event.id, action = PendingAction.CONFIRM)
            is SmsReviewEvent.Dismiss -> scheduleSingle(id = event.id, action = PendingAction.DISMISS)
            is SmsReviewEvent.Edit -> sendSideEffect(SmsReviewSideEffect.NavigateToEdit(event.id))
            is SmsReviewEvent.Undo -> undo(event.ids)
            SmsReviewEvent.ConfirmAll -> scheduleBulk(PendingAction.CONFIRM)
            SmsReviewEvent.DismissAll -> scheduleBulk(PendingAction.DISMISS)
        }
    }

    private fun scheduleSingle(id: Long, action: PendingAction) {
        val ids = setOf(id)
        veil(ids, action)
        sendSideEffect(SmsReviewSideEffect.ShowSingleUndo(id, action))
        startCommitJob(ids, action)
    }

    private fun scheduleBulk(action: PendingAction) {
        val ids = uiState.value.visibleItems.map { it.id }.toSet()
        if (ids.isEmpty()) return
        veil(ids, action)
        sendSideEffect(SmsReviewSideEffect.ShowBulkUndo(ids, action))
        startCommitJob(ids, action)
    }

    private fun veil(ids: Set<Long>, action: PendingAction) {
        setState {
            copy(pendingActions = pendingActions + ids.associateWith { action })
        }
    }

    private fun unveil(ids: Set<Long>) {
        setState { copy(pendingActions = pendingActions - ids) }
    }

    private fun startCommitJob(ids: Set<Long>, action: PendingAction) {
        pendingJobs[ids] = viewModelScope.launch {
            delay(UNDO_WINDOW_MS)
            commit(ids, action)
            pendingJobs.remove(ids)
        }
    }

    private fun undo(ids: Set<Long>) {
        pendingJobs.remove(ids)?.cancel()
        unveil(ids)
    }

    private suspend fun commit(ids: Set<Long>, action: PendingAction) {
        val failures = mutableListOf<Long>()
        ids.forEach { id ->
            val ok = when (action) {
                PendingAction.CONFIRM -> confirmUseCase(id).isSuccess
                PendingAction.DISMISS -> {
                    dismissUseCase(id); true
                }
            }
            if (!ok) failures += id
        }
        if (failures.isNotEmpty()) {
            unveil(failures.toSet())
            sendSideEffect(
                SmsReviewSideEffect.ShowError(
                    "Couldn't ${action.name.lowercase()} ${failures.size} transaction(s)"
                )
            )
        }
    }

    override fun onCleared() {
        pendingJobs.values.forEach { it.cancel() }
        super.onCleared()
    }
}
