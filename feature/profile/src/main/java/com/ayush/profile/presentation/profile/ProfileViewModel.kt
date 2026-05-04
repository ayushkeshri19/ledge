package com.ayush.profile.presentation.profile

import androidx.lifecycle.viewModelScope
import com.ayush.datastore.domain.usecase.GetThemeModeUseCase
import com.ayush.datastore.domain.usecase.ObserveBiometricsEnabledUseCase
import com.ayush.datastore.domain.usecase.ObserveSmsAutoDetectEnabledUseCase
import com.ayush.datastore.domain.usecase.SetBiometricsEnabledUseCase
import com.ayush.datastore.domain.usecase.SetSmsAutoDetectEnabledUseCase
import com.ayush.datastore.domain.usecase.SetThemeModeUseCase
import com.ayush.security.data.repository.BiometricAvailability
import com.ayush.security.domain.repository.AppLockManager
import com.ayush.sms.domain.permission.SmsPermissionManager
import com.ayush.ui.base.BaseMviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    getThemeModeUseCase: GetThemeModeUseCase,
    observeBiometricsEnabled: ObserveBiometricsEnabledUseCase,
    observeSmsAutoDetectEnabled: ObserveSmsAutoDetectEnabledUseCase,
    private val setThemeModeUseCase: SetThemeModeUseCase,
    private val setBiometricsEnabled: SetBiometricsEnabledUseCase,
    private val setSmsAutoDetectEnabled: SetSmsAutoDetectEnabledUseCase,
    private val biometricAvailability: BiometricAvailability,
    private val appLockManager: AppLockManager,
    private val smsPermissionManager: SmsPermissionManager
) : BaseMviViewModel<ProfileEvent, ProfileState, ProfileSideEffect>(
    initialState = ProfileState()
) {

    init {
        setState { copy(biometricStatus = biometricAvailability.status()) }

        viewModelScope.launch {
            getThemeModeUseCase().collect { mode ->
                setState { copy(themeMode = mode) }
            }
        }
        viewModelScope.launch {
            observeBiometricsEnabled().collect { enabled ->
                setState { copy(biometricEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            observeSmsAutoDetectEnabled().collect { enabled ->
                setState { copy(smsAutoDetectEnabled = enabled) }
            }
        }
    }

    override fun onEvent(event: ProfileEvent) {
        when (event) {
            is ProfileEvent.ThemeModeChanged ->
                viewModelScope.launch { setThemeModeUseCase(event.mode) }

            is ProfileEvent.BiometricToggleRequested ->
                sendSideEffect(ProfileSideEffect.RequestBiometricAuth(event.enable))

            ProfileEvent.EnrollmentRequested ->
                sendSideEffect(ProfileSideEffect.OpenBiometricEnrollment)

            is ProfileEvent.BiometricAuthResult -> {
                if (event.success) {
                    viewModelScope.launch {
                        setBiometricsEnabled(event.intendedEnable)
                        if (!event.intendedEnable) {
                            appLockManager.onBiometricDisabled()
                        }
                    }
                }
            }

            ProfileEvent.Resumed ->
                setState { copy(biometricStatus = biometricAvailability.status()) }

            is ProfileEvent.SmsAutoDetectToggled -> {
                if (event.enable) {
                    if (smsPermissionManager.isGranted()) {
                        viewModelScope.launch { setSmsAutoDetectEnabled(true) }
                    } else {
                        sendSideEffect(ProfileSideEffect.ShowSmsPermissionDialog)
                    }
                } else {
                    viewModelScope.launch { setSmsAutoDetectEnabled(false) }
                }
            }

            ProfileEvent.SmsDialogDismissed -> {
                if (smsPermissionManager.isGranted()) {
                    viewModelScope.launch { setSmsAutoDetectEnabled(true) }
                }
            }
        }
    }
}
