package com.example.bucovinawanders.models.users

//clasa de tip data ce defineste datele de register pentru user
data class UserRegisterRequestModel(
    val userName: String,
    val userEmail: String,
    val userPassword: String
)