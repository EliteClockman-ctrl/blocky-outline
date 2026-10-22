## What's new in version v1.1.6:

- **Minecraft 1.21.11 Dedicated**: Native support tailored specifically for Minecraft 1.21.11 Fabric.
- **Damped Spring Physics (Harmonic Oscillator)**: Replaced static time interpolation with a true physical mass-spring-damper simulation. Inherits cursor velocity continuously across frames so rapid mouse flicks never feel jerky or abrupt.
- **Ease-Out Back (Overshoot Engine)**: Crisp, modern UI movement modeled after macOS/iOS fluid animations. The outline snaps quickly toward the target block, overshoots with a subtle 1-bounce spring, and pulls tightly into place.
- **Circ (Circular) & Quint (Quintic) Curves**: Maximum initial burst speed for instant target acquisition without visual delay, terminating in an ultra-smooth, gentle deceleration landing.
- **Fluid Cubic-Bezier**: Implementation of modern UI curves (Apple Fluid & Material Design Fast-Out Slow-In) offering instant responsiveness with soft edge transitions.
- **Interactive Motion Engine Selector**: Added an intuitive mode cycler in Tab 2 (`Smooth`) with a dynamic parameter slider that automatically adapts to the chosen curve (Spring Tension, Overshoot Push, Glide Duration, or Glide Speed).
- **Zero-GC & High Performance**: All physics updates, velocity integration, and curve evaluation use primitive scalar math with zero heap object allocation on the render loop.
