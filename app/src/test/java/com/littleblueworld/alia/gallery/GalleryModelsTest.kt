package com.littleblueworld.alia.gallery

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class GalleryModelsTest {
    @Test
    fun `window keeps only previous current and next`() {
        assertEquals(GalleryWindow(null, 0, 1), GalleryWindow.around(0, 6))
        assertEquals(GalleryWindow(1, 2, 3), GalleryWindow.around(2, 6))
        assertEquals(GalleryWindow(4, 5, null), GalleryWindow.around(5, 6))
        assertNull(GalleryWindow.around(0, 1).previous)
        assertNull(GalleryWindow.around(0, 1).next)
    }

    @Test
    fun `distance threshold commits only toward an available photo`() {
        assertEquals(GallerySwipeResult.NEXT, decision(index = 0, drag = -23f))
        assertEquals(GallerySwipeResult.SNAP_BACK, decision(index = 0, drag = -21f))
        assertEquals(GallerySwipeResult.SNAP_BACK, decision(index = 0, drag = 40f))
        assertEquals(GallerySwipeResult.PREVIOUS, decision(index = 5, drag = 23f))
        assertEquals(GallerySwipeResult.SNAP_BACK, decision(index = 5, drag = -40f))
    }

    @Test
    fun `flick commits when velocity follows drag direction`() {
        assertEquals(GallerySwipeResult.NEXT, decision(index = 2, drag = -8f, velocity = -700f))
        assertEquals(GallerySwipeResult.PREVIOUS, decision(index = 2, drag = 8f, velocity = 700f))
        assertEquals(GallerySwipeResult.SNAP_BACK, decision(index = 2, drag = -8f, velocity = 700f))
    }

    @Test
    fun `focal crop moves within cover overflow`() {
        val center = FocalCropCalculator.calculate(100, 100, 200, 100, 0f, 0f)
        val left = FocalCropCalculator.calculate(100, 100, 200, 100, -1f, 0f)
        val right = FocalCropCalculator.calculate(100, 100, 200, 100, 1f, 0f)

        assertEquals(1f, center.scale, 0.001f)
        assertEquals(-50f, center.translateX, 0.001f)
        assertEquals(0f, left.translateX, 0.001f)
        assertEquals(-100f, right.translateX, 0.001f)
        assertEquals(0f, center.translateY, 0.001f)
    }

    private fun decision(index: Int, drag: Float, velocity: Float = 0f) =
        GallerySwipeDecision.decide(
            index = index,
            count = 6,
            dragPx = drag,
            widthPx = 100f,
            velocityPxPerSecond = velocity,
            velocityThresholdPxPerSecond = 600f,
        )
}
