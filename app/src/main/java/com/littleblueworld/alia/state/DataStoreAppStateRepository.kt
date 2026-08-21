package com.littleblueworld.alia.state

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class DataStoreAppStateRepository(
    private val dataStore: DataStore<Preferences>,
) : AppStateRepository {

    override val state: Flow<AppState> = dataStore.data
        .catch { error ->
            if (error is IOException) {
                emit(emptyPreferences())
            } else {
                throw error
            }
        }
        .map(::toAppState)
        .distinctUntilChanged()

    override suspend fun markOpened() {
        dataStore.edit { preferences ->
            preferences[Keys.HAS_OPENED_BEFORE] = true
        }
    }

    override suspend fun markWorldHintSeen() {
        dataStore.edit { preferences ->
            preferences[Keys.WORLD_HINT_SEEN] = true
        }
    }

    override suspend fun markMainDiscovery(discovery: MainDiscovery) {
        dataStore.edit { preferences ->
            preferences[discovery.preferenceKey] = true

            val discoveryCount = MainDiscovery.entries.count { item ->
                item == discovery || preferences[item.preferenceKey] == true
            }
            if (discoveryCount >= WISH_UNLOCK_THRESHOLD) {
                preferences[Keys.WISH_UNLOCKED] = true
            }
        }
    }

    override suspend fun markShellFound() {
        dataStore.edit { preferences ->
            preferences[Keys.SHELL_FOUND] = true
        }
    }

    override suspend fun updateFishMilestone(milestone: Int) {
        dataStore.edit { preferences ->
            val current = preferences[Keys.FISH_MILESTONE] ?: 0
            preferences[Keys.FISH_MILESTONE] = maxOf(
                current,
                milestone.coerceIn(0, MAX_FIRST_FISH_MILESTONE),
            )
        }
    }

    override suspend fun updateWishDraft(draft: String) {
        dataStore.edit { preferences ->
            val currentState = preferences.wishState
            if (!WishRules.canEdit(currentState)) return@edit
            val sanitized = WishRules.sanitizeDraft(draft).takeIf(String::isNotBlank).orEmpty()
            preferences[Keys.WISH_DRAFT] = sanitized
            preferences[Keys.WISH_STATE] = WishRules.draftState(sanitized).name
        }
    }

    override suspend fun sealWish(): Boolean {
        var accepted = false
        dataStore.edit { preferences ->
            val draft = WishRules.sanitizeDraft(preferences[Keys.WISH_DRAFT].orEmpty())
            if (!WishRules.canSeal(preferences.wishState, draft)) return@edit
            preferences[Keys.WISH_DRAFT] = draft
            preferences[Keys.WISH_STATE] = WishState.SEALED.name
            accepted = true
        }
        return accepted
    }

    override suspend fun keepWishLocal(): Boolean {
        var accepted = false
        dataStore.edit { preferences ->
            if (!WishRules.canChooseDestination(preferences.wishState)) return@edit
            preferences[Keys.WISH_STATE] = WishState.KEPT_LOCAL.name
            preferences.remove(Keys.PENDING_WISH_REQUEST_ID)
            preferences.remove(Keys.PENDING_WISH_MESSAGE)
            preferences.remove(Keys.PENDING_WISH_RETRY_ENABLED)
            accepted = true
        }
        return accepted
    }

    override suspend fun prepareWishSend(newRequestId: String): PendingWish? {
        var pendingWish: PendingWish? = null
        dataStore.edit { preferences ->
            when (preferences.wishState) {
                WishState.SEALED -> {
                    val message = WishRules.sanitizeDraft(
                        preferences[Keys.WISH_DRAFT].orEmpty(),
                    )
                    if (message.isBlank() || !WishRules.isValidRequestId(newRequestId)) {
                        return@edit
                    }
                    pendingWish = PendingWish(
                        requestId = newRequestId,
                        message = message,
                    )
                    preferences[Keys.WISH_STATE] = WishState.PENDING_SEND.name
                    preferences[Keys.PENDING_WISH_REQUEST_ID] = newRequestId
                    preferences[Keys.PENDING_WISH_MESSAGE] = message
                    preferences[Keys.PENDING_WISH_RETRY_ENABLED] = true
                }

                WishState.PENDING_SEND -> {
                    val requestId = preferences[Keys.PENDING_WISH_REQUEST_ID]
                    val message = preferences[Keys.PENDING_WISH_MESSAGE]
                    if (
                        requestId != null &&
                        message != null &&
                        WishRules.isValidRequestId(requestId) &&
                        message.isNotBlank()
                    ) {
                        pendingWish = PendingWish(requestId, message)
                    }
                }

                else -> Unit
            }
        }
        return pendingWish
    }

    override suspend fun markWishSent(requestId: String): Boolean {
        var accepted = false
        dataStore.edit { preferences ->
            if (
                preferences.wishState != WishState.PENDING_SEND ||
                preferences[Keys.PENDING_WISH_REQUEST_ID] != requestId
            ) {
                return@edit
            }
            preferences[Keys.WISH_STATE] = WishState.SENT.name
            preferences.remove(Keys.PENDING_WISH_REQUEST_ID)
            preferences.remove(Keys.PENDING_WISH_MESSAGE)
            preferences.remove(Keys.PENDING_WISH_RETRY_ENABLED)
            accepted = true
        }
        return accepted
    }

    override suspend fun markWishPermanentFailure(requestId: String): Boolean {
        var accepted = false
        dataStore.edit { preferences ->
            if (
                preferences.wishState != WishState.PENDING_SEND ||
                preferences[Keys.PENDING_WISH_REQUEST_ID] != requestId
            ) {
                return@edit
            }
            preferences[Keys.PENDING_WISH_RETRY_ENABLED] = false
            accepted = true
        }
        return accepted
    }

    override suspend fun markFinaleCompleted() {
        dataStore.edit { preferences ->
            preferences[Keys.FINALE_COMPLETED] = true
            preferences[Keys.HAS_OPENED_BEFORE] = true
        }
    }

    private fun toAppState(preferences: Preferences): AppState {
        val galleryVisited = preferences[Keys.GALLERY_VISITED] ?: false
        val messagesVisited = preferences[Keys.MESSAGES_VISITED] ?: false
        val starsVisited = preferences[Keys.STARS_VISITED] ?: false
        val cafeVisited = preferences[Keys.CAFE_VISITED] ?: false
        val discoveryCount = listOf(
            galleryVisited,
            messagesVisited,
            starsVisited,
            cafeVisited,
        ).count { it }

        return AppState(
            hasOpenedBefore = preferences[Keys.HAS_OPENED_BEFORE] ?: false,
            worldHintSeen = preferences[Keys.WORLD_HINT_SEEN] ?: false,
            galleryVisited = galleryVisited,
            messagesVisited = messagesVisited,
            starsVisited = starsVisited,
            cafeVisited = cafeVisited,
            shellFound = preferences[Keys.SHELL_FOUND] ?: false,
            fishMilestone = (preferences[Keys.FISH_MILESTONE] ?: 0)
                .coerceIn(0, MAX_FIRST_FISH_MILESTONE),
            wishUnlocked = (preferences[Keys.WISH_UNLOCKED] ?: false) ||
                discoveryCount >= WISH_UNLOCK_THRESHOLD,
            finaleCompleted = preferences[Keys.FINALE_COMPLETED] ?: false,
            wishState = preferences[Keys.WISH_STATE]
                ?.let { stored -> WishState.entries.firstOrNull { it.name == stored } }
                ?: WishState.NONE,
            wishDraft = preferences[Keys.WISH_DRAFT].orEmpty().take(MAX_WISH_LENGTH),
            pendingWishRequestId = preferences[Keys.PENDING_WISH_REQUEST_ID],
            pendingWishMessage = preferences[Keys.PENDING_WISH_MESSAGE],
            pendingWishRetryEnabled = preferences[Keys.PENDING_WISH_RETRY_ENABLED] ?: true,
            soundEnabled = preferences[Keys.SOUND_ENABLED] ?: true,
        )
    }

    private val MainDiscovery.preferenceKey: Preferences.Key<Boolean>
        get() = when (this) {
            MainDiscovery.GALLERY -> Keys.GALLERY_VISITED
            MainDiscovery.MESSAGES -> Keys.MESSAGES_VISITED
            MainDiscovery.STARS -> Keys.STARS_VISITED
            MainDiscovery.CAFE -> Keys.CAFE_VISITED
        }

    private val Preferences.wishState: WishState
        get() = this[Keys.WISH_STATE]
            ?.let { stored -> WishState.entries.firstOrNull { it.name == stored } }
            ?: WishState.NONE

    private object Keys {
        val HAS_OPENED_BEFORE = booleanPreferencesKey("has_opened_before")
        val WORLD_HINT_SEEN = booleanPreferencesKey("world_hint_seen")
        val GALLERY_VISITED = booleanPreferencesKey("gallery_visited")
        val MESSAGES_VISITED = booleanPreferencesKey("messages_visited")
        val STARS_VISITED = booleanPreferencesKey("stars_visited")
        val CAFE_VISITED = booleanPreferencesKey("cafe_visited")
        val SHELL_FOUND = booleanPreferencesKey("shell_found")
        val FISH_MILESTONE = intPreferencesKey("fish_milestone")
        val WISH_UNLOCKED = booleanPreferencesKey("wish_unlocked")
        val FINALE_COMPLETED = booleanPreferencesKey("finale_completed")
        val WISH_STATE = stringPreferencesKey("wish_state")
        val WISH_DRAFT = stringPreferencesKey("wish_draft")
        val PENDING_WISH_REQUEST_ID = stringPreferencesKey("pending_wish_request_id")
        val PENDING_WISH_MESSAGE = stringPreferencesKey("pending_wish_message")
        val PENDING_WISH_RETRY_ENABLED = booleanPreferencesKey("pending_wish_retry_enabled")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
    }

    private companion object {
        const val WISH_UNLOCK_THRESHOLD = 3
        const val MAX_FIRST_FISH_MILESTONE = 5
        const val MAX_WISH_LENGTH = WishRules.MAX_LENGTH
    }
}
