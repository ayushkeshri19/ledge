package com.ayush.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.ayush.ui.theme.LedgeTextStyle
import com.ayush.ui.theme.LedgeTheme

@Composable
fun LedgeErrorText(
    message: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = message,
        style = LedgeTextStyle.Caption.copy(
            color = LedgeTheme.colors.semanticRed,
            letterSpacing = 0.2.sp,
        ),
        modifier = modifier,
    )
}
