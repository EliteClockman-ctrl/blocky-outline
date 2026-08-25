# Blocky Outline v1.1.1 — Official Source Code

A lightweight, modern client-side mod that customizes the block selection outline and fill in Minecraft (compatible with Fabric 1.21, 1.21.1 - 1.21.11, and 26.2).

## Features

- **Custom Outline & Fill**: Customize block outline color, opacity, and width, with optional transparent block face fill.
- **Two-Tone Gradient Fill**: Top-Bottom vertical 2-tone color blend with independent Color 1 and Color 2 pickers.
- **Dynamic HSV & Rainbow RGB**: Static HSV color tuning (via Hue bar, Saturation/Value box, or Hex input) or independent animated Rainbow RGB modes.
- **Universal Multi-Version Compatibility**: Automatically adapts and runs seamlessly across Minecraft 1.21 - 26.2.
- **Ultra-Responsive Smooth Movement**: High-performance exponential decay LERP interpolation algorithm with instant auto-snapping (fixed at 60% optimal speed).
- **Zero-GC Executive Engine**: Bitwise color conversion and GPU-direct rendering eliminating per-frame memory allocation for stutter-free FPS.
- **In-Game Executive UI**: Press **M** anywhere in-game to toggle the configuration screen. Includes 5 built-in presets (Minimalist Silver, Executive Purple, Vibrant Gold, Rainbow Corporate, Dark Slate).
- **Auto Configuration Persistence**: Automatically saves settings to `.minecraft/config/blocky-outline.json`.

## Requirements

- Minecraft `1.21` - `26.2` (Fabric)
- Fabric Loader `>=0.15.0`
- Fabric API

## Building

To build the mod JAR file, run:
```bash
./gradlew build
```
The output JAR file will be generated in `build/libs/blocky-outline-1.1.1.jar`.

## License

This project is licensed under the MIT License — see the [LICENSE](LICENSE_blocky-outline) file for details.

