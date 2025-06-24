package com.example.bucovinawanders.api

import android.util.*
import java.net.*
import kotlinx.coroutines.*
import org.json.*

//functie ce returneaza temperatura dupa coordonatele lat si lon primite ca parametru
suspend fun getTemperature(lat: Double, lon: Double): String {
    val apiKey = "9e15809bc5094baf952125038251302"
    val urlString = "https://api.weatherapi.com/v1/current.json?key=$apiKey&q=$lat,$lon&aqi=no"

    //executa cererea pe un thread de tip IO
    return withContext(Dispatchers.IO) {
        try {
            val url = URL(urlString) //cream obiectul url
            val connection = url.openConnection() as HttpURLConnection //deschidem conexiunea http
            connection.requestMethod = "GET" //setam metoda http

            val responseCode = connection.responseCode //salvam codul de raspuns
            Log.d("WeatherAPI", "Response Code: $responseCode")

            //verificam codul de raspuns
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val stream = connection.inputStream.bufferedReader().use { it.readText() } //citeste streamul de date
                Log.d("WeatherAPI", "Response: $stream") //afiseaza streamul de date

                val json = JSONObject(stream) //parseaza streamul de date in json
                val temp = json.getJSONObject("current").getDouble("temp_c") //salvare temperatura
                return@withContext "$temp°C" //returneaza temperatura
            } else {
                return@withContext "N/A" //returneaza N/A daca codul de raspuns nu este 200
            }
        } catch (e: Exception) {
            Log.e("WeatherAPI", "Error fetching temperature: ${e.message}") //afiseaza eroarea daca apare
            return@withContext "Error"
        }
    }
}