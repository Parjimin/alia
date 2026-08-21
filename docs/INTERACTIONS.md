# INTERACTIONS.md

## General touch
Every accepted tap gets visible feedback within roughly 100ms perceived time.
Major transitions temporarily lock duplicate navigation input.

Minimum touch target: 48dp.

## Camera
Press: scale to ~0.96.
Release: return with tiny overshoot.
Then brief cream flash -> Gallery.

## Bottle
Gentle float/rotation idle.
Tap -> lift slightly -> cork/pop beat -> Messages.
First message reveal may stage/type first line. Reopen is faster.

## Star
Idle pulse.
Tap collect -> contract -> expand -> small burst -> message reveal.
Collected star cannot collect twice.

## Café
Mostly anchored.
Tap landmark -> subtle focus/zoom -> Café scene.
Tea and coffee each have tiny lift/tilt feedback.

## Shell
Mostly static with occasional shimmer.
Tap -> scale ~1.06 + tiny rotation + sparkle -> hidden message.

## Fish
Appears intermittently and swims across World.
Tap uses small squash/stretch and bubble.
First five taps use the exact content sequence.
After fifth message, pause briefly then escape at ~2× normal speed.

## Author Note
Gentle float.
Tap -> lift/seal sparkle/unfold illusion -> Author scene.

## Gallery swipe
Horizontal card follows finger directly.
Commit if displacement is about 20–25% width or velocity threshold is reached.
Otherwise snap back.
Maximum rotation around ±2°.
Very subtle drag scale (~0.985).
Frame overlay stays stable while photo changes.

## Wish point
Locked:
- subtle pulse only
- show locked copy
- no shake/red error

Unlocked:
- tap begins day->dusk Wish entry

## Wish input
Native EditText, multiline, max 500.
Draft persists.
If text is empty, hold action does not start and shows clear copy.

## Hold to seal
Required hold: ~1.35s.
Show 10 pixel progress blocks.
Early release -> progress animates back to zero.
Completion -> state SEALED.
Sealing never implies Send consent.

## Wish destination
After sealing, user must explicitly choose Keep or Send.

Keep:
- state KEPT_LOCAL
- no API call
- no WorkManager
- continue finale

Send:
- generate/reuse request_id
- persist PENDING_SEND before network
- attempt API
- online success -> SENT
- temporary failure -> schedule retry, continue finale
- duplicate same request_id -> logical success

## Wish typing + keyboard
When IME is visible:
- typing/cursor/IME takes priority
- Wish Star may move up and shrink
- nonessential particle density may reduce
- no layout jump that hides the text field/button

## System Back
- message panel: close panel first
- Gallery/Messages/Stars/Café/Author: return World
- Wish with draft: custom leave confirmation
- World root: normal Android back/exit
