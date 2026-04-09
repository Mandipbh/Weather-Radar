package com.example.theweatherapp.ui.dashboard.customise

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.theweatherapp.databinding.FragmentCustomiseBinding
import com.example.theweatherapp.databinding.FragmentLanguageBinding
import com.example.theweatherapp.ui.WeatherViewModel
import com.example.theweatherapp.ui.dashboard.customise.model.WidgetPreview
import com.example.theweatherapp.ui.dashboard.customise.model.WidgetType
import com.google.android.material.tabs.TabLayout

class CustomiseFragment : Fragment() {

    private var _binding: FragmentCustomiseBinding? = null
    private val binding get() = _binding!!

    private val weatherViewModel: WeatherViewModel by activityViewModels()
    private lateinit var adapter: WidgetsAdapter
    private var selectedWidgetId: Int = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCustomiseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        _binding = FragmentCustomiseBinding.bind(view)

        val prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        selectedWidgetId = prefs.getInt("selected_widget_id", 1)

        setupTabs()
        setupRecyclerView()
        loadWidgets()
        observeViewModel()
        setupAds()

        if (weatherViewModel.weatherData.value == null) {
            val apiKey = "YOUR_API_KEY"
            weatherViewModel.getWeather("auto:ip", apiKey)
        }
    }

    private fun setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    1 -> {
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

        binding.tabLayout.getTabAt(1)?.select()
    }

    private fun setupRecyclerView() {
        adapter = WidgetsAdapter(
            widgets = emptyList(),
            onWidgetSelected = {
                updateSelection(it.id)
            },
            onRefreshClicked = {
                val apiKey = "YOUR_API_KEY"
                val location = weatherViewModel.weatherData.value?.location?.name ?: "auto:ip"
                weatherViewModel.getWeather(location, apiKey)
            }
        )

        binding.rvWidgets.layoutManager = LinearLayoutManager(requireContext())
        binding.rvWidgets.adapter = adapter
    }

    private fun observeViewModel() {
        weatherViewModel.weatherData.observe(viewLifecycleOwner) {
            adapter.setWeatherData(it)
            loadWidgets()
        }

        weatherViewModel.currentAddress.observe(viewLifecycleOwner) {
            adapter.setCurrentAddress(it)
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

        val prefs = requireContext().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        prefs.edit().putInt("selected_widget_id", id).apply()

        loadWidgets()
    }

    private fun setupAds() {}

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}