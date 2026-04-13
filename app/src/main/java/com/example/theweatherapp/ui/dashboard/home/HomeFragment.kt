package com.example.theweatherapp.ui.dashboard.home

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.theweatherapp.R
import com.example.theweatherapp.data.api.WeatherResponse
import com.example.theweatherapp.databinding.FragmentHomeBinding
import com.example.theweatherapp.ui.WeatherViewModel
import com.example.theweatherapp.ui.dashboard.DashboardActivity
import com.example.theweatherapp.ui.dashboard.home.model.HourlyData
import com.example.theweatherapp.ui.radar.RadarActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val weatherViewModel: WeatherViewModel by activityViewModels()

    private var autoRefreshJob: Job? = null
    private val refreshInterval = 15 * 60 * 1000L

    private lateinit var savedAddressAdapter: SavedAddressAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()
        setupSwipeRefresh()
        setupClickListeners()

        // Observe weather data
        weatherViewModel.weatherData.observe(viewLifecycleOwner) { response ->
            response?.let { updateUI(it) }
        }

        // Observe address updates
        weatherViewModel.currentAddress.observe(viewLifecycleOwner) { address ->
            if (address != null) {
                val parts = address.split("|")
                binding.tvLocation.text = parts[0]
                if (parts.size > 1) {
                    binding.tvLocationLine2.text = parts[1]
                    binding.tvLocationLine2.visibility = View.VISIBLE
                } else {
                    binding.tvLocationLine2.visibility = View.GONE
                }
                
                // Update AQI station label with current city
                binding.tvCurrentAqiLocation.text = parts[0]
            } else {
                binding.tvLocation.text = getString(R.string.detecting_location)
                binding.tvLocationLine2.text = ""
                binding.tvCurrentAqiLocation.text = getString(R.string.detecting)
            }
        }

        // Observe loading state
        weatherViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.swipeRefreshLayout.isRefreshing = isLoading
        }

        // Observe saved addresses
        weatherViewModel.savedAddresses.observe(viewLifecycleOwner) { addresses ->
            savedAddressAdapter.updateList(addresses)
            binding.layoutSavedAddresses.visibility = if (addresses.isEmpty()) View.GONE else View.VISIBLE
            
            // If there's a selected address and no weather data yet, load it
            val selected = addresses.find { it.isSelected }
            if (selected != null && weatherViewModel.weatherData.value == null) {
                weatherViewModel.selectAddress(selected)
            }
        }
    }

    private fun setupRecyclerViews() {
        // Hourly Forecast
        binding.rvHourly.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvHourly.isNestedScrollingEnabled = false

        // Saved Addresses
        savedAddressAdapter = SavedAddressAdapter(
            addresses = emptyList(),
            onDeleteClick = { address -> weatherViewModel.deleteAddress(address) },
            onItemClick = { address ->
                // When an address is clicked, update selected state and fetch weather
                weatherViewModel.selectAddress(address)
                Toast.makeText(context, "Fetching weather for ${address.addressType}", Toast.LENGTH_SHORT).show()
            }
        )
        binding.rvSavedAddresses.adapter = savedAddressAdapter
    }

    private fun setupClickListeners() {
        binding.btnAdd.setOnClickListener {
            val intent = Intent(requireContext(), PickLocationActivity::class.java)
            startActivity(intent)
        }
        
        binding.btnTarget.setOnClickListener {
             // Re-detect current GPS location
             (activity as? DashboardActivity)?.requestLocation()
        }

        binding.btnSeeMore.setOnClickListener {
            // Logic to navigate to customise or show more widgets
            Toast.makeText(requireContext(), "Opening customisation...", Toast.LENGTH_SHORT).show()
        }

        binding.btnAddWidget.setOnClickListener {
            requestPinWidget()
        }

        binding.btnViewDetailsAurora.setOnClickListener {
            Toast.makeText(requireContext(), "Opening Aurora Details...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestPinWidget() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val appWidgetManager = requireContext().getSystemService(AppWidgetManager::class.java)
            // Note: In a real app, you'd provide your actual AppWidgetProvider class here
            // val myProvider = ComponentName(requireContext(), WeatherWidget::class.java)
            // if (appWidgetManager.isRequestPinAppWidgetSupported) {
            //     appWidgetManager.requestPinAppWidget(myProvider, null, null)
            // } else {
                Toast.makeText(requireContext(), "Please add widget from your launcher", Toast.LENGTH_LONG).show()
            // }
        } else {
            Toast.makeText(requireContext(), "Widget pinning not supported", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        startAutoRefresh()
    }

    override fun onPause() {
        super.onPause()
        stopAutoRefresh()
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            val selectedAddress = weatherViewModel.savedAddresses.value?.find { it.isSelected }
            if (selectedAddress != null) {
                weatherViewModel.selectAddress(selectedAddress)
            } else {
                (activity as? DashboardActivity)?.requestLocation()
            }
        }

        binding.swipeRefreshLayout.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        )
    }

    private fun startAutoRefresh() {
        stopAutoRefresh()
        autoRefreshJob = viewLifecycleOwner.lifecycleScope.launch {
            while (isActive) {
                delay(refreshInterval)
                val selectedAddress = weatherViewModel.savedAddresses.value?.find { it.isSelected }
                if (selectedAddress != null) {
                    weatherViewModel.selectAddress(selectedAddress)
                } else {
                    (activity as? DashboardActivity)?.requestLocation()
                }
            }
        }
    }

    private fun stopAutoRefresh() {
        autoRefreshJob?.cancel()
        autoRefreshJob = null
    }

    private fun updateUI(weather: WeatherResponse) {
        binding.tvCondition.text = weather.current.condition.text.uppercase()
        binding.tvTemperature.text = weather.current.tempC.toInt().toString()

        val forecastDays = weather.forecast?.forecastday ?: emptyList()
        val firstDay = forecastDays.firstOrNull()
        
        if (firstDay != null) {
            binding.tvMinMax.text = "${firstDay.day.minTempC.toInt()}°C / ${firstDay.day.maxTempC.toInt()}°C"
            binding.tvRainChance.text = "${firstDay.day.dailyChanceOfRain}%"
            
            // Update Sunrise and Sunset
            binding.tvSunrise.text = firstDay.astro.sunrise
            binding.tvSunset.text = firstDay.astro.sunset
            
            // Update Sun Progress Bar
            updateSunProgress(firstDay.astro.sunrise, firstDay.astro.sunset, weather.location.localtime)
            
            updateHourlyForecast(firstDay.hour, weather.location.localtime)

            // Update Moon Phase
            updateMoonUI(firstDay, weather.location.localtime)
        }

        // Update the 30-Day Graph
        if (forecastDays.isNotEmpty()) {
            binding.forecastGraph.setData(forecastDays)
        }

        // Update Widget Preview
        updateWidgetPreview(weather)

        binding.tvRealFeel.text = "Real Feel ${weather.current.feelslikeC.toInt()}°C"
        
        // Update Air Quality Section
        weather.current.airQuality?.let { aqi ->
            val rawAqi = when(aqi.usEpaIndex) {
                1 -> (aqi.pm25.toInt().coerceIn(0, 50))
                2 -> (aqi.pm25.toInt().coerceIn(51, 100))
                3 -> (aqi.pm25.toInt().coerceIn(101, 150))
                4 -> (aqi.pm25.toInt().coerceIn(151, 200))
                5 -> (aqi.pm25.toInt().coerceIn(201, 300))
                else -> (aqi.pm25.toInt().coerceIn(301, 500))
            }
            
            binding.aqiProgressView.setAqiValue(rawAqi)
            
            val (desc, color) = getAqiDescription(rawAqi)
            binding.tvAqiDesc.text = "$rawAqi - $desc"
            binding.tvAqiDesc.setTextColor(Color.parseColor(color))
            
            // Station 1 is now dynamic Current Location
            binding.tvStation1Val.text = rawAqi.toString()
            binding.tvCurrentAqiLocation.text = weather.location.name

            binding.tvStation2Val.text = (rawAqi + 8).toString()
            binding.tvStation3Val.text = (rawAqi + 26).toString()
        }

        // Format local time for display
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
            val outputFormat = SimpleDateFormat("h:mm a · EEE, d MMM yyyy", Locale.US)
            val date = inputFormat.parse(weather.location.localtime)
            binding.tvCurrentTime.text = if (date != null) outputFormat.format(date) else weather.location.localtime
        } catch (e: Exception) {
            binding.tvCurrentTime.text = weather.location.localtime
        }

        //navigate to radar screen
        binding.llRadar.setOnClickListener {
            val intent = Intent(requireContext(), RadarActivity::class.java)
            startActivity(intent)
        }

        // Update Aurora Forecast images
        updateAuroraForecast()
    }

    private fun updateAuroraForecast() {
        // Using real-time NOAA aurora forecast images
        val northUrl = "https://services.swpc.noaa.gov/images/aurora-forecast-northern-hemisphere.jpg"
        val southUrl = "https://services.swpc.noaa.gov/images/aurora-forecast-southern-hemisphere.jpg"

        Glide.with(this)
            .load(northUrl)
            .placeholder(R.drawable.bg_weather_icon_circle)
            .into(binding.ivAuroraNorth)

        Glide.with(this)
            .load(southUrl)
            .placeholder(R.drawable.bg_weather_icon_circle)
            .into(binding.ivAuroraSouth)
    }

    private fun updateMoonUI(day: com.example.theweatherapp.data.api.ForecastDay, localTime: String) {
        binding.tvMoonPhaseName.text = day.astro.moonPhase
        binding.tvMoonIllumination.text = day.astro.moonIllumination + "%"
        binding.tvMoonriseTime.text = day.astro.moonrise
        
        // Date formatting for moon phase display
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
            val outputFormat = SimpleDateFormat("d MMM yyyy hh:mm a", Locale.US)
            val date = inputFormat.parse(localTime)
            binding.tvMoonDate.text = outputFormat.format(date!!)
        } catch (e: Exception) {
            binding.tvMoonDate.text = localTime
        }

        // Map phase name to moon icon
        val phase = day.astro.moonPhase.lowercase()
        val iconRes = when {
            phase.contains("new") -> R.drawable.bg_weather_icon_circle
            phase.contains("full") -> R.drawable.bg_weather_icon_circle
            phase.contains("crescent") -> R.drawable.baseline_wb_cloudy_24
            else -> R.drawable.baseline_wb_cloudy_24
        }
        binding.ivMoonGraphic.setImageResource(iconRes)
    }

    private fun updateWidgetPreview(weather: WeatherResponse) {
        val preview = binding.widgetPreview
        preview.tvLocation.text = "${weather.location.name}, ${weather.location.country}"
        preview.tvTemp.text = "${weather.current.tempC.toInt()}°"
        preview.tvCondition.text = weather.current.condition.text
        preview.tvTimeDate.text = weather.location.localtime
    }

    private fun getAqiDescription(value: Int): Pair<String, String> {
        return when {
            value <= 50 -> "Good" to "#4CAF50"
            value <= 100 -> "Moderate" to "#FFEB3B"
            value <= 150 -> "Unhealthy for Sensitive Groups" to "#FF9800"
            value <= 200 -> "Unhealthy" to "#F44336"
            value <= 300 -> "Very Unhealthy" to "#9C27B0"
            else -> "Hazardous" to "#795548"
        }
    }

    private fun updateSunProgress(sunriseStr: String, sunsetStr: String, localTimeStr: String) {
        try {
            val sunFormat = SimpleDateFormat("hh:mm a", Locale.US)
            val localFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
            
            val sunriseDate = sunFormat.parse(sunriseStr)
            val sunsetDate = sunFormat.parse(sunsetStr)
            
            // Local time from API can sometimes be yyyy-MM-dd H:mm
            val localDate = try {
                localFormat.parse(localTimeStr)
            } catch (e: Exception) {
                SimpleDateFormat("yyyy-MM-dd H:mm", Locale.US).parse(localTimeStr)
            }
            
            if (sunriseDate != null && sunsetDate != null && localDate != null) {
                val cal = Calendar.getInstance()
                
                cal.time = sunriseDate
                val sunriseMinutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
                
                cal.time = sunsetDate
                val sunsetMinutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
                
                cal.time = localDate
                val currentMinutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
                
                val totalDaylightMinutes = sunsetMinutes - sunriseMinutes
                
                if (totalDaylightMinutes > 0) {
                    val progress = when {
                        currentMinutes <= sunriseMinutes -> 0
                        currentMinutes >= sunsetMinutes -> 100
                        else -> {
                            val elapsed = currentMinutes - sunriseMinutes
                            ((elapsed.toFloat() / totalDaylightMinutes) * 100).toInt()
                        }
                    }
                    Log.d("SunProgress", "Sunrise: $sunriseMinutes, Sunset: $sunsetMinutes, Current: $currentMinutes, Progress: $progress")
                    binding.pbSun.progress = progress
                }
            }
        } catch (e: Exception) {
            Log.e("SunProgress", "Error updating sun progress", e)
        }
    }

    private fun updateHourlyForecast(hours: List<com.example.theweatherapp.data.api.Hour>, localTime: String) {
        val hourlyList = mutableListOf<HourlyData>()
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val currentHourStr = try {
            val date = sdf.parse(localTime)
            SimpleDateFormat("HH", Locale.getDefault()).format(date!!)
        } catch (e: Exception) {
            ""
        }

        hours.forEach { hour ->
            val hourOfDay = hour.time.split(" ").last()
            val hourOnly = hourOfDay.split(":").first()
            val displayTime = if (hourOnly == currentHourStr) "Now" else hourOfDay
            
            hourlyList.add(
                HourlyData(
                    time = displayTime,
                    tempC = hour.tempC.toInt(),
                    rainPercent = hour.chanceOfRain,
                    iconType = hour.condition.text.lowercase()
                )
            )
        }

        val filteredList = hourlyList.filterIndexed { index, hourlyData ->
            val hourOnly = hours[index].time.split(" ").last().split(":").first()
            hourOnly >= currentHourStr || hourlyData.time == "Now"
        }

        binding.rvHourly.adapter = HourlyForecastAdapter(filteredList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
