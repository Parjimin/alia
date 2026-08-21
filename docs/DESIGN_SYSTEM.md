# DESIGN_SYSTEM.md

## Identity
Visual north star: **Dreamy Pixel Ocean** — modern soft pixel art, feminine, cheerful, dreamy, elegant, cute but not childish.

The result must feel like a tiny handcrafted world, not a normal Android app decorated with pixel images.

## Palette
- Ocean Blue `#59BFEF`
- Sky Blue `#A8E3FA`
- Deep Ocean `#2779B8`
- Soft Pink `#FFA9D3`
- Candy Pink `#FF7FC1`
- Lavender `#C5A6FF`
- Soft Purple `#9C78E8`
- Cream White `#FFF9FD`
- Deep Navy `#17325B`

Approximate visual balance:
- 55% blues
- 20% cream/pale blue
- 12% pink
- 10% lavender/purple
- 3% navy

No random extra palette colors unless required by supplied photography.

## Typography
Final preferred families:
- Display/headings: **Pixelify Sans**
- Body/captions: **Nunito**

Bundle font files as local Android font resources from their official open-source distribution. Do not depend on runtime network font fetching.

Use pixel display font for short headings only; never for long paragraphs.

Suggested size ranges:
- Hero: 32–40sp
- Screen title: 22–28sp
- Section title: 18–22sp
- Body: 15–17sp
- Caption: 13–15sp
- Tiny label: 11–13sp

Primary long-form text: Deep Navy on Cream/pale-blue.

## Pixel UI language
- 4dp base layout grid
- stepped/pixel corners
- offset pixel shadows, not large blurry elevation
- no pill buttons everywhere
- no glassmorphism
- no generic gradient CTAs
- no default Material-looking controls in final visible UI

Native controls are allowed underneath, but final visible treatment must match the world.

## Responsive baseline
Design reference: 360×800dp portrait.
Must remain usable roughly 320–430dp width.
Background can be edge-to-edge; important interaction stays inside safe insets.

Normal content horizontal padding: 20–24dp.

## World composition
Render order:
1. world background
2. clouds/large ambience
3. anchored environment (Café)
4. interactive landmarks
5. Fish/living object
6. ambient effects
7. UI overlay

Object positions use normalized coordinates and responsive scale. No generic card behind World landmarks.

Discoverability comes from restrained movement + contrast + occasional tiny sparkle, not labels or giant glow.

Minimum touch target: 48dp, preferably 56–64dp for small landmarks.

## Buttons
Primary pixel button:
- Cream/light surface
- blue stepped outline
- tiny pink/lavender accent
- height about 48–56dp
- no smooth pill radius

Wish hold button has visible pixel-block progress.

## Panels
Reusable pixel window:
- cream surface
- blue/purple stepped border
- 2–4dp pixel-offset shadow
- at most 1–2 decorative accents per panel

Do not place bow + flower + shell + star on every panel.

## Gallery
Photo is the focal point.
- 1:1 image
- photo frame overlay
- approximate displayed square 280–330dp on typical phone
- simple pale background
- minimal extra decoration

Author photo uses a simpler frame, not the Alia Archive ornate frame.

## Wish
Highest visual priority.
Use:
- world_dusk
- Wish Star hero
- restrained star/particle field
- clear input panel
- no confetti storm
- moon reserved for post-finale

## Asset use
- `world_day`: first/main day world
- `world_dusk`: Wish + post-finale world
- `island`: auxiliary only
- `cafe`: World landmark + Café hero
- `cloud_01`, `cloud_02`: atmosphere
- `camera`: Archive
- `bottle`: Messages
- `shell`: bonus
- `fish`: Easter Egg
- `star`: Collectible Stars
- `wish_star`: Wish hero only
- `sparkle_01`: main themed particle
- `sparkle_02`: rare cluster
- `flower`: rare accent/post-finale
- `bow`: rare accent
- `tiny_shell`: reusable subtle decoration
- `wish_capsule`: sealing/send
- `author_note`: About Author landmark
- `photo_frame`: Archive overlay
- `wave_strip`: Boot/world transition
- `loading_star`: Boot
- `teacup`, `coffee`: Café
- `moon`: post-finale reward

## New artwork rule
Do not create new major art if existing supplied art can fulfill the role.
Allowed generated/native additions:
- VectorDrawable back/sound controls
- pixel borders
- Canvas dots/crosses/stars
- adaptive launcher icon assembled from existing supplied art

## Launcher icon
Do not generate a new illustration.
Use a clean ocean-blue background and a cropped `loading_star` or simplified supplied star as adaptive foreground.
