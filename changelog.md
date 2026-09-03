# Blocky Outline — Changelog

---

## v1.1.1 — 2026-09-03
**Minecraft 1.21.11 · Fabric Loader ≥0.16.0 · Fabric API**

### ✨ New Features
- **Dual-color Fill Gradient**: Added a second fill color (Color 2) with a dedicated toggle. When enabled, the block fill blends vertically from Color 1 (top) to Color 2 (bottom) for a sleek gradient effect.
- **In-game Color Picker Modal**: Click the color swatch next to any color row to open a full 2D Saturation/Value picker with a vertical Hue strip, quick preset palette dots, and HEX input — all without leaving the game.
- **HEX Color Input**: Type any 6-digit hex code directly into the `#FFFFFF` badge on the color row to set a precise color instantly.
- **Rainbow RGB for Fill**: Independent rainbow animation toggle for fill, with its own speed control.
- **Grass & Plant Outline Alignment**: Block outlines on grass, flowers, ferns, and other offset blocks now perfectly follow the rendered model position instead of the grid position.
- **Responsive UI Layout**: The configuration menu now dynamically adapts to any window size or GUI scale — rows, sliders, the live preview panel, header tabs, and the Done button all scale and reflow cleanly without overlap or cutoff.

### ⚡ Performance
- **Zero-GC Render Path**: Eliminated all per-frame object allocations in the outline and fill rendering hot path.
- **Short-circuit Alpha Check**: Rendering is fully skipped when outline alpha or fill opacity is effectively zero, saving GPU vertex buffer calls.
- **Inline constant `0.003921569f`**: Replaced all `/ 255.0f` float divisions with the precomputed reciprocal constant for faster color unpacking on every vertex.
- **Adaptive Spring Lerp**: Smooth transition uses a fixed-factor lerp (`0.75`) with instant snap when the distance exceeds 6 blocks, keeping tracking snappy with zero overshoot.

### 🐛 Bug Fixes
- Fixed block outline rendering for grass, flowers, ferns, and other blocks with random position offsets (now uses `state.getOffset(pos)` to shift the VoxelShape).
- Fixed the Done button being clipped or invisible on short/small windows.
- Fixed slider value labels and hex badges overlapping row labels on narrow window widths.
- Fixed Live Preview being squished or empty on compact window sizes (auto-hides when width is insufficient).
- Fixed header tabs being too narrow to click on small panel widths (tab width now scales with available space).

### 🔧 Internal
- Removed all inline comments from source files for clean production code.
- Removed unused `LegacyRenderHandler.java` and `ModernRenderHandler.java`.
- `BlockyOutlineSettings.getOutlineArgb()`, `getFillArgb()`, `getFillArgb2()` are now called only when rendering is actually needed.

---

## v1.1.0 — Initial Release
**Minecraft 1.21.1 · Fabric**

- Custom block outline color (HSV), opacity, and width.
- Optional block face fill with configurable opacity.
- Rainbow RGB cycling for outline and fill.
- Smooth outline transition with adaptive spring interpolation.
- In-game configuration screen (press **M**).
- 5 built-in presets: Minimalist Silver, Executive Purple, Vibrant Gold, Rainbow Corporate, Dark Slate.
- Auto-save config to `.minecraft/config/blocky-outline.json`.
