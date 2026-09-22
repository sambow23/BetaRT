#!/usr/bin/env python3
"""Create a clean Linux x86_64 Prism instance and importable ZIP from a native package."""
import argparse
import hashlib
import json
from pathlib import Path
import re
import shutil
import subprocess
import tempfile
import zipfile


ROOT = Path(__file__).resolve().parents[1]
REQUIRED_LIBRARIES = ('libmcrtx_jni.so', 'libremix.so', 'libSDL3.so.0')
PACK_IGNORE = ('minecraft/saves', 'minecraft/stats', 'minecraft/screenshots',
               'minecraft/logs', 'minecraft/crash-reports', 'minecraft/servers.dat',
               'minecraft/servers.dat_old', 'minecraft/mcrtx_texturepack_cache',
               'minecraft/betart-vrr-session.json', 'minecraft/betart-vrr-session.lock')


def validate_elf(path):
    header = subprocess.check_output(['readelf', '-h', str(path)], text=True)
    if 'ELF64' not in header or 'Advanced Micro Devices X86-64' not in header:
        raise ValueError(f'Expected Linux x86_64 ELF: {path}')
    versions = subprocess.check_output(['readelf', '--version-info', str(path)], text=True)
    required = [tuple(map(int, v.split('.'))) for v in re.findall(r'GLIBC_(\d+\.\d+)', versions)]
    if required and max(required) > (2, 28):
        raise ValueError(f'{path.name} requires glibc {max(required)}, newer than 2.28; rebuild with build-portable-native.sh')
    dynamic = subprocess.check_output(['readelf', '-d', str(path)], text=True)
    needed = re.findall(r'\(NEEDED\).*\[(.*?)\]', dynamic)
    if any('/' in name or name in ('libstdc++.so.6', 'libgcc_s.so.1') for name in needed):
        raise ValueError(f'Unbundled C++ runtime or absolute dependency in {path.name}: {needed}')
    for entry in re.findall(r'\((?:RUNPATH|RPATH)\).*\[(.*?)\]', dynamic):
        if any(part not in ('$ORIGIN', '${ORIGIN}') for part in entry.split(':')):
            raise ValueError(f'Nonportable library search path in {path.name}: {entry}')
    if path.name.startswith('libremix.so') and '$ORIGIN' not in dynamic:
        raise ValueError(f'Renderer must locate SDL beside itself: {path.name}')


def copy_contained(source, target, boundary):
    """Materialize internal aliases so Prism re-export cannot leave broken links."""
    resolved = source.resolve(strict=True)
    if not resolved.is_relative_to(boundary.resolve()):
        raise ValueError(f'Refusing a link outside the package: {source}')
    if source.is_dir():
        target.mkdir(parents=True, exist_ok=True)
        for child in sorted(source.iterdir()):
            copy_contained(child, target / child.name, boundary)
    else:
        target.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(source, target)


