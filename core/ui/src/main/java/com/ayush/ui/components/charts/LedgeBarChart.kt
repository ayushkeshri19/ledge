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
    val colors: List<Color>,
) {
    init {
        require(values.size == colors.size) {
            "values and colors must match; got ${values.size} vs ${colors.size}"
        }
        require(values.isNotEmpty()) { "group must have at least one value" }
    }
}

@Composable
fun LedgeBarChart(
    bars: List<BarChartData>,
    modifier: Modifier = Modifier,
    barCornerRadius: Dp = 6.dp,
    animationDurationMs: Int = 600,
    selectedIndex: Int? = null,
    onBarTap: ((Int) -> Unit)? = null,
    labelColor: Color = Color(0x8CFFFFFF),
    showValues: Boolean = false,
    valueFormatter: (Float) -> String = { it.toInt().toString() },
    valueColor: Color = Color(0xCCFFFFFF),
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
        val valueLabelHeight = if (showValues) 16.sp.toPx() else 0f
        val topPadding = 8f + valueLabelHeight
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

            if (showValues && progress > 0.3f) {
                val valueText = textMeasurer.measure(
                    text = valueFormatter(bar.value),
                    style = TextStyle(
                        color = if (isSelected) bar.color else valueColor,
                        fontSize = 11.sp
                    )
                )
                drawText(
                    textLayoutResult = valueText,
                    topLeft = Offset(
                        x = slotStart + (slotWidth - valueText.size.width) / 2,
                        y = (barY - valueText.size.height - 4f).coerceAtLeast(2f)
                    )
                )
            }

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
    labelColor: Color = Color(0x8CFFFFFF)
) {
    if (groups.isEmpty()) return

    val subBarCount = groups.first().values.size
    require(groups.all { it.values.size == subBarCount }) {
        "all groups must have the same number of sub-bars"
    }

    val maxValue = groups.maxOf { g -> g.values.maxOrNull() ?: 0f }
    if (maxValue == 0f) return

    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(groups) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = animationDurationMs,
                easing = EaseOutCubic
            )
        )
    }

    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        val horizontalPadding = 24f
        val labelAreaHeight = 24.sp.toPx()
        val topPadding = 8f
        val barAreaHeight = size.height - labelAreaHeight - topPadding
        val totalBarArea = size.width - horizontalPadding * 2

        val groupCount = groups.size
        val groupGapPx = groupGap.toPx()
        val subBarGapPx = subBarGap.toPx()
        val cornerRadius = barCornerRadius.toPx()
        val progress = animationProgress.value

        val slotWidth = (totalBarArea - (groupCount - 1) * groupGapPx) / groupCount
        val subBarWidth = (slotWidth - (subBarCount - 1) * subBarGapPx) / subBarCount

        groups.forEachIndexed { groupIdx, group ->
            val slotStart = horizontalPadding + groupIdx * (slotWidth + groupGapPx)

            group.values.forEachIndexed { subIdx, value ->
                val normalizedHeight = (value / maxValue) * barAreaHeight * progress
                val barX = slotStart + subIdx * (subBarWidth + subBarGapPx)
                val barY = topPadding + barAreaHeight - normalizedHeight

                drawRoundRect(
                    color = group.colors[subIdx],
                    topLeft = Offset(barX, barY),
                    size = Size(subBarWidth, normalizedHeight),
                    cornerRadius = CornerRadius(cornerRadius, cornerRadius)
                )
            }

            val labelText = textMeasurer.measure(
                text = group.label,
                style = TextStyle(color = labelColor, fontSize = 10.sp)
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
