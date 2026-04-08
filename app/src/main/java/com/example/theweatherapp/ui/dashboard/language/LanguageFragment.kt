package com.example.theweatherapp.ui.dashboard.language

import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
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
import com.example.theweatherapp.utils.LocaleHelper
import com.example.theweatherapp.utils.PrefManager

class LanguageFragment : Fragment() {

    private var _binding: FragmentLanguageBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LanguageViewModel by viewModels()

    private fun setupLanguageList() {
        val savedLang = PrefManager.getLanguage(requireContext())

        val dummyLanguages = listOf(
            LanguageModel("Device Default", "default", savedLang == "default"),
            LanguageModel("English (US)", "en", savedLang == "en"),
            LanguageModel("Hindi [हिन्दी]", "hi", savedLang == "hi"),
            LanguageModel("Spanish [Español]", "es", savedLang == "es"),
            LanguageModel("French [Français]", "fr", savedLang == "fr"),
            LanguageModel("Arabic [العربية]", "ar", savedLang == "ar"),
            LanguageModel("Bengali [বাংলা]", "bn", savedLang == "bn"),
            LanguageModel("Russian [Русский]", "ru", savedLang == "ru"),
            LanguageModel("Portuguese [Português]", "pt", savedLang == "pt"),
            LanguageModel("Urdu [اردو]", "ur", savedLang == "ur"),
            LanguageModel("German [Deutsch]", "de", savedLang == "de")
        )

        binding.rvLanguages.layoutManager = LinearLayoutManager(requireContext())
        binding.rvLanguages.isNestedScrollingEnabled = false

        binding.rvLanguages.adapter = LanguageAdapter(dummyLanguages, ::onLanguageSelected)
    }

    private fun onLanguageSelected(language: LanguageModel) {

        PrefManager.setLanguage(requireContext(), language.code)
        requireActivity().recreate()
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