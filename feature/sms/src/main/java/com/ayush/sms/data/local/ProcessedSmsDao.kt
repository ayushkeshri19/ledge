package com.ayush.sms.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ProcessedSmsDao {

    @Query("SELECT EXISTS(SELECT 1 FROM processed_sms WHERE smsId = :smsId)")
    suspend fun exists(smsId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(row: ProcessedSmsEntity)
}

object SmsIdKey {
    fun of(sender: String, body: String, smsTimestamp: Long): String =
        (sender + body + smsTimestamp).hashCode().toString()
}