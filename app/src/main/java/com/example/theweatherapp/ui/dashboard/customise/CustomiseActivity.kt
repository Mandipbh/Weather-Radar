package com.example.theweatherapp.ui.dashboard.customise

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.theweatherapp.databinding.ActivityCustomiseBinding
import com.example.theweatherapp.ui.WeatherViewModel
import com.example.theweatherapp.ui.dashboard.customise.model.WidgetPreview
import com.example.theweatherapp.ui.dashboard.customise.model.WidgetType
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CustomiseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCustomiseBinding
    private val weatherViewModel: WeatherViewModel by viewModels()
    private lateinit var adapter: WidgetsAdapter
    private var selectedWidgetId: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCustomiseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load saved selection
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        selectedWidgetId = prefs.getInt("selected_widget_id", 1)

        setupToolbar()
        setupTabs()
        setupRecyclerView()
        
        // Initial load to show widgets even before weather data arrives
        loadWidgets()
        
        observeViewModel()
        setupAds()
        
        // If data is null, try to fetch it
        if (weatherViewModel.weatherData.value == null) {
            val apiKey = "e2ea45395b5f4367bc9135347260204"
            weatherViewModel.getWeather("auto:ip", apiKey)
        }
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    1 -> { // Widgets
                        binding.rvWidgets.visibility = View.VISIBLE
                        binding.tvPlaceholder.visibility = View.GONE
                    }
                    else -> {
                        binding.rvWidgets.visibility = View.GONE
                        binding.tvPlaceholder.visibility = View.VISIBLE
                        binding.tvPlaceholder.text = "${tab?.text} Coming Soon"
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // Select Widgets tab by default
        binding.tabLayout.getTabAt(1)?.select()
    }

    private fun setupRecyclerView() {
        adapter = WidgetsAdapter(
            widgets = emptyList(),
            onWidgetSelected = { selectedWidget ->
                updateSelection(selectedWidget.id)
            },
            onRefreshClicked = {
                val apiKey = "e2ea45395b5f4367bc9135347260204"
                val location = weatherViewModel.weatherData.value?.location?.name ?: "auto:ip"
                weatherViewModel.getWeather(location, apiKey)
            }
        )
        binding.rvWidgets.layoutManager = LinearLayoutManager(this)
        binding.rvWidgets.adapter = adapter
    }

    private fun observeViewModel() {
        weatherViewModel.weatherData.observe(this) { response ->
            adapter.setWeatherData(response)
            loadWidgets()
        }

        weatherViewModel.currentAddress.observe(this) { address ->
            adapter.setCurrentAddress(address)
            loadWidgets()
        }
    }

    private fun loadWidgets() {
        val widgets = listOf(
            WidgetPreview(1, WidgetType.DETAILED, selectedWidgetId == 1),
            WidgetPreview(2, WidgetType.LARGE_TEMP, selectedWidgetId == 2),
            WidgetPreview(3, WidgetType.LARGE_TIME, selectedWidgetId == 3)
        )
        adapter.updateList(widgets)
    }

    private fun updateSelection(id: Int) {
        selectedWidgetId = id
        
        // Save to preferences
        val prefs = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit().putInt("selected_widget_id", id).apply()
        
        loadWidgets()
    }

    private fun setupAds() {
        // Banner ad integration logic
    }
}
