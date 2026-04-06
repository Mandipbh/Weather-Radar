package com.example.theweatherapp.ui.dashboard.home.model

data class SavedAddress(
    val id: Long = System.currentTimeMillis(),
    val pincode: String,
    val cityName: String,
    val stateName: String,
    val fullAddress: String
)
