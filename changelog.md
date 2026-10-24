# Changelog

All notable changes to the **Blocky Outline** mod will be documented in this file.

---

## [v1.1.6] - 2026-10-24

### ✨ New Features & Gameplay
- **Minecraft 1.21.11 Dedicated**: Native, dedicated support specifically optimized for Minecraft `1.21.11` running on Fabric Loader `0.17.3+` and Fabric API `0.141.6+1.21.11`.
- **X-Ray / See-Through Mode**:
  - Implemented custom Blaze3D render pipelines (`XRAY_LINES` & `XRAY_QUADS`) with `DepthTestFunction.NO_DEPTH_TEST` and disabled depth write (`withDepthWrite(false)`).
  - When **X-Ray Mode** is enabled in the menu, both the block outline and face fill render right through opaque walls and obstacles.
- **Overhauled Physics & Motion Engine**:
  - **Damped Spring Physics (Harmonic Oscillator)**: Replaced static interpolation with a 4 sub-step physical mass-spring-damper simulation. Preserves cursor momentum during fast flick shots for an ultra-fluid, natural feel.
  - **Ease-Out Back (Overshoot Engine)**: Crisp, modern UI movement inspired by macOS/iOS fluid animations. The outline snaps quickly toward the target block, overshoots with a subtle 1-bounce spring, and pulls tightly into place.
  - **Fast Quintic (Instant Lock-On)**: 5th-degree polynomial curve delivering maximum initial burst speed to lock onto targets with zero visual delay and soft-cushion landing.
  - **Fluid Apple Bezier**: Precision cubic-bezier easing with rapid initial acceleration and soft deceleration.
  - **Smooth Lerp (Exponential Decay)**: High-framerate, framerate-independent smooth tracking.
  - **Instant (Raw)**: Direct 0ms lock-on for competitive players who prefer raw responsiveness.
- **Zero Appearance Latency**:
  - Crosshair acquiring a block from air (>100ms air gap) or upon large camera jumps (>2.5 blocks) snaps instantly with 0ms visual delay. Smooth interpolation activates only when continuously gliding across neighboring blocks.

### ⚡ Performance & Zero-GC
- **Zero-Allocation Bounding Box Traversal**: Replaced `shape.toAabbs()` with `shape.forAllBoxes(...)` directly in `OutlineRenderer`. This completely eliminates `ArrayList` and `AABB` heap allocations on the render loop, keeping the render path 100% Zero-GC at 144+ FPS.
- **Blaze3D Render Pipelines**: Static singleton pipeline initialization ensures zero shader re-compilation during gameplay.
- **HSV to RGB Color Caching**: Color calculations and breathing sine waves are cached and evaluated only when settings change or per-frame intervals occur.

### 🐛 Bug Fixes & Stability
- **Missing Elements in Vertex (LineWidth)**: Resolved `IllegalStateException: Missing elements in vertex: LineWidth` on Minecraft 1.21.11 by providing `.setLineWidth(width)` on every line vertex.
- **Settings Clamping Range Fix**: Fixed `copyFrom` clamping boundaries in `BlockyOutlineSettings` so ultra-fast animations (down to 25ms duration), subtle overshoot values (down to 0.5x), and spring stiffness values (up to 600k) persist reliably across restarts without being reset.
- **Input System Compatibility**: Fully adapted `BlockyOutlineMenuScreen` mouse events (`mouseClicked`, `mouseDragged`, `mouseReleased`) to Minecraft 1.21.11's `MouseButtonEvent`.
- **Missing Classpath Warning**: Added `src/client/resources/.gitkeep` to prevent FabricLoader from logging missing client resources warnings during game launch.
- **Camera Null-Safety**: Added safety guards in `BEFORE_BLOCK_OUTLINE` to prevent rare NPEs when disconnecting or transitioning screens.

---

## [v1.1.5] - 2026-10-20

### ⚡ Performance & Quality of Life
- **Zero-GC Lambda Optimizations**: Removed transient lambda allocations across render call sites.
- **Block Model Offset Support**: Outlines and face fills now accurately follow model-offset blocks such as flowers, grass, ferns, and bamboo.

---

## [v1.1.4] - 2026-10-18

### 🔄 Porting & Compatibility
- **Minecraft 1.21.11 Port**: Ported rendering systems to Mojang's 1.21.11 Blaze3D pipeline.
- **Codebase Standardization**: Refactored renderer and configuration modules for enhanced maintainability.

---

## [v1.1.3] - 2026-10-15

### 🐛 Bug Fixes
- **Cross-Version Runtime Compatibility**: Fixed class loading and runtime discrepancies across 1.21 minor revisions.
- **Jar Packaging**: Refreshed binary distribution artifacts.

---

## [v1.1.2] - 2026-10-12

### 🎨 UI & Controls
- **Color Picker Refinement**: Improved precision of 2D Saturation/Value picker and Hue slider in the in-game configuration menu.
- **HEX Input Support**: Added keyboard support for direct hexadecimal color entry.

---

## [v1.1.1] - 2026-10-09

### ✨ Initial Release for Minecraft 1.21.11
- **Customizable Outline**: Color (HSV/HEX), opacity, and outline thickness.
- **Dual-Color Gradient Fill**: Top and bottom color blending on block faces.
- **Rainbow RGB Mode**: Smooth cycling rainbow mode for outline and fill.
- **Corner Accents Mode**: Sci-fi style corner brackets with adjustable edge size.
- **In-Game Configuration Menu**: Responsive GUI opened via the **M** key.
- **User Preset Profiles**: 3 user-customizable save slots with Load, Save, and Delete options.
