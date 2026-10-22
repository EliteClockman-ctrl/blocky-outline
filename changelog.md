## What's new in version 1.1.5:

- **Minecraft 1.21 Compatibility**: Added full native compatibility for Minecraft `1.21 - 1.21.4`.
- **Flexible Dependency Rules**: Updated Fabric mod metadata predicates to load seamlessly across any 1.21 Fabric loader environment without version mismatch blocks.
- **Deep Zero-Allocation Rendering**: Replaced lambda iteration with indexed loops over cached bounding boxes (`toAabbs()`), eliminating runtime heap allocations during per-frame outline and fill drawing.
- **Pixel-Perfect Plant Alignment**: Added block model offset tracking (`state.getOffset(...)`) for accurate, jitter-free outlines on grass, flowers, and bamboo.
- **Micro-Optimized Frame Timing**: Eliminated duplicate system time polling and pre-cached color calculations for ultra-smooth framerates.
