package com.belltree.pomodoroshareapp.infra.datastore

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private const val DATA_STORE_NAME = "id_history_prefs"
private val Context.idHistoryDataStore by preferencesDataStore(name = DATA_STORE_NAME)

class IdHistoryStore(context: Context) {

    private val appContext: Context = context.applicationContext

    val historyFlow: Flow<List<String>> = appContext.idHistoryDataStore.data.map { prefs: Preferences ->
        prefs[KEY_HISTORY]?.split(DELIM)?.filter { it.isNotBlank() } ?: emptyList()
    }

    suspend fun saveId(id: String, maxSize: Int = 10) {
        val trimmed = id.trim()
        if (trimmed.isEmpty()) return
        appContext.idHistoryDataStore.edit { prefs ->
            val current = prefs[KEY_HISTORY]?.split(DELIM)?.toMutableList() ?: mutableListOf()
            current.removeAll { it.equals(trimmed, ignoreCase = true) }
            current.add(0, trimmed)
            while (current.size > maxSize) current.removeLast()
            prefs[KEY_HISTORY] = current.joinToString(DELIM)
        }
    }

    companion object {
        private const val DELIM = "\u0001" // unlikely delimiter
        private val KEY_HISTORY = stringPreferencesKey("id_history")
    }
}


