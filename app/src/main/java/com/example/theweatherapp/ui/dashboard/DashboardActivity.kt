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
import androidx.fragment.app.Fragment
import com.example.theweatherapp.R
import com.example.theweatherapp.base.BaseActivity
import com.example.theweatherapp.databinding.ActivityDashboardBinding
import com.example.theweatherapp.databinding.NavHeaderDashboardBinding
import com.example.theweatherapp.ui.WeatherViewModel
import com.example.theweatherapp.ui.dashboard.customise.CustomiseFragment
import com.example.theweatherapp.ui.dashboard.feedback.FeedbackFragment
import com.example.theweatherapp.ui.dashboard.home.HomeFragment
import com.example.theweatherapp.ui.dashboard.language.LanguageFragment
import com.example.theweatherapp.ui.dashboard.notification.NotificationFragment
import com.example.theweatherapp.ui.dashboard.privacyPolicy.PrivacyPolicyFragment
import com.example.theweatherapp.ui.dashboard.proVersion.ProVersionFragment
import com.example.theweatherapp.ui.dashboard.unitSetting.UnitSettingFragment
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

@AndroidEntryPoint
class DashboardActivity : BaseActivity() {

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

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment_content_dashboard, HomeFragment())
                .commit()
        }

        val drawerLayout = binding.drawerLayout
        val openDrawerBtn = findViewById<ImageButton>(R.id.btn_open_drawer)

        openDrawerBtn.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        binding.navView.setNavigationItemSelectedListener { item ->
            handleDrawerNavigation(item.itemId)
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        val headerView = binding.navView.getHeaderView(0)
        val headerBinding = NavHeaderDashboardBinding.bind(headerView)

        headerBinding.btnViewDetails.setOnClickListener {
            navigateFragment(ProVersionFragment(), "Pro")
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        headerBinding.tvPremium.setOnClickListener {
            navigateFragment(ProVersionFragment(), "Pro")
            binding.drawerLayout.closeDrawer(GravityCompat.START)
        }

        checkLocationPermissions()
    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            return
        }

        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }

    private fun handleDrawerNavigation(itemId: Int) {
        when (itemId) {

            R.id.nav_home -> navigateFragment(HomeFragment(), "Home", addToBack = false)

            R.id.nav_customise ->
                navigateFragment(CustomiseFragment(), "Customise")

            R.id.nav_unit_preference ->
                navigateFragment(UnitSettingFragment(), "Unit")

            R.id.nav_language -> navigateFragment(LanguageFragment(), "Language")

            R.id.nav_feedback,
            R.id.nav_report_a_problem -> navigateFragment(FeedbackFragment(), "Feedback")

            R.id.nav_notification -> navigateFragment(NotificationFragment(), "Notification")

            R.id.nav_privacy_policy -> navigateFragment(PrivacyPolicyFragment(), "Privacy")

            R.id.nav_pro -> navigateFragment(ProVersionFragment(), "Pro")

            R.id.nav_rate_me -> rateApp()

            R.id.nav_share -> shareApp()
        }
    }

    private fun navigateFragment(
        fragment: Fragment,
        tag: String,
        addToBack: Boolean = true
    ) {
        val fm = supportFragmentManager

        val currentFragment =
            fm.findFragmentById(R.id.nav_host_fragment_content_dashboard)

        if (currentFragment?.javaClass == fragment.javaClass) return

        val transaction = fm.beginTransaction()
            .setCustomAnimations(
                R.anim.slide_in_right,
                R.anim.fade_out,
                R.anim.fade_in,
                R.anim.slide_out_right
            )
            .replace(R.id.nav_host_fragment_content_dashboard, fragment, tag)

        if (addToBack) {
            transaction.addToBackStack(tag)
        } else {
            fm.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
        }

        transaction.commit()
    }

    private fun rateApp() {
        try {
            startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
            )
        } catch (e: Exception) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                )
            )
        }
    }

    private fun shareApp() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(
                Intent.EXTRA_TEXT,
                "https://play.google.com/store/apps/details?id=$packageName"
            )
        }
        startActivity(Intent.createChooser(intent, "Share via"))
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
        // Line 1: Detailed address (Full Address Line)
        val line1 = address.getAddressLine(0) ?: ""

        // Line 2: City, State, Pincode
        val parts = mutableListOf<String>()
        address.locality?.let { parts.add(it) } // City
        address.adminArea?.let { parts.add(it) } // State
        address.postalCode?.let { parts.add(it) } // Pincode

        val line2 = parts.joinToString(", ")

        val fullAddress = if (line2.isNotEmpty()) "$line1|$line2" else line1

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
