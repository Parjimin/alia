package com.littleblueworld.alia.stars

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.animation.DecelerateInterpolator
import com.littleblueworld.alia.R
import com.littleblueworld.alia.content.BirthdayContent
import com.littleblueworld.alia.databinding.ScreenStarsBinding
import com.littleblueworld.alia.navigation.AppScreen
import com.littleblueworld.alia.navigation.ScreenId
import com.littleblueworld.alia.state.AppState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class StarActions(
    val goBack: () -> Unit,
    val onFirstMeaningfulInteraction: () -> Unit,
)

class StarScreenFactory(
    context: Context,
    private val content: BirthdayContent,
) {
    private val inflater = LayoutInflater.from(context)

    fun create(
        state: AppState,
        actions: StarActions,
    ): AppScreen = StarScreen(
        binding = ScreenStarsBinding.inflate(inflater),
        content = content,
        alreadyDiscovered = state.starsVisited,
        actions = actions,
    )

    private class StarScreen(
        private val binding: ScreenStarsBinding,
        private val content: BirthdayContent,
        alreadyDiscovered: Boolean,
        private val actions: StarActions,
    ) : AppScreen {
        override val id = ScreenId.STARS
        override val view: View = binding.root

        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        private val tracker = StarCollectionTracker()
        private val starViews = listOf(binding.starEnergy, binding.starMind, binding.starFace)
        private val kinds = StarKind.entries
        private val pulseController = StarPulseController(starViews)

        private var collectionAnimator: AnimatorSet? = null
        private var panelAnimator: AnimatorSet? = null
        private var faceRevealJob: Job? = null
        private var pendingCollection: StarKind? = null
        private var panelMode = PanelMode.NONE
        private var currentPanelKind: StarKind? = null
        private var discoveryRecorded = alreadyDiscovered
        private var visible = false
        private var foreground = false

        init {
            require(content.faceReveal.size == 5)
            binding.starEnergyLabel.text = content.energyTitle
            binding.starMindLabel.text = content.mindTitle
            binding.starFaceLabel.text = content.faceReveal.first()
            binding.starOverlay.visibility = View.GONE
            binding.starsBack.setOnClickListener { actions.goBack() }
            binding.starScrim.setOnClickListener { closeContentPanel() }
            binding.starPanelClose.setOnClickListener { closeContentPanel() }
            binding.starCompletionCta.setOnClickListener { actions.goBack() }
            starViews.forEachIndexed { index, star ->
                star.setOnClickListener { collect(kinds[index]) }
            }
            renderStarStates()
        }

        override fun render(state: AppState) = Unit

        override fun onShown() {
            visible = true
            startPulsesIfReady()
        }

        override fun onForegrounded() {
            foreground = true
            if (panelMode == PanelMode.NONE && collectionAnimator == null) startPulsesIfReady()
        }

        override fun onBackgrounded() {
            foreground = false
            val interruptedCollection = pendingCollection
            cancelCollectionAnimator()
            if (interruptedCollection != null) finishCollection(interruptedCollection, immediate = true)
            faceRevealJob?.cancel()
            faceRevealJob = null
            if (currentPanelKind == StarKind.FACE && panelMode == PanelMode.CONTENT) finishFaceReveal()
            panelAnimator?.cancel()
            panelAnimator = null
            finishPanelEntrance()
            pulseController.stop()
            binding.starBurst.stop()
        }

        override fun onHidden() {
            visible = false
            cancelCollectionAnimator()
            panelAnimator?.cancel()
            panelAnimator = null
            faceRevealJob?.cancel()
            faceRevealJob = null
            pulseController.stop()
            binding.starBurst.stop()
            binding.starsBack.setOnClickListener(null)
            binding.starScrim.setOnClickListener(null)
            binding.starPanelClose.setOnClickListener(null)
            binding.starCompletionCta.setOnClickListener(null)
            starViews.forEach { it.setOnClickListener(null) }
            scope.cancel()
        }

        private fun collect(kind: StarKind) {
            if (panelMode != PanelMode.NONE || collectionAnimator != null) return
            val result = tracker.collect(kind)
            if (!result.accepted) return
            if (!discoveryRecorded) {
                discoveryRecorded = true
                actions.onFirstMeaningfulInteraction()
            }

            pendingCollection = kind
            pulseController.stop()
            setStarInteractionsEnabled(false)
            val star = starViews[kind.ordinal]
            val contract = scaleAnimator(star, star.scaleX, CONTRACT_SCALE, CONTRACT_DURATION_MS)
            val expand = scaleAnimator(star, CONTRACT_SCALE, EXPAND_SCALE, EXPAND_DURATION_MS)
            val settleScale = scaleAnimator(star, EXPAND_SCALE, COLLECTED_SCALE, SETTLE_DURATION_MS)
            val settleAlpha = ObjectAnimator.ofFloat(star, View.ALPHA, 1f, COLLECTED_ALPHA).apply {
                duration = SETTLE_DURATION_MS
            }
            val settle = AnimatorSet().apply { playTogether(settleScale, settleAlpha) }
            var cancelled = false
            collectionAnimator = AnimatorSet().apply {
                playSequentially(contract, expand, settle)
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationCancel(animation: Animator) {
                        cancelled = true
                    }

                    override fun onAnimationEnd(animation: Animator) {
                        if (cancelled || collectionAnimator !== animation) return
                        collectionAnimator = null
                        pendingCollection = null
                        finishCollection(kind, immediate = false)
                    }
                })
                start()
            }
        }

        private fun finishCollection(kind: StarKind, immediate: Boolean) {
            pendingCollection = null
            renderStarStates()
            if (!immediate) {
                val star = starViews[kind.ordinal]
                val starLocation = IntArray(2)
                val burstLocation = IntArray(2)
                star.getLocationInWindow(starLocation)
                binding.starBurst.getLocationInWindow(burstLocation)
                binding.starBurst.burstAt(
                    starLocation[0] - burstLocation[0] + star.width / 2f,
                    starLocation[1] - burstLocation[1] + star.height / 2f,
                )
            }
            showContentPanel(kind, immediate)
        }

        private fun showContentPanel(kind: StarKind, immediate: Boolean) {
            panelMode = PanelMode.CONTENT
            currentPanelKind = kind
            binding.starCompletionCta.visibility = View.GONE
            binding.starPanelClose.visibility = if (kind == StarKind.FACE) View.INVISIBLE else View.VISIBLE
            when (kind) {
                StarKind.ENERGY -> {
                    binding.starPanelTitle.text = content.energyTitle
                    binding.starPanelBody.text = content.energyBody
                }

                StarKind.MIND -> {
                    binding.starPanelTitle.text = content.mindTitle
                    binding.starPanelBody.text = content.mindBody
                }

                StarKind.FACE -> {
                    val timeline = FaceRevealTimeline.from(content.faceReveal)
                    binding.starPanelTitle.text = timeline.first().text
                    binding.starPanelBody.text = ""
                }
            }
            showPanelEntrance(immediate)
            if (kind == StarKind.FACE) {
                if (immediate || !foreground) finishFaceReveal() else startFaceReveal()
            }
        }

        private fun startFaceReveal() {
            faceRevealJob?.cancel()
            val timeline = FaceRevealTimeline.from(content.faceReveal)
            faceRevealJob = scope.launch {
                val revealed = mutableListOf<String>()
                timeline.drop(1).forEach { step ->
                    delay(step.delayAfterPreviousMs)
                    revealed += step.text
                    binding.starPanelBody.text = revealed.joinToString("\n\n")
                }
                binding.starPanelClose.visibility = View.VISIBLE
                faceRevealJob = null
            }
        }

        private fun finishFaceReveal() {
            faceRevealJob?.cancel()
            faceRevealJob = null
            binding.starPanelTitle.text = content.faceReveal.first()
            binding.starPanelBody.text = content.faceReveal.drop(1).joinToString("\n\n")
            binding.starPanelClose.visibility = View.VISIBLE
        }

        private fun closeContentPanel(): Boolean {
            if (panelMode != PanelMode.CONTENT || faceRevealJob?.isActive == true) return false
            if (tracker.completed) {
                showCompletion()
            } else {
                hidePanel()
            }
            return true
        }

        private fun showCompletion() {
            panelMode = PanelMode.COMPLETION
            currentPanelKind = null
            binding.starPanelTitle.text = content.starsCompletion[0]
            binding.starPanelBody.text = content.starsCompletion[1]
            binding.starPanelClose.visibility = View.GONE
            binding.starCompletionCta.apply {
                text = content.starsCta
                visibility = View.VISIBLE
            }
        }

        private fun hidePanel() {
            panelAnimator?.cancel()
            panelAnimator = null
            panelMode = PanelMode.NONE
            currentPanelKind = null
            binding.starOverlay.visibility = View.GONE
            binding.starCompletionCta.visibility = View.GONE
            renderStarStates()
            startPulsesIfReady()
        }

        private fun showPanelEntrance(immediate: Boolean) {
            panelAnimator?.cancel()
            binding.starOverlay.visibility = View.VISIBLE
            binding.starScrim.alpha = if (immediate) SCRIM_ALPHA else 0f
            binding.starPanel.apply {
                alpha = if (immediate) 1f else 0f
                scaleX = if (immediate) 1f else PANEL_START_SCALE
                scaleY = if (immediate) 1f else PANEL_START_SCALE
            }
            if (immediate) return

            val scrim = ObjectAnimator.ofFloat(binding.starScrim, View.ALPHA, 0f, SCRIM_ALPHA)
            val alpha = ObjectAnimator.ofFloat(binding.starPanel, View.ALPHA, 0f, 1f)
            val scaleX = ObjectAnimator.ofFloat(binding.starPanel, View.SCALE_X, PANEL_START_SCALE, 1f)
            val scaleY = ObjectAnimator.ofFloat(binding.starPanel, View.SCALE_Y, PANEL_START_SCALE, 1f)
            panelAnimator = AnimatorSet().apply {
                duration = PANEL_DURATION_MS
                interpolator = DecelerateInterpolator()
                playTogether(scrim, alpha, scaleX, scaleY)
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        if (panelAnimator !== animation) return
                        panelAnimator = null
                        finishPanelEntrance()
                    }
                })
                start()
            }
        }

        private fun finishPanelEntrance() {
            if (panelMode == PanelMode.NONE) return
            binding.starScrim.alpha = SCRIM_ALPHA
            binding.starPanel.apply {
                alpha = 1f
                scaleX = 1f
                scaleY = 1f
            }
        }

        private fun renderStarStates() {
            kinds.forEachIndexed { index, kind ->
                val star = starViews[index]
                val collected = tracker.isCollected(kind)
                star.apply {
                    alpha = if (collected) COLLECTED_ALPHA else 1f
                    scaleX = if (collected) COLLECTED_SCALE else 1f
                    scaleY = if (collected) COLLECTED_SCALE else 1f
                    isEnabled = !collected && panelMode == PanelMode.NONE && collectionAnimator == null
                    isClickable = isEnabled
                    contentDescription = context.getString(
                        if (collected) R.string.star_collected_description else R.string.star_collect_description,
                        starTitle(kind),
                    )
                }
            }
        }

        private fun startPulsesIfReady() {
            if (!visible || !foreground || panelMode != PanelMode.NONE || collectionAnimator != null) return
            renderStarStates()
            pulseController.start(kinds.filterNot(tracker::isCollected).toSet())
        }

        private fun setStarInteractionsEnabled(enabled: Boolean) {
            starViews.forEachIndexed { index, star ->
                val canInteract = enabled && !tracker.isCollected(kinds[index])
                star.isEnabled = canInteract
                star.isClickable = canInteract
            }
        }

        private fun starTitle(kind: StarKind): String = when (kind) {
            StarKind.ENERGY -> content.energyTitle
            StarKind.MIND -> content.mindTitle
            StarKind.FACE -> content.faceReveal.first()
        }

        private fun scaleAnimator(view: View, from: Float, to: Float, durationMs: Long) =
            AnimatorSet().apply {
                duration = durationMs
                interpolator = DecelerateInterpolator()
                playTogether(
                    ObjectAnimator.ofFloat(view, View.SCALE_X, from, to),
                    ObjectAnimator.ofFloat(view, View.SCALE_Y, from, to),
                )
            }

        private fun cancelCollectionAnimator() {
            collectionAnimator?.removeAllListeners()
            collectionAnimator?.cancel()
            collectionAnimator = null
            pendingCollection = null
        }

        private enum class PanelMode {
            NONE,
            CONTENT,
            COMPLETION,
        }

        private companion object {
            const val SCRIM_ALPHA = 0.66f
            const val PANEL_START_SCALE = 0.94f
            const val CONTRACT_SCALE = 0.78f
            const val EXPAND_SCALE = 1.10f
            const val COLLECTED_SCALE = 0.90f
            const val COLLECTED_ALPHA = 0.38f
            const val CONTRACT_DURATION_MS = 100L
            const val EXPAND_DURATION_MS = 150L
            const val SETTLE_DURATION_MS = 180L
            const val PANEL_DURATION_MS = 220L
        }
    }
}
