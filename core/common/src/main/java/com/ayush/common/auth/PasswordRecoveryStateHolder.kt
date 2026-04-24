package com.ayush.common.auth

import kotlinx.coroutines.flow.StateFlow

interface PasswordRecoveryStateHolder {
    val recoveryActive: StateFlow<Boolean>
    fun onRecoveryDetected()
    fun onRecoveryCompleted()
}
