package com.littleblueworld.alia.state

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class DataStoreAppStateRepositoryTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    private lateinit var dataStoreScope: CoroutineScope
    private lateinit var repository: DataStoreAppStateRepository

    @Before
    fun setUp() {
        dataStoreScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val stateFile = File(temporaryFolder.root, "app-state.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(
            scope = dataStoreScope,
            produceFile = { stateFile },
        )
        repository = DataStoreAppStateRepository(dataStore)
    }

    @After
    fun tearDown() {
        dataStoreScope.cancel()
    }

    @Test
    fun defaultStateIsFirstVisitAndLocked() = runTest {
        val state = repository.state.first()

        assertEquals(ExperiencePhase.FIRST_VISIT, state.experiencePhase)
        assertEquals(0, state.mainDiscoveryCount)
        assertFalse(state.wishUnlocked)
        assertEquals(WishState.NONE, state.wishState)
    }

    @Test
    fun exactlyThreeUniqueMainDiscoveriesUnlockWish() = runTest {
        repository.markMainDiscovery(MainDiscovery.GALLERY)
        repository.markMainDiscovery(MainDiscovery.GALLERY)
        repository.markMainDiscovery(MainDiscovery.MESSAGES)

        assertEquals(2, repository.state.first().mainDiscoveryCount)
        assertFalse(repository.state.first().wishUnlocked)

        repository.markMainDiscovery(MainDiscovery.CAFE)
        val unlocked = repository.state.first()

        assertEquals(3, unlocked.mainDiscoveryCount)
        assertTrue(unlocked.wishUnlocked)
    }

    @Test
    fun fishMilestoneNeverRegressesAndIsBounded() = runTest {
        repository.updateFishMilestone(4)
        repository.updateFishMilestone(2)
        assertEquals(4, repository.state.first().fishMilestone)

        repository.updateFishMilestone(99)
        val bounded = repository.state.first()
        assertEquals(5, bounded.fishMilestone)
        assertEquals(0, bounded.mainDiscoveryCount)
        assertFalse(bounded.wishUnlocked)
    }

    @Test
    fun openedAndFinaleFlagsPersistAsExperiencePhase() = runTest {
        repository.markOpened()
        assertEquals(
            ExperiencePhase.RETURNING_PRE_FINALE,
            repository.state.first().experiencePhase,
        )

        repository.markFinaleCompleted()
        val finalState = repository.state.first()
        assertTrue(finalState.hasOpenedBefore)
        assertTrue(finalState.finaleCompleted)
        assertEquals(ExperiencePhase.POST_FINALE, finalState.experiencePhase)
    }

    @Test
    fun worldHintSeenPersistsIndependentlyFromOpenedState() = runTest {
        repository.markWorldHintSeen()

        val state = repository.state.first()
        assertTrue(state.worldHintSeen)
        assertFalse(state.hasOpenedBefore)
    }

    @Test
    fun shellPersistsWithoutCountingTowardWishUnlock() = runTest {
        repository.markMainDiscovery(MainDiscovery.GALLERY)
        repository.markMainDiscovery(MainDiscovery.MESSAGES)
        repository.markShellFound()

        val state = repository.state.first()
        assertTrue(state.shellFound)
        assertEquals(2, state.mainDiscoveryCount)
        assertFalse(state.wishUnlocked)
    }

    @Test
    fun wishDraftIsBoundedAndSealRequiresNonBlankContent() = runTest {
        assertFalse(repository.sealWish())

        repository.updateWishDraft("x".repeat(520))
        val draft = repository.state.first { it.wishState == WishState.DRAFT }
        assertEquals(500, draft.wishDraft.length)

        assertTrue(repository.sealWish())
        val sealed = repository.state.first { it.wishState == WishState.SEALED }
        assertEquals(500, sealed.wishDraft.length)

        repository.updateWishDraft("must not replace a sealed wish")
        assertEquals(500, repository.state.first().wishDraft.length)
    }

    @Test
    fun keepLocalRequiresSealAndPersistsWithoutPendingPayload() = runTest {
        assertFalse(repository.keepWishLocal())
        repository.updateWishDraft("a quiet little wish")
        assertTrue(repository.sealWish())
        assertTrue(repository.keepWishLocal())

        val kept = repository.state.first { it.wishState == WishState.KEPT_LOCAL }
        assertEquals("a quiet little wish", kept.wishDraft)
        assertEquals(null, kept.pendingWishRequestId)
        assertEquals(null, kept.pendingWishMessage)
        assertEquals(null, repository.prepareWishSend(REQUEST_ID))
    }

    @Test
    fun sendPersistsPendingPayloadBeforeItCanBecomeSent() = runTest {
        repository.updateWishDraft("something good")
        assertEquals(null, repository.prepareWishSend(REQUEST_ID))
        assertEquals(WishState.DRAFT, repository.state.first().wishState)

        assertTrue(repository.sealWish())
        val pending = repository.prepareWishSend(REQUEST_ID)
        assertEquals(PendingWish(REQUEST_ID, "something good"), pending)

        val pendingState = repository.state.first { it.wishState == WishState.PENDING_SEND }
        assertEquals(REQUEST_ID, pendingState.pendingWishRequestId)
        assertEquals("something good", pendingState.pendingWishMessage)

        assertTrue(repository.markWishSent(REQUEST_ID))
        val sent = repository.state.first { it.wishState == WishState.SENT }
        assertEquals(null, sent.pendingWishRequestId)
        assertEquals(null, sent.pendingWishMessage)
    }

    @Test
    fun pendingRetryReusesPersistedRequestAndRejectsMismatchedCompletion() = runTest {
        repository.updateWishDraft("same logical wish")
        assertTrue(repository.sealWish())
        assertEquals(
            PendingWish(REQUEST_ID, "same logical wish"),
            repository.prepareWishSend(REQUEST_ID),
        )

        assertEquals(
            PendingWish(REQUEST_ID, "same logical wish"),
            repository.prepareWishSend(OTHER_REQUEST_ID),
        )
        assertFalse(repository.markWishSent(OTHER_REQUEST_ID))
        assertEquals(WishState.PENDING_SEND, repository.state.first().wishState)
    }

    private companion object {
        const val REQUEST_ID = "3f67e80a-3912-4f5d-a1e7-c321bfecbd17"
        const val OTHER_REQUEST_ID = "6108bca9-0a71-4ed6-8405-df445e81a9f8"
    }
}
