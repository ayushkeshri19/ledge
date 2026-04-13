package com.ayush.budget.domain.models

import androidx.compose.ui.graphics.Color
import com.ayush.database.data.CategoryEntity
import com.ayush.ui.utils.hexToColor

data class Category(
    val id: Long,
    val name: String,
    val color: Color
)

fun CategoryEntity.toBudgetCategory(): Category = Category(
    id = id,
    name = name,
    color = hexToColor(colorHex)
)
