package com.ayush.budget.domain.models

data class BudgetWithSpent(
    val budget: Budget,
    val spent: Double,
) {
    val ratio: Float get() = if (budget.amount > 0) (spent / budget.amount).toFloat() else 0f
    val remaining: Double get() = (budget.amount - spent).coerceAtLeast(0.0)
    val overBy: Double get() = (spent - budget.amount).coerceAtLeast(0.0)
    val warningRatio: Float get() = budget.warningThreshold / 100f
    val status: BudgetStatus
        get() = when {
            ratio >= 1.0f -> BudgetStatus.EXCEEDED
            ratio >= warningRatio -> BudgetStatus.WARNING
            else -> BudgetStatus.NORMAL
        }
}
