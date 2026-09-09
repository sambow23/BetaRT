#!/usr/bin/env python3
"""Stage paired binaries only in the isolated BetaRT-Raster-Profile instance."""
import argparse
import hashlib
import json
import os
from pathlib import Path
import shutil

FILES = ('customjar-1.jar', 'libmcrtx_jni.so', 'libremix.so.0.1003.0', 'libSDL3.so.0.4.14')

def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()

def run(action, instance, payload, backup):
    instance, payload, backup = (p.resolve() for p in (instance, payload, backup))
    if instance.name != 'BetaRT-Raster-Profile':
        raise ValueError('This tool only accepts the isolated BetaRT-Raster-Profile instance')
    destination = instance / 'libraries'
    if not destination.is_dir():
        raise ValueError('Missing instance-local libraries')
    temporaries = [destination / (name + suffix) for name in FILES
                   for suffix in ('.pathtrace-tmp', '.pathtrace-old')]
    if any(path.exists() or path.is_symlink() for path in temporaries):
        raise ValueError('Unexpected staging temporary file')
    for process in Path('/proc').glob('[0-9]*'):
        try:
            if (process / 'comm').read_text().strip() == 'java' and (process / 'cwd').resolve() == instance / 'minecraft':
                raise ValueError('Close the profiling instance before staging or rollback')
        except (PermissionError, FileNotFoundError, ProcessLookupError):
            pass
    manifest = backup / 'manifest.json'
    if action == 'stage':
        if backup.exists():
            raise ValueError('Use a fresh backup directory')
        changes = {}
        for name in FILES:
            source, target = payload / name, destination / name
            if not source.is_file() or not target.is_file() or target.is_symlink():
                raise ValueError('Expected regular paired artifact: ' + name)
            changes[name] = {'before': digest(target), 'after': digest(source)}
        backup.mkdir(parents=True)
        for name in FILES:
            shutil.copy2(destination / name, backup / name)
        manifest.write_text(json.dumps({'instance': str(instance), 'files': changes}, indent=2) + '\n')
        sources = payload
    else:
        state = json.loads(manifest.read_text())
        if state['instance'] != str(instance):
            raise ValueError('Backup belongs to a different instance')
        if set(state['files']) != set(FILES):
            raise ValueError('Backup has an unexpected artifact set')
        for name, hashes in state['files'].items():
            if (destination / name).is_symlink() or (backup / name).is_symlink():
                raise ValueError('Expected regular paired artifact: ' + name)
            if digest(destination / name) != hashes['after'] or digest(backup / name) != hashes['before']:
                raise ValueError('Changed artifact: ' + name)
        sources = backup
    replaced = []
    try:
        # Prepare and verify every artifact before publishing any of the pair.
        for name in FILES:
            temporary = destination / (name + '.pathtrace-tmp')
            shutil.copy2(destination / name, destination / (name + '.pathtrace-old'))
            shutil.copy2(sources / name, temporary)
            if digest(temporary) != digest(sources / name):
                raise ValueError('Copy verification failed: ' + name)
        for name in FILES:
            os.replace(destination / (name + '.pathtrace-tmp'), destination / name)
            replaced.append(name)
    except Exception:
        for name in reversed(replaced):
            os.replace(destination / (name + '.pathtrace-old'), destination / name)
        raise
    finally:
        for temporary in temporaries:
            if temporary.exists():
                temporary.unlink()
    print(action + ': paired artifacts verified; manifest ' + str(manifest))

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('action', choices=['stage', 'rollback'])
    parser.add_argument('--instance', type=Path, required=True)
    parser.add_argument('--payload', type=Path, required=True)
    parser.add_argument('--backup', type=Path, required=True)
    args = parser.parse_args()
    run(args.action, args.instance, args.payload, args.backup)
