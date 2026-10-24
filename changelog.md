## What's new in Blocky Outline v1.1.3-new:

### 🛠️ Bug Fixes
- **Fixed Menu (M) Crash across 1.21.x & 26.x**: Resolved all menu crashes on both legacy Minecraft 1.21 (1.21.0 – 1.21.4) and modern versions (1.21.11+ / 26.x) via the dynamic Dual-Runtime Bridge.
- **Fixed Render Event Registration**: Resolved `IllegalAccessException` when registering `BEFORE_BLOCK_OUTLINE` with Fabric API.
- **Fixed Minecraft Version Detection for 26.1.x – 26.3**: Added proper version range declarations in `fabric.mod.json`, enabling seamless loading across all Fabric 1.21.x and 26.x releases.
- **Fixed Incorrect Menu Version Tag**: Corrected the version indicator displayed in the menu header from `v1.1.7` to `v1.1.3-new`.
- **Fixed Menu Layout Overflow**: Fixed elements and cards overlapping the footer on smaller window resolutions or higher GUI Scales (Scale 3, 4, Auto).
- **Fixed Frame Micro-Stutters (Zero-GC)**: Completely eliminated per-frame heap allocations during outline/fill rendering and crosshair tracking.

---

### 🔄 Changes & Improvements
- **Added `/report` Command**: Run `/report` or `/blockyoutline report` in chat to quickly open the official GitHub Issues page in your browser.
- **Added "Report Bug" Button in Menu [M]**: Added a quick teal button next to the Done button in the settings footer for instant feedback submission.
- **Render Fast-Path**: Standard cube blocks (>95% of world blocks) are now drawn directly via a fast-path, skipping complex shape slice evaluations.
- **Optimized Reflection with `MethodHandle`**: Boosted per-frame matrix, camera, and offset queries by 20–50x with zero boxing overhead.
- **Resting CPU Optimization**: Halts spring and physics calculations when the crosshair rests steadily on a target block.
