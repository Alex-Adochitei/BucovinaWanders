package com.example.bucovinawanders.models.obiective

//clasa de tip data care contine datele despre un obiect turistic
data class ObiectivTuristicModel(
    val idObiectiv: Int,
    val nume: String,
    val coordonataX: Double,
    val coordonataY: Double,
    val stare: String,
    val program: String,
    val contact: String,
    val descriere: String?,
    val notaRecenzii: Float?,
    val numarRecenzii: Int?,
    val idTip: Int,
    val poze: List<PozeObiectivModel>
)