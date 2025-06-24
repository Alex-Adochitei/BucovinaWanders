package com.example.bucovinawanders.models.users

//clasa de tip data ce contine datele despre obiectivele vizitate de user
data class UserVisitedModel(
    val idObiectiv: Int,
    val nume: String,
    val dataVizita: String
)