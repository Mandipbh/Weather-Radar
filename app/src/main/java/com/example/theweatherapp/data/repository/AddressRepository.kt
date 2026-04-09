package com.example.theweatherapp.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.theweatherapp.ui.dashboard.home.model.SavedAddress
import com.example.theweatherapp.utils.PrefManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AddressRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _savedAddresses = MutableLiveData<List<SavedAddress>>(PrefManager.getAddresses(context))
    val savedAddresses: LiveData<List<SavedAddress>> = _savedAddresses

    fun addAddress(address: SavedAddress) {
        val currentList = _savedAddresses.value?.toMutableList() ?: mutableListOf()
        if (address.isSelected) {
            currentList.forEach { it.isSelected = false }
        }
        currentList.add(address)
        updateAndSave(currentList)
    }

    fun selectAddress(address: SavedAddress) {
        val currentList = _savedAddresses.value ?: emptyList()
        
        // Check if already selected to avoid redundant updates and infinite loops
        val alreadySelected = currentList.find { it.isSelected }
        if (alreadySelected?.id == address.id) return
        
        val newList = currentList.map {
            it.copy(isSelected = it.id == address.id)
        }
        updateAndSave(newList)
    }

    fun clearSelection() {
        val currentList = _savedAddresses.value ?: emptyList()
        if (currentList.none { it.isSelected }) return
        
        val newList = currentList.map {
            it.copy(isSelected = false)
        }
        updateAndSave(newList)
    }

    fun deleteAddress(address: SavedAddress) {
        val currentList = _savedAddresses.value?.toMutableList() ?: mutableListOf()
        currentList.removeAll { it.id == address.id }
        updateAndSave(currentList)
    }

    private fun updateAndSave(list: List<SavedAddress>) {
        _savedAddresses.value = list
        PrefManager.saveAddresses(context, list)
    }
}