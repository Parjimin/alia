package com.littleblueworld.alia.wish

import com.littleblueworld.alia.state.AppState
import com.littleblueworld.alia.state.AppStateRepository
import com.littleblueworld.alia.state.MainDiscovery
import com.littleblueworld.alia.state.PendingWish
import com.littleblueworld.alia.state.WishState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class WishDeliveryOrchestratorTest {
    @Test
    fun `temporary send failure schedules exactly once`() = runTest {
        var scheduled = 0
        val orchestrator = orchestrator(
            apiResult = WishApiResult.TemporaryFailure(TemporaryFailureKind.NETWORK),
            onSchedule = { scheduled += 1 },
        )

        assertEquals(WishSendResult.PendingOffline, orchestrator.sendSealedWish())
        assertEquals(1, scheduled)
    }

    @Test
    fun `online success and permanent failure do not schedule retry`() = runTest {
        var scheduled = 0
        val sent = orchestrator(WishApiResult.Inserted) { scheduled += 1 }
        val permanent = orchestrator(WishApiResult.PermanentFailure) { scheduled += 1 }

        assertEquals(WishSendResult.Sent, sent.sendSealedWish())
        assertEquals(WishSendResult.PendingPermanentFailure, permanent.sendSealedWish())
        assertEquals(0, scheduled)
    }

    @Test
    fun `process restart recovers only retryable pending state`() {
        var scheduled = 0
        val orchestrator = orchestrator(WishApiResult.Inserted) { scheduled += 1 }

        orchestrator.recoverPendingWish(AppState(wishState = WishState.KEPT_LOCAL))
        orchestrator.recoverPendingWish(
            AppState(
                wishState = WishState.PENDING_SEND,
                pendingWishRetryEnabled = false,
            ),
        )
        orchestrator.recoverPendingWish(
            AppState(
                wishState = WishState.PENDING_SEND,
                pendingWishRetryEnabled = true,
            ),
        )

        assertEquals(1, scheduled)
    }

    @Test
    fun `worker decisions are bounded and explicit`() {
        assertEquals(WishWorkerDecision.SUCCESS, WishRetryPolicy.workerDecision(WishSendResult.Sent))
        assertEquals(
            WishWorkerDecision.SUCCESS,
            WishRetryPolicy.workerDecision(WishSendResult.Rejected),
        )
        assertEquals(
            WishWorkerDecision.RETRY,
            WishRetryPolicy.workerDecision(WishSendResult.PendingOffline),
        )
        assertEquals(
            WishWorkerDecision.RETRY,
            WishRetryPolicy.workerDecision(WishSendResult.PendingTemporary),
        )
        assertEquals(
            WishWorkerDecision.FAILURE,
            WishRetryPolicy.workerDecision(WishSendResult.PendingPermanentFailure),
        )
    }

    private fun orchestrator(
        apiResult: WishApiResult,
        onSchedule: () -> Unit,
    ): WishDeliveryOrchestrator {
        val stateRepository = FakePendingRepository()
        return WishDeliveryOrchestrator(
            wishRepository = WishRepository(
                stateRepository = stateRepository,
                wishApi = WishApi { apiResult },
                requestIdFactory = { REQUEST_ID },
            ),
            retryScheduler = WishRetryScheduler(onSchedule),
        )
    }

    private class FakePendingRepository : AppStateRepository {
        private val mutableState = MutableStateFlow(AppState())
        override val state: Flow<AppState> = mutableState

        override suspend fun prepareWishSend(newRequestId: String): PendingWish {
            val pending = PendingWish(newRequestId, "test wish")
            mutableState.value = AppState(
                wishState = WishState.PENDING_SEND,
                pendingWishRequestId = pending.requestId,
                pendingWishMessage = pending.message,
            )
            return pending
        }

        override suspend fun markWishSent(requestId: String): Boolean {
            if (mutableState.value.pendingWishRequestId != requestId) return false
            mutableState.value = mutableState.value.copy(
                wishState = WishState.SENT,
                pendingWishRequestId = null,
                pendingWishMessage = null,
            )
            return true
        }

        override suspend fun markWishPermanentFailure(requestId: String): Boolean {
            if (mutableState.value.pendingWishRequestId != requestId) return false
            mutableState.value = mutableState.value.copy(pendingWishRetryEnabled = false)
            return true
        }

        override suspend fun markOpened() = Unit
        override suspend fun markWorldHintSeen() = Unit
        override suspend fun markMainDiscovery(discovery: MainDiscovery) = Unit
        override suspend fun markShellFound() = Unit
        override suspend fun updateFishMilestone(milestone: Int) = Unit
        override suspend fun updateWishDraft(draft: String) = Unit
        override suspend fun sealWish(): Boolean = false
        override suspend fun keepWishLocal(): Boolean = false
        override suspend fun markFinaleCompleted() = Unit
    }

    private companion object {
        const val REQUEST_ID = "3f67e80a-3912-4f5d-a1e7-c321bfecbd17"
    }
}
