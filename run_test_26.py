import json, os, subprocess

game_dir = r"D:\Minecraft\game"
version_dir = os.path.join(game_dir, "versions", "Fabric 26.1.2")
json_path = os.path.join(version_dir, "Fabric 26.1.2.json")
natives_dir = os.path.join(version_dir, "natives")

with open(json_path, "r", encoding="utf-8") as f:
    data = json.load(f)

cp_jars = []
for lib in data.get("libraries", []):
    parts = lib["name"].split(":")
    group, name, ver = parts[0].replace(".", os.sep), parts[1], parts[2]
    jar_name = f"{name}-{ver}.jar"
    p = os.path.join(game_dir, "libraries", group, name, ver, jar_name)
    if os.path.exists(p):
        cp_jars.append(p)

client_jar = os.path.join(version_dir, "Fabric 26.1.2.jar")
if os.path.exists(client_jar):
    cp_jars.append(client_jar)

java_exe = r"C:\Program Files\Eclipse Adoptium\jdk-25.0.4.101-hotspot\bin\java.exe"

args = [
    java_exe,
    f"-Djava.library.path={natives_dir}",
    "-Dorg.lwjgl.librarypath=" + natives_dir,
    "-cp", ";".join(cp_jars),
    "net.fabricmc.loader.impl.launch.knot.KnotClient",
    "--username", "Player",
    "--version", "Fabric 26.1.2",
    "--gameDir", game_dir,
    "--assetsDir", os.path.join(game_dir, "assets"),
    "--assetIndex", "17",
    "--uuid", "00000000-0000-0000-0000-000000000000",
    "--accessToken", "0",
    "--userType", "mojang",
    "--versionType", "release"
]

print("Launching Minecraft Fabric 26.1.2 with Blocky Outline...")
subprocess.run(args, cwd=game_dir)
