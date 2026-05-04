package com.ayush.sms.domain.model

sealed interface SmsPermissionStatus {
    data object NotAsked : SmsPermissionStatus
    data object Granted : SmsPermissionStatus
    data object Denied : SmsPermissionStatus
    data object PermanentlyDenied : SmsPermissionStatus
}