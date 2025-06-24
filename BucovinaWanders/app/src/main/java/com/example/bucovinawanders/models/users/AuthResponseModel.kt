package com.example.bucovinawanders.models.users

import com.google.gson.annotations.*

//clasa de tip data care modeleaza raspunsul primit de la server dupa autentificare
data class AuthResponseModel(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String,
    @SerializedName("userName") val userName: String
)