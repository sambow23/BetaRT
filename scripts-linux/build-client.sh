#!/usr/bin/env bash
set -euo pipefail
if [[ $# != 6 ]]; then
  echo "usage: $0 MINECRAFT_JAR LWJGL_JAR LWJGL_UTIL_JAR ASM_JAR ASM_TREE_JAR OUTPUT_DIRECTORY" >&2
  exit 2
fi
direct_source=$(realpath "$1")
direct_lwjgl=$(realpath "$2")
direct_util=$(realpath "$3")
direct_asm=$(realpath "$4")
direct_tree=$(realpath "$5")
direct_output=$(realpath -m "$6")
direct_root=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)
direct_javac=${JAVA_HOME:+$JAVA_HOME/bin/}javac
direct_java=${JAVA_HOME:+$JAVA_HOME/bin/}java
direct_jar=${JAVA_HOME:+$JAVA_HOME/bin/}jar
if [[ -e "$direct_output" ]]; then
  echo "Output directory must not already exist: $direct_output" >&2
  exit 2
fi
mkdir -p "$direct_output/classes" "$direct_output/tool-classes"
cd "$direct_root"
"$direct_javac" -cp "$direct_asm:$direct_tree" -d "$direct_output/tool-classes" src/tools-src/patcher/mcrtx/tools/DirectClientPatchTool.java
"$direct_java" -cp "$direct_output/tool-classes:$direct_asm:$direct_tree" mcrtx.tools.DirectClientPatchTool \
  "$direct_source" "$direct_output/minecraft-b1.7.3-client-mcrtx.jar"
mapfile -t direct_sources < <(find src/java-src/direct -name '*.java' -type f | sort)
direct_sources+=(
  src/java-src/core/mcrtx/bridge/RemixBridgeNative.java
  src/java-src/core/mcrtx/bridge/McrtxRuntimeConfig.java
  src/java-src/core/mcrtx/bridge/HookProfiler.java
  src/java-src/lifecycle/mcrtx/bridge/McrtxPerfNative.java
  src/java-src/chunks/DirtyChunkSection.java
  src/java-src/chunks/RemixChunkSectionKey.java
  src/java-src/chunks/RemixChunkBlockCapture.java
  src/java-src/platform/org/lwjgl/LWJGLException.java
  src/java-src/platform/org/lwjgl/Sys.java
  src/java-src/platform/org/lwjgl/opengl/DisplayMode.java
  src/java-src/platform/org/lwjgl/input/Keyboard.java
  src/java-src/platform/org/lwjgl/input/Mouse.java
  src/java-src/platform/org/lwjgl/opengl/Display.java
  src/java-src/platform/org/lwjgl/input/Controllers.java
  src/java-src/platform/org/lwjgl/input/Cursor.java
  src/java-src/platform/mcrtx/lwjglshim/LegacyAL10.java
  src/java-src/platform/mcrtx/lwjglshim/OpenAlCompat.java
  src/java-src/platform/paulscode/sound/libraries/LibraryLWJGLOpenAL.java
)
"$direct_javac" --release 8 -Xlint:-options -cp "$direct_output/minecraft-b1.7.3-client-mcrtx.jar:$direct_lwjgl:$direct_util" \
  -d "$direct_output/classes" "${direct_sources[@]}"
"$direct_jar" uf "$direct_output/minecraft-b1.7.3-client-mcrtx.jar" -C "$direct_output/classes" .
"$direct_java" -cp "$direct_output/tool-classes:$direct_asm:$direct_tree" mcrtx.tools.DirectClientPatchTool --audit "$direct_output/minecraft-b1.7.3-client-mcrtx.jar"
echo "Built direct Remix client: $direct_output/minecraft-b1.7.3-client-mcrtx.jar"
