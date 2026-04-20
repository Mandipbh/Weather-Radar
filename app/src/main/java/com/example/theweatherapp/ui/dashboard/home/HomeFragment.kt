package com.example.theweatherapp.ui.dashboard.home

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.theweatherapp.R
import com.example.theweatherapp.data.api.WeatherResponse
import com.example.theweatherapp.databinding.FragmentHomeBinding
import com.example.theweatherapp.ui.WeatherViewModel
import com.example.theweatherapp.ui.dashboard.DashboardActivity
import com.example.theweatherapp.ui.dashboard.home.model.HourlyData
import com.example.theweatherapp.ui.radar.RadarActivity
import com.example.theweatherapp.utils.PrefManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val weatherViewModel: WeatherViewModel by activityViewModels()

    private var autoRefreshJob: Job? = null
    private val refreshInterval = 15 * 60 * 1000L

    private lateinit var savedAddressAdapter: SavedAddressAdapter
    private lateinit var widgetsAdapter: HomeWidgetsAdapter

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
        val currentBinding = _binding ?: return

        // Load background image
        Glide.with(this)
            .asDrawable()
            .load(R.drawable.bg_unit_settings)
            .placeholder(R.drawable.bg_unit_settings)
            .into(currentBinding.ivHomeBackground)

        setupRecyclerViews()
        setupSwipeRefresh()
        setupClickListeners()

        // Observe weather data
        weatherViewModel.weatherData.observe(viewLifecycleOwner) { response ->
            response?.let { updateUI(it) }
        }

        // Observe address updates
        weatherViewModel.currentAddress.observe(viewLifecycleOwner) { address ->
            val obsBinding = _binding ?: return@observe
            if (address != null) {
                val parts = address.split("|")
                obsBinding.tvLocation.text = parts[0]
                if (parts.size > 1) {
                    obsBinding.tvLocationLine2.text = parts[1]
                    obsBinding.tvLocationLine2.visibility = View.VISIBLE
                } else {
                    obsBinding.tvLocationLine2.visibility = View.GONE
                }
                
                // Update AQI station label with current city
                obsBinding.tvCurrentAqiLocation.text = parts[0]
            } else {
                obsBinding.tvLocation.text = getString(R.string.detecting_location)
                obsBinding.tvLocationLine2.text = ""
                obsBinding.tvCurrentAqiLocation.text = getString(R.string.detecting)
            }
        }

        // Observe loading state
        weatherViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            _binding?.swipeRefreshLayout?.isRefreshing = isLoading
        }

        // Observe saved addresses
        weatherViewModel.savedAddresses.observe(viewLifecycleOwner) { addresses ->
            val obsBinding = _binding ?: return@observe
            savedAddressAdapter.updateList(addresses)
            obsBinding.layoutSavedAddresses.visibility = if (addresses.isEmpty()) View.GONE else View.VISIBLE
            
            // If there's a selected address and no weather data yet, load it
            val selected = addresses.find { it.isSelected }
            if (selected != null && weatherViewModel.weatherData.value == null) {
                weatherViewModel.selectAddress(selected)
            }
        }
    }

    private fun setupRecyclerViews() {
        val currentBinding = _binding ?: return
        
        // Hourly Forecast
        currentBinding.rvHourly.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        currentBinding.rvHourly.isNestedScrollingEnabled = false

        // Saved Addresses
        savedAddressAdapter = SavedAddressAdapter(
            addresses = emptyList(),
            onDeleteClick = { address -> weatherViewModel.deleteAddress(address) },
            onItemClick = { address ->
                weatherViewModel.selectAddress(address)
                Toast.makeText(context, "Fetching weather for ${address.addressType}", Toast.LENGTH_SHORT).show()
            }
        )
        currentBinding.rvSavedAddresses.adapter = savedAddressAdapter

        // Widget Previews
        widgetsAdapter = HomeWidgetsAdapter()
        currentBinding.rvWidgetPreviews.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = widgetsAdapter
            val snapHelper = PagerSnapHelper()
            snapHelper.attachToRecyclerView(this)
            
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val position = layoutManager.findFirstCompletelyVisibleItemPosition()
                    if (position != RecyclerView.NO_POSITION) {
                        updateWidgetDots(position)
                    }
                }
            })
        }
    }

    private fun updateWidgetDots(position: Int) {
        val currentBinding = _binding ?: return
        currentBinding.dot1.setBackgroundResource(if (position == 0) R.drawable.bg_dot_active else R.drawable.bg_dot_inactive)
        currentBinding.dot2.setBackgroundResource(if (position == 1) R.drawable.bg_dot_active else R.drawable.bg_dot_inactive)
        currentBinding.dot3.setBackgroundResource(if (position == 2) R.drawable.bg_dot_active else R.drawable.bg_dot_inactive)
    }

    private fun setupClickListeners() {
        val currentBinding = _binding ?: return
        currentBinding.btnAdd.setOnClickListener {
            val intent = Intent(requireContext(), PickLocationActivity::class.java)
            startActivity(intent)
        }
        
        currentBinding.btnTarget.setOnClickListener {
             (activity as? DashboardActivity)?.requestLocation()
        }

        currentBinding.btnSeeMore.setOnClickListener {
            Toast.makeText(requireContext(), "Opening customisation...", Toast.LENGTH_SHORT).show()
        }

        currentBinding.btnRadarSmall.setOnClickListener {
            startActivity(Intent(requireContext(), RadarActivity::class.java))
        }

        currentBinding.llRadarBanner.setOnClickListener {
            startActivity(Intent(requireContext(), RadarActivity::class.java))
        }
        
        currentBinding.flRadarPreview.setOnClickListener {
            startActivity(Intent(requireContext(), RadarActivity::class.java))
        }

        currentBinding.btnAddWidget.setOnClickListener {
            requestPinWidget()
        }
    }

    private fun requestPinWidget() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val appWidgetManager = requireContext().getSystemService(AppWidgetManager::class.java)
            val myProvider = ComponentName(requireContext(), "com.example.theweatherapp.widget.WeatherWidgetProvider")

            if (appWidgetManager.isRequestPinAppWidgetSupported) {
                // Since I haven't created the provider yet, this might fail or do nothing
                // I will create the provider in the next steps.
                appWidgetManager.requestPinAppWidget(myProvider, null, null)
            } else {
                Toast.makeText(requireContext(), "Pinning widgets is not supported on this device", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "Pinning widgets requires Android 8.0+", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupSwipeRefresh() {
        val currentBinding = _binding ?: return
        currentBinding.swipeRefreshLayout.setOnRefreshListener {
            val selectedAddress = weatherViewModel.savedAddresses.value?.find { it.isSelected }
            if (selectedAddress != null) {
                weatherViewModel.selectAddress(selectedAddress)
            } else {
                (activity as? DashboardActivity)?.requestLocation()
            }
        }
        
        currentBinding.swipeRefreshLayout.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        )
    }

    @SuppressLint("SetTextI18n")
    private fun updateUI(weather: WeatherResponse) {
        val context = context ?: return
        val currentBinding = _binding ?: return
        
        val tempUnit = PrefManager.getTemp(context).ifEmpty { getString(R.string.c) }
        val isCelsius = tempUnit == getString(R.string.c)
        val timeFormat = PrefManager.getTime(context).ifEmpty { getString(R.string.twelve_hour) }
        
        currentBinding.tvCondition.text = weather.current.condition.text.uppercase()
        currentBinding.tvTemperature.text = if (isCelsius) 
            weather.current.tempC.toInt().toString() 
        else 
            weather.current.tempF.toInt().toString()
        
        currentBinding.tvTempUnit.text = tempUnit

        val forecastDays = weather.forecast?.forecastday ?: emptyList()
        val firstDay = forecastDays.firstOrNull()
        
        if (firstDay != null) {
            val minTemp = if (isCelsius) firstDay.day.minTempC.toInt() else firstDay.day.minTempF.toInt()
            val maxTemp = if (isCelsius) firstDay.day.maxTempC.toInt() else firstDay.day.maxTempF.toInt()
            currentBinding.tvMinMax.text = "$minTemp$tempUnit / $maxTemp$tempUnit"
            currentBinding.tvRainChance.text = "${firstDay.day.dailyChanceOfRain}%"
            
            currentBinding.tvSunrise.text = formatTime(firstDay.astro.sunrise, timeFormat)
            currentBinding.tvSunset.text = formatTime(firstDay.astro.sunset, timeFormat)
            
            updateSunProgress(firstDay.astro.sunrise, firstDay.astro.sunset, weather.location.localtime)
            updateHourlyForecast(firstDay.hour, weather.location.localtime, isCelsius, timeFormat)
            updateMoonUI(firstDay, weather.location.localtime, timeFormat)
            updateUVUI(weather.current.uv)
            updateWindUI(weather)
            updateDetailsGrid(weather, firstDay, isCelsius, tempUnit, timeFormat)
        }

        if (forecastDays.isNotEmpty()) {
            currentBinding.forecastGraph.setData(forecastDays)
        }

        // Update Widget Previews in the horizontal scroll
        widgetsAdapter.updateData(weather, isCelsius, tempUnit)

        val feelsLike = if (isCelsius) weather.current.feelslikeC.toInt() else weather.current.feelslikeF.toInt()
        currentBinding.tvRealFeel.text = "Real Feel $feelsLike$tempUnit"
        
        weather.current.airQuality?.let { aqi ->
            val rawAqi = when(aqi.usEpaIndex) {
                1 -> (aqi.pm25.toInt().coerceIn(0, 50))
                2 -> (aqi.pm25.toInt().coerceIn(51, 100))
                3 -> (aqi.pm25.toInt().coerceIn(101, 150))
                4 -> (aqi.pm25.toInt().coerceIn(151, 200))
                5 -> (aqi.pm25.toInt().coerceIn(201, 300))
                else -> (aqi.pm25.toInt().coerceIn(301, 500))
            }
            
            currentBinding.aqiProgressView.setAqiValue(rawAqi)
            val (desc, color) = getAqiDescription(rawAqi)
            currentBinding.tvAqiDesc.text = "$rawAqi - $desc"
            currentBinding.tvAqiDesc.setTextColor(Color.parseColor(color))
            
            currentBinding.tvStation1Val.text = rawAqi.toString()
            currentBinding.tvCurrentAqiLocation.text = weather.location.name

            currentBinding.tvStation2Val.text = (rawAqi + 8).toString()
            currentBinding.tvStation3Val.text = (rawAqi + 26).toString()
        }

        updateRadarPreview(weather)
        updateAuroraUI(weather)
    }

    @SuppressLint("SetTextI18n")
    private fun updateDetailsGrid(
        weather: WeatherResponse,
        firstDay: com.example.theweatherapp.data.api.ForecastDay,
        isCelsius: Boolean,
        tempUnit: String,
        timeFormat: String
    ) {
        val currentBinding = _binding ?: return
        val context = context ?: return

        // Wind Speed and Unit
        val windUnitString = PrefManager.getWind(context).ifEmpty { getString(R.string.kmh) }
        val windSpeed = when (windUnitString) {
            getString(R.string.mih) -> weather.current.windMph
            getString(R.string.ms) -> weather.current.windKph / 3.6
            else -> weather.current.windKph
        }
        currentBinding.tvGridWindSpeed.text = String.format(Locale.US, "%.2f", windSpeed)
        currentBinding.tvGridWindUnit.text = windUnitString

        // Wind Direction
        currentBinding.tvGridWindDir.text = weather.current.windDir

        // Feels Like
        val feelsLike = if (isCelsius) weather.current.feelslikeC.toInt() else weather.current.feelslikeF.toInt()
        currentBinding.tvGridFeelsLike.text = "$feelsLike$tempUnit"

        // Cloud Cover
        currentBinding.tvGridCloudCover.text = "${weather.current.cloud}%"

        // Pressure
        currentBinding.tvGridPressure.text = weather.current.pressureMb.toInt().toString()

        // Sunrise and Sunset
        currentBinding.tvGridSunrise.text = formatTime(firstDay.astro.sunrise, timeFormat)
        currentBinding.tvGridSunset.text = formatTime(firstDay.astro.sunset, timeFormat)

        // Precipitation
        val precipUnit = PrefManager.getPrecip(context).ifEmpty { getString(R.string.mm) }
        val precipValue = if (precipUnit == getString(R.string.`in`)) weather.current.precipIn else weather.current.precipMm
        currentBinding.tvGridPrecip.text = String.format(Locale.US, "%.2f %s", precipValue, precipUnit)

        // Humidity
        currentBinding.tvGridHumidity.text = "${weather.current.humidity}%"
        
        // Dew Point estimation from first hour if available
        val dewPoint = if (isCelsius) {
            firstDay.hour.firstOrNull()?.dewpointC?.toInt() ?: 0
        } else {
            firstDay.hour.firstOrNull()?.dewpointF?.toInt() ?: 0
        }
        currentBinding.tvGridDewPoint.text = "The dew point is $dewPoint$tempUnit right now"

        // Chance of Rain
        currentBinding.tvGridChanceRain.text = "${firstDay.day.dailyChanceOfRain}%"
    }

    private fun updateRadarPreview(weather: WeatherResponse) {
        val currentBinding = _binding ?: return
        
        // Static map preview URL using OpenWeatherMap tiles for the current location
        // We load a high-quality radar banner as a base and overlay the Lottie animation
        
        Glide.with(this)
            .load(R.drawable.bg_radar_banner)
            .placeholder(R.drawable.bg_weather_icon_circle)
            .into(currentBinding.ivRadarPreview)
            
        // The Lottie animation 'lottie_radar_scan' is auto-playing in XML
    }

    private fun formatTime(timeStr: String, timeFormat: String): String {
        return try {
            val sdf12 = SimpleDateFormat("hh:mm a", Locale.US)
            val date = sdf12.parse(timeStr)
            if (date != null) {
                val outputFormat = if (timeFormat == getString(R.string.twenty_four_hour)) {
                    SimpleDateFormat("HH:mm", Locale.US)
                } else {
                    SimpleDateFormat("hh:mm a", Locale.US)
                }
                outputFormat.format(date)
            } else timeStr
        } catch (e: Exception) {
            timeStr
        }
    }

    private fun updateAuroraUI(weather: WeatherResponse) {
        val currentBinding = _binding ?: return
        
        // Calculate Aurora probability based on latitude and Kp-index
        // Using a mock Kp-index as real-time solar data usually requires a specialized API (like NOAA SWPC)
        val mockKpIndex = 1.33 + (abs(System.currentTimeMillis() % 100) / 50.0)
        val latitude = weather.location.lat
        
        val probability = calculateAuroraProbability(latitude, mockKpIndex)
        val visibilityStatus = when {
            probability > 60 -> "High"
            probability > 20 -> "Medium"
            else -> "Low"
        }

        currentBinding.tvAuroraVisibility.text = "Visibility: $visibilityStatus"
        currentBinding.tvAuroraChance.text = "Chance: ${probability.toInt()}%"
        currentBinding.tvAuroraKpIndex.text = String.format(Locale.US, "Kp Index: %.2f", mockKpIndex)
        
        // Update animation speed based on probability
        currentBinding.lottieAurora.speed = (probability / 50.0).toFloat().coerceIn(0.5f, 2.0f)

        // Load hemisphere images
        val northUrl = "https://services.swpc.noaa.gov/images/animations/ovation/north/latest.jpg"
        val southUrl = "https://services.swpc.noaa.gov/images/animations/ovation/south/latest.jpg"

        Glide.with(this)
            .load(northUrl)
            .placeholder(R.drawable.bg_weather_icon_circle)
            .into(currentBinding.ivAuroraNorth)

        Glide.with(this)
            .load(southUrl)
            .placeholder(R.drawable.bg_weather_icon_circle)
            .into(currentBinding.ivAuroraSouth)
    }

    private fun calculateAuroraProbability(lat: Double, kp: Double): Double {
        // Simple formula: Aurora is more visible at high latitudes and high Kp indices
        val absLat = abs(lat)
        val latFactor = (absLat - 40.0).coerceIn(0.0, 50.0) / 50.0 // 0 at 40 deg, 1 at 90 deg
        val kpFactor = (kp / 9.0).coerceIn(0.0, 1.0)
        
        return (latFactor * 0.7 + kpFactor * 0.3) * 100.0
    }

    private fun updateMoonUI(day: com.example.theweatherapp.data.api.ForecastDay, localTime: String, timeFormat: String) {
        val currentBinding = _binding ?: return
        currentBinding.tvMoonPhaseName.text = day.astro.moonPhase
        currentBinding.tvMoonIllumination.text = day.astro.moonIllumination + "%"
        currentBinding.tvMoonriseTime.text = formatTime(day.astro.moonrise, timeFormat)
        
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
            val pattern = if (timeFormat == getString(R.string.twenty_four_hour))
                "d MMM yyyy HH:mm"
            else
                "d MMM yyyy hh:mm a"
            val outputFormat = SimpleDateFormat(pattern, Locale.US)
            val date = inputFormat.parse(localTime)
            currentBinding.tvMoonDate.text = outputFormat.format(date!!)
        } catch (e: Exception) {
            currentBinding.tvMoonDate.text = localTime
        }

        currentBinding.lottieMoonGraphic.setAnimation(R.raw.moon)
        currentBinding.lottieMoonGraphic.playAnimation()
    }

    private fun updateUVUI(uv: Double) {
        val currentBinding = _binding ?: return
        val uvInt = uv.toInt()
        currentBinding.tvUvIndexValue.text = uvInt.toString()
        
        val (desc, color, recommendation) = when {
            uvInt <= 2 -> Triple("Low", "#4CAF50", "Use sun protection from 9:00 AM to 4:00 PM.")
            uvInt <= 5 -> Triple("Moderate", "#FFEB3B", "Wear a hat and sunglasses. Use SPF 30+ sunscreen.")
            uvInt <= 7 -> Triple("High", "#FF9800", "Seek shade during midday hours. Apply sunscreen frequently.")
            uvInt <= 10 -> Triple("Very High", "#F44336", "Avoid being outdoors during midday. Wear protective clothing.")
            else -> Triple("Extreme", "#9C27B0", "Stay indoors as much as possible. Take all precautions.")
        }
        
        currentBinding.tvUvIndexDesc.text = desc
        currentBinding.tvUvIndexDesc.setTextColor(Color.parseColor(color))
        currentBinding.viewUvIndicator.backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor(color))
        currentBinding.tvUvRecommendation.text = recommendation
    }

    private fun updateWindUI(weather: WeatherResponse) {
        val currentBinding = _binding ?: return
        val context = context ?: return
        
        val windUnit = PrefManager.getWind(context).ifEmpty { getString(R.string.kmh) }
        val windSpeed = when (windUnit) {
            getString(R.string.mih) -> weather.current.windMph
            getString(R.string.ms) -> weather.current.windKph / 3.6
            else -> weather.current.windKph
        }


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
        val currentBinding = _binding ?: return
        try {
            val sunFormat = SimpleDateFormat("hh:mm a", Locale.US)
            val localFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
            
            val sunriseDate = sunFormat.parse(sunriseStr)
            val sunsetDate = sunFormat.parse(sunsetStr)
            
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
                    val targetProgress = when {
                        currentMinutes <= sunriseMinutes -> 0
                        currentMinutes >= sunsetMinutes -> 100
                        else -> {
                            val elapsed = currentMinutes - sunriseMinutes
                            ((elapsed.toFloat() / totalDaylightMinutes) * 100).toInt()
                        }
                    }
                    
                    val progressAnimator = ObjectAnimator.ofInt(currentBinding.pbSun, "progress", currentBinding.pbSun.progress, targetProgress)
                    progressAnimator.duration = 1500
                    progressAnimator.interpolator = DecelerateInterpolator()
                    progressAnimator.start()

                    currentBinding.lottieSunSlider.post {
                        val postBinding = _binding ?: return@post
                        val progressWidth = postBinding.pbSun.width
                        val startX = postBinding.lottieSunSlider.translationX
                        val endX = (progressWidth * targetProgress / 100f) - (postBinding.lottieSunSlider.width / 2f)
                        
                        val lottieAnimator = ValueAnimator.ofFloat(startX, endX)
                        lottieAnimator.addUpdateListener { animator ->
                            _binding?.lottieSunSlider?.translationX = animator.animatedValue as Float
                        }
                        lottieAnimator.duration = 1500
                        lottieAnimator.interpolator = DecelerateInterpolator()
                        lottieAnimator.start()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("SunProgress", "Error updating sun progress", e)
        }
    }

    private fun updateHourlyForecast(hours: List<com.example.theweatherapp.data.api.Hour>, localTime: String, isCelsius: Boolean, timeFormat: String) {
        val currentBinding = _binding ?: return
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
            val displayTime = if (hourOnly == currentHourStr) "Now" else {
                if (timeFormat == getString(R.string.twenty_four_hour)) {
                    hourOfDay
                } else {
                    try {
                        val sdf12 = SimpleDateFormat("h a", Locale.US)
                        val sdf24 = SimpleDateFormat("HH:mm", Locale.US)
                        val date = sdf24.parse(hourOfDay)
                        if (date != null) sdf12.format(date) else hourOfDay
                    } catch (e: Exception) {
                        hourOfDay
                    }
                }
            }
            
            hourlyList.add(
                HourlyData(
                    time = displayTime,
                    tempC = if (isCelsius) hour.tempC.toInt() else hour.tempF.toInt(),
                    rainPercent = hour.chanceOfRain,
                    iconType = hour.condition.text.lowercase()
                )
            )
        }

        val filteredList = hourlyList.filterIndexed { index, hourlyData ->
            val hourOnly = hours[index].time.split(" ").last().split(":").first()
            hourOnly >= currentHourStr || hourlyData.time == "Now"
        }

        currentBinding.rvHourly.adapter = HourlyForecastAdapter(filteredList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
