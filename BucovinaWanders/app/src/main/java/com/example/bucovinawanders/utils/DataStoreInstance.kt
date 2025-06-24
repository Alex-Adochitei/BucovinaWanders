package com.example.bucovinawanders.utils

import android.content.*
import androidx.datastore.preferences.*

val Context.dataStore by preferencesDataStore(name = "settings")  //extensie care creeaza datastore de tip preferences