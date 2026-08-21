package com.littleblueworld.alia.world

import com.littleblueworld.alia.state.AppState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WorldProgressionTest {
    @Test
    fun wishPointReflectsLockedUnlockedAndCompletedStates() {
        assertEquals(
            WishPointMode.LOCKED,
            WorldProgression.from(AppState()).wishPointMode,
        )
        assertEquals(
            WishPointMode.UNLOCKED,
            WorldProgression.from(AppState(wishUnlocked = true)).wishPointMode,
        )
        assertEquals(
            WishPointMode.COMPLETED,
            WorldProgression.from(
                AppState(wishUnlocked = true, finaleCompleted = true),
            ).wishPointMode,
        )
    }

    @Test
    fun finaleSelectsPostFinaleWorldFoundation() {
        assertEquals(WorldMode.DAY, WorldProgression.from(AppState()).mode)
        assertEquals(
            WorldMode.POST_FINALE,
            WorldProgression.from(AppState(finaleCompleted = true)).mode,
        )
    }

    @Test
    fun unlockCelebrationOnlyFiresOnLockedToUnlockedEdge() {
        assertTrue(
            WorldProgression.shouldCelebrateWishUnlock(
                previous = AppState(wishUnlocked = false),
                current = AppState(wishUnlocked = true),
            ),
        )
        assertFalse(
            WorldProgression.shouldCelebrateWishUnlock(
                previous = AppState(wishUnlocked = true),
                current = AppState(wishUnlocked = true),
            ),
        )
        assertFalse(
            WorldProgression.shouldCelebrateWishUnlock(
                previous = AppState(wishUnlocked = false),
                current = AppState(wishUnlocked = false),
            ),
        )
    }

    @Test
    fun visitedMarkersMirrorOnlyMainDiscoveryState() {
        val progression = WorldProgression.from(
            AppState(
                galleryVisited = true,
                cafeVisited = true,
                shellFound = true,
                fishMilestone = 5,
            ),
        )

        assertTrue(progression.galleryVisited)
        assertFalse(progression.messagesVisited)
        assertFalse(progression.starsVisited)
        assertTrue(progression.cafeVisited)
    }
}
