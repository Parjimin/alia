package com.littleblueworld.alia.wish

import com.littleblueworld.alia.state.AppStateRepository
import java.util.UUID

class WishRepository(
    private val stateRepository: AppStateRepository,
    private val wishApi: WishApi,
    private val requestIdFactory: () -> String = { UUID.randomUUID().toString() },
) {
    suspend fun sendSealedWish(): WishSendResult {
        val pendingWish = stateRepository.prepareWishSend(requestIdFactory())
            ?: return WishSendResult.Rejected
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

            WishApiResult.PermanentFailure -> WishSendResult.PendingPermanentFailure
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
