package com.example.theweatherapp.data.repository

import com.example.theweatherapp.data.api.WeatherApi
import com.example.theweatherapp.data.api.WeatherResponse
import javax.inject.Inject

class WeatherRepository @Inject constructor(
    private val api: WeatherApi
) {
    suspend fun getForecast(query: String, apiKey: String): WeatherResponse = 
        api.getForecast(query, apiKey)
}
