package com.example.theweatherapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import com.example.theweatherapp.base.BaseActivity
import com.example.theweatherapp.databinding.ActivityMainBinding
import com.example.theweatherapp.ui.dashboard.DashboardActivity
import com.example.theweatherapp.ui.unitSetting.UnitSettingActivity
import com.example.theweatherapp.utils.AdManager
import com.example.theweatherapp.utils.PrefManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding
    private val handler = Handler(Looper.getMainLooper())
    private var isNavigated = false
    private val timeoutRunnable = Runnable {
        proceedToNextScreen()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            insets
        }

        // Check for ad and show if ready, otherwise wait briefly or skip
        startLoadingProcess()
    }

    private fun startLoadingProcess() {
        val adManager = AdManager.getInstance(this)
        
        // Timeout safeguard: 5 seconds max for the loading screen
        handler.postDelayed(timeoutRunnable, 5000)

        // Poll for ad readiness or just check once if "without delay" is literal
        // Usually, we want to give it a second or two if the app just started
        checkAndShowAd()
    }

    private fun checkAndShowAd() {
        val adManager = AdManager.getInstance(this)
        
        if (adManager.isAppOpenAdReady()) {
            handler.removeCallbacks(timeoutRunnable)
            adManager.showAppOpenAdIfReady(this) {
                proceedToNextScreen()
            }
        } else {
            // If not ready, we could poll for a bit or just wait for the timeout
            // For this implementation, let's wait up to 3 seconds for the ad to load
            // then proceed if it still hasn't loaded.
            handler.postDelayed({
                if (!isNavigated) {
                    if (adManager.isAppOpenAdReady()) {
                        handler.removeCallbacks(timeoutRunnable)
                        adManager.showAppOpenAdIfReady(this) {
                            proceedToNextScreen()
                        }
                    } else {
                        proceedToNextScreen()
                    }
                }
            }, 3000)
        }
    }

    private fun proceedToNextScreen() {
        if (isNavigated) return
        isNavigated = true
        handler.removeCallbacks(timeoutRunnable)
        
        Log.d("MainActivity", "Navigating to UnitSettingActivity")
        if (!PrefManager.isOnboardingDone(this)) {
            startActivity(Intent(this, UnitSettingActivity::class.java))
            finish()
        } else {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        }
    }

    override fun onDestroy() {
        handler.removeCallbacks(timeoutRunnable)
        super.onDestroy()
    }
}
