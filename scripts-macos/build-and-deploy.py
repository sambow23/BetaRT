#!/usr/bin/env python3
"""Build the paired OpenRemix/BetaRT macOS client and deploy it to Prism.

OpenRemix is built with its native Metal backend unless --vulkan selects MoltenVK."""

import argparse
import datetime
import hashlib
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import sys


BETART = Path(__file__).resolve().parents[1]
OPENREMIX_DEFAULT = BETART.parent / "OpenRemix"
PRISM_DEFAULT = Path.home() / "Library/Application Support/PrismLauncher"
SDK_DEFAULT = Path("/Users/cr/VulkanSDK/1.4.350.1/macOS")


def run(command, cwd=None, env=None):
    print("+", " ".join(str(part) for part in command), flush=True)
    subprocess.run([str(part) for part in command], cwd=cwd, env=env, check=True)


def one_file(directory, pattern):
    matches = sorted(Path(directory).glob(pattern))
    if len(matches) != 1:
        raise RuntimeError(f"expected one {pattern} under {directory}, found {len(matches)}")
    return matches[0]


def update_openremix(repo):
    status = subprocess.check_output(["git", "status", "--porcelain"], cwd=repo, text=True)
    stashed = bool(status.strip())
    if stashed:
        stamp = datetime.datetime.now().strftime("%Y%m%d-%H%M%S")
        run(["git", "stash", "push", "-u", "-m", f"macOS build before pull {stamp}"], cwd=repo)
    try:
        run(["git", "pull", "--rebase", "origin", "mesh-surface-limit"], cwd=repo)
    finally:
        if stashed:
            result = subprocess.run(["git", "stash", "pop"], cwd=repo)
            if result.returncode:
                raise RuntimeError("OpenRemix stash could not be reapplied; resolve the worktree and retry")


