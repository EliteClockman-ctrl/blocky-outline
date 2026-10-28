import json
import os
import re
import shutil
import subprocess
import zipfile
from pathlib import Path


def migrate_menu_screen(menu_path: Path):
    src = menu_path.read_text(encoding="utf-8")

    # 1. Replace imports
    src = src.replace(
        "import net.minecraft.client.gui.GuiGraphics;\n"
        "import net.minecraft.client.gui.screens.Screen;\n"
        "import net.minecraft.client.resources.sounds.SimpleSoundInstance;\n"
        "import net.minecraft.network.chat.Component;\n"
        "import net.minecraft.sounds.SoundEvents;\n"
        "import net.minecraft.util.Mth;",
        "import charaz.blockoutline.client.Compat26;\n"
        "import net.minecraft.client.gui.GuiGraphicsExtractor;\n"
        "import net.minecraft.client.gui.screens.Screen;\n"
        "import net.minecraft.client.input.KeyEvent;\n"
        "import net.minecraft.client.input.MouseButtonEvent;\n"
        "import net.minecraft.client.resources.sounds.SimpleSoundInstance;\n"
        "import net.minecraft.network.chat.Component;\n"
        "import net.minecraft.sounds.SoundEvents;"
    )

    if "private static float clamp(" not in src:
        src = src.replace(
            '    private static final String MOD_TITLE = "Blocky Outline";',
            '    private static float clamp(float val, float min, float max) {\n'
            '        return val < min ? min : Math.min(val, max);\n'
            '    }\n\n'
            '    private static final String MOD_TITLE = "Blocky Outline";'
        )

    # 2. Replace GuiGraphics -> GuiGraphicsExtractor & draw methods
    src = src.replace(
        "public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta)",
        "public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float delta)"
    )
    src = src.replace(
        "super.render(guiGraphics, mouseX, mouseY, delta);",
        "super.extractRenderState(guiGraphics, mouseX, mouseY, delta);"
    )
    src = src.replace("GuiGraphics ", "GuiGraphicsExtractor ")
    src = src.replace(".drawString(", ".text(")
    src = src.replace(".drawCenteredString(", ".centeredText(")
    src = src.replace("Mth.clamp(", "clamp(")
    src = src.replace(
        'charaz.blockoutline.client.compat.CompatBridge.openUrl("https://github.com/EliteClockman-ctrl/blocky-outline/issues");',
        "Compat26.openUrl(Compat26.GITHUB_ISSUES_URL);"
    )

    # 3. Replace mouse/key event handlers with 26.x Screen signatures
    src = src.replace(
        "    public boolean handleMouseClicked(double mx, double my, int button) {",
        "    @Override\n"
        "    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {\n"
        "        double mx = event.x();\n"
        "        double my = event.y();\n"
        "        int button = event.button();"
    )
    src = src.replace(
        "                uY += uCardH + uGap;\n"
        "            }\n"
        "        }\n\n"
        "        return false;\n"
        "    }\n\n"
        "    public boolean handleMouseReleased(double mx, double my, int button) {",
        "                uY += uCardH + uGap;\n"
        "            }\n"
        "        }\n\n"
        "        return super.mouseClicked(event, doubleClick);\n"
        "    }\n\n"
        "    @Override\n"
        "    public boolean mouseReleased(MouseButtonEvent event) {"
    )
    src = src.replace(
        "        if (this.draggingCube) {\n"
        "            this.draggingCube = false;\n"
        "            return true;\n"
        "        }\n"
        "        return false;\n"
        "    }\n\n"
        "    public boolean handleMouseDragged(double mx, double my, int button, double dragX, double dragY) {",
        "        if (this.draggingCube) {\n"
        "            this.draggingCube = false;\n"
        "            return true;\n"
        "        }\n"
        "        return super.mouseReleased(event);\n"
        "    }\n\n"
        "    @Override\n"
        "    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {\n"
        "        double mx = event.x();\n"
        "        double my = event.y();"
    )
    src = src.replace(
        "            this.updateSliderValue(this.activeSliderTab, this.activeSliderRow, mx, sX, sW);\n"
        "            return true;\n"
        "        }\n\n"
        "        return false;\n"
        "    }",
        "            this.updateSliderValue(this.activeSliderTab, this.activeSliderRow, mx, sX, sW);\n"
        "            return true;\n"
        "        }\n\n"
        "        return super.mouseDragged(event, dragX, dragY);\n"
        "    }"
    )
    src = src.replace(
        "    public boolean handleKeyPressed(int keyCode, int scanCode, int modifiers) {\n"
        "        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_M) {\n"
        "            this.onClose();\n"
        "            return true;\n"
        "        }\n"
        "        return false;\n"
        "    }",
        "    @Override\n"
        "    public boolean keyPressed(KeyEvent event) {\n"
        "        int keyCode = event.key();\n"
        "        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_M) {\n"
        "            this.onClose();\n"
        "            return true;\n"
        "        }\n"
        "        return super.keyPressed(event);\n"
        "    }"
    )

    menu_path.write_text(src, encoding="utf-8")


