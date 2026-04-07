package com.example.theweatherapp.ui.dashboard.customise.model

data class WidgetPreview(
    val id: Int,
    val type: WidgetType,
    val isSelected: Boolean = false
)

enum class WidgetType {
    DETAILED,
    LARGE_TEMP,
    LARGE_TIME,
    MINIMAL
}
