package com.example.theweatherapp.ui.unitSetting

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.theweatherapp.databinding.ActivityUnitSettingBinding
import com.example.theweatherapp.ui.locationPermission.LocationPermissionActivity

class UnitSettingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUnitSettingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUnitSettingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnDone.setOnClickListener {
            val intent = Intent(this, LocationPermissionActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}