def set_menu_version(menu_path: Path, ver_tag: str):
    src = menu_path.read_text(encoding="utf-8")
    src = re.sub(
        r'private static final String MOD_VERSION = "v[^"]+";',
        f'private static final String MOD_VERSION = "{ver_tag}";',
        src
    )
    menu_path.write_text(src, encoding="utf-8")


def gather_classpath(base_dir: str, game_dir: str):
    version_name = "Fabric 26.2"
    version_dir = os.path.join(game_dir, "versions", version_name)
    json_path = os.path.join(version_dir, f"{version_name}.json")

    with open(json_path, "r", encoding="utf-8") as f:
        data = json.load(f)

    cp_jars = [
        os.path.join(base_dir, "src", "main", "java"),
        os.path.join(base_dir, "src", "client", "java"),
    ]
    for lib in data.get("libraries", []):
        parts = lib["name"].split(":")
        group, name, ver = parts[0].replace(".", os.sep), parts[1], parts[2]
        p = os.path.join(game_dir, "libraries", group, name, ver, f"{name}-{ver}.jar")
        if os.path.exists(p):
            cp_jars.append(p)

    # Put 26.2 jar first, then 26.1.2 jar (for MultiBufferSource in LevelRendererMixin26_1)
    cp_jars.append(os.path.join(version_dir, f"{version_name}.jar"))
    jar_26_1 = os.path.join(game_dir, "versions", "Fabric 26.1.2", "Fabric 26.1.2.jar")
    if os.path.exists(jar_26_1):
        cp_jars.append(jar_26_1)

    proc_dir = os.path.join(game_dir, ".fabric", "processedMods")
    if os.path.exists(proc_dir):
        for f in os.listdir(proc_dir):
            if f.endswith(".jar") and ("9e" in f or "fabric-api-base-2" in f):
                cp_jars.append(os.path.join(proc_dir, f))

    return cp_jars