def patch_wrapper(instance, deployment):
    config = instance / "instance.cfg"
    text = config.read_text()
    replacement = 'WrapperCommand=\\"' + str(deployment / "launch.py") + '\\"'
    updated, count = re.subn(r"^WrapperCommand=.*$", replacement, text, flags=re.MULTILINE)
    if count != 1:
        raise RuntimeError(f"expected one WrapperCommand in {config}")
    config.write_text(updated)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--openremix", type=Path, default=OPENREMIX_DEFAULT)
    parser.add_argument("--prism", type=Path, default=PRISM_DEFAULT)
    parser.add_argument("--vulkan-sdk", type=Path, default=SDK_DEFAULT)
    parser.add_argument("--deployment", type=Path,
                        help="existing deployment directory; defaults to the deployment manifest")
    parser.add_argument("--pull-openremix", action="store_true",
                        help="stash local OpenRemix edits, pull origin/mesh-surface-limit, then reapply them")
    parser.add_argument("--vulkan", action="store_true",
                        help="build the Vulkan backend and run it on MoltenVK instead of Metal")
    parser.add_argument("--rebuild-moltenvk", action="store_true",
                        help="with --vulkan, rebuild the pinned patched MoltenVK/ SPIRV-Cross driver")
    parser.add_argument("--use-sdk-moltenvk", action="store_true",
                        help="with --vulkan, deploy the SDK driver instead of the patched experimental driver")
    parser.add_argument("--reuse-remix-build", action="store_true",
                        help="package the existing OpenRemix build instead of building and testing it")
    parser.add_argument("--no-backup", action="store_true",
                        help="skip the APFS clone backup before deployment")
    parser.add_argument("--deploy", action="store_true",
                        help="copy the finished artifacts into the Prism instance")
    args = parser.parse_args()

    openremix = args.openremix.resolve()
    prism = args.prism.expanduser().resolve()
    instance = prism / "instances/b1.7.3"
    sdk = args.vulkan_sdk.expanduser().resolve()
    if not openremix.is_dir() or not instance.is_dir():
        raise RuntimeError("OpenRemix or Prism b1.7.3 path does not exist")

    if args.pull_openremix:
        update_openremix(openremix)

    deps = openremix / "_work/deps/macos"
    remix_build = openremix / ("_work/build/macos" if args.vulkan else "_work/build/macos-metal")
    run([sys.executable, openremix / "scripts/common/build-dependencies.py", "--output", deps])

    if args.vulkan and args.rebuild_moltenvk:
        run([sys.executable, openremix / "tools/moltenvk/build-moltenvk.py"], cwd=openremix,
            env=dict(os.environ, CPM_SOURCE_CACHE="/Users/cr/.cache/CPM"))
    if args.reuse_remix_build:
        run(["meson", "compile", "-C", remix_build])
    else:
        backend = ["--vulkan-sdk", sdk] if args.vulkan else ["--metal"]
        run([sys.executable, openremix / "scripts/macos/build-remix.py", "--deps", deps,
             "--build", remix_build, *backend])

    stamp = datetime.datetime.now().strftime("%Y%m%d-%H%M%S")
    package = openremix / "_work/packages" / f"macos-deploy-{stamp}"
    run([sys.executable, openremix / "scripts/common/package-remix.py",
         "--build", remix_build, "--deps", deps, "--output", package,
         *(["--vulkan-sdk", sdk] if args.vulkan else [])])

    patched_driver = openremix / "_work/deps/moltenvk/prefix/lib/libMoltenVK.dylib"
    if args.vulkan and not args.use_sdk_moltenvk:
        if not patched_driver.is_file():
            raise RuntimeError(f"patched driver missing: {patched_driver}; use --rebuild-moltenvk")
        shutil.copy2(patched_driver, package / "lib/libMoltenVK.dylib")
        run(["install_name_tool", "-id", "@rpath/libMoltenVK.dylib", package / "lib/libMoltenVK.dylib"])
        run(["codesign", "--force", "--sign", "-", package / "lib/libMoltenVK.dylib"])

    beta_build = BETART / "_work/build/macos"
    java_home = subprocess.check_output(["/usr/libexec/java_home", "-v17"], text=True).strip()
    run(["cmake", "-S", BETART, "-B", beta_build, "-G", "Ninja",
         "-DCMAKE_BUILD_TYPE=Release", "-DMCRTX_BUILD_TESTS=ON",
         "-DMCRTX_DXVK_REMIX_ROOT=" + str(openremix)],
        env=dict(os.environ, JAVA_HOME=java_home, CC="clang", CXX="clang++"))
    run(["cmake", "--build", beta_build, "-j8"])
    run(["ctest", "--test-dir", beta_build, "--output-on-failure"])

    prism_libraries = prism / "libraries"
    vanilla = prism_libraries / "com/mojang/minecraft/b1.7.3/minecraft-b1.7.3-client.jar"
    lwjgl = prism_libraries / "org/lwjgl/lwjgl/lwjgl/2.9.4-nightly-20150209/lwjgl-2.9.4-nightly-20150209.jar"
    lwjgl_util = prism_libraries / "org/lwjgl/lwjgl/lwjgl_util/2.9.4-nightly-20150209/lwjgl_util-2.9.4-nightly-20150209.jar"
    asm = one_file(Path.home() / ".gradle/caches/modules-2/files-2.1/org.ow2.asm/asm/9.9", "*/asm-9.9.jar")
    asm_tree = one_file(Path.home() / ".gradle/caches/modules-2/files-2.1/org.ow2.asm/asm-tree/9.9", "*/asm-tree-9.9.jar")
    client_build = BETART / "_work/build" / f"client-macos-{stamp}"
    coreutils = "/opt/homebrew/opt/coreutils/libexec/gnubin"
    run(["/opt/homebrew/bin/bash", BETART / "scripts-linux/build-client.sh",
         vanilla, lwjgl, lwjgl_util, asm, asm_tree, client_build],
        env=dict(os.environ, JAVA_HOME=java_home,
                 PATH=coreutils + os.pathsep + os.environ.get("PATH", "")))

    if not args.deploy:
        print(f"Build complete. Package: {package}")
        print("Pass --deploy to update the Prism instance.")
        return

    manifest = BETART / "_work/deployment-macos.json"
    deployment = args.deployment.resolve() if args.deployment else Path(
        json.loads(manifest.read_text())["deployment"]
    ).resolve()
    if not deployment.is_dir():
        raise RuntimeError(f"deployment directory does not exist: {deployment}")

    if not args.no_backup:
        backup = prism / "backups" / f"b1.7.3-before-betart-{stamp}"
        backup.parent.mkdir(parents=True, exist_ok=True)
        run(["cp", "-cR", instance, backup])

    # Replace the runtime wholesale so a Metal deploy leaves no MoltenVK behind.
    shutil.rmtree(deployment / "runtime", ignore_errors=True)
    shutil.copytree(package, deployment / "runtime", symlinks=True)
    shutil.copy2(beta_build / "native/libmcrtx_jni.dylib", deployment / "libmcrtx_jni.dylib")
    shutil.copy2(BETART / "scripts-macos/launch.py", deployment / "launch.py")
    (deployment / "launch.py").chmod(0o755)

    patch = json.loads((instance / "patches/net.minecraft.json").read_text())
    jar_name = patch["mainJar"]["MMC-filename"]
    shutil.copy2(client_build / "minecraft-b1.7.3-client-mcrtx.jar", instance / "libraries" / jar_name)
    patch_wrapper(instance, deployment)
    manifest_data = json.loads(manifest.read_text()) if manifest.is_file() else {}
    manifest_data.update({
        "deployment": str(deployment),
        "backup": str(backup) if not args.no_backup else manifest_data.get("backup", ""),
        "client": str(instance / "libraries" / jar_name),
        "backend": ("vulkan/moltenvk" if args.vulkan else "metal") + " pathtrace; ray primary visibility",
    })
    if args.vulkan:
        manifest_data["driver_sha256"] = hashlib.sha256(
            (deployment / "runtime/lib/libMoltenVK.dylib").read_bytes()).hexdigest()
    else:
        manifest_data.pop("driver_sha256", None)
    manifest.write_text(json.dumps(manifest_data, indent=2) + "\n")
    print(f"Deployed paired client, JNI, and OpenRemix runtime to {deployment}")


if __name__ == "__main__":
    try:
        main()
    except (OSError, RuntimeError, subprocess.CalledProcessError) as error:
        print(f"error: {error}", file=sys.stderr)
        raise SystemExit(1)
