package com.ayush.budget.domain.usecase

import com.ayush.budget.domain.repository.BudgetRepository
import javax.inject.Inject

class DeleteBudgetUseCase @Inject constructor(
    private val repository: BudgetRepository
) {
    suspend operator fun invoke(id: Long) = repository.deleteBudget(id)
}
