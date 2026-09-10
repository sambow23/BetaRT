#!/usr/bin/env python3
"""Analyze archived emissive-quality captures (requires NumPy and Pillow).

This comparison predates emissive illumination removal and is not a quality gate
for the current sun/sky-only renderer.

CAPTURES contains quality-<scene>-<sampler>-<rate>-<indirect>/ directories,
each with 32 PNGs. Additional runs may use quality-extra-...-c<budget>.
NATIVE contains primary-quality-{4,8,16}.log and optional sampling-quality.log.
Ratios always use the corrected full-rate RTXDI/eight-candidate baseline.
Moving-camera variance includes scene motion and is not a pure noise metric.
"""
import argparse
import json
from pathlib import Path
import re

import numpy as np
from PIL import Image

REGIONS = {
    "opaque_wall": (560, 80, 810, 650),
    "stone_slab": (980, 640, 1260, 720),
    "glass_floor": (120, 780, 430, 915),
    "water": (1080, 880, 1310, 940),
}


def measure(directory):
    files = sorted(directory.glob("quality-*.png"))
    if len(files) != 32:
        raise ValueError(f"Expected 32 captures in {directory}, found {len(files)}")
    sums, squares = {}, {}
    for path in files:
        with Image.open(path) as image:
            if image.size != (1920, 1080):
                raise ValueError(f"Expected native 1080p capture: {path}")
            for name, bounds in REGIONS.items():
                value = np.asarray(image.crop(bounds).convert("RGB"), dtype=np.float64) / 255
                value = np.where(value <= .04045, value / 12.92, ((value + .055) / 1.055) ** 2.4)
                if name not in sums:
                    sums[name], squares[name] = np.zeros_like(value), np.zeros_like(value)
                sums[name] += value
                squares[name] += value * value
    return {name: {
        "variance": float(np.maximum(squares[name] / 32 - (sums[name] / 32) ** 2, 0).mean()),
        "mean": float((sums[name] / 32).mean()),
    } for name in REGIONS}


def native_log(path):
    text = path.read_text()
    result = {}
    for sampler, rate, indirect, error, variance in re.findall(
            r"Sampling quality (\d)/(\d)/(\d): MSE ([\d.]+) variance ([\d.]+)", text):
        name = (f'{["power", "rtxdi", "lightbvh"][int(sampler)]}-'
                f'{[1, 4][int(rate)]}-{["full", "checkerboard"][int(indirect)]}')
        result[name] = dict(mse=float(error), variance=float(variance))
    if len(result) != 12:
        raise ValueError(f"Expected twelve native sampling combinations in {path}")
    return result


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("captures", type=Path)
    parser.add_argument("native", type=Path)
    parser.add_argument("output", type=Path)
    args = parser.parse_args()
    scenes = {}
    pattern = r"quality-(?:extra-)?(localhost|daylight|moving)-(power|rtxdi|lightbvh)-(1|4)-(full|checkerboard)(?:-c(4|8|16))?"
    for directory in sorted(args.captures.iterdir()):
        match = re.fullmatch(pattern, directory.name)
        if not match or not directory.is_dir():
            continue
        scene, sampler, rate, indirect, budget = match.groups()
        key = f"{sampler}-{rate}-{indirect}-c{budget or '8'}"
        scenes.setdefault(scene, {})[key] = {"regions": measure(directory)}
    for scene, modes in scenes.items():
        baseline = modes["rtxdi-1-full-c8"]["regions"]
        for mode in modes.values():
            for name, value in mode["regions"].items():
                value["variance_ratio"] = value["variance"] / max(baseline[name]["variance"], 1e-20)
            mode["passes_static_variance_gate"] = (None if scene == "moving" else
                all(value["variance_ratio"] <= 1.1 for value in mode["regions"].values()))
    native = {}
    for budget in (4, 8, 16):
        path = args.native / f"primary-quality-{budget}.log"
        if path.exists():
            for name, value in native_log(path).items():
                native[f"{name}-c{budget}"] = value
    if native:
        baseline = native["rtxdi-1-full-c8"]
        for value in native.values():
            value.update(error_ratio=value["mse"] / baseline["mse"],
                         variance_ratio=value["variance"] / baseline["variance"])
            value["passes_quality_gate"] = value["error_ratio"] <= 1.1 and value["variance_ratio"] <= 1.1
    indirect = args.native / "sampling-quality.log"
    output = dict(regions=REGIONS, scenes=scenes, native_primary=native,
                  native_indirect=native_log(indirect) if indirect.exists() else {},
                  method="32-frame linear RGB variance in fixed image rectangles; moving variance includes motion. "
                         "Native tests measure linear red-channel MSE against >=4096 progressive samples "
                         "with identical sources and bounces. Ratios use corrected RTXDI/full/full/eight-candidate output.")
    args.output.write_text(json.dumps(output, indent=2) + "\n")
    print(args.output)


if __name__ == "__main__":
    main()
