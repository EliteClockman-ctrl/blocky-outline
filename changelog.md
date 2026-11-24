# What's new in 1.1.8-hydro (Fabric 26.1.x)

Lightweight, high-FPS **Hydro** edition updated for **Minecraft 26.1.x** (`26.1` – `26.1.2`, Fabric + Java 25).

### Updates
- Upgraded **Blocky Outline (Hydro)** to `1.1.8-hydro` targeting **Minecraft 26.1.x** (`26.1.2`) using unobfuscated `net.fabricmc.fabric-loom`, Fabric Loader `0.20.1`, Fabric API `0.155.3+26.1.2`, and Java 25.
- Migrated outline rendering to Fabric's `LevelRenderEvents.BEFORE_BLOCK_OUTLINE` (`context.poseStack()`, `context.bufferSource()`, and direct `Camera.position()`), removing reflection overhead from the hot render path.
- Updated Fabric API & Minecraft 26.1.x calls: `ClientCommands`, `KeyMappingHelper.registerKeyMapping`, `GuiGraphicsExtractor` / `extractRenderState`, `renderer.state.level.BlockOutlineRenderState`, and `RenderPipeline` (`ColorTargetState` / `DepthStencilState`).
- Updated menu header version label to `v1.1.8-hydro` and required `fabric-api` in `fabric.mod.json` `depends`.

### Fixes & Packaging
- Fixed double XZ offset misalignment on offset blocks (bamboo, flowers, tall grass, pointed dripstone) by removing duplicate `state.getOffset(pos)` addition.
- Added `LevelRenderEvents` fallback in `CompatBridge` and excluded `.gitkeep` from the packaged JAR (`~100KB` standalone build).

---

## What's new in Blocky Outline v1.1.4 (Minecraft 1.21.11):

### 🛠️ Bug Fixes
- **Ported & Stabilized for Minecraft 1.21.11**: Fully updated build target and mappings to Minecraft 1.21.11 while maintaining full backwards and forwards compatibility across 1.21.x and 26.x.
- **Fixed Menu (M) Crash across 1.21.x & 26.x**: Resolved all menu crashes on both legacy Minecraft 1.21 (1.21.0 – 1.21.4) and modern versions (1.21.11+ / 26.x) via the dynamic Dual-Runtime Bridge.
- **Fixed Render Event Registration**: Resolved `IllegalAccessException` when registering `BEFORE_BLOCK_OUTLINE` with Fabric API.
- **Fixed KeyMapping Crash on macOS & 26.x**: Fixed `NoSuchFieldError` (`InputConstants$Type KEYSYM`) by making keybinding registration resilient and fully dynamic.
- **Fixed Menu Layout Overflow**: Fixed elements and cards overlapping the footer on smaller window resolutions or higher GUI Scales (Scale 3, 4, Auto).
- **Fixed Frame Micro-Stutters (Zero-GC)**: Completely eliminated per-frame heap allocations during outline/fill rendering and crosshair tracking.

---

### 🔄 Changes & Improvements
- **Added `/report` Command**: Run `/report` or `/blockyoutline report` in chat to quickly open the official GitHub Issues page in your browser.
- **Added "Report Bug" Button in Menu [M]**: Added a quick teal button next to the Done button in the settings footer for instant feedback submission.
- **Render Fast-Path**: Standard cube blocks (>95% of world blocks) are now drawn directly via a fast-path, skipping complex shape slice evaluations.
- **Optimized Reflection with `MethodHandle`**: Boosted per-frame matrix, camera, and offset queries by 20–50x with zero boxing overhead.
- **Resting CPU Optimization**: Halts spring and physics calculations when the crosshair rests steadily on a target block.
