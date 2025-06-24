package com.example.bucovinawanders.models.obiective

//clasa de tip data ce defineste un review creat de un user
data class ReviewCreateModel(
    val idObiectiv: Int,
    val nota: Int,
    val comentariu: String
)