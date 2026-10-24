## What's new in version v1.1.7:

- **Full 1.21.x Cross-Version Runtime Bridge**: Implemented a dynamic compatibility layer bridging Minecraft 1.21.0 - 1.21.4 (legacy hit result, VertexConsumer, and standard mouse events) and 1.21.11+ (modern render states, pipeline-based X-Ray, and MouseButtonEvent), enabling a single JAR to run natively across all 1.21.x releases without crashes.
- **X-Ray (See-Through) Mode**: Block outlines and face fills can now render right through walls and opaque blocks. Built using custom Blaze3D render pipelines with disabled depth testing (`NO_DEPTH_TEST`).
- **Zero Appearance Latency (Instant Lock-On)**: Completely eliminated block highlight delay. Crosshair arriving from air (>100ms gap) or large camera jumps (>2.5 blocks) snaps instantly with 0ms delay. Smooth glide transitions only activate when continuously scanning adjacent blocks.
- **Damped Spring Physics (Harmonic Oscillator)**: Replaced static time interpolation with a 4-step physical mass-spring-damper simulation. Inherits cursor velocity continuously across frames so rapid mouse flicks feel natural and fluid without jerky stops.
- **Ease-Out Back (Overshoot Engine)**: Crisp, modern UI movement inspired by macOS/iOS fluid animations. The outline snaps quickly toward the target block, overshoots with a subtle 1-bounce spring, and pulls tightly into place.
- **Fast Quintic (5th Degree Curve)**: Maximum initial burst velocity for instant target lock-on with zero perceived delay, terminating in a soft, gentle deceleration.
- **Fluid Apple Bezier**: Precision cubic-bezier easing curve providing immediate response with cushioned edge landings.
- **Zero-GC & High Performance**: Replaced bounding box list iteration with direct primitive box traversal in `OutlineRenderer`, completely eliminating heap allocations per frame for maximum FPS stability.
