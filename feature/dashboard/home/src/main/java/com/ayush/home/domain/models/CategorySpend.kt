package com.ayush.home.domain.models

import androidx.compose.ui.graphics.Color

data class CategorySpend(
    val categoryId: Long?,
    val categoryName: String,
    val color: Color,
    val amount: Double,
    val percentage: Float,
)
