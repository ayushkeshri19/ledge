package com.ayush.budget.domain.models

import androidx.compose.ui.graphics.Color

data class Budget(
    val id: Long,
    val categoryId: Long?,
    val categoryName: String?,
    val categoryColor: Color?,
    val amount: Double,
    val warningThreshold: Int,
)
