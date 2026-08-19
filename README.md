# Blocky Outline v1.1.0 — Official Source Code

A lightweight, modern client-side mod that customizes the block selection outline and fill in Minecraft 26.2. Built for Fabric Loader & Fabric API.

## Features

- **Custom Outline & Fill**: Customize block outline color, opacity, and width, with optional transparent block face fill.
- **Dynamic HSV & Rainbow RGB**: Static HSV color tuning (via Hue bar, Saturation/Value box, or Hex input) or independent animated Rainbow RGB modes.
- **Ultra-Responsive Smooth Movement**: High-performance exponential decay LERP interpolation algorithm with instant auto-snapping when switching between blocks.
- **Zero-GC Executive Engine**: Optimized color conversion engine eliminating per-frame memory allocation for smooth, stutter-free FPS.
- **In-Game Executive UI**: Press **M** anywhere in-game to toggle the configuration screen. Includes 5 built-in presets (Minimalist Silver, Executive Purple, Vibrant Gold, Rainbow Corporate, Dark Slate).
- **Auto Configuration Persistence**: Automatically saves settings to `.minecraft/config/blocky-outline.json`.

## Requirements

- Minecraft `26.2` (1.21.11 / Fabric)
- Fabric Loader `>=0.19.3`
- Fabric API

## Building

To build the mod JAR file, run:
```bash
./gradlew build
```
The output JAR file will be generated in `build/libs/blocky-outline-1.1.0.jar`.

## License

This project is licensed under the MIT License — see the [LICENSE](LICENSE_blocky-outline) file for details.
