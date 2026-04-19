package com.ayush.security.data.repository

import com.ayush.datastore.domain.usecase.ObserveBiometricsEnabledUseCase
import com.ayush.security.domain.repository.AppLockManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLockManagerImpl @Inject constructor(
    observeBiometricsEnabled: ObserveBiometricsEnabledUseCase
) : AppLockManager {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override val biometricEnabled: StateFlow<Boolean> =
        observeBiometricsEnabled.invoke()
            .stateIn(
                scope = scope,
                started = SharingStarted.Eagerly,
                initialValue = false
            )

    private val _locked = MutableStateFlow(false)
    override val locked: StateFlow<Boolean> = _locked.asStateFlow()

    override fun onAppBackgrounded() {
        if (biometricEnabled.value) {
            _locked.value = true
        }
    }

    override fun onAppForegrounded() {
        /** Intentionally kept no-op so that OS does not show the thumbnail for app in recents*/
    }

    override fun onBiometricDisabled() {
        _locked.value = false
    }

    override fun unlock() {
        _locked.value = false
    }
}
