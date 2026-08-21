package com.littleblueworld.alia.world

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
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

/** Sparse pixel ambience that only schedules frames during an active twinkle. */
class AmbientEffectsView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {
    private val density = resources.displayMetrics.density
    private val staticPaint = Paint().apply {
        isAntiAlias = false
        color = ContextCompat.getColor(context, R.color.cream_white)
        alpha = STATIC_ALPHA
    }
    private val twinklePaint = Paint().apply {
        isAntiAlias = false
        color = ContextCompat.getColor(context, R.color.cream_white)
    }
    private val sparkleBitmap = BitmapFactory.decodeResource(resources, R.drawable.sparkle_01)
    private val sparkleSource = Rect(0, 0, sparkleBitmap.width, sparkleBitmap.height)
    private val sparkleDestination = RectF()
    private val sparklePaint = Paint().apply {
        isAntiAlias = false
        isFilterBitmap = false
    }

    private var running = false
    private var twinkleStartedAt = NO_TWINKLE
    private var twinkleIndex = 0
    private var burstStartedAt = NO_BURST
    private var burstCenterX = 0.5f
    private var burstCenterY = 0.2f
    private val beginTwinkle = Runnable {
        if (!running) return@Runnable
        twinkleStartedAt = SystemClock.uptimeMillis()
        postInvalidateOnAnimation()
    }

    fun start() {
        if (running) return
        running = true
        invalidate()
        postDelayed(beginTwinkle, FIRST_TWINKLE_DELAY_MS)
    }

    fun stop() {
        running = false
        twinkleStartedAt = NO_TWINKLE
        burstStartedAt = NO_BURST
        removeCallbacks(beginTwinkle)
    }

    fun burstAt(centerX: Float, centerY: Float) {
        if (!running) return
        burstCenterX = centerX.coerceIn(0f, 1f)
        burstCenterY = centerY.coerceIn(0f, 1f)
        burstStartedAt = SystemClock.uptimeMillis()
        postInvalidateOnAnimation()
    }

    override fun onDetachedFromWindow() {
        stop()
        super.onDetachedFromWindow()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawStaticPixels(canvas)
        drawActiveTwinkle(canvas)
        drawUnlockBurst(canvas)
    }

    private fun drawStaticPixels(canvas: Canvas) {
        for (index in STATIC_X.indices) {
            val size = dp(if (index % 4 == 0) 2f else 1f)
            val x = width * STATIC_X[index]
            val y = height * STATIC_Y[index]
            canvas.drawRect(x, y, x + size, y + size, staticPaint)
        }
    }

    private fun drawActiveTwinkle(canvas: Canvas) {
        if (!running || twinkleStartedAt == NO_TWINKLE) return
        val elapsed = SystemClock.uptimeMillis() - twinkleStartedAt
        val progress = (elapsed.toFloat() / TWINKLE_DURATION_MS).coerceIn(0f, 1f)
        if (progress >= 1f) {
            twinkleStartedAt = NO_TWINKLE
            twinkleIndex = (twinkleIndex + 1) % TWINKLE_X.size
            postDelayed(beginTwinkle, NEXT_TWINKLE_DELAY_MS)
            return
        }

        val pulse = sin(progress * PI).toFloat()
        val arm = dp(2f + 3f * pulse)
        val thickness = dp(1.5f)
        val x = width * TWINKLE_X[twinkleIndex]
        val y = height * TWINKLE_Y[twinkleIndex]
        twinklePaint.alpha = (70 + pulse * 185).roundToInt()
        canvas.drawRect(x - arm, y - thickness, x + arm, y + thickness, twinklePaint)
        canvas.drawRect(x - thickness, y - arm, x + thickness, y + arm, twinklePaint)
        postInvalidateOnAnimation()
    }

    private fun drawUnlockBurst(canvas: Canvas) {
        if (!running || burstStartedAt == NO_BURST) return
        val elapsed = SystemClock.uptimeMillis() - burstStartedAt
        val progress = (elapsed.toFloat() / BURST_DURATION_MS).coerceIn(0f, 1f)
        if (progress >= 1f) {
            burstStartedAt = NO_BURST
            return
        }

        val eased = 1f - (1f - progress) * (1f - progress)
        val centerX = width * burstCenterX
        val centerY = height * burstCenterY
        val distance = dp(42f) * eased
        val spriteSize = dp(10f + 5f * (1f - progress))
        sparklePaint.alpha = ((1f - progress) * 235).roundToInt()

        for (index in BURST_X.indices) {
            val x = centerX + BURST_X[index] * distance
            val y = centerY + BURST_Y[index] * distance
            sparkleDestination.set(
                x - spriteSize,
                y - spriteSize,
                x + spriteSize,
                y + spriteSize,
            )
            canvas.drawBitmap(
                sparkleBitmap,
                sparkleSource,
                sparkleDestination,
                sparklePaint,
            )
        }

        val microDistance = dp(30f) * eased
        val microSize = dp(1.5f)
        twinklePaint.alpha = ((1f - progress) * 220).roundToInt()
        for (index in MICRO_X.indices) {
            val x = centerX + MICRO_X[index] * microDistance
            val y = centerY + MICRO_Y[index] * microDistance
            canvas.drawRect(
                x - microSize,
                y - microSize,
                x + microSize,
                y + microSize,
                twinklePaint,
            )
        }
        postInvalidateOnAnimation()
    }

    private fun dp(value: Float): Float = value * density

    private companion object {
        const val NO_TWINKLE = -1L
        const val NO_BURST = -1L
        const val STATIC_ALPHA = 76
        const val FIRST_TWINKLE_DELAY_MS = 1_100L
        const val NEXT_TWINKLE_DELAY_MS = 2_300L
        const val TWINKLE_DURATION_MS = 720L
        const val BURST_DURATION_MS = 1_050L

        val STATIC_X = floatArrayOf(0.08f, 0.16f, 0.29f, 0.41f, 0.57f, 0.68f, 0.79f, 0.91f)
        val STATIC_Y = floatArrayOf(0.18f, 0.58f, 0.36f, 0.66f, 0.29f, 0.55f, 0.17f, 0.62f)
        val TWINKLE_X = floatArrayOf(0.12f, 0.63f, 0.88f, 0.34f, 0.76f)
        val TWINKLE_Y = floatArrayOf(0.31f, 0.15f, 0.45f, 0.57f, 0.67f)
        val BURST_X = floatArrayOf(-1f, -0.55f, 0f, 0.6f, 1f)
        val BURST_Y = floatArrayOf(0f, -0.8f, -1f, -0.72f, 0.1f)
        val MICRO_X = floatArrayOf(-0.8f, -0.45f, 0f, 0.48f, 0.82f, 0.55f, -0.52f, 0f)
        val MICRO_Y = floatArrayOf(0.2f, -0.7f, -0.9f, -0.65f, 0.15f, 0.72f, 0.68f, 0.9f)
    }
}
