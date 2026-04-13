package com.ayush.home.domain.models

import androidx.compose.ui.graphics.Color

data class RecentTransaction(
    val id: Long,
    val amount: Double,
    val isExpense: Boolean,
    val note: String,
    val categoryName: String?,
    val categoryColor: Color?,
    val date: Long,
)
