package com.littleblueworld.alia.state

import kotlinx.coroutines.flow.Flow

interface AppStateRepository {
    val state: Flow<AppState>

    suspend fun markOpened()

    suspend fun markWorldHintSeen()

    suspend fun markMainDiscovery(discovery: MainDiscovery)

    suspend fun markShellFound()

    suspend fun updateFishMilestone(milestone: Int)

    suspend fun updateWishDraft(draft: String)

    suspend fun sealWish(): Boolean

    suspend fun keepWishLocal(): Boolean

    suspend fun prepareWishSend(newRequestId: String): PendingWish?

    suspend fun markWishSent(requestId: String): Boolean

    suspend fun markFinaleCompleted()
}

data class PendingWish(
    val requestId: String,
    val message: String,
)
