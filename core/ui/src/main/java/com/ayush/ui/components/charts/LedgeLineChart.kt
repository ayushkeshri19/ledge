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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ayush.ui.theme.LedgeTheme
import kotlin.math.abs

data class LineChartPoint(
    val label: String,
    val value: Float,
)

@Composable
fun LedgeLineChart(
    points: List<LineChartPoint>,
    lineColor: Color,
    modifier: Modifier = Modifier,
    fillGradient: Boolean = true,
    showGridLines: Boolean = true,
    animationDurationMs: Int = 800,
    selectedIndex: Int? = null,
    onPointTap: ((Int) -> Unit)? = null,
    labelColor: Color = Color(0x8CFFFFFF),
    gridLineColor: Color = Color(0x0FFFFFFF),
) {
    if (points.size < 2) return

    val dotCenterColor = LedgeTheme.colors.bgDeep

    val maxValue = points.maxOf { it.value }
    val minValue = points.minOf { it.value }
    val valueRange = (maxValue - minValue).let { if (it == 0f) 1f else it }


    val animationProgress = remember { Animatable(0f) }
    LaunchedEffect(points) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = animationDurationMs,
                easing = EaseOutCubic,
            ),
        )
    }


    val selectedRadius = remember { Animatable(5f) }
    LaunchedEffect(selectedIndex) {
        if (selectedIndex != null) {
            selectedRadius.snapTo(5f)
            selectedRadius.animateTo(10f, tween(200))
        } else {
            selectedRadius.animateTo(5f, tween(200))
        }
    }

    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .then(
                if (onPointTap != null) {
                    Modifier.pointerInput(points.size) {
                        detectTapGestures { tapOffset ->
                            val horizontalPadding = 24f
                            val chartWidth = size.width - horizontalPadding * 2
                            val spacing = chartWidth / (points.size - 1)


                            val nearestIndex = ((tapOffset.x - horizontalPadding) / spacing)
                                .toInt()
                                .coerceIn(0, points.lastIndex)


                            val pointX = horizontalPadding + nearestIndex * spacing
                            if (abs(tapOffset.x - pointX) < spacing / 2) {
                                onPointTap(nearestIndex)
                            }
                        }
                    }
                } else Modifier
            ),
    ) {
        val horizontalPadding = 24f
        val labelAreaHeight = 24.sp.toPx()
        val topPadding = 16f
        val chartHeight = size.height - labelAreaHeight - topPadding
        val chartWidth = size.width - horizontalPadding * 2
        val spacing = chartWidth / (points.size - 1)
        val progress = animationProgress.value


        fun pointOffset(index: Int): Offset {
            val x = horizontalPadding + index * spacing
            val normalized = (points[index].value - minValue) / valueRange
            val y = topPadding + chartHeight * (1f - normalized)
            return Offset(x, y)
        }


        if (showGridLines) {
            val gridCount = 3
            for (i in 0..gridCount) {
                val y = topPadding + chartHeight * i / gridCount
                drawLine(
                    color = gridLineColor,
                    start = Offset(horizontalPadding, y),
                    end = Offset(size.width - horizontalPadding, y),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f)),
                )
            }
        }


        val linePath = Path().apply {
            points.forEachIndexed { index, _ ->
                val offset = pointOffset(index)
                if (index == 0) moveTo(offset.x, offset.y) else lineTo(offset.x, offset.y)
            }
        }


        val clipRight = horizontalPadding + chartWidth * progress

        clipRect(
            left = 0f,
            top = 0f,
            right = clipRight,
            bottom = size.height,
        ) {

            if (fillGradient) {
                val fillPath = Path().apply {
                    addPath(linePath)
                    lineTo(pointOffset(points.lastIndex).x, topPadding + chartHeight)
                    lineTo(horizontalPadding, topPadding + chartHeight)
                    close()
                }
                drawPath(
                    path = fillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            lineColor.copy(alpha = 0.25f),
                            lineColor.copy(alpha = 0.0f),
                        ),
                        startY = topPadding,
                        endY = topPadding + chartHeight,
                    ),
                )
            }


            drawPath(
                path = linePath,
                color = lineColor,
                style = Stroke(
                    width = 3f,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                ),
            )


            points.forEachIndexed { index, _ ->
                val offset = pointOffset(index)
                val isSelected = index == selectedIndex

                if (isSelected) {

                    drawCircle(
                        color = lineColor.copy(alpha = 0.2f),
                        radius = selectedRadius.value + 4f,
                        center = offset,
                    )

                    drawLine(
                        color = lineColor.copy(alpha = 0.3f),
                        start = Offset(offset.x, topPadding),
                        end = Offset(offset.x, topPadding + chartHeight),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f)),
                    )
                }


                drawCircle(
                    color = lineColor,
                    radius = if (isSelected) selectedRadius.value else 4f,
                    center = offset,
                )

                drawCircle(
                    color = dotCenterColor,
                    radius = if (isSelected) selectedRadius.value - 2.5f else 2f,
                    center = offset,
                )
            }
        }


        points.forEachIndexed { index, point ->
            val labelText = textMeasurer.measure(
                text = point.label,
                style = TextStyle(
                    color = if (index == selectedIndex) lineColor else labelColor,
                    fontSize = 10.sp,
                ),
            )
            drawText(
                textLayoutResult = labelText,
                topLeft = Offset(
                    x = horizontalPadding + index * spacing - labelText.size.width / 2,
                    y = topPadding + chartHeight + 6f,
                ),
            )
        }
    }
}
