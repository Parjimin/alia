package com.littleblueworld.alia.stars

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.os.SystemClock
import android.util.AttributeSet
import android.view.View
import com.littleblueworld.alia.R
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class StarBurstView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {
    private val sparkle: Drawable? = context.getDrawable(R.drawable.sparkle_01)?.mutate()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val angleX = FloatArray(StarParticleBudget.TOTAL)
    private val angleY = FloatArray(StarParticleBudget.TOTAL)
    private val distance = FloatArray(StarParticleBudget.TOTAL)
    private val size = FloatArray(StarParticleBudget.TOTAL)

    private var centerX = 0f
    private var centerY = 0f
    private var startedAtMs = 0L
    private var active = false

    init {
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
        val density = resources.displayMetrics.density
        for (index in 0 until StarParticleBudget.TOTAL) {
            val angle = (2.0 * PI * index / StarParticleBudget.TOTAL) + (index % 3) * 0.09
            angleX[index] = cos(angle).toFloat()
            angleY[index] = sin(angle).toFloat()
            distance[index] = density * (34f + (index % 4) * 10f)
            size[index] = density * (if (index < StarParticleBudget.THEMED_SPARKLES) 13f else 3f)
        }
    }

    fun burstAt(x: Float, y: Float) {
        centerX = x
        centerY = y
        startedAtMs = SystemClock.uptimeMillis()
        active = true
        postInvalidateOnAnimation()
    }

    fun stop() {
        active = false
        sparkle?.alpha = 255
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (!active) return
        val elapsed = SystemClock.uptimeMillis() - startedAtMs
        val progress = (elapsed / DURATION_MS.toFloat()).coerceIn(0f, 1f)
        val eased = 1f - (1f - progress) * (1f - progress)
        val alpha = ((1f - progress) * 255).toInt().coerceIn(0, 255)

        for (index in 0 until StarParticleBudget.TOTAL) {
            val x = centerX + angleX[index] * distance[index] * eased
            val y = centerY + angleY[index] * distance[index] * eased
            val particleSize = size[index] * (0.65f + 0.55f * (1f - progress))
            if (index < StarParticleBudget.THEMED_SPARKLES) {
                sparkle?.let { drawable ->
                    drawable.alpha = alpha
                    drawable.setBounds(
                        (x - particleSize).toInt(),
                        (y - particleSize).toInt(),
                        (x + particleSize).toInt(),
                        (y + particleSize).toInt(),
                    )
                    drawable.draw(canvas)
                }
            } else {
                paint.color = if (index % 2 == 0) CREAM else PINK
                paint.alpha = alpha
                canvas.drawRect(x - particleSize, y - 1f, x + particleSize, y + 1f, paint)
                canvas.drawRect(x - 1f, y - particleSize, x + 1f, y + particleSize, paint)
            }
        }

        if (progress < 1f) postInvalidateOnAnimation() else stop()
    }

    private companion object {
        const val DURATION_MS = 680L
        val CREAM = Color.rgb(255, 249, 253)
        val PINK = Color.rgb(255, 169, 211)
    }
}
