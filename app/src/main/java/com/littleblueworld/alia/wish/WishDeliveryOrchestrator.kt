package com.littleblueworld.alia.wish

import com.littleblueworld.alia.state.AppState
import com.littleblueworld.alia.state.WishState

class WishDeliveryOrchestrator(
    private val wishRepository: WishRepository,
    private val retryScheduler: WishRetryScheduler,
) {
    suspend fun sendSealedWish(): WishSendResult = wishRepository.sendSealedWish().also { result ->
        if (WishRetryPolicy.shouldSchedule(result)) {
            retryScheduler.schedulePendingWish()
        }
    }

    fun recoverPendingWish(state: AppState) {
        if (state.wishState == WishState.PENDING_SEND && state.pendingWishRetryEnabled) {
            retryScheduler.schedulePendingWish()
        }
    }
}

enum class WishWorkerDecision {
    SUCCESS,
    RETRY,
    FAILURE,
}

object WishRetryPolicy {
    fun shouldSchedule(result: WishSendResult): Boolean = when (result) {
        WishSendResult.PendingOffline,
        WishSendResult.PendingTemporary,
        -> true
        else -> false
    }

    fun workerDecision(result: WishSendResult): WishWorkerDecision = when (result) {
        WishSendResult.Sent,
        WishSendResult.Rejected,
        -> WishWorkerDecision.SUCCESS
        WishSendResult.PendingOffline,
        WishSendResult.PendingTemporary,
        -> WishWorkerDecision.RETRY
        WishSendResult.PendingPermanentFailure -> WishWorkerDecision.FAILURE
    }
}
