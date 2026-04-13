package com.ayush.budget.domain.usecase

import com.ayush.budget.domain.models.BudgetWithSpent
import com.ayush.budget.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetBudgetsUseCase @Inject constructor(
    private val repository: BudgetRepository
) {
    operator fun invoke(): Flow<List<BudgetWithSpent>> = repository.observeAllBudgetsWithSpent()
}
