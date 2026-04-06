package com.example.theweatherapp.ui.dashboard.feedback

import android.content.Intent
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.theweatherapp.R
import com.example.theweatherapp.databinding.FragmentFeedbackBinding
import com.example.theweatherapp.databinding.FragmentLanguageBinding

class FeedbackFragment : Fragment() {
    private var _binding: FragmentFeedbackBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FeedbackViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentFeedbackBinding.bind(view)

        binding.btnSendFeedback.setOnClickListener {
            sendFeedbackEmail()
        }

        binding.btnShareTop.setOnClickListener {
            sendFeedbackEmail()
        }
    }

    private fun sendFeedbackEmail() {
        val title = binding.etTitle.text.toString().trim()
        val message = binding.etMessage.text.toString().trim()

        if (title.isEmpty()) {
            binding.etTitle.error = "Please enter title"
            return
        }

        if (message.isEmpty()) {
            binding.etMessage.error = "Please enter message"
            return
        }

        val emailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf("lion@liolan.com"))
            putExtra(Intent.EXTRA_SUBJECT, title)
            putExtra(Intent.EXTRA_TEXT, message)
        }

        try {
            startActivity(Intent.createChooser(emailIntent, "Send Feedback"))
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "No email app found", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFeedbackBinding.inflate(inflater, container, false)
        return binding.root
    }
}
