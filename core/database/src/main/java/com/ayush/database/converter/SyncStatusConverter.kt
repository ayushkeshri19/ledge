package com.ayush.database.converter

import androidx.room.TypeConverter
import com.ayush.database.data.SyncStatus

class SyncStatusConverter {
    @TypeConverter
    fun fromSyncStatus(status: SyncStatus): String = status.name

    @TypeConverter
    fun toSyncStatus(value: String): SyncStatus = SyncStatus.valueOf(value)
}