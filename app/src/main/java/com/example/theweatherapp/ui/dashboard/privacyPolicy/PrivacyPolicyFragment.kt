package com.example.theweatherapp.ui.dashboard.privacyPolicy

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import com.example.theweatherapp.R
import com.example.theweatherapp.databinding.FragmentLanguageBinding
import com.example.theweatherapp.databinding.FragmentNotificationBinding
import com.example.theweatherapp.databinding.FragmentPrivacyPolicyBinding

class PrivacyPolicyFragment : Fragment() {

    private var _binding: FragmentPrivacyPolicyBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentPrivacyPolicyBinding.bind(view)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPrivacyPolicyBinding.inflate(inflater, container, false)


        binding.webView.settings.javaScriptEnabled = true
        binding.webView.webViewClient = WebViewClient()

        binding.webView.loadUrl("https://www.google.com")

        return binding.root
    }
}