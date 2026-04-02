package com.example.theweatherapp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.theweatherapp.data.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository
) : ViewModel() {

    fun getWeather(city: String, apiKey: String) {
        viewModelScope.launch {
            try {
                val response = repository.getCurrentWeather(city, apiKey)
                // Update UI state with response
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