def build_single_version(base_dir: str, cp_jars: list[str], mod_ver: str, mc_label: str, mc_depends: list[str]):
    print(f"\n=== Building Blocky Outline v{mod_ver} for Minecraft {mc_label} ===")
    menu_path = Path(base_dir) / "src" / "client" / "java" / "charaz" / "blockoutline" / "client" / "ui" / "BlockyOutlineMenuScreen.java"
    set_menu_version(menu_path, f"v{mod_ver}")

    classes_out = os.path.join(base_dir, "build", f"classes_{mod_ver}")
    if os.path.exists(classes_out):
        shutil.rmtree(classes_out)
    os.makedirs(classes_out, exist_ok=True)

    java_files = []
    for root_dir in [os.path.join(base_dir, "src", "main", "java"), os.path.join(base_dir, "src", "client", "java")]:
        for root, _, files in os.walk(root_dir):
            for file in files:
                if file.endswith(".java"):
                    java_files.append(os.path.join(root, file))

    javac = r"C:\Program Files\Eclipse Adoptium\jdk-25.0.4.101-hotspot\bin\javac.exe"
    arg_file = os.path.join(base_dir, "build", f"javac_args_{mod_ver}.txt")
    with open(arg_file, "w", encoding="utf-8") as f:
        cp_str = ";".join(cp_jars).replace("\\", "/")
        f.write("--release\n21\n-encoding\nUTF-8\n-cp\n\"" + cp_str + "\"\n-d\n\"" + classes_out.replace("\\", "/") + "\"\n")
        for jf in java_files:
            f.write("\"" + jf.replace("\\", "/") + "\"\n")

    res = subprocess.run([javac, f"@{arg_file}"], capture_output=True, text=True)
    if res.returncode != 0:
        print("Compilation FAILED:")
        print(res.stderr)
        raise RuntimeError(f"Compilation failed for {mod_ver}")

    dist_dir = os.path.join(base_dir, "dist")
    os.makedirs(dist_dir, exist_ok=True)
    jar_name = f"blocky-outline-{mod_ver}.jar"
    jar_path = os.path.join(dist_dir, jar_name)
    if os.path.exists(jar_path):
        os.remove(jar_path)

    with zipfile.ZipFile(jar_path, "w", compression=zipfile.ZIP_DEFLATED) as z:
        for root, _, files in os.walk(classes_out):
            for file in files:
                full = os.path.join(root, file)
                rel = os.path.relpath(full, classes_out).replace(os.sep, "/")
                z.write(full, rel)

        res_dir = os.path.join(base_dir, "src", "main", "resources")
        for root, _, files in os.walk(res_dir):
            for file in files:
                full = os.path.join(root, file)
                rel = os.path.relpath(full, res_dir).replace(os.sep, "/")
                if file == "fabric.mod.json":
                    with open(full, "r", encoding="utf-8") as rf:
                        mod_json = json.load(rf)
                    mod_json["version"] = mod_ver
                    mod_json["description"] = (
                        f"Customizable block selection outline and gradient fill with physics animation for Minecraft {mc_label}."
                    )
                    mod_json["mixins"] = ["blocky-outline.mixins.json"]
                    mod_json["depends"]["minecraft"] = mc_depends
                    z.writestr(rel, json.dumps(mod_json, indent=2).encode("utf-8"))
                else:
                    z.write(full, rel)

        lic = os.path.join(base_dir, "LICENSE")
        if os.path.exists(lic):
            z.write(lic, "LICENSE_blocky-outline")

    verify_jar_bytecode(jar_path)
    print(f"Verified & packaged: {jar_path}")
    return jar_path


def verify_jar_bytecode(jar_path: str):
    bad_patterns = [b"net/minecraft/class_", b"method_", b"field_", b"KEYSYM"]
    with zipfile.ZipFile(jar_path, "r") as z:
        names = z.namelist()
        assert "fabric.mod.json" in names, "Missing fabric.mod.json"
        assert "blocky-outline.mixins.json" in names, "Missing blocky-outline.mixins.json"
        for name in names:
            if name.endswith(".class"):
                data = z.read(name)
                for pat in bad_patterns:
                    if pat in data:
                        raise AssertionError(f"Forbidden bytecode reference {pat!r} found in {name} inside {jar_path}!")


