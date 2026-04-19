package com.ayush.datastore.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val DATASTORE_NAME = "app_preferences"

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = DATASTORE_NAME
)

class AppDataStore @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    object PreferencesKey {
        val IS_LOGGED_IN = booleanPreferencesKey("IS_LOGGED_IN")
        val THEME_MODE = stringPreferencesKey("THEME_MODE")
        val BIOMETRICS_ENABLED = booleanPreferencesKey("BIOMETRICS")
    }

    suspend fun <T> putValue(
        key: Preferences.Key<T>,
        value: T
    ) {
        withContext(Dispatchers.IO) {
            dataStore.edit { preferences ->
                preferences[key] = value
            }
        }
    }

    fun <T> getValue(
        key: Preferences.Key<T>,
        defaultValue: T
    ): Flow<T> {
        return dataStore.data.map { preferences ->
            preferences[key] ?: defaultValue
        }
    }

    suspend fun removeKey(key: Preferences.Key<*>) {
        withContext(Dispatchers.IO) {
            dataStore.edit { preferences ->
                preferences.remove(key)
            }
        }
    }

    suspend fun clear() {
        withContext(Dispatchers.IO) {
            dataStore.edit { it.clear() }
        }
    }

    suspend fun clearExcept(vararg preservedKeys: Preferences.Key<*>) {
        withContext(Dispatchers.IO) {
            val preserved = preservedKeys.toSet()
            dataStore.edit { preferences ->
                preferences.asMap().keys
                    .filterNot { it in preserved }
                    .forEach { preferences.remove(it) }
            }
        }
    }
}