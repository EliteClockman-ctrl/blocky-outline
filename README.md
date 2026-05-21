# 🟩 Blocky Outline

A premium, high-performance Minecraft Fabric mod designed to enhance your block targeting visuals. **Blocky Outline** replaces the default, basic selection box with a highly customizable, animated outline and a vibrant translucent face fill, complete with a fully interactive in-game configuration menu.

---

## ✨ Features

* **🌈 Dynamic Rainbow Effects:** Enable cycling RGB rainbow transitions on both the block outline and the face fill independently.
* **⚡ Adjustable Speeds & Colors:** Control the transition speed of rainbow effects, or pick static custom colors using modern HSL slider controls.
* **📐 Thickness & Opacity Tuning:** Smoothly scale outline thickness and opacity, as well as the face fill transparency, to suit your aesthetic preferences.
* **🛡️ Iris & Sodium Ready:** Built with an advanced double-faced rendering technique (dual CW/CCW winding order) and slight geometric inflation to ensure perfect translucent rendering on all 6 faces under modern optimization engines and shader packs.
* **⚡ Zero-Latency Caching:** Features a frame-synchronized cache reset (`BEFORE_ENTITIES`) that completely eliminates outline lag, drift, or floating coordinate displacement.
* **⌨️ Native Keybinding:** Easily open the customizer in-game using the **M** hotkey, beautifully categorized under its own translated "Blocky Outline" controls group.

---

## 🎮 How to Use

1. Enter your Minecraft world.
2. Press the **`M`** key (default) to open the interactive customizer menu.
   * *If needed, this keybind can be remapped directly inside the standard Minecraft controls settings menu.*
3. Toggle options dynamically:
   * **Left Panel:** Manage outline settings (Rainbow mode, cycle speed, hue sliders, opacity, and line thickness).
   * **Right Panel:** Manage translucent face fill settings (Toggle fill, Rainbow mode, speed, hue sliders, and transparency).
4. Click **Done** or press **Esc** to save changes and return to the game immediately.

---

## 🛠️ Installation

### Requirements
* Minecraft **1.21.1**
* Fabric Loader (>=0.19.2)
* Fabric API

### Steps
1. Make sure you have installed **Fabric Loader** and the **Fabric API** for Minecraft 1.21.1.
2. Download the `blocky-outline-1.0.0.jar` from the releases or build it yourself.
3. Place the `.jar` file into your Minecraft `mods` folder.
4. Launch the game!

---

## 💻 Building from Source

To compile the mod yourself, follow these standard Gradle commands:

### 1. Clone the repository
```bash
git clone https://github.com/your-username/blocky-outline.git
cd blocky-outline
```

### 2. Compile the mod
Run the following build command in your terminal:
```powershell
# Windows
.\gradlew build

# macOS / Linux
./gradlew build
```
The compiled, remapped, and production-ready `.jar` file will be located under `dist/` or `build/libs/`.

### 3. Run the development environment
To launch Minecraft directly in a development environment for testing:
```powershell
.\gradlew runClient
```

---

## 🔬 Tech Stack & Architecture Notes

* **Fabric Modding Toolchain:** Configured under Fabric Loom `1.16.2` using official Mojang mappings.
* **Double-Faced Rendering (`OutlineRenderer.java`):** Iterates over VoxelShape bounding boxes, applying a subtle `0.005D` offset inflation factor. Each face is rendered with both clockwise and counter-clockwise vertex orders to bypass backface culling and Z-fighting issues under heavy optimizations (Iris/Sodium/Canvas).
* **Frame-Synchronized Cache Loop (`BlockyOutlineClient.java`):** Captures targets during the `BEFORE_BLOCK_OUTLINE` pass and caches coordinate state for subsequent processing in `BEFORE_TRANSLUCENT`, reset cleanly at the start of each frame inside the `BEFORE_ENTITIES` pass. This prevents 1-frame latency slips and prevents the highlight from getting stuck to the ground.

---

## 📄 License

This mod template is available under the **CC0-1.0** license. Feel free to learn from it, customize it, and incorporate it in your own creative projects.
