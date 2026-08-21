# OPEN_ITEMS.md

These are the only expected unresolved inputs after blueprint lock.

## Needed before final production release
1. Six final square Alia photos:
   - `alia_01`
   - `alia_02`
   - `alia_03`
   - `alia_04`
   - `alia_05`
   - `alia_06`

2. One final square author photo:
   - `author`

3. Final caption-to-photo mapping after visual review.

4. M14 manual device acceptance:
   - airplane mode -> Send -> close app/process -> reconnect
   - confirm eventual delivery creates exactly one row
   - confirm Keep Local creates zero rows

5. Remove development Supabase rows listed in `AI_HANDOFF.md` before release.

## Optional
- `[AUTHOR_NAME]` for signature/display
- ambient/SFX audio assets

Audio is not a release blocker. If absent, use a no-op/silent AudioController and hide sound UI.

## Already resolved
- No new visual asset generation needed.
- Launcher icon can be assembled from supplied star artwork.
- Production resource names use underscores.
- World rendering uses layered Views + Canvas effects.
- Wish can continue to finale while PENDING_SEND.
- Birthday Entrance is first-launch only.
- Supabase migration has been applied by the user.
- M13 INSERT-only grant/RLS behavior has been live-tested.
- Supabase runtime values are supplied outside Git through `ALIA_SUPABASE_URL` and `ALIA_SUPABASE_PUBLISHABLE_KEY`.
