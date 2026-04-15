package com.example.theweatherapp.ui.dashboard.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.theweatherapp.R
import com.example.theweatherapp.data.api.WeatherResponse
import com.example.theweatherapp.ui.dashboard.customise.model.WidgetType

class HomeWidgetsAdapter(
    private val types: List<WidgetType> = listOf(WidgetType.DETAILED, WidgetType.LARGE_TEMP, WidgetType.LARGE_TIME)
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var weatherData: WeatherResponse? = null
    private var isCelsius: Boolean = true
    private var tempUnit: String = "°C"

    companion object {
        private const val TYPE_DETAILED = 0
        private const val TYPE_LARGE_TEMP = 1
        private const val TYPE_LARGE_TIME = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (types[position]) {
            WidgetType.DETAILED -> TYPE_DETAILED
            WidgetType.LARGE_TEMP -> TYPE_LARGE_TEMP
            WidgetType.LARGE_TIME -> TYPE_LARGE_TIME
            else -> TYPE_DETAILED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_DETAILED -> DetailedViewHolder(inflater.inflate(R.layout.item_widget_detailed, parent, false))
            TYPE_LARGE_TEMP -> LargeTempViewHolder(inflater.inflate(R.layout.item_widget_large_temp, parent, false))
            TYPE_LARGE_TIME -> LargeTimeViewHolder(inflater.inflate(R.layout.item_widget_large_time, parent, false))
            else -> DetailedViewHolder(inflater.inflate(R.layout.item_widget_detailed, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val weather = weatherData ?: return
        
        when (holder) {
            is DetailedViewHolder -> {
                holder.tvLocation.text = "${weather.location.name}, ${weather.location.country}"
                val temp = if (isCelsius) weather.current.tempC.toInt() else weather.current.tempF.toInt()
                holder.tvTemp.text = "$temp$tempUnit"
                holder.tvCondition.text = weather.current.condition.text
                holder.tvTimeDate.text = weather.location.localtime
            }
            is LargeTempViewHolder -> {
                holder.tvLocation.text = "${weather.location.name}, ${weather.location.country}"
                val temp = if (isCelsius) weather.current.tempC.toInt() else weather.current.tempF.toInt()
                holder.tvTemp.text = "$temp$tempUnit"
                holder.tvCondition.text = weather.current.condition.text
                holder.tvDate.text = weather.location.localtime.split(" ").firstOrNull() ?: ""
                holder.tvTime.text = weather.location.localtime.split(" ").lastOrNull() ?: ""
            }
            is LargeTimeViewHolder -> {
                holder.tvLocation.text = "${weather.location.name}, ${weather.location.country}"
                val temp = if (isCelsius) weather.current.tempC.toInt() else weather.current.tempF.toInt()
                holder.tvTemp.text = "$temp$tempUnit"
                holder.tvCondition.text = weather.current.condition.text
                holder.tvDate.text = weather.location.localtime.split(" ").firstOrNull() ?: ""
                holder.tvTime.text = weather.location.localtime.split(" ").lastOrNull() ?: ""
            }
        }
    }

    override fun getItemCount(): Int = if (weatherData == null) 0 else types.size

    fun updateData(weather: WeatherResponse, isCelsius: Boolean, tempUnit: String) {
        this.weatherData = weather
        this.isCelsius = isCelsius
        this.tempUnit = tempUnit
        notifyDataSetChanged()
    }

    class DetailedViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvLocation: TextView = view.findViewById(R.id.tv_location)
        val tvTemp: TextView = view.findViewById(R.id.tv_temp)
        val tvCondition: TextView = view.findViewById(R.id.tv_condition)
        val tvTimeDate: TextView = view.findViewById(R.id.tv_time_date)
    }

    class LargeTempViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvLocation: TextView = view.findViewById(R.id.tv_location)
        val tvTemp: TextView = view.findViewById(R.id.tv_temp)
        val tvCondition: TextView = view.findViewById(R.id.tv_condition)
        val tvDate: TextView = view.findViewById(R.id.tv_date)
        val tvTime: TextView = view.findViewById(R.id.tv_time)
    }

    class LargeTimeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvLocation: TextView = view.findViewById(R.id.tv_location)
        val tvTemp: TextView = view.findViewById(R.id.tv_temp)
        val tvCondition: TextView = view.findViewById(R.id.tv_condition)
        val tvDate: TextView = view.findViewById(R.id.tv_date)
        val tvTime: TextView = view.findViewById(R.id.tv_time)
    }
}
