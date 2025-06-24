package com.example.bucovinawanders.utils

//functie pentru a converti un float in 2 zecimale
fun Float?.twoDecimalPlaces(): Float {
    return ((this ?: 0f) * 100).toInt() / 100f
}