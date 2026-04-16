package com.example.theweatherapp.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.example.theweatherapp.R
import com.example.theweatherapp.data.api.WeatherResponse
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Locale

class WeatherWidgetProvider : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == "ACTION_WIDGET_PINNED") {
            val widgetType = intent.getIntExtra("EXTRA_WIDGET_TYPE", 0)
            val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
            prefs.edit().putInt("pending_widget_type", widgetType).apply()
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
        val pendingType = prefs.getInt("pending_widget_type", -1)

        for (appWidgetId in appWidgetIds) {
            if (pendingType != -1) {
                if (!prefs.contains("widget_type_$appWidgetId")) {
                    prefs.edit().putInt("widget_type_$appWidgetId", pendingType).apply()
                }
            }
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
        
        if (pendingType != -1) {
            prefs.edit().remove("pending_widget_type").apply()
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
        val typeId = prefs.getInt("widget_type_$appWidgetId", 0)
        val weatherJson = prefs.getString("last_weather_data", null)
        
        val weather = if (weatherJson != null) {
            try {
                Gson().fromJson(weatherJson, WeatherResponse::class.java)
            } catch (e: Exception) { null }
        } else null

        val layoutId = when (typeId) {
            0 -> R.layout.widget_detailed
            1 -> R.layout.widget_large_temp
            2 -> R.layout.widget_large_time
            else -> R.layout.widget_detailed
        }

        val views = RemoteViews(context.packageName, layoutId)
        
        if (weather != null) {
            val tempUnit = "°C"
            val localtime = weather.location.localtime
            
            views.setTextViewText(R.id.tv_location, "${weather.location.name}, ${weather.location.country}")
            views.setTextViewText(R.id.tv_temp, "${weather.current.tempC.toInt()}$tempUnit")
            views.setTextViewText(R.id.tv_condition, weather.current.condition.text)
            
            if (layoutId == R.layout.widget_large_temp || layoutId == R.layout.widget_large_time) {
                views.setTextViewText(R.id.tv_date, localtime.split(" ").firstOrNull() ?: "")
                views.setTextViewText(R.id.tv_time, localtime.split(" ").lastOrNull() ?: "")
                
                val condition = weather.current.condition.text.lowercase()
                val iconRes = getConditionIcon(condition)
                views.setImageViewResource(R.id.iv_condition_icon, iconRes)
            }

            if (layoutId == R.layout.widget_detailed) {
                views.setTextViewText(R.id.tv_time_date, localtime)
                views.setTextViewText(R.id.tv_uv_humidity, "UV Index: ${weather.current.airQuality?.usEpaIndex ?: 0}\nHumidity: ${weather.current.humidity}%")
                
                val forecast = weather.forecast?.forecastday?.firstOrNull()?.day
                val minT = forecast?.minTempC?.toInt() ?: 0
                val maxT = forecast?.maxTempC?.toInt() ?: 0
                views.setTextViewText(R.id.tv_min_max_precip, "Min: $minT$tempUnit Max: $maxT$tempUnit\nPrecipitation: ${weather.current.precipMm} mm")

                // Hourly Forecast
                val currentHourStr = try {
                    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                    val date = sdf.parse(localtime)
                    SimpleDateFormat("HH", Locale.getDefault()).format(date!!)
                } catch (e: Exception) { "00" }

                val hours = weather.forecast?.forecastday?.firstOrNull()?.hour ?: emptyList()
                val upcoming = hours.filter { it.time.split(" ").last().split(":").first() >= currentHourStr }
                
                val timeIds = listOf(R.id.tv_mini_time_1, R.id.tv_mini_time_2, R.id.tv_mini_time_3, R.id.tv_mini_time_4, R.id.tv_mini_time_5, R.id.tv_mini_time_6)
                val tempIds = listOf(R.id.tv_mini_temp_1, R.id.tv_mini_temp_2, R.id.tv_mini_temp_3, R.id.tv_mini_temp_4, R.id.tv_mini_temp_5, R.id.tv_mini_temp_6)
                val iconIds = listOf(R.id.iv_mini_icon_1, R.id.iv_mini_icon_2, R.id.iv_mini_icon_3, R.id.iv_mini_icon_4, R.id.iv_mini_icon_5, R.id.iv_mini_icon_6)
                val itemIds = listOf(R.id.item_1, R.id.item_2, R.id.item_3, R.id.item_4, R.id.item_5, R.id.item_6)

                for (i in 0 until 6) {
                    if (i < upcoming.size) {
                        val h = upcoming[i]
                        views.setTextViewText(timeIds[i], if (i == 0) "Now" else h.time.split(" ").last())
                        views.setTextViewText(tempIds[i], "${h.tempC.toInt()}°")
                        views.setImageViewResource(iconIds[i], getConditionIcon(h.condition.text.lowercase()))
                        views.setViewVisibility(itemIds[i], View.VISIBLE)
                    } else {
                        views.setViewVisibility(itemIds[i], View.GONE)
                    }
                }
            }
        } else {
            views.setTextViewText(R.id.tv_location, "Wait for data...")
        }

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    private fun getConditionIcon(condition: String): Int {
        return when {
            condition.contains("sunny") || condition.contains("clear") -> R.drawable.ic_sunrise
            condition.contains("rain") || condition.contains("drizzle") -> R.drawable.ic_rain
            condition.contains("cloud") -> R.drawable.ic_cloudy
            condition.contains("snow") -> R.drawable.ic_snow
            condition.contains("wind") -> R.drawable.ic_wind
            else -> R.drawable.ic_cloudy
        }
    }
}
