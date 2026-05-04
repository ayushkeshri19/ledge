package com.ayush.sms.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [RawSmsEntity::class],
    version = 1,
    exportSchema = false
)
abstract class SmsDatabase : RoomDatabase() {
    abstract fun rawSmsDao(): RawSmsDao
}
