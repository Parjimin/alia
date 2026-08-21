package com.littleblueworld.alia.boot

import com.littleblueworld.alia.content.BirthdayContent
import com.littleblueworld.alia.navigation.ScreenId
import com.littleblueworld.alia.state.ExperiencePhase
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BootSequenceTest {
    private val content = BirthdayContent()

    @Test
    fun firstVisitAlwaysKeepsPhaseOrderAndEndsReady() {
        repeat(200) { seed ->
            val sequence = generator(seed).generate(ExperiencePhase.FIRST_VISIT)
            val phaseOrder = sequence.map(BootLine::phase).distinct()

            assertEquals(
                listOf(
                    BootPhase.PIXELS,
                    BootPhase.OCEAN,
                    BootPhase.COLOR,
                    BootPhase.PERSONAL,
                    BootPhase.BIRTHDAY,
                    BootPhase.READY,
                ),
                phaseOrder,
            )
            assertEquals(content.firstRunFinalLine, sequence.last().text)
            assertEquals(1, sequence.count { it.phase == BootPhase.READY })
        }
    }

    @Test
    fun signatureResultIsAlwaysAdjacentAndTriggersSparkles() {
        var signatureSeen = false

        repeat(200) { seed ->
            val sequence = generator(seed).generate(ExperiencePhase.FIRST_VISIT)
            val startIndex = sequence.indexOfFirst { it.text == content.loadingSignatureStart }
            if (startIndex >= 0) {
                signatureSeen = true
                val result = sequence[startIndex + 1]
                assertEquals(content.loadingSignatureResult, result.text)
                assertEquals(BootPhase.PERSONAL, result.phase)
                assertTrue(result.triggersSparkleBurst)
            }
        }

        assertTrue("Seed coverage should exercise the signature sequence", signatureSeen)
    }

    @Test
    fun groupedOceanSequenceIsNeverSplitOrReordered() {
        var groupedSequenceSeen = false

        repeat(200) { seed ->
            val texts = generator(seed)
                .generate(ExperiencePhase.FIRST_VISIT)
                .map(BootLine::text)
            val startIndex = texts.indexOf(content.loadingOceanGrouped.first())
            if (startIndex >= 0) {
                groupedSequenceSeen = true
                assertEquals(
                    content.loadingOceanGrouped,
                    texts.subList(startIndex, startIndex + content.loadingOceanGrouped.size),
                )
            }
        }

        assertTrue("Seed coverage should exercise the grouped ocean sequence", groupedSequenceSeen)
    }

    @Test
    fun revisitVariantsAreShorterAndUseOnlyTheirApprovedPool() {
        val shortestFirstVisit = (0 until 200).minOf { seed ->
            generator(seed).generate(ExperiencePhase.FIRST_VISIT).size
        }

        listOf(
            ExperiencePhase.RETURNING_PRE_FINALE to content.revisitPreFinale,
            ExperiencePhase.POST_FINALE to content.revisitPostFinale,
        ).forEach { (phase, approvedPool) ->
            repeat(200) { seed ->
                val sequence = generator(seed).generate(phase)
                assertTrue(sequence.size < shortestFirstVisit)
                assertTrue(sequence.all { it.phase == BootPhase.REVISIT })
                assertTrue(sequence.all { it.text in approvedPool })
                assertEquals(approvedPool.first(), sequence.first().text)
                assertEquals(approvedPool.last(), sequence.last().text)
            }
        }
    }

    @Test
    fun bootRouteSkipsBirthdayEntranceOnEveryRevisit() {
        assertEquals(
            ScreenId.BIRTHDAY_INTRO,
            BootRoute.destinationFor(ExperiencePhase.FIRST_VISIT),
        )
        assertEquals(
            ScreenId.WORLD,
            BootRoute.destinationFor(ExperiencePhase.RETURNING_PRE_FINALE),
        )
        assertEquals(
            ScreenId.WORLD,
            BootRoute.destinationFor(ExperiencePhase.POST_FINALE),
        )
    }

    private fun generator(seed: Int) = BootSequenceGenerator(
        content = content,
        random = Random(seed),
    )
}
