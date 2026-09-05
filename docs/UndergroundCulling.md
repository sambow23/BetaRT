# Adaptive underground culling

In BetaRT's graphics settings, enable **Adaptive Underground Culling**. It is
off by default and persists as `MCRTX_UNDERGROUND_CULLING_ENABLED` in
`mcrtx-runtime.env`. This is a BetaRT submission feature; it needs the matching
patched client jar and JNI library, but no new Remix runtime or GPU extension.

The existing frustum test remains in place. When enabled, adaptive culling
replaces the legacy whole-section cave visibility gate. Turning it off restores
that legacy behavior and submits the original, unsplit section meshes.

## Visibility rules

Loaded 16-cube sections are flood-filled into air pockets. Shared boundary
openings connect those pockets into a world-space graph. Natural full-cube
terrain determines surface depth; an ordinary constructed roof alone does not
make a surface house an underground cave. Raw-stone buildings and unusual
terrain can still conservatively select cave protection.

Visibility is compiled for an 8-by-1-by-8-block region around both the player
and actual camera. The entire regions must classify as exterior before trimming.
Portal clipping uses separating planes enclosing rays from the whole camera
region, not center/corner samples or just the view frustum. Straight entrance
views are retained. One-block vertical cells avoid classifying a surface player
as underground merely because a tall cache cell extends below the ground.
Two additional pocket hops and all pockets within 32 blocks of the player are
retained as a lighting/movement guard. New results have a half-second activation
delay. Changing regions immediately rejects the old mask and retains geometry
until a matching result is ready. Inside, that result restores the connected
component without a distance or portal-depth cutoff.

This deliberately trades distant, indirectly visible cave contributions for
less ray-tracing work while outside. It is not an exact path-traced visibility
solution: a hidden deep bend can still contribute bounce light or appear in a
mirror. Visible entrance preservation also remains subject to the existing
frustum capture policy. Test representative entrances before enabling it by
default in a pack.

Unknown, dirty, or missing topology retains affected geometry; unloaded chunks
are never treated as new sky entrances. While outside, uncertain pockets also
seed portal clipping and the same two-hop lighting guard; a distant missing
section does not disable trimming throughout an entire connected cave network.
Inside, the full connected component is still restored. Transparent and partial blocks do not
seal pockets. Unsupported dimensions and portal-budget exhaustion use
conservative connected-component visibility. Material replacements that make
normally opaque full blocks transmissive are not known to the topology system;
leave the feature off for those replacements until their block classification
is updated.

## Submission and lifetime

Mixed surface/cave meshes are grouped by the pockets touching each triangle.
A group is omitted only when every owner pocket is hidden. Fully visible
sections continue to use their original single mesh. Group handles and hashes
are cached across visibility changes, with a separate hash namespace from the
original mesh. Geometry/topology revision mismatches retain geometry until
recapture catches up; obsolete handles use the existing deferred destruction
path.

The same published frame mask filters mobs and signs, particles and fire,
static torch lights, entity-held torch lights, and flame-particle lights.
First-person geometry, the player shadow, and the player's held light are
retained. Bounds crossing visible or unknown pockets are retained. Terrain
emissives follow their geometry. Definitions can stay cached while instance
submission is suppressed; culling does not promise reduced VRAM consumption.

Topology has a lossless queue separate from visibility-gated mesh recapture.
The game thread snapshots at most eight 16-cube sections per present, stopping
after a two-millisecond sampling budget (a section already in progress finishes).
Only loaded blocks are read; the worker never calls game-world or JNI methods.
Y-aware invalidation avoids rescanning entire columns, small light-only
notifications are ignored, and equal block flags do not rebuild/upload topology.
Adaptive mode also skips the legacy pocket classifier. Turning OFF clears its
stale cache and retains unclassified sections while legacy recapture catches up.

A low-priority daemon owns the graph and an LRU region cache (16 entries, about
32 MiB of masks, allowing one oversized result). It prefetches four horizontal
neighbors, then sleeps. Real topology changes invalidate all region results
conservatively, while graph connectivity rebuilds remain incremental. World
changes close the old worker; obsolete generations cannot activate masks.
Rotation/standing within settled regions performs no traversal or topology
upload, regardless of the number of loaded sections.

