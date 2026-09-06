# Terrain pipeline

Minecraft render-section allocation owns terrain residency. Every allocated,
available section stays submitted, including terrain behind the camera and below
ground. Entity and particle frustum/distance policies are unchanged. The UI's
anti-cull distance applies to those dynamic objects. The obsolete
`MCRTX_UNDERGROUND_CULLING_ENABLED` configuration entry is ignored.

The pipeline retains Remix's path tracer, materials, acceleration structures,
denoisers and upscalers. It uses the original block-shape helpers and vertex format.
More resident geometry can increase GPU time and memory use.

## Capture and ownership

Renderer positioning and unloading hooks assign section lifetimes. World changes
advance a generation and invalidate native work immediately. Vanilla build hooks
only enqueue dirty notifications. A single game-thread queue coalesces their
bounds with world-listener updates; it never truncates pending work.

Snapshots have a 2 ms budget per present, checked between 256-block slices. A
snapshot invalidated during collection restarts. Both render passes share one
capture, including Minecraft-dependent shapes, tiles, colors and fluid properties.
Missing chunks leave snapshots pending; availability checks never generate chunks.
The one retained snapshot is retried unchanged when native admission returns false.

The bulk JNI packet contains section coordinates, generation, lifetime, revision,
inclusive dirty bounds and 23 integers per block: ID, metadata, render type, pass,
six tile IDs, six float bounds, color, fluid visibility, four float heights and
flow angle. Float fields use their raw IEEE bits. Air is explicit. JNI copies the
array before returning; native storage merges partial updates into an immutable
section. Metadata already represented by captured bounds, tiles, colors and fluid
properties is normalized before comparison for the emitters that do not read it.

## Meshing and publication

Two workers are used on machines with at least four logical CPUs, otherwise one.
At most twice that number of jobs are dispatched. Each section has one coalesced
successor. Nearby work is preferred, with the oldest pending section selected on
every eighth dispatch. Identical normalized inputs do not generate geometry.

Workers retain immutable sections and a one-block neighbor halo. Boundary
revisions distinguish face, edge and corner dependencies, so an interior edit
does not invalidate another section. Completion validates world, internal reset
epoch, lifetime, content, neighbor lifetimes/revisions and geometry settings.
Obsolete work is discarded and current work scheduled.

Workers produce both passes' vertices, indices, material classes and desired light
placements. They never call Minecraft or Remix or access mutable renderer maps.
Completed results are limited to 128 MiB, conservatively including retained
canonical data; one oversized result may progress when the queue is empty.
Completed results release their neighbor snapshots immediately. Workers waiting for room are released by reset or shutdown.
Shutdown joins workers without holding their mutex.

The renderer consumes results before the next frame snapshot without waiting for
workers. Publication admits four sections, 8 MiB or 1 ms per frame, whichever is
reached first. A started section finishes atomically; budget overruns are counted.
Both passes and lights are staged together. Resource-creation failures preserve
the previous meshes and lights and retry the completed result. Unchanged meshes
and light handles are reused. Existing deferred destruction protects frame
snapshots in flight. Static terrain submission lists are cached until publication
or residency changes; camera rotation does not change their membership.

Remix mesh creation still runs during publication. CPU geometry generation is
backgrounded; GPU allocation is not promised to be asynchronous.

## Reproduce verification

Build in dedicated directories, with `JAVA_HOME` pointing to a JDK and
`MCRTX_DXVK_REMIX_ROOT` pointing to the runtime checkout:

```sh
cmake -S . -B /tmp/betart-worker -G Ninja -DCMAKE_BUILD_TYPE=RelWithDebInfo -DMCRTX_BUILD_TESTS=ON
cmake --build /tmp/betart-worker
ctest --test-dir /tmp/betart-worker --output-on-failure
cmake -S . -B /tmp/betart-sync -G Ninja -DCMAKE_BUILD_TYPE=RelWithDebInfo -DMCRTX_BUILD_TESTS=ON -DMCRTX_TERRAIN_SYNCHRONOUS=ON
cmake --build /tmp/betart-sync
ctest --test-dir /tmp/betart-sync --output-on-failure
```

The synchronous option preserves the same residency, snapshots and emitter with
meshing executed on the renderer thread. It admits one CPU build per pump. Save
both libraries, the client jar, source revision/diff and SHA-256 hashes alongside
measurements. Use separate builds with `-fsanitize=address,undefined` and
`-fsanitize=thread` in `CMAKE_CXX_FLAGS` for sanitizer runs.

`scripts-linux/build-client.sh` compiles the client with `--release 8`.
`scripts-linux/test-java.sh PATCHED_JAR LWJGL_JAR JAVA8_EXECUTABLE OUTPUT_DIRECTORY`
runs compatibility tests on Java 8 and standalone queue fixtures. The native
terrain tests cover ownership, partial/empty updates, unchanged-input reuse,
neighbor edits, revision/lifetime rejection, out-of-order completion, queue bounds,
shutdown, resource failures and in-flight retirement. Geometry comparisons use
the repository's synchronous emitter preserved in `tests/chunks/terrain_reference.cpp`,
including fluid, fence, redstone, cutout, translucent and boundary fixtures.

`scripts-linux/test-terrain-pipeline.py` uses the runtime repository's
`scripts-linux/profile-betart.py` to create private Prism copies on the GPU host.
It verifies the normal instance's library and configuration hashes before and
after each run, forces indirect lighting on, checks complete residency and idle
stationary terrain, and rejects invalid timing frames. Pass `--mode all` with
worker/synchronous libraries and the patched client to compare both against the
installed adaptive-OFF build. Pass `--manual` for rotation and movement; close the
private game to finish. Raw frame timestamps, GPU utilization/VRAM, screenshots,
CPU scope logs and terrain counters accompany each run.

`Terrain snapshots` logs report snapshot queue depth and cost. `Terrain` logs
report residency/publication counts, pending/dispatched/completed work, canonical
and completed bytes, oldest queue age, captures/reuse, builds/stale work/failures,
worker and admission-lock time, publication time/overruns, mesh creations and
triangles. CPU scopes expose snapshot, JNI copy/update, worker meshing, publication
and Remix mesh creation costs. Runtime frame CSVs provide CPU and GPU timing
percentiles; CPU frame intervals include synchronization and are not isolated
game-thread work time.

Do not accept performance measurements with missing terrain or inactive indirect
lighting. Compare gameplay CPU work against the synchronous equal-residency build
and total frame time against the installed adaptive-OFF build separately. Private
verification must pass before deploying to the normal instance.
