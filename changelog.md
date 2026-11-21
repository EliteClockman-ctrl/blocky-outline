## What's new in 1.1.7-argon (Fabric 1.21.11)

A version for modern graphics. Major visual, motion, particle, and performance overhaul for **Blocky Outline (Argon)** on Minecraft 1.21.11.

### New Features & Visual Overhauls
- **Hyper-Nova Break Shockwave & Style Selector:** Rebuilt block-break shockwaves with dual-pass 3D rendering (`GLOW_LINES` + additive `GLOW_QUADS`) and 3 selectable styles (**Hyper-Nova**, **Minimal Ring**, **Shatter Frame**) — featuring a quantum implosion core, dual-stage expanding corner brackets, a rotating 3D tilted halo ring, an anamorphic impact flare, a holographic face flash, a translucent expanding floor pulse ribbon, and 14 glowing spark shards.
- **Block Place Ripple:** Added tactile block-placement feedback with snap-in 3D corner brackets, a subtle translucent seating flash, and a clean expanding base ring triggered strictly when a block is confirmed placed.
- **Motion Engine V3:** Upgraded block-to-block transitions with 5 distinct physics curves (**Glide**, **Spring**, **Pop**, **Morph**, **Raw 0ms**), tunable **Overshoot**, **3D Motion Trail** ghost afterimages, **Shape Morphing**, and **Focus Fade** in/out, backed by a live animated 3D preview inside the Motion tab.
- **Modern 3D Particle Engine:** Rebuilt all 5 particle styles with dual-pass rendering and matching live menu previews:
  - **Starlight:** Anamorphic lens-flare stars with dynamic gossamer constellation links.
  - **Sparks:** 360° golden-angle curved plasma arcs with white-hot star heads.
  - **Soul Fire:** 3-segment double-helix plasma ribbons with breathing wisp cores.
  - **Sakura:** Sculpted 3D tumbling petals with delicate wind-current ribbons and pollen sparkles.
  - **Orbital:** Dual tilted gyroscope rings with rotating calibration ticks and 4-point energy comets.
- **Architectural Glass & Aimed-Face Highlight:** Added subtle top-face specular sheen on Fresnel glass fills and a soft rim highlight on the targeted block face when Edge Smoothing is enabled.
- **Minimalist Block Info HUD:** Streamlined into a compact single-row glass pill displaying only the targeted block icon and name alongside the crosshair/bar mining progress indicator.
- **Preset Management & Polished UI:** Added preset slot deletion, refined AI Styling harmony/contrast controls, and standardized terminology across all 7 tabs (`Outline`, `Fill`, `Particles`, `Mining`, `Motion`, `Presets`, `Interface`).

### Performance & Smoothness
- **Color & Oklab Fast Paths:** Added precomputed sRGB/Linear LUTs, fast cube-root approximation, per-frame color caching, static Classic fill fast paths, and lazy side-face shading.
- **Zero-GC World & Menu Rendering:** Eliminated per-frame heap allocations across shockwaves, place ripples, motion trails, particles, and 3D menu previews using reusable static buffers and precomputed ring tangent LUTs.
- **Smarter GPU Batching:** Skipped empty `GLOW_LINES` buffer switches when glow/particles/mining are inactive, cached block destroy-rate lookups at tick cadence (50ms), and added run-length horizontal batching + frame-cached fill LUTs to the 3D menu preview.
- **Frame-Rate Independent Menu Motion:** Decoupled 3D preview rotation from FPS (`animDt`) and reduced window drop-shadow overdraw.
