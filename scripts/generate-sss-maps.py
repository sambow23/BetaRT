"""Generate RTX Remix SSS helper maps from a source texture.

This script writes three sibling textures using the filenames that mc-rtx
already resolves automatically:

- <stem>_transmittance.png
- <stem>_single_scattering_albedo.png
- <stem>_thickness.png

It is meant for quick iteration on foliage and other thin materials where a
coverage mask or the source alpha can be turned into a usable first-pass SSS
set without painting each map by hand.

Examples:
    python scripts/generate-sss-maps.py assets/terrain.png
    python scripts/generate-sss-maps.py leaf.png --mask leaf_mask.png
    python scripts/generate-sss-maps.py grass.png --albedo-tint 120,190,90
"""

from __future__ import annotations

import argparse
import sys
from pathlib import Path

try:
    from PIL import Image, ImageChops, ImageOps
except ImportError:
    sys.exit("Pillow is required: pip install pillow")


def clamp_u8(value: float) -> int:
    return max(0, min(255, int(round(value))))


def parse_color(value: str) -> tuple[int, int, int]:
    text = value.strip()
    if text.startswith("#"):
        text = text[1:]
    if len(text) == 6 and all(ch in "0123456789abcdefABCDEF" for ch in text):
        return tuple(int(text[index:index + 2], 16) for index in (0, 2, 4))

    parts = [part.strip() for part in value.split(",")]
    if len(parts) != 3:
        raise argparse.ArgumentTypeError("expected R,G,B or #RRGGBB")

    try:
        channels = tuple(int(part) for part in parts)
    except ValueError as exc:
        raise argparse.ArgumentTypeError("expected integer color channels") from exc

    if any(channel < 0 or channel > 255 for channel in channels):
        raise argparse.ArgumentTypeError("color channels must be in the range 0-255")

    return channels


def parse_unit_interval(value: str) -> float:
    try:
        number = float(value)
    except ValueError as exc:
        raise argparse.ArgumentTypeError("expected a floating point value") from exc

    if number < 0.0 or number > 1.0:
        raise argparse.ArgumentTypeError("expected a value in the range 0.0-1.0")
    return number


def rgb_max_channel(image: Image.Image) -> Image.Image:
    red, green, blue = image.convert("RGB").split()
    return ImageChops.lighter(ImageChops.lighter(red, green), blue)


def extract_mask(image: Image.Image, mask_source: str, threshold: float, invert: bool) -> Image.Image:
    rgba = image.convert("RGBA")
    alpha = rgba.getchannel("A")

    if mask_source == "auto":
        mask = alpha if alpha.getextrema()[0] < 255 else rgb_max_channel(rgba)
    elif mask_source == "alpha":
        mask = alpha
    elif mask_source == "luminance":
        mask = rgba.convert("RGB").convert("L")
    else:
        mask = rgb_max_channel(rgba)

    if invert:
        mask = ImageOps.invert(mask)

    cutoff = clamp_u8(threshold * 255.0)
    if cutoff > 0:
        mask = mask.point(lambda value: 0 if value < cutoff else value)

    return mask


def build_transmittance(mask: Image.Image, gain: float, gamma: float) -> Image.Image:
    def scale(value: int) -> int:
        normalized = (value / 255.0) ** gamma
        return clamp_u8(normalized * gain * 255.0)

    grayscale = mask.point(scale)
    return Image.merge("RGB", (grayscale, grayscale, grayscale))


def build_single_scattering_albedo(
    source: Image.Image,
    mask: Image.Image,
    gain: float,
    tint: tuple[int, int, int] | None,
) -> Image.Image:
    source_rgb = source.convert("RGB")
    output = Image.new("RGB", source_rgb.size)

    source_pixels = source_rgb.load()
    mask_pixels = mask.load()
    output_pixels = output.load()

    for y in range(source_rgb.height):
        for x in range(source_rgb.width):
            mask_factor = mask_pixels[x, y] / 255.0
            if mask_factor <= 0.0:
                output_pixels[x, y] = (0, 0, 0)
                continue

            base_color = tint or source_pixels[x, y]
            output_pixels[x, y] = tuple(
                clamp_u8(channel * mask_factor * gain) for channel in base_color
            )

    return output


