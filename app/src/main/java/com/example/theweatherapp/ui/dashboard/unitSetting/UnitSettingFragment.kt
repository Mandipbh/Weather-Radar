package com.example.theweatherapp.ui.dashboard.unitSetting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.theweatherapp.databinding.FragmentUnitSettingBinding
import com.example.theweatherapp.utils.PrefManager

class UnitSettingFragment : Fragment() {

    private var _binding: FragmentUnitSettingBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUnitSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        setPrefData()

        binding.btnDone.setOnClickListener {
            if (!validate()) return@setOnClickListener

            PrefManager.saveUnits(
                requireContext(),
                getSelectedChipText(binding.chipTemp.checkedChipId),
                getSelectedChipText(binding.chipTime.checkedChipId),
                getSelectedChipText(binding.chipPrecip.checkedChipId),
                getSelectedChipText(binding.chipDistance.checkedChipId),
                getSelectedChipText(binding.chipWind.checkedChipId),
                getSelectedChipText(binding.chipPressure.checkedChipId),
                binding.switchNotif.isChecked
            )

            Toast.makeText(requireContext(), "Saved", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validate(): Boolean {
        if (binding.chipTemp.checkedChipId == -1 ||
            binding.chipTime.checkedChipId == -1 ||
            binding.chipPrecip.checkedChipId == -1 ||
            binding.chipDistance.checkedChipId == -1 ||
            binding.chipWind.checkedChipId == -1 ||
            binding.chipPressure.checkedChipId == -1
        ) {
            Toast.makeText(requireContext(), "Select all fields", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun getSelectedChipText(id: Int): String {
        val chip = binding.root.findViewById<com.google.android.material.chip.Chip>(id)
        return chip.text.toString()
    }

    private fun setPrefData() {
        setSelection(binding.chipTemp, PrefManager.getTemp(requireContext()))
        setSelection(binding.chipTime, PrefManager.getTime(requireContext()))
        setSelection(binding.chipPrecip, PrefManager.getPrecip(requireContext()))
        setSelection(binding.chipDistance, PrefManager.getDistance(requireContext()))
        setSelection(binding.chipWind, PrefManager.getWind(requireContext()))
        setSelection(binding.chipPressure, PrefManager.getPressure(requireContext()))

        binding.switchNotif.isChecked = PrefManager.getNotif(requireContext())
    }

    private fun setSelection(group: com.google.android.material.chip.ChipGroup, value: String) {
        for (i in 0 until group.childCount) {
            val chip = group.getChildAt(i) as com.google.android.material.chip.Chip
            if (chip.text.toString() == value) {
                chip.isChecked = true
                break
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}