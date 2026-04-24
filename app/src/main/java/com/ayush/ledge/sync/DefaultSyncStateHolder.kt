package com.ayush.ledge.sync

import com.ayush.common.sync.SyncState
import com.ayush.common.sync.SyncStateHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DefaultSyncStateHolder : SyncStateHolder {

    private val _state = MutableStateFlow<SyncState>(SyncState.Idle)
    override val state: StateFlow<SyncState> = _state.asStateFlow()

    override fun onSyncStarted() {
        _state.value = SyncState.Syncing
    }

    override fun onSyncCompleted(error: Throwable?) {
        _state.value = if (error != null) {
            SyncState.Failed(reason = GENERIC_FAILURE_MESSAGE)
        } else {
            SyncState.Idle
        }
    }

    override fun onSyncErrorDismissed() {
        _state.update { current ->
            if (current is SyncState.Failed) SyncState.Idle else current
        }
    }

    companion object {
        private const val GENERIC_FAILURE_MESSAGE = "Couldn't refresh data. Check your connection and try again."
    }
}
