package com.ayush.auth.presentation

import androidx.compose.runtime.Stable
import androidx.lifecycle.viewModelScope
import com.ayush.auth.domain.usecase.AuthEligibilityResult
import com.ayush.auth.domain.usecase.AuthEligibilityUseCase
import com.ayush.auth.domain.usecase.AuthFlow
import com.ayush.auth.domain.usecase.SignInWithEmailUseCase
import com.ayush.auth.domain.usecase.SignInWithGoogleIdToken
import com.ayush.auth.domain.usecase.SignUpWithEmailUseCase
import com.ayush.common.result.ApiResult
import com.ayush.ui.base.BaseMviViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val signInWithEmailUseCase: SignInWithEmailUseCase,
    private val signUpWithEmailUseCase: SignUpWithEmailUseCase,
    private val authEligibilityUseCase: AuthEligibilityUseCase,
    private val signInWithGoogleIdTokenUseCase: SignInWithGoogleIdToken
) : BaseMviViewModel<AuthUiEvent, AuthUiState, AuthUiSideEffect>(
    initialState = AuthUiState()
) {

    companion object {
        private const val TAG = "AuthViewModel"
    }

    override fun onEvent(event: AuthUiEvent) {
        when (event) {

            is AuthUiEvent.OnStart -> {
                viewModelScope.launch {
                    setState { copy(authFlow = event.flow) }
                }
            }

            is AuthUiEvent.EmailChanged -> {
                viewModelScope.launch {
                    setState { copy(email = event.email) }
                    evaluateEligibility()
                }
            }

            is AuthUiEvent.PasswordChanged -> {
                viewModelScope.launch {
                    setState { copy(password = event.password) }
                    evaluateEligibility()
                    validateSignUpFields()
                }
            }

            is AuthUiEvent.FullNameChanged -> {
                viewModelScope.launch {
                    setState { copy(fullName = event.fullName) }
                    validateSignUpFields()
                }
            }

            is AuthUiEvent.ConfirmPasswordChanged -> {
                viewModelScope.launch {
                    setState { copy(confirmPassword = event.confirmPassword) }
                    validateSignUpFields()
                }
            }

            is AuthUiEvent.GoogleIdTokenFailed -> {
                viewModelScope.launch {
                    Timber.tag(TAG).e(event.cause, "Google ID token failed")
                    setState { copy(isGoogleLoading = false) }
                    sendSideEffect(AuthUiSideEffect.ShowToast("Could not get Google account. Please try again."))
                }
            }

            is AuthUiEvent.OnGoogleIdTokenReceived -> {
                viewModelScope.launch {
                    signInWithGoogle(event.idToken)
                }
            }

            AuthUiEvent.SignUpWithEmailClicked -> {
                viewModelScope.launch {
                    validateSignUpFields()
                    val state = currentState()
                    if (state.fullNameError != null || state.confirmPasswordError != null) return@launch
                    if (state.canProceed != AuthEligibilityResult.Eligible) return@launch

                    setState { copy(isLoading = true) }

                    val result = signUpWithEmailUseCase(
                        email = state.email,
                        name = state.fullName.trim(),
                        password = state.password,
                    )
                    handleEmailAuthResult(result)
                }
            }

            AuthUiEvent.SignInWithEmailClicked -> {
                viewModelScope.launch {
                    if (currentState().canProceed != AuthEligibilityResult.Eligible) return@launch

                    setState { copy(isLoading = true) }
                    val state = currentState()

                    val result = signInWithEmailUseCase(
                        email = state.email,
                        password = state.password,
                    )
                    handleEmailAuthResult(result)
                }
            }

            AuthUiEvent.SignInWithGoogleClicked -> {
                viewModelScope.launch {
                    setState { copy(isGoogleLoading = true) }
                    sendSideEffect(AuthUiSideEffect.GetGoogleSignInToken)
                }
            }

            AuthUiEvent.ResetState -> resetState()
        }
    }

    private fun evaluateEligibility() {
        val state = currentState()
        val result = authEligibilityUseCase.canProceed(
            email = state.email,
            password = state.password,
            authFlow = state.authFlow,
        )
        setState { copy(canProceed = result) }
    }

    private suspend fun validateSignUpFields() {
        if (currentState().authFlow != AuthFlow.SIGN_UP) return
        val state = currentState()
        val fullNameError = when {
            state.fullName.isBlank() -> "Full name is required"
            state.fullName.trim().length < 2 -> "Enter at least 2 characters"
            else -> null
        }
        val confirmPasswordError = when {
            state.confirmPassword.isBlank() -> "Please confirm your password"
            state.confirmPassword != state.password -> "Passwords don't match"
            else -> null
        }
        setState {
            copy(
                fullNameError = fullNameError,
                confirmPasswordError = confirmPasswordError,
            )
        }
    }

    private suspend fun handleEmailAuthResult(result: ApiResult<*>) {
        setState { copy(isLoading = false) }
        when (result) {
            is ApiResult.Success -> {
                sendSideEffect(
                    AuthUiSideEffect.NotifySignInStateUpdate(SignInState.Success)
                )
            }

            is ApiResult.Error -> {
                Timber.tag(TAG).e(result.cause, "Email auth failed: %s", result.message)
                sendSideEffect(AuthUiSideEffect.ShowToast(result.message))
            }
        }
    }

    private suspend fun signInWithGoogle(idToken: String?) {
        if (idToken == null) {
            setState { copy(isGoogleLoading = false) }
            sendSideEffect(AuthUiSideEffect.ShowToast("Google sign-in failed"))
            Timber.tag(TAG).e("Google sign-in failed — id token was null")
            return
        }

        when (val result = signInWithGoogleIdTokenUseCase(idToken)) {
            is ApiResult.Success -> {
                setState { copy(isGoogleLoading = false) }
                sendSideEffect(
                    AuthUiSideEffect.NotifySignInStateUpdate(SignInState.Success)
                )
            }

            is ApiResult.Error -> {
                Timber.tag(TAG).e(result.cause, "Google sign-in failed: %s", result.message)
                setState { copy(isGoogleLoading = false) }
                sendSideEffect(AuthUiSideEffect.ShowToast(result.message))
            }
        }
    }
}

