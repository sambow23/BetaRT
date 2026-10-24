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

## Portable Linux Prism instances

Host builds can acquire newer glibc/libstdc++ requirements than other machines.
Build both native libraries against the pinned glibc 2.28 container baseline:

```sh
JAVA_HOME=/path/to/compiler-jdk scripts-linux/build-portable-native.sh \
  /path/to/openremix /path/to/new-portable-package
python3 scripts-linux/create-portable-instance.py \
  --template /path/to/working-BetaRT-instance \
  --native /path/to/new-portable-package/native \
  --output /path/to/PrismLauncher/instances/BetaRT-Portable-Linux \
  --archive /path/to/BetaRT-Portable-Linux.zip
python3 src/tests/python/test_portable_instance.py
```

The build requires Docker and a compiler JDK. It builds pinned SDL3 and the current
renderer/bridge sources with Clang, includes static C++ runtimes and their license
notices, and checks all packaged ELF libraries against the glibc ceiling. GPU
drivers and the Vulkan loader remain host dependencies. `BETART_PORTABLE_BUILD_IMAGE`
can select an already prepared image using the same Dockerfile/toolchain.

The instance creator reads the template's custom client, component metadata and
PBR assets, and writes fresh launch/runtime settings. It rejects host build paths
and newer glibc dependencies, and materializes library symlinks for ZIP transport.
It leaves the original instance intact. See [portable usage](docs/PortableLinux.md)
for import, Java selection and re-export instructions.

To verify loader diagnostics without a GPU, compile `DirectLibraryFailureTest`
against the packaged client and run it with `MCRTX_REMIX_PATH` pointing at a
nonexistent file, passing that same path as its argument. Keep the JNI library
beside the JAR and run from a directory without `mcrtx-runtime.env` overrides.

Optional environment settings: `MCRTX_VSYNC=0` disables VSync (or use the debug
panel), `MCRTX_PERF=1` enables detailed timings, and `MCRTX_PERF_LOG` selects the
log file. `MCRTX_GAMEPLAY_FOV` and `MCRTX_VIEWMODEL_FOV` retain camera FOV controls (30–120 degrees, default 70). OpenRemix opens and closes the runtime menu itself on its shortcut, Alt+X unless another is set in the menu's header. Detailed profiling affects throughput. Old RTX, capture, lighting and
vanilla-suppression settings have no effect and should be removed.

Runtime features BetaRT enables by default, when the runtime provides them: terrain
sections are registered once as retained instances and replayed by the runtime
(`MCRTX_RETAINED_TERRAIN=0` restores per-frame draws); the path tracer keeps static
sections between frames (`MCRTX_PERSISTENT_STATIC`); recording and presenting run on a
runtime render thread while the next frame is built (`MCRTX_ASYNC_PRESENT`); shadow rays
stop at the first blocker (`MCRTX_PT_SHADOW_EARLY_OUT`); and one-sided entity models cull
back faces in traversal (`MCRTX_PT_HARDWARE_FACE_CULLING`; terrain is double-sided). Set a
variable to `0` to disable its feature. These apply at every launch over the saved runtime-menu
profile, so a panel change lasts for that session.

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
The pathtrace run also checks streamed dynamic faces across the camera horizon,
including mirrored transforms and both raster/ray primary visibility. GPU tests
use isolated OpenRemix configuration directories under their output directory.

The private JNI protocol is version 6, independently of the public Remix SDK.
Update the patched JAR and JNI together. The paired openremix runtime supplies
camera environment metadata and tracing/denoising statistics. Lighting and sampling
statistics are optional: CMake checks the SDK types, missing exports are non-fatal,
and unsupported CSV fields read `unavailable`.
Path tracing uses Minecraft sun/sky illumination with REBLUR SH and TAA enabled by
default. `MCRTX_PT_SKY=0` disables indirect sky lighting; `MCRTX_PT_DENOISER=off` and
`MCRTX_PT_TAA=0` select raw output. Openremix supports optional SHARC radiance caching
for static diffuse and rough PBR surfaces (`MCRTX_PT_SHARC=0/1`),
with `MCRTX_PT_SHARC_DOWNSCALE`, `MCRTX_PT_SHARC_SCALE`, and
`MCRTX_PT_SHARC_CAPACITY_LOG2` controls. Progressive accumulation bypasses SHARC.
The profiling CSV reports SHARC timings, memory, occupancy and resets when the
companion export exists; sampled query/hit counts have separate named columns.
Unsupported full-ray counters, emitter sampling and glow remain unavailable.
Use client and JNI artifacts from the same build and the companion runtime with
`remix_native_ext.h` exports. Windows client parity and arbitrary mods issuing GL
calls are outside this Linux migration.

