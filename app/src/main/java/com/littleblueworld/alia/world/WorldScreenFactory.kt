package com.littleblueworld.alia.world

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import com.littleblueworld.alia.R
import com.littleblueworld.alia.content.BirthdayContent
import com.littleblueworld.alia.databinding.ScreenWorldBinding
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

data class WorldActions(
    val openGallery: () -> Unit,
    val openMessages: () -> Unit,
    val openStars: () -> Unit,
    val openCafe: () -> Unit,
    val onShellFound: () -> Unit,
    val onFishMilestone: (Int) -> Unit,
    val openAuthor: () -> Unit,
    val openWish: () -> Unit,
)

class WorldScreenFactory(
    context: Context,
    private val content: BirthdayContent,
) {
    private val inflater = LayoutInflater.from(context)

    fun create(
        state: AppState,
        actions: WorldActions,
        shouldCelebrateWishUnlock: Boolean,
        onFirstHintShown: () -> Unit,
    ): AppScreen {
        val binding = ScreenWorldBinding.inflate(inflater)
        return WorldScreen(
            binding = binding,
            actions = actions,
            shouldShowFirstHint = !state.worldHintSeen,
            shouldCelebrateWishUnlock = shouldCelebrateWishUnlock,
            hintText = content.worldHint.joinToString("\n"),
            lockedWishVariants = content.wishLockedVariants,
            wishUnlockText = content.wishUnlock,
            shellAlreadyFound = state.shellFound,
            shellMessage = content.shellMessage,
            shellOptionalJoke = content.shellOptionalJoke,
            initialFishMilestone = state.fishMilestone,
            postFinale = state.finaleCompleted,
            fishFirstSequence = content.fishFirstSequence,
            fishPostFinale = content.fishPostFinale,
            fishGenericLater = content.fishGenericLater,
            onFirstHintShown = onFirstHintShown,
        ).also { it.render(state) }
    }

    private class WorldScreen(
        private val binding: ScreenWorldBinding,
        private val actions: WorldActions,
        private val shouldShowFirstHint: Boolean,
        private val shouldCelebrateWishUnlock: Boolean,
        hintText: String,
        private val lockedWishVariants: List<List<String>>,
        private val wishUnlockText: String,
        shellAlreadyFound: Boolean,
        private val shellMessage: List<String>,
        private val shellOptionalJoke: String,
        initialFishMilestone: Int,
        postFinale: Boolean,
        fishFirstSequence: List<String>,
        fishPostFinale: List<String>,
        fishGenericLater: List<String>,
        private val onFirstHintShown: () -> Unit,
    ) : AppScreen {
        override val id = ScreenId.WORLD
        override val view: View = binding.root

        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        private val clouds = listOf(binding.cloudFar, binding.cloudNear)
        private val landmarks = listOf(
            binding.cafeLandmark,
            binding.cameraLandmark,
            binding.wishLandmark,
            binding.starLandmark,
            binding.bottleLandmark,
            binding.authorNoteLandmark,
            binding.shellLandmark,
        )
        private val interactiveLandmarks = listOf(
            binding.cafeLandmark,
            binding.cameraLandmark,
            binding.wishLandmark,
            binding.starLandmark,
            binding.bottleLandmark,
            binding.authorNoteLandmark,
            binding.shellLandmark,
        )
        private val motionController = WorldMotionController(
            host = binding.worldSceneLayer,
            cloudFar = binding.cloudFar,
            cloudNear = binding.cloudNear,
        )
        private val fishController = FishController(
            host = binding.fishLayer,
            fish = binding.fishTouch,
            fishArt = binding.fishArt,
            bubbles = binding.fishBubbles,
            dialogueState = FishDialogueState(
                initialMilestone = initialFishMilestone,
                firstSequence = fishFirstSequence,
                postFinaleSequence = fishPostFinale,
                genericLater = fishGenericLater,
                postFinale = postFinale,
            ),
            onDialogue = { message -> showWorldMessage(listOf(message), FISH_MESSAGE_HOLD_MS) },
            onMilestone = actions.onFishMilestone,
        )

        private var revealJob: Job? = null
        private var messageJob: Job? = null
        private var activeRevealAnimator: AnimatorSet? = null
        private var shellShimmerJob: Job? = null
        private var shellShimmerAnimator: AnimatorSet? = null
        private var shellTapAnimator: AnimatorSet? = null
        private var revealStep = 0
        private var lockedMessageIndex = 0
        private var hintMarked = false
        private var cloudsIntroduced = false
        private var landmarksIntroduced = false
        private var wishPointMode = WishPointMode.LOCKED
        private var shellFound = shellAlreadyFound
        private var isVisible = false
        private var isForeground = false

        init {
            clouds.forEach { it.alpha = 0f }
            landmarks.forEach { it.alpha = 0f }
            setInteractionsEnabled(false)
            bindLandmarkActions()

            binding.worldHint.text = hintText
            binding.worldHint.alpha = 0f
            binding.worldHint.visibility = if (shouldShowFirstHint) View.INVISIBLE else View.GONE
            binding.worldMessage.alpha = 0f
            binding.worldMessage.visibility = View.GONE
        }

        override fun render(state: AppState) {
            val progression = WorldProgression.from(state)
            wishPointMode = progression.wishPointMode
            binding.root.isActivated = progression.mode == WorldMode.POST_FINALE
            binding.wishMarker.alpha = when (progression.wishPointMode) {
                WishPointMode.LOCKED -> LOCKED_WISH_ALPHA
                WishPointMode.UNLOCKED -> 1f
                WishPointMode.COMPLETED -> COMPLETED_WISH_ALPHA
            }
            binding.cameraVisitedMarker.shown(progression.galleryVisited)
            binding.bottleVisitedMarker.shown(progression.messagesVisited)
            binding.starVisitedMarker.shown(progression.starsVisited)
            binding.cafeVisitedMarker.shown(progression.cafeVisited)
            shellFound = state.shellFound
            fishController.update(state.fishMilestone, state.finaleCompleted)
        }

        override fun onShown() {
            isVisible = true
            startIfReady()
        }

        override fun onForegrounded() {
            isForeground = true
            if (cloudsIntroduced) motionController.startOrResume()
            if (landmarksIntroduced) binding.ambientEffects.start()
            if (landmarksIntroduced) startShellShimmerIfReady()
            if (landmarksIntroduced) fishController.start()
            startIfReady()
        }

        override fun onBackgrounded() {
            isForeground = false
            stopReveal()
            stopMessage()
            motionController.pause()
            binding.ambientEffects.stop()
            stopShellMotion()
            fishController.stop()
        }

        override fun onHidden() {
            isVisible = false
            stopReveal()
            stopMessage()
            setInteractionsEnabled(false)
            interactiveLandmarks.forEach {
                it.animate().cancel()
                it.setOnClickListener(null)
            }
            motionController.clear()
            binding.ambientEffects.stop()
            stopShellMotion()
            fishController.clear()
            scope.cancel()
        }

        private fun bindLandmarkActions() {
            bindLandmark(
                binding.cameraLandmark,
                R.string.world_camera_description,
                LandmarkFeedbackProfile(pressedScale = 0.96f),
                actions.openGallery,
            )
            bindLandmark(
                binding.bottleLandmark,
                R.string.world_bottle_description,
                LandmarkFeedbackProfile(pressedScale = 0.97f, pressedLiftDp = 3f),
                actions.openMessages,
            )
            bindLandmark(
                binding.starLandmark,
                R.string.world_star_description,
                LandmarkFeedbackProfile(pressedScale = 0.94f),
                actions.openStars,
            )
            bindLandmark(
                binding.cafeLandmark,
                R.string.world_cafe_description,
                LandmarkFeedbackProfile(pressedScale = 0.98f),
                actions.openCafe,
            )
            bindLandmark(
                binding.shellLandmark,
                R.string.world_shell_description,
                LandmarkFeedbackProfile(pressedScale = 1.02f),
                ::handleShellTap,
            )
            bindLandmark(
                binding.authorNoteLandmark,
                R.string.world_author_description,
                LandmarkFeedbackProfile(pressedScale = 0.97f, pressedLiftDp = 2f),
                actions.openAuthor,
            )
            bindLandmark(
                binding.wishLandmark,
                R.string.world_wish_description,
                LandmarkFeedbackProfile(pressedScale = 0.96f),
                ::handleWishTap,
            )
        }

        private fun bindLandmark(
            view: View,
            descriptionRes: Int,
            feedback: LandmarkFeedbackProfile,
            action: () -> Unit,
        ) {
            view.contentDescription = binding.root.context.getString(descriptionRes)
            view.isClickable = true
            view.isFocusable = true
            view.installLandmarkTouchFeedback(feedback)
            view.setOnClickListener { action() }
        }

        private fun handleWishTap() {
            when (wishPointMode) {
                WishPointMode.LOCKED -> showLockedWishMessage()
                WishPointMode.UNLOCKED -> actions.openWish()
                WishPointMode.COMPLETED -> Unit
            }
        }

        private fun showLockedWishMessage() {
            if (!isForeground || lockedWishVariants.isEmpty()) return
            val message = lockedWishVariants[lockedMessageIndex % lockedWishVariants.size]
            lockedMessageIndex += 1
            showWorldMessage(message, MESSAGE_HOLD_MS)
        }

        private fun showWorldMessage(message: List<String>, holdMillis: Long) {
            stopMessage()
            messageJob = scope.launch {
                binding.worldMessage.text = message.joinToString("\n")
                binding.worldMessage.visibility = View.VISIBLE
                binding.worldMessage.fadeTo(1f, MESSAGE_REVEAL_MS)
                delay(holdMillis)
                binding.worldMessage.fadeTo(0f, MESSAGE_HIDE_MS)
                binding.worldMessage.visibility = View.GONE
            }
        }

        private fun handleShellTap() {
            if (!isForeground || shellTapAnimator != null) return
            val wasAlreadyFound = shellFound
            if (!wasAlreadyFound) {
                shellFound = true
                actions.onShellFound()
            }

            showWorldMessage(
                message = ShellRevealPolicy.message(
                    alreadyFound = wasAlreadyFound,
                    firstReveal = shellMessage,
                    optionalJoke = shellOptionalJoke,
                ),
                holdMillis = if (wasAlreadyFound) MESSAGE_HOLD_MS else SHELL_MESSAGE_HOLD_MS,
            )
            binding.ambientEffects.burstAt(SHELL_CENTER_X, SHELL_CENTER_Y)
            stopShellShimmer()
            binding.shellLandmark.animate().cancel()
            binding.shellLandmark.translationY = 0f

            val lift = ObjectAnimator.ofPropertyValuesHolder(
                binding.shellLandmark,
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, SHELL_TAP_SCALE),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, SHELL_TAP_SCALE),
                PropertyValuesHolder.ofFloat(View.ROTATION, 0f, SHELL_TAP_ROTATION),
            ).apply { duration = SHELL_TAP_RISE_MS }
            val settle = ObjectAnimator.ofPropertyValuesHolder(
                binding.shellLandmark,
                PropertyValuesHolder.ofFloat(View.SCALE_X, SHELL_TAP_SCALE, 1f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, SHELL_TAP_SCALE, 1f),
                PropertyValuesHolder.ofFloat(View.ROTATION, SHELL_TAP_ROTATION, 0f),
            ).apply { duration = SHELL_TAP_SETTLE_MS }
            shellTapAnimator = AnimatorSet().apply {
                interpolator = AccelerateDecelerateInterpolator()
                playSequentially(lift, settle)
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        if (shellTapAnimator !== animation) return
                        shellTapAnimator = null
                        resetShellTransform()
                        startShellShimmerIfReady()
                    }
                })
                start()
            }
        }

        private fun startIfReady() {
            if (!isVisible || !isForeground || revealStep >= REVEAL_COMPLETE) return
            if (revealJob?.isActive == true) return
            revealJob = scope.launch { revealWorld() }
        }

        private suspend fun revealWorld() {
            while (revealStep < REVEAL_COMPLETE && scope.isActive) {
                when (revealStep) {
                    0 -> {
                        delay(BACKGROUND_SETTLE_MS)
                        fadeTogether(clouds, CLOUD_REVEAL_MS)
                        cloudsIntroduced = true
                        motionController.startOrResume()
                        delay(AFTER_CLOUDS_MS)
                    }

                    1 -> {
                        fadeTogether(landmarks, LANDMARK_REVEAL_MS)
                        landmarksIntroduced = true
                        binding.ambientEffects.start()
                        startShellShimmerIfReady()
                        fishController.start()
                        if (!shouldCelebrateWishUnlock) setInteractionsEnabled(true)
                        delay(BEFORE_HINT_MS)
                    }

                    2 -> {
                        celebrateWishUnlockIfNeeded()
                        setInteractionsEnabled(true)
                        revealHintIfNeeded()
                    }
                }
                revealStep += 1
            }
        }

        private suspend fun celebrateWishUnlockIfNeeded() {
            if (!shouldCelebrateWishUnlock || wishPointMode != WishPointMode.UNLOCKED) return

            binding.worldMessage.text = wishUnlockText
            binding.worldMessage.visibility = View.VISIBLE
            binding.worldMessage.alpha = 0f
            binding.wishMarker.alpha = UNLOCK_START_ALPHA
            binding.ambientEffects.burstAt(WISH_CENTER_X, WISH_CENTER_Y)

            runAnimator(
                AnimatorSet().apply {
                    playTogether(
                        ObjectAnimator.ofFloat(binding.worldDim, View.ALPHA, 0f, UNLOCK_DIM_ALPHA),
                        ObjectAnimator.ofFloat(binding.worldMessage, View.ALPHA, 0f, 1f),
                        ObjectAnimator.ofFloat(binding.wishLandmark, View.SCALE_X, 1f, 1.14f),
                        ObjectAnimator.ofFloat(binding.wishLandmark, View.SCALE_Y, 1f, 1.14f),
                        ObjectAnimator.ofFloat(binding.wishMarker, View.ALPHA, UNLOCK_START_ALPHA, 1f),
                    )
                    duration = UNLOCK_ENTER_MS
                    interpolator = AccelerateDecelerateInterpolator()
                },
            )
            delay(UNLOCK_HOLD_MS)
            runAnimator(
                AnimatorSet().apply {
                    playTogether(
                        ObjectAnimator.ofFloat(binding.worldDim, View.ALPHA, UNLOCK_DIM_ALPHA, 0f),
                        ObjectAnimator.ofFloat(binding.worldMessage, View.ALPHA, 1f, 0f),
                        ObjectAnimator.ofFloat(binding.wishLandmark, View.SCALE_X, 1.14f, 1f),
                        ObjectAnimator.ofFloat(binding.wishLandmark, View.SCALE_Y, 1.14f, 1f),
                    )
                    duration = UNLOCK_EXIT_MS
                    interpolator = AccelerateDecelerateInterpolator()
                },
            )
            binding.worldMessage.visibility = View.GONE
        }

        private suspend fun revealHintIfNeeded() {
            if (!shouldShowFirstHint) return
            binding.worldHint.visibility = View.VISIBLE
            fadeTogether(listOf(binding.worldHint), HINT_REVEAL_MS)
            if (!hintMarked) {
                hintMarked = true
                onFirstHintShown()
            }
            delay(HINT_HOLD_MS)
            fadeTogether(listOf(binding.worldHint), HINT_HIDE_MS, targetAlpha = 0f)
            binding.worldHint.visibility = View.GONE
        }

        private fun setInteractionsEnabled(enabled: Boolean) {
            interactiveLandmarks.forEach { it.isEnabled = enabled }
        }

        private fun stopReveal() {
            revealJob?.cancel()
            revealJob = null
            activeRevealAnimator?.cancel()
            activeRevealAnimator = null
        }

        private fun stopMessage() {
            messageJob?.cancel()
            messageJob = null
            binding.worldMessage.animate().cancel()
            if (activeRevealAnimator == null) {
                binding.worldMessage.alpha = 0f
                binding.worldMessage.visibility = View.GONE
            }
        }

        private fun startShellShimmerIfReady() {
            if (!isVisible || !isForeground || !landmarksIntroduced) return
            if (shellShimmerJob?.isActive == true || shellTapAnimator != null) return
            shellShimmerJob = scope.launch {
                delay(if (shellFound) SHELL_FOUND_INITIAL_DELAY_MS else SHELL_INITIAL_DELAY_MS)
                while (isActive && isForeground) {
                    runShellShimmer()
                    delay(SHELL_SHIMMER_INTERVAL_MS)
                }
            }
        }

        private suspend fun runShellShimmer() =
            suspendCancellableCoroutine { continuation ->
                val animator = AnimatorSet().apply {
                    duration = SHELL_SHIMMER_MS
                    interpolator = AccelerateDecelerateInterpolator()
                    playTogether(
                        ObjectAnimator.ofFloat(binding.shellLandmark, View.ALPHA, 1f, 0.72f, 1f),
                        ObjectAnimator.ofFloat(
                            binding.shellLandmark,
                            View.SCALE_X,
                            1f,
                            SHELL_SHIMMER_SCALE,
                            1f,
                        ),
                        ObjectAnimator.ofFloat(
                            binding.shellLandmark,
                            View.SCALE_Y,
                            1f,
                            SHELL_SHIMMER_SCALE,
                            1f,
                        ),
                    )
                }
                shellShimmerAnimator = animator
                animator.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        if (shellShimmerAnimator === animation) shellShimmerAnimator = null
                        resetShellTransform()
                        if (continuation.isActive) continuation.resume(Unit)
                    }
                })
                animator.start()
                continuation.invokeOnCancellation {
                    animator.removeAllListeners()
                    animator.cancel()
                    if (shellShimmerAnimator === animator) shellShimmerAnimator = null
                    resetShellTransform()
                }
            }

        private fun stopShellShimmer() {
            shellShimmerJob?.cancel()
            shellShimmerJob = null
            shellShimmerAnimator?.removeAllListeners()
            shellShimmerAnimator?.cancel()
            shellShimmerAnimator = null
            resetShellTransform()
        }

        private fun stopShellMotion() {
            stopShellShimmer()
            shellTapAnimator?.removeAllListeners()
            shellTapAnimator?.cancel()
            shellTapAnimator = null
            resetShellTransform()
        }

        private fun resetShellTransform() {
            binding.shellLandmark.apply {
                alpha = if (landmarksIntroduced) 1f else alpha
                scaleX = 1f
                scaleY = 1f
                rotation = 0f
                translationY = 0f
            }
        }

        private suspend fun fadeTogether(
            views: List<View>,
            durationMillis: Long,
            targetAlpha: Float = 1f,
        ) = runAnimator(
            AnimatorSet().apply {
                playTogether(
                    views.map { view ->
                        ObjectAnimator.ofFloat(view, View.ALPHA, view.alpha, targetAlpha)
                    },
                )
                duration = durationMillis
                interpolator = AccelerateDecelerateInterpolator()
            },
        )

        private suspend fun runAnimator(animator: AnimatorSet) =
            suspendCancellableCoroutine { continuation ->
                animator.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        activeRevealAnimator = null
                        if (continuation.isActive) continuation.resume(Unit)
                    }
                })
                activeRevealAnimator = animator
                animator.start()
                continuation.invokeOnCancellation {
                    animator.removeAllListeners()
                    animator.cancel()
                    if (activeRevealAnimator === animator) activeRevealAnimator = null
                }
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

        private fun View.shown(shown: Boolean) {
            visibility = if (shown) View.VISIBLE else View.GONE
        }

        private companion object {
            const val REVEAL_COMPLETE = 3
            const val LOCKED_WISH_ALPHA = 0.62f
            const val COMPLETED_WISH_ALPHA = 0.78f
            const val UNLOCK_START_ALPHA = 0.7f
            const val UNLOCK_DIM_ALPHA = 0.16f
            const val WISH_CENTER_X = 0.5f
            const val WISH_CENTER_Y = 0.2f
            const val SHELL_CENTER_X = 0.17f
            const val SHELL_CENTER_Y = 0.73f
            const val SHELL_TAP_SCALE = 1.06f
            const val SHELL_TAP_ROTATION = 3f
            const val SHELL_SHIMMER_SCALE = 1.025f

            const val BACKGROUND_SETTLE_MS = 150L
            const val CLOUD_REVEAL_MS = 420L
            const val AFTER_CLOUDS_MS = 150L
            const val LANDMARK_REVEAL_MS = 480L
            const val BEFORE_HINT_MS = 300L
            const val HINT_REVEAL_MS = 320L
            const val HINT_HOLD_MS = 4_200L
            const val HINT_HIDE_MS = 320L
            const val MESSAGE_REVEAL_MS = 180L
            const val MESSAGE_HOLD_MS = 1_600L
            const val SHELL_MESSAGE_HOLD_MS = 3_200L
            const val FISH_MESSAGE_HOLD_MS = 1_700L
            const val MESSAGE_HIDE_MS = 240L
            const val SHELL_TAP_RISE_MS = 130L
            const val SHELL_TAP_SETTLE_MS = 190L
            const val SHELL_SHIMMER_MS = 560L
            const val SHELL_INITIAL_DELAY_MS = 3_800L
            const val SHELL_FOUND_INITIAL_DELAY_MS = 6_200L
            const val SHELL_SHIMMER_INTERVAL_MS = 8_200L
            const val UNLOCK_ENTER_MS = 500L
            const val UNLOCK_HOLD_MS = 500L
            const val UNLOCK_EXIT_MS = 600L
        }
    }
}
