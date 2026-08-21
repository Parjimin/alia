package com.littleblueworld.alia.gallery

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.Drawable

class GalleryPlaceholderDrawable(
    private val photoNumber: Int,
) : Drawable() {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    override fun draw(canvas: Canvas) {
        val area = RectF(bounds)
        paint.color = COLORS[(photoNumber - 1) % COLORS.size]
        canvas.drawRect(area, paint)

        paint.color = Color.argb(36, 23, 50, 91)
        val grid = area.width() / 8f
        for (step in 0..8) {
            canvas.drawRect(area.left + step * grid, area.top, area.left + step * grid + 2f, area.bottom, paint)
            canvas.drawRect(area.left, area.top + step * grid, area.right, area.top + step * grid + 2f, paint)
        }

        paint.apply {
            color = Color.rgb(23, 50, 91)
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
            textSize = area.width() * 0.14f
        }
        canvas.drawText("PHOTO %02d".format(photoNumber), area.centerX(), area.centerY(), paint)
        paint.textSize = area.width() * 0.045f
        paint.typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        canvas.drawText("DEBUG PLACEHOLDER", area.centerX(), area.centerY() + area.width() * 0.09f, paint)
    }

    override fun setAlpha(alpha: Int) {
        paint.alpha = alpha
        invalidateSelf()
    }

    override fun setColorFilter(colorFilter: ColorFilter?) {
        paint.colorFilter = colorFilter
        invalidateSelf()
    }

    @Deprecated("Deprecated in Android")
    override fun getOpacity(): Int = PixelFormat.OPAQUE

    override fun getIntrinsicWidth(): Int = 768

    override fun getIntrinsicHeight(): Int = 768

    private companion object {
        val COLORS = intArrayOf(
            Color.rgb(255, 218, 235),
            Color.rgb(211, 232, 255),
            Color.rgb(231, 218, 255),
            Color.rgb(255, 235, 206),
            Color.rgb(202, 239, 233),
            Color.rgb(245, 220, 255),
        )
    }
}
