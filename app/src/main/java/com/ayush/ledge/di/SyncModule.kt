package com.ayush.ledge.di

import com.ayush.common.sync.SyncOrchestrator
import com.ayush.common.sync.SyncStateHolder
import com.ayush.ledge.sync.DefaultSyncStateHolder
import com.ayush.ledge.sync.SyncAllUserDataUseCase
import dagger.Binds
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

@Module
@InstallIn(SingletonComponent::class)
abstract class SyncBindingsModule {

    @Binds
    @Singleton
    abstract fun bindSyncOrchestrator(impl: SyncAllUserDataUseCase): SyncOrchestrator
}
