package com.ayush.sms.data.local.processedSms

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "processed_sms")
data class ProcessedSmsEntity(
    @PrimaryKey val smsId: String,
    val processedAt: Long
)
