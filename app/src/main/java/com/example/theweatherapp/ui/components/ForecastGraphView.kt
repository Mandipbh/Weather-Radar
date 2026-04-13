package com.example.theweatherapp.ui.components

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.theweatherapp.R
import com.example.theweatherapp.data.api.ForecastDay
import java.text.SimpleDateFormat
import java.util.*

class ForecastGraphView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var forecastData: List<ForecastDay> = emptyList()

    private val maxTempPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#FFD700")
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }

    private val minTempPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#90EE90")
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }

    private val areaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#22FFFFFF")
        style = Paint.Style.FILL
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 28f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }

    private val dayLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 30f
        textAlign = Paint.Align.CENTER
    }

    private val rainPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#00E5FF")
        style = Paint.Style.FILL
        alpha = 100
    }

    private val snowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#F5A623")
        style = Paint.Style.FILL
        alpha = 100
    }

    private val precipLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }

    fun setData(data: List<ForecastDay>) {
        this.forecastData = data
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (forecastData.isEmpty()) return

        val padding = 80f
        val graphWidth = width - 2 * padding
        val stepX = if (forecastData.size > 1) graphWidth / (forecastData.size - 1) else 0f
        
        val maxTemps = forecastData.map { it.day.maxTempC }
        val minTemps = forecastData.map { it.day.minTempC }
        
        val overallMax = (maxTemps.maxOrNull() ?: 40.0) + 5
        val overallMin = (minTemps.minOrNull() ?: 0.0) - 5
        val tempRange = (overallMax - overallMin).coerceAtLeast(1.0)
        
        // Define vertical sections
        val labelsY = 40f
        val iconsY = 100f
        val graphTopY = 200f
        val graphBottomY = height * 0.65f
        val precipTopY = graphBottomY + 40f
        val precipBottomY = height - 40f
        
        val graphHeight = graphBottomY - graphTopY

        // 1. Draw Day Labels and Icons
        forecastData.forEachIndexed { i, day ->
            val x = padding + i * stepX
            
            // Day Name
            canvas.drawText(getDayName(day.date), x, labelsY, dayLabelPaint)
            
            // Weather Icon
            val iconRes = getIconRes(day.day.condition.text)
            val drawable = ContextCompat.getDrawable(context, iconRes)
            drawable?.let {
                val iconSize = 50
                it.setBounds((x - iconSize/2).toInt(), (iconsY - iconSize/2).toInt(), (x + iconSize/2).toInt(), (iconsY + iconSize/2).toInt())
                it.setTint(Color.WHITE)
                it.draw(canvas)
            }
        }

        // 2. Draw Temperature Graph
        val maxPath = Path()
        val minPath = Path()
        val areaPath = Path()

        forecastData.forEachIndexed { i, day ->
            val x = padding + i * stepX
            val yMax = graphTopY + (overallMax - day.day.maxTempC).toFloat() / tempRange.toFloat() * graphHeight
            val yMin = graphTopY + (overallMax - day.day.minTempC).toFloat() / tempRange.toFloat() * graphHeight

            if (i == 0) {
                maxPath.moveTo(x, yMax)
                minPath.moveTo(x, yMin)
                areaPath.moveTo(x, yMax)
            } else {
                // Curved lines
                val prevX = padding + (i - 1) * stepX
                val prevYMax = graphTopY + (overallMax - forecastData[i-1].day.maxTempC).toFloat() / tempRange.toFloat() * graphHeight
                val prevYMin = graphTopY + (overallMax - forecastData[i-1].day.minTempC).toFloat() / tempRange.toFloat() * graphHeight
                
                maxPath.cubicTo((prevX + x) / 2, prevYMax, (prevX + x) / 2, yMax, x, yMax)
                minPath.cubicTo((prevX + x) / 2, prevYMin, (prevX + x) / 2, yMin, x, yMin)
            }
            
            // Temp text
            canvas.drawText("${day.day.maxTempC.toInt()}°", x, yMax - 25, textPaint)
            canvas.drawText("${day.day.minTempC.toInt()}°", x, yMin + 45, textPaint)
        }

        // Complete area path for shading between max and min
        for (i in forecastData.indices.reversed()) {
            val x = padding + i * stepX
            val yMin = graphTopY + (overallMax - forecastData[i].day.minTempC).toFloat() / tempRange.toFloat() * graphHeight
            if (i == forecastData.size - 1) {
                areaPath.lineTo(x, yMin)
            } else {
                val nextX = padding + (i + 1) * stepX
                val nextYMin = graphTopY + (overallMax - forecastData[i+1].day.minTempC).toFloat() / tempRange.toFloat() * graphHeight
                areaPath.cubicTo((nextX + x) / 2, nextYMin, (nextX + x) / 2, yMin, x, yMin)
            }
        }
        areaPath.close()

        canvas.drawPath(areaPath, areaPaint)
        canvas.drawPath(maxPath, maxTempPaint)
        canvas.drawPath(minPath, minTempPaint)
        
        // Dots on temp lines
        forecastData.forEachIndexed { i, day ->
            val x = padding + i * stepX
            val yMax = graphTopY + (overallMax - day.day.maxTempC).toFloat() / tempRange.toFloat() * graphHeight
            val yMin = graphTopY + (overallMax - day.day.minTempC).toFloat() / tempRange.toFloat() * graphHeight
            canvas.drawCircle(x, yMax, 8f, maxTempPaint.apply { style = Paint.Style.FILL })
            canvas.drawCircle(x, yMin, 8f, minTempPaint.apply { style = Paint.Style.FILL })
        }

        // 3. Draw Precipitation Humps (Rain and Snow)
        drawPrecipitation(canvas, padding, stepX, precipBottomY)
    }

    private fun drawPrecipitation(canvas: Canvas, padding: Float, stepX: Float, bottomY: Float) {
        val maxHumpHeight = 80f
        
        forecastData.forEachIndexed { i, day ->
            val centerX = padding + i * stepX
            val rainChance = day.day.dailyChanceOfRain
            val snowChance = day.day.dailyChanceOfSnow
            
            if (rainChance > 0 || snowChance > 0) {
                val humpPath = Path()
                val chance = if (rainChance >= snowChance) rainChance else snowChance
                val activePaint = if (rainChance >= snowChance) rainPaint else snowPaint
                val activeLinePaint = precipLinePaint.apply { color = activePaint.color; alpha = 255 }
                
                val humpHeight = maxHumpHeight * (chance / 100f)
                
                humpPath.moveTo(centerX - stepX / 2.2f, bottomY)
                humpPath.quadTo(centerX, bottomY - humpHeight * 2, centerX + stepX / 2.2f, bottomY)
                
                canvas.drawPath(humpPath, activePaint)
                canvas.drawPath(humpPath, activeLinePaint)
                
                // Chance Text above hump
                canvas.drawText("${chance}%", centerX, bottomY - humpHeight - 20, textPaint.apply { color = activeLinePaint.color; textSize = 22f })
                
                // Rain/Snow total mm inside or near hump
                val totalPrecip = day.day.totalPrecipMm
                if (totalPrecip > 0) {
                    drawPrecipValue(canvas, centerX, bottomY - 30, totalPrecip, activeLinePaint.color)
                }
            } else {
                // Draw a small dot or baseline indicator for 0%
                canvas.drawCircle(centerX, bottomY, 4f, textPaint.apply { color = Color.parseColor("#44FFFFFF") })
                canvas.drawText("0%", centerX, bottomY - 15, textPaint.apply { textSize = 18f })
            }
        }
    }

    private fun drawPrecipValue(canvas: Canvas, x: Float, y: Float, value: Double, color: Int) {
        val dropDrawable = ContextCompat.getDrawable(context, R.drawable.ic_rain)
        dropDrawable?.let {
            val size = 24
            it.setBounds((x - size - 10).toInt(), (y - size/2).toInt(), (x - 10).toInt(), (y + size/2).toInt())
            it.setTint(color)
            it.draw(canvas)
        }
        canvas.drawText(String.format("%.1f", value), x + 15, y, textPaint.apply { this.color = Color.WHITE; textSize = 20f; textAlign = Paint.Align.LEFT })
        canvas.drawText("mm", x + 15, y + 15, textPaint.apply { this.color = Color.WHITE; textSize = 14f; textAlign = Paint.Align.LEFT })
    }

    private fun getIconRes(condition: String): Int {
        val c = condition.lowercase()
        return when {
            c.contains("rain") || c.contains("drizzle") -> R.drawable.ic_rain
            c.contains("snow") || c.contains("sleet") || c.contains("ice") -> R.drawable.ic_snow
            c.contains("cloud") || c.contains("overcast") -> R.drawable.ic_cloudy
            c.contains("sun") || c.contains("clear") -> R.drawable.ic_sunrise
            else -> R.drawable.ic_cloudy
        }
    }

    private fun getDayName(dateStr: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(dateStr)
            val today = Calendar.getInstance()
            val target = Calendar.getInstance().apply { time = date!! }
            
            if (today.get(Calendar.YEAR) == target.get(Calendar.YEAR) && 
                today.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)) {
                "Today"
            } else {
                SimpleDateFormat("EEE", Locale.getDefault()).format(date!!)
            }
        } catch (e: Exception) {
            ""
        }
    }
}
