package com.ayush.datastore.domain.repository

import com.ayush.common.theme.ThemeMode
import kotlinx.coroutines.flow.Flow

interface AppDataStoreRepository {
    suspend fun setSignedIn()
    suspend fun isSignedIn(): Boolean
    suspend fun clearData()

    fun observeThemeMode(): Flow<ThemeMode>
    suspend fun setThemeMode(mode: ThemeMode)
}
