package com.littleblueworld.alia.state

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppStateTest {

    @Test
    fun onlyMainDiscoveriesContributeToCount() {
        val state = AppState(
            galleryVisited = true,
            messagesVisited = true,
            shellFound = true,
            fishMilestone = 5,
        )

        assertEquals(2, state.mainDiscoveryCount)
        assertTrue(state.hasVisited(MainDiscovery.GALLERY))
        assertFalse(state.hasVisited(MainDiscovery.CAFE))
    }

    @Test
    fun experiencePhaseUsesFinaleBeforeOpenedFlag() {
        assertEquals(ExperiencePhase.FIRST_VISIT, AppState().experiencePhase)
        assertEquals(
            ExperiencePhase.RETURNING_PRE_FINALE,
            AppState(hasOpenedBefore = true).experiencePhase,
        )
        assertEquals(
            ExperiencePhase.POST_FINALE,
            AppState(finaleCompleted = true).experiencePhase,
        )
    }
}
