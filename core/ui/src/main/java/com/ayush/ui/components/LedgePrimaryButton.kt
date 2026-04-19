package com.ayush.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ayush.ui.theme.LedgeRadius
import com.ayush.ui.theme.LedgeTextStyle
import com.ayush.ui.theme.LedgeTheme

@Composable
fun LedgePrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    val colors = LedgeTheme.colors
    val gradient = Brush.horizontalGradient(
        colors = if (enabled) listOf(colors.gold, colors.goldAccent)
        else listOf(colors.bgCard2, colors.bgCard2)
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(RoundedCornerShape(LedgeRadius.large))
            .background(gradient)
            .clickable(enabled = enabled && !isLoading, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = colors.bgDeep,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                text = text,
                style = LedgeTextStyle.Button.copy(
                    color = if (enabled) colors.bgDeep else colors.textMuted,
                    fontSize = 15.sp
                )
            )
        }
    }
}
