package com.littleblueworld.alia.navigation

import java.util.ArrayDeque

class AppNavigator(
    startDestination: ScreenId = ScreenId.BOOT,
) {
    private val history = ArrayDeque<ScreenId>()

    var current: ScreenId = startDestination
        private set

    val canGoBack: Boolean
        get() = history.isNotEmpty()

    val backStackDepth: Int
        get() = history.size

    fun push(destination: ScreenId): NavigationChange? {
        if (destination == current) return null

        val previous = current
        history.addLast(previous)
        current = destination
        return NavigationChange(previous, destination, NavigationDirection.FORWARD)
    }

    fun replace(destination: ScreenId): NavigationChange? {
        if (destination == current) return null

        val previous = current
        current = destination
        return NavigationChange(previous, destination, NavigationDirection.REPLACE)
    }

    fun resetTo(destination: ScreenId): NavigationChange? {
        val previous = current
        history.clear()
        if (destination == previous) return null

        current = destination
        return NavigationChange(previous, destination, NavigationDirection.REPLACE)
    }

    fun back(): NavigationChange? {
        if (history.isEmpty()) return null

        val previous = current
        val destination = history.removeLast()
        current = destination
        return NavigationChange(previous, destination, NavigationDirection.BACK)
    }

    internal fun historySnapshot(): List<ScreenId> = history.toList()
}
