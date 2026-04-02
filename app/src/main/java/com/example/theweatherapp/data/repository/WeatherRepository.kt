package com.example.theweatherapp.data.repository

import com.example.theweatherapp.data.api.WeatherApi
import javax.inject.Inject

class WeatherRepository @Inject constructor(
    private val api: WeatherApi
) {
    suspend fun getCurrentWeather(city: String, apiKey: String) = 
        api.getCurrentWeather(city, apiKey)
}
