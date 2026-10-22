## What's new in version 1.1.5 (Compared to 1.1.4):

- **Expanded Minecraft 1.21 Compatibility**: Added full native compatibility for Minecraft `1.21` and all `1.21.x` releases (`1.21`, `1.21.1`, `1.21.2`, `1.21.3`, `1.21.4`).
- **Flexible Dependency Rules**: Updated Fabric mod metadata predicates to load seamlessly across any 1.21 Fabric loader environment without version mismatch blocks.

---

## What's new in version 1.1.4 (Compared to 1.1.3):

- **Minecraft 1.21 Support**: Ported from 26.x down to official Minecraft `1.21` (Fabric).
- **Native Render Pipeline**: Integrated with native render pipeline and Fabric's `WorldRenderEvents.BEFORE_BLOCK_OUTLINE`.
- **Zero-Allocation Rendering**: Eliminated runtime memory allocations in the block outline and fill rendering loops, preventing micro-stutters and GC lag spikes.
- **Frame-Cached Calculations**: Cached breathing waves and rainbow color calculations per frame rather than recalculating on every block face.
- **Snappier Glide**: Refined interpolation math for smooth, instant outline tracking with zero delay.
