import os
import shutil
import subprocess
import json
import zipfile

def build_mod():
    print("=== Building Blocky Outline v1.1.3 for Minecraft 26.x ===")
    base_dir = os.path.dirname(os.path.abspath(__file__))
    game_dir = r"D:\Minecraft\game"
    
    # Select Fabric 26.x version directory (prefer 26.2)
    version_name = "Fabric 26.2" if os.path.exists(os.path.join(game_dir, "versions", "Fabric 26.2")) else "Fabric 26.1.2"
    version_dir = os.path.join(game_dir, "versions", version_name)
    json_path = os.path.join(version_dir, f"{version_name}.json")
    print(f"Targeting Minecraft version: {version_name}")
    
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
            
    cp_jars.append(os.path.join(version_dir, f"{version_name}.jar"))
    
    # Add Fabric API jars for 26.x
    proc_dir = os.path.join(game_dir, ".fabric", "processedMods")
    if os.path.exists(proc_dir):
        for f in os.listdir(proc_dir):
            if f.endswith(".jar") and ("9e" in f or "fabric-api-base-2" in f):
                cp_jars.append(os.path.join(proc_dir, f))

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
    arg_file = os.path.join(base_dir, "build", "javac_args.txt")
    with open(arg_file, "w", encoding="utf-8") as f:
        cp_str = ";".join(cp_jars).replace("\\", "/")
        f.write("-encoding\nUTF-8\n-cp\n\"" + cp_str + "\"\n-d\n\"" + classes_out.replace("\\", "/") + "\"\n")
        for jf in java_files:
            f.write("\"" + jf.replace("\\", "/") + "\"\n")

    print(f"Compiling {len(java_files)} Java source files with JDK 25...")
    cmd = [javac, f"@{arg_file}"]
    res = subprocess.run(cmd, capture_output=True, text=True)
    if res.returncode != 0:
        print("Compilation FAILED:")
        print(res.stderr)
        raise RuntimeError("Compilation failed")
    print("Compilation successful!")

    # 3. Create Jar
    dist_dir = os.path.join(base_dir, "dist")
    os.makedirs(dist_dir, exist_ok=True)
    jar_name = "blocky-outline-1.1.3.jar"
    jar_path = os.path.join(dist_dir, jar_name)
    if os.path.exists(jar_path):
        os.remove(jar_path)

    # Clean up old demo jar in dist
    old_demo = os.path.join(dist_dir, "blocky-outline-demo-1.1.3.jar")
    if os.path.exists(old_demo):
        os.remove(old_demo)

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
                        content = rf.read().replace("${version}", "1.1.3")
                    z.writestr(rel, content.encode("utf-8"))
                else:
                    z.write(full, rel)

        # Add license if present
        lic = os.path.join(base_dir, "LICENSE")
        if os.path.exists(lic):
            z.write(lic, "LICENSE_blocky-outline")

    print(f"Jar successfully created at: {jar_path}")
    
    # 4. Copy to game mods directory and mods_26
    dest_mod = os.path.join(game_dir, "mods", jar_name)
    shutil.copyfile(jar_path, dest_mod)
    print(f"Copied to: {dest_mod}")

    # Remove old demo jar from mods
    old_mod = os.path.join(game_dir, "mods", "blocky-outline-demo-1.1.3.jar")
    if os.path.exists(old_mod):
        os.remove(old_mod)

    mods_26 = os.path.join(game_dir, "mods_26")
    os.makedirs(mods_26, exist_ok=True)
    dest_mod_26 = os.path.join(mods_26, jar_name)
    shutil.copyfile(jar_path, dest_mod_26)
    print(f"Copied to: {dest_mod_26}")

    old_mod_26 = os.path.join(mods_26, "blocky-outline-demo-1.1.3.jar")
    if os.path.exists(old_mod_26):
        os.remove(old_mod_26)

    # Ensure required 26.x fabric-api submodules are present in mods_26
    proc_dir = os.path.join(game_dir, ".fabric", "processedMods")
    needed_fapi = [
        "fabric-rendering-v1-25.3.2+515ac5339e-ef38fe27d6988403.jar",
        "fabric-key-mapping-api-v1-2.0.5+e2bdee789e-40579a8e16a5fe53.jar",
        "fabric-lifecycle-events-v1-4.1.3+4575b05f9e-366e4ba6f40f2e4.jar",
        "fabric-api-base-2.0.4+ece063239e-25b880d50d5aa2e2.jar"
    ]
    for jar in needed_fapi:
        src_jar = os.path.join(proc_dir, jar)
        dst_jar = os.path.join(mods_26, jar)
        if os.path.exists(src_jar) and not os.path.exists(dst_jar):
            shutil.copyfile(src_jar, dst_jar)
        # Patch fabric-rendering-v1 to remove incompatible BuiltInBlockModelsMixin on 26.2 if present
        if "fabric-rendering-v1" in jar and os.path.exists(dst_jar):
            try:
                with zipfile.ZipFile(dst_jar, 'r') as z:
                    mixin_str = z.read('fabric-rendering-v1.mixins.json').decode('utf-8')
                    mdata = json.loads(mixin_str)
                    if 'BuiltInBlockModelsMixin' in mdata.get('client', []):
                        all_entries = {n: z.read(n) for n in z.namelist()}
                        mdata['client'].remove('BuiltInBlockModelsMixin')
                        all_entries['fabric-rendering-v1.mixins.json'] = json.dumps(mdata, indent=2).encode('utf-8')
                        with zipfile.ZipFile(dst_jar, 'w', zipfile.ZIP_DEFLATED) as zw:
                            for n, c in all_entries.items():
                                zw.writestr(n, c)
                        print("Auto-patched fabric-rendering-v1 BuiltInBlockModelsMixin.")
            except Exception as e:
                print(f"Warning: could not check fabric-rendering-v1: {e}")

    # Remove obsolete versions from game mods and mods_26
    for folder in [os.path.join(game_dir, "mods"), mods_26]:
        for old_name in ["blocky-outline-v1.1.2.jar", "blocky-outline-v1.1.3.jar"]:
            old_p = os.path.join(folder, old_name)
            if os.path.exists(old_p):
                try:
                    os.remove(old_p)
                    print(f"Removed older mod jar: {old_p}")
                except Exception:
                    pass

if __name__ == "__main__":
    build_mod()
