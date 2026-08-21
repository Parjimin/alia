package com.littleblueworld.alia.stars

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout

class StarFieldLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
) : FrameLayout(context, attrs) {
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val topInset = (TOP_INSET_DP * resources.displayMetrics.density).toInt()
        val bottomInset = (BOTTOM_INSET_DP * resources.displayMetrics.density).toInt()
        for (index in 0 until childCount) {
            val child = getChildAt(index)
            val placement = StarFieldLayoutSpec.placements.getOrNull(index) ?: continue
            val bounds = StarFieldLayoutSpec.boundsFor(
                placement = placement,
                parentWidth = width,
                parentHeight = height,
                childWidth = child.measuredWidth,
                childHeight = child.measuredHeight,
                topInset = topInset,
                bottomInset = bottomInset,
            )
            child.layout(bounds.left, bounds.top, bounds.right, bounds.bottom)
        }
    }

    private companion object {
        const val TOP_INSET_DP = 64
        const val BOTTOM_INSET_DP = 20
    }
}
