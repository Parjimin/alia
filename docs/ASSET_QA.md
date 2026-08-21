# M2 Production Asset QA

Date: 2026-08-21

## Result

- All 25 immutable PNG masters and 25 prepared PNG finals retained their original SHA-256 hashes.
- All 25 prepared finals were converted to lossless WebP in `app/src/main/res/drawable-nodpi/`.
- Every decoded WebP matches its corresponding prepared PNG with `0` changed pixels and identical dimensions.
- Runtime filenames use Android-safe underscore names.
- Prepared asset bytes decreased from 7,556,329 to 5,666,478 bytes (25.01%) without a pixel change.

## Special checks

- `world_day` and `world_dusk` are both 941×1672 and their horizon, island, and foreground composition align.
- `photo_frame` remains 960×960; its central 400×400 sample is fully transparent.
- `wave_strip` is 1024×110 and three adjacent tiles have no visible vertical seam.
- `bottle` and `wish_capsule` each retain 255 distinct alpha levels, preserving glass translucency.
- Fish, teacup, coffee, moon, and cloud silhouettes were inspected on a contrasting background with no obvious rectangular matte or newly introduced halo.
- Sparkles remain legible at their 128×128 and 160×160 production canvases.
- The launcher uses the existing `loading_star` on Ocean Blue, with adaptive, legacy, round, and monochrome declarations.

## Build behavior

The debug APK intentionally contains the complete production asset catalog for upcoming scenes. Resource shrinking may remove not-yet-referenced assets from the current M2 release skeleton; later scene milestones will reference them normally.

Lint's temporary `UnusedResources` warnings for not-yet-built scenes are expected at M2 and must disappear as those scenes are implemented. They are not globally suppressed.
