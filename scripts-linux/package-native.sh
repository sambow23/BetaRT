#!/usr/bin/env bash

set -euo pipefail

if [[ $# -ne 3 ]]; then
  echo "usage: $0 BUILD_DIRECTORY REMIX_PACKAGE_DIRECTORY OUTPUT_DIRECTORY" >&2
  exit 2
fi

build_directory=$(realpath "$1")
remix_package_directory=$(realpath "$2")
output_directory=$(realpath -m "$3")

if [[ "$output_directory" == "/" ]]; then
  echo "refusing to use / as the package output directory" >&2
  exit 2
fi
if [[ "$output_directory" == "$build_directory" || "$output_directory" == "$remix_package_directory" ]]; then
  echo "the output directory must differ from both input directories" >&2
  exit 2
fi
if [[ ! -f "$build_directory/native/libmcrtx_jni.so" ]]; then
  echo "native bridge not found: $build_directory/native/libmcrtx_jni.so" >&2
  exit 1
fi
if [[ ! -e "$remix_package_directory/lib/libremix.so.0" ]]; then
  echo "native Remix runtime not found under $remix_package_directory/lib" >&2
  exit 1
fi
if [[ ! -e "$remix_package_directory/lib/libSDL3.so.0" ]]; then
  echo "packaged SDL3 runtime not found under $remix_package_directory/lib" >&2
  exit 1
fi

rm -rf "$output_directory"
mkdir -p "$output_directory"
cmake --install "$build_directory" --prefix "$output_directory" >/dev/null
cp -a "$remix_package_directory/lib"/*.so* "$output_directory/"

if [[ -d "$remix_package_directory/share/remix" ]]; then
  mkdir -p "$output_directory/share"
  cp -a "$remix_package_directory/share/remix" "$output_directory/share/"
fi

echo "packaged BetaRT native runtime in $output_directory"
