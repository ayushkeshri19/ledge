package com.ayush.network.data.auth

import com.ayush.common.auth.PasswordRecoveryStateHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class DefaultPasswordRecoveryStateHolder @Inject constructor() : PasswordRecoveryStateHolder {

    private val _recoveryActive = MutableStateFlow(false)
    override val recoveryActive: StateFlow<Boolean> = _recoveryActive.asStateFlow()

    override fun onRecoveryDetected() {
        _recoveryActive.value = true
    }

    override fun onRecoveryCompleted() {
        _recoveryActive.value = false
    }
}
