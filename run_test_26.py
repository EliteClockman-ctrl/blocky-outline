import json, os, subprocess, zipfile

game_dir = r"D:\Minecraft\game"
version_name = "Fabric 26.2" if os.path.exists(os.path.join(game_dir, "versions", "Fabric 26.2")) else "Fabric 26.1.2"
version_dir = os.path.join(game_dir, "versions", version_name)
json_path = os.path.join(version_dir, f"{version_name}.json")
natives_dir = os.path.join(version_dir, "natives")
os.makedirs(natives_dir, exist_ok=True)

mods_26 = os.path.join(game_dir, "mods_26")
os.makedirs(mods_26, exist_ok=True)

with open(json_path, "r", encoding="utf-8") as f:
    data = json.load(f)

cp_jars = []
for lib in data.get("libraries", []):
    parts = lib["name"].split(":")
    group = parts[0].replace(".", os.sep)
    artifact = parts[1]
    ver = parts[2]
    classifier = parts[3] if len(parts) > 3 else ""

    if classifier == "natives-windows":
        nat_jar = os.path.join(game_dir, "libraries", group, artifact, ver, f"{artifact}-{ver}-natives-windows.jar")
        if os.path.exists(nat_jar):
            try:
                with zipfile.ZipFile(nat_jar) as z:
                    for entry in z.namelist():
                        if entry.endswith(".dll"):
                            dst = os.path.join(natives_dir, os.path.basename(entry))
                            if not os.path.exists(dst):
                                with open(dst, "wb") as out_f:
                                    out_f.write(z.read(entry))
            except Exception:
                pass
    elif classifier not in ("natives-linux", "natives-macos", "natives-macos-arm64", "natives-windows", "natives-windows-arm64", "natives-windows-x86"):
        jar_name = f"{artifact}-{ver}-{classifier}.jar" if classifier else f"{artifact}-{ver}.jar"
        p = os.path.join(game_dir, "libraries", group, artifact, ver, jar_name)
        if os.path.exists(p) and p not in cp_jars:
            cp_jars.append(p)
        std_p = os.path.join(game_dir, "libraries", group, artifact, ver, f"{artifact}-{ver}.jar")
        if os.path.exists(std_p) and std_p not in cp_jars:
            cp_jars.append(std_p)

client_jar = os.path.join(version_dir, f"{version_name}.jar")
if os.path.exists(client_jar):
    cp_jars.append(client_jar)

java_exe = r"C:\Program Files\Eclipse Adoptium\jdk-25.0.4.101-hotspot\bin\java.exe"

args = [
    java_exe,
    f"-Djava.library.path={natives_dir}",
    "-Dorg.lwjgl.librarypath=" + natives_dir,
    f"-Dfabric.modsFolder={mods_26}",
    "-cp", ";".join(cp_jars),
    "net.fabricmc.loader.impl.launch.knot.KnotClient",
    "--username", "Player",
    "--version", version_name,
    "--gameDir", game_dir,
    "--assetsDir", os.path.join(game_dir, "assets"),
    "--assetIndex", "17",
    "--uuid", "00000000-0000-0000-0000-000000000000",
    "--accessToken", "0",
    "--userType", "mojang",
    "--versionType", "release"
]

print(f"Launching Minecraft {version_name} with Blocky Outline v1.1.3...")
subprocess.run(args, cwd=game_dir)
