package com.example.bucovinawanders.utils

import android.os.*
import androidx.annotation.*
import com.google.gson.*
import com.google.gson.reflect.*
import java.text.*
import java.time.*
import java.time.format.*
import java.util.*

@RequiresApi(Build.VERSION_CODES.O)
fun programFormat(programJson: String): List<Triple<String, String, Boolean>> {
    return try {
        val gson = Gson() //initializare gson, librarie de parsare json
        val mapType = object : TypeToken<Map<String, String>>() {}.type //variabila pentru a converti un json in map
        val programMap: Map<String, String> = gson.fromJson(programJson, mapType) //transforma json in map

        //obtine ziua curenta
        val currentDay = LocalDate.now()
            .dayOfWeek
            .getDisplayName(TextStyle.FULL, Locale("ro", "RO"))
            .replaceFirstChar { it.titlecase(Locale("ro", "RO")) }

        //mapam fiecare element din garta in formatul dorit
        programMap.map { (zi, interval) ->
            Triple(zi, interval, normalize(zi) == normalize(currentDay)) //zi, interval, esteZiuaCurenta
        }
    } catch (e: Exception) {
        println("Error parsing program: ${e.message}")
        emptyList()
    }
}
fun normalize(text: String): String {
    return Normalizer.normalize(text, Normalizer.Form.NFD)
        .replace("\\p{InCombiningDiacriticalMarks}".toRegex(), "")
        .lowercase()
}