package com.ayush.datastore.domain.repository

import com.ayush.common.theme.ThemeMode
import kotlinx.coroutines.flow.Flow

interface AppDataStoreRepository {
    suspend fun setSignedIn()
    suspend fun isSignedIn(): Boolean
    suspend fun clearData()
    suspend fun clearUserData()

    fun observeThemeMode(): Flow<ThemeMode>
    suspend fun setThemeMode(mode: ThemeMode)

    fun observeBiometricsStatus(): Flow<Boolean>
    suspend fun setBiometricsEnabled(enabled: Boolean)

    fun observeHasSeenOnboarding(): Flow<Boolean>
    suspend fun setOnboardingSeen()
}
