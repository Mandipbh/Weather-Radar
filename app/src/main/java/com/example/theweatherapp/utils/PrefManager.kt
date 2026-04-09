package com.example.theweatherapp.utils

import android.content.Context
import com.example.theweatherapp.ui.dashboard.home.model.SavedAddress
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object PrefManager {

    private const val PREF = "app_prefs"
    private const val PREF_NAME = "app_prefs"

    // ===== EXISTING KEYS (DON'T BREAK) =====
    private const val KEY_LANG = "selected_language"
    private const val KEY_ONBOARDING_DONE = "onboarding_done"
    private const val KEY_SAVED_ADDRESSES = "saved_addresses"

    // ===== NEW UNIT KEYS =====
    private const val KEY_UNIT_DONE = "unit_done"

    private const val TEMP = "temp"
    private const val TIME = "time"
    private const val PRECIP = "precip"
    private const val DISTANCE = "distance"
    private const val WIND = "wind"
    private const val PRESSURE = "pressure"
    private const val NOTIF = "notif"

    // ================= LANGUAGE =================

    fun setLanguage(context: Context, lang: String) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANG, lang)
            .apply()
    }

    fun getLanguage(context: Context): String {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(KEY_LANG, "default") ?: "default"
    }

    // ================= ONBOARDING =================

    fun setOnboardingDone(context: Context, done: Boolean) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_ONBOARDING_DONE, done)
            .apply()
    }

    fun isOnboardingDone(context: Context): Boolean {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getBoolean(KEY_ONBOARDING_DONE, false)
    }

    // ================= UNIT SETTINGS =================

    fun saveUnits(
        context: Context,
        temp: String,
        time: String,
        precip: String,
        distance: String,
        wind: String,
        pressure: String,
        notif: Boolean
    ) {
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit()
            .putString(TEMP, temp)
            .putString(TIME, time)
            .putString(PRECIP, precip)
            .putString(DISTANCE, distance)
            .putString(WIND, wind)
            .putString(PRESSURE, pressure)
            .putBoolean(NOTIF, notif)
            .putBoolean(KEY_UNIT_DONE, true)
            .apply()
    }

    fun isUnitDone(context: Context): Boolean {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getBoolean(KEY_UNIT_DONE, false)
    }

    fun getTemp(context: Context) = getString(context, TEMP)
    fun getTime(context: Context) = getString(context, TIME)
    fun getPrecip(context: Context) = getString(context, PRECIP)
    fun getDistance(context: Context) = getString(context, DISTANCE)
    fun getWind(context: Context) = getString(context, WIND)
    fun getPressure(context: Context) = getString(context, PRESSURE)

    fun getNotif(context: Context): Boolean {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getBoolean(NOTIF, false)
    }

    private fun getString(context: Context, key: String): String {
        return context.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getString(key, "") ?: ""
    }

    fun saveAddresses(context: Context, addresses: List<SavedAddress>) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = Gson().toJson(addresses)
        prefs.edit().putString(KEY_SAVED_ADDRESSES, json).apply()
    }

    fun getAddresses(context: Context): List<SavedAddress> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString(KEY_SAVED_ADDRESSES, null) ?: return emptyList()
        val type = object : TypeToken<List<SavedAddress>>() {}.type
        return Gson().fromJson(json, type)
    }
}