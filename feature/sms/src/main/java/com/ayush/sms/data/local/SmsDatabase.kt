package com.ayush.sms.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ayush.sms.data.local.classifier.ClassifierRuleDao
import com.ayush.sms.data.local.classifier.ClassifierRuleEntity
import com.ayush.sms.data.local.parserMiss.ParserMissDao
import com.ayush.sms.data.local.parserMiss.ParserMissEntity
import com.ayush.sms.data.local.pending.PendingTransactionDao
import com.ayush.sms.data.local.pending.PendingTransactionEntity
import com.ayush.sms.data.local.processedSms.ProcessedSmsDao
import com.ayush.sms.data.local.processedSms.ProcessedSmsEntity
import com.ayush.sms.data.local.rawSms.RawSmsDao
import com.ayush.sms.data.local.rawSms.RawSmsEntity

@Database(
    entities = [
        RawSmsEntity::class,
        ProcessedSmsEntity::class,
        PendingTransactionEntity::class,
        ParserMissEntity::class,
        ClassifierRuleEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SmsDatabase : RoomDatabase() {
    abstract fun rawSmsDao(): RawSmsDao
    abstract fun processedSmsDao(): ProcessedSmsDao
    abstract fun pendingTransactionDao(): PendingTransactionDao
    abstract fun parserMissDao(): ParserMissDao
    abstract fun classifierRuleDao(): ClassifierRuleDao
}
