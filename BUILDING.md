# Building the direct client

Use Linux x64, Clang, CMake 3.24+, Ninja, a JDK supporting `javac --release 8`,
and Java 8 for running Minecraft. Build the companion `openremix` raster
runtime with SDL3/Vulkan first. No shader compiler or RTX dependency is needed.

```sh
export JAVA_HOME=/path/to/compiler-jdk
scripts-linux/build-native.sh /tmp/betart-direct /path/to/openremix Release
scripts-linux/build-client.sh /path/to/minecraft-b1.7.3-client.jar \
  /path/to/lwjgl-2.9.4.jar /path/to/lwjgl_util-2.9.4.jar \
  /path/to/asm-9.9.jar /path/to/asm-tree-9.9.jar /tmp/betart-client
```

The two LWJGL 2 JARs supply compile-time declarations only. They must not be on
the installed runtime classpath. The final build audits all client class constant
pools for OpenGL/GLFW dependencies; only the SDL-backed `Display` and `DisplayMode`
compatibility class names remain. Patching modifies original bytecode and adds
adapters; decompiled Minecraft source is not redistributed.

In a separate Prism instance, install the resulting client as its custom main
JAR. Place `libmcrtx_jni.so`, `libremix.so.0` (and its real file/symlinks), and
`libSDL3.so.0` next to that JAR. Alternatively set `MCRTX_JNI_PATH` and
`MCRTX_REMIX_PATH` in `minecraft/mcrtx-runtime.env` to absolute library paths.
Keep LWJGL 3 **core and OpenAL only**, with their Linux natives, for audio. Remove
LWJGL OpenGL/GLFW modules and the old LWJGL 2 component. Standard Prism assets and
Java 8 still apply. Back up the original instance before replacing its files.

Generate the audio-only component from Prism's cached metadata:

```sh
python3 scripts-linux/prism-audio-component.py \
  /path/to/PrismLauncher/meta/org.lwjgl3/3.3.3.json \
  /path/to/instance/patches/org.lwjgl3.json
```

The output must not already exist. This keeps core/OpenAL natives while removing
the graphics modules; it does not change shared launcher metadata.

`scripts-linux/package-native.sh BUILD_DIRECTORY REMIX_PACKAGE_DIRECTORY NEW_OUTPUT_DIRECTORY`
combines the JNI library with the runtime's packaged SDL3/Remix libraries and
license notices. It requires a fresh output directory. The system Vulkan loader
and a Vulkan graphics driver are required on the target.

Optional environment settings: `MCRTX_VSYNC=0` disables VSync (or use the debug
panel), `MCRTX_PERF=1` enables detailed timings, and `MCRTX_PERF_LOG` selects the
log file. `MCRTX_GAMEPLAY_FOV` and `MCRTX_VIEWMODEL_FOV` retain camera FOV controls (30–120 degrees, default 70). Detailed profiling affects throughput. Old RTX, capture, lighting and
vanilla-suppression settings have no effect and should be removed.

## Verification

```sh
CC=clang CXX=clang++ cmake -S . -B /tmp/betart-tests -G Ninja \
  -DMCRTX_BUILD_TESTS=ON -DCMAKE_BUILD_TYPE=Release
cmake --build /tmp/betart-tests
ctest --test-dir /tmp/betart-tests --output-on-failure
python3 src/tests/python/test_prism_audio_component.py
scripts-linux/test-java.sh PATCHED_JAR /path/to/java8/bin/java NEW_TEST_DIRECTORY
```

Set `MCRTX_TEST_GPU=1`, `MCRTX_JNI_PATH` and `MCRTX_REMIX_PATH` to include Java/JNI,
real vanilla menu/model/startup and in-memory world tests. `SDL_VIDEODRIVER=offscreen`
works with a compatible software Vulkan driver. Enable `REMIX_VULKAN_VALIDATION=1`
and synchronization validation with the installed validation layer for GPU tests.
Set `MCRTX_RENDER_BACKEND=pathtrace` to exercise the in-memory world with lighting reuse; the transport, menu and exact raster-pixel fixtures retain raster. World tests allow cold driver compilation before timing warmup. The test classpath excludes all LWJGL modules; local audio therefore uses the
vanilla silent fallback. Verify OpenAL separately in the installed Prism client.

The private JNI protocol is version 6, independently of the public Remix SDK.
Update the patched JAR and JNI together. The paired openremix runtime supplies
camera environment metadata and tracing/denoising statistics. Lighting and sampling
statistics are optional: CMake checks the SDK types, missing exports are non-fatal,
and unsupported CSV fields read `unavailable`.
Path tracing uses Minecraft sun/sky illumination with REBLUR SH and TAA enabled by
default. `MCRTX_PT_SKY=0` disables indirect sky lighting; `MCRTX_PT_DENOISER=off` and
`MCRTX_PT_TAA=0` select raw output. Openremix supports optional SHARC diffuse radiance caching (`MCRTX_PT_SHARC=0/1`),
with `MCRTX_PT_SHARC_DOWNSCALE`, `MCRTX_PT_SHARC_SCALE`, and
`MCRTX_PT_SHARC_CAPACITY_LOG2` controls. Progressive accumulation bypasses SHARC.
The profiling CSV reports SHARC timings, memory, occupancy and resets when the
companion export exists; sampled query/hit counts have separate named columns.
Unsupported full-ray counters, emitter sampling and glow remain unavailable.
Use client and JNI artifacts from the same build and the companion runtime with
`remix_native_ext.h` exports. Windows client parity and arbitrary mods issuing GL
calls are outside this Linux migration.

## PBR maps

Set `MCRTX_LOAD_PBR=0` in `minecraft/mcrtx-runtime.env` to skip loading normal,
roughness, metallic and authored emissive maps. Set it to `1` to enable them
(the default). Restart the client after changing this setting. Base-color textures,
default EON material parameters and procedural block emission remain available.

The direct client loads `_normal`, `_roughness`, `_metallic` and `_emissive` DDS/PNG
siblings from the selected texture pack and existing `mcrtx_assets` roots. DDS takes
precedence within each source; selected-pack maps override bundled files. Set
`MCRTX_ASSETS` for an additional asset root. Terrain, entity and item maps retain
their existing layouts; compact water/lava/portal textures load their named siblings.
Maps are cached by content under `mcrtx_texturepack_cache/direct` so reloads preserve
queued resource lifetimes. Missing maps use roughness 1, metalness 0 and geometric normals.

Use the paired OpenRemix EON/GGX runtime and protocol 6 JNI/client artifacts.
Emissive maps retain intrinsic block intensities and the analytic-torch exclusion.
Displacement is deferred. The existing `reblur-sh` setting enables separate PBR
diffuse/specular denoising, followed by TAA/FSR. Mapped normals and roughness guide
reflection filtering; visible emissive maps stay sharp. `localDenoiser` controls
whether local PBR lighting joins the filter. Use an optimized OpenRemix release
build for gameplay.
