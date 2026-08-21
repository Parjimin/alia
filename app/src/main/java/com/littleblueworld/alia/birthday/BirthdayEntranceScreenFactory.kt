package com.littleblueworld.alia.birthday

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import com.littleblueworld.alia.content.BirthdayContent
import com.littleblueworld.alia.databinding.ScreenBirthdayEntranceBinding
import com.littleblueworld.alia.navigation.AppScreen
import com.littleblueworld.alia.navigation.ScreenId
import com.littleblueworld.alia.state.AppState
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

class BirthdayEntranceScreenFactory(
    context: Context,
    private val content: BirthdayContent,
) {
    private val inflater = LayoutInflater.from(context)

    fun create(onOpenWorld: () -> Boolean): AppScreen {
        val binding = ScreenBirthdayEntranceBinding.inflate(inflater)
        return BirthdayEntranceScreen(binding, content, onOpenWorld)
    }

    private class BirthdayEntranceScreen(
        private val binding: ScreenBirthdayEntranceBinding,
        content: BirthdayContent,
        private val onOpenWorld: () -> Boolean,
    ) : AppScreen {
        override val id = ScreenId.BIRTHDAY_INTRO
        override val view: View = binding.root

        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        private var revealJob: Job? = null
        private var revealStep = 0
        private var isVisible = false
        private var isForeground = false

        init {
            binding.birthdayHeading.text = content.birthdayHeading
            binding.birthdayName.text = content.birthdayName
            binding.openWorldButton.text = content.birthdayCta

            binding.birthdayHeading.alpha = 0f
            binding.birthdayName.alpha = 0f
            binding.openWorldButton.alpha = 0f
            binding.openWorldButton.visibility = View.INVISIBLE
            binding.openWorldButton.isEnabled = false
            binding.openWorldButton.setOnClickListener {
                if (!binding.openWorldButton.isEnabled) return@setOnClickListener
                binding.openWorldButton.isEnabled = false
                if (!onOpenWorld()) {
                    binding.openWorldButton.isEnabled = true
                }
            }
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
            stopReveal()
        }

        override fun onHidden() {
            isVisible = false
            stopReveal()
            scope.cancel()
        }

        private fun startIfReady() {
            if (!isVisible || !isForeground || revealStep >= REVEAL_COMPLETE) return
            if (revealJob?.isActive == true) return
            revealJob = scope.launch { revealRemaining() }
        }

        private suspend fun revealRemaining() {
            while (revealStep < REVEAL_COMPLETE && scope.isActive) {
                when (revealStep) {
                    0 -> {
                        binding.birthdayHeading.fadeTo(1f, HEADING_REVEAL_MS)
                        delay(AFTER_HEADING_MS)
                    }

                    1 -> {
                        binding.birthdayName.fadeTo(1f, NAME_REVEAL_MS)
                        delay(AFTER_NAME_MS)
                    }

                    2 -> {
                        binding.openWorldButton.visibility = View.VISIBLE
                        binding.openWorldButton.fadeTo(1f, CTA_REVEAL_MS)
                        binding.openWorldButton.isEnabled = true
                    }
                }
                revealStep += 1
            }
        }

        private fun stopReveal() {
            revealJob?.cancel()
            revealJob = null
            binding.birthdayHeading.animate().cancel()
            binding.birthdayName.animate().cancel()
            binding.openWorldButton.animate().cancel()
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
            const val REVEAL_COMPLETE = 3
            const val HEADING_REVEAL_MS = 430L
            const val AFTER_HEADING_MS = 400L
            const val NAME_REVEAL_MS = 560L
            const val AFTER_NAME_MS = 600L
            const val CTA_REVEAL_MS = 600L
        }
    }
}
