package com.ayush.ui.utils

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.toColorInt

fun hexToColor(hex: String): Color {
    return try {
        Color(hex.toColorInt())
    } catch (e: Exception) {
        Color(0xFFD1D8E0)
    }
}