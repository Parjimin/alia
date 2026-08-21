package com.littleblueworld.alia.boot

import com.littleblueworld.alia.navigation.ScreenId
import com.littleblueworld.alia.state.ExperiencePhase

object BootRoute {
    fun destinationFor(experiencePhase: ExperiencePhase): ScreenId = when (experiencePhase) {
        ExperiencePhase.FIRST_VISIT -> ScreenId.BIRTHDAY_INTRO
        ExperiencePhase.RETURNING_PRE_FINALE,
        ExperiencePhase.POST_FINALE,
        -> ScreenId.WORLD
    }
}