For repeatable SHARC timings, `scripts-linux/profile-sharc.py` runs
`src/tests/java/direct/SharcProfileTest.java` against an immutable terrain snapshot
in RAM. It never connects to the server or opens a game save. Compile that class
with `javac --release 8 -cp LIBRARIES/customjar-1.jar -d ROOT/classes`, supplying
the Java source path, and compile `src/tests/native/sharc_profile.cpp` with
`clang++ -shared -fPIC -O2`, the JDK `include` and `include/linux` paths,
OpenRemix's `public/include`, and `-ldl -o ROOT/libsharc_profile.so`.
Run the driver with `ROOT LABEL --libraries LIBRARIES --java JAVA8
--renderer RENDERER --sharc 0` (or `1`). The renderer must support
`REMIX_PROFILE_EVERY_FRAME=1`; fresh packet counts can otherwise include reused
lighting. The standalone native helper reads existing diagnostics and sets
configuration without changing the game's JNI protocol.

`ROOT/terrain-snapshot.bin` uses Java `DataInputStream` byte order: version `1`
and chunk count as 32-bit integers; camera eye XYZ as doubles; yaw/pitch as
floats; world seed and time as 64-bit integers; then each chunk's integer X/Z
and five length-prefixed byte arrays (blocks, metadata, block light, sky light,
height map). Use already-saved, stable chunk data and include the requested view
plus a loading margin. Entities and game ticks are not replayed. Keep source
hashes and the camera pose beside the snapshot.

The driver records exact settings, artifact hashes, GPU clocks and frame windows.
It requires at least ten seconds and 128 GPU frames with stable geometry/lights/
cache resets before measurement, then checks that GPU lighting actually ran. Use
`--phases FILE` for repeated settings in one process: each headerless CSV row is
`name,sharc,bounces,capacityLog2,downscale`. Alternate off/on and on/off pairs.
An optional sixth column chooses dispatch: `0` inline, `1` split, `a` automatic.
The seventh column selects scene-table placement (`0` host, `1` device), the
eighth toggles adaptive indirect termination (`0` fixed, `1` adaptive), and the
ninth sets its minimum completed bounces (`1`–`4`). `--roulette` and `--roulette-min`
set launch defaults. `--camera FILE` selects another view of the same immutable
snapshot from a JSON array `[eyeX, eyeY, eyeZ, yaw, pitch]`; the driver records its
hash. Keep cache, dispatch, placement and reconstruction settings fixed within
each comparison. Use three alternating fixed/adaptive pairs with ten-second
stable warmups and twenty-second measurements. Record individual pairs and run
HDR captures, validation and `--rays`/`--state` diagnostics separately from timing.
The driver creates the renderer's diagnostic control file and the fixture replaces
it atomically before each cache reset. `--state` adds completed-frame pipeline and
buffer state to stage diagnostics. `--device-local-scene` enables the companion
renderer's opt-in GPU-local scene-table placement experiment.
`--validation-layers` and
`--validation-libraries` select explicit layer directories for separate validation
runs; exclude those runs from performance comparisons. The fixture rejects a
missing reconstruction backend before measurement, and the driver rejects
validation errors or a separate-indirect fallback.
Captures occur after measurement; use `--stages`, `--rays` and pipeline statistics
only in separate diagnostic runs. Trace timing already includes SHARC update and
resolve. Disabled SHARC retains its allocations and last diagnostics, so stale
update/resolve times and hit counts must not be attributed to an off phase.

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
Emissive maps retain intrinsic block intensities. Torch and lit redstone-torch tips
use authored maps or procedural masks for their visible glow. Their reserved terrain
material IDs select OpenRemix's `MaterialInfoEmissionEXT` policy to suppress duplicate
area lights; the existing analytic sphere lights keep their brightness and placement.
Use the matching updated client, JNI and OpenRemix together; protocol 6 is unchanged.
Displacement is deferred. The existing `reblur-sh` setting enables separate PBR
diffuse/specular denoising, followed by TAA/FSR. Mapped normals and roughness guide
reflection filtering; visible emissive maps stay sharp. `localDenoiser` controls
whether local PBR lighting joins the filter. Use an optimized OpenRemix release
build for gameplay.
