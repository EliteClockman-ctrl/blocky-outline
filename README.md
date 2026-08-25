# Blocky Outline v1.1.2 — Official Source Code

A lightweight, modern client-side mod that customizes the block selection outline and fill in Minecraft (compatible with Fabric 26.1.x).

## Features

- **Custom Outline & Fill**: Choose your favorite RGB/HEX colors or toggle dynamic RGB Rainbow mode.
- **Dedicated Sliders**: Intuitive sliders for adjusting RGB cycling speed, opacity, and line thickness in real time.
- **Smooth Transition**: Ultra-responsive LERP camera-tracking animation as your crosshair moves between blocks.
- **Zero-GC Executive Engine**: Completely allocation-free rendering loop maintaining peak FPS and minimal memory footprint.
- **Interactive UI**: Futuristic dark glassmorphic configuration menu with live 3D preview and built-in preset manager.

## Controls

Press **`M`** anywhere in-game to open the configuration menu.

## Build Instructions

```bash
./gradlew build
```

The output JAR file will be generated in `build/libs/blocky-outline-1.1.2.jar`.

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

