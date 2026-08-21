package com.littleblueworld.alia.world

import com.littleblueworld.alia.state.AppState

enum class WorldMode {
    DAY,
    POST_FINALE,
}

enum class WishPointMode {
    LOCKED,
    UNLOCKED,
    COMPLETED,
}

data class WorldProgression(
    val mode: WorldMode,
    val wishPointMode: WishPointMode,
    val galleryVisited: Boolean,
    val messagesVisited: Boolean,
    val starsVisited: Boolean,
    val cafeVisited: Boolean,
) {
    companion object {
        fun from(state: AppState): WorldProgression = WorldProgression(
            mode = if (state.finaleCompleted) WorldMode.POST_FINALE else WorldMode.DAY,
            wishPointMode = when {
                state.finaleCompleted -> WishPointMode.COMPLETED
                state.wishUnlocked -> WishPointMode.UNLOCKED
                else -> WishPointMode.LOCKED
            },
            galleryVisited = state.galleryVisited,
            messagesVisited = state.messagesVisited,
            starsVisited = state.starsVisited,
            cafeVisited = state.cafeVisited,
        )

        fun shouldCelebrateWishUnlock(previous: AppState, current: AppState): Boolean =
            !previous.wishUnlocked && current.wishUnlocked
    }
}
