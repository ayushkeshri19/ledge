package com.ayush.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ayush.ui.theme.LedgeRadius
import com.ayush.ui.theme.LedgeTextStyle
import com.ayush.ui.theme.LedgeTheme

@Composable
fun LedgeSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val colors = LedgeTheme.colors
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp),
        shape = RoundedCornerShape(LedgeRadius.large),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = colors.textMuted2,
            disabledContainerColor = Color.Transparent,
            disabledContentColor = colors.textMuted
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (enabled) colors.borderMid else colors.borderSubtle,
        )
    ) {
        Text(
            text = text,
            style = LedgeTextStyle.Button.copy(fontSize = 15.sp)
        )
    }
}
