package com.littleblueworld.alia.app

import android.content.Context
import android.view.ViewGroup
import com.littleblueworld.alia.boot.BootRoute
import com.littleblueworld.alia.boot.BootScreenFactory
import com.littleblueworld.alia.birthday.BirthdayEntranceScreenFactory
import com.littleblueworld.alia.cafe.CafeActions
import com.littleblueworld.alia.cafe.CafeScreenFactory
import com.littleblueworld.alia.content.BirthdayContent
import com.littleblueworld.alia.gallery.GalleryActions
import com.littleblueworld.alia.gallery.GalleryScreenFactory
import com.littleblueworld.alia.messages.MessageActions
import com.littleblueworld.alia.messages.MessageScreenFactory
import com.littleblueworld.alia.navigation.AppNavigator
import com.littleblueworld.alia.navigation.AppScreen
import com.littleblueworld.alia.navigation.NavigationChange
import com.littleblueworld.alia.navigation.ScreenActionSpec
import com.littleblueworld.alia.navigation.ScreenFactory
import com.littleblueworld.alia.navigation.ScreenId
import com.littleblueworld.alia.navigation.TemporaryScreenSpec
import com.littleblueworld.alia.navigation.TransitionController
import com.littleblueworld.alia.navigation.WaveTransitionController
import com.littleblueworld.alia.state.AppState
import com.littleblueworld.alia.state.AppStateRepository
import com.littleblueworld.alia.state.MainDiscovery
import com.littleblueworld.alia.stars.StarActions
import com.littleblueworld.alia.stars.StarScreenFactory
import com.littleblueworld.alia.world.WorldActions
import com.littleblueworld.alia.world.WorldProgression
import com.littleblueworld.alia.world.WorldScreenFactory
import com.littleblueworld.alia.wish.WishActions
import com.littleblueworld.alia.wish.WishDeliveryOrchestrator
import com.littleblueworld.alia.wish.WishScreenFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AppCoordinator(
    context: Context,
    screenHost: ViewGroup,
    globalOverlayHost: ViewGroup,
    private val stateRepository: AppStateRepository,
    private val wishDeliveryOrchestrator: WishDeliveryOrchestrator,
    private val content: BirthdayContent,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val navigator = AppNavigator()
    private val screenFactory = ScreenFactory(context, content)
    private val bootScreenFactory = BootScreenFactory(context, content)
    private val birthdayEntranceScreenFactory = BirthdayEntranceScreenFactory(context, content)
    private val worldScreenFactory = WorldScreenFactory(context, content)
    private val galleryScreenFactory = GalleryScreenFactory(context, content)
    private val messageScreenFactory = MessageScreenFactory(context, content)
    private val starScreenFactory = StarScreenFactory(context, content)
    private val cafeScreenFactory = CafeScreenFactory(context, content)
    private val wishScreenFactory = WishScreenFactory(context, content)
    private val transitionController = TransitionController(screenHost)
    private val waveTransitionController = WaveTransitionController(globalOverlayHost)

    private var stateJob: Job? = null
    private var latestState = AppState()
    private var pendingWishUnlockCelebration = false
    private var started = false

    fun start() {
        if (started) return
        started = true

        stateJob = scope.launch {
            var initialScreenShown = false
            stateRepository.state.collect { state ->
                latestState = state
                if (!initialScreenShown) {
                    initialScreenShown = true
                    wishDeliveryOrchestrator.recoverPendingWish(state)
                    transitionController.setInitial(createScreen(navigator.current))
                } else {
                    transitionController.render(state)
                }
            }
        }
    }

    fun onForegrounded() {
        transitionController.onForegrounded()
        waveTransitionController.onForegrounded()
    }

    fun onBackgrounded() {
        waveTransitionController.onBackgrounded()
        transitionController.onBackgrounded()
    }

    /** Returns true when the coordinator consumed Back. */
    fun onBackPressed(): Boolean {
        if (transitionController.isRunning || waveTransitionController.isRunning) return true
        if (transitionController.currentScreen?.onBackPressed() == true) return true
        val change = navigator.back() ?: return false
        show(change)
        return true
    }

    fun destroy() {
        stateJob?.cancel()
        waveTransitionController.clear()
        transitionController.clear()
        scope.cancel()
    }

    private fun push(destination: ScreenId) {
        if (transitionController.isRunning) return
        navigator.push(destination)?.let(::show)
    }

    private fun replace(destination: ScreenId) {
        if (transitionController.isRunning) return
        navigator.replace(destination)?.let(::show)
    }

    private fun resetTo(destination: ScreenId) {
        if (transitionController.isRunning) return
        navigator.resetTo(destination)?.let(::show)
    }

    private fun show(change: NavigationChange) {
        transitionController.transitionTo(
            incoming = createScreen(change.to),
            direction = change.direction,
        )
    }

    private fun createScreen(id: ScreenId): AppScreen = when (id) {
        ScreenId.BOOT -> bootScreenFactory.create(
            experiencePhase = latestState.experiencePhase,
            onComplete = { experiencePhase ->
                val destination = BootRoute.destinationFor(experiencePhase)
                if (destination == ScreenId.BIRTHDAY_INTRO) {
                    replace(destination)
                } else {
                    resetTo(destination)
                }
            },
        )

        ScreenId.BIRTHDAY_INTRO -> birthdayEntranceScreenFactory.create(
            onOpenWorld = ::openWorldFromBirthday,
        )

        ScreenId.WORLD -> {
            val shouldCelebrateWishUnlock = pendingWishUnlockCelebration
            pendingWishUnlockCelebration = false
            worldScreenFactory.create(
                state = latestState,
                actions = worldActions(),
                shouldCelebrateWishUnlock = shouldCelebrateWishUnlock,
                onFirstHintShown = {
                    scope.launch { stateRepository.markWorldHintSeen() }
                },
            )
        }

        ScreenId.GALLERY -> galleryScreenFactory.create(
            state = latestState,
            actions = GalleryActions(
                goBack = { onBackPressed() },
                onFirstMeaningfulInteraction = {
                    scope.launch { recordMainDiscovery(MainDiscovery.GALLERY) }
                },
            ),
        )

        ScreenId.MESSAGES -> messageScreenFactory.create(
            state = latestState,
            actions = MessageActions(
                goBack = { onBackPressed() },
                onFirstMeaningfulInteraction = {
                    scope.launch { recordMainDiscovery(MainDiscovery.MESSAGES) }
                },
            ),
        )

        ScreenId.STARS -> starScreenFactory.create(
            state = latestState,
            actions = StarActions(
                goBack = { onBackPressed() },
                onFirstMeaningfulInteraction = {
                    scope.launch { recordMainDiscovery(MainDiscovery.STARS) }
                },
            ),
        )

        ScreenId.CAFE -> cafeScreenFactory.create(
            state = latestState,
            actions = CafeActions(
                goBack = { onBackPressed() },
                onFirstMeaningfulInteraction = {
                    scope.launch { recordMainDiscovery(MainDiscovery.CAFE) }
                },
            ),
        )

        ScreenId.WISH -> wishScreenFactory.create(
            state = latestState,
            actions = WishActions(
                saveDraft = stateRepository::updateWishDraft,
                flushDraft = { draft ->
                    scope.launch { stateRepository.updateWishDraft(draft) }
                },
                sealWish = stateRepository::sealWish,
                keepLocal = stateRepository::keepWishLocal,
                sendWish = wishDeliveryOrchestrator::sendSealedWish,
                goBack = ::popCurrentScreen,
                continueToFinale = { replace(ScreenId.FINAL_MESSAGE) },
            ),
        )

        else -> screenFactory.create(
            id = id,
            spec = temporarySpecFor(id),
            state = latestState,
        )
    }

    private fun openWorldFromBirthday(): Boolean = waveTransitionController.coverThenReveal { reveal ->
        scope.launch {
            navigator.resetTo(ScreenId.WORLD)
            transitionController.setInitial(createScreen(ScreenId.WORLD))
            runCatching { stateRepository.markOpened() }
            reveal()
        }
    }

    private fun popCurrentScreen() {
        if (transitionController.isRunning || waveTransitionController.isRunning) return
        navigator.back()?.let(::show)
    }

    private fun worldActions() = WorldActions(
        openGallery = { push(ScreenId.GALLERY) },
        openMessages = { push(ScreenId.MESSAGES) },
        openStars = { push(ScreenId.STARS) },
        openCafe = { push(ScreenId.CAFE) },
        onShellFound = {
            scope.launch { stateRepository.markShellFound() }
        },
        onFishMilestone = { milestone ->
            scope.launch { stateRepository.updateFishMilestone(milestone) }
        },
        openAuthor = { push(ScreenId.AUTHOR) },
        openWish = { push(ScreenId.WISH) },
    )

    private fun temporarySpecFor(id: ScreenId): TemporaryScreenSpec = when (id) {
        ScreenId.BOOT -> error("Pixel Boot uses its production screen factory")

        ScreenId.BIRTHDAY_INTRO -> error("Birthday Entrance uses its production screen factory")

        ScreenId.WORLD -> error("Little Blue World uses its production screen factory")

        ScreenId.GALLERY -> error("Alia Archive uses its production screen factory")
        ScreenId.MESSAGES -> error("Message Bottles uses its production screen factory")
        ScreenId.STARS -> error("Collectible Stars uses its production screen factory")
        ScreenId.CAFE -> error("Tiny Café uses its production screen factory")

        ScreenId.WISH -> error("Make a Wish uses its production screen factory")

        ScreenId.FINAL_MESSAGE -> TemporaryScreenSpec(
            milestone = "M15",
            actions = listOf(
                action(content.postscriptCta) { push(ScreenId.AUTHOR) },
            ),
            showsBackHint = true,
        )

        ScreenId.AUTHOR -> TemporaryScreenSpec(
            milestone = "M16",
            actions = listOf(
                action(content.endingCta) { resetTo(ScreenId.WORLD) },
            ),
            showsBackHint = true,
        )
    }

    private fun discoverySpec(
        milestone: String,
        discovery: MainDiscovery,
    ): TemporaryScreenSpec {
        var actionAccepted = false
        val recordAndReturn: () -> Unit = record@{
            if (actionAccepted) return@record
            actionAccepted = true
            scope.launch {
                recordMainDiscovery(discovery)
                resetTo(ScreenId.WORLD)
            }
        }

        return TemporaryScreenSpec(
            milestone = milestone,
            actions = listOf(
                action("Record discovery and return to World") {
                    recordAndReturn()
                },
            ),
            showsBackHint = true,
        )
    }

    private suspend fun recordMainDiscovery(discovery: MainDiscovery) {
        val previousState = latestState
        stateRepository.markMainDiscovery(discovery)
        val updatedState = stateRepository.state.first { it.hasVisited(discovery) }
        latestState = updatedState
        pendingWishUnlockCelebration = pendingWishUnlockCelebration ||
            WorldProgression.shouldCelebrateWishUnlock(
                previous = previousState,
                current = updatedState,
            )
    }

    private fun action(
        label: String,
        onClick: () -> Unit,
    ) = ScreenActionSpec(
        label = label,
        onClick = onClick,
    )
}
