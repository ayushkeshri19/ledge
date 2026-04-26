package com.ayush.sms.presentation

import android.app.Activity
import androidx.lifecycle.viewModelScope
import com.ayush.sms.data.permission.SmsPermissionManager
import com.ayush.sms.domain.model.SmsPermissionStatus
import com.ayush.ui.base.BaseMviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SmsPermissionViewModel @Inject constructor(
    private val smsPermissionManager: SmsPermissionManager
) : BaseMviViewModel<SmsPermissionEvent, SmsPermissionState, SmsPermissionSideEffect>(SmsPermissionState()) {

    override fun onEvent(event: SmsPermissionEvent) {
        when (event) {
            is SmsPermissionEvent.Resumed -> refresh(event.activity)

            is SmsPermissionEvent.AllowClicked -> handleAllow(event.activity)

            is SmsPermissionEvent.PermissionResult -> handleResult(event.activity, event.granted)

            SmsPermissionEvent.OpenAppSettingsClicked -> sendSideEffect(SmsPermissionSideEffect.OpenAppSettings)

            SmsPermissionEvent.SkipClicked -> handleSkip()
        }
    }

    private fun refresh(activity: Activity) {
        viewModelScope.launch {
            val status = smsPermissionManager.computeStatus(activity)
            setState { copy(status = status) }
            if (status == SmsPermissionStatus.Granted) {
                sendSideEffect(SmsPermissionSideEffect.Complete)
            }
        }
    }

    private fun handleAllow(activity: Activity) {
        viewModelScope.launch {
            when (smsPermissionManager.computeStatus(activity)) {
                SmsPermissionStatus.NotAsked,
                SmsPermissionStatus.Denied -> sendSideEffect(SmsPermissionSideEffect.RequestPermission)

                SmsPermissionStatus.PermanentlyDenied -> sendSideEffect(SmsPermissionSideEffect.OpenAppSettings)
                SmsPermissionStatus.Granted -> sendSideEffect(SmsPermissionSideEffect.Complete)
            }
        }
    }

    private fun handleResult(activity: Activity, granted: Boolean) {
        viewModelScope.launch {
            smsPermissionManager.markAsked()
            val status = smsPermissionManager.computeStatus(activity)
            setState { copy(status = status) }
            if (granted) sendSideEffect(SmsPermissionSideEffect.Complete)
        }
    }

    private fun handleSkip() {
        viewModelScope.launch {
            smsPermissionManager.markAsked()
            sendSideEffect(SmsPermissionSideEffect.Complete)
        }
    }
}