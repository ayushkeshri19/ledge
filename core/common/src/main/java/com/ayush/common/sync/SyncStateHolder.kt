package com.ayush.common.sync

import kotlinx.coroutines.flow.StateFlow

interface SyncStateHolder {
    val isSyncing: StateFlow<Boolean>
    fun onSyncStarted()
    fun onSyncCompleted()
}
