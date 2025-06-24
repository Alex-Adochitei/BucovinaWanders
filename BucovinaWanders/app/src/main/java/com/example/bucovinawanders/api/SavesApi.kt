package com.example.bucovinawanders.api

import retrofit2.http.*
import retrofit2.*

import com.example.bucovinawanders.models.obiective.*
import com.example.bucovinawanders.models.users.*

interface SavesApi {
    //API pentru a salva un obiectiv
    @POST("/userSaves/save")
    suspend fun saveObiectiv(@Header("Authorization") token: String, @Body body: UserSavesCreateModel): Response<Void>

    //API pentru a vedea lista obiectivelor salvate de un user
    @GET("/userSaves/view")
    suspend fun getSavedObiective(@Header("Authorization") authHeader: String): Response<List<ObiectivTuristicModel>>

    //API pentru a sterge un obiectiv salvat de un user
    @DELETE("/userSaves/{idObiectiv}")
    suspend fun deleteObiectiv(
        @Header("Authorization") authHeader: String,
        @Path("idObiectiv") idObiectiv: Int
    ): Response<Unit>
}