sealed interface AuthUiEvent {
    data class OnStart(val flow: AuthFlow) : AuthUiEvent
    data class EmailChanged(val email: String) : AuthUiEvent
    data class PasswordChanged(val password: String) : AuthUiEvent
    data class FullNameChanged(val fullName: String) : AuthUiEvent
    data class ConfirmPasswordChanged(val confirmPassword: String) : AuthUiEvent
    data class OnGoogleIdTokenReceived(val idToken: String) : AuthUiEvent
    data class GoogleIdTokenFailed(val cause: Throwable) : AuthUiEvent

    data object SignUpWithEmailClicked : AuthUiEvent
    data object SignInWithEmailClicked : AuthUiEvent
    data object SignInWithGoogleClicked : AuthUiEvent
    data object ResetState : AuthUiEvent
}

sealed interface AuthUiSideEffect {
    data class ShowToast(val message: String) : AuthUiSideEffect

    data class NotifySignInStateUpdate(
        val signInState: SignInState
    ) : AuthUiSideEffect

    data object GetGoogleSignInToken : AuthUiSideEffect
}

sealed interface SignInState {
    data object Success : SignInState
    data class Failed(val exception: Throwable?) : SignInState
}

@Stable
data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val fullName: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val isGoogleLoading: Boolean = false,
    val canProceed: AuthEligibilityResult = AuthEligibilityResult.Nothing,
    val authFlow: AuthFlow = AuthFlow.SIGN_IN,
    val fullNameError: String? = null,
    val confirmPasswordError: String? = null,
)


