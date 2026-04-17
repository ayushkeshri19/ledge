package com.ayush.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ayush.ui.theme.LedgeRadius
import com.ayush.ui.theme.LedgeTextStyle
import com.ayush.ui.theme.LedgeTheme

@Composable
fun LedgeTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = true,
    enabled: Boolean = true,
) {
    val colors = LedgeTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    val borderColor by animateColorAsState(
        targetValue = when {
            isError -> colors.semanticRed
            isFocused -> colors.gold
            else -> colors.borderSubtle
        },
        animationSpec = tween(200),
        label = "borderColor",
    )

    Column(modifier = modifier) {
        if (label.isNotEmpty()) {
            Text(
                text = label,
                style = LedgeTextStyle.Caption.copy(
                    color = if (isError) colors.semanticRed else colors.textMuted2,
                    letterSpacing = 0.8.sp,
                ),
                modifier = Modifier.padding(bottom = 6.dp),
            )
        }

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    style = LedgeTextStyle.Body.copy(color = colors.textMuted),
                )
            },
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            isError = isError,
            singleLine = singleLine,
            enabled = enabled,
            interactionSource = interactionSource,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            textStyle = LedgeTextStyle.Body.copy(color = colors.textPrimary),
            shape = RoundedCornerShape(LedgeRadius.medium),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = colors.bgCard2,
                unfocusedContainerColor = colors.bgCard,
                disabledContainerColor = colors.bgCard,
                errorContainerColor = colors.redDim,
                focusedBorderColor = colors.gold,
                unfocusedBorderColor = colors.borderSubtle,
                errorBorderColor = colors.semanticRed,
                focusedTextColor = colors.textPrimary,
                unfocusedTextColor = colors.textPrimary,
                cursorColor = colors.gold,
                focusedLeadingIconColor = colors.gold,
                unfocusedLeadingIconColor = colors.textMuted,
                focusedTrailingIconColor = colors.gold,
                unfocusedTrailingIconColor = colors.textMuted,
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        if (isError && !errorMessage.isNullOrEmpty()) {
            LedgeErrorText(
                message = errorMessage,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp),
            )
        }
    }
}

@Composable
fun LedgeTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier,
    placeholder: String,
    isError: Boolean,
    errorMessage: String?,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions,
    trailingIcon: @Composable (() -> Unit)?,
    visualTransformation: VisualTransformation,
) {
    val colors = LedgeTheme.colors
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Column(modifier = modifier) {
        if (label.isNotEmpty()) {
            Text(
                text = label,
                style = LedgeTextStyle.Caption.copy(
                    color = if (isError) colors.semanticRed else colors.textMuted2,
                    letterSpacing = 0.8.sp,
                ),
                modifier = Modifier.padding(bottom = 6.dp),
            )
        }

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    style = LedgeTextStyle.Body.copy(color = colors.textMuted),
                )
            },
            trailingIcon = trailingIcon,
            isError = isError,
            singleLine = true,
            interactionSource = interactionSource,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            visualTransformation = visualTransformation,
            textStyle = LedgeTextStyle.Body.copy(color = colors.textPrimary),
            shape = RoundedCornerShape(LedgeRadius.medium),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = colors.bgCard2,
                unfocusedContainerColor = colors.bgCard,
                errorContainerColor = colors.redDim,
                focusedBorderColor = colors.gold,
                unfocusedBorderColor = colors.borderSubtle,
                errorBorderColor = colors.semanticRed,
                focusedTextColor = colors.textPrimary,
                unfocusedTextColor = colors.textPrimary,
                cursorColor = colors.gold,
                focusedTrailingIconColor = colors.gold,
                unfocusedTrailingIconColor = colors.textMuted,
            ),
            modifier = Modifier.fillMaxWidth(),
        )

        if (isError && !errorMessage.isNullOrEmpty()) {
            LedgeErrorText(
                message = errorMessage,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp),
            )
        }
    }
}
