package com.ayush.sms.di

import com.ayush.common.transactions.PendingReviewCountSource
import com.ayush.sms.data.PendingReviewCountSourceImpl
import com.ayush.sms.data.permission.SmsPermissionManagerImpl
import com.ayush.sms.data.repository.SmsRepositoryImpl
import com.ayush.sms.domain.permission.SmsPermissionManager
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

    @Binds
    internal abstract fun bindSmsPermissionManager(impl: SmsPermissionManagerImpl): SmsPermissionManager

    @Binds
    internal abstract fun bindSmsReviewCountSourceImpl(impl: PendingReviewCountSourceImpl): PendingReviewCountSource
}
