package com.example.theweatherapp.ui.dashboard.language

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.theweatherapp.R
import com.example.theweatherapp.databinding.FragmentHomeBinding
import com.example.theweatherapp.databinding.FragmentLanguageBinding
import com.example.theweatherapp.ui.dashboard.home.HourlyForecastAdapter
import com.example.theweatherapp.ui.dashboard.language.model.LanguageModel

class LanguageFragment : Fragment() {

    private var _binding: FragmentLanguageBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LanguageViewModel by viewModels()

    private fun setupLanguageList() {
        val dummyLanguages = listOf(
            LanguageModel("Device Default", "default", true),
            LanguageModel("English (US)", "en"),
            LanguageModel("Hindi [हिन्दी]", "hi"),
            LanguageModel("Spanish [Español]", "es"),
            LanguageModel("French [Français]", "fr"),
            LanguageModel("Arabic [العربية]", "ar"),
            LanguageModel("Bengali [বাংলা]", "bn"),
            LanguageModel("Russian [Русский]", "ru"),
            LanguageModel("Portuguese [Português]", "pt"),
            LanguageModel("Urdu [اردو]", "ur"),
            LanguageModel("German [Deutsch]", "de")
        )

        binding.rvLanguages.layoutManager = LinearLayoutManager(requireContext())
        binding.rvLanguages.isNestedScrollingEnabled = false

        binding.rvLanguages.adapter = LanguageAdapter(dummyLanguages, ::onLanguageSelected)
    }

    private fun onLanguageSelected(language: LanguageModel) {
        Toast.makeText(requireContext(), language.name, Toast.LENGTH_SHORT).show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentLanguageBinding.bind(view)

        setupLanguageList()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLanguageBinding.inflate(inflater, container, false)
        return binding.root
    }
}