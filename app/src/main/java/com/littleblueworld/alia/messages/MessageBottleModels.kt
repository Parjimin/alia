package com.littleblueworld.alia.messages

import kotlin.math.roundToInt

data class BottlePlacement(
    val centerXFraction: Float,
    val centerYFraction: Float,
    val baseRotation: Float,
)

data class BottleBounds(
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int,
)

object MessageBottleLayoutSpec {
    val placements = listOf(
        BottlePlacement(0.24f, 0.24f, -3.5f),
        BottlePlacement(0.73f, 0.31f, 2.8f),
        BottlePlacement(0.28f, 0.68f, 2.2f),
        BottlePlacement(0.75f, 0.74f, -2.9f),
    )

    fun boundsFor(
        placement: BottlePlacement,
        parentWidth: Int,
        parentHeight: Int,
        childWidth: Int,
        childHeight: Int,
        topInset: Int,
        bottomInset: Int,
    ): BottleBounds {
        val usableHeight = (parentHeight - topInset - bottomInset).coerceAtLeast(childHeight)
        val desiredLeft = (parentWidth * placement.centerXFraction - childWidth / 2f).roundToInt()
        val desiredTop = (
            topInset + usableHeight * placement.centerYFraction - childHeight / 2f
            ).roundToInt()
        val left = desiredLeft.coerceIn(0, (parentWidth - childWidth).coerceAtLeast(0))
        val maximumTop = (parentHeight - bottomInset - childHeight).coerceAtLeast(0)
        val minimumTop = topInset.coerceIn(0, maximumTop)
        val top = desiredTop.coerceIn(minimumTop, maximumTop)
        return BottleBounds(left, top, left + childWidth, top + childHeight)
    }
}

data class BottleMessageParts(
    val firstBeat: String,
    val remainder: String,
) {
    companion object {
        fun from(message: String): BottleMessageParts {
            val separator = message.indexOf("\n\n")
            return if (separator < 0) {
                BottleMessageParts(message, "")
            } else {
                BottleMessageParts(
                    firstBeat = message.substring(0, separator),
                    remainder = message.substring(separator + 2),
                )
            }
        }
    }
}
