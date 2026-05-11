package com.ayush.ui.extension

import androidx.compose.ui.graphics.Color
import com.ayush.common.models.Category
import com.ayush.ui.utils.hexToColor

val Category.color: Color
    get() = hexToColor(colorHex)
