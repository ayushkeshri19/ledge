package com.ayush.sms.di

import android.content.Context
import androidx.room.Room
import com.ayush.sms.data.local.SmsDatabase
import com.ayush.sms.data.local.classifier.ClassifierRuleDao
import com.ayush.sms.data.local.parserMiss.ParserMissDao
import com.ayush.sms.data.local.pending.PendingTransactionDao
import com.ayush.sms.data.local.processedSms.ProcessedSmsDao
import com.ayush.sms.data.local.rawSms.RawSmsDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SmsModule {

    @Provides
    @Singleton
    fun provideSmsDatabase(@ApplicationContext context: Context): SmsDatabase =
        Room.databaseBuilder(
            context,
            SmsDatabase::class.java,
            "sms.db"
        )
            .fallbackToDestructiveMigration(dropAllTables = true)
            .build()

    @Provides
    fun provideRawSmsDao(database: SmsDatabase): RawSmsDao = database.rawSmsDao()

    @Provides
    fun provideProcessedSmsDao(database: SmsDatabase): ProcessedSmsDao = database.processedSmsDao()

    @Provides
    fun providePendingTransactionDao(database: SmsDatabase): PendingTransactionDao =
        database.pendingTransactionDao()

    @Provides
    fun provideParserMissDao(database: SmsDatabase): ParserMissDao = database.parserMissDao()

    @Provides
    fun providesClassifierRuleDao(database: SmsDatabase): ClassifierRuleDao = database.classifierRuleDao()
}
