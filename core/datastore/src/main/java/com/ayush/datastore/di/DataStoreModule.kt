package com.ayush.datastore.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.ayush.common.auth.PasswordRecoveryStateHolder
import com.ayush.datastore.data.AppDataStore
import com.ayush.datastore.data.auth.PersistedPasswordRecoveryStateHolder
import com.ayush.datastore.data.dataStore
import com.ayush.datastore.data.repository.AppDataStoreRepositoryImpl
import com.ayush.datastore.domain.repository.AppDataStoreRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun providePreferencesDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> {
        return context.dataStore
    }

    @Provides
    @Singleton
    fun providesAppDataStore(
        dataStore: DataStore<Preferences>
    ): AppDataStore = AppDataStore(dataStore = dataStore)

    @Provides
    @Singleton
    fun providesAppDataStoreRepository(
        appDataStore: AppDataStore
    ): AppDataStoreRepository = AppDataStoreRepositoryImpl(appDataStore)

    @Provides
    @Singleton
    fun providePasswordRecoveryStateHolder(
        appDataStore: AppDataStore
    ): PasswordRecoveryStateHolder = PersistedPasswordRecoveryStateHolder(appDataStore)
}
