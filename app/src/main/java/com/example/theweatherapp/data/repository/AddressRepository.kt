package com.example.theweatherapp.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.theweatherapp.ui.dashboard.home.model.SavedAddress
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AddressRepository @Inject constructor() {
    private val _savedAddresses = MutableLiveData<List<SavedAddress>>(emptyList())
    val savedAddresses: LiveData<List<SavedAddress>> = _savedAddresses

    fun addAddress(address: SavedAddress) {
        val currentList = _savedAddresses.value?.toMutableList() ?: mutableListOf()
        if (address.isSelected) {
            currentList.forEach { it.isSelected = false }
        }
        currentList.add(address)
        _savedAddresses.value = currentList
    }

    fun selectAddress(address: SavedAddress) {
        val currentList = _savedAddresses.value?.map {
            it.copy(isSelected = it.id == address.id)
        } ?: emptyList()
        _savedAddresses.value = currentList
    }

    fun deleteAddress(address: SavedAddress) {
        val currentList = _savedAddresses.value?.toMutableList() ?: mutableListOf()
        currentList.removeAll { it.id == address.id }
        _savedAddresses.value = currentList
    }
}
