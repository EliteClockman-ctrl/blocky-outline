## What's new in version 1.1.4 :

### Version & Target Migration
- **Targeted Minecraft 1.21.11**: Fully transitioned and ported from Minecraft 26.x experiments to official Minecraft `1.21.11` (Fabric).
- **Native Render Events**: Replaced experimental level rendering pipeline hooks with stable `WorldRenderEvents.BEFORE_BLOCK_OUTLINE`.
- **Clean Standard Architecture**: Removed dirty package injection workarounds; now using clean, standard Minecraft 1.21.11 `RenderTypes` and `GuiGraphics` APIs.

### Performance & Zero-Lag Enhancements
- **Zero-Allocation Rendering**: Eliminated temporary vector, object, and matrix allocations in the render hot loop.
- **Micro-Optimized Math**: Pre-computed trigonometric pulses and cached frame calculations prevent FPS drops and micro-stutters.
- **Smooth Glide & Transition**: Fine-tuned block glide interpolation for instantaneous, snappy cursor tracking.

### Code Quality & Readability
- **Human-Authored Style**: Completely rewritten and refactored code structure and comments to match clean, idiomatic open-source Minecraft mod standards.
- **Robust UI Rendering**: Updated all menu text rendering to modern `GuiGraphics.drawString` and `drawCenteredString` for sharp, reliable font display across all GUI scales.

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
