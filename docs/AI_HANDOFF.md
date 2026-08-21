# AI_HANDOFF.md

## Purpose

This is the operational handoff for the next AI or developer continuing **Little Blue World — for Alia**. It summarizes project intent, user preferences, implementation state, known gaps, and the exact next step.

The chat history is supporting context only. Repository source and the specification files listed in `AGENTS.md` remain authoritative.

## Resume point

- Repository: `Parjimin/alia`
- Working branch to resume from: `codex/m14-offline-delivery`
- Draft pull request: `https://github.com/Parjimin/alia/pull/1`
- Local linear checkpoint: `5313904` (`feat: add offline Wish delivery`)
- Implemented through: **M14**
- Next implementation milestone: **M15 — Finale**
- Do not start M16 in the same task unless the user explicitly asks.

The remote `main` branch is a consolidated M0–M13 checkpoint. The local repository contains a clean linear M0–M14 history with one milestone commit per step. The branch name above, not a remembered commit SHA, should be treated as the remote resume pointer.

## Read this first

Read in this order before editing:

1. `docs/AI_HANDOFF.md` — current state and continuation rules.
2. `AGENTS.md` — binding engineering and product constraints.
3. `docs/MILESTONE_STATUS.md` — completed work and evidence.
4. `docs/ROADMAP.md` — implement one bounded milestone only.
5. Relevant source-of-truth specs for the requested milestone.

For M15 specifically, read:

1. `docs/CONTENT.md` — `Final birthday message` and `P.S.` are frozen copy.
2. `docs/UX_FLOW.md` — sections 17–19.
3. `docs/MOTION.md` — Final message reveal timing.
4. `docs/INTERACTIONS.md` — Back and duplicate-input rules.
5. `docs/DESIGN_SYSTEM.md` — dusk visual language and asset restrictions.
6. `docs/ARCHITECTURE.md` — coordinator, screen lifecycle, and state ownership.
7. `docs/PERFORMANCE.md` — weaker-device budgets.
8. `docs/ACCEPTANCE_CRITERIA.md` — Finale requirements.

Then inspect these implementation entry points:

- `app/src/main/java/com/littleblueworld/alia/app/AppCoordinator.kt`
- `app/src/main/java/com/littleblueworld/alia/navigation/ScreenId.kt`
- `app/src/main/java/com/littleblueworld/alia/navigation/ScreenFactory.kt`
- `app/src/main/java/com/littleblueworld/alia/content/BirthdayContent.kt`
- `app/src/main/java/com/littleblueworld/alia/wish/`
- the production screen factories for patterns already used by M7–M14

## Product intent

Little Blue World is a portrait native Android birthday APK for **Pradipa Fauziyyah Alya**, displayed as **Alia**, whose birthday is **23 July**.

The app is a gift from a close friend. Never assign a formal relationship between the author and Alia. Do not add girlfriend, soulmate, princess, forever, `my love`, or similar language.

Desired feeling:

- playful first;
- personal and warm underneath;
- subtly flirty in small moments;
- sincere during Wish and Finale;
- self-aware and funny during the Author reveal.

Avoid cringe, excessive romance, Valentine styling, childish UI, generic birthday templates, generic Material dashboards, and obvious AI-template composition.

Content is frozen in `docs/CONTENT.md`. If text does not fit, change layout—not copy.

## User priorities and collaboration style

- The recipient uses a low-end-class device comparable to Samsung A04, portrait 720×1600, roughly 3–4 GB RAM.
- Smooth touch response, stable memory, and a small APK matter more than architectural fashion.
- Keep the rich interactions, but implement them with lightweight native primitives.
- The user wants progress visible on GitHub because they cannot access a transient agent workspace.
- Commit each bounded milestone separately and publish a reviewable branch/PR when access permits.
- Report concrete outcomes first: source changed, tests passed, build status, artifact size, and remaining manual gates.
- Do not spend long periods producing screenshots or recordings for routine checkpoints. The previous software-only emulator made visual capture extremely slow and unreliable. Prefer deterministic tests, build/lint output, state inspection, and GitHub Actions evidence. If actual visual/device QA remains necessary, state that honestly and defer it rather than claiming it passed.
- Never claim a device, lifecycle, performance, or network scenario was tested unless it actually ran.
- Work on only the requested milestone. Do not silently continue into the next milestone.

## Non-negotiable technical constraints

