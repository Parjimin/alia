# AGENTS.md

## Project
**Little Blue World — for Alia**

This repository is specification-driven. Read the relevant document under `docs/` before changing behavior. Do not invent product behavior, personal copy, visual direction, or architecture when already specified.

## Source of truth
- `docs/PRODUCT.md`
- `docs/UX_FLOW.md`
- `docs/DESIGN_SYSTEM.md`
- `docs/CONTENT.md`
- `docs/INTERACTIONS.md`
- `docs/MOTION.md`
- `docs/ASSET_MANIFEST.md`
- `docs/ARCHITECTURE.md`
- `docs/PERFORMANCE.md`
- `docs/SUPABASE.md`
- `docs/ACCEPTANCE_CRITERIA.md`
- `docs/ROADMAP.md`

If specs conflict: stop, quote the conflict, and request review. Do not silently improvise.

## Required stack
Use Kotlin, Native Android Views/XML, ViewBinding, single Activity, layered Android Views, small custom Canvas effects, Preferences DataStore, Kotlin Coroutines, and WorkManager only for pending Wish delivery.

Use MediaPlayer/SoundPool only if audio assets are actually supplied.

## Forbidden by default
Do not introduce Jetpack Compose, React Native, Flutter, Unity, Godot, libGDX, OpenGL/Vulkan, Fragments, Navigation Component, Room, Hilt, Dagger, Koin, Retrofit, Ktor, Glide, Coil, Picasso, Lottie, Rive, Firebase, or the full Supabase client SDK without explicit architecture review and concrete evidence.

## Architecture rule
This is neither an enterprise app nor a full game. Prefer normal Views + property animations + bounded Canvas effects. Do not create ECS, physics, generalized NPC systems, rendering frameworks, or repositories/use cases for static content.

## Visual rule
Supplied artwork is the visual source of truth. Preserve ocean-blue dominance, cream/pale-blue surfaces, pink/lavender accents, and soft modern pixel art. Avoid generic Material UI, glassmorphism, blur-heavy UI, generic gradients, pill buttons everywhere, and random new illustration styles.

## Asset rule
Never overwrite master assets. Runtime must use processed production assets from `docs/ASSET_MANIFEST.md`. Android resource filenames use underscores only.

## Performance rule
Preserve the experience first. Investigate oversized bitmaps, repeated decode, lifecycle/leaks, invisible animation, unnecessary redraw, per-frame allocation, and particle density before removing designed effects.

## Rendering rule
Use normal Views for supplied artwork. Use Canvas mainly for micro particles, tiny stars, pixel crosses, sparkle movement, temporary bursts, and small procedural effects. Do not turn the app into one continuously redrawn giant Canvas.

## Content rule
Personal copy is frozen in `docs/CONTENT.md`. Do not rewrite, formalize, romanticize, add quotes, invent inside jokes, or add extra compliments. If text does not fit, adjust the layout.

## Relationship rule
Never assign a formal relationship between author and Alia. Do not add "my love", girlfriend, soulmate, princess, forever, or similar language.

## Navigation rule
No bottom navigation. The Little Blue World itself is the primary navigation surface. System Back must remain predictable.

## Progression rule
Main discoveries are exactly: Alia Archive, Message Bottles, Collectible Stars, Tiny Café. Wish unlocks at 3 of 4. Shell, Fish, and About the Author do not count. Once `wishUnlocked` becomes true, it never relocks.

## Wish privacy rule
The recipient must explicitly choose `KEEP IT WITH ME` or `SEND IT INTO THE LITTLE WORLD`. No upload before explicit Send. Send disclosure must clearly state that the person who made the app can see the Wish.

## Supabase security rule
APK may contain only Supabase URL + publishable key. Never include `sb_secret_`, service-role keys, DB passwords, or personal access tokens. Client capability is INSERT of `request_id` + `message` only. No SELECT/UPDATE/DELETE.

## Wish logging rule
Never log Wish message content in Logcat, HTTP logs, worker logs, crash strings, or debug output.

## Offline Wish rule
If Send was selected and delivery fails: persist `PENDING_SEND` and schedule WorkManager retry. If Keep Local was selected: never enqueue or upload.

## Dependency rule
Before adding a dependency, explain the exact problem, why Android SDK is insufficient, and the cost/benefit. Avoid dependency accumulation.

## Implementation rule
Work milestone-by-milestone from `docs/ROADMAP.md`. For nontrivial work: inspect -> plan -> implement -> test -> build -> visually verify -> report evidence.

## Scope rule
Implement only the requested milestone. Report unrelated improvements separately.

## Testing rule
Never claim something works without evidence. Use tests, Gradle output, screenshots/recordings, profiler evidence, or Supabase results as appropriate.

## Error rule
Never expose raw HTTP/exception/database errors to the recipient. User-facing copy must come from `docs/CONTENT.md`.

## Lifecycle rule
Pause unnecessary animation, Canvas scheduling, and audio when backgrounded. Resume current experience without replaying the first-run Birthday Entrance.

## State rule
Persist progression, `wishUnlocked`, `finaleCompleted`, Wish state/draft/pending payload, first-open state, and sound preference. Do not persist exact particle/cloud/fish positions.

## Stop-and-ask rule
Stop before proceeding if specs conflict, a supplied asset is unusable, a forbidden dependency seems necessary, product/privacy behavior must materially change, or a performance fix would remove a designed experience.

## Definition of done
A successful compile is not enough. Follow `docs/ACCEPTANCE_CRITERIA.md`.
