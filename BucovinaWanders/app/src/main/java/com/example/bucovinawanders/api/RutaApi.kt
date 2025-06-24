package com.example.bucovinawanders.api

import retrofit2.http.*

import com.example.bucovinawanders.models.obiective.*

interface RutaApi {
    //API pentru generarea rutei auto
    @POST("/ruta/genereaza")
    suspend fun genereazaRuta(
        @Query("user_lat") userLat: Double,
        @Query("user_lng") userLng: Double,
        @Query("obiective_ids") obiectiveIds: List<Int>
    ): RutaResponse
}
