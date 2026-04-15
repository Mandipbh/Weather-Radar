package com.example.theweatherapp.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.example.theweatherapp.R
import com.example.theweatherapp.utils.PrefManager

class WeatherWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_weather)
        
        // In a real app, you'd fetch data from a database or shared preferences
        // For now, let's just set some placeholder text
        views.setTextViewText(R.id.widget_location, "Current City")
        views.setTextViewText(R.id.widget_temp, "25°C")
        views.setTextViewText(R.id.widget_condition, "Partly Cloudy")
        views.setImageViewResource(R.id.widget_icon, R.drawable.ic_cloudy)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
