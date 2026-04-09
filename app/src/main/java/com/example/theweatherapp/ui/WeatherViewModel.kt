package com.example.theweatherapp.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.theweatherapp.data.api.WeatherResponse
import com.example.theweatherapp.data.repository.AddressRepository
import com.example.theweatherapp.data.repository.WeatherRepository
import com.example.theweatherapp.ui.dashboard.home.model.SavedAddress
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val repository: WeatherRepository,
    private val addressRepository: AddressRepository
) : ViewModel() {

    private val _weatherData = MutableLiveData<WeatherResponse?>()
    val weatherData: LiveData<WeatherResponse?> = _weatherData

    private val _currentAddress = MutableLiveData<String?>()
    val currentAddress: LiveData<String?> = _currentAddress

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    val WEATHER_API_KEY = "e2ea45395b5f4367bc9135347260204"

    // Saved Addresses from Repository
    val savedAddresses: LiveData<List<SavedAddress>> = addressRepository.savedAddresses

    fun addAddress(
        addressType: String,
        cityName: String,
        stateName: String,
        pincode: String,
        fullAddress: String,
        landmark: String? = null,
        latitude: Double = 0.0,
        longitude: Double = 0.0
    ) {
        val currentList = addressRepository.savedAddresses.value ?: emptyList()
        val isFirst = currentList.isEmpty()
        
        val newAddress = SavedAddress(
            addressType = addressType,
            cityName = cityName,
            stateName = stateName,
            pincode = pincode,
            fullAddress = fullAddress,
            landmark = landmark,
            latitude = latitude,
            longitude = longitude,
            isSelected = isFirst
        )
        addressRepository.addAddress(newAddress)
        
        if (isFirst) {
            selectAddress(newAddress)
        }
    }

    fun selectAddress(address: SavedAddress) {
        addressRepository.selectAddress(address)
        
        // Update currentAddress display
        val displayAddress = "${address.addressType}: ${address.fullAddress}|${address.cityName}, ${address.stateName}"
        setAddress(displayAddress)
        
        // Trigger weather fetch
        val query = if (address.latitude != 0.0 && address.longitude != 0.0) {
            "${address.latitude},${address.longitude}"
        } else {
            address.cityName
        }
        
        getWeather(query, WEATHER_API_KEY)
    }

    fun clearSelectedAddress() {
        addressRepository.clearSelection()
    }

    fun deleteAddress(savedAddress: SavedAddress) {
        addressRepository.deleteAddress(savedAddress)
    }

    fun setAddress(address: String?) {
        _currentAddress.postValue(address)
    }
    
    fun setLoading(loading: Boolean) {
        _isLoading.postValue(loading)
    }

    fun getWeather(query: String, apiKey: String = WEATHER_API_KEY) {
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
