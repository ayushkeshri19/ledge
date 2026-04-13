package com.ayush.ui.components.charts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Animated horizontal progress bar for budget tracking.
 *
 * Color transitions:
 * - Gold (< warningThreshold)
 * - Amber (warningThreshold – 100%)
 * - Red (> 100%)
 *
 * Shows a small marker line at the warning threshold position.
 *
 * @param progress Ratio of spent/limit (0f = empty, 1f = 100%, can exceed 1f).
 * @param warningThreshold Fraction (0f–1f) where the bar turns amber.
 * @param height Bar height.
 * @param normalColor Color below warning threshold.
 * @param warningColor Color between warning and 100%.
 * @param exceededColor Color above 100%.
 * @param trackColor Background track color.
 * @param animationDurationMs Entrance animation duration.
 */
@Composable
fun LedgeBudgetProgressBar(
    progress: Float,
    warningThreshold: Float = 0.8f,
    modifier: Modifier = Modifier,
    height: Dp = 8.dp,
    normalColor: Color = Color(0xFFC9A84C),      // Gold
    warningColor: Color = Color(0xFFFF9F43),      // Amber
    exceededColor: Color = Color(0xFFE05A5A),     // SemanticRed
    trackColor: Color = Color(0xFF13161E),        // BgCard
    animationDurationMs: Int = 600,
) {
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(progress) {
        animatedProgress.animateTo(
            targetValue = progress.coerceAtLeast(0f),
            animationSpec = tween(animationDurationMs, easing = EaseOutCubic),
        )
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
    ) {
        val barHeight = size.height
        val cornerRadius = CornerRadius(barHeight / 2, barHeight / 2)

        // Track (background)
        drawRoundRect(
            color = trackColor,
            topLeft = Offset.Zero,
            size = Size(size.width, barHeight),
            cornerRadius = cornerRadius,
        )

        // Progress fill
        val currentProgress = animatedProgress.value
        val fillWidth = (size.width * currentProgress.coerceAtMost(1f))
        val fillColor = when {
            currentProgress >= 1f -> exceededColor
            currentProgress >= warningThreshold -> warningColor
            else -> normalColor
        }

        if (fillWidth > 0f) {
            drawRoundRect(
                color = fillColor,
                topLeft = Offset.Zero,
                size = Size(fillWidth, barHeight),
                cornerRadius = cornerRadius,
            )
        }

        // Overflow indicator (subtle glow if over 100%)
        if (currentProgress > 1f) {
            drawRoundRect(
                color = exceededColor.copy(alpha = 0.3f),
                topLeft = Offset.Zero,
                size = Size(size.width, barHeight),
                cornerRadius = cornerRadius,
            )
        }

        // Warning threshold marker line
        val markerX = size.width * warningThreshold
        if (warningThreshold in 0.05f..0.95f) {
            drawLine(
                color = Color.White.copy(alpha = 0.3f),
                start = Offset(markerX, 0f),
                end = Offset(markerX, barHeight),
                strokeWidth = 1.5f,
            )
        }
    }
}
