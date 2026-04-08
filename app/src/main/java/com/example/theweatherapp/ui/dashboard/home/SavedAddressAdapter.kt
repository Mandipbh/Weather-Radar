package com.example.theweatherapp.ui.dashboard.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.theweatherapp.R
import com.example.theweatherapp.ui.dashboard.home.model.SavedAddress

class SavedAddressAdapter(
    private var addresses: List<SavedAddress>,
    private val onDeleteClick: (SavedAddress) -> Unit,
    private val onItemClick: (SavedAddress) -> Unit
) : RecyclerView.Adapter<SavedAddressAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCityState: TextView = view.findViewById(R.id.tv_city_state)
        val tvPincode: TextView = view.findViewById(R.id.tv_pincode)
        val tvFullAddress: TextView = view.findViewById(R.id.tv_full_address)
        val btnDelete: ImageButton = view.findViewById(R.id.btn_delete)

        init {
            view.setOnClickListener {
                onItemClick(addresses[adapterPosition])
            }
            btnDelete.setOnClickListener {
                onDeleteClick(addresses[adapterPosition])
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_saved_address, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = addresses[position]
        holder.tvCityState.text = "${item.cityName}, ${item.stateName}"
        holder.tvPincode.text = item.pincode
        holder.tvFullAddress.text = item.fullAddress
    }

    override fun getItemCount() = addresses.size

    fun updateList(newList: List<SavedAddress>) {
        addresses = newList
        notifyDataSetChanged()
    }
}
