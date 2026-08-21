# PERFORMANCE.md

## Principle
**Semewah mungkin secara experience, seringan mungkin secara implementation.**

Optimize implementation before removing designed visual richness.

## Targets
Preferred: 60fps.
Acceptable on weaker device: stable ~30fps.
Not acceptable: frequent hitching, touch lag, keyboard lag, random freezes.

Project internal memory guidance:
- normal World decoded raster target: <= ~18MB
- Gallery raster target: <= ~16MB
- Wish normal: <= ~22MB
- day->dusk short peak: <= ~30–35MB
- preferred steady total app memory: < ~90MB
- short cinematic peak: < ~120MB

These are project guardrails, not Android OS limits.

## Bitmap rules
ARGB_8888 is expensive; runtime dimensions matter more than compressed file size.

Never use 1254×1254 master art for a tiny landmark.
Never repeatedly decode sprite bitmaps.
Custom Canvas sprite bitmaps decode once and reuse.

`world_day` and `world_dusk` keep their 941×1672 master dimensions for production unless real-device profiling proves another size is better.

Normal World loads day only.
Approaching Wish:
- preload dusk
- temporarily coexist for crossfade
- release day strong reference after no longer needed

Do not preload day + dusk + six photos + Wish heroes at startup.

## Gallery
Production photo 768×768 is roughly 2.25MiB decoded in ARGB_8888.
Prepare previous/current/next only.
Keep photo frame decoded once during Gallery.

Rapid swiping all six must not cause visible decode freeze or permanently rising memory.

## Particle system
Pool capacity about 48 slots.
Typical World active count ~8–18.
Wish peak ~20–36 total bounded particles.

Micro dots/crosses are Canvas primitives.
Many sparkle instances share one decoded `sparkle_01`/`sparkle_02` bitmap.

No per-frame allocation storm.

## Continuous drawing
Effects view schedules frames only while particles/procedural transitions are active.
Static screens stop continuous invalidation.

Hidden screens stop:
- particle updates
- cloud animations
- Fish timers
- idle loops

## IME mode
When Wish keyboard is active:
- prioritize EditText/cursor/IME
- reduce/pause ambient effects if necessary
- no sparkle effect is allowed to make typing lag

## APK size
Soft target <= ~20MB release.
Review >25MB.
>30MB is a red flag unless clearly justified.
Do not visibly damage artwork merely to hit a round number.

## Startup
Technical goal:
tap -> native splash -> Pixel Boot in roughly <=1.5s on representative weaker hardware.

Pixel Boot can intentionally last longer as an experience, but technical startup should not stall before it.

## Touch
Accepted tap should produce visible reaction within about 100ms perceived time.

## Failure-debug order
1. accidental master-size bitmap
2. repeated decoding
3. retained old Views/bitmaps
4. memory leak
5. invisible animation
6. unnecessary Canvas invalidation
7. frame-time allocation
8. particle count
9. oversized production asset
10. ambient density
11. architecture escalation only after evidence

## Adaptive density
Do not build complex auto-quality logic before profiling.

Possible profiles only if needed:
- FULL
- BALANCED (~70% ambient density)
- LIGHT (~40–50%; one actively drifting cloud at a time)

Same story, same interactions, same core Wish/finale effects.

## Required profiling matrix
- cold launch
- first Pixel Boot
- World idle 60s
- World + Fish + particles
- rapid Gallery swipes
- all Bottles
- all Stars
- Café taps
- Wish unlock
- day->dusk
- Wish typing to 500 chars
- seal
- online send
- offline send
- finale
- post-finale World
- background/resume
- repeated scene navigation

Final conclusions must include an optimized release-like build, not debug only.
