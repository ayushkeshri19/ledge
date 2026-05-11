package com.ayush.sms.data.local.pending

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingTransactionDao {
    @Query("SELECT * FROM pending_transactions WHERE state = 'PENDING' ORDER BY smsTimestamp DESC")
    fun observePending(): Flow<List<PendingTransactionEntity>>

    @Query("SELECT COUNT(*) FROM pending_transactions WHERE state = 'PENDING'")
    fun observePendingCount(): Flow<Int>

    @Query("SELECT * FROM pending_transactions WHERE id = :id")
    suspend fun getById(id: Long): PendingTransactionEntity?

    @Insert
    suspend fun insert(row: PendingTransactionEntity): Long

    @Query("UPDATE pending_transactions SET state = :state WHERE id = :id")
    suspend fun updateState(id: Long, state: String)

    @Query("UPDATE pending_transactions SET state = 'CONFIRMED' WHERE state = 'PENDING'")
    suspend fun confirmAll()
}