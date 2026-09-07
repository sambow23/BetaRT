#!/usr/bin/env python3
"""Export vanilla textures and captured vanilla animation frames as Remix DDS assets."""
import argparse
import io
from pathlib import Path
import zipfile
from PIL import Image

parser = argparse.ArgumentParser()
parser.add_argument('minecraft_jar', type=Path)
parser.add_argument('animations', type=Path)
parser.add_argument('output', type=Path)
args = parser.parse_args()
args.output.mkdir(parents=True, exist_ok=True)

def save(image, name):
    target = args.output / name
    target.parent.mkdir(parents=True, exist_ok=True)
    image.convert('RGBA').save(target.with_suffix('.png'))
    image.convert('RGBA').save(target.with_suffix('.dds'))

with zipfile.ZipFile(args.minecraft_jar) as jar:
    for name in jar.namelist():
        if not name.endswith('.png'):
            continue
        dest = name
        if name.startswith(('mob/', 'armor/')):
            dest = 'entities/' + name
        elif name.startswith(('terrain/', 'environment/')):
            dest = Path(name).name
        save(Image.open(io.BytesIO(jar.read(name))), dest)

terrain = Image.open(args.output / 'terrain.png').convert('RGBA')
animations = {int(p.stem): Image.open(p).convert('RGBA') for p in args.animations.glob('*.png')}
for tile, frames in animations.items():
    terrain.paste(frames.crop((0, 0, 16, 16)), (tile % 16 * 16, tile // 16 * 16))
save(terrain, 'terrain')
for name, tiles in [('water', (205, 206)), ('lava', (237, 238))]:
    atlas = Image.new('RGBA', (1024, 16))
    for frame in range(32):
        for variant, tile in enumerate(tiles):
            atlas.paste(animations[tile].crop((frame*16, 0, frame*16+16, 16)), (frame*32+variant*16, 0))
    save(atlas, name)
    if name == 'lava':
        save(atlas, 'lava_emissive')
save(Image.new('RGBA', (1024, 16), (128, 128, 255, 255)), 'water_normal')
save(animations[14], 'portal')
fire = Image.new('RGBA', (256, 32))
for frame in range(16):
    for variant, tile in enumerate((31, 47)):
        fire.paste(animations[tile].crop((frame*16, 0, frame*16+16, 16)), (frame*16, variant*16))
save(fire, 'fire')
redstone = Image.new('RGBA', terrain.size, (0, 0, 0, 255))
for tile in (164, 165):
    for y in range(tile//16*16, tile//16*16+16):
        for x in range(tile%16*16, tile%16*16+16):
            r, g, b, a = terrain.getpixel((x, y))
            if a:
                redstone.putpixel((x, y), (min(255, round(r*2.4)), min(255, round(g*1.4)), min(255, round(b*1.2)), 255))
save(redstone, 'redstone_emissive')
print(f'Exported {len(list(args.output.rglob("*.dds")))} DDS assets to {args.output}')