- Kotlin.
- Native Android Views/XML with ViewBinding.
- Single Activity.
- Layered Android Views for supplied art.
- Small, bounded Canvas effects for particles and micro lights.
- Preferences DataStore for persistent app state.
- Kotlin Coroutines.
- WorkManager only for pending Wish delivery.
- No Compose, Fragments, Navigation Component, game engine, DI framework, image-loading framework, Retrofit/Ktor, full Supabase SDK, or other forbidden dependency without explicit review.
- No login, analytics, tracking, Firebase, remote CMS, cloud photos, or admin panel.
- No giant continuously redrawn Canvas and no generalized game/NPC framework.
- No `largeHeap`.
- Hidden scenes must stop animators, Canvas scheduling, Fish timers, and unnecessary bitmap retention.

Canonical toolchain:

- JDK 17
- Android Gradle Plugin 9.3.1
- Gradle 9.5.0
- `compileSdk` / `targetSdk` 37
- `minSdk` 23

## Architecture snapshot

- `MainActivity` owns the root binding and delegates behavior.
- `AppContainer` creates repositories, API integration, and delivery orchestration.
- `AppCoordinator` owns active scene creation, navigation, transitions, lifecycle, and state rendering.
- `ScreenHost` contains the current scene; `GlobalOverlayHost` contains transition/global effects.
- `AppStateRepository` / `DataStoreAppStateRepository` persist progression and Wish state.
- Production scenes own their animations and release them when hidden/destroyed.
- `FINAL_MESSAGE` and `AUTHOR` still route through temporary surfaces at the M14 checkpoint.

Persistent state includes:

- first-open / Birthday Entrance completion;
- first World hint;
- four main discoveries;
- Shell found;
- Fish dialogue milestone;
- `wishUnlocked`;
- Wish draft/state/pending request payload;
- pending retry eligibility;
- `finaleCompleted`;
- optional sound preference.

Do not persist particle, cloud, Fish, drag, or transition frame positions.

## Frozen progression and navigation decisions

- Main discoveries are exactly Archive, Bottles, Stars, and Café.
- Wish unlocks after the first meaningful interaction in any 3 of 4 discoveries.
- Shell, Fish, and Author do not count.
- `wishUnlocked` never regresses.
- The World is the navigation surface; there is no bottom navigation.
- Normal revisits skip Birthday Entrance.
- A pending network Wish never blocks the recipient from entering Finale.
- `finaleCompleted` becomes true only after the ending/Wander completion, not after Wish submission.

## Wish privacy and backend invariants

- `SEALED` is not consent to upload.
- Keep Local means `KEPT_LOCAL`, zero API call, and zero WorkManager forever.
- Send persists `PENDING_SEND` and one UUID before the first request.
- Every retry reuses the same UUID and reads the message from DataStore.
- The worker carries no Wish body in WorkManager input data.
- Temporary network/server failure retries; permanent configuration/payload failure stops looping while preserving local payload.
- A confirmed PostgreSQL `23505` duplicate for the persisted UUID is logical success.
- Never log Wish message content.
- APK capability is INSERT-only for `request_id` and `message`.
- Never commit or bundle service-role keys, `sb_secret_` values, database passwords, or personal access tokens.

The Supabase migration `supabase/migrations/0001_create_wishes.sql` has already been applied by the user. M13 live security checks passed: valid INSERT succeeded; SELECT/UPDATE/DELETE, setting `created_at`, blank messages, and 501-character messages were denied; duplicate UUID produced `409` with PostgreSQL `23505` and no second logical Wish.

The project URL and publishable key must be supplied in environment settings using:

- `ALIA_SUPABASE_URL`
- `ALIA_SUPABASE_PUBLISHABLE_KEY`

Do not place their real values in repository documentation or source.

Development rows to verify and remove before final release if still present:

- `27b96167-86d6-455f-b162-e75f07a9f9de`
- `810832a9-afad-466a-92ae-e7530bbc3b0d`

## M14 checkpoint truth

M14 implementation and automated validation are complete:

- network-constrained unique WorkManager delivery;
- exact unique work name `little_blue_world_pending_wish`;
- `ExistingWorkPolicy.KEEP`;
- exponential retry with the Android minimum 10-second backoff;
- process-restart recovery from persisted `PENDING_SEND`;
- retry-disabled permanent-failure state;
- same UUID across direct send and retries;
- no Wish payload in worker input or logs;
- Keep Local does not schedule retry;
- 74/74 unit tests passed in GitHub Actions;
- debug, lint, minified release, and resource shrinking passed;
- minified unsigned release size was 5,722,283 bytes.

