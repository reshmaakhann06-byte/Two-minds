package com.example.core

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "two_minds_prefs")

class AppPreferences(private val context: Context) {
    private val keyThemeMode = stringPreferencesKey("theme_mode")
    private val keyUnlockedRole = stringPreferencesKey("unlocked_role")

    val themeModeFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[keyThemeMode] ?: "dark"
    }

    val unlockedRoleFlow: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[keyUnlockedRole]
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[keyThemeMode] = mode
        }
    }

    suspend fun setUnlockedRole(role: String?) {
        context.dataStore.edit { prefs ->
            if (role == null) prefs.remove(keyUnlockedRole)
            else prefs[keyUnlockedRole] = role
        }
    }
}
