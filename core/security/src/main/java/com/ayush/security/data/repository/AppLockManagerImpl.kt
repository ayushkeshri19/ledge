package com.ayush.security.data.repository

import android.os.SystemClock
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

    private var lastBackgroundedAt: Long? = null
    private val idleTimeoutMs = 5 * 60 * 1000L

    override fun onAppBackgrounded() {
        if (biometricEnabled.value) {
            lastBackgroundedAt = SystemClock.elapsedRealtime()
        }
    }

    override fun onAppForegrounded() {
        if (!biometricEnabled.value) {
            _locked.value = false
            return
        }
        val t = lastBackgroundedAt ?: return
        if (SystemClock.elapsedRealtime() - t >= idleTimeoutMs) {
            _locked.value = true
        }
    }

    override fun onBiometricDisabled() {
        _locked.value = false
        lastBackgroundedAt = null
    }

    override fun unlock() {
        _locked.value = false
        lastBackgroundedAt = null
    }
}