def build_thickness(source: Image.Image, mask: Image.Image, minimum: float, maximum: float) -> Image.Image:
    source_luminance = source.convert("RGB").convert("L")
    output = Image.new("L", source_luminance.size)

    mask_pixels = mask.load()
    luminance_pixels = source_luminance.load()
    output_pixels = output.load()

    for y in range(source_luminance.height):
        for x in range(source_luminance.width):
            mask_factor = mask_pixels[x, y] / 255.0
            if mask_factor <= 0.0:
                output_pixels[x, y] = 0
                continue

            darkness = 1.0 - (luminance_pixels[x, y] / 255.0)
            thickness = minimum + darkness * (maximum - minimum)
            output_pixels[x, y] = clamp_u8(mask_factor * thickness * 255.0)

    return Image.merge("RGB", (output, output, output))


def write_image(path: Path, image: Image.Image) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    image.save(path)


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("source", type=Path, help="source diffuse or authored mask texture")
    parser.add_argument("--mask", type=Path, help="optional separate coverage mask texture")
    parser.add_argument("--out-dir", type=Path, help="directory for generated outputs")
    parser.add_argument("--stem", help="override the output filename stem")
    parser.add_argument(
        "--mask-source",
        choices=("auto", "alpha", "luminance", "maxrgb"),
        default="auto",
        help="how to derive the mask when --mask is not provided",
    )
    parser.add_argument(
        "--invert-mask",
        action="store_true",
        help="invert the derived or supplied mask before generating outputs",
    )
    parser.add_argument(
        "--mask-threshold",
        type=parse_unit_interval,
        default=0.02,
        help="clip very dark mask values to zero (0.0-1.0)",
    )
    parser.add_argument(
        "--transmittance-gain",
        type=float,
        default=1.0,
        help="multiplier applied to the generated transmittance map",
    )
    parser.add_argument(
        "--transmittance-gamma",
        type=float,
        default=1.0,
        help="gamma applied to the coverage mask before transmittance gain",
    )
    parser.add_argument(
        "--scatter-gain",
        type=float,
        default=1.0,
        help="multiplier applied to the generated single-scattering albedo",
    )
    parser.add_argument(
        "--albedo-tint",
        type=parse_color,
        help="override the scattering albedo color using R,G,B or #RRGGBB",
    )
    parser.add_argument(
        "--thickness-min",
        type=parse_unit_interval,
        default=0.08,
        help="minimum generated thickness for filled pixels",
    )
    parser.add_argument(
        "--thickness-max",
        type=parse_unit_interval,
        default=0.18,
        help="maximum generated thickness for the darkest filled pixels",
    )
    return parser.parse_args()


def main() -> int:
    args = parse_args()

    if args.thickness_min > args.thickness_max:
        sys.exit("--thickness-min must be less than or equal to --thickness-max")
    if args.transmittance_gamma <= 0.0:
        sys.exit("--transmittance-gamma must be greater than zero")
    if args.transmittance_gain < 0.0:
        sys.exit("--transmittance-gain must be non-negative")
    if args.scatter_gain < 0.0:
        sys.exit("--scatter-gain must be non-negative")

    source_path = args.source.resolve()
    if not source_path.is_file():
        sys.exit(f"Source texture not found: {source_path}")

    source_image = Image.open(source_path)

    mask_image = source_image
    if args.mask:
        mask_path = args.mask.resolve()
        if not mask_path.is_file():
            sys.exit(f"Mask texture not found: {mask_path}")
        mask_image = Image.open(mask_path)

    mask = extract_mask(mask_image, args.mask_source, args.mask_threshold, args.invert_mask)

    out_dir = args.out_dir.resolve() if args.out_dir else source_path.parent
    stem = args.stem or source_path.stem

    outputs = {
        "transmittance": out_dir / f"{stem}_transmittance.png",
        "single_scattering_albedo": out_dir / f"{stem}_single_scattering_albedo.png",
        "thickness": out_dir / f"{stem}_thickness.png",
    }

    write_image(outputs["transmittance"], build_transmittance(mask, args.transmittance_gain, args.transmittance_gamma))
    write_image(
        outputs["single_scattering_albedo"],
        build_single_scattering_albedo(source_image, mask, args.scatter_gain, args.albedo_tint),
    )
    write_image(outputs["thickness"], build_thickness(source_image, mask, args.thickness_min, args.thickness_max))

    print(f"Source: {source_path}")
    if args.mask:
        print(f"Mask:   {args.mask.resolve()}")
    for label, path in outputs.items():
        print(f"Wrote {label:>25}: {path}")

    return 0


