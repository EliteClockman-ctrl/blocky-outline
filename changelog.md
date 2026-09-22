## What's new in version 1.1.2:

- **Minecraft version**: Ported and updated support for Minecraft 26.1.x Fabric (Java 25).
- **GUI Engine Update**: Migrated settings menu to Minecraft 26.1's new `GuiGraphicsExtractor` pipeline and native input events (`MouseButtonEvent`, `KeyEvent`, `CharacterEvent`).
- **Render Pipeline Update**: Migrated block outline hooks to `LevelRenderEvents.BEFORE_BLOCK_OUTLINE` and `BlockOutlineRenderState`.
- **Key Mapping API**: Updated keybind registration to the new `KeyMappingHelper` API.
- **Bug Fixes**: Fixed startup crash (`NoClassDefFoundError` / unmapped runtime compatibility) and native LWJGL library loading issues.
