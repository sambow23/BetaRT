# mc-rtx

Minecraft Beta 1.7.3 Remix port workspace.

This repo keeps three things together:

1. A repeatable decompilation workflow for the Beta 1.7.3 client jar.
2. Notes about the real Java-side hook points found in the decompiled client.
3. A native x64 JNI scaffold that will host the direct RTX Remix renderer integration.

## Current state

The Beta client jar has been extracted and decompiled with CFR into `work/decompiled-cfr`.

The key render pipeline classes identified so far are:

1. `net.minecraft.client.Minecraft` for startup, the main loop, resize, and shutdown.
2. `px` for camera setup and the world render pass.
3. `n` for chunk visibility, world rendering, and chunk renderer management.
4. `dk` for chunk display-list compilation.
5. `dn` for particles.
6. `mk` for the loading screen.

See `docs/hook-points.md` for the first mapping report.

## Layout

1. `scripts/` contains repeatable extraction and decompilation helpers.
2. `docs/` contains notes and mapping reports.
3. `java-src/` contains support classes for the future Java-side JNI seam.
4. `native/` contains the Windows x64 Remix/JNI implementation scaffold.
5. `work/` contains extracted and decompiled artifacts and should be treated as working data, not polished source.

## Native build

The native project expects a sibling checkout of `dxvk-remix-gmod` at `../dxvk-remix-gmod`.

Example configure command:

```powershell
cmake -S . -B build -G "Visual Studio 17 2022" -A x64
cmake --build build --config Debug
```

## Decompilation workflow

The helper script reruns extraction and both decompilers:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\decompile-minecraft.ps1
```

## Patched client build

The patched client build script compiles the bridge/helper classes for Java 8,
bytecode-patches the original Beta client classes that need runtime hooks, and
stages `mcrtx_jni.dll` next to the patched jar:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\build-patched-client.ps1
```

The output bundle is written to `out/patched-client`.

## Runtime window mode

The native bridge defaults to the current single-window presentation mode,
where the Remix output is shown in a client-area overlay on top of the LWJGL
game window.

For development, set `MCRTX_WINDOW_MODE=dual` to create a separate Remix output
window instead. Supported values are `single` or `overlay` for the default
mode, and `dual`, `detached`, or `separate` for the linked development mode.
Use `MCRTX_WINDOW_MODE=standalone` for a detached window that skips Remix UI
overlay capture and no longer mirrors the LWJGL client window on each present.

The old `MCRTX_USE_SOURCE_WINDOW=1` escape hatch is now treated as a deprecated
alias for `MCRTX_WINDOW_MODE=dual` so older local launch scripts do not try the
broken same-HWND presentation path.

## Profiling

Opt-in profiling instruments every Java hook, the JNI boundary, every
`RemixRenderer::X` native method, and every `remix_.X` call the bridge issues.
It is **off by default** (zero overhead in the hot path). Enable it with:

```powershell
$env:MCRTX_PERF = "1"
```

Aggregated stats are written to `mcrtx-perf.log` next to `mcrtx_jni.dll` (or
to the path in `MCRTX_PERF_LOG`). One line is emitted per `(side, site)` tuple
every 60 presented frames; the interval is configurable via
`MCRTX_PERF_INTERVAL`. Each line reports sample count, average µs, and max µs
since the last flush.

Sides are:

- `Hook` — Java hook entry points in `MinecraftRemixHooks` (wall time spent in
  the hook, including all work it dispatches).
- `Jni` — time spent inside each `Java_mcrtx_bridge_RemixBridgeNative_nX` C
  function (marshalling + native work).
- `Native` — `RemixRenderer::method` wall time.
- `Remix` — individual `remix_.X` calls (labelled with subsystem suffixes such
  as `DrawInstance.chunk`, `CreateMesh.cloud`, `SetupCamera.world`).
- `Call` — reserved for Java-side wrappers around individual JNI sites.

Set `MCRTX_PERF_TRACE=1` in addition to `MCRTX_PERF=1` to emit a per-call JSONL
trace to `mcrtx-perf-trace.jsonl` (or `MCRTX_PERF_TRACE_LOG`). This is
high-volume; use it only for targeted investigations.

## Quick deployment for testing

For the local PrismLauncher `b1.7.3` instance, deploy the latest patched jar and
JNI DLL with:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\deploy-test-build.ps1 -Build
```

That command builds the native JNI bridge in `Release`, rebuilds the patched
client bundle, replaces PrismLauncher's shared `minecraft-b1.7.3-client.jar`,
and copies `mcrtx_jni.dll` next to that shared jar. The bridge loads the DLL
from the jar directory because PrismLauncher recreates the instance `natives`
directory on each launch.

The deploy script also configures the PrismLauncher `b1.7.3` instance with a
pre-launch command that re-runs the deploy script without `-Build`. That keeps
the patched jar and JNI DLL synced immediately before the JVM starts, which is
necessary because Prism may refresh the shared Beta client jar during launch.
The command is written with forward slashes because Prism stores instance
commands in an INI format that strips raw backslashes from Windows paths.

Restore the vanilla jar with:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\deploy-test-build.ps1 -Restore
```

The script keeps a one-time backup under `out/deploy-state`. Because PrismLauncher
uses a shared Beta 1.7.3 library jar, deployment affects any instance that points
at the same `com.mojang:minecraft:b1.7.3:client` artifact.
