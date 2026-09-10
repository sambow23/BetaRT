# Opt-in diffuse path tracing

The direct client can render textured terrain and entities through Remix's Vulkan
hardware ray-query backend. Raster remains the default and all HUD, menus, held
items, particles, weather, labels and selection/damage feedback remain rasterized.
Minecraft supplies the same interpolated sky color and celestial angle used by the
visible sky on every world frame. Sky RGB is converted to linear once, at unit
strength. A spatially uniform environment illuminates diffuse surfaces only when
the existing continuation rays escape, respecting cutouts and tinted transmission.
Sun travel direction is `(0, -cos(theta), -sin(theta))`, where `theta` is the
celestial angle in radians. White noon intensity remains pi, scaled by
`clamp(2*cos(theta), 0, 1) * (1-rain) * (1-thunder)` for a continuous horizon fade.
Dimensions without a sky disable both sources; night ambient uses only Minecraft's
night-sky color. F12 retains the manual sunlight override and adds a sky toggle.
Sun/moon/star sprites remain visual backgrounds. REBLUR SH and native-resolution
TAA default on. SHARC caches indirect diffuse radiance; SIGMA filters primary
opaque sun visibility with distance-dependent penumbra. BetaRT supplies a
0.533-degree sun diameter. F12 exposes SIGMA and a sun-size override; zero diameter
retains hard shadows. See [SIGMA details](../../openremix/docs/features/sigma.md).
Openremix supports analytic sphere-light sampling; general mesh-emitter sampling, glow, moonlight, cubemap,
upscaling, a specular material model, physical refraction and acceleration-structure disk caches remain unavailable.

## Launch controls

Set environment variables on the isolated client's launch command or launcher
environment. F12 also permits switching at a completed native frame boundary.

| Variable | Values | Default |
| --- | --- | --- |
| `MCRTX_RENDER_BACKEND` | `raster`, `pathtrace` | `raster` |
| `MCRTX_PT_SAMPLES` | 1–16 | 1 |
| `MCRTX_PT_BOUNCES` | 0–4 indirect bounces | 2 |
| `MCRTX_PT_DENOISER` | `off`, `reblur-sh` | `reblur-sh` |
| `MCRTX_PT_SIGMA` | 0/1 or false/true | 1 |
| `MCRTX_PT_SUN_DIAMETER` | -1 inherit, 0–10 degrees override | -1 (game: 0.533) |
| `MCRTX_PT_SHADOW_DEBUG` | 0 final, 1 raw visibility, 2 SIGMA visibility | 0 |
| `MCRTX_PT_LOCAL_LIGHTS` | 0/1 or false/true | 1 |
| `MCRTX_PT_LOCAL_SAMPLES` | 1–16 primary local samples | 2 |
| `MCRTX_PT_LOCAL_DENOISER` | 0/1 or false/true (with REBLUR enabled) | 1 |
| `MCRTX_PT_LOCAL_INTENSITY` | 0–100 radiance multiplier | 1 |
| `MCRTX_PT_SHARC` | 0/1 or false/true | 1 |
| `MCRTX_PT_SKY` | 0 or 1 | 1 |
| `MCRTX_PT_TAA` | 0/1 or false/true | 1 |
| `MCRTX_PT_ACCUMULATE` | 0/1 or false/true | 0 |
| `MCRTX_PT_FORCE_REBUILD` | Same booleans; dynamic TLAS profiling | 0 |
| `MCRTX_PT_PROFILE` | Path to per-presentation CSV, overwritten on launch | disabled |

Tracing requires Vulkan 1.2, acceleration structures, ray queries, buffer device
addresses, nonuniform texture indexing, sufficient descriptors and a combined
graphics/compute/presentation queue. Unsupported selection logs Remix's reason and
leaves raster active. Standard SDK MoltenVK and devices without those capabilities
remain on raster. Tracing hardware validation is limited to the Linux RTX 2080 host;
Windows, macOS and Android hardware validation has not been performed.

Raw one-sample output is noisy. REBLUR SH filters indirect lighting; TAA resolves native-resolution world edges and transparent-light noise before raster gameplay overlays.
Normal torches and lit redstone torches provide analytic sphere illumination.
The native terrain worker extracts flame positions from existing v5 block data;
accepted section publications retain stable IDs by cell and lifetime. Wall-mounted
flames use their rendered cap offsets. Unlit redstone keeps a zero-power slot,
while removed chunks retire their lights. All resident torches contribute,
including those outside the camera frustum. Stale worker results never update
light handles.

The renderer uses a persistent LightBVH with a default budget of two samples at
opaque primary surfaces and one at diffuse bounces, SHARC updates and transparent
sheets. Local primary radiance uses a separate NRD filter; SIGMA continues to
filter only the sun. F12 exposes local light controls and diagnostics. See
[local-light architecture](../../openremix/docs/features/local-lights.md) for
radiance strengths, sampling, temporal behavior and current limitations.

Progressive accumulation bypasses SIGMA, REBLUR, TAA and SHARC while retaining their requested settings. It can improve a stationary scene,
but camera motion, object animation, used animated textures or other scene changes
reset it. UI-only redraws do not invalidate it. The worker treats accumulation and warming temporal histories as
explicit redraw reasons and keeps fresh-presentation counters separate. After convergence, UI-only redraws reuse cached world output.

## Ownership and object identity

Update the patched client JAR and private JNI library together. Protocol version 5
retains lifetime identity, static/dynamic/raster-only classification and independent
raster visibility to Draw. TerrainView carries double-precision camera and floating
origin coordinates separately from its original view matrix. The original Remix
0.1003 interface remains unchanged; its independent native extension supplies the
identity, camera temporal/environment metadata and backend/tracing/denoising APIs.
Lighting, sampling, SHARC, SIGMA and local-light statistics are compile-gated against the SDK and loaded as
optional exports; unsupported CSV fields remain explicitly `unavailable`. SIGMA
adds `sigma`, `gpu_sigma_ms`, `sigma_history_resets` and `sun_diameter_degrees`;
`gpu_nrd_ms` remains indirect REBLUR timing.
Protocol-v5 material emission fields and the 24-word terrain snapshot remain
transport-compatible. Torch lighting derives from block identity and orientation;
material emission fields do not enable general mesh emission or glow.
Every camera packet carries world and history-reset epochs, preserving world/cut/rebase resets through replaced pending frames. Camera motion and normal object animation keep temporal history; incompatible mesh changes invalidate affected surfaces. Material/texture and ordinary time/weather changes affect shading confidence independently from geometric motion, without global history resets. Sky changes invalidate progressive accumulation. Zero indirect bounces disables sky lighting.

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
See [the runtime design](../../openremix/docs/path-tracing.md) for synchronization,
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

## Optional diagnostics

`MCRTX_PT_PROFILE` records per-presentation CSV with fresh/redraw status,
presentation intervals, tracing/confidence/NRD/TAA timings, history resets,
resource traffic and acceleration statistics. Unsupported lighting/sampling
statistics fields are `unavailable`, never fabricated zero counters. Legacy RTXDI, general emissive-mesh sampling and checkerboard-sampling controls
belong to other companion runtimes. The separate local-light diagnostics report
light count, sample budget, BVH builds/refits, CPU update cost and torch NRD time.

Keep screenshots, validation results and deployment manifests in ignored
`_work/reports/`. Stage the renderer, JNI and patched JAR as a matched set with the
instance closed; preserve its SDL binary, launcher settings and saved worlds.
