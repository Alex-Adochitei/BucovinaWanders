package com.example.bucovinawanders.utils

import com.google.maps.android.*
import com.google.android.gms.maps.model.*

//calculeaza distanta in km intre doua puncte LatLng
fun distanceInKm(point1: LatLng, point2: LatLng): Double {
    return SphericalUtil.computeDistanceBetween(point1, point2) / 1000.0
}