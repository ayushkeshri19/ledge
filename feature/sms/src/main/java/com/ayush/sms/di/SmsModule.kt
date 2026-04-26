package com.ayush.sms.di

import android.content.Context
import androidx.room.Room
import com.ayush.sms.data.local.RawSmsDao
import com.ayush.sms.data.local.SmsDatabase
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
        ).build()

    @Provides
    fun provideRawSmsDao(database: SmsDatabase): RawSmsDao = database.rawSmsDao()
}
