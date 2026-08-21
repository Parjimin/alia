package com.littleblueworld.alia.messages

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import com.littleblueworld.alia.R
import com.littleblueworld.alia.content.BirthdayContent
import com.littleblueworld.alia.databinding.ScreenMessagesBinding
import com.littleblueworld.alia.navigation.AppScreen
import com.littleblueworld.alia.navigation.ScreenId
import com.littleblueworld.alia.state.AppState

data class MessageActions(
    val goBack: () -> Unit,
    val onFirstMeaningfulInteraction: () -> Unit,
)

class MessageScreenFactory(
    context: Context,
    private val content: BirthdayContent,
) {
    private val inflater = LayoutInflater.from(context)

    fun create(
        state: AppState,
        actions: MessageActions,
    ): AppScreen = MessageScreen(
        binding = ScreenMessagesBinding.inflate(inflater),
        messages = content.bottleMessages,
        alreadyDiscovered = state.messagesVisited,
        actions = actions,
    )

    private class MessageScreen(
        private val binding: ScreenMessagesBinding,
        private val messages: List<String>,
        alreadyDiscovered: Boolean,
        private val actions: MessageActions,
    ) : AppScreen {
        override val id = ScreenId.MESSAGES
        override val view: View = binding.root

        private val bottles = listOf(
            binding.bottle01,
            binding.bottle02,
            binding.bottle03,
            binding.bottle04,
        )
        private val openedBottles = mutableSetOf<Int>()
        private val motionController = MessageBottleMotionController(bottles)
        private var panelAnimator: AnimatorSet? = null
        private var discoveryRecorded = alreadyDiscovered
        private var panelOpen = false
        private var visible = false
        private var foreground = false

        init {
            require(messages.size == bottles.size)
            binding.messageOverlay.visibility = View.GONE
            binding.messagesBack.setOnClickListener {
                if (!closePanel()) actions.goBack()
            }
            binding.messageScrim.setOnClickListener { closePanel() }
            binding.messageClose.setOnClickListener { closePanel() }
            bottles.forEachIndexed { index, bottle ->
                bottle.contentDescription = binding.root.context.getString(
                    R.string.message_bottle_description,
                    index + 1,
                    bottles.size,
                )
                bottle.setOnClickListener { openBottle(index) }
            }
        }

        override fun render(state: AppState) = Unit

        override fun onBackPressed(): Boolean = closePanel()

        override fun onShown() {
            visible = true
            startMotionIfReady()
        }

        override fun onForegrounded() {
            foreground = true
            if (panelOpen) finishPanelReveal() else startMotionIfReady()
        }

        override fun onBackgrounded() {
            foreground = false
            panelAnimator?.cancel()
            if (panelOpen) finishPanelReveal()
            motionController.stop()
        }

        override fun onHidden() {
            visible = false
            panelAnimator?.cancel()
            panelAnimator = null
            motionController.stop()
            binding.messagesBack.setOnClickListener(null)
            binding.messageScrim.setOnClickListener(null)
            binding.messageClose.setOnClickListener(null)
            bottles.forEach { it.setOnClickListener(null) }
        }

        private fun openBottle(index: Int) {
            if (panelOpen) return
            panelOpen = true
            val firstOpen = openedBottles.add(index)
            motionController.stop()
            setBottleInteractionsEnabled(false)
            bottles.forEachIndexed { bottleIndex, bottle ->
                if (bottleIndex != index) bottle.alpha = DIMMED_BOTTLE_ALPHA
            }
            animateBottleLift(bottles[index])
            bindMessage(index)
            showPanel(firstOpen)
            if (!discoveryRecorded) {
                discoveryRecorded = true
                actions.onFirstMeaningfulInteraction()
            }
        }

        private fun bindMessage(index: Int) {
            val parts = BottleMessageParts.from(messages[index])
            binding.messageNumber.text = binding.root.context.getString(
                R.string.message_bottle_number,
                index + 1,
            )
            binding.messageFirstBeat.text = parts.firstBeat
            binding.messageRemainder.text = parts.remainder
            binding.messageRemainder.visibility = if (parts.remainder.isEmpty()) View.GONE else View.VISIBLE
        }

        private fun showPanel(firstOpen: Boolean) {
            panelAnimator?.cancel()
            binding.messageOverlay.visibility = View.VISIBLE
            binding.messageScrim.alpha = 0f
            binding.messagePanel.apply {
                alpha = 0f
                scaleX = PANEL_START_SCALE
                scaleY = PANEL_START_SCALE
            }
            binding.messageFirstBeat.alpha = if (firstOpen) 0f else 1f
            binding.messageRemainder.alpha = if (firstOpen) 0f else 1f

            val scrim = ObjectAnimator.ofFloat(binding.messageScrim, View.ALPHA, 0f, SCRIM_ALPHA)
            val panelAlpha = ObjectAnimator.ofFloat(binding.messagePanel, View.ALPHA, 0f, 1f)
            val panelScaleX = ObjectAnimator.ofFloat(binding.messagePanel, View.SCALE_X, PANEL_START_SCALE, 1f)
            val panelScaleY = ObjectAnimator.ofFloat(binding.messagePanel, View.SCALE_Y, PANEL_START_SCALE, 1f)
            val panelEntrance = AnimatorSet().apply {
                duration = if (firstOpen) FIRST_PANEL_DURATION_MS else REOPEN_DURATION_MS
                interpolator = DecelerateInterpolator()
                playTogether(scrim, panelAlpha, panelScaleX, panelScaleY)
            }

            panelAnimator = if (firstOpen) {
                val firstBeat = ObjectAnimator.ofFloat(binding.messageFirstBeat, View.ALPHA, 0f, 1f).apply {
                    duration = FIRST_BEAT_DURATION_MS
                }
                val remainder = ObjectAnimator.ofFloat(binding.messageRemainder, View.ALPHA, 0f, 1f).apply {
                    duration = REMAINDER_DURATION_MS
                }
                AnimatorSet().apply {
                    interpolator = AccelerateDecelerateInterpolator()
                    playSequentially(panelEntrance, firstBeat, remainder)
                }
            } else {
                AnimatorSet().apply { playTogether(panelEntrance) }
            }.also { animator ->
                animator.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        if (panelAnimator !== animator) return
                        panelAnimator = null
                        finishPanelReveal()
                    }
                })
                animator.start()
            }
        }

        private fun closePanel(): Boolean {
            if (!panelOpen) return false
            panelOpen = false
            panelAnimator?.cancel()
            panelAnimator = null
            binding.messageOverlay.visibility = View.GONE
            binding.messagePanel.alpha = 1f
            binding.messageScrim.alpha = SCRIM_ALPHA
            bottles.forEach { it.alpha = 1f }
            setBottleInteractionsEnabled(true)
            startMotionIfReady()
            return true
        }

        private fun finishPanelReveal() {
            binding.messageScrim.alpha = SCRIM_ALPHA
            binding.messagePanel.apply {
                alpha = 1f
                scaleX = 1f
                scaleY = 1f
            }
            binding.messageFirstBeat.alpha = 1f
            binding.messageRemainder.alpha = 1f
        }

        private fun animateBottleLift(bottle: View) {
            bottle.animate()
                .translationY(-10f * bottle.resources.displayMetrics.density)
                .rotation(0f)
                .scaleX(1.06f)
                .scaleY(1.06f)
                .setDuration(BOTTLE_LIFT_DURATION_MS)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }

        private fun setBottleInteractionsEnabled(enabled: Boolean) {
            bottles.forEach {
                it.isEnabled = enabled
                it.isClickable = enabled
            }
        }

        private fun startMotionIfReady() {
            if (visible && foreground && !panelOpen) motionController.start()
        }

        private companion object {
            const val SCRIM_ALPHA = 0.64f
            const val DIMMED_BOTTLE_ALPHA = 0.5f
            const val PANEL_START_SCALE = 0.94f
            const val BOTTLE_LIFT_DURATION_MS = 160L
            const val FIRST_PANEL_DURATION_MS = 260L
            const val REOPEN_DURATION_MS = 170L
            const val FIRST_BEAT_DURATION_MS = 240L
            const val REMAINDER_DURATION_MS = 320L
        }
    }
}
