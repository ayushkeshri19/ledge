package com.ayush.ui.components.charts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class BarChartData(
    val label: String,
    val value: Float,
    val color: Color,
)

data class BarChartGroup(
    val label: String,
    val values: List<Float>,
    val colors: List<Color>
)

@Composable
fun LedgeBarChart(
    bars: List<BarChartData>,
    modifier: Modifier = Modifier,
    barCornerRadius: Dp = 6.dp,
    animationDurationMs: Int = 600,
    selectedIndex: Int? = null,
    onBarTap: ((Int) -> Unit)? = null,
    labelColor: Color = Color(0x8CFFFFFF)
) {
    if (bars.isEmpty()) return

    val maxValue = bars.maxOf { it.value }
    if (maxValue == 0f) return

    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(bars) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = animationDurationMs,
                easing = EaseOutCubic,
            ),
        )
    }

    val selectedScale = remember { Animatable(1f) }
    LaunchedEffect(selectedIndex) {
        if (selectedIndex != null) {
            selectedScale.snapTo(1f)
            selectedScale.animateTo(1.08f, tween(200))
        } else {
            selectedScale.animateTo(1f, tween(200))
        }
    }

    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .then(
                if (onBarTap != null) {
                    Modifier.pointerInput(bars.size) {
                        detectTapGestures { tapOffset ->
                            val barCount = bars.size
                            val horizontalPadding = 24f
                            val totalBarArea = size.width - horizontalPadding * 2
                            val slotWidth = totalBarArea / barCount

                            val tappedSlot =
                                ((tapOffset.x - horizontalPadding) / slotWidth).toInt()
                            if (tappedSlot in bars.indices) {
                                onBarTap(tappedSlot)
                            }
                        }
                    }
                } else Modifier
            )
    ) {
        val horizontalPadding = 24f
        val labelAreaHeight = 24.sp.toPx()
        val topPadding = 8f
        val barAreaHeight = size.height - labelAreaHeight - topPadding
        val totalBarArea = size.width - horizontalPadding * 2

        val barCount = bars.size
        val slotWidth = totalBarArea / barCount
        val barWidth = slotWidth * 0.6f
        val cornerRadius = barCornerRadius.toPx()
        val progress = animationProgress.value

        bars.forEachIndexed { index, bar ->
            val normalizedHeight = (bar.value / maxValue) * barAreaHeight * progress
            val isSelected = index == selectedIndex
            val effectiveWidth = if (isSelected) barWidth * selectedScale.value else barWidth

            val slotStart = horizontalPadding + index * slotWidth
            val barX = slotStart + (slotWidth - effectiveWidth) / 2
            val barY = topPadding + barAreaHeight - normalizedHeight

            if (isSelected) {
                drawRoundRect(
                    color = bar.color.copy(alpha = 0.2f),
                    topLeft = Offset(barX - 4f, barY - 4f),
                    size = Size(effectiveWidth + 8f, normalizedHeight + 4f),
                    cornerRadius = CornerRadius(cornerRadius + 2f, cornerRadius + 2f)
                )
            }

            drawRoundRect(
                color = bar.color,
                topLeft = Offset(barX, barY),
                size = Size(effectiveWidth, normalizedHeight),
                cornerRadius = CornerRadius(cornerRadius, cornerRadius)
            )

            val labelText = textMeasurer.measure(
                text = bar.label,
                style = TextStyle(
                    color = if (isSelected) bar.color else labelColor,
                    fontSize = 10.sp
                ),
            )
            drawText(
                textLayoutResult = labelText,
                topLeft = Offset(
                    x = slotStart + (slotWidth - labelText.size.width) / 2,
                    y = topPadding + barAreaHeight + 6f
                )
            )
        }
    }
}

@Composable
fun LedgeGroupedBarChart(
    groups: List<BarChartGroup>,
    modifier: Modifier = Modifier,
    barCornerRadius: Dp = 4.dp,
    groupGap: Dp = 12.dp,
    subBarGap: Dp = 3.dp,
    animationDurationMs: Int = 600,
    labelColor: Color = Color(0x8CFFFFFF),
) {

}
