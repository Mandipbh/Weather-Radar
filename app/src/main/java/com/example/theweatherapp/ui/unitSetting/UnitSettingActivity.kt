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
        // Proceed regardless of permission result
        navigateToDashboard()
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
            // Even if "Don't Allow" is clicked, we move to Dashboard now to prevent getting stuck
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
                navigateToDashboard()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            navigateToDashboard()
        }
    }

    private fun navigateToDashboard() {
        val intent = Intent(this, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
