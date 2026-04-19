package com.ayush.datastore.data.repository


import com.ayush.common.theme.ThemeMode
import com.ayush.datastore.data.AppDataStore
import com.ayush.datastore.domain.repository.AppDataStoreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class AppDataStoreRepositoryImpl @Inject constructor(
    private val dataStore: AppDataStore
) : AppDataStoreRepository {

    override suspend fun setSignedIn() {
        dataStore.putValue(
            key = AppDataStore.PreferencesKey.IS_LOGGED_IN,
            value = true
        )
    }

    override suspend fun isSignedIn(): Boolean {
        return dataStore.getValue(AppDataStore.PreferencesKey.IS_LOGGED_IN, false).first()
    }

    override suspend fun clearData() {
        dataStore.clear()
    }

    override suspend fun clearUserData() {
        dataStore.clearExcept(
            AppDataStore.PreferencesKey.THEME_MODE,
            AppDataStore.PreferencesKey.BIOMETRICS_ENABLED
        )
    }

    override fun observeThemeMode(): Flow<ThemeMode> {
        return dataStore.getValue(
            key = AppDataStore.PreferencesKey.THEME_MODE,
            defaultValue = ThemeMode.SYSTEM.name
        ).map { raw ->
            runCatching { ThemeMode.valueOf(raw) }.getOrDefault(ThemeMode.SYSTEM)
        }
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.putValue(
            key = AppDataStore.PreferencesKey.THEME_MODE,
            value = mode.name
        )
    }

    override fun observeBiometricsStatus(): Flow<Boolean> {
        return dataStore.getValue(
            key = AppDataStore.PreferencesKey.BIOMETRICS_ENABLED,
            defaultValue = false
        )
    }

    override suspend fun setBiometricsEnabled(enabled: Boolean) {
        dataStore.putValue(
            key = AppDataStore.PreferencesKey.BIOMETRICS_ENABLED,
            value = enabled
        )
    }
}
