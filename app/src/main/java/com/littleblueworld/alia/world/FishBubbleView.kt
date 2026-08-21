package com.littleblueworld.alia.world

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.SystemClock
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.littleblueworld.alia.R
import kotlin.math.roundToInt

class FishBubbleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {
    private val density = resources.displayMetrics.density
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 1.5f * density
        color = ContextCompat.getColor(context, R.color.cream_white)
    }
    private var centerX = 0f
    private var centerY = 0f
    private var startedAtMs = NO_BUBBLES

    init {
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
    }

    fun bubbleAt(x: Float, y: Float) {
        centerX = x
        centerY = y
        startedAtMs = SystemClock.uptimeMillis()
        postInvalidateOnAnimation()
    }

    fun stop() {
        startedAtMs = NO_BUBBLES
    }

    override fun onDetachedFromWindow() {
        stop()
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (startedAtMs == NO_BUBBLES) return
        val elapsed = SystemClock.uptimeMillis() - startedAtMs
        val progress = (elapsed / DURATION_MS.toFloat()).coerceIn(0f, 1f)
        if (progress >= 1f) {
            stop()
            return
        }

        val eased = 1f - (1f - progress) * (1f - progress)
        paint.alpha = ((1f - progress) * 220).roundToInt()
        for (index in OFFSETS_X.indices) {
            val x = centerX + OFFSETS_X[index] * density
            val y = centerY - (OFFSETS_Y[index] + RISE_DP * eased) * density
            val radius = (2.5f + index * 0.7f) * density * (0.8f + 0.35f * eased)
            canvas.drawCircle(x, y, radius, paint)
        }
        postInvalidateOnAnimation()
    }

    private companion object {
        const val NO_BUBBLES = -1L
        const val DURATION_MS = 620L
        const val RISE_DP = 24f
        val OFFSETS_X = floatArrayOf(0f, 8f, 3f, 13f)
        val OFFSETS_Y = floatArrayOf(0f, 4f, 10f, 15f)
    }
}
