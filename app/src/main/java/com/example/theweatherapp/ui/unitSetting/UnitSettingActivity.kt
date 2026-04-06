package com.example.theweatherapp.ui.unitSetting

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.theweatherapp.databinding.ActivityUnitSettingBinding
import com.example.theweatherapp.databinding.DialogNotificationPermissionBinding
import com.example.theweatherapp.ui.dashboard.DashboardActivity

class UnitSettingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUnitSettingBinding

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ ->
        navigateToLocationPermission()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUnitSettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnDone.setOnClickListener {
            showNotificationDialog()
        }
    }

    private fun showNotificationDialog() {
        val dialogBinding = DialogNotificationPermissionBinding.inflate(LayoutInflater.from(this))
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()

        // requestFeature() must be called before adding content. 
        // For AlertDialog, the view is already set via Builder.setView(), so we should avoid calling it here.
        // AlertDialog by default doesn't show a title if none is set.

        dialog.show()

        // Set transparent background for rounded corners and position it at the bottom
        dialog.window?.let { window ->
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            
            val params = window.attributes
            params.gravity = Gravity.BOTTOM
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            // Add a small bottom margin to make it "float" slightly
            params.y = 40 
            window.attributes = params
            
            // Set entry/exit animation for bottom sheet feel
            window.setWindowAnimations(android.R.style.Animation_InputMethod)
        }

        dialogBinding.btnAllow.setOnClickListener {
            dialog.dismiss()
            checkAndRequestNotificationPermission()
        }

        dialogBinding.btnDontAllow.setOnClickListener {
            dialog.dismiss()
            navigateToLocationPermission()
        }
    }

    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                navigateToLocationPermission()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            navigateToLocationPermission()
        }
    }

    private fun navigateToLocationPermission() {
        startActivity(Intent(this, DashboardActivity::class.java))
        finish()
    }
}