Topology uploads and mask activation are budgeted separately; two mesh groups
are built per renderer frame. JNI receives compact pocket bits directly.
Topology labels and native section maps are shared across immutable frame
snapshots, with copy-on-write only on changes. Mesh owner validation and hidden
flags are revision-cached; fire meshes (including empty results) are reused
until animation, geometry, visibility, or render origin changes. The completed
mask is published with camera/dynamic scene data, never partially activated.

## Status and diagnostics

The graphics panel displays `Exterior trimming`, `Cave protection`, or
`Waiting for topology`, plus submitted/culled group and culled-light counts.
The game output includes two periodic records:

- `Underground:` gives retained/hidden pocket counts and the cached worker
  query's CPU microseconds, not a per-frame cost. `queries`, `cacheHits`,
  `cachedRegions`, `pendingWorker`, `topologyUploads`, `maskUploads`, and `active`
  distinguish compilation from idle reuse. `pendingColumns` is a rounded-up
  count of pending section snapshots. The `hook.undergroundVisibility` profiler
  scope includes the whole game-thread update, including sampling and JNI.
- `Underground submissions:` gives submitted/hidden mesh groups, hidden
  triangles, entities, particles and lights, and pending mesh grouping work.

Warm-up can take longer than shader compilation because existing section
geometry needs revision-matched recapture. Wait for both pending counts to
reach zero and `active=true` before comparing performance. Live edits can
temporarily raise them. Disk persistence remains the subsequent phase, gated on
moving-camera/entrance correctness and CPU verification; this version caches in
memory only. No seed-based assumptions or unvalidated on-disk masks are used.

## Automated verification

Build the native Debug tests in a separate directory with
`-DMCRTX_BUILD_TESTS=ON`, then run `ctest --test-dir <build> --output-on-failure`.
`mcrtx_underground_visibility_tests` checks mixed-section splitting, static and
moving content, stale revisions, frame publication, stable handles, restoration,
and conservative ownership. It can also be built with AddressSanitizer and
UndefinedBehaviorSanitizer.

The topology core has no game dependencies:

```bash
javac --release 8 -d out/culling-tests \
  src/java-src/scene/mcrtx/bridge/UndergroundVisibility.java \
  src/java-src/scene/mcrtx/bridge/UndergroundVisibilityWorker.java \
  src/tests/java/scene/UndergroundVisibilityTest.java \
  src/tests/java/scene/UndergroundVisibilityWorkerTest.java
java -cp out/culling-tests UndergroundVisibilityTest
java -cp out/culling-tests UndergroundVisibilityWorkerTest
```

It covers sealed caves, straight entrances, bent branches, surface houses,
immediate inside restoration, transition delay, third-person cameras, negative
coordinates, dirty/unloaded/reloaded sections, and incremental-vs-fresh graph
agreement. `McrtxUndergroundSettingsTest` covers opt-in defaults and persistence.
The worker test additionally checks whole-region visibility against interior
point queries, bounded prefetch, stable cached identity over 100,000 requests,
zero stationary topology uploads/traversals, snapshot ownership, edit/unload
invalidation, unsupported/invalid cameras, and worker shutdown.

On the graphical NVIDIA host, close the normal game and run:

```bash
python3 scripts-linux/test-underground-culling.py \
  --profiler ../dxvk-remix-gmod/scripts-linux/profile-betart.py \
  --instance /path/to/PrismLauncher/instances/b1.7.3 \
  --jni /path/to/new/libmcrtx_jni.so \
  --client /path/to/new/minecraft-b1.7.3-client-mcrtx.jar \
  --output /path/to/new/comparison --rounds 2
```

The instance must enable `JoinServerOnLaunch` and specify its server. The runner
uses private Prism copies, installs both new binaries only into those copies,
and checks that the source instance's configuration and binaries remain
unchanged. It does not generate or replace texture assets. It inherits the
existing profiler's Ultra/DLSS Balanced, 1080p, OMM-disabled baseline; cloud
settings remain identical between OFF/ON runs. Do not move the player while it
is running. Alternating OFF/ON order, matching camera pose, settled topology,
GPU timestamp windows, screenshots, CPU aggregate profiles, and game logs are saved under the output
directory. Each test-created game is closed and its temporary account copy
removed on completion; the private game directories are retained for debugging.

