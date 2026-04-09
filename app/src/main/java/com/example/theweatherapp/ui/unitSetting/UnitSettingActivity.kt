package com.example.theweatherapp.ui.unitSetting

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.theweatherapp.base.BaseActivity
import com.example.theweatherapp.databinding.ActivityUnitSettingBinding
import com.example.theweatherapp.ui.dashboard.DashboardActivity
import com.example.theweatherapp.utils.PrefManager

class UnitSettingActivity : BaseActivity() {

    private lateinit var binding: ActivityUnitSettingBinding

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            saveAll(isGranted)
            navigate()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUnitSettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnDone.setOnClickListener {
            if (!validate()) return@setOnClickListener
            checkNotificationPermission()
        }
    }

    private fun validate(): Boolean {
        if (binding.chipTemp.checkedChipId == -1 ||
            binding.chipTime.checkedChipId == -1 ||
            binding.chipPrecip.checkedChipId == -1 ||
            binding.chipDistance.checkedChipId == -1 ||
            binding.chipWind.checkedChipId == -1 ||
            binding.chipPressure.checkedChipId == -1
        ) {
            Toast.makeText(this, "Please select all preferences", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun getSelectedChipText(id: Int): String {
        val chip = findViewById<com.google.android.material.chip.Chip>(id)
        return chip.text.toString()
    }

    private fun saveAll(notification: Boolean) {
        PrefManager.saveUnits(
            this,
            getSelectedChipText(binding.chipTemp.checkedChipId),
            getSelectedChipText(binding.chipTime.checkedChipId),
            getSelectedChipText(binding.chipPrecip.checkedChipId),
            getSelectedChipText(binding.chipDistance.checkedChipId),
            getSelectedChipText(binding.chipWind.checkedChipId),
            getSelectedChipText(binding.chipPressure.checkedChipId),
            notification
        )

        PrefManager.setOnboardingDone(this, true)
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                saveAll(true)
                navigate()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            saveAll(true)
            navigate()
        }
    }

    private fun navigate() {
        startActivity(Intent(this, DashboardActivity::class.java))
        finish()
    }
}