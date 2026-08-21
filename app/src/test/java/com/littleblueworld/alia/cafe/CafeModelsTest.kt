package com.littleblueworld.alia.cafe

import com.littleblueworld.alia.content.BirthdayContent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CafeModelsTest {
    @Test
    fun `tea and coffee preserve the frozen first two responses`() {
        val content = BirthdayContent()
        val tracker = CafeTapTracker(content.teaResponses, content.coffeeResponses)

        assertEquals("good choice.", tracker.tap(BeverageKind.TEA).response)
        assertEquals("yes, it's imaginary.", tracker.tap(BeverageKind.TEA).response)
        assertEquals("also acceptable.", tracker.tap(BeverageKind.COFFEE).response)
        assertEquals("still counts.", tracker.tap(BeverageKind.COFFEE).response)
    }

    @Test
    fun `third and later taps alternate only the approved optional responses`() {
        val content = BirthdayContent()
        val tracker = CafeTapTracker(content.teaResponses, content.coffeeResponses)

        val responses = List(6) { tracker.tap(BeverageKind.TEA).response }

        assertEquals(
            listOf(
                "good choice.",
                "yes, it's imaginary.",
                "still good tea.",
                "you're committed to this.",
                "still good tea.",
                "you're committed to this.",
            ),
            responses,
        )
    }

    @Test
    fun `only the first beverage tap is the meaningful discovery interaction`() {
        val content = BirthdayContent()
        val tracker = CafeTapTracker(content.teaResponses, content.coffeeResponses)

        assertTrue(tracker.tap(BeverageKind.COFFEE).firstMeaningfulInteraction)
        assertFalse(tracker.tap(BeverageKind.TEA).firstMeaningfulInteraction)
        assertFalse(tracker.tap(BeverageKind.COFFEE).firstMeaningfulInteraction)
    }
}
