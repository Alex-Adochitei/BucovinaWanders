package com.example.bucovinawanders.api

import retrofit2.http.*

import com.example.bucovinawanders.models.obiective.*

interface ObiectiveApi {
    //API pentru optinerea obiectivelor turistice
    @GET("/obiective/")
    suspend fun getObiective(): List<ObiectivTuristicModel>

    //API pentru a optine un obiectiv turistic in functie de tip
    @GET("/obiective/tip/{idTip}")
    suspend fun getObiectiveByTip(@Path("idTip") idTip: Int): List<ObiectivTuristicModel>

    //API pentru a optine un obiectiv turistic in functie de status (open)
    @GET("/obiective/open")
    suspend fun getOpenObiective(): List<ObiectivTuristicModel>

    //API pentru a obtine statusul unui obiectiv dupa id
    @GET("/obiective/status/{idObiectiv}")
    suspend fun getObiectivStatus(@Path("idObiectiv") idObiectiv: Int): ObiectivStatusResponse
}