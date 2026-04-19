package com.ayush.profile.presentation.profile

import androidx.lifecycle.viewModelScope
import com.ayush.datastore.domain.usecase.GetThemeModeUseCase
import com.ayush.datastore.domain.usecase.ObserveBiometricsEnabledUseCase
import com.ayush.datastore.domain.usecase.SetBiometricsEnabledUseCase
import com.ayush.datastore.domain.usecase.SetThemeModeUseCase
import com.ayush.security.data.repository.BiometricAvailability
import com.ayush.security.domain.repository.AppLockManager
import com.ayush.ui.base.BaseMviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    getThemeModeUseCase: GetThemeModeUseCase,
    observeBiometricsEnabled: ObserveBiometricsEnabledUseCase,
    private val setThemeModeUseCase: SetThemeModeUseCase,
    private val setBiometricsEnabled: SetBiometricsEnabledUseCase,
    private val biometricAvailability: BiometricAvailability,
    private val appLockManager: AppLockManager
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
        }
    }
}
