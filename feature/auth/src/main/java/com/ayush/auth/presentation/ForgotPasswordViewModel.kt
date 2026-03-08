package com.ayush.auth.presentation

import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.ayush.auth.domain.usecase.SendPasswordResetEmailUseCase
import com.ayush.common.result.ApiResult
import com.ayush.ui.base.BaseMviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    private val sendPasswordResetEmailUseCase: SendPasswordResetEmailUseCase,
) : BaseMviViewModel<ResetPasswordUiEvent, ResetPasswordUiState, ResetPasswordUiSideEffect>(
    initialState = ResetPasswordUiState()
) {
    override fun onEvent(event: ResetPasswordUiEvent) {
        when (event) {

            is ResetPasswordUiEvent.EmailChanged -> {
                viewModelScope.launch {
                    setState {
                        copy(
                            email = event.email,
                            emailError = null,
                            apiError = null,
                        )
                    }
                    evaluateEmail()
                }
            }

            ResetPasswordUiEvent.SendResetLinkClicked -> {
                viewModelScope.launch {
                    val state = currentState()
                    if (state.emailError != null || state.email.isBlank()) return@launch

                    setState { copy(isLoading = true, apiError = null) }

                    when (val result = sendPasswordResetEmailUseCase(state.email)) {
                        is ApiResult.Success -> {
                            setState { copy(isLoading = false, step = ResetPasswordStep.EMAIL_SENT) }
                        }

                        is ApiResult.Error -> {
                            setState {
                                copy(
                                    isLoading = false,
                                    apiError = result.cause?.message
                                        ?: "Something went wrong. Please try again.",
                                )
                            }
                        }
                    }
                }
            }

            ResetPasswordUiEvent.ResetState -> resetState()
        }
    }

    private fun evaluateEmail() {
        val email = currentState().email
        val error = when {
            email.isBlank() -> null
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                "Invalid email format"

            else -> null
        }
        setState { copy(emailError = error) }
    }
}


sealed interface ResetPasswordUiEvent {
    data class EmailChanged(val email: String) : ResetPasswordUiEvent
    data object SendResetLinkClicked : ResetPasswordUiEvent
    data object ResetState : ResetPasswordUiEvent
}

sealed interface ResetPasswordUiSideEffect

enum class ResetPasswordStep { REQUEST, EMAIL_SENT }

@Stable
data class ResetPasswordUiState(
    val email: String = "",
    val emailError: String? = null,
    val isLoading: Boolean = false,
    val apiError: String? = null,
    val step: ResetPasswordStep = ResetPasswordStep.REQUEST,
)

val ResetPasswordUiState.ctaEnabled: Boolean
    get() = email.isNotBlank() && emailError == null && !isLoading
