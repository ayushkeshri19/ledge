package com.ayush.auth.presentation

import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.ayush.auth.domain.usecase.AuthEligibilityUseCase
import com.ayush.auth.domain.usecase.SignOutUseCase
import com.ayush.auth.domain.usecase.UpdatePasswordUseCase
import com.ayush.common.auth.PasswordRecoveryStateHolder
import com.ayush.common.result.ApiResult
import com.ayush.ui.base.BaseMviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SetNewPasswordViewModel @Inject constructor(
    private val updatePasswordUseCase: UpdatePasswordUseCase,
    private val signOutUseCase: SignOutUseCase,
    private val authEligibilityUseCase: AuthEligibilityUseCase,
    private val passwordRecoveryStateHolder: PasswordRecoveryStateHolder
) : BaseMviViewModel<SetNewPasswordUiEvent, SetNewPasswordUiState, SetNewPasswordUiSideEffect>(
    initialState = SetNewPasswordUiState()
) {

    override fun onEvent(event: SetNewPasswordUiEvent) {
        when (event) {
            is SetNewPasswordUiEvent.PasswordChanged -> {
                setState {
                    copy(
                        password = event.password,
                        passwordError = null,
                        confirmError = computeConfirmError(event.password, confirmPassword),
                        apiError = null
                    )
                }
            }

            is SetNewPasswordUiEvent.ConfirmPasswordChanged -> {
                setState {
                    copy(
                        confirmPassword = event.password,
                        confirmError = computeConfirmError(password, event.password),
                        apiError = null
                    )
                }
            }

            SetNewPasswordUiEvent.SubmitClicked -> submit()

            SetNewPasswordUiEvent.ContinueToSignInClicked -> exitToSignIn()

            SetNewPasswordUiEvent.CancelClicked -> exitToSignIn()

            SetNewPasswordUiEvent.ResetState -> resetState()
        }
    }

    private fun submit() {
        val state = currentState()
        val passwordError = authEligibilityUseCase.validatePassword(state.password)
        val confirmError = computeConfirmError(state.password, state.confirmPassword)
        if (passwordError != null || confirmError != null) {
            setState { copy(passwordError = passwordError, confirmError = confirmError) }
            return
        }

        setState { copy(isLoading = true, apiError = null) }
        viewModelScope.launch {
            when (val result = updatePasswordUseCase(state.password)) {
                is ApiResult.Success -> {
                    setState { copy(isLoading = false, step = SetNewPasswordStep.SUCCESS) }
                }

                is ApiResult.Error -> {
                    setState {
                        copy(
                            isLoading = false,
                            apiError = result.cause?.message
                                ?: "Couldn't update password. Please try again."
                        )
                    }
                }
            }
        }
    }

    private fun exitToSignIn() {
        viewModelScope.launch {
            passwordRecoveryStateHolder.onRecoveryCompleted()
            signOutUseCase()
            sendSideEffect(SetNewPasswordUiSideEffect.NavigateToSignIn)
        }
    }

    private fun computeConfirmError(password: String, confirm: String): String? = when {
        confirm.isBlank() -> null
        password != confirm -> "Passwords don't match"
        else -> null
    }
}

sealed interface SetNewPasswordUiEvent {
    data class PasswordChanged(val password: String) : SetNewPasswordUiEvent
    data class ConfirmPasswordChanged(val password: String) : SetNewPasswordUiEvent
    data object SubmitClicked : SetNewPasswordUiEvent
    data object ContinueToSignInClicked : SetNewPasswordUiEvent
    data object CancelClicked : SetNewPasswordUiEvent
    data object ResetState : SetNewPasswordUiEvent
}

sealed interface SetNewPasswordUiSideEffect {
    data object NavigateToSignIn : SetNewPasswordUiSideEffect
}

enum class SetNewPasswordStep { REQUEST, SUCCESS }

@Stable
data class SetNewPasswordUiState(
    val password: String = "",
    val confirmPassword: String = "",
    val passwordError: String? = null,
    val confirmError: String? = null,
    val apiError: String? = null,
    val isLoading: Boolean = false,
    val step: SetNewPasswordStep = SetNewPasswordStep.REQUEST
)

val SetNewPasswordUiState.ctaEnabled: Boolean
    get() = password.isNotBlank() &&
            confirmPassword.isNotBlank() &&
            passwordError == null &&
            confirmError == null &&
            !isLoading
