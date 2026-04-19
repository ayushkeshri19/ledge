package com.ayush.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.ayush.ui.theme.LedgeTheme

@Composable
fun FabButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LedgeTheme.colors
    Box(
        modifier = modifier,
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .shadow(
                    elevation = 12.dp,
                    shape = CircleShape,
                    ambientColor = colors.gold.copy(alpha = 0.2f),
                    spotColor = colors.gold.copy(alpha = 0.4f)
                )
                .size(52.dp)
                .background(colors.bgSurface, CircleShape)
                .padding(3.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(colors.goldAccent, colors.gold),
                        start = Offset.Zero,
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                    ),
                    shape = CircleShape
                )
                .clip(CircleShape)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Add Transaction",
                tint = colors.bgDeep,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
