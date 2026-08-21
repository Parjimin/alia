package com.littleblueworld.alia.navigation

enum class ScreenId {
    BOOT,
    BIRTHDAY_INTRO,
    WORLD,
    GALLERY,
    MESSAGES,
    STARS,
    CAFE,
    WISH,
    FINAL_MESSAGE,
    AUTHOR,
}

enum class NavigationDirection {
    FORWARD,
    BACK,
    REPLACE,
}

data class NavigationChange(
    val from: ScreenId,
    val to: ScreenId,
    val direction: NavigationDirection,
)
