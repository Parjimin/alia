package com.littleblueworld.alia.cafe

enum class BeverageKind {
    TEA,
    COFFEE,
}

data class BeverageTapResult(
    val response: String,
    val tapCount: Int,
    val firstMeaningfulInteraction: Boolean,
)

class CafeTapTracker(
    private val teaResponses: List<String>,
    private val coffeeResponses: List<String>,
) {
    private val tapCounts = IntArray(BeverageKind.entries.size)
    private var hasInteracted = false

    init {
        require(teaResponses.size >= MINIMUM_RESPONSES)
        require(coffeeResponses.size >= MINIMUM_RESPONSES)
    }

    fun tap(kind: BeverageKind): BeverageTapResult {
        val index = kind.ordinal
        tapCounts[index] += 1
        val firstInteraction = !hasInteracted
        hasInteracted = true
        val responses = when (kind) {
            BeverageKind.TEA -> teaResponses
            BeverageKind.COFFEE -> coffeeResponses
        }
        return BeverageTapResult(
            response = responses[responseIndex(tapCounts[index], responses.size)],
            tapCount = tapCounts[index],
            firstMeaningfulInteraction = firstInteraction,
        )
    }

    companion object {
        private const val MINIMUM_RESPONSES = 3

        fun responseIndex(tapCount: Int, responseCount: Int): Int {
            require(tapCount >= 1)
            require(responseCount >= MINIMUM_RESPONSES)
            if (tapCount <= 2) return tapCount - 1
            return 2 + (tapCount - 3) % (responseCount - 2)
        }
    }
}
