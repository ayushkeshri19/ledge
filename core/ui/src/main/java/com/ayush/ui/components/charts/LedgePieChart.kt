package com.ayush.ui.components.charts

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

data class PieChartSegment(
    val value: Float,
    val color: Color,
    val label: String,
)

@Composable
fun LedgePieChart(
    segments: List<PieChartSegment>,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 28.dp,
    animationDurationMs: Int = 800,
    selectedIndex: Int? = null,
    onSegmentTap: ((Int) -> Unit)? = null,
    centerContent: @Composable (() -> Unit)? = null,
    animateInitialAppearance: Boolean = true
) {
    if (segments.isEmpty()) return

    val total = segments.sumOf { it.value.toDouble() }.toFloat()
    if (total == 0f) return

    val proportions = segments.map { it.value / total }

    val animationProgress = remember { Animatable(if (animateInitialAppearance) 0f else 1f) }
    var firstSegmentsRun by remember { mutableStateOf(true) }
    LaunchedEffect(segments) {
        val shouldAnimate = !firstSegmentsRun || animateInitialAppearance
        firstSegmentsRun = false
        if (shouldAnimate) {
            animationProgress.snapTo(0f)
            animationProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = animationDurationMs,
                    easing = EaseOutCubic,
                ),
            )
        } else {
            animationProgress.snapTo(1f)
        }
    }

    val selectedScale = remember { Animatable(1f) }
    LaunchedEffect(selectedIndex) {
        if (selectedIndex != null) {
            selectedScale.snapTo(0.5f)
            selectedScale.animateTo(
                targetValue = 1.3f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioHighBouncy,
                    stiffness = Spring.StiffnessHigh
                )
            )
        } else {
            selectedScale.animateTo(1f, tween(200))
        }
    }

    val segmentAngles = remember(proportions) {
        var cumulative = 0f
        proportions.map { proportion ->
            val start = cumulative
            cumulative += proportion * 360f
            start to proportion * 360f
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Canvas(
            modifier = Modifier
                .matchParentSize()
                .then(
                    if (onSegmentTap != null) {
                        Modifier.pointerInput(segmentAngles) {
                            detectTapGestures { tapOffset ->
                                val center = Offset(size.width / 2f, size.height / 2f)
                                val dx = tapOffset.x - center.x
                                val dy = tapOffset.y - center.y
                                val distance = sqrt(dx.pow(2) + dy.pow(2))

                                val ringStroke = strokeWidth.toPx()
                                val outerRadius = minOf(size.width, size.height) / 2f
                                val innerRadius = outerRadius - ringStroke
                                if (distance !in innerRadius..outerRadius) return@detectTapGestures

                                var tapAngle = Math
                                    .toDegrees(atan2(dy.toDouble(), dx.toDouble()))
                                    .toFloat()

                                tapAngle = (tapAngle + 90f + 360f) % 360f

                                var cumAngle = 0f
                                for (i in segmentAngles.indices) {
                                    cumAngle += segmentAngles[i].second
                                    if (tapAngle <= cumAngle) {
                                        onSegmentTap(i)
                                        break
                                    }
                                }
                            }
                        }
                    } else Modifier
                ),
        ) {
            val stroke = strokeWidth.toPx()
            val padding = stroke / 2f
            val arcSize = Size(size.width - stroke, size.height - stroke)
            val topLeft = Offset(padding, padding)

            val progress = animationProgress.value
            var currentAngle = -90f
            val gapAngle = if (segments.size > 1) 2.5f else 0f

            segments.forEachIndexed { index, segment ->
                val fullSweep = proportions[index] * 360f * progress
                val sweep = (fullSweep - gapAngle).coerceAtLeast(0f)
                val startAngle = currentAngle + gapAngle / 2f
                val isSelected = index == selectedIndex

                if (isSelected) {
                    drawArc(
                        color = segment.color.copy(alpha = 0.3f),
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(
                            width = stroke * selectedScale.value,
                            cap = StrokeCap.Butt,
                        ),
                    )
                }

                drawArc(
                    color = segment.color,
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(
                        width = if (isSelected) stroke * 1.1f else stroke,
                        cap = StrokeCap.Butt,
                    ),
                )

                currentAngle += fullSweep
            }
        }
        centerContent?.invoke()
    }
}
