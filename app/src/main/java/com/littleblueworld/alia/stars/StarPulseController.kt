package com.littleblueworld.alia.stars

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator

class StarPulseController(
    private val stars: List<View>,
) {
    private val animators = mutableListOf<Animator>()

    fun start(activeKinds: Set<StarKind>) {
        stop()
        StarKind.entries.forEachIndexed { index, kind ->
            if (kind !in activeKinds) return@forEachIndexed
            val star = stars[index]
            star.scaleX = 1f
            star.scaleY = 1f
            val pulse = ObjectAnimator.ofPropertyValuesHolder(
                star,
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.045f, 1f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.045f, 1f),
            ).apply {
                duration = BASE_DURATION_MS + index * DURATION_STEP_MS
                startDelay = index * START_DELAY_STEP_MS
                repeatCount = ObjectAnimator.INFINITE
                interpolator = AccelerateDecelerateInterpolator()
            }
            animators += pulse
            pulse.start()
        }
    }

    fun stop() {
        animators.forEach(Animator::cancel)
        animators.clear()
    }

    private companion object {
        const val BASE_DURATION_MS = 2_250L
        const val DURATION_STEP_MS = 260L
        const val START_DELAY_STEP_MS = 190L
    }
}
