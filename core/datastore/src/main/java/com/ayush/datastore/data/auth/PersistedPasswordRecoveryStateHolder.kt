package com.ayush.datastore.data.auth

import com.ayush.common.auth.PasswordRecoveryStateHolder
import com.ayush.common.auth.RecoveryState
import com.ayush.datastore.data.AppDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class PersistedPasswordRecoveryStateHolder @Inject constructor(
    private val appDataStore: AppDataStore
) : PasswordRecoveryStateHolder {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _state = MutableStateFlow<RecoveryState>(RecoveryState.Loading)
    override val state: StateFlow<RecoveryState> = _state.asStateFlow()

    init {
        scope.launch {
            val persisted = appDataStore.getValue(
                key = AppDataStore.PreferencesKey.PASSWORD_RECOVERY_PENDING,
                defaultValue = false
            ).first()

            _state.update { current ->
                if (current == RecoveryState.Loading) {
                    if (persisted) RecoveryState.Active else RecoveryState.Inactive
                } else {
                    current
                }
            }
        }
    }

    override fun onRecoveryDetected() {
        _state.value = RecoveryState.Active
        scope.launch {
            appDataStore.putValue(
                key = AppDataStore.PreferencesKey.PASSWORD_RECOVERY_PENDING,
                value = true
            )
        }
    }

    override fun onRecoveryCompleted() {
        _state.value = RecoveryState.Inactive
        scope.launch {
            appDataStore.putValue(
                key = AppDataStore.PreferencesKey.PASSWORD_RECOVERY_PENDING,
                value = false
            )
        }
    }
}
