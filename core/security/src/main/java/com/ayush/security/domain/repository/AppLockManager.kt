package com.ayush.security.domain.repository

import kotlinx.coroutines.flow.StateFlow

interface AppLockManager {
    val biometricEnabled: StateFlow<Boolean>
    val locked: StateFlow<Boolean>
    fun onAppBackgrounded()
    fun onAppForegrounded()
    fun onBiometricDisabled()
    fun unlock()
}