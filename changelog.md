# Changelog

## What's new in Blocky Outline v1.1.6.1 (Minecraft 26.3):

### 🛠️ Bug Fixes & Compatibility Overhaul
- **Fixed `NoClassDefFoundError: net/minecraft/class_2561` (`Component`) on Client Init**: Fixed a critical crash caused by Intermediary remapping on unobfuscated Minecraft 26.3 runtime. Built directly against official Mojang mappings.
- **Fixed `NoClassDefFoundError: net/minecraft/class_3532` (`Mth`) with Existing Configs**: Rewrote `BlockyOutlineSettings` to use pure Java mathematics, completely decoupling config loading from Minecraft internal classes.
- **Fixed `NoSuchFieldError: InputConstants$Type KEYSYM`**: Resolved breaking changes in Mojang's input enum structure via dynamic constructor delegation.
- **Standalone Mode (No Fabric API Required)**: Blocky Outline now runs 100% standalone on clean Fabric instances without crashing when Fabric API is not installed, powered by dynamic Mixin fallbacks (`OptionsMixin`, `MinecraftMixin`, `LevelRendererMixin`).
- **Eliminated Package Collision & SecurityExceptions**: Moved pipeline and render type registrations to internal packages (`charaz.blockoutline.client.renderer`), preventing signer security mismatches with Mojang jars.

---

## What's new in Blocky Outline v1.1.5.1 (Minecraft 26.2):

### 🛠️ Bug Fixes & Compatibility Overhaul
- **Targeted & Optimized for Minecraft 26.2**: Fully adapted to 26.2 architecture changes, including the migration from `MultiBufferSource` to `SubmitNodeCollector`.
- **Screen API Modernization**: Seamlessly bridges `mc.gui.screen()` / `mc.gui.setScreen()` for 26.2 GUI management.
- **Fixed Intermediary Mapping Crashes**: Fixed missing `class_XXXX` exceptions by compiling clean unobfuscated Mojang bytecode.
- **Full Standalone Support**: Auto-detects whether Fabric API is present at runtime. If absent, lightweight Mixins automatically take over keybinding, tick handling, and outline submission.
- **Pure Java Config Engine**: Configuration files load with zero dependency on game classes, preventing crashes during game initialization.

---

## What's new in Blocky Outline v1.1.4.1 (Minecraft 26.1.x):

### 🛠️ Bug Fixes & Compatibility Overhaul
- **Dedicated Minecraft 26.1.x Build**: Tailored specifically for Minecraft 26.1, 26.1.1, and 26.1.2.
- **Dual Render Compatibility**: Retains support for 26.1's `MultiBufferSource` block outline pipeline while isolating it from newer versions.
- **Fixed `mc.screen` Field Differences**: Dynamically supports direct screen access without triggering missing field errors.
- **Full Standalone & Fabric API Support**: Works out-of-the-box whether Fabric API is installed or not.
- **Zero-GC & Fast-Path Rendering**: Retains zero-allocation standard block rendering and all 6 smooth motion physics engines.
