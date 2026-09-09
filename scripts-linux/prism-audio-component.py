#!/usr/bin/env python3
"""Create a Prism LWJGL component containing core/OpenAL and their natives only."""
import argparse
import json
from pathlib import Path

parser = argparse.ArgumentParser(description=__doc__)
parser.add_argument('metadata', type=Path, help='Prism meta/org.lwjgl3/VERSION.json')
parser.add_argument('output', type=Path, help='New instance patches/org.lwjgl3.json')
args = parser.parse_args()
component = json.loads(args.metadata.read_text())
if component.get('uid') != 'org.lwjgl3':
    parser.error('Expected the org.lwjgl3 component.')
component['libraries'] = [library for library in component['libraries']
                          if library['name'].split(':')[1] in ('lwjgl', 'lwjgl-openal')
                          or library['name'].split(':')[1].startswith(('lwjgl-natives-', 'lwjgl-openal-natives-'))]
if not {'lwjgl', 'lwjgl-openal'}.issubset({library['name'].split(':')[1] for library in component['libraries']}):
    parser.error('The metadata must supply LWJGL core and OpenAL.')
for module in ('lwjgl', 'lwjgl-openal'):
    if not any(library['name'].split(':')[1] == module + '-natives-linux'
               or (library['name'].split(':')[1] == module
                   and library['name'].split(':')[3:] == ['natives-linux'])
               for library in component['libraries']):
        parser.error('The metadata must supply Linux x64 natives for ' + module + '.')
component['name'] = 'LWJGL core and OpenAL (no graphics)'
component.pop('volatile', None)
with args.output.open('x') as output:
    output.write(json.dumps(component, indent=2) + '\n')
print(args.output)
