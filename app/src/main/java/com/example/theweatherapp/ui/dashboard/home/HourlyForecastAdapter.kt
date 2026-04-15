package com.example.theweatherapp.ui.dashboard.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.theweatherapp.R
import com.example.theweatherapp.ui.dashboard.home.model.HourlyData

class HourlyForecastAdapter(
    private val hours: List<HourlyData>
) : RecyclerView.Adapter<HourlyForecastAdapter.HourlyViewHolder>() {

    inner class HourlyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTime: TextView = view.findViewById(R.id.tv_hour_time)
        val ivIcon: ImageView = view.findViewById(R.id.iv_hour_icon)
        val lottieIcon: LottieAnimationView = view.findViewById(R.id.lottie_hour_icon)
        val tvTemp: TextView = view.findViewById(R.id.tv_hour_temp)
        val tvRain: TextView = view.findViewById(R.id.tv_hour_rain)
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

        // Set appropriate Lottie animation
        val lottieRes = when {
            d.iconType.contains("sunny") || d.iconType.contains("clear") -> R.raw.sunny
            else -> R.raw.cloud
        }
        
        h.lottieIcon.setAnimation(lottieRes)
        h.lottieIcon.playAnimation()
        
        // Hide static icon
        h.ivIcon.visibility = View.GONE
        h.lottieIcon.visibility = View.VISIBLE

        // Highlight "Now"
        h.itemView.isSelected = position == 0
    }

    override fun getItemCount() = hours.size
}
