package com.example.theweatherapp.ui.radar

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import com.mapbox.geojson.Point
import android.location.Geocoder
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.theweatherapp.R
import com.example.theweatherapp.base.BaseActivity
import com.example.theweatherapp.databinding.ActivityRadarBinding
import com.example.theweatherapp.utils.Constants.LAYER_CLOUDS
import com.example.theweatherapp.utils.Constants.LAYER_HUMIDITY
import com.example.theweatherapp.utils.Constants.LAYER_PRESSURE
import com.example.theweatherapp.utils.Constants.LAYER_RAIN
import com.example.theweatherapp.utils.Constants.LAYER_SNOW
import com.example.theweatherapp.utils.Constants.LAYER_TEMPERATURE
import com.example.theweatherapp.utils.Constants.MAP_LAYER_ID
import com.example.theweatherapp.utils.Constants.MAP_SOURCE_ID
import com.example.theweatherapp.utils.Constants.PERM_LOCATION
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.Style
import com.mapbox.maps.extension.style.layers.addLayer
import com.mapbox.maps.extension.style.layers.generated.rasterLayer
import com.mapbox.maps.extension.style.sources.addSource
import com.mapbox.maps.extension.style.sources.generated.rasterSource
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.gestures.gestures
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class RadarActivity : BaseActivity() {
    private lateinit var binding: ActivityRadarBinding

    private lateinit var mapboxMap: MapboxMap

    private lateinit var fusedLocation: FusedLocationProviderClient

    private val owmApiKey: String by lazy {
        "e2ea45395b5f4367bc9135347260204"
    }

    private fun owmTileUrl(layer: String): String =
        "https://tile.openweathermap.org/map/$layer/{z}/{x}/{y}.png?appid=$owmApiKey"
    private var activeLayerId = LAYER_TEMPERATURE

    private data class WeatherTab(
        val layerId: String,
        val tabView: () -> LinearLayout,
        val iconView: () -> ImageView,
        val labelView: () -> TextView
    )

    // Lazy tab map built after view is created
    private lateinit var tabs: List<WeatherTab>

    override fun onStart() {
        super.onStart(); binding.mapView.onStart()
    }

    override fun onStop() {
        super.onStop(); binding.mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory(); binding.mapView.onLowMemory()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRadarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocation = LocationServices.getFusedLocationProviderClient(this)
        binding.loadingOverlay.visibility = View.GONE

        buildTabList()
        setupBackButton()
        setupMapbox()
        setupLocationButton()
        setupZoomButtons()
        setupTabBar()
    }

    private fun setupBackButton() {
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }
    }
    private fun setupMapbox() {
        mapboxMap = binding.mapView.getMapboxMap()

        // Enable pan/zoom gestures; disable pitch for 2-D radar view
        binding.mapView.gestures.pitchEnabled = false
        binding.mapView.gestures.rotateEnabled = false

        // Load Mapbox dark style — closest to the dark base in the reference screenshot
        mapboxMap.loadStyleUri(Style.DARK) { style ->
            binding.loadingOverlay.visibility = View.GONE
            addWeatherTileLayer(activeLayerId)
            focusUserLocation(animate = false)
        }

        // Start with a region view (zoom level 4.5 shows subcontinent like reference)
        mapboxMap.setCamera(
            CameraOptions.Builder()
                .zoom(4.5)
                .build()
        )
    }

    private fun addWeatherTileLayer(layerId: String) {
        val style = mapboxMap.getStyle() ?: return

        // Remove existing weather source + layer (ignore errors if not present)
        runCatching { style.removeStyleLayer(MAP_LAYER_ID) }
        runCatching { style.removeStyleSource(MAP_SOURCE_ID) }

        // Add new raster source pointing at OWM tile endpoint
        style.addSource(
            rasterSource(MAP_SOURCE_ID) {
                tiles(listOf(owmTileUrl(layerId)))
                tileSize(256)
                minzoom(0)
                maxzoom(18)
            }
        )

        // Add the raster layer — opacity 0.8 gives the vivid heatmap look
        // from the reference screenshot while keeping country borders visible
        style.addLayer(
            rasterLayer(MAP_LAYER_ID, MAP_SOURCE_ID) {
                rasterOpacity(0.80)
            }
        )
    }

    private fun setupLocationButton() {
        binding.btnMyLocation.setOnClickListener {
            focusUserLocation(animate = true)
        }
    }

    @SuppressLint("MissingPermission")
    private fun focusUserLocation(animate: Boolean) {
        if (!hasLocationPermission()) {
            requestLocationPermission()
            return
        }

        fusedLocation.lastLocation.addOnSuccessListener { location ->
            location ?: return@addOnSuccessListener

            val point = Point.fromLngLat(location.longitude, location.latitude)
            val cameraOptions = CameraOptions.Builder()
                .center(point)
                .zoom(3.0)
                .build()

            if (animate) {
                binding.mapView.camera.flyTo(
                    cameraOptions,
                    MapAnimationOptions.mapAnimationOptions { duration(1000) }
                )
            } else {
                mapboxMap.setCamera(cameraOptions)
            }

            // Reverse geocode address for top bar
            reverseGeocode(location.latitude, location.longitude)
        }
    }

    private fun reverseGeocode(lat: Double, lng: Double) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(this@RadarActivity, Locale.getDefault())

                @Suppress("DEPRECATION")
                val results = geocoder.getFromLocation(lat, lng, 1)
                val address = results?.firstOrNull()

                // Build multi-line address matching reference style:
                // "352, Block A Phase 1 Johar Town,\nLahore, 54770, Pakistan"
                val line1 = listOfNotNull(
                    address?.subThoroughfare,
                    address?.thoroughfare,
                    address?.subLocality
                ).joinToString(", ").ifEmpty { null }

                val line2 = listOfNotNull(
                    address?.locality,
                    address?.postalCode,
                    address?.countryName
                ).joinToString(", ")

                val displayAddress = if (line1 != null) "$line1,\n$line2" else line2

                withContext(Dispatchers.Main) {
                    binding.tvAddress.text = displayAddress
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.tvAddress.text = "%.4f, %.4f".format(lat, lng)
                }
            }
        }
    }

    private fun buildTabList() {
        tabs = listOf(
            WeatherTab(
                LAYER_TEMPERATURE,
                { binding.tabTemperature },
                { binding.iconTemperature },
                { binding.labelTemperature }),
            WeatherTab(
                LAYER_RAIN,
                { binding.tabRain }, { binding.iconRain }, { binding.labelRain }),
            WeatherTab(
                LAYER_HUMIDITY,
                { binding.tabHumidity }, { binding.iconHumidity }, { binding.labelHumidity }),
            WeatherTab(
                LAYER_CLOUDS,
                { binding.tabCloudy }, { binding.iconCloudy }, { binding.labelCloudy }),
            WeatherTab(
                LAYER_PRESSURE,
                { binding.tabPressure }, { binding.iconPressure }, { binding.labelPressure }),
            WeatherTab(
                LAYER_SNOW,
                { binding.tabSnow }, { binding.iconSnow }, { binding.labelSnow })
        )
    }

    private fun setupZoomButtons() {
        binding.btnZoomIn.setOnClickListener {
            val zoom = mapboxMap.cameraState.zoom
            binding.mapView.camera.easeTo(
                CameraOptions.Builder().zoom(zoom + 1.0).build(),
                MapAnimationOptions.mapAnimationOptions { duration(250) }
            )
        }

        binding.btnZoomOut.setOnClickListener {
            val zoom = mapboxMap.cameraState.zoom
            binding.mapView.camera.easeTo(
                CameraOptions.Builder().zoom((zoom - 1.0).coerceAtLeast(1.0)).build(),
                MapAnimationOptions.mapAnimationOptions { duration(250) }
            )
        }
    }

    private fun hasLocationPermission(): Boolean =
        ActivityCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    private fun requestLocationPermission() {
        requestPermissions(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            PERM_LOCATION
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERM_LOCATION &&
            grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED
        ) {
            focusUserLocation(animate = true)
        }
    }

    private fun switchToLayer(layerId: String) {
        if (layerId == activeLayerId) return
        activeLayerId = layerId
        addWeatherTileLayer(layerId)
        updateTabSelection()
    }

    private fun setupTabBar() {
        tabs.forEach { tab ->
            tab.tabView().setOnClickListener {
                switchToLayer(tab.layerId)
            }
        }
        // Set initial active state
        updateTabSelection()
    }

    private fun updateTabSelection() {
        tabs.forEach { tab ->
            val isActive = tab.layerId == activeLayerId

            tab.tabView().background =
                if (isActive)
                    getDrawable(R.drawable.bg_tab_selected)
                else
                    null
        }
    }

}