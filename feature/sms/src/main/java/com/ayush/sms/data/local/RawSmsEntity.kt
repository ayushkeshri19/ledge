package com.ayush.sms.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "raw_sms")
data class RawSmsEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val sender: String,
    val body: String,
    val timestamp: Long,
    val receivedAt: Long
)
