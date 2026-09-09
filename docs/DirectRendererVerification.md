# Direct renderer verification — 2026-09-08

This report records the earlier direct-renderer migration baseline. The subsequent
optional path tracer, current paired hashes and tracing measurements are documented
in [PathTracing.md](PathTracing.md) and its linked runtime verification report.

The Linux client uses direct Remix rendering throughout. The patched JAR has no
OpenGL or GLFW classpath dependency; SDL owns the window/input and Vulkan renders
world geometry, materials, vanilla models, effects and UI. The public Remix 0.1003
SDK remains compatible. The private JNI transport replaces the old capture bridge,
so client and JNI artifacts must be updated together.

## Automated checks

| Check | Result |
| --- | --- |
| Runtime Clang debug/Werror | 22 tests passed with offscreen Vulkan synchronization validation. |
| Portable runtime | Rocky Linux 8 / Clang 21.1.8: eight CPU/ABI tests passed. |
| Native client | Both terrain and direct submission/protocol tests passed. |
| Prism audio packaging | Two tests cover separate native artifacts, Maven classifiers and rejection of missing Linux natives. |
| Java 8 | Six checks passed: terrain queue/residency; verification of all 790 packaged classes; real JNI transport; vanilla model/menu/font/texture/input producers; startup/animations; an in-memory gameplay world. |
| Transport/lifetimes | Copied buffers, bounded replacement/backpressure, ordered resources, idle/no-replay, fresh versus redraw counters, world/lifetime changes and shutdown covered. An unavailable optional control does not poison the fatal renderer state. |
| Raster/input | Pixel checks include winding, font text with culling, model/material/texture changes, depth test/write/compare and cloud depth-only prepass, UI sampler state and screenshot resize. SDL/Java checks cover repeats, Unicode, focus/overflow reset, wheel displacement, button coordinates and captured UI shortcuts. |
| Package/ABI | Original SDK header hashes/layouts and 106 required exports preserved; additional native exports remain outside the interface table. ELF/classpath audits exclude removed renderer libraries and LWJGL graphics modules. |
| Compatibility floor | Runtime/SDL3 need glibc 2.27; JNI 2.17; sample 2.14, within the Linux 2.28 target. |

Local evidence is `/tmp/remix-direct-20260908`, with runtime logs in the companion
checkout's `_build-direct-runtime/meson-logs`. The Java headless tests intentionally
omit audio modules and use the vanilla silent fallback. The old vanilla resource
URL can return 404 in this isolated harness; it does not affect those test results.
OpenAL initializes in the actual host client.

## Gameplay and performance host

On `192.168.50.13`, the isolated `BetaRT-Raster-Profile` instance displays the dense
scene with signs, water animation, flames/smoke, player skin, held item and HUD.
Checks exercise inventory/player models, third-person camera, chat draft editing
without sending a message, native debug text editing, F12 capture/restore, wheel
selection, F2 PNG capture, fullscreen, resize, minimize/restore and shutdown.
An in-memory local world additionally exercises standard entities, effects, block
invalidation and unload without editing a save.

The scene passes Khronos synchronization validation through window/input changes
and clean shutdown with MangoHud disabled. The first overlay-enabled check recorded
ten hazards in an extra LOAD render pass; the runtime uses a CLEAR pass. Both
results are retained. The verified process loads the portable SDL/Remix/JNI files,
LWJGL core/OpenAL only, and no removed renderer libraries.

Three runs per build, each 45 s warmup plus 60 s measurement, use the same 1920×1080
scene/camera, render-distance setting 2, GUI scale 2, fancy graphics, 90° gameplay
FOV, 70° view-model FOV, and no frame limit/VSync:

| Mean over three runs | Previous capture build | Direct build |
| --- | ---: | ---: |
| Java publications/s | 101.80 | 1068.61 |
| Native presentations/s | 171.51, including replay | 536.03, all fresh |
| Java interval p99 | 17.60 ms | 2.71 ms |
| Native interval p99 | 10.31 ms | 4.23 ms |
| Explicit redraws in measured direct windows | unavailable | 0 |

Java publishes about twice as fast as native presentation; pending frames are
replaced under the bounded mailbox policy. F3 shows these separate rates.
The old renderer does not expose an exact fresh-present count: its Java publication
rate is an upper bound on new scene updates, and its native FPS includes repeats.
The results use instrumentation and automatic GPU boost, not fixed clocks or GPU
timestamps. The direct workload raises GPU power/clocks and process RSS while
reducing CPU use. Full distributions, draw/upload traffic, power/memory figures,
methods and binary hashes are in the companion runtime report
`docs/performance-direct-2026-09-08.md` and its `performance-data/direct-2026-09-08` files.

## Deployment and rollback

The normal `b1.7.3` instance receives a fresh library directory with the patched
client, JNI, Remix, SDL3 and licenses, plus an instance-local audio-only LWJGL
component. Original libraries (including old renderer assets), patches, options
and runtime settings are backed up under:

`/home/cr/remix-raster-deployments/20260909-direct`

The deployment did not restart the server or edit world data. The normal instance passed a second validation run using SDL's default desktop
window backend, with OpenAL initialized and zero validation/fatal errors through
resize, minimize/restore and clean shutdown. It was relaunched for gameplay without
validation or profiling agents; the final process audit confirms their absence.
The profiling instance remains separate with its wrapper disabled. An initial
normal-install attempt exposed missing separate LWJGL native artifacts in the
metadata helper; the helper and packaging regression tests now cover that case. After closing
the normal client, restore the previous build with:

```sh
python3 /home/cr/remix-raster-deployments/20260909-direct/deploy.py rollback
```

Rollback verifies installed binary hashes, preserves the direct library directory
and restores the original files. Staging, installation, changed-binary refusal and
rollback were tested using an isolated fixture.

| Artifact | SHA-256 |
| --- | --- |
| Patched client | `130c1280e62b4bf61cb516e8895510909c00e4c043273957768360d08a48c213` |
| JNI | `847bd9c4e039594e59539036c7ac91a6363304c75d265b358434203208bb90c7` |
| Remix | `b288da68ff772c4f20641f35439db84a30f482cfdff752b53893867a52d201c6` |
| SDL3 | `f4444de2ae3ec283450375896bdc19432426b5a28849235b6089494ec11bf784` |

Remote CSVs, screenshots, profiler sources and validation records are retained in
`/home/cr/remix-raster-profiles/20260909-direct`. Launcher logs are not committed.
The companion runtime preserves its four-platform CI/build paths; those CI jobs
were not run here. Windows client parity, mobile/touch/IME and monitor-DPI changes,
actual device loss, arbitrary GL-using mods and sustained world-traversal benchmarks
remain unverified. Standard effects have implementation and synthetic coverage,
not exhaustive visual coverage of every gameplay condition.
