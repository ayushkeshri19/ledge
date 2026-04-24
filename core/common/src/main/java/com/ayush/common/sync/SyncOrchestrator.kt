package com.ayush.common.sync

interface SyncOrchestrator {
    suspend fun syncAll()
}
