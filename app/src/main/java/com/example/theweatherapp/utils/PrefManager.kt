package com.example.theweatherapp.utils

import android.content.Context

object PrefManager {
    private const val PREF_NAME = "app_pref"
    private const val KEY_LANG = "language"

    fun setLanguage(context: Context, lang: String) {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("selected_language", lang).apply()
    }

    fun getLanguage(context: Context): String {
        val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        return prefs.getString("selected_language", "default") ?: "default"
    }
}