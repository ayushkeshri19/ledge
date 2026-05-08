package com.ayush.sms.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        RawSmsEntity::class,
        ProcessedSmsEntity::class,
        PendingTransactionEntity::class,
        ParserMissEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SmsDatabase : RoomDatabase() {
    abstract fun rawSmsDao(): RawSmsDao
    abstract fun processedSmsDao(): ProcessedSmsDao
    abstract fun pendingTransactionDao(): PendingTransactionDao
    abstract fun parserMissDao(): ParserMissDao
}
