## What's new in version 1.1.2:

- **Minecraft 26.1.x Support**: Added full compatibility for Minecraft 26.1.x on Fabric Loader (0.19.5+) running on Java 25.
- **GUI Engine Modernization**: Migrated the in-game settings menu (`M`) to Minecraft 26.1's new `GuiGraphicsExtractor` pipeline and native input event system (`MouseButtonEvent`, `KeyEvent`, `CharacterEvent`).
- **New Rendering Architecture**: Ported block outline hooks to `LevelRenderEvents.BEFORE_BLOCK_OUTLINE`, utilizing direct `BlockOutlineRenderState` for pixel-perfect block alignment.
- **Updated Keymapping**: Updated keybind registrations to the new `KeyMappingHelper` API.
- **Zero-GC Render Optimization**: Completely eliminated per-frame memory allocations in the render hot path, resulting in buttery-smooth FPS.
- **Crash Fixes**: Resolved `NoClassDefFoundError` and runtime mapping incompatibility with official Mojang named environments.
