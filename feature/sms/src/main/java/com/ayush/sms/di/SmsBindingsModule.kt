package com.ayush.sms.di

import com.ayush.sms.data.repository.SmsRepositoryImpl
import com.ayush.sms.domain.repository.SmsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class SmsBindingsModule {

    @Binds
    internal abstract fun bindSmsRepository(impl: SmsRepositoryImpl): SmsRepository
}
