package com.example.theweatherapp.ui.dashboard.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
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
            } else {
                binding.tvLocation.text = getString(R.string.detecting_location)
                binding.tvLocationLine2.text = ""
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
            val intent = Intent(requireContext(), ManageAddressActivity::class.java)
            startActivity(intent)
        }
        
        binding.btnTarget.setOnClickListener {
             // Re-detect current GPS location
             (activity as? DashboardActivity)?.requestLocation()
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

        val forecastDay = weather.forecast?.forecastday?.firstOrNull()
        if (forecastDay != null) {
            binding.tvMinMax.text = "${forecastDay.day.minTempC.toInt()}°C / ${forecastDay.day.maxTempC.toInt()}°C"
            binding.tvRainChance.text = "${forecastDay.day.dailyChanceOfRain}%"
            
            // Update Sunrise and Sunset
            binding.tvSunrise.text = forecastDay.astro.sunrise
            binding.tvSunset.text = forecastDay.astro.sunset
            
            // Update Sun Progress Bar
            updateSunProgress(forecastDay.astro.sunrise, forecastDay.astro.sunset, weather.location.localtime)
            
            updateHourlyForecast(forecastDay.hour, weather.location.localtime)
        }

        binding.tvRealFeel.text = "Real Feel ${weather.current.feelslikeC.toInt()}°C"
        
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
