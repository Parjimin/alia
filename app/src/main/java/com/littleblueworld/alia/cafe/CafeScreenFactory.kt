package com.littleblueworld.alia.cafe

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import com.littleblueworld.alia.content.BirthdayContent
import com.littleblueworld.alia.databinding.ScreenCafeBinding
import com.littleblueworld.alia.navigation.AppScreen
import com.littleblueworld.alia.navigation.ScreenId
import com.littleblueworld.alia.state.AppState

data class CafeActions(
    val goBack: () -> Unit,
    val onFirstMeaningfulInteraction: () -> Unit,
)

class CafeScreenFactory(
    context: Context,
    private val content: BirthdayContent,
) {
    private val inflater = LayoutInflater.from(context)

    fun create(
        state: AppState,
        actions: CafeActions,
    ): AppScreen = CafeScreen(
        binding = ScreenCafeBinding.inflate(inflater),
        content = content,
        alreadyDiscovered = state.cafeVisited,
        actions = actions,
    )

    private class CafeScreen(
        private val binding: ScreenCafeBinding,
        content: BirthdayContent,
        alreadyDiscovered: Boolean,
        private val actions: CafeActions,
    ) : AppScreen {
        override val id = ScreenId.CAFE
        override val view: View = binding.root

        private val tracker = CafeTapTracker(content.teaResponses, content.coffeeResponses)
        private var discoveryRecorded = alreadyDiscovered
        private var entranceAnimator: AnimatorSet? = null
        private var beverageAnimator: Animator? = null
        private var entranceFinished = false
        private var visible = false
        private var foreground = false

        init {
            binding.cafeHeader.text = content.cafeHeader
            binding.cafeForecast.text = content.cafeBody
            binding.cafeResponse.visibility = View.INVISIBLE
            binding.cafeBack.setOnClickListener { actions.goBack() }
            binding.teaChoice.setOnClickListener { onBeverageTap(BeverageKind.TEA) }
            binding.coffeeChoice.setOnClickListener { onBeverageTap(BeverageKind.COFFEE) }
            setBeverageInteractionsEnabled(false)
            prepareEntrance()
        }

        override fun render(state: AppState) = Unit

        override fun onShown() {
            visible = true
            startEntranceIfReady()
        }

        override fun onForegrounded() {
            foreground = true
            startEntranceIfReady()
        }

        override fun onBackgrounded() {
            foreground = false
            finishEntrance()
            finishBeverageFeedback()
        }

        override fun onHidden() {
            visible = false
            entranceAnimator?.removeAllListeners()
            entranceAnimator?.cancel()
            entranceAnimator = null
            beverageAnimator?.removeAllListeners()
            beverageAnimator?.cancel()
            beverageAnimator = null
            binding.cafeBack.setOnClickListener(null)
            binding.teaChoice.setOnClickListener(null)
            binding.coffeeChoice.setOnClickListener(null)
        }

        private fun prepareEntrance() {
            binding.cafeHero.apply {
                alpha = 0f
                scaleX = HERO_START_SCALE
                scaleY = HERO_START_SCALE
            }
            binding.cafeForecastPanel.alpha = 0f
            binding.cafeBeverages.alpha = 0f
        }

        private fun startEntranceIfReady() {
            if (!visible || !foreground || entranceFinished || entranceAnimator != null) return
            val heroAlpha = ObjectAnimator.ofFloat(binding.cafeHero, View.ALPHA, 0f, 1f)
            val heroScaleX = ObjectAnimator.ofFloat(
                binding.cafeHero,
                View.SCALE_X,
                HERO_START_SCALE,
                1f,
            )
            val heroScaleY = ObjectAnimator.ofFloat(
                binding.cafeHero,
                View.SCALE_Y,
                HERO_START_SCALE,
                1f,
            )
            val panelAlpha = ObjectAnimator.ofFloat(binding.cafeForecastPanel, View.ALPHA, 0f, 1f)
            val beveragesAlpha = ObjectAnimator.ofFloat(binding.cafeBeverages, View.ALPHA, 0f, 1f)
            entranceAnimator = AnimatorSet().apply {
                duration = ENTRY_DURATION_MS
                interpolator = DecelerateInterpolator()
                playTogether(heroAlpha, heroScaleX, heroScaleY, panelAlpha, beveragesAlpha)
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        if (entranceAnimator !== animation) return
                        entranceAnimator = null
                        finishEntrance()
                    }
                })
                start()
            }
        }

        private fun finishEntrance() {
            entranceAnimator?.removeAllListeners()
            entranceAnimator?.cancel()
            entranceAnimator = null
            entranceFinished = true
            binding.cafeHero.apply {
                alpha = 1f
                scaleX = 1f
                scaleY = 1f
            }
            binding.cafeForecastPanel.alpha = 1f
            binding.cafeBeverages.alpha = 1f
            setBeverageInteractionsEnabled(true)
        }

        private fun onBeverageTap(kind: BeverageKind) {
            if (!foreground || beverageAnimator != null) return
            val result = tracker.tap(kind)
            if (!discoveryRecorded && result.firstMeaningfulInteraction) {
                discoveryRecorded = true
                actions.onFirstMeaningfulInteraction()
            }

            binding.cafeResponse.apply {
                text = result.response
                visibility = View.VISIBLE
            }
            setBeverageInteractionsEnabled(false)
            val target = when (kind) {
                BeverageKind.TEA -> binding.teaChoice
                BeverageKind.COFFEE -> binding.coffeeChoice
            }
            beverageAnimator = ObjectAnimator.ofPropertyValuesHolder(
                target,
                PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, 0f, -LIFT_DP.toPx(), 0f),
                PropertyValuesHolder.ofFloat(View.ROTATION, 0f, rotationFor(kind), 0f),
                PropertyValuesHolder.ofFloat(View.SCALE_X, 1f, 1.035f, 1f),
                PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f, 1.035f, 1f),
            ).apply {
                duration = BEVERAGE_FEEDBACK_MS
                interpolator = AccelerateDecelerateInterpolator()
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        if (beverageAnimator !== animation) return
                        beverageAnimator = null
                        finishBeverageFeedback()
                    }
                })
                start()
            }
        }

        private fun finishBeverageFeedback() {
            beverageAnimator?.removeAllListeners()
            beverageAnimator?.cancel()
            beverageAnimator = null
            listOf(binding.teaChoice, binding.coffeeChoice).forEach { choice ->
                choice.translationY = 0f
                choice.rotation = 0f
                choice.scaleX = 1f
                choice.scaleY = 1f
            }
            setBeverageInteractionsEnabled(entranceFinished)
        }

        private fun setBeverageInteractionsEnabled(enabled: Boolean) {
            binding.teaChoice.isEnabled = enabled
            binding.coffeeChoice.isEnabled = enabled
        }

        private fun rotationFor(kind: BeverageKind): Float = when (kind) {
            BeverageKind.TEA -> -2.2f
            BeverageKind.COFFEE -> 2.2f
        }

        private fun Float.toPx(): Float = this * binding.root.resources.displayMetrics.density

        private companion object {
            const val HERO_START_SCALE = 0.94f
            const val ENTRY_DURATION_MS = 760L
            const val BEVERAGE_FEEDBACK_MS = 280L
            const val LIFT_DP = 6f
        }
    }
}
