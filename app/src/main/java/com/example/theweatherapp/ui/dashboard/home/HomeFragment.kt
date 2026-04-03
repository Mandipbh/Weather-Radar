package com.example.theweatherapp.ui.dashboard.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.example.theweatherapp.data.api.WeatherResponse
import com.example.theweatherapp.databinding.FragmentHomeBinding
import com.example.theweatherapp.ui.WeatherViewModel
import com.example.theweatherapp.ui.dashboard.DashboardActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // Use activityViewModels() to share the ViewModel with DashboardActivity
    private val weatherViewModel: WeatherViewModel by activityViewModels()

    private var autoRefreshJob: Job? = null
    private val refreshInterval = 15 * 60 * 1000L // Auto-refresh every 15 minutes

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

        setupSwipeRefresh()

        // Observe weather data from the shared ViewModel
        weatherViewModel.weatherData.observe(viewLifecycleOwner) { response ->
            response?.let { updateUI(it) }
        }

        // Observe address updates
        weatherViewModel.currentAddress.observe(viewLifecycleOwner) { address ->
            binding.tvLocation.text = address ?: "Unknown Location"
        }

        // Observe loading state
        weatherViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.swipeRefreshLayout.isRefreshing = isLoading
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
            // Manually trigger a refresh
            (activity as? DashboardActivity)?.requestLocation()
        }
        
        // Configure refresh colors
        binding.swipeRefreshLayout.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        )
    }

    private fun startAutoRefresh() {
        // Stop any existing job to avoid duplicates
        stopAutoRefresh()
        
        autoRefreshJob = viewLifecycleOwner.lifecycleScope.launch {
            while (isActive) {
                // Wait for the interval before the next refresh
                delay(refreshInterval)
                // Trigger refresh via the activity
                (activity as? DashboardActivity)?.requestLocation()
            }
        }
    }

    private fun stopAutoRefresh() {
        autoRefreshJob?.cancel()
        autoRefreshJob = null
    }

    private fun updateUI(weather: WeatherResponse) {
        // Main weather info
        binding.tvCondition.text = weather.current.condition.text
        binding.tvTemperature.text = weather.current.tempC.toInt().toString()
        
        // Min/Max and Rain
        val forecastDay = weather.forecast?.forecastday?.firstOrNull()
        if (forecastDay != null) {
            binding.tvMinMax.text = "${forecastDay.day.minTempC.toInt()}°C / ${forecastDay.day.maxTempC.toInt()}°C"
            binding.tvRainChance.text = "${forecastDay.day.dailyChanceOfRain}%"
        }

        // Real Feel
        binding.tvRealFeel.text = "Real Feel ${weather.current.feelslikeC.toInt()}°C"
        
        // Current Time from API
        binding.tvCurrentTime.text = weather.location.localtime
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