if __name__ == "__main__":
    raise SystemExit(main())

try:
    from PIL import Image, ImageChops, ImageOps
except ImportError:
    sys.exit("Pillow is required: pip install pillow")


def clamp_u8(value: float) -> int:
    return max(0, min(255, int(round(value))))


def parse_color(value: str) -> tuple[int, int, int]:
    text = value.strip()
    if text.startswith("#"):
        text = text[1:]
    if len(text) == 6 and all(ch in "0123456789abcdefABCDEF" for ch in text):
        return tuple(int(text[index:index + 2], 16) for index in (0, 2, 4))

    parts = [part.strip() for part in value.split(",")]
    if len(parts) != 3:
        raise argparse.ArgumentTypeError("expected R,G,B or #RRGGBB")

    try:
        channels = tuple(int(part) for part in parts)
    except ValueError as exc:
        raise argparse.ArgumentTypeError("expected integer color channels") from exc

    if any(channel < 0 or channel > 255 for channel in channels):
        raise argparse.ArgumentTypeError("color channels must be in the range 0-255")

    return channels


def parse_unit_interval(value: str) -> float:
    try:
        number = float(value)
    except ValueError as exc:
        raise argparse.ArgumentTypeError("expected a floating point value") from exc

    if number < 0.0 or number > 1.0:
        raise argparse.ArgumentTypeError("expected a value in the range 0.0-1.0")
    return number


def rgb_max_channel(image: Image.Image) -> Image.Image:
    red, green, blue = image.convert("RGB").split()
    return ImageChops.lighter(ImageChops.lighter(red, green), blue)


def extract_mask(image: Image.Image, mask_source: str, threshold: float, invert: bool) -> Image.Image:
    rgba = image.convert("RGBA")
    alpha = rgba.getchannel("A")

    if mask_source == "auto":
        mask = alpha if alpha.getextrema()[0] < 255 else rgb_max_channel(rgba)
    elif mask_source == "alpha":
        mask = alpha
    elif mask_source == "luminance":
        mask = rgba.convert("RGB").convert("L")
    else:
        mask = rgb_max_channel(rgba)

    if invert:
        mask = ImageOps.invert(mask)

    cutoff = clamp_u8(threshold * 255.0)
    if cutoff > 0:
        mask = mask.point(lambda value: 0 if value < cutoff else value)

    return mask


def build_transmittance(mask: Image.Image, gain: float, gamma: float) -> Image.Image:
    def scale(value: int) -> int:
        normalized = (value / 255.0) ** gamma
        return clamp_u8(normalized * gain * 255.0)

    grayscale = mask.point(scale)
    return Image.merge("RGB", (grayscale, grayscale, grayscale))


def build_single_scattering_albedo(
    source: Image.Image,
    mask: Image.Image,
    gain: float,
    tint: tuple[int, int, int] | None,
) -> Image.Image:
    source_rgb = source.convert("RGB")
    output = Image.new("RGB", source_rgb.size)

    source_pixels = source_rgb.load()
    mask_pixels = mask.load()
    output_pixels = output.load()

    for y in range(source_rgb.height):
        for x in range(source_rgb.width):
            mask_factor = mask_pixels[x, y] / 255.0
            if mask_factor <= 0.0:
                output_pixels[x, y] = (0, 0, 0)
                continue

            base_color = tint or source_pixels[x, y]
            output_pixels[x, y] = tuple(
                clamp_u8(channel * mask_factor * gain) for channel in base_color
            )

    return output


def build_thickness(source: Image.Image, mask: Image.Image, minimum: float, maximum: float) -> Image.Image:
    source_luminance = source.convert("RGB").convert("L")
    output = Image.new("L", source_luminance.size)

    mask_pixels = mask.load()
    luminance_pixels = source_luminance.load()
    output_pixels = output.load()

    for y in range(source_luminance.height):
        for x in range(source_luminance.width):
            mask_factor = mask_pixels[x, y] / 255.0
            if mask_factor <= 0.0:
                output_pixels[x, y] = 0
                continue

            darkness = 1.0 - (luminance_pixels[x, y] / 255.0)
            thickness = minimum + darkness * (maximum - minimum)
            output_pixels[x, y] = clamp_u8(mask_factor * thickness * 255.0)

    return Image.merge("RGB", (output, output, output))


