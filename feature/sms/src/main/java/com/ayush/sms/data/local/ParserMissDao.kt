package com.ayush.sms.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ParserMissDao {
    @Insert
    suspend fun insert(row: ParserMissEntity): Long

    @Query("SELECT * FROM parser_misses ORDER BY timestamp DESC LIMIT :limit")
    fun observeRecent(limit: Int = 200): Flow<List<ParserMissEntity>>

    @Query("DELETE FROM parser_misses")
    suspend fun clear()
}
