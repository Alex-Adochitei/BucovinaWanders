package com.example.bucovinawanders.utils

import android.os.*
import java.time.*
import java.time.format.*
import java.util.*

//functie care formateaza data
fun formatDate(dateString: String): String {
    return try {
        //parsam data primita ca string intr-un obiect de tip LocalDate
        val parsedDate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDate.parse(dateString) //transformam stringul in LocalDate
        } else {
            TODO("VERSION.SDK_INT < O")
        }

        //formatam data folosind un pattern specific
        val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ro"))

        //aplicam patternul la data formatata si o returnam
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            parsedDate.format(formatter)
        } else {
            TODO("VERSION.SDK_INT < O")
        }
    } catch (_ : Exception) {
        dateString //fallback in caz de eroare
    }
}