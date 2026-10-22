## What's new in version 1.1.4 (Compared to 1.1.3):

### 🔄 Minecraft Version & Platform
- **Target Updated to Minecraft 1.21.11**: Replaced 26.x (26.1–26.3) support with official Minecraft `1.21.11` (Fabric).
- **Native 1.21.11 Render Pipeline**: Switched to Fabric's standard `WorldRenderEvents.BEFORE_BLOCK_OUTLINE` and native Minecraft `RenderTypes`.
- **Removed Obsolete 26.x Injection**: Removed experimental package-injection files (`BlockyOutlinePipelines` & `BlockyOutlineRenderTypes`).

### ⚡ Performance & Zero-Lag
- **Zero-GC Allocation in Render Loop**: Completely removed temporary object allocations (`Vector3f`, `Matrix4f`, wrappers) during per-frame rendering to eliminate micro-stutters.
- **Cached Frame Calculations**: Pulsing breathing waves and rainbow RGB calculations are now calculated once per frame instead of recomputed per block face.
- **Improved Glide Responsiveness**: Fine-tuned block glide interpolation for instant, jitter-free cursor tracking.

### 🎨 UI & Code Quality
- **1.21.11 Menu Rendering**: Updated all menu text elements to use `GuiGraphics.drawString` and `GuiGraphics.drawCenteredString`.
- **Clean Human-Written Codebase**: Cleaned up code structure and removed all robotic/AI-style comments into idiomatic, experienced mod developer conventions.

---

## What's new in version 1.1.3 :

### New Features
- **Snappy Smooth Movement**: Rebuilt block glide interpolation inspired by *Custom Block Highlight*. Transition time is reduced to 60–80ms for an ultra-responsive, fluid feel.
- **Instant Target Acquisition (0ms Delay)**: Fixed the delay when looking at blocks from air or after looking away; the outline now snaps instantly upon targeting.
- **X-Ray / Through Walls Mode**: Added option to render the outline and face fill visible through solid blocks and walls.
- **Corner Accents Mode**: Added futuristic corner brackets with adjustable edge size.
- **Profile Manager with Delete**: Added 3 user preset slots with **Load**, **Save**, and **Delete** buttons.

### UI & Usability Fixes
- **Responsive Layout Fix**: Resolved element overflow on small window resolutions and high GUI Scales (Auto, Scale 3, Scale 4). Cards now scale dynamically to prevent overlapping the footer.
- **Compact Viewport Support**: Automatically switches to single-line labels on compact window heights.
- **Clamped Header Tabs**: Tab navigation pills no longer collide with the mod title or close button on narrow screens.
- **Short & Natural Descriptions**: Cleaned up all feature descriptions to be concise, clear, and player-friendly.

### Performance Optimizations
- **Zero-GC Render Engine**: Completely eliminated runtime allocations in the block outline and fill rendering pipelines.
- **Fast Axis-Aligned Edges**: Removed runtime square-root operations from edge rendering.
- **Frame-Cached Math**: Breathing pulse wave and rainbow calculations are now cached per frame instead of recomputed per face.
