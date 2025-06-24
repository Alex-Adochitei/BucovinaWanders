package com.example.bucovinawanders.utils

import kotlin.math.*

fun calcDist(
    lat1: Double, lon1: Double,
    lat2: Double, lon2: Double
): Double {
    val p = Math.PI / 180 //conversie din grade in radiani
    val a = 0.5 - cos((lat2 - lat1) * p)/2 +
            cos(lat1 * p) * cos(lat2 * p) *
            (1 - cos((lon2 - lon1) * p)) / 2
    return 12742 * asin(sqrt(a)) //formula haversine, 12742 = diametrul Pamantului in km
}