The runner fixes the client sun/moon angle with
`-Dmcrtx.profile.celestialAngle=0.125` (override using `--celestial-angle`). This
does not change server time or persisted settings. Ordinary launches without
that JVM property still follow game time. Weather, cloud motion and entities
can still vary, so inspect screenshots and repeat runs. Windows with no
meaningful indirect-pass timing are rejected instead of being reported as a
culling speedup. ON runs must also show unchanged traversal/upload counters
during their stationary measurement. CPU profiling is enabled in the private
process environment only; `cpu-perf.log` includes Java hooks and native scopes.

Automated stationary comparisons do not replace these manual checks:

1. Rotate above ground, including looking down at cave entrances; compare OFF/ON.
2. Approach and enter a straight and a bent cave. Confirm immediate restoration
   on entry, then delayed trimming on exit. Repeat in third person.
3. Check a roofed surface house, doors, windows, water, leaves, and cutout blocks.
4. Break/place entrance blocks, unload/reload chunks, teleport, and change world.
5. Watch mobs, signs, moving held lights, fire and particles near a visibility
   boundary. Check for holes, flicker, stale shadows and unexpected light loss.
6. Toggle OFF and confirm restoration without restarting or changing textures.

Add `--manual --mode on` to the runner command (without `--rounds 2`) to launch
one private game that stays open for these checks. Movement is allowed and no
benchmark claim is produced. Close that game normally to clean up the temporary
account copy; Ctrl-C in the runner also closes only its own private game.

Keep the option off if any entrance or transition fails. Deploy the jar and JNI
library together, with backups of both; the normal settings need not change.

## Initial verification result

The 2026-09-05 private RTX 2080 comparison at the saved dense-server viewpoint
passed frame/pose, topology-settling, binary-load and source-unchanged checks.
The final fixed-sky pair measured 56.78 ms OFF (169 frames) and 59.04 ms ON
(164 frames), with about 278,000 triangles and 205 lights culled. This is not a
performance win; the feature remains experimental and off by default. Unknown
regions are intentionally retained, and mixed-section grouping adds submission
work. Weather/cloud motion and server entities can still vary between launches.
Manual entrances, movement and world-transition checks remain required.

All seven native test groups passed, including the new visibility/publication
test; that test also passed ASan/UBSan. Fifteen Java tests passed. The existing
`McrtxSourceOrganizationTest` fails its pre-existing assertion that
`remix_geometry_common.cpp` contains `appendDoubleSidedTexturedQuad` (it is
already absent at the parent commit); this change does not alter that helper.

## Background-cache verification (2026-09-05)

The region-cache revision builds as a Java 8 client and Linux Release JNI pair.
All seven native test groups pass, including shared-map publication, identical
mask no-ops, neighbor revalidation, fire reuse, and reset isolation. The native
visibility test also passes ASan/UBSan. Sixteen Java tests pass; only the same
pre-existing source-organization assertion above fails. The pure visibility and
worker tests additionally pass on the GPU host's actual Prism Java 8 runtime.

A private, fixed-pose RTX 2080 comparison with indirect lighting active recorded:

| Measurement | OFF | ON |
| --- | ---: | ---: |
| GPU `InjectRTX` median | 64.97 ms | 57.22 ms |
| Indirect pass median | 20.85 ms | 22.40 ms |
| Native snapshot preparation, recent average | 1.58 ms | 2.77 ms |
| Java culling update, recent average | disabled | 0.00194 ms |

The ON run retained 3,694 pockets and trimmed 2,659, with approximately 636,000
triangles and 304 lights omitted. `queries=11`, five cached regions, 3,921 topology
uploads, and 3,872 mask uploads remained unchanged throughout the settled window.
GPU samples covered 160 OFF / 167 ON frames. Source instance settings, textures,
client and JNI were not replaced; all test-created games were closed afterward.

This is one valid comparison, not a guarantee of gains in other worlds. Native
submission still costs more with mixed-section groups, and this viewpoint did
not improve indirect-pass timing. A preceding run was rejected because its
indirect pass recorded almost no work; that apparent speedup is not valid data.
The unchanged-build repeat above passed that check. Screenshots preserve the
visible stationary scene, but manual cave entries, moving cameras, edits, and
the user's CPU-heavy worlds remain required before normal deployment or disk
persistence. Initial native mesh grouping still takes a substantial warm-up;
an on-disk PVS alone would not remove that cost.