Live network simulation used one UUID and produced:

- deliberately unreachable target: HTTP `000`;
- reconnect to Supabase using the same UUID: HTTP `201`;
- duplicate retry using the same UUID: HTTP `409`, PostgreSQL `23505`.

One acceptance gate remains explicitly incomplete:

- actual Android airplane mode -> Send -> close process -> reconnect -> confirm exactly one row.

Do not repeat the M14 implementation. Run that manual device gate later when a reliable device/emulator is available. Keep the M14 PR draft until the gate is complete or the user explicitly accepts deferral.

## Exact M15 scope

M15 replaces the temporary `FINAL_MESSAGE` screen with a production Finale screen.

Entry is allowed after any terminal/local-continuation Wish result:

- `KEPT_LOCAL`;
- `PENDING_SEND`;
- `SENT`.

Implement only:

1. The frozen final birthday message from `docs/CONTENT.md`.
2. Paragraph-by-paragraph reveal, 350–550 ms per paragraph; no full-paragraph typewriter.
3. A separate P.S. beat:
   - `p.s.`
   - `yes,`
   - `i actually made\nan app for this.`
4. CTA: `there's one more thing`.
5. Lifecycle-safe, duplicate-tap-safe transition to `ScreenId.AUTHOR`.
6. Responsive/readable layout on short and tall portrait screens, with scrolling if required.

M15 must not implement:

- full About Author layout, stats, photo, or personal note (M16);
- `okay.`, `that's actually it.`, `probably.`, or Wander CTA (M16);
- setting `finaleCompleted` (M16/M17 handoff);
- moon, richer stars, flower, post-finale Fish, or revisit transformation (M17);
- final personal photo mapping (M18);
- audio (M19, optional).

M15 validation should cover:

- exact content and order;
- P.S. is a separate beat;
- all valid Wish states can continue;
- CTA activates once and routes to Author;
- background/resume does not duplicate reveal jobs or navigation;
- scene teardown cancels animators/coroutines;
- short-screen content remains accessible;
- unit tests, debug build, lint, and minified release build.

## Milestone workflow for the next AI

For every requested milestone:

1. Inspect current branch, source, relevant specs, and dirty status.
2. State the bounded implementation plan.
3. Implement only that milestone.
4. Add focused deterministic tests.
5. Run debug/unit/lint/minified-release validation.
6. Perform visual/device validation only when practical and material; never fabricate it.
7. Audit permissions, dependencies, logs, state, and APK size where relevant.
8. Commit with one clear milestone commit.
9. Publish to a feature branch/draft PR without force-pushing `main`.
10. Report remaining manual gates separately from completed automated checks.

Never reset, overwrite, or discard unrelated user changes. Never recreate the repository from screenshots or chat when Git source is available.

## Build and verification

From repository root:

```bash
chmod +x gradlew
./gradlew clean \
  :app:testDebugUnitTest \
  :app:assembleDebug \
  :app:lintDebug \
  :app:assembleRelease
```

For a configured Supabase build, provide the two environment variables through the environment configuration, not a committed file.

Expected artifact roles:

- `app-debug.apk`: debug-signed, directly installable test package; larger and not for final distribution.
- `app-release-unsigned.apk`: minified release candidate that still needs release signing before distribution.

## Ready-to-paste prompt for the next AI

```text
Continue the Android project Little Blue World for Alia from repository
Parjimin/alia, branch codex/m14-offline-delivery.

Before editing, read docs/AI_HANDOFF.md, AGENTS.md,
docs/MILESTONE_STATUS.md, and every M15 source-of-truth document listed
in the handoff. Inspect the current source and Git status; do not rebuild or
repeat M0-M14.

Implement M15 — Finale only:
- production FINAL_MESSAGE screen
- frozen final birthday copy
- paragraph reveal, no long typewriter
- separate P.S. beat
- CTA `there's one more thing`
- lifecycle-safe single transition to AUTHOR
- responsive short/tall portrait layout
- focused tests, debug build, lint, and minified release build

Do not implement M16, M17, audio, new backend behavior, or new artwork.
Do not spend time on screenshots or screen recordings unless they are fast
and materially necessary; report visual/device gates honestly if deferred.
Commit the milestone separately and publish a reviewable branch/draft PR.
Report exact evidence and any remaining manual gate.
```

## Final warning

Do not infer new personal copy, relationship labels, product behavior, or architecture from this summary. When details matter, the corresponding canonical document in `docs/` wins. If two canonical specs conflict, stop and ask the user rather than improvising.
