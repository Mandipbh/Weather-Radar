package com.example.theweatherapp.ui.dashboard.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import com.example.theweatherapp.base.BaseActivity
import com.example.theweatherapp.databinding.ActivityManageAddressBinding
import com.example.theweatherapp.ui.WeatherViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ManageAddressActivity : BaseActivity() {

    private lateinit var binding: ActivityManageAddressBinding
    private val weatherViewModel: WeatherViewModel by viewModels()
    private lateinit var adapter: SavedAddressAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageAddressBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener { finish() }
        binding.btnAddNew.setOnClickListener { 
            startActivity(Intent(this, PickLocationActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        adapter = SavedAddressAdapter(
            addresses = emptyList(),
            onDeleteClick = { address -> weatherViewModel.deleteAddress(address) },
            onItemClick = { address ->
                weatherViewModel.selectAddress(address)
                Toast.makeText(this, "Selected ${address.addressType}", Toast.LENGTH_SHORT).show()
            }
        )
        binding.rvAddresses.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        binding.rvAddresses.adapter = adapter
    }

    private fun observeViewModel() {
        weatherViewModel.savedAddresses.observe(this) { addresses ->
            if (addresses.isEmpty()) {
                binding.layoutEmptyState.visibility = View.VISIBLE
                binding.rvAddresses.visibility = View.GONE
            } else {
                binding.layoutEmptyState.visibility = View.GONE
                binding.rvAddresses.visibility = View.VISIBLE
                adapter.updateList(addresses)
            }
        }
    }
}
