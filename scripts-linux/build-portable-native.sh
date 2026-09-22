#!/usr/bin/env bash
set -euo pipefail

if [[ $# -lt 2 || $# -gt 3 || -z ${JAVA_HOME:-} ]]; then
  echo "usage: JAVA_HOME=/path/to/jdk $0 OPENREMIX_ROOT NEW_OUTPUT_DIRECTORY [JOBS]" >&2
  exit 2
fi
betart_root=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)
remix_root=$(realpath "$1")
output=$(realpath -m "$2")
if [[ -e "$output" ]]; then
  echo "Output must not exist: $output" >&2
  exit 2
fi
image=${BETART_PORTABLE_BUILD_IMAGE:-betart-portable-toolchain:glibc228}
if [[ -z ${BETART_PORTABLE_BUILD_IMAGE:-} ]]; then
  docker build -t "$image" "$betart_root/scripts-linux/portable"
fi
mkdir -p "$output"
docker run --rm --user "$(id -u):$(id -g)" \
  -v "$betart_root:$betart_root" -v "$remix_root:$remix_root" \
  -v "$output:$output" -v "$(realpath "$JAVA_HOME"):/opt/betart-jdk:ro" \
  -e PATH='/opt/python/cp312-cp312/bin:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin' \
  "$image" bash "$betart_root/scripts-linux/portable/build-in-container.sh" \
  "$remix_root" "$betart_root" "$output" "${3:-8}"
