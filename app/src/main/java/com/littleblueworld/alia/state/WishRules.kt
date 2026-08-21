package com.littleblueworld.alia.state

import java.util.UUID

object WishRules {
    const val MAX_LENGTH = 500
    const val HOLD_DURATION_MS = 1_350L
    const val HOLD_PROGRESS_BLOCKS = 10

    fun sanitizeDraft(draft: String): String = draft.take(MAX_LENGTH)

    fun draftState(draft: String): WishState =
        if (draft.isBlank()) WishState.NONE else WishState.DRAFT

    fun canEdit(state: WishState): Boolean = state == WishState.NONE || state == WishState.DRAFT

    fun canSeal(
        state: WishState,
        draft: String,
    ): Boolean = canEdit(state) && draft.isNotBlank()

    fun canChooseDestination(state: WishState): Boolean = state == WishState.SEALED

    fun isComplete(state: WishState): Boolean = state == WishState.KEPT_LOCAL || state == WishState.SENT

    fun isValidRequestId(value: String): Boolean = runCatching {
        UUID.fromString(value).toString() == value
    }.getOrDefault(false)
}
