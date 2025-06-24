package com.example.bucovinawanders.api

import retrofit2.*
import retrofit2.http.*

import com.example.bucovinawanders.models.obiective.*

interface ReviewsApi {
    //API pentru lasarea unui review
    @POST("/obiective/review")
    suspend fun sendReview(
        @Header("Authorization") token: String,
        @Body review: ReviewCreateModel
    ): Response<Unit>

    //API care verifica daca a fost deja dat review la un anumit obiectiv
    @GET("/obiective/hasReviewed")
    suspend fun hasReviewed(
        @Header("Authorization") token: String,
        @Query("idObiectiv") idObiectiv: Int?
    ): Response<Boolean>
}