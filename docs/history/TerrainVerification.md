# Terrain rewrite verification — 2026-09-05

Implementation is complete. The user reported no visible problems after an
external server move and authorized deployment to the normal Prism instance for
further testing. The verified worker library and client were deployed on
2026-09-05 at 22:01:48 UTC. Subsequent user testing found no visible problems,
much smoother frame times than the previous build and fast chunk handling.
Controlled gameplay performance acceptance is still pending; rotation, movement
and a matched gameplay CPU comparison remain required.

## Automated checks

- All eight native tests pass in separate RelWithDebInfo worker, synchronous,
  ASan/UBSan and ThreadSanitizer build directories.
- Fourteen existing Java functional tests and the new terrain queue fixture pass
  on an actual Java 8 runtime. The complete client builds with `--release 8`;
  all 888 client classes have a class-file version no greater than 52.
- The broad `McrtxSourceOrganizationTest` still fails on its assertion that
  `remix_geometry_common.cpp` contains `appendDoubleSidedTexturedQuad`. The same
  failure was reproduced from the unmodified base commit. Terrain-specific
  architecture assertions preceding that check pass.
- Native fixtures compare vertices, indices, winding, UVs, material classes and
  desired light positions/radiance against the preserved synchronous emitter.
  They cover fluids, fences, redstone, cutouts, translucent passes and boundaries.
- Additional fixtures cover capture ownership, partial/empty updates, unchanged
  inputs, normalized metadata, boundary dependencies, stale content/settings/worlds,
  coordinate reuse and world changes during collection, out-of-order completion,
  oldest-job fairness, completed-result
  pressure, reset with a result held by the publisher, shutdown and reinitialization.
- Publication tests use Remix's actual hash-based handle semantics. They inject
  second-pass mesh and light failures, verify rollback and successful retry, preserve
  unchanged light handles, protect in-flight resources and check rotation/unload.

Sanitizers exposed an existing piston-helper out-of-bounds access for invalid
direction metadata 6/7. The helper now rejects those directions; valid piston
geometry is unchanged.

## Private host measurements

Host: i7-1270P, RTX 2080 8 GiB. Separate private Prism copies joined the configured
server. Runtime settings were matched: Medium, DLSS Ultra Performance, 1920×1080
output / 640×360 rendering, RIS 8, two maximum bounces, one minimum bounce,
ReSTIR GI, indirect lighting enabled, RR/FG/NEE cache/OMM disabled. The Remix
runtime binary was identical across runs.

These are preliminary measurements from the `comparison-retry` artifacts,
before the final idle-scheduler, result-retention, retry-handle and additional
metadata-normalization refinements. They are not a final performance acceptance.
Each stationary window lasted at least 15 seconds; all timing frames were valid
and indirect lighting was active.

| Measurement | Synchronous | Workers | Installed adaptive OFF |
|---|---:|---:|---:|
| Frames | 494 | 446 | 416 |
| CPU frame interval median / p95 / p99, ms | 30.47 / 31.51 / 32.37 | 33.54 / 35.82 / 36.84 | 35.61 / 39.87 / 42.41 |
| GPU RTX median / p95 / p99, ms | 22.32 / 23.50 / 30.75 | 21.88 / 23.14 / 30.14 | 32.69 / 34.20 / 34.69 |
| Resident and published sections | 2,312 | 2,312 | Legacy visibility policy |
| Static terrain triangles | 1,753,254 | 1,753,262 | Not instrumented equivalently |
| Peak observed GPU memory, MiB | 3,865 | 3,850 | 4,218 |
| Time until settled, seconds, including 45 s warmup | 79.87 | 50.56 | Different residency policy |
| Maximum observed pending native sections | 1,878 | 651 | — |
| Maximum observed dispatched jobs | 0 | 4 | — |
| Maximum observed native queue age, seconds | 50.64 | 8.32 | — |
| Publication overruns / resource failures | 1 / 0 | 2 / 0 | — |

Both new builds reached full residency and held build, mesh-creation and residency
counters constant through the sampled stationary checks. Their resident canonical
data occupied 691,361,984 bytes (about 659 MiB). Counters are sampled every 120 frames;
queue maxima above are observed samples, not exact high-water marks. Unit pressure
tests exercise the enforced dispatch and completion bounds directly.

