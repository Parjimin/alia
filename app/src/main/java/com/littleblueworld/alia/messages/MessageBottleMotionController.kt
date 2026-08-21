package com.littleblueworld.alia.messages

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator

class MessageBottleMotionController(
    private val bottles: List<View>,
) {
    private val animators = mutableListOf<Animator>()

    fun start() {
        stop()
        bottles.forEachIndexed { index, bottle ->
            val placement = MessageBottleLayoutSpec.placements[index]
            val travel = bottle.resources.displayMetrics.density * 3f
            val drift = ObjectAnimator.ofPropertyValuesHolder(
                bottle,
                PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, 0f, -travel, 0f, travel, 0f),
                PropertyValuesHolder.ofFloat(
                    View.ROTATION,
                    placement.baseRotation,
                    placement.baseRotation + ROTATION_AMPLITUDE,
                    placement.baseRotation,
                    placement.baseRotation - ROTATION_AMPLITUDE,
                    placement.baseRotation,
                ),
            ).apply {
                duration = BASE_DURATION_MS + index * DURATION_STEP_MS
                startDelay = index * START_DELAY_STEP_MS
                repeatCount = ObjectAnimator.INFINITE
                interpolator = AccelerateDecelerateInterpolator()
            }
            animators += drift
            drift.start()
        }
    }

    fun stop() {
        animators.forEach(Animator::cancel)
        animators.clear()
        bottles.forEachIndexed { index, bottle ->
            bottle.animate().cancel()
            bottle.translationY = 0f
            bottle.rotation = MessageBottleLayoutSpec.placements[index].baseRotation
            bottle.scaleX = 1f
            bottle.scaleY = 1f
            bottle.alpha = 1f
        }
    }

    private companion object {
        const val ROTATION_AMPLITUDE = 1.3f
        const val BASE_DURATION_MS = 3_050L
        const val DURATION_STEP_MS = 190L
        const val START_DELAY_STEP_MS = 170L
    }
}
