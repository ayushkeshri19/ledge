package com.ayush.common.auth

import kotlinx.coroutines.flow.StateFlow

sealed interface RecoveryState {
    data object Loading : RecoveryState
    data object Active : RecoveryState
    data object Inactive : RecoveryState
}

interface PasswordRecoveryStateHolder {
    val state: StateFlow<RecoveryState>
    fun onRecoveryDetected()
    fun onRecoveryCompleted()
}
