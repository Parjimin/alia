package com.littleblueworld.alia.stars

import com.littleblueworld.alia.content.BirthdayContent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StarModelsTest {
    @Test
    fun `each star collects once and completion requires all three`() {
        val tracker = StarCollectionTracker()

        assertTrue(tracker.collect(StarKind.ENERGY).accepted)
        assertFalse(tracker.collect(StarKind.ENERGY).accepted)
        assertFalse(tracker.completed)
        assertTrue(tracker.collect(StarKind.MIND).accepted)
        val final = tracker.collect(StarKind.FACE)

        assertEquals(3, final.collectedCount)
        assertTrue(final.completed)
        assertTrue(tracker.completed)
    }

    @Test
    fun `face reveal preserves exact lines and approved timing`() {
        val content = BirthdayContent()
        val timeline = FaceRevealTimeline.from(content.faceReveal)

        assertEquals(content.faceReveal, timeline.map { it.text })
        assertEquals(listOf(0L, 800L, 800L, 300L, 500L), timeline.map { it.delayAfterPreviousMs })
    }

    @Test
    fun `star copy stays frozen`() {
        val content = BirthdayContent()

        assertEquals("YOUR ENERGY", content.energyTitle)
        assertEquals("YOUR MIND", content.mindTitle)
        assertEquals(listOf("you found all three.", "good job, apparently."), content.starsCompletion)
        assertEquals("back to the ocean", content.starsCta)
    }

    @Test
    fun `three stars do not overlap on narrow viewport`() {
        val bounds = StarFieldLayoutSpec.placements.map { placement ->
            StarFieldLayoutSpec.boundsFor(
                placement = placement,
                parentWidth = 320,
                parentHeight = 504,
                childWidth = 104,
                childHeight = 132,
                topInset = 64,
                bottomInset = 20,
            )
        }

        bounds.forEach {
            assertTrue(it.left >= 0)
            assertTrue(it.top >= 64)
            assertTrue(it.right <= 320)
            assertTrue(it.bottom <= 484)
        }
        bounds.forEachIndexed { index, current ->
            bounds.drop(index + 1).forEach { other -> assertFalse(current.overlaps(other)) }
        }
    }

    @Test
    fun `burst budget stays bounded`() {
        assertEquals(6, StarParticleBudget.THEMED_SPARKLES)
        assertEquals(6, StarParticleBudget.MICRO_LIGHTS)
        assertEquals(12, StarParticleBudget.TOTAL)
    }

    private fun StarBounds.overlaps(other: StarBounds): Boolean =
        left < other.right && right > other.left && top < other.bottom && bottom > other.top
}
