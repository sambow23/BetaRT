#!/usr/bin/env bash
set -euo pipefail
if [[ $# != 3 ]]; then
  echo "usage: $0 PATCHED_CLIENT_JAR JAVA8_EXECUTABLE OUTPUT_DIRECTORY" >&2
  echo "Set MCRTX_TEST_GPU=1 and MCRTX_JNI_PATH/MCRTX_REMIX_PATH to run Vulkan client tests." >&2
  exit 2
fi
test_client=$(realpath "$1")
test_java=$(realpath "$2")
test_output=$(realpath -m "$3")
test_javac=${JAVA_HOME:+$JAVA_HOME/bin/}javac
test_root=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)
if [[ -e "$test_output" ]]; then
  echo "Output directory must not already exist: $test_output" >&2
  exit 2
fi
mkdir -p "$test_output/client" "$test_output/queue"
cd "$test_root"
mapfile -t test_queue_sources < <(rg --files src/tests/java/chunks -g '*.java' | sort)
"$test_javac" --release 8 -d "$test_output/queue" "${test_queue_sources[@]}" \
  src/java-src/chunks/DirtyChunkSection.java src/java-src/chunks/RemixChunkSectionKey.java \
  src/java-src/direct/DirectSectionResidency.java src/java-src/direct/DirectSectionUpdates.java
"$test_java" -cp "$test_output/queue" TerrainQueueTest
"$test_javac" --release 8 -cp "$test_client" -d "$test_output/client" src/tests/java/direct/DirectClassLoadingTest.java src/tests/java/direct/DirectIdentityTest.java src/tests/java/direct/DirectEmissionTest.java src/tests/java/direct/DirectPbrTest.java src/tests/java/direct/DirectEnvironmentTest.java src/tests/java/direct/DirectWaterIceTransparencyTest.java
"$test_java" -Xverify:all -cp "$test_output/client:$test_client" DirectClassLoadingTest "$test_client"
"$test_java" -cp "$test_output/client:$test_client" DirectIdentityTest
"$test_java" -cp "$test_output/client:$test_client" DirectEmissionTest
(cd "$test_output" && "$test_java" -cp "$test_output/client:$test_client" DirectPbrTest)
"$test_java" -cp "$test_output/client:$test_client" DirectEnvironmentTest
"$test_java" -cp "$test_output/client:$test_client" DirectWaterIceTransparencyTest
if [[ ${MCRTX_TEST_GPU:-0} == 1 ]]; then
  mapfile -t test_sources < <(rg --files src/tests/java/direct -g '*.java' | sort)
  "$test_javac" --release 8 -cp "$test_client" -d "$test_output/client" "${test_sources[@]}"
  for test_name in DirectTransportTest DirectClientTest DirectStartupTest DirectWorldTest; do
    test_data="$test_output/$test_name"
    [[ $test_name != DirectTransportTest ]] || test_data="$test_data.png"
    test_backend=raster
    test_timeout=60
    if [[ $test_name == DirectWorldTest ]]; then
      test_backend=${MCRTX_RENDER_BACKEND:-raster}
      test_timeout=300
    fi
    timeout "$test_timeout" env MCRTX_RENDER_BACKEND="$test_backend" "$test_java" -Djava.awt.headless=true -Dmcrtx.test.directGpu=true \
      -cp "$test_output/client:$test_client" "$test_name" "$test_data"
  done
fi
