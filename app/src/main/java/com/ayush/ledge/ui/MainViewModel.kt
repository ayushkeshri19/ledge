package com.ayush.ledge.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ayush.auth.domain.usecase.SignOutUseCase
import com.ayush.common.auth.AuthState
import com.ayush.common.auth.AuthStateProvider
import com.ayush.common.auth.PasswordRecoveryStateHolder
import com.ayush.common.auth.RecoveryState
import com.ayush.common.theme.ThemeMode
import com.ayush.datastore.domain.usecase.GetThemeModeUseCase
import com.ayush.datastore.domain.usecase.SetBiometricsEnabledUseCase
import com.ayush.ledge.sync.SyncAllUserDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authStateProvider: AuthStateProvider,
    passwordRecoveryStateHolder: PasswordRecoveryStateHolder,
    private val signOutUseCase: SignOutUseCase,
    private val setBiometricsEnabledUseCase: SetBiometricsEnabledUseCase,
    private val syncAllUserDataUseCase: SyncAllUserDataUseCase,
    getThemeModeUseCase: GetThemeModeUseCase
) : ViewModel() {

    val authState: StateFlow<AuthState> = authStateProvider.authState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AuthState.Loading
        )

    val recoveryState: StateFlow<RecoveryState> = passwordRecoveryStateHolder.state

    val themeMode: StateFlow<ThemeMode> = getThemeModeUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ThemeMode.SYSTEM
        )

    init {
        viewModelScope.launch {
            var syncedForSession = false
            authStateProvider.authState.collect { state ->
                when (state) {
                    AuthState.Authenticated -> {
                        if (!syncedForSession) {
                            syncedForSession = true
                            launch { syncAllUserDataUseCase() }
                        }
                    }

                    AuthState.NotAuthenticated -> syncedForSession = false

                    else -> {}
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch { signOutUseCase.invoke() }
    }

    fun disableBiometric() {
        viewModelScope.launch {
            setBiometricsEnabledUseCase(false)
        }
    }
}
