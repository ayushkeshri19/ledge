package com.ayush.auth.presentation

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ayush.auth.di.GoogleSignInProviderEntryPoint
import com.ayush.auth.domain.usecase.AuthEligibilityResult
import com.ayush.auth.domain.usecase.AuthFlow
import com.ayush.auth.domain.usecase.emailError
import com.ayush.auth.domain.usecase.passwordError
import com.ayush.ui.components.LedgeAuthScaffold
import com.ayush.ui.components.LedgeDivider
import com.ayush.ui.components.LedgeLogo
import com.ayush.ui.components.LedgePasswordField
import com.ayush.ui.components.LedgePrimaryButton
import com.ayush.ui.components.LedgeSocialButton
import com.ayush.ui.components.LedgeTextField
import com.ayush.ui.components.LedgeTextLink
import com.ayush.ui.theme.LedgeTextStyle
import com.ayush.ui.theme.LedgeTheme
import com.ayush.ui.theme.TextMuted2
import com.ayush.ui.theme.TextPrimary
import dagger.hilt.android.EntryPointAccessors

@Composable
fun SignInScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onAuthSuccess: () -> Unit = {},
    onNavigateToSignUp: () -> Unit = {},
    onForgotPassword: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.onEvent(AuthUiEvent.OnStart(AuthFlow.SIGN_IN))
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.onEvent(AuthUiEvent.ResetState) }
    }

    val googleSignInProvider = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            GoogleSignInProviderEntryPoint::class.java
        ).googleSignInIdTokenProvider()
    }

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                AuthUiSideEffect.GetGoogleSignInToken -> {
                    val result = googleSignInProvider.getIdToken(activity = context as Activity)
                    result.fold(
                        onSuccess = { idToken ->
                            viewModel.onEvent(AuthUiEvent.OnGoogleIdTokenReceived(idToken))
                        },
                        onFailure = { viewModel.onEvent(AuthUiEvent.GoogleIdTokenFailed(it)) }
                    )
                }

                is AuthUiSideEffect.NotifySignInStateUpdate -> {
                    when (effect.signInState) {
                        SignInState.Success -> onAuthSuccess()
                        is SignInState.Failed -> {}
                    }
                }

                is AuthUiSideEffect.ShowToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    SignInScreenContent(
        uiState = uiState,
        onEmailChange = { viewModel.onEvent(AuthUiEvent.EmailChanged(it)) },
        onPasswordChange = { viewModel.onEvent(AuthUiEvent.PasswordChanged(it)) },
        onSubmit = { viewModel.onEvent(AuthUiEvent.SignInWithEmailClicked) },
        onForgotPassword = onForgotPassword,
        onNavigateToSignUp = onNavigateToSignUp,
        onGoogleClicked = { viewModel.onEvent(AuthUiEvent.SignInWithGoogleClicked) },
    )
}


@Composable
internal fun SignInScreenContent(
    uiState: AuthUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onForgotPassword: () -> Unit,
    onNavigateToSignUp: () -> Unit,
    onGoogleClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val passwordFocus = remember { FocusRequester() }

    val emailFieldError = uiState.canProceed.emailError()
    val passwordFieldError = uiState.canProceed.passwordError()
    val ctaEnabled = uiState.canProceed == AuthEligibilityResult.Eligible
            && !uiState.isLoading
            && !uiState.isGoogleLoading

    LedgeAuthScaffold(modifier = modifier) {

        Spacer(Modifier.height(56.dp))

        LedgeLogo(subtitle = "track · split · settle")

        Spacer(Modifier.height(36.dp))

        Text(
            text = "Welcome back",
            style = LedgeTextStyle.HeadingScreen.copy(
                fontSize = 24.sp,
                color = TextPrimary,
            ),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Sign in to your account",
            style = LedgeTextStyle.BodySmall.copy(color = TextMuted2),
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(32.dp))

        LedgeTextField(
            value = uiState.email,
            onValueChange = onEmailChange,
            label = "EMAIL",
            placeholder = "you@example.com",
            isError = emailFieldError != null,
            errorMessage = emailFieldError,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            ),
            keyboardActions = KeyboardActions(
                onNext = { passwordFocus.requestFocus() }
            ),
        )

        Spacer(Modifier.height(16.dp))

        LedgePasswordField(
            value = uiState.password,
            onValueChange = onPasswordChange,
            label = "PASSWORD",
            isError = passwordFieldError != null,
            errorMessage = passwordFieldError,
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus(); onSubmit() }
            ),
            modifier = Modifier.focusRequester(passwordFocus),
        )

        Spacer(Modifier.height(12.dp))
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            LedgeTextLink(
                text = "Forgot password?",
                onClick = onForgotPassword,
            )
        }

        Spacer(Modifier.height(24.dp))

        LedgePrimaryButton(
            text = "Sign in",
            enabled = ctaEnabled,
            isLoading = uiState.isLoading,
            onClick = { focusManager.clearFocus(); onSubmit() },
        )

        Spacer(Modifier.height(24.dp))

        LedgeDivider()

        Spacer(Modifier.height(16.dp))

        LedgeSocialButton(
            iconPainter = painterResource(com.ayush.ui.R.drawable.ic_google),
            label = "Continue with Google",
            onClick = onGoogleClicked,
            isLoading = uiState.isGoogleLoading,
        )

        Spacer(Modifier.weight(1f))

        Row(
            modifier = Modifier.padding(bottom = 40.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Don't have an account? ",
                style = LedgeTextStyle.BodySmall.copy(color = TextMuted2),
            )
            LedgeTextLink(
                text = "Sign up",
                onClick = onNavigateToSignUp,
            )
        }
    }
}



@Preview(showBackground = true, backgroundColor = 0xFF080A0F)
@Composable
private fun SignInDefaultPreview() {
    LedgeTheme {
        SignInScreenContent(
            uiState = AuthUiState(),
            onEmailChange = {},
            onPasswordChange = {},
            onSubmit = {},
            onForgotPassword = {},
            onNavigateToSignUp = {},
            onGoogleClicked = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF080A0F, name = "Eligible — CTA lit")
@Composable
private fun SignInEligiblePreview() {
    LedgeTheme {
        SignInScreenContent(
            uiState = AuthUiState(
                email = "jane@example.com",
                password = "hunter2",
                canProceed = AuthEligibilityResult.Eligible,
            ),
            onEmailChange = {},
            onPasswordChange = {},
            onSubmit = {},
            onForgotPassword = {},
            onNavigateToSignUp = {},
            onGoogleClicked = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF080A0F, name = "Loading")
@Composable
private fun SignInLoadingPreview() {
    LedgeTheme {
        SignInScreenContent(
            uiState = AuthUiState(
                email = "jane@example.com",
                password = "hunter2",
                isLoading = true,
                canProceed = AuthEligibilityResult.Eligible,
            ),
            onEmailChange = {},
            onPasswordChange = {},
            onSubmit = {},
            onForgotPassword = {},
            onNavigateToSignUp = {},
            onGoogleClicked = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF080A0F, name = "Inline email error")
@Composable
private fun SignInEmailErrorPreview() {
    LedgeTheme {
        SignInScreenContent(
            uiState = AuthUiState(
                email = "not-an-email",
                password = "",
                canProceed = AuthEligibilityResult.Error("Invalid email format"),
            ),
            onEmailChange = {},
            onPasswordChange = {},
            onSubmit = {},
            onForgotPassword = {},
            onNavigateToSignUp = {},
            onGoogleClicked = {},
        )
    }
}
