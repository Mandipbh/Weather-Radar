package com.example.theweatherapp

import android.app.Application
import com.example.theweatherapp.utils.AdManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class WeatherApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize AdManager / AppLovin SDK
        AdManager.getInstance(this).initialize {
            // SDK Initialized
        }
    }
}
