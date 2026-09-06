#!/usr/bin/env bash
set -euo pipefail

if [[ $# != 4 ]]; then
  echo "usage: $0 PATCHED_CLIENT_JAR LWJGL_JAR JAVA8_EXECUTABLE OUTPUT_DIRECTORY" >&2
  exit 2
fi
test_client=$(realpath "$1")
test_lwjgl=$(realpath "$2")
test_java=$(realpath "$3")
test_output=$(realpath -m "$4")
test_javac=${JAVA_HOME:+$JAVA_HOME/bin/}javac
test_root=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)
if [[ -e "$test_output" ]]; then
  echo "Output directory must not already exist: $test_output" >&2
  exit 2
fi
mkdir -p "$test_output/client" "$test_output/queue"
cd "$test_root"
mapfile -t test_sources < <(rg --files src/tests/java -g '*.java' | rg -v '/chunks/' | sort)
"$test_javac" --release 8 -cp "$test_client:$test_lwjgl" -d "$test_output/client" "${test_sources[@]}"
mapfile -t test_queue_sources < <(rg --files src/tests/java/chunks -g '*.java' | sort)
"$test_javac" --release 8 -d "$test_output/queue" "${test_queue_sources[@]}" \
  src/java-src/chunks/DirtyChunkSection.java src/java-src/chunks/RemixChunkSectionKey.java \
  src/java-src/chunks/RemixChunkWorldState.java src/java-src/chunks/RemixChunkRecaptureQueue.java
test_failed=0
for test_source in "${test_sources[@]}"; do
  test_name=$(basename "$test_source" .java)
  if "$test_java" -cp "$test_output/client:$test_client:$test_lwjgl" "$test_name"; then
    echo "PASS $test_name"
  else
    echo "FAIL $test_name" >&2
    test_failed=1
  fi
done
if ! "$test_java" -cp "$test_output/queue" TerrainQueueTest; then
  test_failed=1
fi
exit "$test_failed"
