package com.ayush.auth.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ayush.ui.components.LedgeAuthScaffold
import com.ayush.ui.components.LedgeErrorText
import com.ayush.ui.components.LedgeLogo
import com.ayush.ui.components.LedgePasswordField
import com.ayush.ui.components.LedgePrimaryButton
import com.ayush.ui.components.LedgeSecondaryButton
import com.ayush.ui.theme.LedgeTextStyle
import com.ayush.ui.theme.LedgeTheme

@Composable
fun SetNewPasswordScreen(
    onComplete: () -> Unit,
    viewModel: SetNewPasswordViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DisposableEffect(Unit) {
        onDispose { viewModel.onEvent(SetNewPasswordUiEvent.ResetState) }
    }

    LaunchedEffect(Unit) {
        viewModel.sideEffect.collect { effect ->
            when (effect) {
                SetNewPasswordUiSideEffect.NavigateToSignIn -> onComplete()
            }
        }
    }

    SetNewPasswordScreenContent(
        uiState = uiState,
        onPasswordChange = { viewModel.onEvent(SetNewPasswordUiEvent.PasswordChanged(it)) },
        onConfirmChange = { viewModel.onEvent(SetNewPasswordUiEvent.ConfirmPasswordChanged(it)) },
        onSubmit = { viewModel.onEvent(SetNewPasswordUiEvent.SubmitClicked) },
        onContinue = { viewModel.onEvent(SetNewPasswordUiEvent.ContinueToSignInClicked) },
        onCancel = { viewModel.onEvent(SetNewPasswordUiEvent.CancelClicked) },
        onBackFromInvalidLink = {
            viewModel.onEvent(SetNewPasswordUiEvent.BackToSignInFromInvalidLinkClicked)
        }
    )
}

@Composable
internal fun SetNewPasswordScreenContent(
    uiState: SetNewPasswordUiState,
    onPasswordChange: (String) -> Unit,
    onConfirmChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onContinue: () -> Unit,
    onCancel: () -> Unit,
    onBackFromInvalidLink: () -> Unit,
    modifier: Modifier = Modifier
) {
    LedgeAuthScaffold(modifier = modifier) {

        Spacer(Modifier.height(56.dp))

        LedgeLogo()

        Spacer(Modifier.height(40.dp))

        AnimatedContent(
            targetState = uiState.step,
            transitionSpec = {
                (fadeIn(tween(300)) + slideInVertically { it / 8 })
                    .togetherWith(fadeOut(tween(200)))
            },
            label = "setNewPasswordStep",
            modifier = Modifier.fillMaxWidth()
        ) { step ->
            when (step) {
                SetNewPasswordStep.REQUEST -> RequestStep(
                    uiState = uiState,
                    onPasswordChange = onPasswordChange,
                    onConfirmChange = onConfirmChange,
                    onSubmit = onSubmit,
                    onCancel = onCancel
                )

                SetNewPasswordStep.SUCCESS -> SuccessStep(onContinue = onContinue)

                SetNewPasswordStep.INVALID_LINK -> InvalidLinkStep(onBack = onBackFromInvalidLink)
            }
        }
    }
}

@Composable
private fun RequestStep(
    uiState: SetNewPasswordUiState,
    onPasswordChange: (String) -> Unit,
    onConfirmChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onCancel: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Set a new password",
            style = LedgeTextStyle.HeadingScreen.copy(
                fontSize = 24.sp,
                color = LedgeTheme.colors.textPrimary
            ),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Choose a strong password you haven't\nused on this account before.",
            style = LedgeTextStyle.BodySmall.copy(
                color = LedgeTheme.colors.textMuted2,
                lineHeight = 18.sp
            ),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(32.dp))

        LedgePasswordField(
            value = uiState.password,
            onValueChange = onPasswordChange,
            label = "NEW PASSWORD",
            isError = uiState.passwordError != null,
            errorMessage = uiState.passwordError,
            imeAction = ImeAction.Next
        )

        Spacer(Modifier.height(16.dp))

        LedgePasswordField(
            value = uiState.confirmPassword,
            onValueChange = onConfirmChange,
            label = "CONFIRM PASSWORD",
            isError = uiState.confirmError != null,
            errorMessage = uiState.confirmError,
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus(); onSubmit() }
            )
        )

        if (uiState.apiError != null) {
            Spacer(Modifier.height(8.dp))
            LedgeErrorText(
                message = uiState.apiError,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        Spacer(Modifier.height(24.dp))

        LedgePrimaryButton(
            text = "Update password",
            enabled = uiState.ctaEnabled,
            isLoading = uiState.isLoading,
            onClick = { focusManager.clearFocus(); onSubmit() }
        )

        Spacer(Modifier.height(16.dp))

        LedgeSecondaryButton(
            text = "Cancel",
            onClick = onCancel
        )
    }
}

