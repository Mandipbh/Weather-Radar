package com.example.theweatherapp.utils

object Constants {
    // Mapbox internal IDs
    const val MAP_SOURCE_ID  = "owm-weather-source"
    const val MAP_LAYER_ID   = "owm-weather-layer"

    // Permission request code
    const val PERM_LOCATION  = 1001

    // OWM layer identifiers
    const val LAYER_TEMPERATURE = "temp_new"
    const val LAYER_RAIN        = "precipitation_new"
    const val LAYER_HUMIDITY    = "humidity_new"
    const val LAYER_CLOUDS      = "clouds_new"
    const val LAYER_PRESSURE    = "pressure_new"
    const val LAYER_SNOW        = "snow_new"
}