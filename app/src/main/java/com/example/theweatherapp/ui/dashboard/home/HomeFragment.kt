package com.example.theweatherapp.ui.dashboard.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
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
import com.example.theweatherapp.ui.dashboard.home.model.SavedAddress
import com.google.android.material.bottomsheet.BottomSheetDialog
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
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
                binding.tvLocation.text = "Detecting Location..."
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
                // When an address is clicked, fetch weather for the city
                weatherViewModel.getWeather(address.cityName, "6397395029e04870817112041242311")
                Toast.makeText(context, "Fetching weather for ${address.cityName}", Toast.LENGTH_SHORT).show()
            }
        )
        binding.rvSavedAddresses.adapter = savedAddressAdapter
    }

    private fun setupClickListeners() {
        binding.btnAdd.setOnClickListener {
            showAddAddressBottomSheet()
        }
    }

    private fun showAddAddressBottomSheet() {
        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_add_address, null)

        val etPincode = view.findViewById<EditText>(R.id.et_pincode)
        val etCity = view.findViewById<EditText>(R.id.et_city_name)
        val etState = view.findViewById<EditText>(R.id.et_state_name)
        val etFull = view.findViewById<EditText>(R.id.et_full_address)
        val btnSave = view.findViewById<Button>(R.id.btn_save_address)

        btnSave.setOnClickListener {
            val pincode = etPincode.text.toString().trim()
            val city = etCity.text.toString().trim()
            val state = etState.text.toString().trim()
            val full = etFull.text.toString().trim()

            if (pincode.isNotEmpty() && city.isNotEmpty() && state.isNotEmpty() && full.isNotEmpty()) {
                weatherViewModel.addAddress(pincode, city, state, full)
                dialog.dismiss()
                Toast.makeText(context, "Address saved", Toast.LENGTH_SHORT).show()
            } else {
                if (pincode.isEmpty()) etPincode.error = "Enter pincode"
                if (city.isEmpty()) etCity.error = "Enter city"
                if (state.isEmpty()) etState.error = "Enter state"
                if (full.isEmpty()) etFull.error = "Enter full address"
            }
        }

        dialog.setContentView(view)
        dialog.show()
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
            (activity as? DashboardActivity)?.requestLocation()
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
                (activity as? DashboardActivity)?.requestLocation()
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
            updateHourlyForecast(forecastDay.hour, weather.location.localtime)
        }

        binding.tvRealFeel.text = "Real Feel ${weather.current.feelslikeC.toInt()}°C"
        binding.tvCurrentTime.text = weather.location.localtime

        //navigate to radar screen
        binding.llRadar.setOnClickListener {
            val intent = Intent(requireContext(), RadarActivity::class.java)
            startActivity(intent)
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
