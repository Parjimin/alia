package com.littleblueworld.alia.gallery

import android.content.Context
import android.graphics.Matrix
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.ImageView

class FocalCropImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : ImageView(context, attrs) {
    private var focalOffsetX = 0f
    private var focalOffsetY = 0f

    init {
        scaleType = ScaleType.MATRIX
    }

    fun setFocalOffset(x: Float, y: Float) {
        focalOffsetX = x.coerceIn(-1f, 1f)
        focalOffsetY = y.coerceIn(-1f, 1f)
        updateImageMatrix()
    }

    override fun setImageDrawable(drawable: Drawable?) {
        super.setImageDrawable(drawable)
        updateImageMatrix()
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        updateImageMatrix()
    }

    private fun updateImageMatrix() {
        val image = drawable ?: return
        val transform = FocalCropCalculator.calculate(
            viewWidth = width,
            viewHeight = height,
            drawableWidth = image.intrinsicWidth,
            drawableHeight = image.intrinsicHeight,
            focalOffsetX = focalOffsetX,
            focalOffsetY = focalOffsetY,
        )
        imageMatrix = Matrix().apply {
            setScale(transform.scale, transform.scale)
            postTranslate(transform.translateX, transform.translateY)
        }
    }
}
