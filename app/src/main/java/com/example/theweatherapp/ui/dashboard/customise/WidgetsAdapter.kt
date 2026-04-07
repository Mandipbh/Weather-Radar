package com.example.theweatherapp.ui.dashboard.customise

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.theweatherapp.R
import com.example.theweatherapp.data.api.WeatherResponse
import com.example.theweatherapp.ui.dashboard.customise.model.WidgetPreview
import com.example.theweatherapp.ui.dashboard.customise.model.WidgetType
import java.text.SimpleDateFormat
import java.util.Locale

class WidgetsAdapter(
    private var widgets: List<WidgetPreview>,
    private val onWidgetSelected: (WidgetPreview) -> Unit,
    private val onRefreshClicked: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var weatherData: WeatherResponse? = null
    private var currentAddress: String? = null

    companion object {
        private const val TYPE_DETAILED = 0
        private const val TYPE_LARGE_TEMP = 1
        private const val TYPE_LARGE_TIME = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (widgets[position].type) {
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
        val widget = widgets[position]

        when (holder) {
            is DetailedViewHolder -> {
                holder.tvLocation.text = currentAddress ?: weatherData?.location?.name ?: "Detecting..."
                holder.tvTemp.text = "${weatherData?.current?.tempC?.toInt() ?: "--"}°"
                holder.tvCondition.text = weatherData?.current?.condition?.text ?: "--"
                holder.tvTimeDate.text = weatherData?.location?.localtime ?: "--"
                
                holder.tvUvHumidity.text = "UV Index: ${weatherData?.current?.uv?.toInt() ?: 0}\nHumidity: ${weatherData?.current?.humidity ?: 0}%"
                val forecast = weatherData?.forecast?.forecastday?.firstOrNull()?.day
                holder.tvMinMaxPrecip.text = "Min: ${forecast?.minTempC?.toInt() ?: 0}° Max: ${forecast?.maxTempC?.toInt() ?: 0}°\nPrecipitation: ${weatherData?.current?.precipMm ?: 0.0} mm"

                // Set Hourly Forecast - Filtering from current hour
                val localTime = weatherData?.location?.localtime ?: ""
                val currentHourStr = try {
                    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    val date = sdf.parse(localTime)
                    SimpleDateFormat("HH", Locale.getDefault()).format(date!!)
                } catch (e: Exception) {
                    "00"
                }

                val hours = weatherData?.forecast?.forecastday?.firstOrNull()?.hour ?: emptyList()
                val upcomingHours = hours.filter { it.time.split(" ").last().split(":").first() >= currentHourStr }
                
                val forecastViews = listOf(holder.item1, holder.item2, holder.item3, holder.item4, holder.item5, holder.item6)
                
                forecastViews.forEachIndexed { index, view ->
                    if (index < upcomingHours.size) {
                        val hourData = upcomingHours[index]
                        val time = hourData.time.split(" ").last()
                        view.findViewById<TextView>(R.id.tv_mini_time).text = if (index == 0) "Now" else time
                        view.findViewById<TextView>(R.id.tv_mini_temp).text = "${hourData.tempC.toInt()}°"
                        view.findViewById<TextView>(R.id.tv_mini_rain).text = "${hourData.chanceOfRain}%"
                        
                        val iconView = view.findViewById<ImageView>(R.id.iv_mini_icon)
                        val condition = hourData.condition.text.lowercase()
                        val iconRes = when {
                            condition.contains("sunny") || condition.contains("clear") -> R.drawable.ic_sunrise
                            condition.contains("rain") || condition.contains("drizzle") -> R.drawable.ic_rain
                            condition.contains("cloud") -> R.drawable.ic_cloudy
                            condition.contains("wind") -> R.drawable.ic_wind
                            else -> R.drawable.ic_cloudy
                        }
                        iconView.setImageResource(iconRes)
                        view.visibility = View.VISIBLE
                    } else {
                        view.visibility = View.INVISIBLE
                    }
                }

                holder.ivRefresh.setOnClickListener { onRefreshClicked() }
                holder.selectionOverlay.visibility = if (widget.isSelected) View.VISIBLE else View.GONE
            }
            is LargeTempViewHolder -> {
                holder.tvLocation.text = currentAddress ?: weatherData?.location?.name ?: "Detecting..."
                holder.tvTemp.text = "${weatherData?.current?.tempC?.toInt() ?: "--"}°"
                holder.tvCondition.text = weatherData?.current?.condition?.text ?: "--"
                holder.tvDate.text = weatherData?.location?.localtime?.split(" ")?.firstOrNull() ?: "--"
                holder.tvTime.text = weatherData?.location?.localtime?.split(" ")?.lastOrNull() ?: "--"
                
                holder.ivRefresh.setOnClickListener { onRefreshClicked() }
                holder.selectionOverlay.visibility = if (widget.isSelected) View.VISIBLE else View.GONE
            }
            is LargeTimeViewHolder -> {
                holder.tvLocation.text = currentAddress ?: weatherData?.location?.name ?: "Detecting..."
                holder.tvTemp.text = "${weatherData?.current?.tempC?.toInt() ?: "--"}°"
                holder.tvCondition.text = weatherData?.current?.condition?.text ?: "--"
                holder.tvDate.text = weatherData?.location?.localtime?.split(" ")?.firstOrNull() ?: "--"
                holder.tvTime.text = weatherData?.location?.localtime?.split(" ")?.lastOrNull() ?: "--"
                
                holder.ivRefresh.setOnClickListener { onRefreshClicked() }
                holder.selectionOverlay.visibility = if (widget.isSelected) View.VISIBLE else View.GONE
            }
        }

        holder.itemView.setOnClickListener {
            onWidgetSelected(widget)
        }
    }

    override fun getItemCount(): Int = widgets.size

    fun updateList(newList: List<WidgetPreview>) {
        widgets = newList
        notifyDataSetChanged()
    }

    fun setWeatherData(data: WeatherResponse?) {
        this.weatherData = data
        notifyDataSetChanged()
    }

    fun setCurrentAddress(address: String?) {
        this.currentAddress = address?.replace("|", ", ")
        notifyDataSetChanged()
    }

    class DetailedViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvLocation: TextView = view.findViewById(R.id.tv_location)
        val tvTemp: TextView = view.findViewById(R.id.tv_temp)
        val tvCondition: TextView = view.findViewById(R.id.tv_condition)
        val tvTimeDate: TextView = view.findViewById(R.id.tv_time_date)
        val ivRefresh: ImageView = view.findViewById(R.id.iv_refresh)
        val selectionOverlay: View = view.findViewById(R.id.selection_overlay)
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
        val ivRefresh: ImageView = view.findViewById(R.id.iv_refresh)
        val selectionOverlay: View = view.findViewById(R.id.selection_overlay)
    }

    class LargeTimeViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvLocation: TextView = view.findViewById(R.id.tv_location)
        val tvTemp: TextView = view.findViewById(R.id.tv_temp)
        val tvCondition: TextView = view.findViewById(R.id.tv_condition)
        val tvDate: TextView = view.findViewById(R.id.tv_date)
        val tvTime: TextView = view.findViewById(R.id.tv_time)
        val ivRefresh: ImageView = view.findViewById(R.id.iv_refresh)
        val selectionOverlay: View = view.findViewById(R.id.selection_overlay)
    }
}
