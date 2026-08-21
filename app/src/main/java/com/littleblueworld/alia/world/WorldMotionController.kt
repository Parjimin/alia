package com.littleblueworld.alia.world

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import kotlin.math.roundToLong

class WorldMotionController(
    private val host: ViewGroup,
    private val cloudFar: View,
    private val cloudNear: View,
) {
    private data class CloudAnimation(
        val animator: ObjectAnimator,
        val initialPhase: Float,
    )

    private val animations = mutableListOf<CloudAnimation>()
    private var shouldRun = false
    private var initialized = false
    private val startWhenLaidOut = Runnable {
        if (shouldRun && !initialized && host.width > 0) createAnimators()
    }

    fun startOrResume() {
        shouldRun = true
        if (!initialized) {
            host.removeCallbacks(startWhenLaidOut)
            host.post(startWhenLaidOut)
            return
        }
        animations.map(CloudAnimation::animator).filter { it.isPaused }.forEach { it.resume() }
    }

    fun pause() {
        shouldRun = false
        animations.map(CloudAnimation::animator).filter { it.isRunning }.forEach { it.pause() }
    }

    fun clear() {
        shouldRun = false
        host.removeCallbacks(startWhenLaidOut)
        animations.map(CloudAnimation::animator).forEach {
            it.removeAllListeners()
            it.cancel()
        }
        animations.clear()
        initialized = false
    }

    private fun createAnimators() {
        initialized = true
        animations += CloudAnimation(
            animator = cloudAnimator(cloudFar, speedDpPerSecond = 3.5f),
            initialPhase = 0.18f,
        )
        animations += CloudAnimation(
            animator = cloudAnimator(cloudNear, speedDpPerSecond = 6f),
            initialPhase = 0.64f,
        )
        animations.forEach { animation ->
            animation.animator.start()
            animation.animator.currentPlayTime =
                (animation.animator.duration.toFloat() * animation.initialPhase).roundToLong()
            if (!shouldRun) animation.animator.pause()
        }
    }

    private fun cloudAnimator(
        cloud: View,
        speedDpPerSecond: Float,
    ): ObjectAnimator {
        val startX = -cloud.right.toFloat()
        val endX = host.width - cloud.left.toFloat()
        val distanceDp = (endX - startX) / host.resources.displayMetrics.density
        var cycle = 0
        return ObjectAnimator.ofFloat(cloud, View.TRANSLATION_X, startX, endX).apply {
            duration = (distanceDp / speedDpPerSecond * 1_000f).roundToLong()
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            interpolator = LinearInterpolator()
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationRepeat(animation: Animator) {
                    cycle = (cycle + 1) % CLOUD_VARIATION_Y_DP.size
                    cloud.translationY = CLOUD_VARIATION_Y_DP[cycle] *
                        host.resources.displayMetrics.density
                    val scale = CLOUD_VARIATION_SCALE[cycle]
                    cloud.scaleX = scale
                    cloud.scaleY = scale
                }
            })
        }
    }

    private companion object {
        val CLOUD_VARIATION_Y_DP = floatArrayOf(0f, 5f, -3f, 2f)
        val CLOUD_VARIATION_SCALE = floatArrayOf(1f, 0.96f, 1.03f, 0.99f)
    }
}
