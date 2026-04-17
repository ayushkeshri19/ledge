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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ayush.ui.components.LedgeAuthScaffold
import com.ayush.ui.components.LedgeErrorText
import com.ayush.ui.components.LedgeLogo
import com.ayush.ui.components.LedgePrimaryButton
import com.ayush.ui.components.LedgeSecondaryButton
import com.ayush.ui.components.LedgeTextField
import com.ayush.ui.theme.DmMonoFontFamily
import com.ayush.ui.theme.LedgeRadius
import com.ayush.ui.theme.LedgeTextStyle
import com.ayush.ui.theme.LedgeTheme

@Composable
fun ResetPasswordScreen(
    onBack: () -> Unit,
    viewModel: ResetPasswordViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DisposableEffect(Unit) {
        onDispose { viewModel.onEvent(ResetPasswordUiEvent.ResetState) }
    }

    ResetPasswordScreenContent(
        uiState = uiState,
        onEmailChange = { viewModel.onEvent(ResetPasswordUiEvent.EmailChanged(it)) },
        onSubmit = { viewModel.onEvent(ResetPasswordUiEvent.SendResetLinkClicked) },
        onBack = onBack,
    )
}

@Composable
internal fun ResetPasswordScreenContent(
    uiState: ResetPasswordUiState,
    onEmailChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
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
            label = "resetPasswordStep",
            modifier = Modifier.fillMaxWidth(),
        ) { step ->
            when (step) {
                ResetPasswordStep.REQUEST -> RequestStepContent(
                    uiState = uiState,
                    onEmailChange = onEmailChange,
                    onSubmit = onSubmit,
                    onBack = onBack,
                )

                ResetPasswordStep.EMAIL_SENT -> EmailSentContent(
                    email = uiState.email,
                    onBack = onBack,
                )
            }
        }
    }
}

@Composable
private fun RequestStepContent(
    uiState: ResetPasswordUiState,
    onEmailChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit,
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Text(
            text = "Reset password",
            style = LedgeTextStyle.HeadingScreen.copy(
                fontSize = 24.sp,
                color = LedgeTheme.colors.textPrimary,
            ),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Enter the email address linked to your account\nand we'll send you a reset link.",
            style = LedgeTextStyle.BodySmall.copy(
                color = LedgeTheme.colors.textMuted2,
                lineHeight = 18.sp,
            ),
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(32.dp))

        LedgeTextField(
            value = uiState.email,
            onValueChange = onEmailChange,
            label = "EMAIL",
            placeholder = "you@example.com",
            isError = uiState.emailError != null,
            errorMessage = uiState.emailError,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus(); onSubmit() }
            ),
        )

        if (uiState.apiError != null) {
            Spacer(Modifier.height(8.dp))
            LedgeErrorText(
                message = uiState.apiError,
                modifier = Modifier.padding(start = 4.dp),
            )
        }

        Spacer(Modifier.height(24.dp))

        LedgePrimaryButton(
            text = "Send reset link",
            enabled = uiState.ctaEnabled,
            isLoading = uiState.isLoading,
            onClick = { focusManager.clearFocus(); onSubmit() },
        )

        Spacer(Modifier.height(16.dp))

        LedgeSecondaryButton(
            text = "Back to sign in",
            onClick = onBack,
        )
    }
}

@Composable
private fun EmailSentContent(
    email: String,
    onBack: () -> Unit,
) {
    val colors = LedgeTheme.colors
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(colors.goldGlow, androidx.compose.ui.graphics.Color.Transparent)
                        ),
                        shape = RoundedCornerShape(50),
                    )
            )
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(colors.bgCard2, RoundedCornerShape(20.dp))
                    .border(1.dp, colors.borderFocus, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter = painterResource(com.ayush.ui.R.drawable.ic_mail),
                    contentDescription = null,
                    tint = colors.gold,
                    modifier = Modifier.size(28.dp),
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text = "Check your inbox",
            style = LedgeTextStyle.HeadingScreen.copy(
                fontSize = 24.sp,
                color = colors.textPrimary,
            ),
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(10.dp))

        Text(
            text = "We've sent a reset link to",
            style = LedgeTextStyle.BodySmall.copy(color = colors.textMuted2),
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .background(colors.goldDim, RoundedCornerShape(LedgeRadius.pill))
                .border(1.dp, colors.borderFocus, RoundedCornerShape(LedgeRadius.pill))
                .padding(horizontal = 16.dp, vertical = 6.dp),
        ) {
            Text(
                text = email,
                style = LedgeTextStyle.BodySmall.copy(
                    color = colors.gold,
                    fontFamily = DmMonoFontFamily,
                ),
            )
        }

        Spacer(Modifier.height(10.dp))

        Text(
            text = "If it doesn't arrive within a minute,\ncheck your spam folder.",
            style = LedgeTextStyle.Caption.copy(
                color = colors.textMuted,
                lineHeight = 16.sp,
            ),
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(36.dp))

        LedgePrimaryButton(
            text = "Back to sign in",
            onClick = onBack,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF080A0F)
@Composable
private fun ResetPasswordRequestPreview() {
    LedgeTheme {
        ResetPasswordScreenContent(
            uiState = ResetPasswordUiState(),
            onEmailChange = {},
            onSubmit = {},
            onBack = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF080A0F, name = "CTA enabled")
@Composable
private fun ResetPasswordEligiblePreview() {
    LedgeTheme {
        ResetPasswordScreenContent(
            uiState = ResetPasswordUiState(email = "jane@example.com"),
            onEmailChange = {},
            onSubmit = {},
            onBack = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF080A0F, name = "Loading")
@Composable
private fun ResetPasswordLoadingPreview() {
    LedgeTheme {
        ResetPasswordScreenContent(
            uiState = ResetPasswordUiState(
                email = "jane@example.com",
                isLoading = true,
            ),
            onEmailChange = {},
            onSubmit = {},
            onBack = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF080A0F, name = "Inline email error")
@Composable
private fun ResetPasswordEmailErrorPreview() {
    LedgeTheme {
        ResetPasswordScreenContent(
            uiState = ResetPasswordUiState(
                email = "not-an-email",
                emailError = "Invalid email format",
            ),
            onEmailChange = {},
            onSubmit = {},
            onBack = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF080A0F, name = "API error")
@Composable
private fun ResetPasswordApiErrorPreview() {
    LedgeTheme {
        ResetPasswordScreenContent(
            uiState = ResetPasswordUiState(
                email = "ghost@example.com",
                apiError = "No account found for this email address.",
            ),
            onEmailChange = {},
            onSubmit = {},
            onBack = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF080A0F, name = "Email sent")
@Composable
private fun ResetPasswordSentPreview() {
    LedgeTheme {
        ResetPasswordScreenContent(
            uiState = ResetPasswordUiState(
                email = "jane@example.com",
                step = ResetPasswordStep.EMAIL_SENT,
            ),
            onEmailChange = {},
            onSubmit = {},
            onBack = {},
        )
    }
}