# ACCEPTANCE_CRITERIA.md

A feature is not done because it compiles. Required pattern:
implemented -> builds -> tested -> visually checked -> state verified -> no regression.

## Global
Final release must:
- install and cold launch without crash
- complete first-run flow
- use shorter revisit flow
- have no dead ends
- preserve correct Back behavior
- survive background/resume
- work when network is unavailable
- have no visible placeholder/TODO/test/debug UI
- contain no secret credentials
- build and run as optimized release

## Visual
- coherent Dreamy Pixel Ocean identity
- ocean-blue dominant
- supplied art crisp at actual size
- no obvious accidental halo/box
- no generic Material-looking contamination
- no random new illustration style

## Boot
- Pixel Boot appears quickly after native splash
- phases remain ordered
- signature `looking for something pretty...` -> `found it.` rule passes
- revisit sequence is shorter

## Birthday Entrance
First-ever launch only:
- HAPPY BIRTHDAY, ALIA.
- CTA visible
- CTA feedback immediate
- wave transition to World

## World
All landmarks accessible:
Camera, Bottle, Star, Café, Shell, Fish, Author Note, Wish.
Touch areas >=48dp.
First hint appears only on first arrival.
Idle loops are not synchronized.
No important object falls into system inset/crop.

## Progression
Only Gallery/Messages/Stars/Café count.
0/1/2 -> Wish locked.
3/4 -> `wishUnlocked = true`.
Restart must preserve unlocked state.

## Gallery
- 6 square photos
- frame alpha correct
- swipe responsive
- index and caption correct
- no obvious decode hitch
- repeated swiping does not leak memory
- Back -> World

## Messages
- exactly 4 final messages
- first reveal behavior works
- reopen faster
- no clipping
- content matches CONTENT.md

## Stars
- all three collect once
- correct particle feedback
- YOUR FACE timing staged
- completion copy appears

## Café + Shell
- forecast copy exact
- tea/coffee sequences work
- Café counts discovery
- Shell does not count
- shell state persists

## Fish
- intermittent movement
- first five-tap dialogue exact
- fifth tap escape faster
- post-finale dialogue differs
- no generalized NPC bugs/hidden timers after leaving World

## Wish
- day->dusk smooth
- intro staged
- 500-char native input remains responsive
- draft persists
- 1.35s hold works, cancel resets
- sealing yields SEALED, not Send
- explicit Keep/Send selection required

### Keep Local
- zero API call
- zero WorkManager
- state KEPT_LOCAL after restart
- never uploads later

### Send Online
- PENDING_SEND persisted before network
- one server row
- state SENT on success
- success copy shown

### Send Offline
- state PENDING_SEND
- retry scheduled
- recipient continues finale immediately
- reconnect after app close eventually yields exactly one row

### Backend security
Publishable key can INSERT only.
SELECT/UPDATE/DELETE must fail.
501 chars/blank fail.
Duplicate request_id creates no second row.
No Wish body logs.

## Finale
- final message readable and complete
- P.S. is a separate beat
- About Author follows
- ending timing works
- Wander CTA sets/enters completed post-finale experience

## Post-finale
- `finaleCompleted = true`
- dusk/evening World
- moon visible
- richer tiny stars
- extra flower
- Fish post-finale mode
- later cold launch skips Birthday Entrance

## Lifecycle
Background/resume:
- no duplicated audio stream if audio exists
- no duplicate particle loop
- no reset to first-run intro
- draft preserved

## Performance
Representative weaker hardware:
- no severe World/ Gallery/ Wish typing jank
- touch reacts immediately
- no OOM
- memory does not grow indefinitely through repeated navigation
- day->dusk peak drops afterward
- optimized release-like profiling completed

## Release
- R8/minify/resource shrinking active as supported by chosen stable toolchain
- release APK tested
- zero placeholder photos when final personal photos have been supplied
- zero debug/test content
- no `sb_secret_`, service-role key, DB password
- publishable key only
- app remains fully functional without audio assets

## Final done
Product Flow ✅
Visual QA ✅
Content ✅
Interactions ✅
State ✅
Wish Privacy ✅
Backend ✅
Offline ✅
Security ✅
Performance ✅
Release Build ✅
Actual Device QA ✅
