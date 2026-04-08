package com.example.theweatherapp.base

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import com.example.theweatherapp.utils.LocaleHelper
import com.example.theweatherapp.utils.PrefManager

open class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        val lang = PrefManager.getLanguage(newBase)
        val context = LocaleHelper.setLocale(newBase, lang)
        super.attachBaseContext(context)
    }
}