def create(template, native, output, archive):
    template, native, output, archive = (p.resolve() for p in (template, native, output, archive))
    if output.exists() or archive.exists():
        raise ValueError('Instance and archive outputs must both be new paths')
    if output == archive or output in archive.parents:
        raise ValueError('The ZIP must be outside the instance')
    for name in REQUIRED_LIBRARIES:
        if not (native / name).is_file():
            raise ValueError(f'Missing native library: {name}')
    for library in sorted(native.rglob('*.so*')):
        if library.is_file():
            validate_elf(library)
    custom = json.loads((template / 'patches/customjar.json').read_text())
    if custom.get('mainJar', {}).get('name') != 'custom:customjar:1':
        raise ValueError('Expected the custom:customjar:1 client component')
    output.parent.mkdir(parents=True, exist_ok=True)
    archive.parent.mkdir(parents=True, exist_ok=True)
    with tempfile.TemporaryDirectory(prefix='.betart-portable-', dir=output.parent) as temporary:
        stage = Path(temporary) / 'instance'
        stage.mkdir()
        for name in ('mmc-pack.json', 'patches/customjar.json', 'patches/net.minecraft.json', 'patches/org.lwjgl3.json'):
            copy_contained(template / name, stage / name, template)
        copy_contained(native, stage / 'libraries', native)
        for name in ('customjar-1.jar', 'mcrtx_assets', 'lighting-notices', 'LICENSE-pbrt.txt', 'NOTICE-pbrt.txt'):
            copy_contained(template / 'libraries' / name, stage / 'libraries' / name, template / 'libraries')
        (stage / 'instance.cfg').write_text('''[General]
InstanceType=OneSix
ConfigVersion=1.3
name=BetaRT Portable Linux
iconKey=default
OverrideJavaLocation=false
OverrideJavaArgs=true
JvmArgs=-Dhttp.proxyHost=betacraft.uk
IgnoreJavaCompatibility=false
OverrideCommands=true
PreLaunchCommand=
WrapperCommand=
PostExitCommand=
OverridePerformance=true
EnableMangoHud=false
EnableFeralGamemode=false
UseDiscreteGpu=false
OverrideNativeWorkarounds=true
UseZink=false
OverrideMiscellaneous=true
JoinServerOnLaunch=false
OverrideWindow=true
LaunchMaximized=false
MinecraftWinWidth=1920
MinecraftWinHeight=1080
OverrideMemory=true
MinMemAlloc=512
MaxMemAlloc=2048
notes=Linux x86_64 portable build. See PORTABLE-README.md. Requires Java 8 and a working Vulkan driver.
''')
        game = stage / 'minecraft'
        game.mkdir()
        (game / 'mcrtx-runtime.env').write_text('''# Libraries are located beside customjar-1.jar; keep machine-specific paths out of this file.
MCRTX_GAMEPLAY_FOV=90
MCRTX_VIEWMODEL_FOV=70
MCRTX_OPAQUE_WATER_ICE=1
MCRTX_VSYNC=1
MCRTX_RENDER_BACKEND=pathtrace
MCRTX_LOAD_PBR=1
MCRTX_PT_SEPARATE_LOCAL_LIGHTING=3
MCRTX_PT_UPSCALER=auto
MCRTX_PT_DLSS_QUALITY=quality
MCRTX_PT_DLSS_RR=1
''')
        (game / 'options.txt').write_text('''music:1.0
sound:1.0
viewDistance:2
guiScale:2
fpsLimit:0
fancyGraphics:true
ao:false
skin:Default
lastServer:
''')
        (stage / '.packignore').write_text('\n'.join(PACK_IGNORE) + '\n')
        shutil.copy2(ROOT / 'docs/PortableLinux.md', stage / 'PORTABLE-README.md')
        files = {p.relative_to(stage).as_posix(): hashlib.sha256(p.read_bytes()).hexdigest()
                 for p in sorted(stage.rglob('*')) if p.is_file()}
        (stage / 'portable-manifest.json').write_text(json.dumps({
            'format': 1, 'platform': 'linux-x86_64', 'maximumRequiredGlibc': '2.28',
            'files': files,
        }, indent=2) + '\n')
        # ZIP root matches Prism's MultiMC import format. Only regular files are
        # written, including library aliases that were symlinks in the build.
        published_archive = False
        try:
            staged_archive = Path(temporary) / 'export.zip'
            with zipfile.ZipFile(staged_archive, 'w', compression=zipfile.ZIP_DEFLATED, compresslevel=6) as package:
                for path in sorted(stage.rglob('*')):
                    if path.is_file():
                        package.write(path, path.relative_to(stage).as_posix())
            with archive.open('xb') as target, staged_archive.open('rb') as source:
                published_archive = True
                shutil.copyfileobj(source, target)
            if output.exists():
                raise ValueError('Instance output was created while packaging')
            stage.rename(output)
        except BaseException:
            if published_archive:
                archive.unlink(missing_ok=True)
            raise
    return output, archive


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--template', type=Path, required=True, help='Working BetaRT Prism instance (read only)')
    parser.add_argument('--native', type=Path, required=True, help='Portable native package, including licenses')
    parser.add_argument('--output', type=Path, required=True, help='New directory under Prism instances')
    parser.add_argument('--archive', type=Path, required=True, help='New importable ZIP outside the instance')
    args = parser.parse_args()
    for path in create(args.template, args.native, args.output, args.archive):
        print(path)


if __name__ == '__main__':
    main()
