package com.ayush.budget.domain.usecase

import com.ayush.budget.domain.repository.BudgetRepository
import javax.inject.Inject

class SaveBudgetUseCase @Inject constructor(
    private val repository: BudgetRepository
) {
    suspend operator fun invoke(
        categoryId: Long?,
        amount: Double,
        warningThreshold: Int
    ): Result<Unit> {
        if (amount <= 0) return Result.failure(IllegalArgumentException("Amount must be positive"))
        if (warningThreshold !in 1..99) return Result.failure(IllegalArgumentException("Threshold must be 1–99"))
        repository.saveBudget(categoryId, amount, warningThreshold)
        return Result.success(Unit)
    }
}
