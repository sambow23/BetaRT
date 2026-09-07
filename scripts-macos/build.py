#!/usr/bin/env python3
"""Build the native bridge, patched Java client, and vanilla DDS assets on macOS."""
import argparse
import hashlib
import os
from pathlib import Path
import subprocess
import urllib.request

root = Path(__file__).resolve().parent.parent
parser = argparse.ArgumentParser()
parser.add_argument('--prism', type=Path, default=Path.home() / 'Library/Application Support/PrismLauncher')
parser.add_argument('--remix', type=Path, default=root.parent / 'dxvk-remix-gmod')
parser.add_argument('--output', type=Path, default=root / 'out/macos')
args = parser.parse_args()
out = args.output.resolve()
out.mkdir(parents=True, exist_ok=True)
libs = args.prism / 'libraries'
java_home = Path(os.environ.get('JAVA_HOME') or subprocess.check_output(['/usr/libexec/java_home'], text=True).strip())
def run(*command, **kwargs):
    subprocess.run([str(c) for c in command], check=True, **kwargs)

run('cmake', '-S', root, '-B', out/'native-build', '-G', 'Ninja', '-DCMAKE_BUILD_TYPE=Release', '-DMCRTX_BUILD_TESTS=ON', f'-DMCRTX_DXVK_REMIX_ROOT={args.remix.resolve()}')
run('cmake', '--build', out/'native-build', '-j', os.cpu_count() or 4)
asm_tree = out / 'asm-tree-9.6.jar'
if not asm_tree.exists():
    urllib.request.urlretrieve('https://repo.maven.apache.org/maven2/org/ow2/asm/asm-tree/9.6/asm-tree-9.6.jar', asm_tree)
if hashlib.sha256(asm_tree.read_bytes()).hexdigest() != 'c43ecf17b539c777e15da7b5b86553b377e2d39a683de6285567d5283888e7ef':
    raise SystemExit('ASM tree checksum mismatch')
vanilla = libs/'com/mojang/minecraft/b1.7.3/minecraft-b1.7.3-client.jar'
lwjgl = libs/'org/lwjgl/lwjgl/lwjgl/2.9.4-nightly-20150209/lwjgl-2.9.4-nightly-20150209.jar'
lwjgl_util = libs/'org/lwjgl/lwjgl/lwjgl_util/2.9.4-nightly-20150209/lwjgl_util-2.9.4-nightly-20150209.jar'
asm = libs/'org/ow2/asm/asm/9.6/asm-9.6.jar'
env = os.environ.copy()
env['JAVA_HOME'] = str(java_home)
env['PATH'] = '/opt/homebrew/opt/coreutils/libexec/gnubin:' + env['PATH']
run('/opt/homebrew/bin/bash', root/'scripts-linux/build-client.sh', vanilla, lwjgl, lwjgl_util, asm, asm_tree, out/'client', env=env)
run(java_home/'bin/javac', '--release', '8', '-d', out, root/'scripts-macos/ExportVanillaAnimations.java')
run(java_home/'bin/java', '-Djava.awt.headless=true', '-cp', os.pathsep.join(map(str, (out, vanilla, lwjgl))), 'ExportVanillaAnimations', out/'animations')
run('python3', '-m', 'venv', out/'python')
run(out/'python/bin/pip', 'install', 'Pillow==12.3.0')
run(out/'python/bin/python', root/'scripts-macos/export-assets.py', vanilla, out/'animations', out/'client/mcrtx_assets')
print(f'Built client and assets under {out / "client"}; native bridge under {out / "native-build/native"}')
