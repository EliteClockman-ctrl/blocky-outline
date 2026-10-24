## What's new in version v1.1.6:

- **Minecraft 1.21.11 Dedicated**: Native, dedicated support specifically optimized for Minecraft 1.21.11 running on Fabric Loader 0.17.3+ and Fabric API 0.141.6+1.21.11.
- **X-Ray (See-Through) Mode**: Block outlines and face fills can now render right through walls and opaque blocks. Built using custom Blaze3D render pipelines (`XRAY_LINES` & `XRAY_QUADS`) with disabled depth testing (`NO_DEPTH_TEST`) and no depth write.
- **Zero Appearance Latency (Instant Lock-On)**: Completely eliminated block highlight delay. Crosshair arriving from air (>100ms gap) or large camera jumps (>2.5 blocks) snaps instantly with 0ms delay. Smooth glide transitions only activate when continuously scanning adjacent blocks.
- **Damped Spring Physics (Harmonic Oscillator)**: Replaced static time interpolation with a 4-step physical mass-spring-damper simulation. Inherits cursor velocity continuously across frames so rapid mouse flicks feel natural and fluid without jerky stops.
- **Ease-Out Back (Overshoot Engine)**: Crisp, modern UI movement inspired by macOS/iOS fluid animations. The outline snaps quickly toward the target block, overshoots with a subtle 1-bounce spring, and pulls tightly into place.
- **Fast Quintic (5th Degree Curve)**: Maximum initial burst velocity for instant target lock-on with zero perceived delay, terminating in a soft, gentle deceleration.
- **Fluid Apple Bezier**: Precision cubic-bezier easing curve providing immediate response with cushioned edge landings.
- **Zero-GC & High Performance**: Replaced `shape.toAabbs()` with `shape.forAllBoxes(...)` directly in `OutlineRenderer`, completely eliminating `ArrayList` and `AABB` heap object allocations per frame for maximum FPS stability.
- **Blaze3D LineWidth Fix**: Fixed `IllegalStateException: Missing elements in vertex: LineWidth` crash on 1.21.11 by providing line width attributes on every line vertex.
- **Input System Compatibility**: Fully adapted the configuration menu to Minecraft 1.21.11's `MouseButtonEvent` input API.
- **Settings & Clamping Range Fix**: Expanded configuration clamp boundaries to match all menu sliders (25ms - 250ms motion duration, 0.5x - 3.0x overshoot, 60 - 600k spring tension), preventing fast user presets from being reset on game restart.
- **Clean Launch**: Added client resources to eliminate FabricLoader's missing client classpath warning during startup.

---

## What was in previous versions:

### v1.1.5
- Expanded compatibility to Minecraft 1.21 and 1.21.x releases.
- Optimized render lambdas and added block model offset tracking (flowers, tall grass, ferns).

### v1.1.4
- Initial port to Minecraft 1.21.11 Blaze3D rendering system.
- Codebase standardization and refactoring.

### v1.1.3
- Cross-version runtime compatibility fixes for 26.1 through 26.3.

### v1.1.2
- Color picker dialog precision improvements and HEX input support.

### v1.1.1
- Initial release for Fabric 1.21.11 with custom outline width, colors, breathing animations, and user preset slots.
