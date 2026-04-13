package com.ayush.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.ayush.database.data.BudgetEntity
import com.ayush.database.data.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Query("SELECT * FROM budgets WHERE syncStatus != 'PENDING_DELETE' ORDER BY categoryId IS NULL DESC, createdAt ASC")
    fun getAllBudgets(): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets")
    suspend fun getAllBudgetsSnapshot(): List<BudgetEntity>

    @Query("SELECT * FROM budgets WHERE categoryId = :categoryId AND syncStatus != 'PENDING_DELETE' LIMIT 1")
    suspend fun getBudgetByCategory(categoryId: Long): BudgetEntity?

    @Query("SELECT * FROM budgets WHERE categoryId IS NULL AND syncStatus != 'PENDING_DELETE' LIMIT 1")
    suspend fun getOverallBudget(): BudgetEntity?

    @Query("SELECT * FROM budgets WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getByRemoteId(remoteId: String): BudgetEntity?

    @Upsert
    suspend fun upsert(budget: BudgetEntity)

    @Query("DELETE FROM budgets WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE budgets SET syncStatus = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: Long, status: SyncStatus)

    @Query("SELECT * FROM budgets WHERE syncStatus != 'SYNCED' ORDER BY createdAt ASC")
    suspend fun getPendingSyncBudgets(): List<BudgetEntity>
}
