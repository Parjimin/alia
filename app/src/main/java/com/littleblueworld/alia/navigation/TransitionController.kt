package com.littleblueworld.alia.navigation

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import com.littleblueworld.alia.state.AppState

class TransitionController(
    private val screenHost: ViewGroup,
) {
    private var runningAnimator: AnimatorSet? = null
    private var incomingScreen: AppScreen? = null
    private var isForeground = false

    var currentScreen: AppScreen? = null
        private set

    val isRunning: Boolean
        get() = runningAnimator?.isRunning == true

    fun setInitial(screen: AppScreen) {
        cancelRunningTransition()
        if (isForeground) currentScreen?.onBackgrounded()
        currentScreen?.onHidden()
        screenHost.removeAllViews()
        screenHost.addView(screen.view)
        currentScreen = screen
        screen.onShown()
        if (isForeground) screen.onForegrounded()
    }

    fun render(state: AppState) {
        currentScreen?.render(state)
        incomingScreen?.render(state)
    }

    fun transitionTo(
        incoming: AppScreen,
        direction: NavigationDirection,
        onComplete: () -> Unit = {},
    ): Boolean {
        if (isRunning) return false

        val outgoing = currentScreen
        if (outgoing == null) {
            setInitial(incoming)
            onComplete()
            return true
        }

        val distance = screenHost.resources.displayMetrics.density * TRANSLATION_DP
        val directionSign = if (direction == NavigationDirection.BACK) -1f else 1f

        incoming.view.apply {
            alpha = 0f
            translationX = distance * directionSign
        }
        incomingScreen = incoming
        screenHost.addView(incoming.view)

        val outgoingAnimator = ObjectAnimator.ofPropertyValuesHolder(
            outgoing.view,
            PropertyValuesHolder.ofFloat(View.ALPHA, outgoing.view.alpha, 0f),
            PropertyValuesHolder.ofFloat(
                View.TRANSLATION_X,
                outgoing.view.translationX,
                -distance * directionSign * OUTGOING_DISTANCE_FRACTION,
            ),
        )

        val incomingAnimator = ObjectAnimator.ofPropertyValuesHolder(
            incoming.view,
            PropertyValuesHolder.ofFloat(View.ALPHA, incoming.view.alpha, 1f),
            PropertyValuesHolder.ofFloat(View.TRANSLATION_X, incoming.view.translationX, 0f),
        )

        var settled = false
        val animator = AnimatorSet().apply {
            duration = DURATION_MS
            interpolator = AccelerateDecelerateInterpolator()
            playTogether(outgoingAnimator, incomingAnimator)
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (settled) return
                    settled = true
                    screenHost.removeView(outgoing.view)
                    outgoing.view.alpha = 1f
                    outgoing.view.translationX = 0f
                    if (isForeground) outgoing.onBackgrounded()
                    outgoing.onHidden()
                    currentScreen = incoming
                    incomingScreen = null
                    incoming.onShown()
                    if (isForeground) incoming.onForegrounded()
                    runningAnimator = null
                    onComplete()
                }
            })
        }

        runningAnimator = animator
        animator.start()
        return true
    }

    fun clear() {
        cancelRunningTransition()
        if (isForeground) currentScreen?.onBackgrounded()
        currentScreen?.onHidden()
        currentScreen = null
        screenHost.removeAllViews()
    }

    fun onForegrounded() {
        if (isForeground) return
        isForeground = true
        runningAnimator?.resume()
        currentScreen?.onForegrounded()
        incomingScreen?.onForegrounded()
    }

    fun onBackgrounded() {
        if (!isForeground) return
        currentScreen?.onBackgrounded()
        incomingScreen?.onBackgrounded()
        runningAnimator?.pause()
        isForeground = false
    }

    private fun cancelRunningTransition() {
        runningAnimator?.let { animator ->
            animator.removeAllListeners()
            animator.cancel()
        }
        runningAnimator = null
        incomingScreen = null
    }

    private companion object {
        const val DURATION_MS = 240L
        const val TRANSLATION_DP = 8f
        const val OUTGOING_DISTANCE_FRACTION = 0.5f
    }
}
