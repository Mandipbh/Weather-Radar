package com.example.theweatherapp.ui.unitSetting

import android.Manifest
import android.content.Context
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
import com.example.theweatherapp.base.BaseActivity
import com.example.theweatherapp.databinding.ActivityUnitSettingBinding
import com.example.theweatherapp.databinding.DialogNotificationPermissionBinding
import com.example.theweatherapp.ui.dashboard.DashboardActivity

class UnitSettingActivity : BaseActivity() {

    private lateinit var binding: ActivityUnitSettingBinding

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        saveNotificationPreference(isGranted)
        navigateToDashboard()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUnitSettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnDone.setOnClickListener {
            val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            val isHandled = prefs.getBoolean("notification_permission_handled", false)
            
            if (isHandled) {
                navigateToDashboard()
            } else {
                showNotificationDialog()
            }
        }
    }

    private fun showNotificationDialog() {
        val dialogBinding = DialogNotificationPermissionBinding.inflate(LayoutInflater.from(this))
        val dialog = AlertDialog.Builder(this)
            .setView(dialogBinding.root)
            .setCancelable(false)
            .create()

        dialog.show()

        dialog.window?.let { window ->
            window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val params = window.attributes
            params.gravity = Gravity.BOTTOM
            params.width = WindowManager.LayoutParams.MATCH_PARENT
            params.height = WindowManager.LayoutParams.WRAP_CONTENT
            params.y = 40 
            window.attributes = params
            window.setWindowAnimations(android.R.style.Animation_InputMethod)
        }

        dialogBinding.btnAllow.setOnClickListener {
            dialog.dismiss()
            checkAndRequestNotificationPermission()
        }

        dialogBinding.btnDontAllow.setOnClickListener {
            dialog.dismiss()
            saveNotificationPreference(false)
            navigateToDashboard()
        }
    }

    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                saveNotificationPreference(true)
                navigateToDashboard()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            // Below Android 13, permission is granted by default at install
            saveNotificationPreference(true)
            navigateToDashboard()
        }
    }

    private fun saveNotificationPreference(enabled: Boolean) {
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("notifications_enabled", enabled)
            .putBoolean("notification_permission_handled", true)
            .apply()
    }

    private fun navigateToDashboard() {
        val intent = Intent(this, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
