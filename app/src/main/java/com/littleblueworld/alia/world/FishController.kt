package com.littleblueworld.alia.world

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.animation.ValueAnimator
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import kotlin.coroutines.resume
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

class FishController(
    private val host: ViewGroup,
    private val fish: View,
    private val fishArt: View,
    private val bubbles: FishBubbleView,
    private val dialogueState: FishDialogueState,
    private val onDialogue: (String) -> Unit,
    private val onMilestone: (Int) -> Unit,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var cycleJob: Job? = null
    private var escapeJob: Job? = null
    private var swimAnimator: ObjectAnimator? = null
    private var wobbleAnimator: ObjectAnimator? = null
    private var tapAnimator: AnimatorSet? = null
    private var swimContinuation: CancellableContinuation<Unit>? = null
    private var cycle = 0
    private var running = false
    private var escapePending = false
    private var currentEndX = 0f
    private var currentSpeedDpPerSecond = 30f
    private var currentLeftToRight = false
    private val startWhenLaidOut = Runnable {
        if (running && host.width > 0 && cycleJob == null) startCycleLoop()
    }

    init {
        fish.visibility = View.INVISIBLE
        fish.setOnClickListener { handleTap() }
    }

    fun update(
        persistedMilestone: Int,
        postFinale: Boolean,
    ) {
        dialogueState.update(persistedMilestone, postFinale)
    }

    fun start() {
        if (running) return
        running = true
        if (host.width > 0) {
            startCycleLoop()
        } else {
            host.removeCallbacks(startWhenLaidOut)
            host.post(startWhenLaidOut)
        }
    }

    fun stop() {
        running = false
        host.removeCallbacks(startWhenLaidOut)
        cycleJob?.cancel()
        cycleJob = null
        escapeJob?.cancel()
        escapeJob = null
        cancelAnimators()
        bubbles.stop()
        resetFish()
    }

    fun clear() {
        stop()
        fish.setOnClickListener(null)
        scope.cancel()
    }

    private fun startCycleLoop() {
        cycleJob = scope.launch {
            while (isActive && running) {
                val config = FishMotionPlan.forCycle(cycle++)
                delay(config.delayMs)
                if (!running) break
                swimAcross(config)
            }
        }.also { job ->
            job.invokeOnCompletion {
                if (cycleJob === job) cycleJob = null
            }
        }
    }

    private suspend fun swimAcross(config: FishSpawnConfig) =
        suspendCancellableCoroutine { continuation ->
            val density = host.resources.displayMetrics.density
            currentLeftToRight = config.leftToRight
            currentSpeedDpPerSecond = config.speedDpPerSecond
            fishArt.scaleX = if (config.leftToRight) -1f else 1f
            fish.visibility = View.VISIBLE
            fish.alpha = 1f
            fish.scaleX = 1f
            fish.scaleY = 1f
            fish.translationY = 0f
            val margin = OFFSCREEN_MARGIN_DP * density
            val startX = if (config.leftToRight) -fish.width - margin else host.width + margin
            currentEndX = if (config.leftToRight) host.width + margin else -fish.width - margin
            fish.x = startX
            fish.y = (host.height * config.centerYFraction - fish.height / 2f)
                .coerceIn(0f, (host.height - fish.height).coerceAtLeast(0).toFloat())
            swimContinuation = continuation
            startWobble(config)
            startSwimAnimator(
                startX = startX,
                endX = currentEndX,
                speedDpPerSecond = config.speedDpPerSecond,
            )
            continuation.invokeOnCancellation {
                cancelAnimators()
                resetFish()
            }
        }

    private fun startSwimAnimator(
        startX: Float,
        endX: Float,
        speedDpPerSecond: Float,
    ) {
        val density = host.resources.displayMetrics.density
        swimAnimator = ObjectAnimator.ofFloat(fish, View.X, startX, endX).apply {
            duration = FishMotionPlan.durationMs(startX / density, endX / density, speedDpPerSecond)
            interpolator = LinearInterpolator()
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    completeSwim(animation)
                }
            })
            start()
        }
    }

    private fun startWobble(config: FishSpawnConfig) {
        val amplitudePx = config.wobbleAmplitudeDp * host.resources.displayMetrics.density
        wobbleAnimator = ObjectAnimator.ofFloat(
            fish,
            View.TRANSLATION_Y,
            -amplitudePx,
            amplitudePx,
            -amplitudePx,
        ).apply {
            duration = config.wobblePeriodMs
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    private fun handleTap() {
        if (!running || fish.visibility != View.VISIBLE || escapePending || tapAnimator != null) return
        val result = dialogueState.tap()
        result.persistedMilestone?.let(onMilestone)
        onDialogue(result.message)
        val mouthX = fish.x + fish.width * if (currentLeftToRight) 0.82f else 0.18f
        bubbles.bubbleAt(mouthX, fish.y + fish.height * 0.43f)
        startTapFeedback()
        if (result.escapeAfterMessage) scheduleEscape()
    }

    private fun startTapFeedback() {
        val squash = ObjectAnimator.ofPropertyValuesHolder(
            fish,
            PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.08f),
            PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 0.88f),
        ).apply { duration = TAP_SQUASH_MS }
        val settle = ObjectAnimator.ofPropertyValuesHolder(
            fish,
            PropertyValuesHolder.ofFloat(View.SCALE_X, 1.08f, 1f),
            PropertyValuesHolder.ofFloat(View.SCALE_Y, 0.88f, 1f),
        ).apply { duration = TAP_SETTLE_MS }
        tapAnimator = AnimatorSet().apply {
            interpolator = AccelerateDecelerateInterpolator()
            playSequentially(squash, settle)
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    if (tapAnimator !== animation) return
                    tapAnimator = null
                    fish.scaleX = 1f
                    fish.scaleY = 1f
                }
            })
            start()
        }
    }

    private fun scheduleEscape() {
        escapePending = true
        swimAnimator?.pause()
        wobbleAnimator?.pause()
        escapeJob?.cancel()
        escapeJob = scope.launch {
            delay(ESCAPE_PAUSE_MS)
            if (!running || fish.visibility != View.VISIBLE) return@launch
            startFastEscape()
        }
    }

    private fun startFastEscape() {
        val oldAnimator = swimAnimator
        oldAnimator?.removeAllListeners()
        oldAnimator?.cancel()
        swimAnimator = null
        val density = host.resources.displayMetrics.density
        val startX = fish.x
        startSwimAnimator(
            startX = startX,
            endX = currentEndX,
            speedDpPerSecond = FishMotionPlan.escapeSpeed(currentSpeedDpPerSecond),
        )
        wobbleAnimator?.resume()
    }

    private fun completeSwim(animation: Animator) {
        if (swimAnimator !== animation) return
        swimAnimator = null
        wobbleAnimator?.removeAllListeners()
        wobbleAnimator?.cancel()
        wobbleAnimator = null
        tapAnimator?.removeAllListeners()
        tapAnimator?.cancel()
        tapAnimator = null
        escapeJob?.cancel()
        escapeJob = null
        escapePending = false
        resetFish()
        val continuation = swimContinuation
        swimContinuation = null
        if (continuation?.isActive == true) continuation.resume(Unit)
    }

    private fun cancelAnimators() {
        swimAnimator?.removeAllListeners()
        swimAnimator?.cancel()
        swimAnimator = null
        wobbleAnimator?.removeAllListeners()
        wobbleAnimator?.cancel()
        wobbleAnimator = null
        tapAnimator?.removeAllListeners()
        tapAnimator?.cancel()
        tapAnimator = null
        swimContinuation = null
        escapePending = false
    }

    private fun resetFish() {
        fish.visibility = View.INVISIBLE
        fish.alpha = 1f
        fish.scaleX = 1f
        fish.scaleY = 1f
        fish.translationY = 0f
        fish.rotation = 0f
    }

    private companion object {
        const val OFFSCREEN_MARGIN_DP = 12f
        const val TAP_SQUASH_MS = 90L
        const val TAP_SETTLE_MS = 150L
        const val ESCAPE_PAUSE_MS = 900L
    }
}
