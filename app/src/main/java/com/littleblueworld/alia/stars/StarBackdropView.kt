package com.littleblueworld.alia.stars

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class StarBackdropView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : View(context, attrs) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val positions = floatArrayOf(
        0.10f, 0.15f, 0.35f, 0.10f, 0.58f, 0.16f, 0.88f, 0.12f,
        0.17f, 0.42f, 0.42f, 0.36f, 0.82f, 0.40f, 0.08f, 0.67f,
        0.29f, 0.78f, 0.66f, 0.72f, 0.91f, 0.64f, 0.15f, 0.91f,
        0.47f, 0.88f, 0.77f, 0.90f,
    )

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val density = resources.displayMetrics.density
        for (index in positions.indices step 2) {
            val x = width * positions[index]
            val y = height * positions[index + 1]
            val radius = if (index % 4 == 0) 1.5f * density else 1f * density
            paint.color = if (index % 6 == 0) SOFT_PINK else CREAM
            paint.alpha = if (index % 4 == 0) 150 else 105
            canvas.drawRect(x - radius, y - radius / 3f, x + radius, y + radius / 3f, paint)
            canvas.drawRect(x - radius / 3f, y - radius, x + radius / 3f, y + radius, paint)
        }
    }

    private companion object {
        val CREAM = Color.rgb(255, 249, 253)
        val SOFT_PINK = Color.rgb(255, 169, 211)
    }
}
