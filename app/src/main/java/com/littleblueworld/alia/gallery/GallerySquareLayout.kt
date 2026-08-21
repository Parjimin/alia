package com.littleblueworld.alia.gallery

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import kotlin.math.min

class GallerySquareLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val maxSize = (MAX_SIZE_DP * resources.displayMetrics.density).toInt()
        val size = min(min(measuredWidth, measuredHeight), maxSize)
        setMeasuredDimension(size, size)
    }

    private companion object {
        const val MAX_SIZE_DP = 330
    }
}
