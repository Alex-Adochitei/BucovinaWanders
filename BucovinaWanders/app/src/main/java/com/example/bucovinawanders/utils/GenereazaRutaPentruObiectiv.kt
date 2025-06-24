package com.example.bucovinawanders.utils

import com.google.android.gms.maps.model.*
import com.example.bucovinawanders.api.*
import com.example.bucovinawanders.models.obiective.*

suspend fun genereazaRutaPentruObiectiv(obiectiv: ObiectivTuristicModel, userLocation: android.location.Location, showToast: (String) -> Unit, onRouteGenerated: (List<LatLng>) -> Unit) {
    val maxDistanceKm = 500.0 //distanta maxima admisa pana la obiectiv

    try {
        //calculeaza distanta intre utilizator si obiectiv
        val dist = calcDist(
            userLocation.latitude,
            userLocation.longitude,
            obiectiv.coordonataX,
            obiectiv.coordonataY
        )

        if (dist <= maxDistanceKm) {
            //apeleaza API-ul pentru a genera ruta
            val response = ApiClient.routesApi.genereazaRuta(
                userLat = userLocation.latitude,
                userLng = userLocation.longitude,
                obiectiveIds = listOf(obiectiv.idObiectiv) //trimite ID-ul obiectivului
            )

            //extrage lista de coordonate din raspunsul GeoJSON
            val coordinates = response.geojson.features
                .firstOrNull() //foloseste primul feature din lista
                ?.geometry
                ?.coordinates
                ?.map { LatLng(it[1], it[0]) } //GeoJSON are format [lng, lat] → convertim in [lat, lng]

            if (!coordinates.isNullOrEmpty()) {
                onRouteGenerated(coordinates) //trimitem lista de puncte inapoi
            } else {
                showToast("Nu s-a putut genera ruta.") //daca nu avem puncte valide
            }
        } else {
            showToast("Obiectivul este prea departe.") //afisam mesaj daca e in afara razei permise
        }
    } catch (e: Exception) {
        e.printStackTrace()
        showToast("Eroare la generarea rutei.") //tratam orice exceptie aparuta
    }
}