#!/usr/bin/env python3
"""Deploy an instance-local BetaRT build, backing up Prism metadata first."""
import argparse
import datetime
import json
import re
import shutil
import subprocess
import sys
from pathlib import Path

parser = argparse.ArgumentParser()
parser.add_argument('--prism', type=Path, default=Path.home() / 'Library/Application Support/PrismLauncher')
parser.add_argument('--instance', default='b1.7.3')
parser.add_argument('--client', type=Path, required=True)
parser.add_argument('--bridge', type=Path, required=True)
parser.add_argument('--assets', type=Path, required=True)
parser.add_argument('--runtime', type=Path, required=True)
args = parser.parse_args()
# Prism keeps instance commands in memory and can overwrite an on-disk update.
processes = subprocess.check_output(['ps', '-axo', 'comm='], text=True)
if any(Path(command.strip()).name.lower() == 'prismlauncher' for command in processes.splitlines()):
    parser.error('Close Prism Launcher before deploying: its cached wrapper command can load an older native bridge.')
instance = args.prism / 'instances' / args.instance
for source in (args.client, args.bridge, args.assets, args.runtime / 'lib/libremix.0.dylib', instance / 'instance.cfg'):
    if not source.exists():
        parser.error(f'Missing {source}')
stamp = datetime.datetime.now().strftime('%Y%m%d-%H%M%S')
backup = instance / 'betart-backups' / stamp
backup.mkdir(parents=True)
for name in ('instance.cfg', 'mmc-pack.json', 'patches'):
    source = instance / name
    if source.is_dir():
        shutil.copytree(source, backup / name)
    else:
        shutil.copy2(source, backup / name)
bundle = instance / f'betart-macos-{stamp}'
bundle.mkdir()
shutil.copytree(args.runtime, bundle / 'runtime', symlinks=True)
shutil.copy2(args.bridge, bundle / 'libmcrtx_jni.dylib')
shutil.copytree(args.assets, bundle / 'mcrtx_assets')
local = instance / 'libraries'
local.mkdir(exist_ok=True)
asset_link = local / 'mcrtx_assets'
if not asset_link.exists():
    asset_link.symlink_to(bundle / 'mcrtx_assets', target_is_directory=True)
client_name = f'betart-client-{stamp}.jar'
shutil.copy2(args.client, local / client_name)
libs = []
for artifact in ('lwjgl', 'lwjgl-glfw', 'lwjgl-opengl', 'lwjgl-openal'):
    for variant in (artifact, artifact + '-natives-macos-arm64'):
        jar = args.prism / 'libraries/org/lwjgl' / variant / '3.3.3' / f'{variant}-3.3.3.jar'
        if not jar.is_file():
            parser.error(f'Missing LWJGL dependency {jar}')
        filename = 'betart-' + jar.name
        shutil.copy2(jar, local / filename)
        libs.append({'name': f'betart.local:{variant}:3.3.3', 'MMC-hint': 'local', 'MMC-filename': filename})
patches = instance / 'patches'
minecraft = json.loads((patches / 'net.minecraft.json').read_text())
minecraft['mainJar'] = {'name': f'betart.local:client:{stamp}', 'MMC-hint': 'local', 'MMC-filename': client_name}
minecraft['requires'] = [{'uid': 'org.lwjgl3', 'suggests': '3.3.3'}]
minecraft['+traits'] = list(dict.fromkeys(minecraft.get('+traits', []) + ['noapplet']))
(patches / 'net.minecraft.json').write_text(json.dumps(minecraft, indent=4) + '\n')
(patches / 'org.lwjgl3.json').write_text(json.dumps({'formatVersion': 1, 'uid': 'org.lwjgl3', 'name': 'BetaRT LWJGL 3 (macOS)', 'version': '3.3.3', 'libraries': libs}, indent=4) + '\n')
pack = json.loads((instance / 'mmc-pack.json').read_text())
pack['components'] = [c for c in pack['components'] if c['uid'] not in ('org.lwjgl', 'org.lwjgl3') and not c.get('cachedName', '').startswith('legacyfix-')]
pack['components'].insert(0, {'uid': 'org.lwjgl3', 'version': '3.3.3', 'cachedName': 'BetaRT LWJGL 3 (macOS)', 'cachedVersion': '3.3.3', 'dependencyOnly': True})
for c in pack['components']:
    if c['uid'] == 'net.minecraft':
        c['cachedRequires'] = minecraft['requires']
(instance / 'mmc-pack.json').write_text(json.dumps(pack, indent=4) + '\n')
wrapper = bundle / 'launch.py'
wrapper.write_text('#!' + sys.executable + '\n' + '''import os
from pathlib import Path
import sys
root = Path(__file__).resolve().parent
os.environ.update(
    MCRTX_JNI_PATH=str(root / 'libmcrtx_jni.dylib'),
    MCRTX_REMIX_DLL=str(root / 'runtime/lib/libremix.0.dylib'),
    VK_DRIVER_FILES=str(root / 'runtime/etc/vulkan/icd.d/MoltenVK_icd.json'),
    MVK_CONFIG_ENABLE_EXPERIMENTAL_RAY_TRACING='1',
    SDL_VIDEODRIVER='cocoa',
    MCRTX_PLATFORM_BACKEND='lwjgl2',
)
# Keep the JVM's main run loop available for Cocoa/SDL dispatch.
args = [arg for arg in sys.argv[1:] if arg != '-XstartOnFirstThread']
import subprocess
import signal
import time
log_path = root / ('game-' + time.strftime('%Y%m%d-%H%M%S') + '.log')
with log_path.open('wb') as log:
    child = subprocess.Popen(args, stdout=subprocess.PIPE, stderr=subprocess.STDOUT)
    def forward(signum, frame):
        child.send_signal(signum)
    signal.signal(signal.SIGTERM, forward)
    signal.signal(signal.SIGINT, forward)
    for line in child.stdout:
        log.write(line)
        log.flush()
        sys.stdout.buffer.write(line)
        sys.stdout.buffer.flush()
    sys.exit(child.wait())
''')
wrapper.chmod(0o755)
config = (instance / 'instance.cfg').read_text()
for key, value in {'OverrideCommands': 'true', 'WrapperCommand': json.dumps('"' + str(wrapper) + '"')}.items():
    config = re.sub(r'^' + key + r'=.*$', lambda _: key + '=' + value, config, flags=re.M)
(instance / 'instance.cfg').write_text(config)
restore = backup / 'restore.py'
restore.write_text('#!' + sys.executable + '\n' + '''from pathlib import Path
import shutil
backup = Path(__file__).resolve().parent
instance = backup.parent.parent
for name in ('instance.cfg', 'mmc-pack.json'):
    shutil.copy2(backup / name, instance / name)
for name in ('net.minecraft.json', 'org.lwjgl3.json'):
    original = backup / 'patches' / name
    target = instance / 'patches' / name
    if original.exists():
        shutil.copy2(original, target)
    elif target.exists():
        target.unlink()
print('Restored original Prism instance metadata; worlds and assets were untouched.')
''')
restore.chmod(0o755)
print(json.dumps({'instance': str(instance), 'bundle': str(bundle), 'backup': str(backup), 'restore': str(restore)}, indent=2))
