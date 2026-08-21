package com.littleblueworld.alia.messages

import com.littleblueworld.alia.content.BirthdayContent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MessageBottleModelsTest {
    @Test
    fun `four spatial placements stay inside a narrow viewport`() {
        val bounds = MessageBottleLayoutSpec.placements.map { placement ->
            MessageBottleLayoutSpec.boundsFor(
                placement = placement,
                parentWidth = 320,
                parentHeight = 504,
                childWidth = 104,
                childHeight = 144,
                topInset = 64,
                bottomInset = 16,
            )
        }

        assertEquals(4, bounds.size)
        bounds.forEach {
            assertTrue(it.left >= 0)
            assertTrue(it.top >= 64)
            assertTrue(it.right <= 320)
            assertTrue(it.bottom <= 488)
        }
        bounds.forEachIndexed { index, current ->
            bounds.drop(index + 1).forEach { other ->
                assertTrue(!current.overlaps(other))
            }
        }
    }

    @Test
    fun `first paragraph becomes the staged reveal beat`() {
        val parts = BottleMessageParts.from(
            "another year unlocked.\n\nhopefully this one comes with\nmore good days,",
        )

        assertEquals("another year unlocked.", parts.firstBeat)
        assertEquals("hopefully this one comes with\nmore good days,", parts.remainder)
    }

    @Test
    fun `message without paragraph break stays complete`() {
        assertEquals(
            BottleMessageParts("one complete message", ""),
            BottleMessageParts.from("one complete message"),
        )
    }

    @Test
    fun `all four frozen messages survive staged splitting exactly`() {
        val messages = BirthdayContent().bottleMessages

        assertEquals(4, messages.size)
        messages.forEach { original ->
            val parts = BottleMessageParts.from(original)
            val reconstructed = if (parts.remainder.isEmpty()) {
                parts.firstBeat
            } else {
                "${parts.firstBeat}\n\n${parts.remainder}"
            }
            assertEquals(original, reconstructed)
        }
    }

    private fun BottleBounds.overlaps(other: BottleBounds): Boolean =
        left < other.right && right > other.left && top < other.bottom && bottom > other.top
}
