package com.belltree.pomodoroshareapp.infra.datastore

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * DataStore wrapper for user simple preferences (e.g., auth token, display name, etc.).
 */
class UserPreferencesDataStore(private val context: Context) {
    private val Context.dataStore by preferencesDataStore(name = DATA_STORE_NAME)

    suspend fun saveAuthToken(token: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_AUTH_TOKEN] = token
        }
    }

    val authTokenFlow: Flow<String?> = context.dataStore.data.map { prefs: Preferences ->
        prefs[KEY_AUTH_TOKEN]
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }

    companion object {
        private const val DATA_STORE_NAME = "user_prefs"
        private val KEY_AUTH_TOKEN = stringPreferencesKey("auth_token")
    }
}
