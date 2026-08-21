package com.littleblueworld.alia.wish

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.os.SystemClock
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.littleblueworld.alia.R
import kotlin.math.roundToInt

class WishEffectsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {
    private val density = resources.displayMetrics.density
    private val staticPaint = Paint().apply {
        isAntiAlias = false
        color = ContextCompat.getColor(context, R.color.cream_white)
        alpha = 90
    }
    private val sparkle = BitmapFactory.decodeResource(resources, R.drawable.sparkle_01)
    private val source = Rect(0, 0, sparkle.width, sparkle.height)
    private val destination = RectF()
    private val sparklePaint = Paint().apply {
        isAntiAlias = false
        isFilterBitmap = false
    }
    private var burstStartedAt = NO_BURST
    private var burstX = 0.5f
    private var burstY = 0.38f

    init {
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
    }

    fun burstAt(centerX: Float, centerY: Float) {
        burstX = centerX.coerceIn(0f, 1f)
        burstY = centerY.coerceIn(0f, 1f)
        burstStartedAt = SystemClock.uptimeMillis()
        postInvalidateOnAnimation()
    }

    fun stop() {
        burstStartedAt = NO_BURST
    }

    override fun onDetachedFromWindow() {
        stop()
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawStaticField(canvas)
        drawBurst(canvas)
    }

    private fun drawStaticField(canvas: Canvas) {
        for (index in STATIC_X.indices) {
            val x = width * STATIC_X[index]
            val y = height * STATIC_Y[index]
            val arm = (if (index % 3 == 0) 2f else 1.2f) * density
            val thickness = density
            canvas.drawRect(x - arm, y - thickness, x + arm, y + thickness, staticPaint)
            canvas.drawRect(x - thickness, y - arm, x + thickness, y + arm, staticPaint)
        }
    }

    private fun drawBurst(canvas: Canvas) {
        if (burstStartedAt == NO_BURST) return
        val progress = ((SystemClock.uptimeMillis() - burstStartedAt) / BURST_MS.toFloat())
            .coerceIn(0f, 1f)
        if (progress >= 1f) {
            stop()
            return
        }
        val eased = 1f - (1f - progress) * (1f - progress)
        val centerX = width * burstX
        val centerY = height * burstY
        val distance = 38f * density * eased
        val size = (12f + 4f * (1f - progress)) * density
        sparklePaint.alpha = ((1f - progress) * 230).roundToInt()
        for (index in BURST_X.indices) {
            val x = centerX + BURST_X[index] * distance
            val y = centerY + BURST_Y[index] * distance
            destination.set(x - size, y - size, x + size, y + size)
            canvas.drawBitmap(sparkle, source, destination, sparklePaint)
        }
        postInvalidateOnAnimation()
    }

    private companion object {
        const val NO_BURST = -1L
        const val BURST_MS = 850L
        val STATIC_X = floatArrayOf(0.08f, 0.18f, 0.31f, 0.44f, 0.61f, 0.73f, 0.87f, 0.94f)
        val STATIC_Y = floatArrayOf(0.16f, 0.34f, 0.12f, 0.47f, 0.21f, 0.42f, 0.14f, 0.56f)
        val BURST_X = floatArrayOf(-0.9f, 0f, 0.9f)
        val BURST_Y = floatArrayOf(-0.25f, -1f, -0.2f)
    }
}
