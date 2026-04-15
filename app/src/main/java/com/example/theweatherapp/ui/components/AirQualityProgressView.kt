package com.example.theweatherapp.ui.components

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator

class AirQualityProgressView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var aqiValue: Int = 0
    private var animatedValue: Float = 0f
    private val barHeight = 20f
    private val indicatorRadius = 15f
    private val bubbleRadius = 40f
    private val bubbleMargin = 20f

    private val barPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val indicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 30f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }
    private val scaleTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#80FFFFFF")
        textSize = 24f
        textAlign = Paint.Align.CENTER
    }

    private val colors = intArrayOf(
        Color.parseColor("#4CAF50"), // Green (Good)
        Color.parseColor("#FFEB3B"), // Yellow (Moderate)
        Color.parseColor("#FF9800"), // Orange (Unhealthy for Sensitive Groups)
        Color.parseColor("#F44336"), // Red (Unhealthy)
        Color.parseColor("#9C27B0"), // Purple (Very Unhealthy)
        Color.parseColor("#795548")  // Brown (Hazardous)
    )

    private val scale = floatArrayOf(0f, 50f, 100f, 150f, 200f, 300f, 500f)
    private var animator: ValueAnimator? = null

    fun setAqiValue(value: Int) {
        val startValue = animatedValue
        val endValue = value.toFloat()
        
        animator?.cancel()
        animator = ValueAnimator.ofFloat(startValue, endValue).apply {
            duration = 1500
            interpolator = DecelerateInterpolator()
            addUpdateListener {
                val value = it.animatedValue as Float
                this@AirQualityProgressView.animatedValue = value
                this@AirQualityProgressView.aqiValue = value.toInt()
                invalidate()
            }
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val padding = 60f
        val availableWidth = width - 2 * padding
        val centerY = height * 0.6f

        // Draw Gradient Bar
        val gradient = LinearGradient(
            padding, centerY, width - padding, centerY,
            colors, null, Shader.TileMode.CLAMP
        )
        barPaint.shader = gradient
        val rectF = RectF(padding, centerY - barHeight / 2, width - padding, centerY + barHeight / 2)
        canvas.drawRoundRect(rectF, barHeight / 2, barHeight / 2, barPaint)

        // Calculate Indicator Position using animatedValue
        val position = calculatePosition(animatedValue.toInt(), availableWidth)
        val indicatorX = padding + position

        // Draw Bubble
        val bubbleY = centerY - barHeight - bubbleMargin - bubbleRadius
        indicatorPaint.color = getColorForAqi(animatedValue.toInt())
        canvas.drawCircle(indicatorX, bubbleY, bubbleRadius, indicatorPaint)
        
        // Draw Triangle below bubble
        val path = Path()
        path.moveTo(indicatorX - 15f, bubbleY + bubbleRadius - 5f)
        path.lineTo(indicatorX + 15f, bubbleY + bubbleRadius - 5f)
        path.lineTo(indicatorX, bubbleY + bubbleRadius + 15f)
        path.close()
        canvas.drawPath(path, indicatorPaint)

        // Draw AQI Value in Bubble
        textPaint.color = Color.BLACK
        canvas.drawText(aqiValue.toString(), indicatorX, bubbleY + 10f, textPaint)

        // Draw Indicator on Bar
        canvas.drawCircle(indicatorX, centerY, indicatorRadius, indicatorPaint)
        indicatorPaint.color = Color.WHITE
        canvas.drawCircle(indicatorX, centerY, indicatorRadius * 0.6f, indicatorPaint)

        // Draw Scale Labels
        scale.forEach { s ->
            val x = padding + calculatePosition(s.toInt(), availableWidth)
            canvas.drawText(s.toInt().toString(), x, centerY + barHeight + 30f, scaleTextPaint)
            // Draw small dots on scale positions
            canvas.drawCircle(x, centerY, 4f, scaleTextPaint)
        }
    }

    private fun calculatePosition(value: Int, width: Float): Float {
        val clampedValue = value.coerceIn(0, 500)
        return when {
            clampedValue <= 50 -> (clampedValue / 50f) * (width / 6f)
            clampedValue <= 100 -> (width / 6f) + ((clampedValue - 50) / 50f) * (width / 6f)
            clampedValue <= 150 -> (2 * width / 6f) + ((clampedValue - 100) / 50f) * (width / 6f)
            clampedValue <= 200 -> (3 * width / 6f) + ((clampedValue - 150) / 50f) * (width / 6f)
            clampedValue <= 300 -> (4 * width / 6f) + ((clampedValue - 200) / 100f) * (width / 6f)
            else -> (5 * width / 6f) + ((clampedValue - 300) / 200f) * (width / 6f)
        }
    }

    private fun getColorForAqi(value: Int): Int {
        return when {
            value <= 50 -> colors[0]
            value <= 100 -> colors[1]
            value <= 150 -> colors[2]
            value <= 200 -> colors[3]
            value <= 300 -> colors[4]
            else -> colors[5]
        }
    }
}
