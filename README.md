# BetaRT — direct Remix client

Minecraft Beta 1.7.3 with direct rendering through the Remix 0.1003 API.
SDL3 owns the window and input; Vulkan draws terrain, vanilla models, materials,
particles, skies, held items, menus and HUD. Unlit raster is the default; opt-in
Vulkan path tracing adds diffuse world lighting, distant-light shadows and basic
tinted transparency. Gameplay, networking and audio stay in the Java 8 client.

The patcher redirects vanilla geometry producers to CPU transforms and direct
resource/draw submissions. The packaged client has no OpenGL or GLFW dependency
and no capture-renderer fallback. Unchanged terrain and model resources persist;
only fresh game frames and explicit redraw requests are presented.

See [building and installation](BUILDING.md), [renderer design](docs/DirectRenderer.md)
and [terrain ownership](docs/TerrainPipeline.md). Linux x64 is the first supported
client target. Windows client packaging and gameplay validation are deferred.

F12 opens the native debug panel. Closing it restores gameplay mouse capture.
F2 queues a PNG screenshot in the instance's screenshots directory. Vanilla
menus, texture packs, key bindings, fullscreen and text editing use SDL input.

Set `MCRTX_RENDER_BACKEND=pathtrace` at launch, or select Path trace in F12.
Unsupported devices explain the missing capability and retain raster. Defaults are
native resolution, one sample per pixel and two indirect bounces, with accumulation
disabled. [Path-tracing controls, ownership and profiling](docs/PathTracing.md)
describe the paired client/JNI requirement and tested limits.

This project includes AI-assisted code. Historical path-tracing measurements are
in `docs/history`; they describe the removed capture renderer.

## Credits

- [sparkles](https://github.com/Kim2091) and [Remix Plus](https://github.com/RemixProjGroup/dxvk-remix).
- jacob for guidance and the original project logo/PBR pack.
- Minecraft and its original client remain Mojang's work; supply your own original JAR.
