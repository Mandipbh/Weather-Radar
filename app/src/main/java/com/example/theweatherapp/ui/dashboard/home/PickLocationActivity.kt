package com.example.theweatherapp.ui.dashboard.home

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.theweatherapp.R
import com.example.theweatherapp.databinding.ActivityPickLocationBinding
import com.example.theweatherapp.ui.WeatherViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.textfield.TextInputEditText
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.gestures.OnMoveListener
import com.mapbox.maps.plugin.gestures.gestures
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@AndroidEntryPoint
class PickLocationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPickLocationBinding
    private val weatherViewModel: WeatherViewModel by viewModels()
    private lateinit var mapboxMap: MapboxMap
    
    private var currentLat: Double = 0.0
    private var currentLng: Double = 0.0
    private var currentFullAddress: String = ""
    private var currentCity: String = ""
    private var currentState: String = ""
    private var currentPincode: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPickLocationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupMapbox()
        setupSearch()
        
        binding.btnConfirmLocation.setOnClickListener {
            showAddAddressBottomSheet()
        }
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun setupMapbox() {
        mapboxMap = binding.mapView.getMapboxMap()
        mapboxMap.loadStyleUri(Style.MAPBOX_STREETS) {
            enableLocation()
        }

        binding.mapView.gestures.addOnMoveListener(object : OnMoveListener {
            override fun onMoveBegin(detector: com.mapbox.android.gestures.MoveGestureDetector) {}
            override fun onMove(detector: com.mapbox.android.gestures.MoveGestureDetector): Boolean {
                return false
            }
            override fun onMoveEnd(detector: com.mapbox.android.gestures.MoveGestureDetector) {
                val center = mapboxMap.cameraState.center
                updateAddressFromLocation(center.latitude(), center.longitude())
            }
        })
    }

    private fun enableLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Logic to move camera to user location can go here
        }
    }

    private fun setupSearch() {
        binding.etSearch.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                searchLocation(binding.etSearch.text.toString())
                true
            } else false
        }
    }

    private fun searchLocation(query: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(this@PickLocationActivity, Locale.getDefault())
                val addresses = geocoder.getFromLocationName(query, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    withContext(Dispatchers.Main) {
                        moveMapToLocation(address.latitude, address.longitude)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun moveMapToLocation(lat: Double, lng: Double) {
        val cameraOptions = CameraOptions.Builder()
            .center(Point.fromLngLat(lng, lat))
            .zoom(15.0)
            .build()
        binding.mapView.camera.flyTo(cameraOptions, MapAnimationOptions.mapAnimationOptions { duration(1000) })
        updateAddressFromLocation(lat, lng)
    }

    private fun updateAddressFromLocation(lat: Double, lng: Double) {
        currentLat = lat
        currentLng = lng
        
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(this@PickLocationActivity, Locale.getDefault())
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    
                    val title = address.subLocality ?: address.locality ?: "Unknown Location"
                    val detail = address.getAddressLine(0) ?: ""
                    
                    currentFullAddress = detail
                    currentCity = address.locality ?: ""
                    currentState = address.adminArea ?: ""
                    currentPincode = address.postalCode ?: ""

                    withContext(Dispatchers.Main) {
                        binding.tvPickedAddressTitle.text = title
                        binding.tvPickedAddressDetail.text = detail
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun showAddAddressBottomSheet() {
        val dialog = BottomSheetDialog(this, R.style.BottomSheetDialogTheme)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_add_address, null)
        dialog.setContentView(view)

        val btnClose = view.findViewById<ImageButton>(R.id.btn_close)
        val chipHome = view.findViewById<Chip>(R.id.chip_home)
        val chipOffice = view.findViewById<Chip>(R.id.chip_office)
        val chipOther = view.findViewById<Chip>(R.id.chip_other)
        
        val etReceiverName = view.findViewById<TextInputEditText>(R.id.et_receiver_name)
        val etFullAddress = view.findViewById<TextInputEditText>(R.id.et_full_address)
        val etLandmark = view.findViewById<TextInputEditText>(R.id.et_landmark)
        val btnSave = view.findViewById<Button>(R.id.btn_save_address)

        // Pre-fill address
        etFullAddress.setText(currentFullAddress)

        var selectedType = "Home"
        chipHome.isChecked = true
        chipHome.setOnClickListener { selectedType = "Home" }
        chipOffice.setOnClickListener { selectedType = "Office" }
        chipOther.setOnClickListener { selectedType = "Other" }

        btnClose.setOnClickListener { dialog.dismiss() }

        btnSave.setOnClickListener {
            val name = etReceiverName.text.toString().trim()
            val addressText = etFullAddress.text.toString().trim()
            val landmark = etLandmark.text.toString().trim()

            if (name.isNotEmpty() && addressText.isNotEmpty()) {
                weatherViewModel.addAddress(
                    addressType = selectedType,
                    receiverName = name,
                    cityName = currentCity,
                    stateName = currentState,
                    pincode = currentPincode,
                    fullAddress = addressText,
                    landmark = if (landmark.isNotEmpty()) landmark else null,
                    latitude = currentLat,
                    longitude = currentLng
                )
                dialog.dismiss()
                finish() // Go back to ManageAddressActivity
                Toast.makeText(this, "Address saved", Toast.LENGTH_SHORT).show()
            } else {
                if (name.isEmpty()) etReceiverName.error = "Enter name"
                if (addressText.isEmpty()) etFullAddress.error = "Enter address"
            }
        }

        dialog.show()
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
    }
}
