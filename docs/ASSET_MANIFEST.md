# ASSET_MANIFEST.md

## Master policy
The original 25 PNGs are immutable masters. Never overwrite them.

Original master names may contain hyphens. **Android production resources must use underscores.**

Production path:
`app/src/main/res/drawable-nodpi/`

Pipeline:
MASTER PNG -> inspect alpha -> remove unintended outer halo -> crop -> controlled padding -> resize from master -> WebP -> actual-device QA.

Default transparent pixel art format: WebP lossless.
Photos: WebP lossy, start around Q86 and raise per image if faces/detail suffer.
Opaque backgrounds: compare lossless vs high-quality lossy; keep lossless if pixel edges/banding degrade.

Do not use destructive global alpha thresholds; preserve intentional glass/internal alpha.

## Final manifest

| # | Android resource | Master | Production target | Treatment | Role |
|---|---|---|---|---|---|
| 01 | `world_day.webp` | `1_world-day.png` | keep 941×1672 | WebP QA | day World |
| 02 | `world_dusk.webp` | `2_world-dusk.png` | keep 941×1672 | same policy | Wish/post-finale |
| 03 | `island.webp` | `3_island.png` | ~768px long edge | crop/lossless | auxiliary only |
| 04 | `cafe.webp` | `4_cafe.png` | ~768px long edge | crop/lossless | landmark + Café hero |
| 05 | `cloud_01.webp` | `5_cloud-01.png` | ~960px wide | crop/lossless | far parallax |
| 06 | `cloud_02.webp` | `6_cloud-02.png` | ~768px wide | halo cleanup | near parallax |
| 07 | `camera.webp` | `7_camera.png` | ~448px long edge | crop/lossless | Archive |
| 08 | `bottle.webp` | `8_bottle.png` | ~448px long edge | preserve glass alpha | Messages |
| 09 | `shell.webp` | `9_shell.png` | ~448px long edge | crop/lossless | bonus |
| 10 | `fish.webp` | `10_fish.png` | ~512px wide | strong halo cleanup | Easter Egg |
| 11 | `star.webp` | `11_star.png` | ~384px | crop/lossless | Stars |
| 12 | `wish_star.webp` | `12_wish-star.png` | ~896px long edge | hero/lossless | Wish |
| 13 | `sparkle_01.webp` | `13_sparkle-01.png` | 128px | aggressive crop | main sprite |
| 14 | `sparkle_02.webp` | `14_sparkle-02.png` | 160px | aggressive crop | rare cluster |
| 15 | `flower.webp` | `15_flower.png` | 192px | crop/lossless | rare accent |
| 16 | `bow.webp` | `16_bow.png` | 256px | crop/lossless | rare accent |
| 17 | `tiny_shell.webp` | `17_tiny-shell.png` | 160px | crop/lossless | decoration |
| 18 | `wish_capsule.webp` | `18_wish-capsule.png` | ~768px long edge | preserve alpha | Wish send |
| 19 | `author_note.webp` | `19_author-note.png` | ~448px long edge | crop/lossless | Author landmark |
| 20 | `photo_frame.webp` | `20_photo-frame.png` | 960×960 canvas | preserve center alpha | Archive overlay |
| 21 | `wave_strip.webp` | `21_wave-strip.png` | ~1024px wide, preserve useful cropped aspect | seam QA | Boot transition |
| 22 | `loading_star.webp` | `22_loading-star.png` | 128×128 final canvas | heavy crop | Boot |
| 23 | `teacup.webp` | `23_teacup.png` | ~384px long edge | halo cleanup | Café |
| 24 | `coffee.webp` | `24_coffee.png` | ~384px long edge | halo cleanup | Café |
| 25 | `moon.webp` | `25_moon.png` | ~640px long edge | halo cleanup | post-finale |

The wave height is **not** hardcoded to 160px; preserve the useful wave profile and validate `[A][B]` seam visually.

## Photos
Final source photos are square.

Production:
- `alia_01.webp` ... `alia_06.webp`: 768×768, WebP lossy around Q86 starting point
- `author.webp`: 640×640, WebP lossy around Q86 starting point

Adjust per-image compression upward if face/hair/skin/background quality visibly suffers.

Gallery should prepare previous/current/next only, not all six full decoded images.

## Special QA
- `world_day` <-> `world_dusk`: composition alignment
- `photo_frame`: truly transparent center
- `wave_strip`: side-by-side seam
- `bottle`, `wish_capsule`: preserve intended translucent glass
- `fish`, `teacup`, `coffee`, `moon`, `cloud_02`: external halo cleanup
- sparkles: still readable at actual tiny render size

## Launcher icon
Create adaptive icon from existing art:
- background: Ocean Blue
- foreground: clean crop of `loading_star` (preferred) or supplied star
No new illustration required.
