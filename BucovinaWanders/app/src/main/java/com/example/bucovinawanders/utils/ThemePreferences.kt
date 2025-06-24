package com.example.bucovinawanders.utils

import android.content.*
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.*

object ThemePreferences {
    private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode") //cheie pentru tema

    //functie care returneaza un flow de tip boolean
    fun getTheme(context: Context): Flow<Boolean> {
        return context.dataStore.data.map { preferences ->
            preferences[DARK_MODE_KEY] == true //default: Light mode
        }
    }

    //functie care seteaza tema
    suspend fun setTheme(context: Context, isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = isDark
        }
    }
}