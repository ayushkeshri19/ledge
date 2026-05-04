package com.ayush.sms.domain.permission

import android.app.Activity
import com.ayush.sms.domain.model.SmsPermissionStatus

interface SmsPermissionManager {
    fun isGranted(): Boolean
    suspend fun computeStatus(activity: Activity): SmsPermissionStatus
    suspend fun markAsked()
}