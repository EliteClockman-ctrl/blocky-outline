# Blocky Outline

A lightweight, modern client-side Fabric mod that replaces Minecraft's default block selection outline with a fully customizable outline and optional gradient fill.

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.11-62ba3d?style=flat-square)](https://minecraft.net)
[![Fabric](https://img.shields.io/badge/Fabric-0.16+-b87333?style=flat-square)](https://fabricmc.net)
[![License](https://img.shields.io/badge/License-MIT-blue?style=flat-square)](LICENSE_blocky-outline)
[![Version](https://img.shields.io/badge/Version-1.1.6-9333ea?style=flat-square)](https://github.com/EliteClockman-ctrl/blocky-outline/releases/tag/v1.1.6)

---

## Features

- **Custom Outline**: Adjust color (HSV or HEX), opacity, and width of the block selection outline.
- **Block Fill with Dual-Color Gradient**: Enable an optional transparent fill on block faces. Set two colors (top/bottom) for a smooth vertical gradient blend.
- **In-game Color Picker**: Click the color swatch to open a full 2D Saturation/Value picker with a Hue strip and HEX input — no external tools needed.
- **Rainbow RGB Mode**: Animated rainbow cycling for both outline and fill, each with independent speed control.
- **Advanced Motion Engines**:
  - **Spring Physics (Damped Harmonic Oscillator)**: Physical spring with continuous velocity tracking across mouse flicks — zero jitter or abrupt stops.
  - **Ease-Out Back (Overshoot)**: Snappy modern macOS/iOS style 1-bounce overshoot animation with adjustable bounce push.
  - **Circ & Quint Easing**: Ultra-fast initial burst speed to lock on targets with zero visual delay and soft-cushion landing.
  - **Custom Fluid Bezier**: Apple Fluid & Material Design easing curves for responsive, fluid movement.
  - **Classic Lerp**: Traditional smooth exponential glide.
- **X-Ray / Through Walls Mode**: See the outline and fill through opaque obstacles.
- **Corner Accents Mode**: Futuristic corner brackets with adjustable edge size.
- **Grass & Plant Alignment**: Outlines correctly follow the rendered position of grass, flowers, ferns, and all other offset blocks.
- **Responsive UI**: The configuration menu adapts to any window size or GUI scale — no overlap, no cutoff.
- **Profile Manager**: 3 customizable user preset slots with Load, Save, and Delete support.
- **Zero-GC Render Engine**: No per-frame allocations in the render hot path — zero FPS drops or micro-stutters.
- **Auto-Save Config**: Settings persist to `.minecraft/config/blocky-outline.json` automatically.

---

## Controls

| Action | Keybind |
|---|---|
| Open configuration menu | **M** |

---

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/installer/) `≥0.17.3`
2. Install [Fabric API](https://modrinth.com/mod/fabric-api) for Minecraft `1.21.11`
3. Drop `blocky-outline-1.1.6.jar` into your `.minecraft/mods/` folder
4. Launch Minecraft `1.21.11`

---

## Requirements

| Dependency | Version |
|---|---|
| Minecraft | `1.21.11` |
| Fabric Loader | `≥0.17.3` |
| Fabric API | Compatible with 1.21.x |
| Java | `≥21` |

---

## Building from Source

```bash
./gradlew build
```

Built jars will be located in `build/libs/` and `dist/`.

---

## License

This project is licensed under the [MIT License](LICENSE_blocky-outline).
