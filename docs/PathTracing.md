# Opt-in diffuse path tracing

The direct client can render textured terrain and entities through Remix's Vulkan
hardware ray-query backend. Raster remains the default and all HUD, menus, held
items, particles, weather, labels and selection/damage feedback remain rasterized.
The sky is a visual background, not a source of illumination. A white distant light
supplies direct shadows and diffuse indirect lighting; F12 controls its direction,
color and intensity. There is no denoising, upscaling, specular material model,
physical refraction or acceleration-structure disk cache.

## Launch controls

Set environment variables on the isolated client's launch command or launcher
environment. F12 also permits switching at a completed native frame boundary.

| Variable | Values | Default |
| --- | --- | --- |
| `MCRTX_RENDER_BACKEND` | `raster`, `pathtrace` | `raster` |
| `MCRTX_PT_SAMPLES` | 1–16 | 1 |
| `MCRTX_PT_BOUNCES` | 0–4 indirect bounces | 2 |
| `MCRTX_PT_ACCUMULATE` | 0/1 or false/true | 0 |
| `MCRTX_PT_FORCE_REBUILD` | Same booleans; dynamic TLAS profiling | 0 |
| `MCRTX_PT_PROFILE` | Path to per-presentation CSV, overwritten on launch | disabled |

Tracing requires Vulkan 1.2, acceleration structures, ray queries, buffer device
addresses, nonuniform texture indexing, sufficient descriptors and a combined
graphics/compute/presentation queue. Unsupported selection logs Remix's reason and
leaves raster active. Standard SDK MoltenVK and devices without those capabilities
remain on raster. Tracing hardware validation is limited to the Linux RTX 2080 host;
Windows, macOS and Android hardware validation has not been performed.

One sample per pixel is visibly noisy. Accumulation can improve a stationary scene,
but camera motion, object animation, used animated textures or other scene changes
reset it. UI-only redraws do not invalidate it. The worker treats accumulation as
an explicit redraw reason and keeps fresh-presentation counters separate.

## Ownership and object identity

Update the patched client JAR and private JNI library together. Protocol version 2
adds lifetime identity, static/dynamic/raster-only classification and independent
raster visibility to Draw. TerrainView carries double-precision camera and floating
origin coordinates separately from its original view matrix. The original Remix
0.1003 interface remains unchanged; its independent native extension supplies the
identity and backend/statistics API.

JNI copies the complete packet before returning. Framing, versions, finite values,
identity/classification and lengths are validated before a pending frame can be
replaced. Resources remain ordered and reliable, with one executing and one
replaceable pending frame. Terrain worker/publication budgets remain bounded.

Terrain instance IDs derive from world generation, section lifetime and mesh pass.
Entity IDs derive from entity lifetime, model part and material pass, including a
per-part repeat index when a vanilla producer draws the same part several times.
Changing overall entity draw order, tint, alpha state or model-part mesh does not
change these IDs. Texture/blend combinations distinguish vanilla material passes.
World changes invalidate entity identities. Weak object maps avoid retaining dead
Java entities and model parts. Entity keys use object identity because vanilla's
equality/hash methods use mutable, reusable network IDs; a replacement object must
receive a new tracing lifetime even if it has the same network ID.

The origin initially snaps to a 256-block grid and rebases after the camera moves
more than 1,024 blocks along an axis. Entity producers render in object space relative
to this origin; ordinary camera motion changes camera data only. Rebasing changes
instance transforms without rewriting mesh vertices. Terrain includes the existing
loaded-section residency range. Entities are submitted throughout that range,
independently of camera-frustum visibility; their vanilla visibility predicate is
retained for raster drawing. First-person player-body exclusion retains vanilla behavior.

Rigid model parts share cached BLAS geometry across instances. Animation changes
instance transforms and normally refits the dynamic TLAS. Native skinned meshes
use private update-capable BLASes when vertices deform. Static terrain TLAS work
occurs only on membership, geometry-reference or origin changes. Recovery rebuilds
follow 300 refits or excessive displacement, with an explicit forced-rebuild mode.
See [the runtime design](../../dxvk-remix-gmod/docs/path-tracing.md) for synchronization,
compaction retirement, transparency, depth composition and resource limits.

## Verification and isolated staging

`scripts-linux/test-java.sh` runs identity/camera, terrain, class verification and
optional actual JNI/client/world tests. `DirectWorldTest` creates an in-memory world
and does not open a save or join a server. Its benchmark mode adds a scripted
moving population for comparisons, then checks inventory, third person, block edits
and unload. The runtime's deterministic GPU scenes test illumination and resource
lifetimes separately from Java gameplay.

`scripts-linux/stage-pathtrace-profile.py` only accepts an instance named
`BetaRT-Raster-Profile`, refuses a running instance, and updates four instance-local
artifacts as a pair. It creates a fresh backup with before/after SHA-256 hashes,
prepares every temporary file before publication, and restores the pair on a
publication error. Rollback refuses externally changed artifacts or mismatched
backups. Existing normal gameplay instances, servers and worlds are outside its scope.

```sh
python3 scripts-linux/stage-pathtrace-profile.py stage \
  --instance /path/to/BetaRT-Raster-Profile --payload /path/to/paired-artifacts \
  --backup /path/to/fresh-backup
python3 scripts-linux/stage-pathtrace-profile.py rollback \
  --instance /path/to/BetaRT-Raster-Profile --payload /path/to/paired-artifacts \
  --backup /path/to/fresh-backup
```

The payload contains `customjar-1.jar`, `libmcrtx_jni.so`, `libremix.so.0.1003.0` and
`libSDL3.so.0.4.14`. Build normal runtime/JNI/client artifacts first; staging does
not compile or change launcher options. The default remains raster after staging.

## Reproducible profiling

Compile `src/tests/java/direct/DirectWorldTest.java` against the paired patched JAR
with `javac --release 8 -cp /path/to/customjar-1.jar -d /path/to/classes ...`.
Run the profiler with the same working display/driver environment used for GPU tests:

```sh
python3 scripts-linux/profile-pathtrace.py \
  --instance /path/to/BetaRT-Raster-Profile --classes /path/to/classes \
  --output /path/to/new-results-directory
```

Defaults are three runs per configuration, 45 seconds warmup and 60 seconds
measurement, native 1920×1080, one sample per pixel, two indirect bounces and VSync
off. Configuration order rotates between raster, refit-with-recovery and forced
dynamic rebuild. Validation and injected overlays are disabled during measurements.
The test never loads the instance's saved world; its data directory is the run's
output folder. Keep the profiling instance closed before starting the script.

`frames.csv` records fresh status, presentation intervals, previous-frame GPU
build/trace timings, resource traffic, acceleration bytes, AS counters/rebuild reason,
scene generation, sampling and accumulation. `measurement.json` records the exact
window and fresh/published/presented count deltas. `load.json` samples process RSS
and NVIDIA utilization/memory/clocks/power each second. Per-run and aggregate JSON
summaries preserve interval p50/p95/p99 and AS work. `upload_bytes` includes copied
reliable resource command bytes and admitted terrain upload bytes; it is not a
measurement of PCIe traffic. Acceleration memory excludes textures and framebuffers;
NVIDIA memory samples include other host GPU users.

Compare total fresh frame cost as well as GPU construction and traversal. A cheaper
refit can leave a tree that traces more slowly. Recovery thresholds are initial
heuristics to evaluate in each workload. [The runtime verification report](../../dxvk-remix-gmod/docs/path-tracing-verification.md)
contains the tested host, final binary hashes, measured results and rollback path.
