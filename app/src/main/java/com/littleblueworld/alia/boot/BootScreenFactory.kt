package com.littleblueworld.alia.boot

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import com.littleblueworld.alia.content.BirthdayContent
import com.littleblueworld.alia.databinding.ScreenBootBinding
import com.littleblueworld.alia.navigation.AppScreen
import com.littleblueworld.alia.navigation.ScreenId
import com.littleblueworld.alia.state.AppState
import com.littleblueworld.alia.state.ExperiencePhase
import kotlin.coroutines.resume
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

class BootScreenFactory(
    context: Context,
    private val content: BirthdayContent,
) {
    private val inflater = LayoutInflater.from(context)

    fun create(
        experiencePhase: ExperiencePhase,
        onComplete: (ExperiencePhase) -> Unit,
    ): AppScreen {
        val binding = ScreenBootBinding.inflate(inflater)
        return BootScreen(
            binding = binding,
            experiencePhase = experiencePhase,
            sequence = BootSequenceGenerator(content).generate(experiencePhase),
            onComplete = onComplete,
        )
    }

    private class BootScreen(
        private val binding: ScreenBootBinding,
        private val experiencePhase: ExperiencePhase,
        private val sequence: List<BootLine>,
        private val onComplete: (ExperiencePhase) -> Unit,
    ) : AppScreen {
        override val id = ScreenId.BOOT
        override val view: View = binding.root

        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        private var playbackJob: Job? = null
        private var ambientAnimator: AnimatorSet? = null
        private var currentLineIndex = 0
        private var isVisible = false
        private var isForeground = false
        private var completed = false

        init {
            binding.loadingLine.alpha = 0f
        }

        override fun render(state: AppState) = Unit

        override fun onShown() {
            isVisible = true
            startIfReady()
        }

        override fun onForegrounded() {
            isForeground = true
            startIfReady()
        }

        override fun onBackgrounded() {
            isForeground = false
            stopRunningWork()
        }

        override fun onHidden() {
            isVisible = false
            stopRunningWork()
            scope.cancel()
        }

        private fun startIfReady() {
            if (!isVisible || !isForeground || completed) return
            startAmbientAnimations()
            if (playbackJob?.isActive != true) {
                playbackJob = scope.launch { playRemainingLines() }
            }
        }

        private suspend fun playRemainingLines() {
            while (currentLineIndex < sequence.size && scope.isActive) {
                val line = sequence[currentLineIndex]
                if (binding.loadingLine.text.isNotEmpty()) {
                    binding.loadingLine.fadeTo(0f, TEXT_FADE_OUT_MS)
                }
                binding.loadingLine.text = line.text
                if (line.triggersSparkleBurst) {
                    binding.bootEffects.burst()
                }
                binding.loadingLine.fadeTo(1f, TEXT_FADE_IN_MS)
                delay(line.displayMillis)
                binding.loadingLine.fadeTo(0f, TEXT_FADE_OUT_MS)
                if (line.gapAfterMillis > 0L) delay(line.gapAfterMillis)
                currentLineIndex += 1
            }

            if (currentLineIndex == sequence.size && !completed) {
                completed = true
                onComplete(experiencePhase)
            }
        }

        private fun startAmbientAnimations() {
            if (ambientAnimator?.isStarted == true) return

            val starScaleX = ObjectAnimator.ofFloat(
                binding.loadingStar,
                View.SCALE_X,
                1f,
                1.08f,
                1f,
            ).repeating(STAR_PULSE_MS, AccelerateDecelerateInterpolator())
            val starScaleY = ObjectAnimator.ofFloat(
                binding.loadingStar,
                View.SCALE_Y,
                1f,
                1.08f,
                1f,
            ).repeating(STAR_PULSE_MS, AccelerateDecelerateInterpolator())
            val starAlpha = ObjectAnimator.ofFloat(
                binding.loadingStar,
                View.ALPHA,
                0.7f,
                1f,
                0.7f,
            ).repeating(STAR_PULSE_MS, AccelerateDecelerateInterpolator())
            val waveBob = ObjectAnimator.ofFloat(
                binding.waveStrip,
                View.TRANSLATION_Y,
                0f,
                -binding.root.resources.displayMetrics.density * WAVE_BOB_DP,
                0f,
            ).repeating(WAVE_BOB_MS, LinearInterpolator())

            ambientAnimator = AnimatorSet().also { animator ->
                animator.playTogether(starScaleX, starScaleY, starAlpha, waveBob)
                animator.start()
            }
        }

        private fun stopRunningWork() {
            playbackJob?.cancel()
            playbackJob = null
            ambientAnimator?.cancel()
            ambientAnimator = null
            binding.bootEffects.stop()
            binding.loadingStar.scaleX = 1f
            binding.loadingStar.scaleY = 1f
            binding.loadingStar.alpha = 1f
            binding.waveStrip.translationY = 0f
        }

        private fun ObjectAnimator.repeating(
            durationMillis: Long,
            animationInterpolator: android.animation.TimeInterpolator,
        ) = apply {
            duration = durationMillis
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.RESTART
            interpolator = animationInterpolator
        }

        private suspend fun View.fadeTo(
            targetAlpha: Float,
            durationMillis: Long,
        ) = suspendCancellableCoroutine { continuation ->
            animate().cancel()
            animate()
                .alpha(targetAlpha)
                .setDuration(durationMillis)
                .withEndAction {
                    if (continuation.isActive) continuation.resume(Unit)
                }
                .start()
            continuation.invokeOnCancellation {
                animate().withEndAction(null)
                animate().cancel()
            }
        }

        private companion object {
            const val TEXT_FADE_OUT_MS = 100L
            const val TEXT_FADE_IN_MS = 150L
            const val STAR_PULSE_MS = 1_900L
            const val WAVE_BOB_MS = 2_400L
            const val WAVE_BOB_DP = 2f
        }
    }
}
