package com.ayush.auth.presentation

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
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
import com.ayush.auth.presentation.AuthUiEvent.OnStart
import com.ayush.ui.components.LedgeAuthScaffold
import com.ayush.ui.components.LedgeDivider
import com.ayush.ui.components.LedgeLogo
import com.ayush.ui.components.LedgePasswordField
import com.ayush.ui.components.LedgePrimaryButton
import com.ayush.ui.components.LedgeSocialButton
import com.ayush.ui.components.LedgeTextField
import com.ayush.ui.components.LedgeTextLink
import com.ayush.ui.theme.BorderSubtle
import com.ayush.ui.theme.Gold
import com.ayush.ui.theme.LedgeTextStyle
import com.ayush.ui.theme.LedgeTheme
import com.ayush.ui.theme.SemanticGreen
import com.ayush.ui.theme.SemanticRed
import com.ayush.ui.theme.TextMuted2
import com.ayush.ui.theme.TextPrimary
import dagger.hilt.android.EntryPointAccessors

@Composable
fun SignUpScreen(
    onAuthSuccess: () -> Unit,
    onNavigateToSignIn: () -> Unit = {},
    viewModel: AuthViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.onEvent(OnStart(AuthFlow.SIGN_UP))
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
                    val result = googleSignInProvider.getIdToken(context as Activity)
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

    SignUpScreenContent(
        uiState = uiState,
        fullName = uiState.fullName,
        confirmPassword = uiState.confirmPassword,
        fullNameError = uiState.fullNameError,
        confirmPasswordError = uiState.confirmPasswordError,
        onFullNameChange = { viewModel.onEvent(AuthUiEvent.FullNameChanged(it)) },
        onEmailChange = { viewModel.onEvent(AuthUiEvent.EmailChanged(it)) },
        onPasswordChange = { viewModel.onEvent(AuthUiEvent.PasswordChanged(it)) },
        onConfirmPasswordChange = { viewModel.onEvent(AuthUiEvent.ConfirmPasswordChanged(it)) },
        onSubmit = { viewModel.onEvent(AuthUiEvent.SignUpWithEmailClicked) },
        onNavigateToSignIn = onNavigateToSignIn,
        onGoogleClicked = { viewModel.onEvent(AuthUiEvent.SignInWithGoogleClicked) },
    )
}

@Composable
internal fun SignUpScreenContent(
    uiState: AuthUiState,
    fullName: String,
    confirmPassword: String,
    fullNameError: String?,
    confirmPasswordError: String?,
    onFullNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onNavigateToSignIn: () -> Unit,
    onGoogleClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current
    val emailFocus = remember { FocusRequester() }
    val passwordFocus = remember { FocusRequester() }
    val confirmFocus = remember { FocusRequester() }

    val emailFieldError = uiState.canProceed.emailError()
    val passwordFieldError = uiState.canProceed.passwordError()

    val ctaEnabled = uiState.canProceed == AuthEligibilityResult.Eligible
            && !uiState.isLoading
            && !uiState.isGoogleLoading
            && fullNameError == null
            && confirmPasswordError == null

    val passwordStrength by remember(uiState.password) {
        derivedStateOf { computePasswordStrength(uiState.password) }
    }

    LedgeAuthScaffold(
        modifier = modifier.verticalScroll(rememberScrollState()),
    ) {

        Spacer(Modifier.height(48.dp))

        LedgeLogo(subtitle = "track · split · settle")

        Spacer(Modifier.height(28.dp))

        Text(
            text = "Create account",
            style = LedgeTextStyle.HeadingScreen.copy(
                fontSize = 24.sp,
                color = TextPrimary,
            ),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Start managing your finances",
            style = LedgeTextStyle.BodySmall.copy(color = TextMuted2),
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(28.dp))

        LedgeTextField(
            value = fullName,
            onValueChange = onFullNameChange,
            label = "FULL NAME",
            placeholder = "Jane Doe",
            isError = fullNameError != null,
            errorMessage = fullNameError,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
            ),
            keyboardActions = KeyboardActions(
                onNext = { emailFocus.requestFocus() }
            ),
        )

        Spacer(Modifier.height(14.dp))

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
            modifier = Modifier.focusRequester(emailFocus),
        )

        Spacer(Modifier.height(14.dp))

        LedgePasswordField(
            value = uiState.password,
            onValueChange = onPasswordChange,
            label = "PASSWORD",
            isError = passwordFieldError != null,
            errorMessage = passwordFieldError,
            imeAction = ImeAction.Next,
            keyboardActions = KeyboardActions(
                onNext = { confirmFocus.requestFocus() }
            ),
            modifier = Modifier.focusRequester(passwordFocus),
        )
        if (uiState.password.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            PasswordStrengthBar(strength = passwordStrength)
        }

        Spacer(Modifier.height(14.dp))

        LedgePasswordField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            label = "CONFIRM PASSWORD",
            placeholder = "••••••••",
            isError = confirmPasswordError != null,
            errorMessage = confirmPasswordError,
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus(); onSubmit() }
            ),
            modifier = Modifier.focusRequester(confirmFocus),
        )

        Spacer(Modifier.height(24.dp))

        LedgePrimaryButton(
            text = "Create account",
            enabled = ctaEnabled,
            isLoading = uiState.isLoading,
            onClick = { focusManager.clearFocus(); onSubmit() },
        )

        Spacer(Modifier.height(24.dp))

        LedgeDivider()

        Spacer(Modifier.height(16.dp))

        LedgeSocialButton(
            iconPainter = painterResource(com.ayush.ui.R.drawable.ic_google),
            label = "Sign up with Google",
            onClick = onGoogleClicked,
            isLoading = uiState.isGoogleLoading,
        )

        Spacer(Modifier.height(28.dp))

        Row(
            modifier = Modifier.padding(bottom = 40.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "Already have an account? ",
                style = LedgeTextStyle.BodySmall.copy(color = TextMuted2),
            )
            LedgeTextLink(
                text = "Sign in",
                onClick = onNavigateToSignIn,
            )
        }
    }
}


