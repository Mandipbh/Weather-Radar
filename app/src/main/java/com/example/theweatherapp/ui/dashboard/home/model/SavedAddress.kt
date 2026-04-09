package com.example.theweatherapp.ui.dashboard.home.model

data class SavedAddress(
    val id: Long = System.currentTimeMillis(),
    val addressType: String, // Home, Office, Other
    val cityName: String,
    val stateName: String,
    val pincode: String,
    val fullAddress: String,
    val landmark: String? = null,
    val latitude: Double,
    val longitude: Double,
    var isSelected: Boolean = false
)
