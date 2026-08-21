package com.littleblueworld.alia.stars

import kotlin.math.roundToInt

enum class StarKind {
    ENERGY,
    MIND,
    FACE,
}

data class StarCollectResult(
    val accepted: Boolean,
    val collectedCount: Int,
    val completed: Boolean,
)

class StarCollectionTracker {
    private val collected = mutableSetOf<StarKind>()

    val collectedCount: Int
        get() = collected.size

    val completed: Boolean
        get() = collected.size == StarKind.entries.size

    fun isCollected(kind: StarKind): Boolean = kind in collected

    fun collect(kind: StarKind): StarCollectResult {
        val accepted = collected.add(kind)
        return StarCollectResult(
            accepted = accepted,
            collectedCount = collected.size,
            completed = completed,
        )
    }
}

data class FaceRevealStep(
    val text: String,
    val delayAfterPreviousMs: Long,
)

object FaceRevealTimeline {
    fun from(lines: List<String>): List<FaceRevealStep> {
        require(lines.size == 5)
        return listOf(
            FaceRevealStep(lines[0], 0L),
            FaceRevealStep(lines[1], 800L),
            FaceRevealStep(lines[2], 800L),
            FaceRevealStep(lines[3], 300L),
            FaceRevealStep(lines[4], 500L),
        )
    }
}

data class StarPlacement(
    val centerXFraction: Float,
    val centerYFraction: Float,
)

data class StarBounds(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
)

object StarFieldLayoutSpec {
    val placements = listOf(
        StarPlacement(0.23f, 0.20f),
        StarPlacement(0.76f, 0.25f),
        StarPlacement(0.51f, 0.58f),
    )

    fun boundsFor(
        placement: StarPlacement,
        parentWidth: Int,
        parentHeight: Int,
        childWidth: Int,
        childHeight: Int,
        topInset: Int,
        bottomInset: Int,
    ): StarBounds {
        val usableHeight = (parentHeight - topInset - bottomInset).coerceAtLeast(childHeight)
        val desiredLeft = (parentWidth * placement.centerXFraction - childWidth / 2f).roundToInt()
        val desiredTop = (
            topInset + usableHeight * placement.centerYFraction - childHeight / 2f
            ).roundToInt()
        val left = desiredLeft.coerceIn(0, (parentWidth - childWidth).coerceAtLeast(0))
        val maximumTop = (parentHeight - bottomInset - childHeight).coerceAtLeast(0)
        val minimumTop = topInset.coerceIn(0, maximumTop)
        val top = desiredTop.coerceIn(minimumTop, maximumTop)
        return StarBounds(left, top, left + childWidth, top + childHeight)
    }
}

object StarParticleBudget {
    const val THEMED_SPARKLES = 6
    const val MICRO_LIGHTS = 6
    const val TOTAL = THEMED_SPARKLES + MICRO_LIGHTS
}
