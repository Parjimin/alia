package com.littleblueworld.alia.wish

import com.littleblueworld.alia.state.AppStateRepository
import com.littleblueworld.alia.state.PendingWish
import com.littleblueworld.alia.state.WishRules
import com.littleblueworld.alia.state.WishState
import java.util.UUID
import kotlinx.coroutines.flow.first

class WishRepository(
    private val stateRepository: AppStateRepository,
    private val wishApi: WishApi,
    private val requestIdFactory: () -> String = { UUID.randomUUID().toString() },
) {
    suspend fun sendSealedWish(): WishSendResult {
        val pendingWish = stateRepository.prepareWishSend(requestIdFactory())
            ?: return WishSendResult.Rejected
        return deliver(pendingWish)
    }

    suspend fun retryPendingWish(): WishSendResult {
        val state = stateRepository.state.first()
        val requestId = state.pendingWishRequestId
        val message = state.pendingWishMessage
        if (
            state.wishState != WishState.PENDING_SEND ||
            !state.pendingWishRetryEnabled ||
            requestId == null ||
            message == null ||
            !WishRules.isValidRequestId(requestId) ||
            message.isBlank()
        ) {
            return WishSendResult.Rejected
        }
        return deliver(PendingWish(requestId, message))
    }

    private suspend fun deliver(pendingWish: PendingWish): WishSendResult {
        val submission = WishSubmission(
            requestId = pendingWish.requestId,
            message = pendingWish.message,
        )
        return when (val result = wishApi.insert(submission)) {
            WishApiResult.Inserted, WishApiResult.Duplicate -> {
                if (stateRepository.markWishSent(pendingWish.requestId)) {
                    WishSendResult.Sent
                } else {
                    WishSendResult.Rejected
                }
            }

            is WishApiResult.TemporaryFailure -> when (result.kind) {
                TemporaryFailureKind.NETWORK -> WishSendResult.PendingOffline
                TemporaryFailureKind.SERVER -> WishSendResult.PendingTemporary
            }

            WishApiResult.PermanentFailure -> {
                stateRepository.markWishPermanentFailure(pendingWish.requestId)
                WishSendResult.PendingPermanentFailure
            }
        }
    }
}

sealed interface WishSendResult {
    data object Sent : WishSendResult
    data object PendingOffline : WishSendResult
    data object PendingTemporary : WishSendResult
    data object PendingPermanentFailure : WishSendResult
    data object Rejected : WishSendResult
}
