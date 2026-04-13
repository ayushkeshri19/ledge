package com.ayush.common.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ayush.common.auth.AuthState
import com.ayush.common.auth.AuthStateProvider
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

fun ViewModel.observeAuthState(
    authStateProvider: AuthStateProvider,
    onAuthenticated: () -> Unit,
) {
    viewModelScope.launch {
        authStateProvider.authState
            .distinctUntilChanged()
            .collect { state ->
                if (state == AuthState.Authenticated) onAuthenticated()
            }
    }
}