def write_image(path: Path, image: Image.Image) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    image.save(path)


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("source", type=Path, help="source diffuse or authored mask texture")
    parser.add_argument("--mask", type=Path, help="optional separate coverage mask texture")
    parser.add_argument("--out-dir", type=Path, help="directory for generated outputs")
    parser.add_argument("--stem", help="override the output filename stem")
    parser.add_argument(
        "--mask-source",
        choices=("auto", "alpha", "luminance", "maxrgb"),
        default="auto",
        help="how to derive the mask when --mask is not provided",
    )
    parser.add_argument(
        "--invert-mask",
        action="store_true",
        help="invert the derived or supplied mask before generating outputs",
    )
    parser.add_argument(
        "--mask-threshold",
        type=parse_unit_interval,
        default=0.02,
        help="clip very dark mask values to zero (0.0-1.0)",
    )
    parser.add_argument(
        "--transmittance-gain",
        type=float,
        default=1.0,
        help="multiplier applied to the generated transmittance map",
    )
    parser.add_argument(
        "--transmittance-gamma",
        type=float,
        default=1.0,
        help="gamma applied to the coverage mask before transmittance gain",
    )
    parser.add_argument(
        "--scatter-gain",
        type=float,
        default=1.0,
        help="multiplier applied to the generated single-scattering albedo",
    )
    parser.add_argument(
        "--albedo-tint",
        type=parse_color,
        help="override the scattering albedo color using R,G,B or #RRGGBB",
    )
    parser.add_argument(
        "--thickness-min",
        type=parse_unit_interval,
        default=0.08,
        help="minimum generated thickness for filled pixels",
    )
    parser.add_argument(
        "--thickness-max",
        type=parse_unit_interval,
        default=0.18,
        help="maximum generated thickness for the darkest filled pixels",
    )
    return parser.parse_args()


def main() -> int:
    args = parse_args()

    if args.thickness_min > args.thickness_max:
        sys.exit("--thickness-min must be less than or equal to --thickness-max")
    if args.transmittance_gamma <= 0.0:
        sys.exit("--transmittance-gamma must be greater than zero")
    if args.transmittance_gain < 0.0:
        sys.exit("--transmittance-gain must be non-negative")
    if args.scatter_gain < 0.0:
        sys.exit("--scatter-gain must be non-negative")

    source_path = args.source.resolve()
    if not source_path.is_file():
        sys.exit(f"Source texture not found: {source_path}")

    source_image = Image.open(source_path)

    mask_image = source_image
    if args.mask:
        mask_path = args.mask.resolve()
        if not mask_path.is_file():
            sys.exit(f"Mask texture not found: {mask_path}")
        mask_image = Image.open(mask_path)

    mask = extract_mask(mask_image, args.mask_source, args.mask_threshold, args.invert_mask)

    out_dir = args.out_dir.resolve() if args.out_dir else source_path.parent
    stem = args.stem or source_path.stem

    outputs = {
        "transmittance": out_dir / f"{stem}_transmittance.png",
        "single_scattering_albedo": out_dir / f"{stem}_single_scattering_albedo.png",
        "thickness": out_dir / f"{stem}_thickness.png",
    }

    write_image(outputs["transmittance"], build_transmittance(mask, args.transmittance_gain, args.transmittance_gamma))
    write_image(
        outputs["single_scattering_albedo"],
        build_single_scattering_albedo(source_image, mask, args.scatter_gain, args.albedo_tint),
    )
    write_image(outputs["thickness"], build_thickness(source_image, mask, args.thickness_min, args.thickness_max))

    print(f"Source: {source_path}")
    if args.mask:
        print(f"Mask:   {Path(args.mask).resolve()}")
    for label, path in outputs.items():
        print(f"Wrote {label:>25}: {path}")

    return 0


if __name__ == "__main__":
    raise SystemExit(main())