def run_runtime_tests(base_dir: str, game_dir: str):
    print("\n=== Running Real JVM Runtime Verification Tests ===")
    java_exe = r"C:\Program Files\Eclipse Adoptium\jdk-25.0.4.101-hotspot\bin\java.exe"
    javac_exe = r"C:\Program Files\Eclipse Adoptium\jdk-25.0.4.101-hotspot\bin\javac.exe"

    test_java = """
import charaz.blockoutline.BlockyOutlineMod;
import charaz.blockoutline.client.BlockyOutlineClient;
import charaz.blockoutline.client.Compat26;
import charaz.blockoutline.client.renderer.BlockyOutlinePipelines;
import charaz.blockoutline.client.renderer.BlockyOutlineRenderTypes;
import charaz.blockoutline.client.renderer.OutlineRenderer;
import charaz.blockoutline.client.ui.BlockyOutlineMenuScreen;
import charaz.blockoutline.config.BlockyOutlineSettings;
import charaz.blockoutline.mixin.BlockyOutlineMixinPlugin;
import net.minecraft.client.KeyMapping;

public class RuntimeVerifier {
    public static void main(String[] args) throws Exception {
        String envName = args[0];
        boolean expect26_1 = Boolean.parseBoolean(args[1]);

        // 1. Test Mixin Plugin detection
        BlockyOutlineMixinPlugin plugin = new BlockyOutlineMixinPlugin();
        plugin.onLoad("charaz.blockoutline.mixin");
        boolean apply26_1 = plugin.shouldApplyMixin(
                "net.minecraft.client.renderer.LevelRenderer",
                "charaz.blockoutline.mixin.LevelRendererMixin26_1");
        boolean apply26_2 = plugin.shouldApplyMixin(
                "net.minecraft.client.renderer.LevelRenderer",
                "charaz.blockoutline.mixin.LevelRendererMixin26_2");
        if (apply26_1 != expect26_1 || apply26_2 == expect26_1) {
            throw new AssertionError("MixinPlugin mismatch: apply26_1=" + apply26_1 + ", apply26_2=" + apply26_2);
        }

        // 2. Test Settings copyFrom, color calculations, and presets (pure Java, zero MC classes)
        BlockyOutlineSettings s = BlockyOutlineSettings.get();
        BlockyOutlineSettings other = new BlockyOutlineSettings();
        other.rainbowOutline = true;
        other.fillEnabled = true;
        other.fillGradientEnabled = true;
        s.copyFrom(other);
        int c1 = s.getOutlineArgb(123456L);
        int c2 = s.getFillArgb(123456L);
        int c3 = s.getFillArgb2(123456L);
        if (c1 == 0 || c2 == 0 || c3 == 0) throw new AssertionError("Color calc failed");

        // 3. Test KeyMapping creation (verifies no InputConstants$Type.KEYSYM crash)
        KeyMapping km = Compat26.createMenuKeyMapping("key.blocky-outline.open_menu", 77);
        if (km == null) throw new AssertionError("KeyMapping creation returned null!");

        // 4. Test X-Ray Pipelines & RenderTypes creation across 26.1.x / 26.2 / 26.3
        if (BlockyOutlinePipelines.createXrayLinesPipeline() == null || BlockyOutlinePipelines.createXrayQuadsPipeline() == null) {
            throw new AssertionError("Pipelines null");
        }
        if (BlockyOutlineRenderTypes.xrayLines() == null || BlockyOutlineRenderTypes.xrayQuads() == null) {
            throw new AssertionError("RenderTypes null");
        }

        // 5. Execute actual ModInitializer & ClientModInitializer entrypoints!
        new BlockyOutlineMod().onInitialize();
        new BlockyOutlineClient().onInitializeClient();
        if (BlockyOutlineClient.getOpenMenuKey() == null) {
            throw new AssertionError("openMenuKey null after onInitializeClient()");
        }

        // 6. Force class linking of all client classes
        Class.forName(BlockyOutlineClient.class.getName(), true, RuntimeVerifier.class.getClassLoader());
        Class.forName(BlockyOutlineMenuScreen.class.getName(), true, RuntimeVerifier.class.getClassLoader());
        Class.forName(OutlineRenderer.class.getName(), true, RuntimeVerifier.class.getClassLoader());

        System.out.println("[RUNTIME PASS] " + envName
                + " | Key=" + km.getName()
                + " | FAPI(K/T/R)=" + BlockyOutlineClient.isKeyRegisteredViaFabricApi()
                + "/" + BlockyOutlineClient.isTickRegisteredViaFabricApi()
                + "/" + BlockyOutlineClient.isRenderRegisteredViaFabricApi()
                + " | 26.1Mixin=" + apply26_1
                + " | 26.2Mixin=" + apply26_2);
    }
}
"""

    def get_base_cp(ver_name, include_fapi):
        vdir = os.path.join(game_dir, "versions", ver_name)
        with open(os.path.join(vdir, f"{ver_name}.json"), "r", encoding="utf-8") as f:
            data = json.load(f)
        cp = []
        for lib in data.get("libraries", []):
            parts = lib["name"].split(":")
            g, n, v = parts[0].replace(".", os.sep), parts[1], parts[2]
            p = os.path.join(game_dir, "libraries", g, n, v, f"{n}-{v}.jar")
            if os.path.exists(p):
                cp.append(p)
        cp.append(os.path.join(vdir, f"{ver_name}.jar"))
        if include_fapi:
            proc_dir = os.path.join(game_dir, ".fabric", "processedMods")
            for f in os.listdir(proc_dir):
                if f.endswith(".jar"):
                    if ("26.1" in ver_name and ("84d87f9f3e" in f or "fabric-rendering-v1-23" in f or "fabric-lifecycle-events-v1-4.1.2" in f)) or (
                        "26.2" in ver_name and ("9e-" in f)
                    ):
                        cp.append(os.path.join(proc_dir, f))
        return cp

    tmp_dir = os.path.join(base_dir, "build", "runtime_test")
    os.makedirs(tmp_dir, exist_ok=True)
    vf = os.path.join(tmp_dir, "RuntimeVerifier.java")
    with open(vf, "w", encoding="utf-8") as f:
        f.write(test_java)

    mod_114 = os.path.join(base_dir, "dist", "blocky-outline-1.1.4.jar")
    mod_115 = os.path.join(base_dir, "dist", "blocky-outline-1.1.5.jar")
    mod_116 = os.path.join(base_dir, "dist", "blocky-outline-1.1.6.jar")

    cp_compile = get_base_cp("Fabric 26.2", False) + [mod_116]
    res = subprocess.run([javac_exe, "--release", "21", "-cp", ";".join(cp_compile), "-d", tmp_dir, vf], capture_output=True, text=True)
    if res.returncode != 0:
        raise RuntimeError(f"RuntimeVerifier compile failed: {res.stderr}")

    scenarios = [
        ("MC 26.1 (NO Fabric API - Standalone) + v1.1.4", "Fabric 26.1", False, mod_114, "true"),
        ("MC 26.1.2 (WITH Fabric API) + v1.1.4", "Fabric 26.1.2", True, mod_114, "true"),
        ("MC 26.2 (NO Fabric API - Standalone) + v1.1.5", "Fabric 26.2", False, mod_115, "false"),
        ("MC 26.2 (WITH Fabric API) + v1.1.5", "Fabric 26.2", True, mod_115, "false"),
        ("MC 26.3-sim (NO Fabric API - Standalone) + v1.1.6", "Fabric 26.2", False, mod_116, "false"),
        ("MC 26.3-sim (WITH Fabric API) + v1.1.6", "Fabric 26.2", True, mod_116, "false"),
    ]

    for label, vname, fapi, mod_jar, exp26_1 in scenarios:
        cp = [tmp_dir, mod_jar] + get_base_cp(vname, fapi)
        r = subprocess.run([java_exe, "-cp", ";".join(cp), "RuntimeVerifier", label, exp26_1], capture_output=True, text=True)
        if r.returncode != 0:
            print("FAILED:", label)
            print(r.stdout)
            print(r.stderr)
            raise RuntimeError(f"Runtime test failed for {label}")
        print(r.stdout.strip())


