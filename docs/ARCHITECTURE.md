# ARCHITECTURE.md

## Goal
A native Android interactive diorama, not a business-app architecture and not a game engine.

## Stack
- Kotlin
- Android Views/XML
- ViewBinding
- AndroidX Activity
- AndroidX Core SplashScreen
- Preferences DataStore
- Kotlin Coroutines
- WorkManager only for pending Wish delivery
- custom Canvas View for micro effects
- optional MediaPlayer/SoundPool if audio assets exist
- Supabase Data REST API
- R8/resource optimization for release

`minSdk = 23`.

Pin compileSdk/targetSdk to the stable toolchain available when bootstrapping; do not use preview SDK.

## Rendering architecture
Default scene:
FrameLayout
- background ImageView
- cloud ImageViews
- anchored environment Views
- interactive ImageViews/wrappers
- Fish ImageView
- AmbientEffectsView (Canvas)
- native UI overlay

Do not build a giant PixelGameView or custom engine for all artwork.
Do not use SurfaceView/OpenGL unless profiling proves a specific scene cannot meet requirements.

## Single Activity
`MainActivity : ComponentActivity`

Responsibilities:
- system splash bridge
- inflate root
- create AppContainer
- restore lightweight state
- delegate Back/lifecycle

No multiple feature Activities.

## Root
`activity_main.xml`
- ScreenHost
- GlobalOverlayHost

## Navigation
Lightweight `AppNavigator`, no Navigation Component.

Screen IDs:
- BOOT
- BIRTHDAY_INTRO
- WORLD
- GALLERY
- MESSAGES
- STARS
- CAFE
- WISH
- FINAL_MESSAGE
- AUTHOR

Only current screen plus outgoing/incoming transition hierarchy should normally exist.

## Manual dependency container
`AppContainer`
- AppStateRepository
- BirthdayContent
- AudioController (no-op allowed when no assets)
- WishRepository
- WishRetryScheduler
- AppConfig

No DI framework.

## World
`WorldSceneLayout : FrameLayout`
owns:
- normalized landmark layout
- day/post-finale state
- motion controller
- interaction wrappers
- quality/density profile if later needed

`WorldLayoutSpec.kt` centralizes normalized position, relative scale, z-order, hit target, motion profile.

## Effects
`AmbientEffectsView : View`
only handles:
- micro particles
- themed sparkle sprites
- tiny stars/crosses
- temporary bursts

Particle model is bounded and pooled.
Use `postInvalidateOnAnimation`/Choreographer-style scheduling only while effects are active.

## Fish
Simple `FishController`.
No NPC framework.
Owns spawn timing, direction, dialogue milestone, escape, post-finale mode.

## Gallery
Normal Views:
- title
- photo ImageView
- stable frame overlay
- index
- caption

Prepare previous/current/next when practical.

## Wish
Composite View screen:
- dusk background
- Wish Star
- particle overlay
- intro text
- native multiline EditText
- counter
- PixelHoldButton
- destination cards
- Wish Capsule

Keyboard responsiveness outranks ambient effects.

## Persistent state
Preferences DataStore stores:
- hasOpenedBefore
- galleryVisited
- messagesVisited
- starsVisited
- cafeVisited
- shellFound
- fish milestone
- wishUnlocked
- finaleCompleted
- Wish state
- Wish draft/message as required
- pending request_id
- sound preference if audio exists

Derived phase:
- FIRST_VISIT
- RETURNING_PRE_FINALE
- POST_FINALE

`wishUnlocked` becomes true at 3 main discoveries and never regresses.

## Wish state machine
- NONE
- DRAFT
- SEALED
- KEPT_LOCAL
- PENDING_SEND
- SENT

`SEALED` is not consent to upload.

Experience may proceed to finale from:
- KEPT_LOCAL
- PENDING_SEND
- SENT

`finaleCompleted` is set after the ending / return-to-world completion, not merely after Wish submission.

## Wish networking
`WishRepository`
- local state
- `WishApi`
- retry scheduler

`WishApi`
- `SupabaseWishApi` production
- fake implementation in tests

Use a small standard/JVM HTTPS implementation (for example HttpURLConnection) on IO coroutine rather than adding a large networking dependency for one endpoint.

UI receives logical results, never raw HTTP status.

## Offline retry
Pending payload is stored in DataStore.
Worker input should not duplicate Wish text.

Unique WorkManager task:
`little_blue_world_pending_wish`

Worker:
- read current state
- if not PENDING_SEND -> success/no-op
- attempt insert
- success/duplicate -> mark SENT
- temporary network/server failure -> retry
- permanent configuration/payload error -> stop retrying but preserve safe local state

## Audio
Audio is optional until assets exist.
Architecture should support a no-op AudioController so absence of audio never blocks release.

If supplied later:
- MediaPlayer for ambience/long audio
- SoundPool for short SFX
- pause on background
- avoid duplicate streams on resume

## Packages
Recommended:
- `app/`
- `navigation/`
- `content/`
- `state/`
- `world/`
- `effects/`
- `gallery/`
- `messages/`
- `stars/`
- `cafe/`
- `wish/`
- `author/`
- `audio/`
- `util/`

Do not create empty abstractions or giant framework layers.

## Release
Debug: easy diagnostics, R8 off.
Release: real assets, no payload logging, minify/R8/resource optimization enabled, no visible placeholders.