@Composable
private fun SuccessStep(onContinue: () -> Unit) {
    val colors = LedgeTheme.colors
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(colors.goldGlow, androidx.compose.ui.graphics.Color.Transparent)
                        ),
                        shape = RoundedCornerShape(50)
                    )
            )
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(colors.bgCard2, RoundedCornerShape(20.dp))
                    .border(1.dp, colors.borderFocus, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(com.ayush.ui.R.drawable.ic_mail),
                    contentDescription = null,
                    tint = colors.gold,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Password updated",
            style = LedgeTextStyle.HeadingScreen.copy(
                fontSize = 24.sp,
                color = colors.textPrimary
            ),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(10.dp))

        Text(
            text = "Sign back in with your new password\nto continue.",
            style = LedgeTextStyle.BodySmall.copy(
                color = colors.textMuted2,
                lineHeight = 18.sp
            ),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(36.dp))

        LedgePrimaryButton(
            text = "Continue to sign in",
            onClick = onContinue
        )
    }
}

@Composable
private fun InvalidLinkStep(onBack: () -> Unit) {
    val colors = LedgeTheme.colors
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                colors.semanticRed.copy(alpha = 0.25f),
                                androidx.compose.ui.graphics.Color.Transparent
                            )
                        ),
                        shape = RoundedCornerShape(50)
                    )
            )
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(colors.bgCard2, RoundedCornerShape(20.dp))
                    .border(1.dp, colors.semanticRed, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.ErrorOutline,
                    contentDescription = null,
                    tint = colors.semanticRed,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Link expired or invalid",
            style = LedgeTextStyle.HeadingScreen.copy(
                fontSize = 24.sp,
                color = colors.textPrimary
            ),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(10.dp))

        Text(
            text = "This password reset link is no longer valid.\nSign in and request a new one.",
            style = LedgeTextStyle.BodySmall.copy(
                color = colors.textMuted2,
                lineHeight = 18.sp
            ),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(36.dp))

        LedgePrimaryButton(
            text = "Back to sign in",
            onClick = onBack
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF080A0F)
@Composable
private fun SetNewPasswordRequestPreview() {
    LedgeTheme {
        SetNewPasswordScreenContent(
            uiState = SetNewPasswordUiState(),
            onPasswordChange = {},
            onConfirmChange = {},
            onSubmit = {},
            onContinue = {},
            onCancel = {},
            onBackFromInvalidLink = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF080A0F, name = "Mismatch")
@Composable
private fun SetNewPasswordMismatchPreview() {
    LedgeTheme {
        SetNewPasswordScreenContent(
            uiState = SetNewPasswordUiState(
                password = "Hunter2!1",
                confirmPassword = "Hunter2",
                confirmError = "Passwords don't match"
            ),
            onPasswordChange = {},
            onConfirmChange = {},
            onSubmit = {},
            onContinue = {},
            onCancel = {},
            onBackFromInvalidLink = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF080A0F, name = "Success")
@Composable
private fun SetNewPasswordSuccessPreview() {
    LedgeTheme {
        SetNewPasswordScreenContent(
            uiState = SetNewPasswordUiState(step = SetNewPasswordStep.SUCCESS),
            onPasswordChange = {},
            onConfirmChange = {},
            onSubmit = {},
            onContinue = {},
            onCancel = {},
            onBackFromInvalidLink = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF080A0F, name = "Invalid link")
@Composable
private fun SetNewPasswordInvalidLinkPreview() {
    LedgeTheme {
        SetNewPasswordScreenContent(
            uiState = SetNewPasswordUiState(step = SetNewPasswordStep.INVALID_LINK),
            onPasswordChange = {},
            onConfirmChange = {},
            onSubmit = {},
            onContinue = {},
            onCancel = {},
            onBackFromInvalidLink = {}
        )
    }
}
