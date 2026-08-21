# MILESTONE_STATUS.md

## Status meaning

- **Complete**: implementation and relevant automated validation are complete.
- **Code complete / manual gate open**: source and automated validation are complete, but a named real-device/manual acceptance check remains.
- **Pending**: do not implement unless explicitly requested.
- **Conditional**: implement only if its stated input or measured need exists.

## Checkpoints

The commit IDs below describe the clean local linear history. Remote branch history may use consolidated publication commits, so resume by branch name as documented in `AI_HANDOFF.md`.

| Milestone | Local commit | Status | Result |
|---|---|---|---|
| M0 Bootstrap | `e32902e` | Complete | Native single-Activity Kotlin/XML project, debug/release baseline |
| M1 Foundation | `469beb5` | Complete | Coordinator, navigator, transitions, DataStore state, tests |
| M2 Production Assets | `6c5a2fb` | Complete | 25 pixel-identical lossless WebP resources, launcher icons |
| M3 Pixel Boot | `c6052d6` | Complete | Native launch bridge, ordered boot sequence, revisit variants |
| M4 Birthday Entrance | `656051b` | Complete | Staged birthday title, CTA, wave transition |
| M5 World Core | `5f6e0bc` | Complete | Responsive day World, landmarks, clouds, ambient effects |
| M6 World Progression | `d3a5df1` | Complete | 3-of-4 discovery unlock and persistent progression |
| M7 Alia Archive | `0e1e510` | Complete | Production gallery flow with placeholders/focal support |
| M8 Message Bottles | `748a85a` | Complete | Four interactive messages and reopen behavior |
| M9 Collectible Stars | `bd20551` | Complete | Three collect interactions, timing, completion |
| M10 Café + Shell | `925d007` | Complete | Café interactions and non-counting Shell surprise |
| M11 Fish | `e25d10e` | Complete | Intermittent Fish, five-tap dialogue, escape, post-finale hook |
| M12 Local Wish | `5acf21f` | Complete | Dusk Wish UI, draft, hold-to-seal, Keep/Send consent |
| M13 Supabase | `c8b89b5` | Complete | INSERT-only REST integration and live security validation |
| M14 Offline Delivery | `5313904` | Code complete / manual gate open | WorkManager retry, idempotent UUID, restart recovery, 74 tests |
| M15 Finale | — | Pending | Final birthday message, P.S., Author transition |
| M16 About Author + Ending | — | Pending | Author layout, stats, note, ending, Wander CTA |
| M17 Post-Finale World | — | Pending | Dusk World, moon, stars, flower, completed behaviors |
| M18 Final Photos | — | Pending/input-blocked | Six Alia photos, author photo, captions/focal offsets |
| M19 Audio | — | Conditional | Skip if no real audio assets are supplied |
| M20 Performance Pass | — | Pending | Release-like profiling on representative weaker hardware |
| M21 Adaptive Quality | — | Conditional | Implement only if measured FULL profile is unstable |
| M22 Accessibility/System | — | Pending | Descriptions, font scaling, insets, Back, lifecycle, IME |
| M23 Release Hardening | — | Pending | Remove debug/test data, audit credentials/logs, shrink release |
| M24 Full Acceptance | — | Pending | Fresh-install and online/offline/process-death matrix |
| M25 Final APK | — | Pending | Signed final APK and release record |

## Current automated evidence

- GitHub Actions workflow: `.github/workflows/m14-validation.yml`
- Unit tests: 74 passed, 0 failed, 0 skipped.
- Debug build: passed.
- Lint: passed without a new M14 error; known future-asset/visual warnings remain visible rather than globally suppressed.
- Minified release build: passed.
- M14 unsigned release size: 5,722,283 bytes.
- Release APK soft target remains <= approximately 20 MB.

## Current manual and input gates

1. M14 actual-device test: airplane mode -> Send -> close app/process -> reconnect -> confirm exactly one Supabase row.
2. Verify Keep Local still creates zero server rows during final acceptance.
3. Delete development Supabase rows listed in `AI_HANDOFF.md` before release.
4. M18 requires six final square Alia photos, one author photo, and caption/focal mapping.
5. `[AUTHOR_NAME]` remains optional.
6. Audio is optional; a silent release is valid.

## Next action

Implement **M15 only** from `codex/m14-offline-delivery`, following the exact scope and prompt in `AI_HANDOFF.md`.
