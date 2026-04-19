package com.ayush.profile.presentation.profile

import androidx.compose.runtime.Stable
import com.ayush.common.theme.ThemeMode
import com.ayush.security.domain.models.BiometricStatus

@Stable
data class ProfileState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val biometricEnabled: Boolean = false,
    val biometricStatus: BiometricStatus = BiometricStatus.UNSUPPORTED
)

sealed interface ProfileEvent {
    data class ThemeModeChanged(val mode: ThemeMode) : ProfileEvent
    data class BiometricToggleRequested(val enable: Boolean) : ProfileEvent
    data object EnrollmentRequested : ProfileEvent
    data class BiometricAuthResult(
        val success: Boolean,
        val intendedEnable: Boolean
    ) : ProfileEvent

    data object Resumed : ProfileEvent
}

sealed interface ProfileSideEffect {
    data class RequestBiometricAuth(val intendedEnable: Boolean) : ProfileSideEffect
    data object OpenBiometricEnrollment : ProfileSideEffect
}
