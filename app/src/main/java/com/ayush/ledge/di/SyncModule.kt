package com.ayush.ledge.di

import com.ayush.common.sync.SyncStateHolder
import com.ayush.ledge.sync.DefaultSyncStateHolder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SyncModule {

    @Provides
    @Singleton
    fun provideSyncStateHolder(): SyncStateHolder = DefaultSyncStateHolder()
}
