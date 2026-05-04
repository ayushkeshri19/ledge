package com.ayush.sms.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ayush.datastore.domain.usecase.ObserveSmsPermissionAskedUseCase
import com.ayush.sms.domain.permission.SmsPermissionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SmsPermissionPromptHostViewModel @Inject constructor(
    observeSmsPermissionAsked: ObserveSmsPermissionAskedUseCase,
    private val permissionManager: SmsPermissionManager
) : ViewModel() {

    val shouldShow: StateFlow<Boolean?> = observeSmsPermissionAsked()
        .map { asked ->
            if (permissionManager.isGranted()) {
                if (!asked) viewModelScope.launch { permissionManager.markAsked() }
                false
            } else {
                !asked
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null
        )
}