@Composable
private fun PasswordStrengthBar(
    strength: Int,
    modifier: Modifier = Modifier
) {
    val segmentColors = listOf(
        SemanticRed,
        Color(0xFFE08C4C),
        Gold,
        SemanticGreen
    )
    val labels = listOf("Weak", "Fair", "Good", "Strong")
    val activeColor = segmentColors.getOrElse(strength - 1) { BorderSubtle }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            repeat(4) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(3.dp)
                        .let { m ->
                            if (index < strength)
                                m.background(activeColor, RoundedCornerShape(2.dp))
                            else
                                m.background(BorderSubtle, RoundedCornerShape(2.dp))
                        }
                )
            }
        }
        if (strength > 0) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = labels[strength - 1],
                style = LedgeTextStyle.Caption.copy(color = activeColor, fontSize = 9.sp),
                modifier = Modifier.align(Alignment.End),
            )
        }
    }
}

private fun computePasswordStrength(password: String): Int {
    if (password.isEmpty()) return 0
    var score = 0
    if (password.length >= 8) score++
    if (password.any { it.isDigit() }) score++
    if (password.any { !it.isLetterOrDigit() }) score++
    if (password.length >= 12 && password.any { it.isUpperCase() }
        && password.any { it.isLowerCase() }) score++
    return score.coerceIn(1, 4)
}


@Preview(showBackground = true, backgroundColor = 0xFF080A0F)
@Composable
private fun SignUpDefaultPreview() {
    LedgeTheme {
        SignUpScreenContent(
            uiState = AuthUiState(authFlow = AuthFlow.SIGN_UP),
            fullName = "",
            confirmPassword = "",
            fullNameError = null,
            confirmPasswordError = null,
            onFullNameChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onSubmit = {},
            onNavigateToSignIn = {},
            onGoogleClicked = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF080A0F, name = "Eligible — CTA lit")
@Composable
private fun SignUpEligiblePreview() {
    LedgeTheme {
        SignUpScreenContent(
            uiState = AuthUiState(
                email = "jane@example.com",
                password = "Str0ng!Pass",
                canProceed = AuthEligibilityResult.Eligible,
                authFlow = AuthFlow.SIGN_UP,
            ),
            fullName = "Jane Doe",
            confirmPassword = "Str0ng!Pass",
            fullNameError = null,
            confirmPasswordError = null,
            onFullNameChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onSubmit = {},
            onNavigateToSignIn = {},
            onGoogleClicked = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF080A0F, name = "Loading")
@Composable
private fun SignUpLoadingPreview() {
    LedgeTheme {
        SignUpScreenContent(
            uiState = AuthUiState(
                email = "jane@example.com",
                password = "Str0ng!Pass",
                isLoading = true,
                canProceed = AuthEligibilityResult.Eligible,
                authFlow = AuthFlow.SIGN_UP,
            ),
            fullName = "Jane Doe",
            confirmPassword = "Str0ng!Pass",
            fullNameError = null,
            confirmPasswordError = null,
            onFullNameChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onSubmit = {},
            onNavigateToSignIn = {},
            onGoogleClicked = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF080A0F, name = "Local validation errors")
@Composable
private fun SignUpLocalErrorPreview() {
    LedgeTheme {
        SignUpScreenContent(
            uiState = AuthUiState(authFlow = AuthFlow.SIGN_UP),
            fullName = "J",
            confirmPassword = "mismatch",
            fullNameError = "Enter at least 2 characters",
            confirmPasswordError = "Passwords don't match",
            onFullNameChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onSubmit = {},
            onNavigateToSignIn = {},
            onGoogleClicked = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF080A0F, name = "Password policy error")
@Composable
private fun SignUpPasswordPolicyPreview() {
    LedgeTheme {
        SignUpScreenContent(
            uiState = AuthUiState(
                email = "jane@example.com",
                password = "weakpass",
                canProceed = AuthEligibilityResult.Error(
                    "Password must contain at least one uppercase letter"
                ),
                authFlow = AuthFlow.SIGN_UP,
            ),
            fullName = "Jane Doe",
            confirmPassword = "weakpass",
            fullNameError = null,
            confirmPasswordError = null,
            onFullNameChange = {},
            onEmailChange = {},
            onPasswordChange = {},
            onConfirmPasswordChange = {},
            onSubmit = {},
            onNavigateToSignIn = {},
            onGoogleClicked = {},
        )
    }
}