The live server changed between runs (including an eight-triangle difference),
so this is not an identical-world replay. The synchronous control admits one CPU
mesh job per pump, which also affects cold-load throughput. CPU frame intervals
include synchronization and the existing autonomous renderer pacing. Measured
Java render-hook means were 14.98 ms synchronous, 16.39 ms workers and 29.30 ms
installed; they do not establish a CPU improvement against the synchronous control.
GPU improvements here must not be generalized to other render distances or scenes.

Two earlier measurements were rejected and retained for diagnosis: the first
worker smoke run performed additional stationary mesh work; a synchronous run
reported effectively inactive indirect-lighting timing. The successful retry used
the same effective settings. Rejected windows are not included in the table.

The final worker build also passed a 441-frame stationary window in
`movement-verified`: 2,312/2,312 sections published, no pending/dispatched/completed
work, stable terrain counters and active indirect lighting. GPU RTX median / p95 /
p99 were 21.85 / 23.11 / 23.84 ms; CPU frame intervals were 33.87 / 36.29 / 36.94 ms.
This confirms stationary correctness for the final artifact, but still does not
establish the required gameplay CPU improvement. Its snapshot is saved locally
as `/tmp/betart-terrain-work/final-stationary.json`.

## Existing runtime shutdown failure

Private copies of both new builds and the unmodified installed build crash during
shutdown after Remix reports that 42 common device objects were not disposed of.
Saved process mappings and `addr2line` identify the unmapped instruction as
`dxvk::WorkerThreadPool<65525ul, false, false>::processWork(unsigned int)` inside
the unloaded Remix library. This reproduces without the terrain rewrite. The new
terrain workers join successfully in the native shutdown and sanitizer tests;
the separate Remix runtime issue remains unresolved.

## Artifacts and remaining checks

Local artifacts are under `/tmp/betart-terrain-work/`:

- `artifacts/manifest.json`, `source.tar.gz`, both native libraries and patched
  client preserve the final implementation and SHA-256 hashes.
- `original.tar` preserves base commit `07ef7e83cf186f4ef6d25760bd966cbbd41a5c44`.
- `comparison-retry/` contains accepted preliminary windows, raw frame CSVs,
  screenshots, GPU/VRAM samples, CPU logs, effective settings and source hashes.
- `smoke/` and `comparison/` contain the rejected measurements.
- `native-verified-tests.log`, `sync-verified-tests.log`,
  `sanitize-verified-tests.log`, `tsan-verified-tests.log` and `java-final-suite.log`
  record automated test outcomes.

Private host artifacts are under `/tmp/betart-terrain-20260905/`. The
`movement-final` session used the final worker library and client. It recorded
position/orientation changes, then timed out waiting for a connected, stable
starting scene and closed automatically. It did not produce an accepted benchmark
window. The user confirmed an external server move and reported no visible
problems. The earlier
`movement-worker` session remained stationary and does not count as a movement
test. Normal-instance library/configuration hashes were verified unchanged after
each completed session.

Still required: inspect rotation and movement across section allocation boundaries,
water/fences/cutouts/translucency, moving held lights, particles and animated
entities; repeat a comparable route with the synchronous control; assess gameplay
CPU timing and latency to complete resident geometry. The user explicitly
authorized normal-instance deployment for further testing before these performance
acceptance checks were complete.

## Normal-instance test deployment

Target: `/home/cr/.local/share/PrismLauncher/instances/b1.7.3` on the GPU host.
The instance was closed during replacement. Installed SHA-256 hashes match the
verified artifacts:

- `libraries/libmcrtx_jni.so`:
  `7d5996f12e163368990b676a378c0f706fb6c60709546164bf0ca557c2a5838e`
- `libraries/customjar-1.jar`:
  `c1fe2b8a8ce954fc4df7bebb51dbef9d724213921596eb8dc3a9556c7b8c3270`

Original files and `deployment.json` are saved beneath the instance in
`terrain-backups/20260905-220148-UTC/`. The manifest records original and installed
hashes. The Remix runtime, instance configuration, runtime environment and game
settings were verified unchanged after deployment.

After testing the deployed build, the user reported that everything seemed to
work correctly, frame times were much smoother than the previous build and chunk
handling was fast. This is positive gameplay feedback against the previous build;
no timing samples or matched synchronous-control route accompanied the report.
