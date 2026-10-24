# Direct Remix backend migration

Target: replace all b1.7.3 OpenGL rendering/capture with direct Remix submissions,
using the unlit SDL3/Vulkan runtime. Linux first, with gameplay validation on
192.168.50.13. Keep gameplay, networking, Java 8, original-JAR patching and audio.
The finished build has no selectable capture fallback or OpenGL context.
The optional [diffuse path tracer](PathTracing.md) now shares this direct submission
path. Raster remains the default; the historical capture renderer stays removed.

## Accepted implementation

- Reuse vanilla GUI layout and animation calculations with explicit CPU transforms
  and geometry submission. Persistent models, signs and materials upload on change.
- Retain asynchronous block meshing; move section residency from vanilla display
  lists to loaded chunks, camera and render distance. Preserve generation/lifetime
  checks and neighbor invalidation.
- Raw CPU textures feed world materials and UI, including animated textures,
  skins, font atlases and texture-pack reloads. No framebuffer/texture readback.
- Cover terrain, all standard entities, held items, camera effects, skies/clouds,
  particles/weather, selection/breaking overlays, menus, inventory models and HUD.
- Use a versioned private JNI resource/frame protocol. One executing and one
  replaceable pending frame; resource updates remain ordered and reliable.
- Native thread owns SDL, API submission and Vulkan. Present fresh frames or
  explicit redraw reasons; pump events within 8 ms when idle. Debug UI may redraw
  at 60 FPS. Report fresh game frames separately from repeated presentations.
- Preserve public Remix 0.1003 layouts and exports. Add independent native input
  event/text/metrics exports and implement existing PNG screenshot requests.
- Preserve base colors, tints, transparency and gameplay feedback. Raster remains
  unlit; optional hardware path tracing evaluates a selected distant light and
  sun/sky diffuse lighting with REBLUR SH and native TAA; no upscaling or PBR materials.

## Implementation

The previously validated fixes were committed before migration:
runtime `8a5f92aa`, BetaRT `13cd7ba`. They remain the paired performance baseline.

The client now renders exclusively through Remix. `DirectClientPatchTool` rewrites
the original JAR, delegates its tessellator, model and font producers, and removes
graphics-driver calls. It retains vanilla animation, camera, entity and GUI
calculations. The build audits every packaged class, including paths not reached
during startup, and Java 8 verifies all 792 classes without a graphics classpath.

`DirectTransforms` maintains CPU model/projection/texture matrices.
`DirectRenderer` turns local meshes into persistent Remix resources and per-frame
draw commands. World indices are converted to the SDK's clockwise front faces.
Vanilla GUI geometry uses the existing Remix UI draw list with clipping, sampler
state, blending and inventory-model depth. The native raster-state extension
supplies depth comparison/write control and cloud depth-only prepasses.

`DirectTextures` owns decoded pixels. The original texture manager updates those
pixels for animations, skins and texture packs; dirty images and dependent
materials update through reliable commands. Fonts cache glyph geometry and
invalidate widths/meshes when their atlas changes. Generated item/sprite geometry
has a 512-entry, 8 MiB cache. Terrain residency belongs to loaded chunks and
camera distance; see [terrain ownership](TerrainPipeline.md).

The private JNI protocol uses version 6, a bounded little-endian packet format. Camera packets carry world/reset epochs and complete enabled sky RGB/sun direction/sun RGB state on every submission; client and JNI must be updated together.
Streamed mesh vertices contain position, UV and color, without authored normals.
The bridge leaves their normals zero so OpenRemix derives geometric face normals
after the instance transform. Terrain meshes retain their explicitly computed
normals. Substituting a fixed normal for streamed faces produces incorrect
lighting and a discontinuity where that normal turns away from the camera.
Version 2 adds stable instance identities, classification, independent raster
visibility and separate camera/object transforms; mismatched clients are rejected.
JNI copies its input before returning. The mailbox holds one executing frame
and one pending frame; the pending frame can be replaced once, after which the
producer waits for consumption. This prevents a fast Java producer from flooding
the reliable resource queue with resources for discarded frames. Resources are
ordered and never discarded with replaced frames. Stop releases blocked producers.

The native worker owns SDL, Remix calls and Vulkan presentation. It pumps input
within 8 ms while idle, presents fresh frames, and redraws only for terrain,
window/control changes, progressive accumulation or the visible debug panel. A minimized frame remains
pending until presentation is possible. Fresh counters advance only after an
actual successful/suboptimal Vulkan presentation. F3 reports Java publications,
fresh Remix frames and explicit redraws separately.

SDL input includes physical transitions, Unicode text, mouse motion/buttons and
wheel displacement. Focus/capture changes and overflow reset held inputs.
OpenRemix toggles its menu on its own shortcut (Alt+X unless set otherwise in the menu's header) and keeps
that key from the client, which suppresses gameplay input while the panel is visible. Closing it restores the
requested mouse grab. The hidden launcher AWT window is disposed during shutdown.
PNG screenshots use swapchain readback, including resize and shutdown cleanup.

The companion runtime reuses staging and transient geometry allocations after
the frame fence completes. The pool retains at most 64 MiB of Vulkan allocation
bytes and avoids steady-frame allocation/free churn. It does not introduce a
scene graph or a replacement public rendering API.

The public Remix 0.1003 headers and interface table remain unchanged. Additional
input, window-metric, presentation-counter and tracing capability/statistics exports live in
`remix_native_ext.h`. The private BetaRT JNI interface replaces the removed
capture bridge; old patched client JARs must be paired with their old JNI library.

## Completion checks

Build and launch without LWJGL OpenGL/GLFW modules; audit patched bytecode. Test
resource updates/lifetimes, dropped frames, terrain jobs, world changes, UI,
camera/model matrices, Unicode input, screenshots and shutdown/error recovery.
Run the runtime ABI and Vulkan synchronization validation suites. Exercise full
client gameplay including menus, text editing, inventories, texture reloads,
chunk loading, block updates, entities and effects.

Deploy with hashes/backups/rollback to the profiling instance first. Keep the
server and existing worlds intact. Compare three paired runs per build at the
same camera/resolution/settings, each with 45 s warmup and 60 s measurement.
Report fresh submitted/presented FPS, repeats, frame percentiles, JNI/upload
traffic and CPU/GPU load. Completion requires direct rendering throughout and
improved fresh-frame performance without a repeatable frame-time regression.
Windows client validation and arbitrary mods issuing GL calls are deferred.

Local validation passes 29 runtime tests with Vulkan synchronization validation,
two native bridge/terrain tests, and seven Java checks covering identities, queues, all-class
verification, transport, vanilla client producers, startup and an in-memory world.
The portable runtime/SDL3 require glibc 2.27 and JNI requires 2.17, within the 2.28
target. Headless Java tests use silent audio; OpenAL is checked in the host instance.
Detailed host results, paired measurements and deployment/rollback information are
recorded in [the verification report](DirectRendererVerification.md).
