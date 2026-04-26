package com.ayush.sms.data.permission

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.ayush.datastore.domain.usecase.ObserveSmsPermissionAskedUseCase
import com.ayush.datastore.domain.usecase.SetSmsPermissionAskedUseCase
import com.ayush.sms.domain.model.SmsPermissionStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmsPermissionManager @Inject constructor(
    @param:ApplicationContext private val appContext: Context,
    private val observeAsked: ObserveSmsPermissionAskedUseCase,
    private val setAsked: SetSmsPermissionAskedUseCase
) {

    fun isGranted(): Boolean {
        val read = ContextCompat.checkSelfPermission(
            appContext, Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED
        val receive = ContextCompat.checkSelfPermission(
            appContext, Manifest.permission.RECEIVE_SMS
        ) == PackageManager.PERMISSION_GRANTED
        return read && receive
    }

    suspend fun computeStatus(activity: Activity): SmsPermissionStatus {
        if (isGranted()) return SmsPermissionStatus.Granted
        val asked = observeAsked().first()
        if (!asked) return SmsPermissionStatus.NotAsked
        val canStillPrompt = ActivityCompat.shouldShowRequestPermissionRationale(
            activity, Manifest.permission.READ_SMS
        )
        return if (canStillPrompt) SmsPermissionStatus.Denied
        else SmsPermissionStatus.PermanentlyDenied
    }

    suspend fun markAsked() = setAsked()
}