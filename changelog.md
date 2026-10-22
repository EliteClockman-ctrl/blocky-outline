## What's new in version 1.1.4 (Compared to 1.1.3):

### 🔄 Ported to Minecraft 1.21.11
- **Minecraft 1.21.11 Support**: Fully ported from 26.x down to official Minecraft `1.21.11` (Fabric).
- **Native Render Pipeline**: Integrated with Minecraft 1.21.11's native render pipeline and Fabric's `WorldRenderEvents.BEFORE_BLOCK_OUTLINE`.

### ⚡ Performance Optimizations (Zero-Lag)
- **Zero-Allocation Rendering**: Eliminated runtime memory allocations in the block outline and fill rendering loops, preventing micro-stutters and GC lag spikes.
- **Frame-Cached Calculations**: Cached breathing waves and rainbow color calculations per frame rather than recalculating on every block face.
- **Snappier Glide**: Refined interpolation math for smooth, instant outline tracking with zero delay.

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
