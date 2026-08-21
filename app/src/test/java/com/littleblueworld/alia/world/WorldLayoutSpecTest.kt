package com.littleblueworld.alia.world

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WorldLayoutSpecTest {
    @Test
    fun approvedLandmarkRowsRemainOrdered() {
        val specs = WorldLayoutSpec.objects

        assertIncreasing(
            specs.getValue(WorldObjectId.CAMERA).centerX,
            specs.getValue(WorldObjectId.WISH).centerX,
            specs.getValue(WorldObjectId.STAR).centerX,
        )
        assertIncreasing(
            specs.getValue(WorldObjectId.BOTTLE).centerX,
            specs.getValue(WorldObjectId.AUTHOR_NOTE).centerX,
        )
        assertIncreasing(
            specs.getValue(WorldObjectId.SHELL).centerX,
            specs.getValue(WorldObjectId.ISLAND).centerX,
            specs.getValue(WorldObjectId.CAFE).centerX,
        )
    }

    @Test
    fun everyLandmarkStaysInsideSmallNormalAndTallViewports() {
        val viewports = listOf(
            320 to 640,
            360 to 800,
            430 to 932,
        )
        val landmarks = WorldObjectId.entries.filterNot {
            it == WorldObjectId.CLOUD_FAR || it == WorldObjectId.CLOUD_NEAR
        }

        viewports.forEach { (width, height) ->
            val scale = WorldLayoutSpec.scaleForWidth(width.toFloat())
            landmarks.forEach { id ->
                val spec = WorldLayoutSpec.objects.getValue(id)
                val rect = WorldLayoutCalculator.place(
                    viewportWidth = width,
                    viewportHeight = height,
                    objectWidth = (spec.widthDp * scale).toInt(),
                    objectHeight = (spec.heightDp * scale).toInt(),
                    spec = spec,
                )
                assertTrue("$id left edge at $width x $height", rect.left >= 0)
                assertTrue("$id top edge at $width x $height", rect.top >= 0)
                assertTrue("$id right edge at $width x $height", rect.right <= width)
                assertTrue("$id bottom edge at $width x $height", rect.bottom <= height)
            }
        }
    }

    @Test
    fun responsiveScaleIsBounded() {
        assertEquals(0.88f, WorldLayoutSpec.scaleForWidth(240f), 0.001f)
        assertEquals(1f, WorldLayoutSpec.scaleForWidth(360f), 0.001f)
        assertEquals(1.16f, WorldLayoutSpec.scaleForWidth(600f), 0.001f)
    }

    @Test
    fun interactiveLandmarkTargetsMeetMinimumSize() {
        listOf(
            WorldObjectId.CAMERA,
            WorldObjectId.WISH,
            WorldObjectId.STAR,
            WorldObjectId.BOTTLE,
            WorldObjectId.AUTHOR_NOTE,
            WorldObjectId.CAFE,
        ).forEach { id ->
            val spec = WorldLayoutSpec.objects.getValue(id)
            assertTrue("$id width", spec.widthDp >= 48f)
            assertTrue("$id height", spec.heightDp >= 48f)
        }
    }

    private fun assertIncreasing(vararg values: Float) {
        for (index in 0 until values.lastIndex) {
            assertTrue(values[index] < values[index + 1])
        }
    }
}
