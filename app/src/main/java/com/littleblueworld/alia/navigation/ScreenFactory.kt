package com.littleblueworld.alia.navigation

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.littleblueworld.alia.R
import com.littleblueworld.alia.content.BirthdayContent
import com.littleblueworld.alia.databinding.ItemTemporaryActionBinding
import com.littleblueworld.alia.databinding.ScreenTemporaryBinding
import com.littleblueworld.alia.state.AppState
import com.littleblueworld.alia.state.ExperiencePhase

data class ScreenActionSpec(
    val label: String,
    val contentDescription: String = label,
    val onClick: () -> Unit,
)

data class TemporaryScreenSpec(
    val milestone: String,
    val actions: List<ScreenActionSpec>,
    val showsBackHint: Boolean,
)

class ScreenFactory(
    private val context: Context,
    private val content: BirthdayContent,
) {
    private val inflater = LayoutInflater.from(context)

    fun create(
        id: ScreenId,
        spec: TemporaryScreenSpec,
        state: AppState,
    ): AppScreen {
        val binding = ScreenTemporaryBinding.inflate(inflater)
        binding.screenTitle.text = titleFor(id)
        binding.screenDescription.text = context.getString(
            R.string.m1_temporary_description,
            spec.milestone,
        )
        binding.backHint.visibility = if (spec.showsBackHint) View.VISIBLE else View.GONE
        bindActions(binding.actionContainer, spec.actions)

        return TemporaryScreen(id, binding).also { screen ->
            screen.render(state)
        }
    }

    private fun bindActions(
        parent: ViewGroup,
        actions: List<ScreenActionSpec>,
    ) {
        actions.forEach { action ->
            val actionBinding = ItemTemporaryActionBinding.inflate(inflater, parent, false)
            actionBinding.actionLabel.apply {
                text = action.label
                contentDescription = action.contentDescription
                setOnClickListener { action.onClick() }
            }
            parent.addView(actionBinding.root)
        }
    }

    private fun titleFor(id: ScreenId): String = when (id) {
        ScreenId.BOOT -> "PIXEL BOOT"
        ScreenId.BIRTHDAY_INTRO -> "${content.birthdayHeading}\n${content.birthdayName}"
        ScreenId.WORLD -> "LITTLE BLUE WORLD"
        ScreenId.GALLERY -> content.archiveTitle
        ScreenId.MESSAGES -> "MESSAGE BOTTLES"
        ScreenId.STARS -> "COLLECTIBLE STARS"
        ScreenId.CAFE -> "TINY CAFÉ"
        ScreenId.WISH -> content.wishEntry.last().uppercase()
        ScreenId.FINAL_MESSAGE -> "HAPPY BIRTHDAY, ALIA."
        ScreenId.AUTHOR -> content.authorTitle
    }

    private inner class TemporaryScreen(
        override val id: ScreenId,
        private val binding: ScreenTemporaryBinding,
    ) : AppScreen {
        override val view: View = binding.root

        override fun render(state: AppState) {
            val wishState = context.getString(
                if (state.wishUnlocked) R.string.m1_wish_unlocked else R.string.m1_wish_locked,
            )
            val phase = context.getString(
                when (state.experiencePhase) {
                    ExperiencePhase.FIRST_VISIT -> R.string.m1_first_visit
                    ExperiencePhase.RETURNING_PRE_FINALE -> R.string.m1_returning
                    ExperiencePhase.POST_FINALE -> R.string.m1_post_finale
                },
            )
            binding.stateSummary.text = context.getString(
                R.string.m1_state_summary,
                state.mainDiscoveryCount,
                wishState,
                phase,
            )
        }
    }
}
