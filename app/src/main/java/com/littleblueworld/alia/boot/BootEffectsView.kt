package com.littleblueworld.alia.boot

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.os.SystemClock
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import com.littleblueworld.alia.R
import kotlin.math.roundToInt

/** A bounded pixel effect: static ambience plus a short signature-only sparkle burst. */
class BootEffectsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {
    private val density = resources.displayMetrics.density
    private val lavenderPaint = pixelPaint(R.color.lavender, STATIC_ALPHA)
    private val pinkPaint = pixelPaint(R.color.soft_pink, STATIC_ALPHA)
    private val skyPaint = pixelPaint(R.color.sky_blue, STATIC_ALPHA)
    private val burstPaint = pixelPaint(R.color.cream_white, 255)

    private var burstStartedAt = NO_BURST

    fun burst() {
        burstStartedAt = SystemClock.uptimeMillis()
        postInvalidateOnAnimation()
    }

    fun stop() {
        burstStartedAt = NO_BURST
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawStaticPixels(canvas)
        drawBurst(canvas)
    }

    private fun drawStaticPixels(canvas: Canvas) {
        for (index in STATIC_X.indices) {
            val size = dp(if (index % 3 == 0) 3f else 2f)
            val x = width * STATIC_X[index]
            val y = height * STATIC_Y[index]
            val paint = when (index % 3) {
                0 -> lavenderPaint
                1 -> pinkPaint
                else -> skyPaint
            }
            canvas.drawRect(x, y, x + size, y + size, paint)
        }
    }

    private fun drawBurst(canvas: Canvas) {
        if (burstStartedAt == NO_BURST) return

        val elapsed = SystemClock.uptimeMillis() - burstStartedAt
        val progress = (elapsed.toFloat() / BURST_DURATION_MS).coerceIn(0f, 1f)
        if (progress >= 1f) {
            burstStartedAt = NO_BURST
            return
        }

        val easedProgress = 1f - (1f - progress) * (1f - progress)
        val centerX = width * 0.5f
        val centerY = height * 0.43f
        val distance = dp(34f) * easedProgress
        val size = dp(if (progress < 0.55f) 3f else 2f)
        burstPaint.alpha = ((1f - progress) * 255).roundToInt()

        for (index in BURST_X.indices) {
            val x = centerX + BURST_X[index] * distance
            val y = centerY + BURST_Y[index] * distance
            canvas.drawRect(x - size, y - size, x + size, y + size, burstPaint)
        }
        postInvalidateOnAnimation()
    }

    private fun pixelPaint(
        @ColorRes color: Int,
        alpha: Int,
    ) = Paint().apply {
        isAntiAlias = false
        this.color = ContextCompat.getColor(context, color)
        this.alpha = alpha
        style = Paint.Style.FILL
    }

    private fun dp(value: Float): Float = value * density

    private companion object {
        const val NO_BURST = -1L
        const val BURST_DURATION_MS = 520L
        const val STATIC_ALPHA = 92

        val STATIC_X = floatArrayOf(0.12f, 0.82f, 0.23f, 0.91f, 0.70f, 0.08f, 0.77f)
        val STATIC_Y = floatArrayOf(0.20f, 0.16f, 0.72f, 0.63f, 0.82f, 0.51f, 0.34f)
        val BURST_X = floatArrayOf(-1f, -0.68f, 0f, 0.72f, 1f, 0.55f, -0.5f, 0f)
        val BURST_Y = floatArrayOf(0f, -0.72f, -1f, -0.67f, 0f, 0.78f, 0.72f, 1f)
    }
}
