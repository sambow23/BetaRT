#!/usr/bin/env bash
set -euo pipefail

remix_root=$1
betart_root=$2
output=$3
jobs=${4:-8}
export CC=clang CXX=clang++
export CFLAGS='-march=x86-64 -mtune=generic'
export CXXFLAGS='-march=x86-64 -mtune=generic'
export JAVA_HOME=/opt/betart-jdk

deps="$remix_root/_work/deps/portable-linux"
renderer_build="$remix_root/_work/build/portable-linux"
bridge_build="$betart_root/_work/build/portable-linux"
renderer_package="$output/remix"
native_package="$output/native"
mkdir -p "$output"
python "$remix_root/scripts/common/build-dependencies.py" --output "$deps" --jobs "$jobs"
if [[ ! -f "$renderer_build/build.ninja" ]]; then
  meson setup "$renderer_build" "$remix_root" -Dbuildtype=release -Dwerror=true \
    -Dgpu_tests=true -Ddeps_root="$deps/prefix" \
    '-Dcpp_link_args=["-static-libstdc++", "-static-libgcc"]'
fi
meson compile -C "$renderer_build" -j "$jobs"
cmake -S "$betart_root" -B "$bridge_build" -G Ninja -DCMAKE_BUILD_TYPE=Release \
  -DMCRTX_DXVK_REMIX_ROOT="$remix_root" -DMCRTX_BUILD_TESTS=ON \
  '-DCMAKE_SHARED_LINKER_FLAGS=-static-libstdc++ -static-libgcc -Wl,--exclude-libs,ALL' \
  '-DCMAKE_EXE_LINKER_FLAGS=-static-libstdc++ -static-libgcc'
cmake --build "$bridge_build" --parallel "$jobs"
ctest --test-dir "$bridge_build" --output-on-failure
python "$remix_root/scripts/common/package-remix.py" --build "$renderer_build" \
  --deps "$deps" --output "$renderer_package" --max-glibc 2.28
bash "$betart_root/scripts-linux/package-native.sh" "$bridge_build" "$renderer_package" "$native_package"
mkdir -p "$native_package/share/gcc-runtime"
cp /usr/share/licenses/gcc/COPYING* "$native_package/share/gcc-runtime/"

# Check every packaged ELF, including the JNI bridge and optional NGX releases.
while IFS= read -r -d '' library; do
  bash "$remix_root/scripts/linux/check-glibc.sh" "$library" 2.28
  if readelf -d "$library" | grep -E '\(NEEDED\).*\[(libstdc\+\+|libgcc_s)'; then
    echo "Unexpected dynamic C++ runtime dependency: $library" >&2
    exit 1
  fi
done < <(find "$native_package" -type f -name '*.so*' -print0)
python - "$native_package" <<'PY'
import ctypes
from pathlib import Path
import sys
root = Path(sys.argv[1])
for name in ('libmcrtx_jni.so', 'libremix.so'):
    ctypes.CDLL(str(root / name))
    print('Loaded on glibc 2.28:', name)
PY
