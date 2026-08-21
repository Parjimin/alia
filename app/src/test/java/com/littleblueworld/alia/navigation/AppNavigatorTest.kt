package com.littleblueworld.alia.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppNavigatorTest {

    @Test
    fun pushThenBackReturnsToExactPreviousScreen() {
        val navigator = AppNavigator(ScreenId.WORLD)

        val forward = navigator.push(ScreenId.GALLERY)
        val backward = navigator.back()

        assertEquals(
            NavigationChange(ScreenId.WORLD, ScreenId.GALLERY, NavigationDirection.FORWARD),
            forward,
        )
        assertEquals(
            NavigationChange(ScreenId.GALLERY, ScreenId.WORLD, NavigationDirection.BACK),
            backward,
        )
        assertEquals(ScreenId.WORLD, navigator.current)
        assertFalse(navigator.canGoBack)
    }

    @Test
    fun resetToWorldClearsBirthdayAndBootHistory() {
        val navigator = AppNavigator(ScreenId.BOOT)
        navigator.push(ScreenId.BIRTHDAY_INTRO)
        navigator.push(ScreenId.WORLD)

        navigator.resetTo(ScreenId.WORLD)

        assertEquals(ScreenId.WORLD, navigator.current)
        assertEquals(0, navigator.backStackDepth)
        assertNull(navigator.back())
    }

    @Test
    fun replaceDoesNotCreateBackEntry() {
        val navigator = AppNavigator(ScreenId.BOOT)

        val change = navigator.replace(ScreenId.BIRTHDAY_INTRO)

        assertEquals(NavigationDirection.REPLACE, change?.direction)
        assertFalse(navigator.canGoBack)
    }

    @Test
    fun duplicateDestinationIsIgnoredWithoutMutatingHistory() {
        val navigator = AppNavigator(ScreenId.WORLD)

        val change = navigator.push(ScreenId.WORLD)

        assertNull(change)
        assertTrue(navigator.historySnapshot().isEmpty())
    }
}
