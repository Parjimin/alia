# MOTION.md

## Motion personality
Soft, dreamy, deliberate, small-amplitude. Avoid hyperactive bounce, constant shaking, elastic overshoot, and synchronized idle loops.

Preferred 60fps; stable ~30fps is acceptable on weaker hardware. Movement uses elapsed time, never frame-count logic.

## Duration tokens
- Instant: 80–120ms
- Micro: 120–180ms
- Short: 180–300ms
- Medium: 350–600ms
- Scene: 700–1100ms
- Emotional: 1200–2200ms

Default easing: gentle ease-out / ease-in-out / linear slow for environment.

## Idle amplitudes
Typical:
- translation: 2–6dp
- rotation: ±1–2°
- scale: 1.00 -> 1.03/1.06

Randomize initial phase/delay slightly; do not synchronize.

## Camera
Idle 3.4–4.2s, translateY ~3dp, rotate ±0.7°.
Camera flash full-screen opacity peak ~0.75 for 120–180ms.

## Bottle
Idle translateY ±3dp, rotate ±1.3°, 3.0–3.8s.

## Star
Idle scale 1.00 -> 1.045 -> 1.00, 2.2–3s.
Collect burst: 4–8 themed sparkles + 4–8 micro lights.

## YOUR FACE timing
- title
- 700–900ms
- `...`
- 700–900ms
- `yeah.`
- ~300ms
- `you're pretty.`
- ~500ms
- `moving on.`

## Café
World landmark mostly static.
Entry focus/zoom 650–900ms.
Tea/coffee tap 240–320ms.

## Fish
Normal speed 20–45dp/sec.
Vertical wobble amplitude 2–5dp, period 1.4–2.2s.
Appearance interval roughly 8–20s.
Escape after fifth tap at about 2× normal.

## Clouds
Cloud 01: 2–5dp/sec, farther/slower.
Cloud 02: 4–8dp/sec, nearer/slightly faster.
Recycle offscreen with slight vertical/scale/delay variation.

## Loading star
Scale 1 -> 1.08 -> 1 and opacity 0.7 -> 1 -> 0.7 over 1.6–2.2s.

## Loading text
Fade out 80–120ms; new line fade in 120–180ms.
Signature:
- `looking for something pretty...` hold ~900–1200ms
- brief pause ~200ms
- `found it.` hold ~900–1200ms
- tiny sparkle burst

## Birthday reveal
`HAPPY BIRTHDAY,` reveal 350–500ms.
Pause 300–500ms.
`ALIA.` reveal 450–650ms.
CTA appears 500–700ms after title settles.

## Wave transition
World entrance total 800–1100ms.

## World first settle
Background -> ~150ms -> clouds -> ~150ms -> landmarks -> ~300ms -> first hint.

## Wish unlock
Total ~1.4–2.0s:
- normal ambience
- distant chime
- subtle luminance dip
- Wish point glow
- 4–6 sparkle_01, max 1 sparkle_02, 6–12 micro lights
- `something changed.`

## Day -> dusk
Crossfade 1.2–1.8s.
No hard cut or black flash.
Optional very subtle lavender tint during middle.

## Wish intro
After dusk settle:
- pause ~400ms
- `one last thing.`
- hold 700–1000ms
- fade
- `make a wish.`

## Wish Star
Entrance 700–1000ms:
alpha 0->1, scale 0.88->1, translateY +8dp->0.
Idle scale 1.00 -> 1.025 -> 1.00 over 2.6–3.4s.
Do not continuously rotate the full hero bitmap.

## Hold
1.35s duration.
Early release returns progress to zero over 180–280ms.
Completion gets one soft haptic if enabled.

## Wish Capsule
Entrance 450–650ms: scale 0.8->1, rotation -2°->0, alpha 0->1.

Keep Local:
settle downward 24–40dp, 2–4 sparkles, total 700–1000ms.

Send success:
glow, tiny sparkles, translate upward, scale 1->0.75, alpha 1->0 over 900–1300ms.

Offline:
capsule attempts to rise, stops, gently returns.

## Final message
Paragraph reveal 350–550ms each.
No full-paragraph typewriter.

## Author reveal
Title first, photo 250–400ms later.
Photo scale 0.96->1 + alpha over 300–450ms.
Stats stagger 50–90ms.

## Ending timing
`okay.` -> ~600ms
`that's actually it.` -> ~900ms
`probably.` reveal quickly (~200ms)

## Post-finale
Moon first appearance: alpha + small rise over 900–1400ms.
Star field mostly Canvas micro lights; only several twinkle at once.

## Particle budgets
World typical:
- micro: 6–14
- themed sparkle sprites: 2–6

Wish hero:
- micro: 12–24
- sparkle_01: 5–10
- sparkle_02: 0–2

Use bounded pooling and reuse decoded sprite bitmaps.

## Adaptive density
If needed:
- FULL: normal ambience
- BALANCED: ~70% ambient density
- LIGHT: ~40–50% ambient density, one cloud actively drifting at a time

Never remove core interaction feedback, Gallery gesture, Fish, day->dusk, Wish hero beats, or finale.
