package com.example.bucovinawanders.models.users

//clasa de tip data care modeleaza un request de creare a unei vizite de catre user
data class UserVisitCreateModel(
    val idObiectiv: Int,
    val dataVizita: String? = null
)