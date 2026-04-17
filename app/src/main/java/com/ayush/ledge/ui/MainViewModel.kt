package com.ayush.ledge.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ayush.auth.domain.usecase.SignOutUseCase
import com.ayush.common.auth.AuthState
import com.ayush.common.auth.AuthStateProvider
import com.ayush.common.theme.ThemeMode
import com.ayush.datastore.domain.usecase.GetThemeModeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    authStateProvider: AuthStateProvider,
    private val signOutUseCase: SignOutUseCase,
    getThemeModeUseCase: GetThemeModeUseCase
) : ViewModel() {

    val authState: StateFlow<AuthState> = authStateProvider.authState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AuthState.Loading
        )

    val themeMode: StateFlow<ThemeMode> = getThemeModeUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ThemeMode.SYSTEM
        )

    fun signOut() {
        viewModelScope.launch { signOutUseCase.invoke() }
    }
}
