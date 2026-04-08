package com.example.theweatherapp

import android.app.Application
import android.content.Context
import com.example.theweatherapp.utils.AdManager
import com.example.theweatherapp.utils.LocaleHelper
import com.example.theweatherapp.utils.PrefManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class WeatherApplication : Application() {

    override fun attachBaseContext(base: Context) {
        val lang = PrefManager.getLanguage(base)
        val context = LocaleHelper.setLocale(base, lang)
        super.attachBaseContext(context)
    }

    override fun onCreate() {
        super.onCreate()
        // Initialize AdManager / AppLovin SDK
        AdManager.getInstance(this).initialize {
            // SDK Initialized
        }
    }
}
