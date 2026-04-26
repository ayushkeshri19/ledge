package com.ayush.sms.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RawSmsDao {

    @Insert
    suspend fun insert(entity: RawSmsEntity): Long

    @Query("SELECT * FROM raw_sms ORDER BY receivedAt DESC LIMIT :limit")
    fun observeRecent(limit: Int = 50): Flow<List<RawSmsEntity>>

    @Query("DELETE FROM raw_sms")
    suspend fun clear()
}
