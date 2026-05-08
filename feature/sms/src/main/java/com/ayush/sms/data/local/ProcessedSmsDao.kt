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
    suspend fun insert(smsId: String)
}

object SmsIdKey {
    fun forLive(sender: String, body: String, smsTimestamp: Long): String =
        "live:${(sender + body + smsTimestamp).hashCode()}"

    fun forImport(contentResolverId: Long): String = "import:$contentResolverId"
}