package com.ayush.ledge.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ayush.auth.domain.usecase.SignOutUseCase
import com.ayush.common.auth.AuthState
import com.ayush.common.auth.AuthStateProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    authStateProvider: AuthStateProvider,
    private val signOutUseCase: SignOutUseCase
) : ViewModel() {

    val authState: StateFlow<AuthState> = authStateProvider.authState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AuthState.Loading
        )

    fun signOut() {
        viewModelScope.launch { signOutUseCase.invoke() }
    }

}