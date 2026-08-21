package com.littleblueworld.alia.world

import kotlin.math.abs
import kotlin.math.roundToLong

data class FishDialogueResult(
    val message: String,
    val persistedMilestone: Int?,
    val escapeAfterMessage: Boolean,
)

class FishDialogueState(
    initialMilestone: Int,
    private val firstSequence: List<String>,
    private val postFinaleSequence: List<String>,
    private val genericLater: List<String>,
    postFinale: Boolean,
) {
    private var milestone = initialMilestone.coerceIn(0, FIRST_SEQUENCE_LENGTH)
    private var postFinaleMode = postFinale
    private var postFinaleIndex = 0
    private var genericIndex = 0

    init {
        require(firstSequence.size == FIRST_SEQUENCE_LENGTH)
        require(postFinaleSequence.isNotEmpty())
        require(genericLater.isNotEmpty())
    }

    fun update(
        persistedMilestone: Int,
        postFinale: Boolean,
    ) {
        milestone = maxOf(milestone, persistedMilestone.coerceIn(0, FIRST_SEQUENCE_LENGTH))
        if (postFinale && !postFinaleMode) postFinaleIndex = 0
        postFinaleMode = postFinale
    }

    fun tap(): FishDialogueResult {
        if (postFinaleMode) {
            val message = if (postFinaleIndex < postFinaleSequence.size) {
                postFinaleSequence[postFinaleIndex++]
            } else {
                nextGeneric()
            }
            return FishDialogueResult(message, persistedMilestone = null, escapeAfterMessage = false)
        }

        if (milestone < FIRST_SEQUENCE_LENGTH) {
            milestone += 1
            return FishDialogueResult(
                message = firstSequence[milestone - 1],
                persistedMilestone = milestone,
                escapeAfterMessage = milestone == FIRST_SEQUENCE_LENGTH,
            )
        }

        return FishDialogueResult(
            message = nextGeneric(),
            persistedMilestone = null,
            escapeAfterMessage = false,
        )
    }

    private fun nextGeneric(): String = genericLater[genericIndex++ % genericLater.size]

    private companion object {
        const val FIRST_SEQUENCE_LENGTH = 5
    }
}

data class FishSpawnConfig(
    val delayMs: Long,
    val speedDpPerSecond: Float,
    val centerYFraction: Float,
    val wobbleAmplitudeDp: Float,
    val wobblePeriodMs: Long,
    val leftToRight: Boolean,
)

object FishMotionPlan {
    private val delaysMs = longArrayOf(8_600L, 12_800L, 17_200L, 10_400L, 15_600L)
    private val speedsDpPerSecond = floatArrayOf(26f, 34f, 42f, 30f, 38f)
    private val centerYFractions = floatArrayOf(0.40f, 0.58f, 0.66f, 0.47f, 0.61f)
    private val wobbleAmplitudesDp = floatArrayOf(3f, 4f, 2.5f, 3.5f, 3f)
    private val wobblePeriodsMs = longArrayOf(1_800L, 1_550L, 2_050L, 1_700L, 1_900L)

    fun forCycle(cycle: Int): FishSpawnConfig {
        val index = cycle.mod(delaysMs.size)
        return FishSpawnConfig(
            delayMs = delaysMs[index],
            speedDpPerSecond = speedsDpPerSecond[index],
            centerYFraction = centerYFractions[index],
            wobbleAmplitudeDp = wobbleAmplitudesDp[index],
            wobblePeriodMs = wobblePeriodsMs[index],
            leftToRight = index % 2 == 1,
        )
    }

    fun durationMs(
        startXDp: Float,
        endXDp: Float,
        speedDpPerSecond: Float,
    ): Long {
        require(speedDpPerSecond > 0f)
        return (abs(endXDp - startXDp) / speedDpPerSecond * 1_000f).roundToLong()
    }

    fun escapeSpeed(normalSpeedDpPerSecond: Float): Float = normalSpeedDpPerSecond * 2f
}
