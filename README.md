# Blocky Outline

A lightweight, modern client-side Fabric mod that replaces Minecraft's default block selection outline with a fully customizable outline and optional gradient fill.

[![Minecraft](https://img.shields.io/badge/Minecraft-26.1.x-62ba3d?style=flat-square)](https://minecraft.net)
[![Fabric](https://img.shields.io/badge/Fabric-0.19.3-b87333?style=flat-square)](https://fabricmc.net)
[![License](https://img.shields.io/badge/License-MIT-blue?style=flat-square)](LICENSE_blocky-outline)
[![Version](https://img.shields.io/badge/Version-1.1.2-9333ea?style=flat-square)](https://github.com/EliteClockman-ctrl/blocky-outline/releases/tag/v1.1.2)

---

## Features

- **Custom Outline**: Adjust color (HSV or HEX), opacity, and width of the block selection outline.
- **Block Fill with Dual-Color Gradient**: Enable an optional transparent fill on block faces. Set two colors (top/bottom) for a smooth vertical gradient blend.
- **In-game Color Picker**: Click the color swatch to open a full 2D Saturation/Value picker with a Hue strip and HEX input — no external tools needed.
- **Rainbow RGB Mode**: Animated rainbow cycling for both outline and fill, each with independent speed control.
- **Smooth Movement**: High-performance adaptive spring LERP tracks the cursor block with zero delay and instant snap when switching blocks far apart.
- **Grass & Plant Alignment**: Outlines correctly follow the rendered position of grass, flowers, ferns, and all other offset blocks.
- **Responsive UI**: The configuration menu adapts to any window size or GUI scale — no overlap, no cutoff.
- **5 Built-in Presets**: Minimalist Silver, Executive Purple, Vibrant Gold, Rainbow Corporate, Dark Slate.
- **Zero-GC Render Engine**: No per-frame allocations in the render hot path — smooth FPS regardless of framerate.
- **Auto-Save Config**: Settings persist to `.minecraft/config/blocky-outline.json` automatically.

---

## Controls

| Action | Keybind |
|---|---|
| Open configuration menu | **M** |

---

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/installer/) `≥0.16.0`
2. Install [Fabric API](https://modrinth.com/mod/fabric-api)
3. Drop `blocky-outline-v1.1.2.jar` into your `.minecraft/mods/` folder
4. Launch Minecraft `26.1.x`

---

## Requirements

| Dependency | Version |
|---|---|
| Minecraft | `1.21.11` |
| Fabric Loader | `≥0.16.0` |
| Fabric API | any |
| Java | `≥21` |

---

## Building from Source

```bash
./gradlew build
```

Output JAR: `dist/blocky-outline-v1.1.1.jar`

---

## License

This project is licensed under the MIT License — see the [LICENSE](LICENSE_blocky-outline) file for details.
