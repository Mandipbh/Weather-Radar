package com.example.theweatherapp.ui.dashboard.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.theweatherapp.R
import com.example.theweatherapp.ui.dashboard.home.model.HourlyData

class HourlyForecastAdapter(
    private val hours: List<HourlyData>
) : RecyclerView.Adapter<HourlyForecastAdapter.HourlyViewHolder>() {

    inner class HourlyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTime: TextView   = view.findViewById(R.id.tv_hour_time)
        val ivIcon: ImageView  = view.findViewById(R.id.iv_hour_icon)
        val tvTemp: TextView   = view.findViewById(R.id.tv_hour_temp)
        val tvRain: TextView   = view.findViewById(R.id.tv_hour_rain)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        HourlyViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_hourly_forecast, parent, false)
        )

    override fun onBindViewHolder(h: HourlyViewHolder, position: Int) {
        val d = hours[position]
        h.tvTime.text = d.time
        h.tvTemp.text = "${d.tempC}°"
        h.tvRain.text = "${d.rainPercent}%"

        // Set appropriate icon
        val iconRes = when (d.iconType) {
            "sunny"  -> R.drawable.ic_sunrise
            "clear"  -> R.drawable.ic_star_filled
            "partly" -> R.drawable.ic_sunrise
            else     -> R.drawable.ic_cloudy
        }
        h.ivIcon.setImageResource(iconRes)

        // Highlight "Now"
        h.itemView.isSelected = position == 0
    }

    override fun getItemCount() = hours.size
}

