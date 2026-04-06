package com.example.theweatherapp.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.theweatherapp.data.api.WeatherResponse
import com.example.theweatherapp.data.repository.WeatherRepository
import com.example.theweatherapp.ui.dashboard.home.model.SavedAddress
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository
) : ViewModel() {

    private val _weatherData = MutableLiveData<WeatherResponse?>()
    val weatherData: LiveData<WeatherResponse?> = _weatherData

    private val _currentAddress = MutableLiveData<String?>()
    val currentAddress: LiveData<String?> = _currentAddress

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    // Saved Addresses
    private val _savedAddresses = MutableLiveData<List<SavedAddress>>(emptyList())
    val savedAddresses: LiveData<List<SavedAddress>> = _savedAddresses

    fun addAddress(pincode: String, cityName: String, stateName: String, fullAddress: String) {
        val currentList = _savedAddresses.value?.toMutableList() ?: mutableListOf()
        val newAddress = SavedAddress(
            pincode = pincode,
            cityName = cityName,
            stateName = stateName,
            fullAddress = fullAddress
        )
        currentList.add(newAddress)
        _savedAddresses.value = currentList
    }

    fun deleteAddress(savedAddress: SavedAddress) {
        val currentList = _savedAddresses.value?.toMutableList() ?: mutableListOf()
        currentList.remove(savedAddress)
        _savedAddresses.value = currentList
    }

    fun setAddress(address: String) {
        _currentAddress.postValue(address)
    }
    
    fun setLoading(loading: Boolean) {
        _isLoading.postValue(loading)
    }

    fun getWeather(query: String, apiKey: String) {
        Log.d("WeatherViewModel", "Fetching weather for $query...")
        setLoading(true)
        _error.value = null
        
        viewModelScope.launch {
            try {
                val response = repository.getForecast(query, apiKey)
                Log.d("WeatherViewModel", "Response received: ${response.location.name}")
                _weatherData.postValue(response)
            } catch (e: Exception) {
                Log.e("WeatherViewModel", "Error fetching weather", e)
                _error.postValue(e.message ?: "Unknown error occurred")
            } finally {
                setLoading(false)
            }
        }
    }
}
