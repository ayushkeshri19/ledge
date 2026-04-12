package com.ayush.transactions.domain.models

import androidx.compose.ui.graphics.Color
import com.ayush.database.data.CategoryEntity

data class Category(
    val id: Long,
    val name: String,
    val icon: String,
    val color: Color,
    val isDefault: Boolean,
)

fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    name = name,
    icon = icon,
    color = try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (_: Exception) {
        Color(0xFFD1D8E0)
    },
    isDefault = isDefault,
)
