package com.ayush.ui.utils

import androidx.compose.ui.graphics.Color

fun hexToColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        Color(0xFFD1D8E0)
    }
}