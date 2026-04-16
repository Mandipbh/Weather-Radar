package com.example.theweatherapp.ui.dashboard.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.theweatherapp.R
import com.example.theweatherapp.data.api.WeatherResponse
import com.example.theweatherapp.ui.dashboard.customise.model.WidgetType
import java.text.SimpleDateFormat
import java.util.Locale

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
                
                holder.tvUvHumidity.text = "UV Index: ${weather.current.airQuality?.usEpaIndex ?: 0}\nHumidity: ${weather.current.humidity}%"
                val forecast = weather.forecast?.forecastday?.firstOrNull()?.day
                val minT = if (isCelsius) forecast?.minTempC?.toInt() else forecast?.minTempF?.toInt()
                val maxT = if (isCelsius) forecast?.maxTempC?.toInt() else forecast?.maxTempF?.toInt()
                holder.tvMinMaxPrecip.text = "Min: $minT$tempUnit Max: $maxT$tempUnit\nPrecipitation: ${weather.current.precipMm} mm"

                // Hourly Forecast
                val localTime = weather.location.localtime
                val currentHourStr = try {
                    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    val date = sdf.parse(localTime)
                    SimpleDateFormat("HH", Locale.getDefault()).format(date!!)
                } catch (e: Exception) { "00" }

                val hours = weather.forecast?.forecastday?.firstOrNull()?.hour ?: emptyList()
                val upcomingHours = hours.filter { it.time.split(" ").last().split(":").first() >= currentHourStr }
                
                val forecastViews = listOf(holder.item1, holder.item2, holder.item3, holder.item4, holder.item5, holder.item6)
                forecastViews.forEachIndexed { index, view ->
                    if (index < upcomingHours.size) {
                        val hourData = upcomingHours[index]
                        view.findViewById<TextView>(R.id.tv_mini_time).text = if (index == 0) "Now" else hourData.time.split(" ").last()
                        view.findViewById<TextView>(R.id.tv_mini_temp).text = "${if (isCelsius) hourData.tempC.toInt() else hourData.tempF.toInt()}°"
                        view.findViewById<TextView>(R.id.tv_mini_rain).text = "${hourData.chanceOfRain}%"
                        view.visibility = View.VISIBLE
                    } else view.visibility = View.INVISIBLE
                }
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
        val tvUvHumidity: TextView = view.findViewById(R.id.tv_uv_humidity)
        val tvMinMaxPrecip: TextView = view.findViewById(R.id.tv_min_max_precip)
        val item1: View = view.findViewById(R.id.item_1)
        val item2: View = view.findViewById(R.id.item_2)
        val item3: View = view.findViewById(R.id.item_3)
        val item4: View = view.findViewById(R.id.item_4)
        val item5: View = view.findViewById(R.id.item_5)
        val item6: View = view.findViewById(R.id.item_6)
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
