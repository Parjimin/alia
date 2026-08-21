# UX_FLOW.md

## 1. First launch
Cold launch:
1. Android native splash (brief only)
2. Pixel Boot
3. staged loading phases
4. Birthday Entrance
5. `[ open your little world ]`
6. wave transition
7. Little Blue World

Only the first-ever launch shows the Birthday Entrance.

## 2. Pixel Boot
First-run loading has five ordered phases:
1. Pixels
2. Ocean
3. Color
4. Personal
5. Birthday

Within each phase, choose 1–2 lines semi-randomly. Grouped sequences stay grouped.

Signature rule:
`looking for something pretty...` must be immediately followed by `found it.`

Final first-run loading line: `ready.`

## 3. Revisit loading
If app has opened before, use a shorter revisit sequence and go directly to World.

Pre-finale examples:
- oh, you're back.
- waking the ocean up...
- checking if the fish is still here...
- yep.
- welcome back, alia.

Post-finale examples:
- the stars remembered you.
- the ocean's still here.
- the fish is pretending not to notice.
- welcome back, alia.

## 4. Birthday Entrance
Display:
`HAPPY BIRTHDAY,`
then
`ALIA.`

CTA:
`open your little world`

Tap -> wave rises, covers screen, World is prepared underneath, wave recedes.

## 5. Little Blue World
Primary interactive landmarks:
- Camera -> Alia Archive
- Bottle -> Message Bottles
- Star -> Collectible Stars
- Café -> Tiny Café
- Shell -> surprise
- Fish -> Easter Egg
- Author Note -> About the Author
- Wish point -> Make a Wish

First-arrival hint only:
`there are a few things hiding around here.`
then
`tap anything that looks suspicious.`

## 6. Wish locked/unlocked
Before 3 main discoveries:
- Wish point is visible but subdued.
- tapping shows soft locked copy.
- no numerical progress is shown.

At the third discovery:
- return to World
- subtle chime
- luminance dip
- Wish point gains controlled glow
- brief particles
- text: `something changed.`

Do not auto-open Wish.

## 7. Alia Archive
- 6 square photos
- one stable ornate frame overlay
- horizontal swipe
- index `01 / 06` ... `06 / 06`
- one caption per image
- system Back returns to exact World state

## 8. Message Bottles
Four spatial bottles. First open gets staged reveal; reopen is faster. No vertical settings-style list.

## 9. Collectible Stars
Three stars:
- YOUR ENERGY
- YOUR MIND
- YOUR FACE

The third uses timed comedic reveal.

After all three:
`you found all three.`
then
`good job, apparently.`

## 10. Tiny Café
Café is a small World landmark and larger hero in its scene.

Display TODAY'S FORECAST plus tea/coffee micro-interactions.

## 11. Shell
Tiny one-off surprise. Does not count toward Wish unlock.

## 12. Fish
Intermittent swimming object. Five-tap first sequence. On tap five, says Happy Birthday then escapes faster. Post-finale uses different dialogue.

## 13. About the Author
Can be found early through the Author Note.
It is also shown again after the final birthday message; early discovery does not remove the finale author beat.

## 14. Make a Wish
Entry:
- tap unlocked Wish point
- World interaction disables
- day -> dusk crossfade
- `one last thing.`
- `make a wish.`
- Wish Star appears
- tap Wish Star -> input

Input:
- multiline
- max 500 characters
- draft persists locally
- hold-to-seal ~1.35s

After sealing:
`where should this wish go?`

Choice A:
`KEEP IT WITH ME`
`this wish stays on this device.`

Choice B:
`SEND IT INTO THE LITTLE WORLD`
`the person who made this can see it.`

No network request occurs before explicit Choice B.

## 15. Keep Local
- state -> KEPT_LOCAL
- no network request
- no worker
- show `safe with you.`
- continue to finale

## 16. Send
Persist `PENDING_SEND` before attempting network.

If online success:
- state -> SENT
- capsule departs
- `wish sent.`
- `maybe someone can make a tiny part of it come true.`

If offline/temporary failure:
- keep pending payload
- schedule retry
- show universe-themed offline copy
- continue to finale immediately

The recipient is never trapped waiting for network.

## 17. Final message
Remain in dusk atmosphere. Reveal the final message by paragraph, not full typewriter.

Then:
`p.s.`
`yes,`
`i actually made an app for this.`

CTA:
`there's one more thing`

## 18. Author + ending
Show About the Author, then:
`okay.`
`that's actually it.`
`probably.`

CTA:
`wander around again`

After CTA:
- set `finaleCompleted = true`
- show post-finale World
- moon + extra stars + extra flower
- Fish uses post-finale dialogue

## 19. Subsequent launches
After first open:
- short revisit loading
- direct to appropriate World state
- no automatic Birthday Entrance replay

## 20. Back behavior
- subscene Back -> World
- nested message Back -> close message first
- Wish with typed draft -> custom leave confirmation
- World root Back -> normal Android behavior
- never show generic “Are you sure you want to exit?” dialog
