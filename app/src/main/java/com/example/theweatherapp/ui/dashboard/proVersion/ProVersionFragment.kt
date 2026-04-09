package com.example.theweatherapp.ui.dashboard.proVersion

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.theweatherapp.R
import com.example.theweatherapp.databinding.FragmentLanguageBinding
import com.example.theweatherapp.databinding.FragmentNotificationBinding
import com.example.theweatherapp.databinding.FragmentProVersionBinding

class ProVersionFragment : Fragment() {

    private var _binding: FragmentProVersionBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProVersionBinding.bind(view)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProVersionBinding.inflate(inflater, container, false)
        return binding.root
    }
}