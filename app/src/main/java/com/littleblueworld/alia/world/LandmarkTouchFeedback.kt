package com.littleblueworld.alia.world

import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator

data class LandmarkFeedbackProfile(
    val pressedScale: Float,
    val pressedLiftDp: Float = 0f,
)

fun View.installLandmarkTouchFeedback(profile: LandmarkFeedbackProfile) {
    val liftPx = profile.pressedLiftDp * resources.displayMetrics.density
    setOnTouchListener { view, event ->
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> view.animate()
                .scaleX(profile.pressedScale)
                .scaleY(profile.pressedScale)
                .translationY(-liftPx)
                .setDuration(PRESS_DURATION_MS)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()

            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_CANCEL,
            -> {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .translationY(0f)
                    .setDuration(RELEASE_DURATION_MS)
                    .setInterpolator(AccelerateDecelerateInterpolator())
                    .start()
                if (event.actionMasked == MotionEvent.ACTION_UP) view.performClick()
            }
        }
        true
    }
}

private const val PRESS_DURATION_MS = 90L
private const val RELEASE_DURATION_MS = 160L
