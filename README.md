# Blocky Outline 1.1.7 (Argon)

A modern, high-FPS client-side Fabric mod for **Minecraft 1.21.11** that transforms vanilla block selection into a crisp, responsive 3D visual system — featuring Fresnel glass fills, dual-pass break shockwaves, tactile block-place ripples, 5 physics motion curves, 3D particles, and a live-preview configuration studio.

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.11-62ba3d?style=flat-square)](https://minecraft.net)
[![Fabric](https://img.shields.io/badge/Loader-Fabric-b87333?style=flat-square)](https://fabricmc.net)
[![Version](https://img.shields.io/badge/Version-1.1.7--argon-38bdf8?style=flat-square)](https://github.com/EliteClockman-ctrl/blocky-outline/releases)
[![License](https://img.shields.io/badge/License-MIT-818cf8?style=flat-square)](LICENSE_blocky-outline)

---

## Editions

- **Argon** *(this build)* — Full visual edition with the 7-tab customization menu, live 3D preview, Motion Engine V3, 3D particles, break shockwaves, block place ripples, AI styling, and `BKO#` preset sharing.
- **Hydro** — Ultra-lightweight core edition focused purely on minimal overhead.

---

## Key Features

### Precision Outline & Architectural Glass Fill
- **Outline Styles:** Classic, Dual Wave, and Spectrum modes with smooth Oklab color blending, adjustable line width, opacity, corner brackets, and optional color animation.
- **Fresnel Glass Face Fill:** Translucent 3D fill with Classic, Gradient, Gummy, and Prism modes, subtle top-face specular sheen, and an aimed-face rim highlight when Edge Smoothing is enabled.
- **Smart Depth & Face Filtering:** Switch between `Standard`, `Smart Depth` (subtle occluded ghost edges), and `X-Ray`, plus `All Faces`, `Top & Bottom`, or `Hit Face` visibility.
- **Connected Blocks & Offset Alignment:** Seamlessly outlines double chests, doors, and beds as unified shapes, and aligns accurately with offset blocks (bamboo, flowers, tall grass, pointed dripstone, slabs, stairs).
- **Placement Grid:** Optional center crosshair and edge midpoint alignment ticks on the targeted face while building.

### Break Shockwave & Block Place Ripple
- **Hyper-Nova Break Shockwave (Dual-Pass 3D):** Choose between **Hyper-Nova**, **Minimal Ring**, **Shatter Frame**, or **Off** — combining vector glow lines and additive volumetric quads (implosion core, dual-stage corner brackets, rotating 3D tilted halo ring, anamorphic impact flare, holographic shatter flash, floor pulse ribbon, and 14 glowing spark shards).
- **Block Place Ripple:** Tactile 3D feedback when placing blocks — snap-in corner brackets, a soft seating flash, and a clean expanding base ring triggered strictly once placement is confirmed.
- **3D Mining Progress & Minimalist HUD:** Four in-world mining styles (`Ring`, `Frame`, `Liquid`, `Burst`), crosshair/bar progress indicators, and a compact single-row Block Info HUD pill showing only the targeted block icon and name.

### Motion Engine V3
- **5 Physics Curves:** **Glide**, **Spring**, **Pop**, **Morph**, and **Raw 0ms**, with adjustable **Motion Speed** and **Overshoot**.
- **3D Motion Trail & Focus Fade:** Fading 3D ghost afterimages when sweeping across blocks, smooth fade-in on aim, ghost fade-out on look-away, and **Shape Morphing** across different hitboxes (blocks, slabs, stairs, chests).
- **Selection Sound:** Optional subtle audio feedback (`Soft Pop`, `Tick`, `Chime`) with volume control.

### Modern 3D Particle Engine
- Five dual-pass 3D styles (`GLOW_LINES` + `GLOW_QUADS`) with customizable color, density, speed, size, and spread:
  - **Starlight** — Anamorphic lens-flare stars with dynamic constellation micro-links.
  - **Sparks** — 360° curved kinetic plasma arcs with white-hot star heads.
  - **Soul Fire** — Double-helix plasma ribbons with breathing wisp cores.
  - **Sakura** — Sculpted 3D tumbling petals with delicate wind-current trails and pollen sparkles.
  - **Orbital** — Dual counter-rotating gyroscope rings with telemetry ticks and 4-point energy comets.

### Live-Preview Studio & `BKO#` Preset Sharing
- **7-Tab Responsive Menu (`M`):** `Outline`, `Fill`, `Particles`, `Mining`, `Motion`, `Presets`, and `Interface` with an interactive 3D live preview, custom HSV/HEX color picker, 3 custom save/delete slots, one-click gameplay profiles (`Competitive`, `Smooth`, `Minimal`, `Performance`), and AI Styling presets.
- **1-Click `BKO#` Share Codes:** Export your exact setup to clipboard or import codes from friends with instant validation and preview.

### Zero-GC Performance Architecture
- Precomputed sRGB/Linear & Oklab LUTs, reusable static render buffers, ring tangent LUTs, tick-rate cached mining speed, skipped empty GPU buffer passes, and run-length batched 3D menu rendering keep FPS high and stutter-free.

---

## Controls & Commands

| Action | Binding / Command |
|---|---|
| Open customization studio | **`M`** *(rebindable in Controls)* |
| Open issue tracker | `/report` |

---

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/installer/) `>= 0.15.0` for **Minecraft 1.21.11**.
2. Install [Fabric API](https://modrinth.com/mod/fabric-api).
3. Place `blocky-outline-1.1.7-argon.jar` into your `.minecraft/mods/` folder.
4. Launch Minecraft and press **`M`** in-game to customize.

> Configuration is saved automatically to `.minecraft/config/blocky-outline.json`.

---

## Requirements

| Dependency | Version |
|---|---|
| Minecraft | `1.21.11` |
| Fabric Loader | `>= 0.15.0` |
| Fabric API | Required (`1.21.11`) |
| Java | `>= 21` |

---

## Building from Source

```bash
./gradlew test build
```

On Windows PowerShell:

```powershell
.\gradlew.bat test build
```

The packaged mod JAR is written to `dist/blocky-outline-1.1.7-argon.jar`.

---

## License

Licensed under the MIT License — see [LICENSE_blocky-outline](LICENSE_blocky-outline) for details.
