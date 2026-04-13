package com.ayush.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.ayush.common.utils.formatAmount

@Composable
fun AnimatedAmount(
    targetAmount: Double,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    prefix: String = ""
) {
    var previousAmount by remember { mutableDoubleStateOf(0.0) }
    val animatedAmount = remember { Animatable(0f) }

    LaunchedEffect(targetAmount) {
        animatedAmount.snapTo(previousAmount.toFloat())
        animatedAmount.animateTo(
            targetValue = targetAmount.toFloat(),
            animationSpec = tween(durationMillis = 800, easing = EaseOutCubic),
        )
        previousAmount = targetAmount
    }

    Text(
        text = "$prefix₹${formatAmount(animatedAmount.value.toLong().toDouble())}",
        style = style,
        color = color,
        modifier = modifier
    )
}