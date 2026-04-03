package com.example.theweatherapp.ui.dashboard.home.model

data class HourlyData(
    val time: String,
    val tempC: Int,
    val rainPercent: Int,
    val iconType: String
)