package com.example.theweatherapp.ui.dashboard

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import com.example.theweatherapp.R
import com.example.theweatherapp.databinding.ActivityDashboardBinding
import com.example.theweatherapp.ui.WeatherViewModel
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private val weatherViewModel: WeatherViewModel by viewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var cancellationTokenSource = CancellationTokenSource()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (fineLocationGranted || coarseLocationGranted) {
            Log.d("DashboardActivity", "Location permission granted")
            checkLocationSettings()
        } else {
            Log.e("DashboardActivity", "Location permission denied")
            showPermissionDeniedDialog()
            weatherViewModel.setLoading(false)
        }
    }

    private val resolutionLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            requestLocation()
        } else {
            Toast.makeText(this, "Location settings not enabled.", Toast.LENGTH_SHORT).show()
            weatherViewModel.setLoading(false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val drawerLayout = binding.drawerLayout
        val openDrawerBtn = findViewById<ImageButton>(R.id.btn_open_drawer)

        openDrawerBtn.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        checkLocationPermissions()
    }

    private fun checkLocationPermissions() {
        val fineLocationGranted = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseLocationGranted = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (!fineLocationGranted && !coarseLocationGranted) {
            requestPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        } else {
            checkLocationSettings()
        }
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Location Permission Required")
            .setMessage("This app needs location access to show weather for your current city. Please enable location permission in app settings.")
            .setPositiveButton("Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
                startActivity(intent)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun checkLocationSettings() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000).build()
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client: SettingsClient = LocationServices.getSettingsClient(this)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener {
            requestLocation()
        }

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    val intentSenderRequest = IntentSenderRequest.Builder(exception.resolution).build()
                    resolutionLauncher.launch(intentSenderRequest)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            } else {
                Log.e("DashboardActivity", "Location settings check failed")
                weatherViewModel.setLoading(false)
            }
        }
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    fun requestLocation() {
        weatherViewModel.setLoading(true)
        
        if (!isLocationEnabled()) {
            Toast.makeText(this, "Please enable location services", Toast.LENGTH_SHORT).show()
            weatherViewModel.setLoading(false)
            return
        }

        cancellationTokenSource.cancel()
        cancellationTokenSource = CancellationTokenSource()

        Log.d("DashboardActivity", "Requesting current accurate location...")
        
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.token
        ).addOnSuccessListener { location ->
            if (location != null) {
                val latLong = "${location.latitude},${location.longitude}"
                Log.d("DashboardActivity", "SUCCESS: Found Live Location -> $latLong")
                updateAddress(location.latitude, location.longitude)
                fetchWeather(latLong)
            } else {
                Log.w("DashboardActivity", "Current location null, checking last known...")
                fusedLocationClient.lastLocation.addOnSuccessListener { lastLocation ->
                    if (lastLocation != null) {
                        val latLong = "${lastLocation.latitude},${lastLocation.longitude}"
                        Log.d("DashboardActivity", "SUCCESS: Found Last Location -> $latLong")
                        updateAddress(lastLocation.latitude, lastLocation.longitude)
                        fetchWeather(latLong)
                    } else {
                        Log.e("DashboardActivity", "Location unavailable.")
                        Toast.makeText(this, "Could not retrieve location.", Toast.LENGTH_SHORT).show()
                        weatherViewModel.setLoading(false)
                    }
                }
            }
        }.addOnFailureListener { e ->
            Log.e("DashboardActivity", "Location Error: ${e.message}")
            weatherViewModel.setLoading(false)
        }
    }

    private fun updateAddress(latitude: Double, longitude: Double) {
        val geocoder = Geocoder(this, Locale.getDefault())
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                if (addresses.isNotEmpty()) {
                    processAddress(addresses[0])
                }
            }
        } else {
            @Suppress("DEPRECATION")
            try {
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                if (!addresses.isNullOrEmpty()) {
                    processAddress(addresses[0])
                }
            } catch (e: Exception) {
                Log.e("DashboardActivity", "Geocoder error", e)
            }
        }
    }

    private fun processAddress(address: android.location.Address) {
        // Line 1: SubLocality or FeatureName
        val line1 = address.subLocality ?: address.featureName ?: ""
        
        // Line 2: Locality, PostalCode, AdminArea, Country
        val parts = mutableListOf<String>()
        address.locality?.let { parts.add(it) }
        address.postalCode?.let { parts.add(it) }
        address.adminArea?.let { parts.add(it) }
        address.countryName?.let { parts.add(it) }
        
        val line2 = parts.joinToString(", ")

        val fullAddress = if (line1.isNotEmpty()) "$line1|$line2" else line2

        Log.d("DashboardActivity", "Resolved Address: $fullAddress")
        runOnUiThread {
            weatherViewModel.setAddress(fullAddress)
        }
    }

    private fun fetchWeather(query: String) {
        val apiKey = "e2ea45395b5f4367bc9135347260204"
        weatherViewModel.getWeather(query, apiKey)
    }

    override fun onDestroy() {
        super.onDestroy()
        cancellationTokenSource.cancel()
    }
}
