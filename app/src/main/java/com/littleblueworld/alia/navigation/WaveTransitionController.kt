package com.littleblueworld.alia.navigation

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import com.littleblueworld.alia.databinding.TransitionWaveBinding

class WaveTransitionController(
    private val overlayHost: ViewGroup,
) {
    private var runningAnimator: ObjectAnimator? = null
    private var activeBinding: TransitionWaveBinding? = null
    private var isForeground = false

    val isRunning: Boolean
        get() = activeBinding != null

    /** Covers the old scene and reveals the replacement only after [onCovered] calls its callback. */
    fun coverThenReveal(
        onCovered: (reveal: () -> Unit) -> Unit,
    ): Boolean {
        if (isRunning || overlayHost.height <= 0) return false

        val binding = TransitionWaveBinding.inflate(LayoutInflater.from(overlayHost.context))
        activeBinding = binding
        overlayHost.addView(binding.root)
        binding.root.translationY = overlayHost.height.toFloat()

        animateTo(
            targetY = 0f,
            durationMillis = COVER_DURATION_MS,
        ) {
            onCovered(::reveal)
        }
        return true
    }

    fun onForegrounded() {
        if (isForeground) return
        isForeground = true
        runningAnimator?.resume()
    }

    fun onBackgrounded() {
        if (!isForeground) return
        runningAnimator?.pause()
        isForeground = false
    }

    fun clear() {
        runningAnimator?.removeAllListeners()
        runningAnimator?.cancel()
        runningAnimator = null
        activeBinding?.root?.let(overlayHost::removeView)
        activeBinding = null
    }

    private fun reveal() {
        if (activeBinding == null) return
        animateTo(
            targetY = overlayHost.height.toFloat(),
            durationMillis = REVEAL_DURATION_MS,
            onEnd = ::clear,
        )
    }

    private fun animateTo(
        targetY: Float,
        durationMillis: Long,
        onEnd: () -> Unit,
    ) {
        val root = activeBinding?.root ?: return
        var ended = false
        runningAnimator = ObjectAnimator.ofFloat(
            root,
            "translationY",
            root.translationY,
            targetY,
        ).apply {
            duration = durationMillis
            interpolator = AccelerateDecelerateInterpolator()
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (ended) return
                    ended = true
                    runningAnimator = null
                    onEnd()
                }
            })
            start()
            if (!isForeground) pause()
        }
    }

    private companion object {
        const val COVER_DURATION_MS = 900L
        const val REVEAL_DURATION_MS = 900L
    }
}
