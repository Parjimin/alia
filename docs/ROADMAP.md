# ROADMAP.md

Implement bounded vertical milestones. Each nontrivial milestone:
plan -> implement -> test -> build -> screenshot/evidence -> fix -> commit.

## M0 — Bootstrap
Native Android project:
- Kotlin
- XML/Views
- ViewBinding
- minSdk 23
- single Activity
- debug/release setup
- only approved dependencies

Done: debug + skeleton release builds, app launches.

## M1 — Foundation
- AppContainer
- AppCoordinator
- AppNavigator
- ScreenFactory
- TransitionController
- DataStore/AppStateRepository
- BirthdayContent
- temporary navigation screens

Done: navigation/back/state tests.

## M2 — Production Asset Pipeline
Process 25 supplied masters:
- cleanup
- crop
- resize
- WebP
- underscore Android resource names
- side-by-side visual QA
- wave seam
- frame transparency
- day/dusk alignment
- adaptive launcher icon from existing star art

Never modify masters.

## M3 — Pixel Boot
- native splash bridge
- Pixel Boot
- loading star
- wave
- phased loading
- signature sequence
- revisit variants

Evidence: first launch + revisit recording, unit test for loading sequence invariants.

## M4 — Birthday Entrance
- title staging
- CTA
- wave World transition

## M5 — Little Blue World Core
- world_day
- clouds
- responsive landmark composition
- AmbientEffectsView
- WorldMotionController
- first hint
- all landmark visuals except Fish may initially be static/disabled

Evidence: small/normal/tall viewport screenshots and idle performance.

## M6 — World State + Progression
- visited state
- 3-of-4 discovery logic
- locked Wish
- unlock animation
- post-finale state foundation

Unit tests for thresholds and persistence.

## M7 — Alia Archive
- title/subtitle
- square photo stage
- frame
- 6 debug placeholders if real photos absent
- swipe/index/caption
- focal offset support
- previous/current/next loading window

## M8 — Message Bottles
- four bottles
- open animation
- first reveal/reopen behavior
- exact copy

## M9 — Collectible Stars
- 3 collect interactions
- particles
- exact messages
- YOUR FACE timing
- completion

## M10 — Café + Shell
- Café hero
- forecast
- tea/coffee taps
- Shell shimmer/message
- correct progression counting

## M11 — Fish
- spawn/swim/wobble
- tap progression
- escape
- post-finale mode

## M12 — Make a Wish UI (local/fake backend)
Do before real backend:
- day->dusk
- Wish intro/Star
- input/counter/draft
- hold-to-seal
- destination choices
- Keep Local complete
- fake Send result

Keyboard performance evidence required.

## M13 — Supabase
- WishApi/SupabaseWishApi
- request_id
- exact REST insert
- WishRepository
- run RLS/grant security tests

Do not continue until SELECT/UPDATE/DELETE tests fail as intended.

## M14 — Offline delivery
- PENDING_SEND persistence
- unique WorkManager
- retry classification
- duplicate handling

Mandatory airplane-mode + app-close test.
Mandatory Keep Local zero-upload test.

## M15 — Finale
- final birthday message
- P.S.
- author transition

## M16 — About Author + Ending
- author layout
- stats
- personal note
- ending timing
- Wander CTA

## M17 — Post-Finale World
- world_dusk/evening
- moon
- richer micro stars
- extra flower
- completed Wish behavior
- Fish post-finale dialogue
- revisit loading

## M18 — Final Photos + Caption Mapping
When available:
- replace `alia_01`...`alia_06`
- replace `author`
- map captions
- set focal offsets
- compress/QA each image

This is the main personal-content blocker for final production.

## M19 — Optional Audio
Only if real audio assets are supplied.
- ambience via MediaPlayer
- SFX via SoundPool
- toggle/persistence
- lifecycle/ducking

If no audio assets exist, skip this milestone. Silent release is valid.

## M20 — Performance Pass
Profile actual representative hardware/release-like build.
Fix measured issues in PERFORMANCE.md order.
Do not remove designed effects based on guesswork.

## M21 — Adaptive Quality (conditional)
Skip if FULL is stable.
If needed, implement only density-based FULL/BALANCED/LIGHT behavior.

## M22 — Accessibility + System Behavior
- content descriptions
- 48dp touch target
- font scaling
- safe area
- Back
- lifecycle
- IME

## M23 — Release Hardening
- remove debug UI
- remove test rows/content
- verify credentials
- strip sensitive logs
- R8/resource shrinking
- build release
- repository search for TODO/FIXME/secret/test strings

## M24 — Full Acceptance Run
Fresh install:
Boot -> Birthday -> World -> 3 discoveries -> Wish -> Finale -> Author -> Post-finale -> kill -> revisit.

Separately:
- online Wish
- offline Wish
- Keep Local
- duplicate request
- process death/background/resume
- backend security

## M25 — Final APK
Produce signed release APK and record:
- versionName
- versionCode
- APK size
- tested device(s)
- date
- known limitations

## Command Code session discipline
Suggested grouping:
- A: M0–M2
- B: M3–M6
- C: M7–M11
- D: M12–M14
- E: M15–M18
- F: M19–M25

Start a clean session after a bounded milestone if context becomes noisy.

## Stop conditions
Stop for review when:
- specs conflict
- asset is genuinely unusable
- forbidden dependency seems required
- privacy/security assumptions change
- major architecture escalation is proposed
- performance fix would remove designed experience
