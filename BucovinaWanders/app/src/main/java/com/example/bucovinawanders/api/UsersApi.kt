package com.example.bucovinawanders.api

import retrofit2.*
import retrofit2.http.*

import com.example.bucovinawanders.models.users.*

interface UsersApi {
    //API pentru inregistrarea unui user
    @POST("/users/register")
    fun registerUser(@Body request: UserRegisterRequestModel): Call<UserResponseModel>

    //API pentru autentificarea unui user
    @POST("/auth/login")
    @FormUrlEncoded
    fun loginUser(
        @Field("username") userEmail : String,
        @Field("password") userPassword : String
    ): Call<AuthResponseModel>

    //API pentru stergerea unui user
    @DELETE("/users/{email}")
    fun deleteUser(@Path("email") email: String): Call<Void>
}