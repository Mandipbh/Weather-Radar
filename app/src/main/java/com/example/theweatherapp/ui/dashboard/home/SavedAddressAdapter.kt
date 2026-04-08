package com.example.theweatherapp.ui.dashboard.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
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
        val ivTypeIcon: ImageView = view.findViewById(R.id.iv_type_icon)
        val tvAddressType: TextView = view.findViewById(R.id.tv_address_type)
        val tvFullAddress: TextView = view.findViewById(R.id.tv_full_address)
        val tvReceiverDetails: TextView = view.findViewById(R.id.tv_receiver_details)
        val rbSelected: RadioButton = view.findViewById(R.id.rb_selected)

        init {
            view.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(addresses[position])
                }
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
        
        holder.tvAddressType.text = item.addressType
        holder.tvFullAddress.text = item.fullAddress
        holder.tvReceiverDetails.text = "${item.receiverName} | ${item.pincode}"
        holder.rbSelected.isChecked = item.isSelected

        val iconRes = when (item.addressType.lowercase()) {
            "home" -> R.drawable.ic_home
            "office" -> R.drawable.ic_distance // Placeholder for work/office
            else -> R.drawable.ic_location_pin
        }
        holder.ivTypeIcon.setImageResource(iconRes)
    }

    override fun getItemCount() = addresses.size

    fun updateList(newList: List<SavedAddress>) {
        addresses = newList
        notifyDataSetChanged()
    }
}
