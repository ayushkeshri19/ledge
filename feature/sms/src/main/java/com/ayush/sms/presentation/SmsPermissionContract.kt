package com.ayush.sms.presentation

import android.app.Activity
import com.ayush.sms.domain.model.SmsPermissionStatus

sealed interface SmsPermissionEvent {
    data class Resumed(val activity: Activity) : SmsPermissionEvent
    data class AllowClicked(val activity: Activity) : SmsPermissionEvent
    data class PermissionResult(val activity: Activity, val granted: Boolean) : SmsPermissionEvent
    data object OpenAppSettingsClicked : SmsPermissionEvent
    data object SkipClicked : SmsPermissionEvent
}

data class SmsPermissionState(
    val status: SmsPermissionStatus = SmsPermissionStatus.NotAsked
)

sealed interface SmsPermissionSideEffect {
    data object RequestPermission : SmsPermissionSideEffect
    data object OpenAppSettings : SmsPermissionSideEffect
    data object Complete : SmsPermissionSideEffect
}