def main():
    base_dir = os.path.dirname(os.path.abspath(__file__))
    game_dir = r"D:\Minecraft\game"

    menu_path = Path(base_dir) / "src" / "client" / "java" / "charaz" / "blockoutline" / "client" / "ui" / "BlockyOutlineMenuScreen.java"
    migrate_menu_screen(menu_path)

    cp_jars = gather_classpath(base_dir, game_dir)

    targets = [
        ("1.1.4", "26.1.x", ["26.1", "26.1.1", "26.1.2", "26.1.x", ">=26.1 <26.2"]),
        ("1.1.5", "26.2", ["26.2", "26.2.x", ">=26.2 <26.3"]),
        ("1.1.6", "26.3", ["26.3", "26.3.x", ">=26.3 <26.4"]),
    ]

    built_jars = []
    for mod_ver, mc_label, mc_depends in targets:
        jp = build_single_version(base_dir, cp_jars, mod_ver, mc_label, mc_depends)
        built_jars.append(jp)

    run_runtime_tests(base_dir, game_dir)

    mods_dir = os.path.join(game_dir, "mods")
    if os.path.exists(mods_dir):
        for jp in built_jars:
            dst = os.path.join(mods_dir, os.path.basename(jp))
            shutil.copyfile(jp, dst)
            print(f"Copied to game mods: {dst}")


if __name__ == "__main__":
    main()
