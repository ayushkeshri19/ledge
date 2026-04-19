package com.ayush.profile.presentation.profile

import androidx.compose.runtime.Stable
import com.ayush.common.theme.ThemeMode

@Stable
data class ProfileState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM
)

sealed interface ProfileEvent {
    data class ThemeModeChanged(val mode: ThemeMode) : ProfileEvent
}

sealed interface ProfileSideEffect
