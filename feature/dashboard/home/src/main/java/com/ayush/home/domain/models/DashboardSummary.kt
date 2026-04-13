package com.ayush.home.domain.models

data class DashboardSummary(
    val totalIncome: Double,
    val totalExpense: Double
) {
    val netBalance: Double get() = totalIncome - totalExpense
}
