#!/usr/bin/env bash

set -euo pipefail

if [[ $# -gt 3 ]]; then
  echo "usage: $0 [BUILD_DIRECTORY] [DXVK_REMIX_ROOT] [BUILD_TYPE]" >&2
  exit 2
fi

if [[ $(uname -m) != "x86_64" ]]; then
  echo "BetaRT native rendering only supports x86_64 Linux" >&2
  exit 1
fi

if ! command -v cmake >/dev/null 2>&1; then
  echo "missing required tool: cmake" >&2
  exit 1
fi

script_directory=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
source_directory=$(realpath "$script_directory/..")
build_directory=$(realpath -m "${1:-$source_directory/build-linux}")
remix_root=$(realpath -m "${2:-$source_directory/../dxvk-remix-gmod}")
build_type=${3:-Release}

if [[ ! -f "$remix_root/public/include/remix/remix_c.h" ]]; then
  echo "DXVK Remix headers were not found under $remix_root" >&2
  exit 1
fi

generator_arguments=()
if [[ ! -f "$build_directory/CMakeCache.txt" ]] && command -v ninja >/dev/null 2>&1; then
  generator_arguments=(-G Ninja)
fi

cmake -S "$source_directory" -B "$build_directory" \
  "${generator_arguments[@]}" \
  -DCMAKE_BUILD_TYPE="$build_type" \
  -DMCRTX_DXVK_REMIX_ROOT="$remix_root"
cmake --build "$build_directory" --config "$build_type" --target mcrtx_jni

echo "built $build_directory/native/libmcrtx_jni.so"
