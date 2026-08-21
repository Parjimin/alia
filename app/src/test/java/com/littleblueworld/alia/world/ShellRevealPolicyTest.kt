package com.littleblueworld.alia.world

import com.littleblueworld.alia.content.BirthdayContent
import org.junit.Assert.assertEquals
import org.junit.Test

class ShellRevealPolicyTest {
    @Test
    fun `first shell reveal uses exact message and optional final joke`() {
        val content = BirthdayContent()

        assertEquals(
            content.shellMessage + content.shellOptionalJoke,
            ShellRevealPolicy.message(
                alreadyFound = false,
                firstReveal = content.shellMessage,
                optionalJoke = content.shellOptionalJoke,
            ),
        )
    }

    @Test
    fun `persisted shell reopens with only the approved joke`() {
        val content = BirthdayContent()

        assertEquals(
            listOf(content.shellOptionalJoke),
            ShellRevealPolicy.message(
                alreadyFound = true,
                firstReveal = content.shellMessage,
                optionalJoke = content.shellOptionalJoke,
            ),
        )
    }
}
