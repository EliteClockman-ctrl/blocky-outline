import os
import shutil
import subprocess
import json
import zipfile

def build_mod():
    print("=== Building Blocky Outline for Minecraft 26.1.x ===")
    base_dir = os.path.dirname(os.path.abspath(__file__))
    game_dir = r"D:\Minecraft\game"
    version_dir = os.path.join(game_dir, "versions", "Fabric 26.1.2")
    json_path = os.path.join(version_dir, "Fabric 26.1.2.json")
    
    with open(json_path, "r", encoding="utf-8") as f:
        data = json.load(f)
    
    # 1. Gather classpath
    cp_jars = [
        os.path.join(base_dir, "src", "main", "java"),
        os.path.join(base_dir, "src", "client", "java")
    ]
    for lib in data.get("libraries", []):
        parts = lib["name"].split(":")
        group, name, ver = parts[0].replace(".", os.sep), parts[1], parts[2]
        p = os.path.join(game_dir, "libraries", group, name, ver, f"{name}-{ver}.jar")
        if os.path.exists(p):
            cp_jars.append(p)
            
    cp_jars.append(os.path.join(version_dir, "Fabric 26.1.2.jar"))
    
    # Extract Fabric API 26.1.2 sub-jars if needed
    fapi_dir = os.path.join(base_dir, "build", "fapi_jars")
    os.makedirs(fapi_dir, exist_ok=True)
    fapi_fat = os.path.join(game_dir, "mods", "fabric-api-0.155.3+26.1.2.jar")
    with zipfile.ZipFile(fapi_fat) as z:
        for n in z.namelist():
            if n.startswith("META-INF/jars/") and n.endswith(".jar"):
                fname = os.path.basename(n)
                out_p = os.path.join(fapi_dir, fname)
                if not os.path.exists(out_p):
                    with open(out_p, "wb") as out_f:
                        out_f.write(z.read(n))
                cp_jars.append(out_p)

    # 2. Compile java files
    classes_out = os.path.join(base_dir, "build", "classes_26")
    if os.path.exists(classes_out):
        shutil.rmtree(classes_out)
    os.makedirs(classes_out, exist_ok=True)

    java_files = []
    for root_dir in [os.path.join(base_dir, "src", "main", "java"), os.path.join(base_dir, "src", "client", "java")]:
        for root, dirs, files in os.walk(root_dir):
            for file in files:
                if file.endswith(".java"):
                    java_files.append(os.path.join(root, file))

    javac = r"C:\Program Files\Eclipse Adoptium\jdk-25.0.4.101-hotspot\bin\javac.exe"
    cmd = [javac, "-encoding", "UTF-8", "-cp", ";".join(cp_jars), "-d", classes_out] + java_files
    print(f"Compiling {len(java_files)} Java source files with JDK 25...")
    res = subprocess.run(cmd, capture_output=True, text=True)
    if res.returncode != 0:
        print("Compilation FAILED:")
        print(res.stderr)
        raise RuntimeError("Compilation failed")
    print("Compilation successful!")

    # 3. Create Jar
    dist_dir = os.path.join(base_dir, "dist")
    os.makedirs(dist_dir, exist_ok=True)
    jar_path = os.path.join(dist_dir, "blocky-outline-v1.1.2.jar")
    if os.path.exists(jar_path):
        os.remove(jar_path)

    with zipfile.ZipFile(jar_path, "w", compression=zipfile.ZIP_DEFLATED) as z:
        # Add class files
        for root, dirs, files in os.walk(classes_out):
            for file in files:
                full = os.path.join(root, file)
                rel = os.path.relpath(full, classes_out).replace(os.sep, "/")
                z.write(full, rel)
        
        # Add resources
        res_dir = os.path.join(base_dir, "src", "main", "resources")
        for root, dirs, files in os.walk(res_dir):
            for file in files:
                full = os.path.join(root, file)
                rel = os.path.relpath(full, res_dir).replace(os.sep, "/")
                if file == "fabric.mod.json":
                    with open(full, "r", encoding="utf-8") as rf:
                        content = rf.read().replace("${version}", "1.1.2")
                    z.writestr(rel, content.encode("utf-8"))
                else:
                    z.write(full, rel)

        # Add license if present
        lic = os.path.join(base_dir, "LICENSE")
        if os.path.exists(lic):
            z.write(lic, "LICENSE_blocky-outline")

    print(f"Jar successfully created at: {jar_path}")
    
    # 4. Copy to game mods directory
    dest_mod = os.path.join(game_dir, "mods", "blocky-outline-v1.1.2.jar")
    shutil.copyfile(jar_path, dest_mod)
    print(f"Copied to: {dest_mod}")

if __name__ == "__main__":
    build_mod()
