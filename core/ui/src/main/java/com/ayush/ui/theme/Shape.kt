package com.ayush.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

object LedgeRadius {
    val xs = 8.dp
    val small = 12.dp
    val medium = 16.dp
    val large = 20.dp
    val xl = 22.dp
    val xxl = 28.dp
    val phone = 52.dp
    val pill = 100.dp
}

val LedgeShapes = Shapes(
    extraSmall = RoundedCornerShape(LedgeRadius.xs),
    small = RoundedCornerShape(LedgeRadius.medium),
    medium = RoundedCornerShape(LedgeRadius.large),
    large = RoundedCornerShape(LedgeRadius.xxl),
    extraLarge = RoundedCornerShape(LedgeRadius.phone)
)
