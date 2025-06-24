package com.example.bucovinawanders.utils

import android.content.*
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.*

object DistancePreferences {
    private val MAX_DISTANCE_KEY = floatPreferencesKey("max_distance_km") //cheia pentru distanta salvata

    //functie care salveaza o distanta in DataStore
    suspend fun setDistance(context: Context, distance: Float) {
        context.dataStore.edit { settings ->
            settings[MAX_DISTANCE_KEY] = distance
        }
    }

    //functie care returneaza distanta salvata, sau 10 km daca nu exista
    fun getDistance(context: Context): Flow<Float> {
        return context.dataStore.data.map { preferences ->
            preferences[MAX_DISTANCE_KEY] ?: 10f
        }
    }
}