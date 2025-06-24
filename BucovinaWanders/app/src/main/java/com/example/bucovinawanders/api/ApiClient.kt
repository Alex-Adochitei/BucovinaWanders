package com.example.bucovinawanders.api

import retrofit2.*
import retrofit2.converter.gson.*

//configure Retrofit
object ApiClient {
    //private const val BASE_URL = "http://10.0.2.2:8000"  //emulator
    private const val BASE_URL = "https://1ac8-86-121-67-217.ngrok-free.app"  //host
    //private const val BASE_URL = "http://192.168.1.104:8000"  //telefon - ip pc

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    //API pentru obiective
    val obiectiveApi: ObiectiveApi by lazy {
        retrofit.create(ObiectiveApi::class.java)
    }

    //API pentru utilizatori
    val usersApi: UsersApi by lazy {
        retrofit.create(UsersApi::class.java)
    }

    //API pentru salvari
    val savesApi: SavesApi by lazy {
        retrofit.create(SavesApi::class.java)
    }

    //API pentru obiective vizitate
    val visitsApi: VisitsApi by lazy {
        retrofit.create(VisitsApi::class.java)
    }

    //API pentru review-uri
    val reviewsApi: ReviewsApi by lazy {
        retrofit.create(ReviewsApi::class.java)
    }

    val routesApi: RutaApi by lazy {
        retrofit.create(RutaApi::class.java)
    }
}