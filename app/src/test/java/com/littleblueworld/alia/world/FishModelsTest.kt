package com.littleblueworld.alia.world

import com.littleblueworld.alia.content.BirthdayContent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FishModelsTest {
    @Test
    fun `first five taps preserve exact copy and only fifth escapes`() {
        val content = BirthdayContent()
        val state = dialogueState(content, milestone = 0, postFinale = false)

        val results = List(5) { state.tap() }

        assertEquals(content.fishFirstSequence, results.map { it.message })
        assertEquals(listOf(1, 2, 3, 4, 5), results.map { it.persistedMilestone })
        assertEquals(listOf(false, false, false, false, true), results.map { it.escapeAfterMessage })
    }

    @Test
    fun `persisted fifth milestone uses the approved generic pool without another escape`() {
        val content = BirthdayContent()
        val state = dialogueState(content, milestone = 5, postFinale = false)

        val results = List(content.fishGenericLater.size + 1) { state.tap() }

        assertEquals(content.fishGenericLater + content.fishGenericLater.first(), results.map { it.message })
        assertTrue(results.all { it.persistedMilestone == null })
        assertTrue(results.none { it.escapeAfterMessage })
    }

    @Test
    fun `post finale sequence differs and never rewrites first sequence milestone`() {
        val content = BirthdayContent()
        val state = dialogueState(content, milestone = 2, postFinale = true)

        val results = List(content.fishPostFinale.size) { state.tap() }

        assertEquals(content.fishPostFinale, results.map { it.message })
        assertTrue(results.all { it.persistedMilestone == null })
        assertTrue(results.none { it.escapeAfterMessage })
    }

    @Test
    fun `spawn motion stays inside timing speed and wobble budgets`() {
        val configs = List(10, FishMotionPlan::forCycle)

        configs.forEach { config ->
            assertTrue(config.delayMs in 8_000L..20_000L)
            assertTrue(config.speedDpPerSecond in 20f..45f)
            assertTrue(config.wobbleAmplitudeDp in 2f..5f)
            assertTrue(config.wobblePeriodMs in 1_400L..2_200L)
        }
        assertEquals(60f, FishMotionPlan.escapeSpeed(30f))
        assertEquals(10_000L, FishMotionPlan.durationMs(-30f, 270f, 30f))
    }

    @Test
    fun `state update never regresses a persisted first sequence milestone`() {
        val content = BirthdayContent()
        val state = dialogueState(content, milestone = 4, postFinale = false)
        state.update(persistedMilestone = 2, postFinale = false)

        val fifth = state.tap()

        assertEquals(content.fishFirstSequence[4], fifth.message)
        assertEquals(5, fifth.persistedMilestone)
        assertTrue(fifth.escapeAfterMessage)
    }

    private fun dialogueState(
        content: BirthdayContent,
        milestone: Int,
        postFinale: Boolean,
    ) = FishDialogueState(
        initialMilestone = milestone,
        firstSequence = content.fishFirstSequence,
        postFinaleSequence = content.fishPostFinale,
        genericLater = content.fishGenericLater,
        postFinale = postFinale,
    )
}
