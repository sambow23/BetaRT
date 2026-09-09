# Native raster performance

The F3 counters measure different work. **Java FPS** is Minecraft's game/render
loop, including scene capture and JNI submission. **Remix FPS** counts native
presentations. The native worker can render the last published game snapshot
several times, so a higher Remix count does not mean the game supplied that many
new camera, entity or HUD updates. Java FPS is not the simulation tick rate.

For the unlit raster runtime in `single-native` window mode, these existing
settings avoid duplicate vanilla world and sign drawing:

```ini
MCRTX_WINDOW_MODE=single-native
MCRTX_WORLD_RASTER_VANILLA_SUPPRESSION_ENABLED=1
MCRTX_SIGN_VANILLA_SUPPRESSION_ENABLED=1
```

Set them in the instance's `minecraft/mcrtx-runtime.env` while the client is
closed. File values take precedence over environment variables. These settings
keep the Java capture hooks running; they suppress the associated vanilla draw
calls after capture. Sign model suppression also avoids capturing its model parts
again through the vanilla fallback. Sign capture and sign-text capture should
remain enabled. This configuration was checked against the native raster runtime;
other presentation modes still need their own visual checks.

`RemixSignTextMesh` caches local glyph geometry and sends one JNI quad batch per
nonempty line. Color and bone transforms remain per-frame values. The cache keeps
at most 256 entries / 8 MiB of vertex data, and invalidates on font-mask identity
or character-width changes. It preserves the existing per-pixel geometry when an
atlas mask is available and the textured-glyph fallback otherwise. Text, placement,
color, and transforms can still change each frame.

In the 2026-09-08 dense-scene test on an RTX 2080, the combined configuration and
batching change increased fresh Java publications from 31.5 to 73.3 per second.
Native presentation increased from 78.5 to 92.8 FPS. These are 60-second averages
after 45-second warm-ups at 1920×1080, with the detailed profiler disabled. Short
F3/overlay readings were often higher. The comparison is stationary and does not
establish performance while traversing the world or loading chunks.

The sign geometry/cache regression test and the complete Java test script passed
on Java 8. Coverage includes glyph positions/UVs/winding, character spacing,
format-code skipping, cache reuse/eviction, changed font metrics/masks, and missing
pixel-data fallback. The existing native runtime/JNI ABI is unchanged.

For timing investigations, `MCRTX_PERF=1` enables the detailed hook/native profiler,
but its many per-quad samples affect the workload. Use it to locate expensive
functions, then measure final throughput with `MCRTX_PERF=0` and separate counts
for game publications and native presents. Remaining targets include GL state
queries, native snapshot preparation, transient allocation reuse, and redundant
lighting work when using an unlit runtime.
