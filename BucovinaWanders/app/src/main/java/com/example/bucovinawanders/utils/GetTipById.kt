package com.example.bucovinawanders.utils

fun getTipById(idTip: Int): String {
    return when (idTip) {
        1 -> "Cetate"
        2 -> "Statuie"
        3 -> "Muzeu"
        4 -> "Parc"
        5 -> "Manastire"
        else -> "Unknown"
    }
}