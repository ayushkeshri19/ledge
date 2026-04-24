package com.ayush.common.sync

import kotlinx.coroutines.flow.StateFlow

interface SyncStateHolder {
    val state: StateFlow<SyncState>
    fun onSyncStarted()
    fun onSyncCompleted(error: Throwable? = null)
    fun onSyncErrorDismissed()
}
