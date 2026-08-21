package com.littleblueworld.alia.state

enum class MainDiscovery {
    GALLERY,
    MESSAGES,
    STARS,
    CAFE,
}

enum class WishState {
    NONE,
    DRAFT,
    SEALED,
    KEPT_LOCAL,
    PENDING_SEND,
    SENT,
}

enum class ExperiencePhase {
    FIRST_VISIT,
    RETURNING_PRE_FINALE,
    POST_FINALE,
}

data class AppState(
    val hasOpenedBefore: Boolean = false,
    val worldHintSeen: Boolean = false,
    val galleryVisited: Boolean = false,
    val messagesVisited: Boolean = false,
    val starsVisited: Boolean = false,
    val cafeVisited: Boolean = false,
    val shellFound: Boolean = false,
    val fishMilestone: Int = 0,
    val wishUnlocked: Boolean = false,
    val finaleCompleted: Boolean = false,
    val wishState: WishState = WishState.NONE,
    val wishDraft: String = "",
    val pendingWishRequestId: String? = null,
    val pendingWishMessage: String? = null,
    val soundEnabled: Boolean = true,
) {
    val mainDiscoveryCount: Int
        get() = listOf(
            galleryVisited,
            messagesVisited,
            starsVisited,
            cafeVisited,
        ).count { it }

    val experiencePhase: ExperiencePhase
        get() = when {
            finaleCompleted -> ExperiencePhase.POST_FINALE
            hasOpenedBefore -> ExperiencePhase.RETURNING_PRE_FINALE
            else -> ExperiencePhase.FIRST_VISIT
        }

    fun hasVisited(discovery: MainDiscovery): Boolean = when (discovery) {
        MainDiscovery.GALLERY -> galleryVisited
        MainDiscovery.MESSAGES -> messagesVisited
        MainDiscovery.STARS -> starsVisited
        MainDiscovery.CAFE -> cafeVisited
    }
}
