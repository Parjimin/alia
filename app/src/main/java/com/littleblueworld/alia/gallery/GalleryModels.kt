package com.littleblueworld.alia.gallery

import kotlin.math.abs
import kotlin.math.max

data class GalleryWindow(
    val previous: Int?,
    val current: Int,
    val next: Int?,
) {
    companion object {
        fun around(current: Int, count: Int): GalleryWindow {
            require(count > 0)
            require(current in 0 until count)
            return GalleryWindow(
                previous = (current - 1).takeIf { it >= 0 },
                current = current,
                next = (current + 1).takeIf { it < count },
            )
        }
    }
}

enum class GallerySwipeResult {
    PREVIOUS,
    NEXT,
    SNAP_BACK,
}

object GallerySwipeDecision {
    const val DISTANCE_FRACTION = 0.22f

    fun decide(
        index: Int,
        count: Int,
        dragPx: Float,
        widthPx: Float,
        velocityPxPerSecond: Float,
        velocityThresholdPxPerSecond: Float,
    ): GallerySwipeResult {
        require(count > 0)
        require(index in 0 until count)
        if (widthPx <= 0f) return GallerySwipeResult.SNAP_BACK

        val passedDistance = abs(dragPx) >= widthPx * DISTANCE_FRACTION
        val passedVelocity = abs(velocityPxPerSecond) >= velocityThresholdPxPerSecond &&
            velocityPxPerSecond * dragPx >= 0f
        if (!passedDistance && !passedVelocity) return GallerySwipeResult.SNAP_BACK

        return when {
            dragPx < 0f && index < count - 1 -> GallerySwipeResult.NEXT
            dragPx > 0f && index > 0 -> GallerySwipeResult.PREVIOUS
            else -> GallerySwipeResult.SNAP_BACK
        }
    }
}

data class FocalCropTransform(
    val scale: Float,
    val translateX: Float,
    val translateY: Float,
)

object FocalCropCalculator {
    fun calculate(
        viewWidth: Int,
        viewHeight: Int,
        drawableWidth: Int,
        drawableHeight: Int,
        focalOffsetX: Float,
        focalOffsetY: Float,
    ): FocalCropTransform {
        if (viewWidth <= 0 || viewHeight <= 0 || drawableWidth <= 0 || drawableHeight <= 0) {
            return FocalCropTransform(1f, 0f, 0f)
        }

        val scale = max(
            viewWidth.toFloat() / drawableWidth,
            viewHeight.toFloat() / drawableHeight,
        )
        val scaledWidth = drawableWidth * scale
        val scaledHeight = drawableHeight * scale
        val overflowX = (scaledWidth - viewWidth).coerceAtLeast(0f)
        val overflowY = (scaledHeight - viewHeight).coerceAtLeast(0f)
        val x = focalOffsetX.coerceIn(-1f, 1f)
        val y = focalOffsetY.coerceIn(-1f, 1f)

        return FocalCropTransform(
            scale = scale,
            translateX = -overflowX * (x + 1f) / 2f,
            translateY = -overflowY * (y + 1f) / 2f,
        )
    }
}
