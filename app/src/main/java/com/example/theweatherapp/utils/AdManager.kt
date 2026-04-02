package com.example.theweatherapp.utils

import android.app.Activity
import android.content.Context
import android.util.Log
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.ads.MaxAppOpenAd
import com.applovin.sdk.AppLovinSdk
import com.applovin.sdk.AppLovinSdkConfiguration

class AdManager private constructor(private val context: Context) {

    private var appOpenAd: MaxAppOpenAd? = null
    private var isAdLoading = false
    private var onAdClosed: (() -> Unit)? = null

    companion object {
        private const val TAG = "AdManager"
        private const val APP_OPEN_AD_UNIT_ID = "YOUR_APP_OPEN_AD_UNIT_ID" // Replace with actual ID

        @Volatile
        private var instance: AdManager? = null

        fun getInstance(context: Context): AdManager {
            return instance ?: synchronized(this) {
                instance ?: AdManager(context.applicationContext).also { instance = it }
            }
        }
    }

    fun initialize(onComplete: () -> Unit) {
        AppLovinSdk.getInstance(context).mediationProvider = "max"
        AppLovinSdk.getInstance(context).initializeSdk { configuration: AppLovinSdkConfiguration ->
            Log.d(TAG, "AppLovin SDK Initialized")
            preloadAppOpenAd()
            onComplete()
        }
    }

    fun preloadAppOpenAd() {
        val currentAd = appOpenAd
        if (currentAd != null && currentAd.isReady) return
        if (isAdLoading) return

        isAdLoading = true
        appOpenAd = MaxAppOpenAd(APP_OPEN_AD_UNIT_ID, context)
        appOpenAd?.setListener(object : MaxAdListener {
            override fun onAdLoaded(ad: MaxAd) {
                isAdLoading = false
                Log.d(TAG, "App Open Ad Loaded")
            }

            override fun onAdLoadFailed(adUnitId: String, error: MaxError) {
                isAdLoading = false
                Log.e(TAG, "App Open Ad Load Failed: ${error.message}")
            }

            override fun onAdDisplayed(ad: MaxAd) {}
            override fun onAdClicked(ad: MaxAd) {}
            override fun onAdHidden(ad: MaxAd) {
                Log.d(TAG, "App Open Ad Hidden")
                onAdClosed?.invoke()
                onAdClosed = null
                preloadAppOpenAd() // Load next one
            }
            override fun onAdDisplayFailed(ad: MaxAd, error: MaxError) {
                Log.e(TAG, "App Open Ad Display Failed: ${error.message}")
                onAdClosed?.invoke()
                onAdClosed = null
                preloadAppOpenAd()
            }
        })
        appOpenAd?.loadAd()
    }

    fun isAppOpenAdReady(): Boolean {
        return appOpenAd?.isReady == true
    }

    fun showAppOpenAdIfReady(activity: Activity, onAdDismissed: () -> Unit) {
        val currentAd = appOpenAd
        if (currentAd != null && currentAd.isReady) {
            this.onAdClosed = onAdDismissed
            currentAd.showAd(APP_OPEN_AD_UNIT_ID)
        } else {
            onAdDismissed()
        }
    }
}
