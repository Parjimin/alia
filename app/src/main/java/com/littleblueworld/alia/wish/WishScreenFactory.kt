package com.littleblueworld.alia.wish

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import androidx.core.view.isVisible
import com.littleblueworld.alia.content.BirthdayContent
import com.littleblueworld.alia.databinding.ScreenWishBinding
import com.littleblueworld.alia.navigation.AppScreen
import com.littleblueworld.alia.navigation.ScreenId
import com.littleblueworld.alia.state.AppState
import com.littleblueworld.alia.state.WishRules
import com.littleblueworld.alia.state.WishState
import kotlin.coroutines.resume
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

data class WishActions(
    val saveDraft: suspend (String) -> Unit,
    val flushDraft: (String) -> Unit,
    val sealWish: suspend () -> Boolean,
    val keepLocal: suspend () -> Boolean,
    val sendWish: suspend () -> WishSendResult,
    val goBack: () -> Unit,
    val continueToFinale: () -> Unit,
)

class WishScreenFactory(
    context: Context,
    private val content: BirthdayContent,
) {
    private val inflater = LayoutInflater.from(context)

    fun create(
        state: AppState,
        actions: WishActions,
    ): AppScreen = WishScreen(
        binding = ScreenWishBinding.inflate(inflater),
        content = content,
        initialState = state,
        actions = actions,
    )

    private class WishScreen(
        private val binding: ScreenWishBinding,
        private val content: BirthdayContent,
        initialState: AppState,
        private val actions: WishActions,
    ) : AppScreen {
        override val id = ScreenId.WISH
        override val view: View = binding.root

        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        private var latestState = initialState
        private var phase = Phase.INTRO
        private var sequenceJob: Job? = null
        private var draftSaveJob: Job? = null
        private var activeAnimator: AnimatorSet? = null
        private var starPulse: ObjectAnimator? = null
        private var sceneReady = false
        private var foreground = false
        private var visible = false
        private var choiceInProgress = false
        private var finaleNavigationStarted = false

        private val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(text: CharSequence?, start: Int, count: Int, after: Int) = Unit

            override fun onTextChanged(text: CharSequence?, start: Int, before: Int, count: Int) {
                val draft = text?.toString().orEmpty()
                renderCounter(draft.length)
                binding.wishError.visibility = View.INVISIBLE
                scheduleDraftSave(draft)
            }

            override fun afterTextChanged(text: Editable?) = Unit
        }

        init {
            require(content.wishEntry.size == 2)
            require(content.wishLeaveOptions.size == 2)
            binding.wishPrompt.text = content.wishPrompt
            binding.wishInput.hint = content.wishPlaceholder
            binding.wishInput.setText(initialState.wishDraft)
            binding.wishInput.setSelection(binding.wishInput.text.length)
            renderCounter(binding.wishInput.text.length)
            binding.wishError.text = content.wishEmpty
            binding.wishHold.text = content.wishSealCta
            binding.wishDestinationPrompt.text = content.wishDestination
            binding.wishKeepTitle.text = content.wishKeepTitle
            binding.wishKeepDescription.text = content.wishKeepDescription
            binding.wishSendTitle.text = content.wishSendTitle
            binding.wishSendDescription.text = content.wishSendClearerDescription
            binding.wishLeavePrompt.text = content.wishLeaveDraft
            binding.wishKeepWriting.text = content.wishLeaveOptions[0]
            binding.wishLeaveNow.text = content.wishLeaveOptions[1]
            binding.wishLeaveScrim.contentDescription = content.wishLeaveOptions[0]
            binding.wishInput.addTextChangedListener(textWatcher)
            binding.wishBack.setOnClickListener { requestLeave() }
            binding.wishStarHero.setOnClickListener { openInput() }
            binding.wishKeepChoice.setOnClickListener { beginKeepLocal() }
            binding.wishSendChoice.setOnClickListener { beginSend() }
            binding.wishLeaveScrim.setOnClickListener { closeLeavePrompt() }
            binding.wishKeepWriting.setOnClickListener { closeLeavePrompt() }
            binding.wishLeaveNow.setOnClickListener { leaveNow() }
            binding.wishHold.canStartHold = { binding.wishInput.text?.isNotBlank() == true }
            binding.wishHold.onRejected = { showEmptyWishMessage() }
            binding.wishHold.onCompleted = { beginSeal() }
            hideAllContent()
        }

        override fun render(state: AppState) {
            latestState = state
            if (!sceneReady || choiceInProgress) return
            when {
                state.wishState == WishState.SEALED && phase == Phase.INPUT -> {
                    showDestination(immediate = false)
                }

                WishRules.isComplete(state.wishState) && phase != Phase.COMPLETED -> {
                    showCompletedResume()
                }
            }
        }

        override fun onShown() {
            visible = true
            startSceneIfReady()
        }

        override fun onForegrounded() {
            foreground = true
            if (!sceneReady) startSceneIfReady() else resumeCurrentPhase()
        }

        override fun onBackgrounded() {
            foreground = false
            flushDraftIfNeeded()
            hideKeyboard()
            binding.wishHold.stop()
            stopMotion()
            choiceInProgress = false
            binding.wishHold.isEnabled = true
            binding.wishInput.isEnabled = true
            if (phase == Phase.DESTINATION) showDestination(immediate = true)
            if (!sceneReady) settleInterruptedEntrance()
        }

        override fun onHidden() {
            visible = false
            binding.wishInput.removeTextChangedListener(textWatcher)
            binding.wishBack.setOnClickListener(null)
            binding.wishStarHero.setOnClickListener(null)
            binding.wishKeepChoice.setOnClickListener(null)
            binding.wishSendChoice.setOnClickListener(null)
            binding.wishLeaveScrim.setOnClickListener(null)
            binding.wishKeepWriting.setOnClickListener(null)
            binding.wishLeaveNow.setOnClickListener(null)
            binding.wishHold.canStartHold = { false }
            binding.wishHold.onRejected = {}
            binding.wishHold.onCompleted = {}
            binding.wishHold.stop()
            binding.wishEffects.stop()
            stopMotion()
            binding.wishDay.setImageDrawable(null)
            scope.cancel()
        }

        override fun onBackPressed(): Boolean {
            requestLeave()
            return true
        }

        private fun startSceneIfReady() {
            if (!visible || !foreground || sceneReady || sequenceJob?.isActive == true) return
            sequenceJob = scope.launch {
                crossfadeToDusk()
                sceneReady = true
                when (latestState.wishState) {
                    WishState.SEALED -> showDestination(immediate = false)
                    WishState.KEPT_LOCAL, WishState.SENT -> showCompletedResume()
                    WishState.PENDING_SEND -> showPendingResume()
                    else -> playIntroAndStarEntrance()
                }
            }
        }

        private suspend fun crossfadeToDusk() {
            runAnimator(
                AnimatorSet().apply {
                    duration = DUSK_CROSSFADE_MS
                    interpolator = AccelerateDecelerateInterpolator()
                    playTogether(
                        ObjectAnimator.ofFloat(binding.wishDay, View.ALPHA, 1f, 0f),
                        ObjectAnimator.ofFloat(binding.wishDusk, View.ALPHA, 0f, 1f),
                    )
                },
            )
            releaseDayBackground()
        }

        private suspend fun playIntroAndStarEntrance() {
            phase = Phase.INTRO
            delay(AFTER_DUSK_PAUSE_MS)
            showIntroLine(content.wishEntry[0], FIRST_INTRO_HOLD_MS)
            showIntroLine(content.wishEntry[1], SECOND_INTRO_HOLD_MS)
            binding.wishIntroText.visibility = View.INVISIBLE
            binding.wishStarHero.apply {
                visibility = View.VISIBLE
                alpha = 0f
                scaleX = STAR_ENTRANCE_SCALE
                scaleY = STAR_ENTRANCE_SCALE
                translationY = STAR_ENTRANCE_TRANSLATION_DP.toPx()
            }
            runAnimator(
                AnimatorSet().apply {
                    duration = STAR_ENTRANCE_MS
                    interpolator = DecelerateInterpolator()
                    playTogether(
                        ObjectAnimator.ofFloat(binding.wishStarHero, View.ALPHA, 0f, 1f),
                        ObjectAnimator.ofFloat(
                            binding.wishStarHero,
                            View.SCALE_X,
                            STAR_ENTRANCE_SCALE,
                            1f,
                        ),
                        ObjectAnimator.ofFloat(
                            binding.wishStarHero,
                            View.SCALE_Y,
                            STAR_ENTRANCE_SCALE,
                            1f,
                        ),
                        ObjectAnimator.ofFloat(
                            binding.wishStarHero,
                            View.TRANSLATION_Y,
                            STAR_ENTRANCE_TRANSLATION_DP.toPx(),
                            0f,
                        ),
                    )
                },
            )
            phase = Phase.STAR_READY
            startStarPulse()
        }

        private suspend fun showIntroLine(text: String, holdMillis: Long) {
            binding.wishIntroText.apply {
                this.text = text
                alpha = 0f
                visibility = View.VISIBLE
            }
            runAnimator(alphaAnimator(binding.wishIntroText, 0f, 1f, INTRO_FADE_MS))
            delay(holdMillis)
            runAnimator(alphaAnimator(binding.wishIntroText, 1f, 0f, INTRO_FADE_MS))
        }

        private fun openInput() {
            if (phase != Phase.STAR_READY || !foreground) return
            phase = Phase.INPUT
            binding.wishInputPanel.apply {
                visibility = View.VISIBLE
                alpha = 0f
                translationY = INPUT_PANEL_TRANSLATION_DP.toPx()
            }
            activeAnimator = AnimatorSet().apply {
                duration = INPUT_PANEL_MS
                interpolator = DecelerateInterpolator()
                playTogether(
                    ObjectAnimator.ofFloat(binding.wishInputPanel, View.ALPHA, 0f, 1f),
                    ObjectAnimator.ofFloat(
                        binding.wishInputPanel,
                        View.TRANSLATION_Y,
                        INPUT_PANEL_TRANSLATION_DP.toPx(),
                        0f,
                    ),
                )
                addListener(clearActiveAnimatorListener())
                start()
            }
            binding.wishInput.post {
                if (phase != Phase.INPUT || !foreground) return@post
                binding.wishInput.requestFocus()
                inputMethodManager.showSoftInput(binding.wishInput, 0)
            }
        }

        private fun beginSeal() {
            if (phase != Phase.INPUT || choiceInProgress) return
            choiceInProgress = true
            binding.wishHold.isEnabled = false
            binding.wishInput.isEnabled = false
            hideKeyboard()
            draftSaveJob?.cancel()
            sequenceJob = scope.launch {
                val draft = binding.wishInput.text?.toString().orEmpty()
                actions.saveDraft(draft)
                val accepted = actions.sealWish()
                choiceInProgress = false
                if (accepted) {
                    binding.wishHold.stop()
                    binding.wishEffects.burstAt(0.5f, 0.42f)
                    showDestination(immediate = false)
                } else {
                    binding.wishHold.isEnabled = true
                    binding.wishInput.isEnabled = true
                    showEmptyWishMessage()
                }
            }
        }

        private fun showDestination(immediate: Boolean) {
            if (
                !immediate &&
                phase == Phase.DESTINATION &&
                binding.wishDestinationPanel.isVisible
            ) return
            phase = Phase.DESTINATION
            hideKeyboard()
            stopStarPulse()
            binding.wishInputPanel.visibility = View.GONE
            binding.wishStarHero.visibility = View.GONE
            binding.wishResult.visibility = View.GONE
            binding.wishDestinationPanel.visibility = View.VISIBLE
            binding.wishCapsule.apply {
                visibility = View.VISIBLE
                alpha = if (immediate) 1f else 0f
                scaleX = if (immediate) 1f else CAPSULE_START_SCALE
                scaleY = if (immediate) 1f else CAPSULE_START_SCALE
                rotation = if (immediate) 0f else CAPSULE_START_ROTATION
                translationY = 0f
            }
            setDestinationEnabled(true)
            if (!immediate && foreground) {
                activeAnimator = AnimatorSet().apply {
                    duration = CAPSULE_ENTRANCE_MS
                    interpolator = DecelerateInterpolator()
                    playTogether(
                        ObjectAnimator.ofFloat(binding.wishCapsule, View.ALPHA, 0f, 1f),
                        ObjectAnimator.ofFloat(
                            binding.wishCapsule,
                            View.SCALE_X,
                            CAPSULE_START_SCALE,
                            1f,
                        ),
                        ObjectAnimator.ofFloat(
                            binding.wishCapsule,
                            View.SCALE_Y,
                            CAPSULE_START_SCALE,
                            1f,
                        ),
                        ObjectAnimator.ofFloat(
                            binding.wishCapsule,
                            View.ROTATION,
                            CAPSULE_START_ROTATION,
                            0f,
                        ),
                    )
                    addListener(clearActiveAnimatorListener())
                    start()
                }
            }
        }

        private fun beginKeepLocal() {
            if (phase != Phase.DESTINATION || choiceInProgress) return
            choiceInProgress = true
            setDestinationEnabled(false)
            sequenceJob = scope.launch {
                val accepted = actions.keepLocal()
                if (!accepted) {
                    choiceInProgress = false
                    setDestinationEnabled(true)
                    return@launch
                }
                phase = Phase.COMPLETED
                binding.wishDestinationPanel.visibility = View.GONE
                binding.wishResult.apply {
                    text = content.wishKeptLocal
                    visibility = View.VISIBLE
                }
                binding.wishEffects.burstAt(0.5f, 0.38f)
                runAnimator(
                    AnimatorSet().apply {
                        duration = KEEP_SETTLE_MS
                        interpolator = DecelerateInterpolator()
                        playTogether(
                            ObjectAnimator.ofFloat(
                                binding.wishCapsule,
                                View.TRANSLATION_Y,
                                0f,
                                KEEP_SETTLE_DP.toPx(),
                            ),
                            ObjectAnimator.ofFloat(binding.wishCapsule, View.SCALE_X, 1f, 0.96f),
                            ObjectAnimator.ofFloat(binding.wishCapsule, View.SCALE_Y, 1f, 0.96f),
                        )
                    },
                )
                delay(RESULT_HOLD_MS)
                continueToFinaleOnce()
            }
        }

        private fun beginSend() {
            if (phase != Phase.DESTINATION || choiceInProgress) return
            choiceInProgress = true
            setDestinationEnabled(false)
            sequenceJob = scope.launch {
                binding.wishDestinationPanel.visibility = View.GONE
                binding.wishResult.apply {
                    text = content.wishSending
                    visibility = View.VISIBLE
                }
                when (actions.sendWish()) {
                    WishSendResult.Sent -> showSentResult()
                    WishSendResult.PendingOffline -> showPendingResult(content.wishOffline)
                    WishSendResult.PendingTemporary,
                    WishSendResult.PendingPermanentFailure,
                    -> showPendingResult(content.wishTemporaryFailure)
                    WishSendResult.Rejected -> {
                        choiceInProgress = false
                        showDestination(immediate = true)
                        return@launch
                    }
                }
                delay(RESULT_HOLD_MS)
                continueToFinaleOnce()
            }
        }

        private suspend fun showSentResult() {
            phase = Phase.COMPLETED
            binding.wishEffects.burstAt(0.5f, 0.36f)
            runAnimator(
                AnimatorSet().apply {
                    duration = SEND_DEPARTURE_MS
                    interpolator = AccelerateDecelerateInterpolator()
                    playTogether(
                        ObjectAnimator.ofFloat(
                            binding.wishCapsule,
                            View.TRANSLATION_Y,
                            0f,
                            -SEND_RISE_DP.toPx(),
                        ),
                        ObjectAnimator.ofFloat(binding.wishCapsule, View.SCALE_X, 1f, 0.75f),
                        ObjectAnimator.ofFloat(binding.wishCapsule, View.SCALE_Y, 1f, 0.75f),
                        ObjectAnimator.ofFloat(binding.wishCapsule, View.ALPHA, 1f, 0f),
                    )
                },
            )
            binding.wishResult.text = content.wishSuccess.joinToString("\n\n")
        }

        private fun showPendingResult(copy: String) {
            phase = Phase.COMPLETED
            binding.wishCapsule.alpha = 1f
            binding.wishResult.text = copy
        }

        private fun showPendingResume() {
            phase = Phase.COMPLETED
            choiceInProgress = false
            hideKeyboard()
            stopStarPulse()
            binding.wishInputPanel.visibility = View.GONE
            binding.wishDestinationPanel.visibility = View.GONE
            binding.wishStarHero.visibility = View.GONE
            binding.wishCapsule.visibility = View.VISIBLE
            binding.wishResult.apply {
                text = content.wishTemporaryFailure
                visibility = View.VISIBLE
            }
            if (foreground && !finaleNavigationStarted) {
                sequenceJob = scope.launch {
                    delay(ALREADY_COMPLETED_HOLD_MS)
                    continueToFinaleOnce()
                }
            }
        }

        private fun showCompletedResume() {
            phase = Phase.COMPLETED
            choiceInProgress = false
            hideKeyboard()
            stopStarPulse()
            binding.wishInputPanel.visibility = View.GONE
            binding.wishDestinationPanel.visibility = View.GONE
            binding.wishStarHero.visibility = View.GONE
            binding.wishCapsule.apply {
                visibility = View.VISIBLE
                alpha = 1f
                scaleX = 1f
                scaleY = 1f
                rotation = 0f
                translationY = 0f
            }
            binding.wishResult.apply {
                text = content.wishAlreadyCompleted
                visibility = View.VISIBLE
            }
            if (foreground && !finaleNavigationStarted) {
                sequenceJob = scope.launch {
                    delay(ALREADY_COMPLETED_HOLD_MS)
                    continueToFinaleOnce()
                }
            }
        }

        private fun requestLeave() {
            if (choiceInProgress || phase == Phase.COMPLETED) return
            if (binding.wishLeaveOverlay.isVisible) {
                closeLeavePrompt()
                return
            }
            if (phase == Phase.INPUT && binding.wishInput.text?.isNotBlank() == true) {
                hideKeyboard()
                binding.wishLeaveOverlay.visibility = View.VISIBLE
                binding.wishLeaveScrim.alpha = LEAVE_SCRIM_ALPHA
            } else {
                flushDraftIfNeeded()
                actions.goBack()
            }
        }

        private fun closeLeavePrompt() {
            binding.wishLeaveOverlay.visibility = View.GONE
            if (phase == Phase.INPUT && foreground) {
                binding.wishInput.requestFocus()
                inputMethodManager.showSoftInput(binding.wishInput, 0)
            }
        }

        private fun leaveNow() {
            flushDraftIfNeeded()
            binding.wishLeaveOverlay.visibility = View.GONE
            actions.goBack()
        }

        private fun scheduleDraftSave(draft: String) {
            if (phase != Phase.INPUT && phase != Phase.STAR_READY) return
            draftSaveJob?.cancel()
            draftSaveJob = scope.launch {
                delay(DRAFT_DEBOUNCE_MS)
                actions.saveDraft(draft)
            }
        }

        private fun flushDraftIfNeeded() {
            draftSaveJob?.cancel()
            draftSaveJob = null
            if (phase != Phase.INPUT) return
            val draft = binding.wishInput.text?.toString().orEmpty()
            actions.flushDraft(draft)
        }

        private fun hideKeyboard() {
            binding.wishInput.clearFocus()
            inputMethodManager.hideSoftInputFromWindow(binding.wishInput.windowToken, 0)
        }

        private fun showEmptyWishMessage() {
            binding.wishError.visibility = View.VISIBLE
        }

        private fun resumeCurrentPhase() {
            when (latestState.wishState) {
                WishState.SEALED -> {
                    showDestination(immediate = true)
                    return
                }
                WishState.KEPT_LOCAL, WishState.SENT -> {
                    showCompletedResume()
                    return
                }
                WishState.PENDING_SEND -> {
                    showPendingResume()
                    return
                }
                else -> Unit
            }
            when (phase) {
                Phase.STAR_READY, Phase.INPUT -> startStarPulse()
                Phase.COMPLETED -> if (WishRules.isComplete(latestState.wishState)) {
                    showCompletedResume()
                }
                else -> Unit
            }
        }

        private fun settleInterruptedEntrance() {
            sceneReady = true
            releaseDayBackground()
            binding.wishIntroText.visibility = View.INVISIBLE
            when (latestState.wishState) {
                WishState.SEALED -> showDestination(immediate = true)
                WishState.KEPT_LOCAL, WishState.SENT -> {
                    phase = Phase.COMPLETED
                    binding.wishCapsule.visibility = View.VISIBLE
                    binding.wishResult.text = content.wishAlreadyCompleted
                    binding.wishResult.visibility = View.VISIBLE
                }
                WishState.PENDING_SEND -> {
                    phase = Phase.COMPLETED
                    binding.wishCapsule.visibility = View.VISIBLE
                    binding.wishResult.text = content.wishTemporaryFailure
                    binding.wishResult.visibility = View.VISIBLE
                }
                else -> {
                    phase = Phase.STAR_READY
                    binding.wishStarHero.apply {
                        visibility = View.VISIBLE
                        alpha = 1f
                        scaleX = 1f
                        scaleY = 1f
                        translationY = 0f
                    }
                }
            }
        }

        private fun hideAllContent() {
            binding.wishInputPanel.visibility = View.GONE
            binding.wishDestinationPanel.visibility = View.GONE
            binding.wishResult.visibility = View.GONE
            binding.wishCapsule.visibility = View.GONE
            binding.wishLeaveOverlay.visibility = View.GONE
        }

        private fun releaseDayBackground() {
            binding.wishDusk.alpha = 1f
            binding.wishDay.visibility = View.GONE
            binding.wishDay.setImageDrawable(null)
        }

        private fun startStarPulse() {
            if (!foreground || binding.wishStarHero.visibility != View.VISIBLE || starPulse != null) return
            starPulse = ObjectAnimator.ofPropertyValuesHolder(
                binding.wishStarHero,
                android.animation.PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.025f, 1f),
                android.animation.PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.025f, 1f),
            ).apply {
                duration = STAR_PULSE_MS
                repeatCount = ObjectAnimator.INFINITE
                interpolator = AccelerateDecelerateInterpolator()
                start()
            }
        }

        private fun stopStarPulse() {
            starPulse?.cancel()
            starPulse = null
            binding.wishStarHero.scaleX = 1f
            binding.wishStarHero.scaleY = 1f
        }

        private fun stopMotion() {
            sequenceJob?.cancel()
            sequenceJob = null
            draftSaveJob?.cancel()
            draftSaveJob = null
            activeAnimator?.removeAllListeners()
            activeAnimator?.cancel()
            activeAnimator = null
            stopStarPulse()
            binding.wishEffects.stop()
        }

        private fun setDestinationEnabled(enabled: Boolean) {
            binding.wishKeepChoice.isEnabled = enabled
            binding.wishSendChoice.isEnabled = enabled
        }

        private fun continueToFinaleOnce() {
            if (finaleNavigationStarted || !foreground) return
            finaleNavigationStarted = true
            actions.continueToFinale()
        }

        private fun renderCounter(length: Int) {
            binding.wishCounter.text = binding.root.context.getString(
                com.littleblueworld.alia.R.string.wish_counter_format,
                length.coerceIn(0, WishRules.MAX_LENGTH),
            )
        }

        private fun alphaAnimator(view: View, from: Float, to: Float, durationMs: Long) =
            AnimatorSet().apply {
                duration = durationMs
                playTogether(ObjectAnimator.ofFloat(view, View.ALPHA, from, to))
            }

        private suspend fun runAnimator(animator: AnimatorSet) =
            suspendCancellableCoroutine { continuation ->
                activeAnimator?.removeAllListeners()
                activeAnimator?.cancel()
                activeAnimator = animator
                animator.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        if (activeAnimator === animation) activeAnimator = null
                        if (continuation.isActive) continuation.resume(Unit)
                    }
                })
                animator.start()
                continuation.invokeOnCancellation {
                    animator.removeAllListeners()
                    animator.cancel()
                    if (activeAnimator === animator) activeAnimator = null
                }
            }

        private fun clearActiveAnimatorListener() = object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                if (activeAnimator === animation) activeAnimator = null
            }
        }

        private fun Float.toPx(): Float = this * binding.root.resources.displayMetrics.density

        private val inputMethodManager: InputMethodManager
            get() = binding.root.context.getSystemService(InputMethodManager::class.java)

        private enum class Phase {
            INTRO,
            STAR_READY,
            INPUT,
            DESTINATION,
            COMPLETED,
        }

        private companion object {
            const val DUSK_CROSSFADE_MS = 1_500L
            const val AFTER_DUSK_PAUSE_MS = 400L
            const val INTRO_FADE_MS = 240L
            const val FIRST_INTRO_HOLD_MS = 850L
            const val SECOND_INTRO_HOLD_MS = 700L
            const val STAR_ENTRANCE_MS = 850L
            const val STAR_ENTRANCE_SCALE = 0.88f
            const val STAR_ENTRANCE_TRANSLATION_DP = 8f
            const val STAR_PULSE_MS = 3_000L
            const val INPUT_PANEL_MS = 240L
            const val INPUT_PANEL_TRANSLATION_DP = 12f
            const val CAPSULE_ENTRANCE_MS = 550L
            const val CAPSULE_START_SCALE = 0.8f
            const val CAPSULE_START_ROTATION = -2f
            const val KEEP_SETTLE_MS = 850L
            const val KEEP_SETTLE_DP = 32f
            const val SEND_DEPARTURE_MS = 1_100L
            const val SEND_RISE_DP = 52f
            const val RESULT_HOLD_MS = 1_500L
            const val ALREADY_COMPLETED_HOLD_MS = 1_300L
            const val DRAFT_DEBOUNCE_MS = 320L
            const val LEAVE_SCRIM_ALPHA = 0.72f
        }
    }
}
