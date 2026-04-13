package com.example.theweatherapp.data.api

import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    @GET("forecast.json")
    suspend fun getForecast(
        @Query("q") query: String,
        @Query("key") apiKey: String,
        @Query("days") days: Int = 10,
        @Query("aqi") aqi: String = "yes",
        @Query("alerts") alerts: String = "yes"
    ): WeatherResponse
}
