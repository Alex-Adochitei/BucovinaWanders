package com.example.bucovinawanders.models.obiective

//raspunsul rutei
data class RutaResponse(
    val geojson: Geojson, //obiectul care contine datele rutei in format GeoJSON
    val obiective: List<ObiectivResponse> //lista cu obiectivele turistice de pe ruta
)

data class Geojson(
    val type: String,
    val features: List<Feature> //lista de elemente ale rutei
)

data class Feature(
    val type: String,
    val geometry: Geometry,
    val properties: Properties
)

data class Properties(
    val summary: Summary
)

data class Summary(
    val distance: Double,
    val duration: Double
)

data class Geometry(
    val type: String,
    val coordinates: List<List<Double>>
)

//obiectiv turistic prezent in ruta
data class ObiectivResponse(
    val idObiectiv: Int,
    val nume: String,
    val lat: Double,
    val lng: Double
)