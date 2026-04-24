package com.ayush.ledge.sync

import com.ayush.common.sync.SyncStateHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DefaultSyncStateHolder : SyncStateHolder {

    private val _isSyncing = MutableStateFlow(false)
    override val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    override fun onSyncStarted() {
        _isSyncing.value = true
    }

    override fun onSyncCompleted() {
        _isSyncing.value = false
    }
}
