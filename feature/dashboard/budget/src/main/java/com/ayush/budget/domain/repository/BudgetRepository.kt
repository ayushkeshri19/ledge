package com.ayush.budget.domain.repository

import com.ayush.budget.domain.models.BudgetWithSpent
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    fun observeAllBudgetsWithSpent(): Flow<List<BudgetWithSpent>>
    suspend fun saveBudget(categoryId: Long?, amount: Double, warningThreshold: Int)
    suspend fun deleteBudget(id: Long)
    suspend fun syncFromRemote()
}