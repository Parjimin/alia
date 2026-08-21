---
name: birthday-pixel-android
description: Implement, debug, visually refine, and optimize Little Blue World native Android pixel-art scenes, interactions, assets, motion, and performance while preserving project specifications and low-end-device responsiveness.
---

# Birthday Pixel Android

This is an implementation playbook. It does not replace `AGENTS.md` or `docs/`.

## Before work
Read `AGENTS.md`, identify the exact `docs/ROADMAP.md` milestone, inspect the current repository, then read only relevant specs.

Common:
- visual: `DESIGN_SYSTEM.md`, `MOTION.md`, `INTERACTIONS.md`, `ASSET_MANIFEST.md`
- copy: `CONTENT.md`
- architecture: `ARCHITECTURE.md`
- performance: `PERFORMANCE.md`
- Wish backend: `SUPABASE.md`
- completion: `ACCEPTANCE_CRITERIA.md`

For nontrivial edits, plan first:
- goal
- relevant spec
- files likely to change
- implementation approach
- validation
- risks/assumptions

## Choose the simplest Android primitive
Supplied/static artwork:
- ImageView / TextView / FrameLayout / normal ViewGroup

Simple object movement:
- translationX/Y
- scaleX/Y
- rotation
- alpha

Procedural micro effects:
- custom Canvas View

Do not introduce SurfaceView, OpenGL/Vulkan, or game engines without profiling evidence and approval.

## Scene pattern
FrameLayout:
1. background
2. environment
3. supplied artwork Views
4. living/interactive Views
5. Canvas effect overlay
6. native UI overlay

Environment first, controls second.

## Assets
Never destructively edit master PNGs.
Pipeline:
master -> alpha inspection -> remove unintended outer halo -> crop -> controlled padding -> resize -> WebP -> actual-device QA

Use `docs/ASSET_MANIFEST.md`.
Production Android resource names use underscores.

Preserve intentional transparency in bottle/Wish Capsule/photo frame.

## Pixel quality
Check at actual render size:
- crisp silhouette
- clean alpha
- correct palette
- no mushy detail
- no extreme runtime scaling

Do not regenerate artwork before proving the artwork itself is the problem.

## Touch
Visual size and touch target are separate.
Prefer a touch wrapper when art is small.
Minimum target ~48dp.

## Motion
Small, slow, desynchronized.
Do not apply one generic float loop to everything.

Object personality:
- camera: gentle float + tilt
- bottle: floatier + rotate
- star: pulse
- Café: anchored
- Fish: swim
- Author Note: gentle float
- Wish Star: calm hero pulse

## Gallery
Use stable frame overlay + changing photo underneath.
Prepare previous/current/next when practical.
If swipe stutters, investigate decode timing before deleting visual features.

## Fish
Simple controller only:
spawn, move, tap milestone, escape, post-finale mode.
No NPC/pathfinding framework.

## Particles
Bounded pool.
Reuse particle objects and decoded sparkle bitmaps.
Micro dots/crosses are Canvas primitives.
Improve distribution/timing/alpha/scale before increasing count.

Effect View schedules frames only while an effect is active.

## Time-based movement
Never use frame-count movement such as `x += 1 per frame`.
Use elapsed time so 30fps and 60fps behave consistently.

## Wish
Protect the hero beats:
day->dusk, Wish Star, hold-to-seal, destination consent, capsule, finale.

Keyboard responsiveness outranks ambient effects.

Hold:
- about 1.35s
- supports cancel/reset
- completion -> SEALED only

Privacy sequence is immutable:
TYPE -> SEAL -> CHOOSE DESTINATION

Only explicit Send may trigger network state.
Keep Local must never enqueue WorkManager.

## Offline validation
Never declare complete from code inspection alone.

Test:
network off -> Send -> close app -> network on -> verify exactly one server row

Also:
Keep Local -> reconnect/restart -> verify zero server rows

## Content
Never invent or rewrite personal copy.
Fix layout if text does not fit.

## Audio
Audio is optional. If no real assets exist, preserve a polished silent experience and use no-op audio behavior.

## Performance debugging order
1. master-sized bitmap accidentally used
2. repeated decode
3. retained Views/bitmaps
4. memory leak
5. invisible animation
6. unnecessary Canvas invalidation
7. frame-time allocations
8. particle count
9. oversized runtime asset
10. ambient density

Measure before architectural escalation.

## Lifecycle
Anything that starts work must own stopping it:
- Animator
- effect frame loop
- coroutine
- Fish timer
- audio

Hidden scenes must not keep running.

## Visual QA
Capture screenshots for important scenes and check:
- focal point
- color balance
- crispness
- spacing
- safe area
- wrapping
- interaction hierarchy
- accidental generic Android styling

Check at least small/narrow and normal viewports; World also on a tall viewport.

## Build discipline
Use the repository Gradle wrapper (`gradlew.bat` on Windows).
Build/test during the milestone, not only at the end.

Report:
- files changed
- behavior
- tests
- build command/result
- visual evidence when relevant
- known limitations/deferred work

Never hide skipped validation.

## Architecture escalation
Before changing architecture report:
- problem
- evidence
- current approach
- why it cannot reasonably solve the issue
- smallest escalation
- complexity impact
- spec impact

Wait for approval.

## Final principle
The experience should feel handcrafted and special. The code should stay straightforward, native, inspectable, testable, profileable, and finishable.
