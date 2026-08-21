# Little Blue World

A specification-driven native Android birthday experience for Alia.

The repository contains the canonical product handoff and the native Android implementation. The chat history is **not** the implementation source of truth.

## Start here

1. Read `docs/AI_HANDOFF.md` for the current resume point.
2. Read `AGENTS.md`, `docs/MILESTONE_STATUS.md`, and the relevant canonical specifications.
3. Do **not** overwrite anything in `design-assets/master/`.
4. Implement one roadmap milestone at a time.

## Development status

Implementation and automated validation are complete through the local M14 offline-delivery checkpoint. One M14 actual-device airplane-mode/process-close/reconnect acceptance gate remains explicitly open.

Current continuation links:

- Resume branch: `codex/m14-offline-delivery`
- Draft PR: <https://github.com/Parjimin/alia/pull/1>
- Next milestone: M15 — Finale
- Full handoff: [`docs/AI_HANDOFF.md`](docs/AI_HANDOFF.md)
- Milestone ledger: [`docs/MILESTONE_STATUS.md`](docs/MILESTONE_STATUS.md)

Implemented foundation and features include:

- a single-Activity coordinator and lightweight navigation history
- a screen factory with temporary milestone surfaces
- bounded two-view property transitions
- Preferences DataStore-backed local progress
- centralized, frozen recipient-facing copy
- unit coverage for navigation, progression, persistence mapping, and content invariants
- 25 pixel-identical lossless WebP production assets with Android-safe names
- adaptive/legacy launcher icon assembled from the supplied loading star
- documented asset integrity, alpha, seam, alignment, and visual QA
- native launch-window bridge for API 23–37 without an added splash dependency
- ordered first-visit Pixel Boot phases with the approved signature sequence
- shorter pre-finale and post-finale revisit variants
- scene-owned loading-star, wave, text, and bounded sparkle motion that stops in background
- staged `HAPPY BIRTHDAY,` / `ALIA.` reveal with the frozen CTA copy
- responsive 52dp pixel CTA with immediate pressed feedback and duplicate-tap protection
- lifecycle-aware wave cover that swaps the World underneath before revealing it
- `world_day` environment with the approved Camera–Wish–Star, Bottle–Note, Shell–Island–Café layout
- bounded responsive layout math verified at 320×640, 360×800, and 430×932
- two elapsed-time cloud paths plus sparse Canvas twinkles that stop when hidden
- first-arrival World hint with persisted one-time state
- accessible 48dp+ Camera, Bottle, Star, Café, Author Note, and Wish touch targets
- discovery state recorded only after the placeholder scene's meaningful action
- locked Wish responses using frozen copy with no numeric progress
- one-shot 3-of-4 unlock celebration with dim, Wish glow, themed sparkles, and micro lights
- explicit day/post-finale presentation state foundation for later World transformation
- production Archive, Bottles, Stars, Café, Shell, Fish, and local Wish scenes
- explicit Keep Local versus Send consent with no request before Send
- INSERT-only Supabase REST client using one persisted UUID per logical Wish
- exact `request_id` + `message` payload with no Wish-body logging
- confirmed `23505` duplicate classification without treating every HTTP 409 as success
- unique network-constrained WorkManager retry for persisted `PENDING_SEND`
- process-restart recovery with the same persisted request UUID
- retry classification that stops permanent failures without dropping local payload
- Keep Local protection with no API call or worker
- 74 passing unit tests at the M14 CI checkpoint

Toolchain:

- JDK 17
- Android Gradle Plugin 9.3.1
- Gradle 9.5.0
- `compileSdk` / `targetSdk` 37
- `minSdk` 23

Verify the current source with:

```bash
./gradlew clean :app:testDebugUnitTest :app:assembleDebug :app:assembleRelease :app:lintDebug
```

## Supabase build configuration

Do not commit credentials. Supply the project URL and publishable key through environment variables:

```bash
export ALIA_SUPABASE_URL="https://PROJECT_REF.supabase.co"
export ALIA_SUPABASE_PUBLISHABLE_KEY="sb_publishable_..."
./gradlew :app:assembleRelease
```

The same names may be placed in the developer machine's Gradle user properties. Empty defaults keep
normal local builds reproducible, but Send remains pending until a valid configuration is supplied.
Apply `supabase/migrations/0001_create_wishes.sql` to the intended Supabase project, then run every
manual security check in `docs/SUPABASE.md` before release.

## Canonical stack

- Kotlin
- Native Android Views/XML
- ViewBinding
- Single Activity
- Layered Views for supplied artwork
- Small custom Canvas views for particles/procedural effects
- DataStore for persistent local state
- WorkManager only for pending Wish delivery
- Supabase Data REST API for Wish INSERT only
- MediaPlayer/SoundPool only if audio assets are supplied

## Remaining personal inputs

These do not block early development:

- `alia_01.webp` ... `alia_06.webp` — six square Alia photos
- `author.webp` — one square author photo
- `[AUTHOR_NAME]` — optional display/signature name
- Supabase URL + publishable key — configured outside Git; migration and M13 security validation completed
- Audio files — optional enhancement, not a release blocker unless later supplied

## Important consistency decisions

- Production Android resource filenames always use underscores, never hyphens.
- The World is **layered Android Views + Canvas effects**, not a full custom game engine.
- `wishUnlocked` means the Wish point is unlocked.
- `finaleCompleted` means the ending has been completed and post-finale World is active.
- A `PENDING_SEND` Wish may continue into the finale; the recipient is never trapped waiting for network.
- `KEEP_IT_LOCAL` must never enqueue network work.
- After the first-ever launch, the Birthday Entrance is not replayed on normal revisits.
- Audio is optional until real audio assets exist.
