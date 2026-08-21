package com.littleblueworld.alia.boot

import com.littleblueworld.alia.content.BirthdayContent
import com.littleblueworld.alia.state.ExperiencePhase
import kotlin.random.Random

enum class BootPhase {
    PIXELS,
    OCEAN,
    COLOR,
    PERSONAL,
    BIRTHDAY,
    REVISIT,
    READY,
}

data class BootLine(
    val phase: BootPhase,
    val text: String,
    val displayMillis: Long,
    val gapAfterMillis: Long = NORMAL_GAP_MS,
    val triggersSparkleBurst: Boolean = false,
) {
    companion object {
        const val NORMAL_GAP_MS = 90L
    }
}

/** Builds one coherent loading story while keeping every recipient-facing line frozen. */
class BootSequenceGenerator(
    private val content: BirthdayContent,
    private val random: Random = Random.Default,
) {
    fun generate(experiencePhase: ExperiencePhase): List<BootLine> = when (experiencePhase) {
        ExperiencePhase.FIRST_VISIT -> firstVisit()
        ExperiencePhase.RETURNING_PRE_FINALE -> revisit(
            pool = content.revisitPreFinale,
            groupedMiddle = listOf(
                content.revisitPreFinale[4],
                content.revisitPreFinale[5],
            ),
        )

        ExperiencePhase.POST_FINALE -> revisit(
            pool = content.revisitPostFinale,
            groupedMiddle = listOf(
                content.revisitPostFinale[4],
                content.revisitPostFinale[5],
            ),
        )
    }

    private fun firstVisit(): List<BootLine> = buildList {
        addRandomPhase(BootPhase.PIXELS, content.loadingPixels)
        addOceanPhase()
        addRandomPhase(BootPhase.COLOR, content.loadingColor)
        addPersonalPhase()
        addRandomPhase(BootPhase.BIRTHDAY, content.loadingBirthday)
        add(
            BootLine(
                phase = BootPhase.READY,
                text = content.firstRunFinalLine,
                displayMillis = READY_HOLD_MS,
                gapAfterMillis = 0L,
            ),
        )
    }

    private fun MutableList<BootLine>.addOceanPhase() {
        if (random.nextInt(GROUPED_SEQUENCE_CHANCE) == 0) {
            content.loadingOceanGrouped.forEach { text ->
                add(normalLine(BootPhase.OCEAN, text))
            }
        } else {
            addRandomPhase(BootPhase.OCEAN, content.loadingOcean)
        }
    }

    private fun MutableList<BootLine>.addPersonalPhase() {
        if (random.nextInt(GROUPED_SEQUENCE_CHANCE) == 0) {
            add(
                BootLine(
                    phase = BootPhase.PERSONAL,
                    text = content.loadingSignatureStart,
                    displayMillis = SIGNATURE_HOLD_MS,
                    gapAfterMillis = SIGNATURE_GAP_MS,
                ),
            )
            add(
                BootLine(
                    phase = BootPhase.PERSONAL,
                    text = content.loadingSignatureResult,
                    displayMillis = SIGNATURE_HOLD_MS,
                    triggersSparkleBurst = true,
                ),
            )
        } else {
            addRandomPhase(
                phase = BootPhase.PERSONAL,
                pool = content.loadingPersonal.filterNot { it == content.loadingSignatureStart },
            )
        }
    }

    private fun MutableList<BootLine>.addRandomPhase(
        phase: BootPhase,
        pool: List<String>,
    ) {
        val lineCount = if (random.nextBoolean()) 1 else 2
        pool.shuffled(random).take(lineCount).forEach { text ->
            add(normalLine(phase, text))
        }
    }

    private fun revisit(
        pool: List<String>,
        groupedMiddle: List<String>,
    ): List<BootLine> {
        val first = pool.first()
        val last = pool.last()
        val singleMiddle = pool.subList(1, pool.lastIndex)
            .filterNot(groupedMiddle::contains)
        val middle = if (random.nextInt(GROUPED_SEQUENCE_CHANCE) == 0) {
            groupedMiddle
        } else {
            listOf(singleMiddle.random(random))
        }

        return buildList {
            add(revisitLine(first))
            middle.forEach { add(revisitLine(it)) }
            add(revisitLine(last, gapAfterMillis = 0L))
        }
    }

    private fun normalLine(
        phase: BootPhase,
        text: String,
    ) = BootLine(
        phase = phase,
        text = text,
        displayMillis = NORMAL_HOLD_MS,
    )

    private fun revisitLine(
        text: String,
        gapAfterMillis: Long = BootLine.NORMAL_GAP_MS,
    ) = BootLine(
        phase = BootPhase.REVISIT,
        text = text,
        displayMillis = REVISIT_HOLD_MS,
        gapAfterMillis = gapAfterMillis,
    )

    private companion object {
        const val GROUPED_SEQUENCE_CHANCE = 4
        const val NORMAL_HOLD_MS = 620L
        const val REVISIT_HOLD_MS = 500L
        const val SIGNATURE_HOLD_MS = 1_050L
        const val SIGNATURE_GAP_MS = 200L
        const val READY_HOLD_MS = 760L
    }
}
