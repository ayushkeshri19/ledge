package com.ayush.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.ayush.database.data.BudgetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Query("SELECT * FROM budgets ORDER BY categoryId IS NULL DESC, createdAt ASC")
    fun getAllBudgets(): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets")
    suspend fun getAllBudgetsSnapshot(): List<BudgetEntity>

    @Query("SELECT * FROM budgets WHERE categoryId = :categoryId LIMIT 1")
    suspend fun getBudgetByCategory(categoryId: Long): BudgetEntity?

    @Query("SELECT * FROM budgets WHERE categoryId IS NULL LIMIT 1")
    suspend fun getOverallBudget(): BudgetEntity?

    @Upsert
    suspend fun upsert(budget: BudgetEntity)

    @Query("DELETE FROM budgets WHERE id = :id")
    suspend fun deleteById(id: Long)
}
