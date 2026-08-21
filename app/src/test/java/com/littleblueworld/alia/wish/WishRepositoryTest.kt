package com.littleblueworld.alia.wish

import com.littleblueworld.alia.state.AppState
import com.littleblueworld.alia.state.AppStateRepository
import com.littleblueworld.alia.state.MainDiscovery
import com.littleblueworld.alia.state.PendingWish
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class WishRepositoryTest {
    @Test
    fun `successful insert marks the matching pending request sent`() = runTest {
        val stateRepository = FakeStateRepository()
        val repository = WishRepository(
            stateRepository = stateRepository,
            wishApi = WishApi { WishApiResult.Inserted },
            requestIdFactory = { REQUEST_ID },
        )

        assertEquals(WishSendResult.Sent, repository.sendSealedWish())
        assertTrue(stateRepository.markSentCalled)
        assertEquals(REQUEST_ID, stateRepository.markedRequestId)
    }

    @Test
    fun `temporary failure preserves pending state and never marks sent`() = runTest {
        val stateRepository = FakeStateRepository()
        val repository = WishRepository(
            stateRepository = stateRepository,
            wishApi = WishApi {
                WishApiResult.TemporaryFailure(TemporaryFailureKind.NETWORK)
            },
            requestIdFactory = { REQUEST_ID },
        )

        assertEquals(WishSendResult.PendingOffline, repository.sendSealedWish())
        assertFalse(stateRepository.markSentCalled)
    }

    @Test
    fun `repeated attempt uploads the same persisted logical request`() = runTest {
        val stateRepository = FakeStateRepository()
        val submissions = mutableListOf<WishSubmission>()
        val generatedIds = ArrayDeque(listOf(REQUEST_ID, OTHER_REQUEST_ID))
        val repository = WishRepository(
            stateRepository = stateRepository,
            wishApi = WishApi { submission ->
                submissions += submission
                WishApiResult.TemporaryFailure(TemporaryFailureKind.SERVER)
            },
            requestIdFactory = { generatedIds.removeFirst() },
        )

        repository.sendSealedWish()
        repository.sendSealedWish()

        assertEquals(listOf(REQUEST_ID, REQUEST_ID), submissions.map { it.requestId })
    }

    private class FakeStateRepository : AppStateRepository {
        override val state: Flow<AppState> = MutableStateFlow(AppState())
        private var pendingWish: PendingWish? = null
        var markSentCalled = false
        var markedRequestId: String? = null

        override suspend fun prepareWishSend(newRequestId: String): PendingWish =
            pendingWish ?: PendingWish(newRequestId, "a little wish").also { pendingWish = it }

        override suspend fun markWishSent(requestId: String): Boolean {
            markSentCalled = true
            markedRequestId = requestId
            return pendingWish?.requestId == requestId
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
        const val OTHER_REQUEST_ID = "6108bca9-0a71-4ed6-8405-df445e81a9f8"
    }
}
