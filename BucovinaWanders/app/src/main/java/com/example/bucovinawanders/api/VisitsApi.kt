package com.example.bucovinawanders.api

import retrofit2.*
import retrofit2.http.*

import com.example.bucovinawanders.models.users.*

interface VisitsApi {
    //API pentru a marca un obiectiv ca vizitat
    @POST("/userVisits/visit")
    suspend fun visitObiectiv(
        @Header("Authorization") token: String,
        @Body body: UserVisitCreateModel
    ): Response<Void>

    //API pentru a obtine lista obiectivelor vizitate
    @GET("/userVisits/view")
    suspend fun getVisitedObiective(
        @Header("Authorization") authHeader: String
    ): Response<List<UserVisitedModel>>
}