#!/usr/bin/env bash
set -euo pipefail

if [[ $# != 6 ]]; then
  echo "usage: $0 MINECRAFT_JAR LWJGL_JAR LWJGL_UTIL_JAR ASM_JAR ASM_TREE_JAR OUTPUT_DIRECTORY" >&2
  exit 2
fi
client_source=$(realpath "$1")
client_lwjgl=$(realpath "$2")
client_lwjgl_util=$(realpath "$3")
client_asm=$(realpath "$4")
client_asm_tree=$(realpath "$5")
client_output=$(realpath -m "$6")
client_root=$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)
client_javac=${JAVA_HOME:+$JAVA_HOME/bin/}javac
client_java=${JAVA_HOME:+$JAVA_HOME/bin/}java
client_jar=${JAVA_HOME:+$JAVA_HOME/bin/}jar

if [[ -e "$client_output" ]]; then
  echo "Output directory must not already exist: $client_output" >&2
  exit 2
fi
mkdir -p "$client_output/classes" "$client_output/tool-classes"
cd "$client_root"
mapfile -t client_runtime_sources < <(rg --files src/java-src -g '*.java' |
  rg -v 'platform/org/|platform/mcrtx/lwjglshim/(GlfwBindings|LegacyARBOcclusionQuery|LegacyGL11)\.java' | sort)
mapfile -t client_compat_sources < <(rg --files src/java-src -g '*.java' |
  rg 'platform/org/|platform/mcrtx/lwjglshim/(GlfwBindings|LegacyARBOcclusionQuery|LegacyGL11)\.java' |
  rg -v 'platform/org/lwjgl/opengl/GL11\.java$' | sort)
"$client_javac" --release 8 -Xlint:-options -cp "$client_source:$client_lwjgl:$client_lwjgl_util" \
  -d "$client_output/classes" "${client_runtime_sources[@]}"
"$client_javac" --release 8 -Xlint:-options -cp "$client_output/classes:$client_lwjgl" \
  -d "$client_output/classes" "${client_compat_sources[@]}"
"$client_javac" -cp "$client_asm:$client_asm_tree" -d "$client_output/tool-classes" \
  src/tools-src/patcher/mcrtx/tools/ClientPatchTool.java
"$client_java" -cp "$client_output/tool-classes:$client_asm:$client_asm_tree" \
  mcrtx.tools.ClientPatchTool "$client_source" "$client_output/minecraft-b1.7.3-client-mcrtx.jar"
"$client_jar" uf "$client_output/minecraft-b1.7.3-client-mcrtx.jar" -C "$client_output/classes" .
echo "Built $client_output/minecraft-b1.7.3-client-mcrtx.jar (textures unchanged)"
