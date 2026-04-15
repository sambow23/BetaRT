# Beta 1.7.3 Hook Points

This report maps the first concrete render seams in the CFR decompilation output.

## Primary bootstrap

File: `work/decompiled-cfr/net/minecraft/client/Minecraft.java`

1. `Minecraft.a()` creates or attaches the LWJGL display.
2. `Display.setParent(this.m)` is used when running embedded in an AWT `Canvas`.
3. `Display.create()` is the main LWJGL display bootstrap call.
4. `this.g = new n(this, this.p)` creates the world and chunk renderer.
5. `this.j = new dn(this.f, this.p)` creates the particle manager.
6. `this.t = new px(this)` creates the camera and world render controller.

## Main loop

File: `work/decompiled-cfr/net/minecraft/client/Minecraft.java`

1. `Minecraft.run()` owns the main tick and render loop.
2. `this.k()` is the per-tick update path.
3. `this.t.b(this.T.c)` is the main world render call.
4. Canvas resize is detected after rendering and forwarded to `this.a(this.d, this.e)`.

## Camera and world pass

File: `work/decompiled-cfr/px.java`

1. `a(float)` updates the current camera ray pick and view target.
2. `g(float)` computes the interpolated camera transform.
3. `b(float)` is the outer world render path.
4. `this.j.g.a(sr2, f2)` prepares the world renderer using the current frustum.
5. `n3.a(ls2, 0, (double)f2)` renders opaque terrain.
6. `n3.a(ls2.e(f2), sr2, f2)` renders entities.
7. `dn2.a(ls2, f2)` and `dn2.b(ls2, f2)` render particles.
8. `n3.a(ls2, 1, (double)f2)` renders the transparent terrain pass.

## World and chunk renderer

File: `work/decompiled-cfr/n.java`

1. The constructor allocates render lists and sky display lists.
2. `a()` rebuilds the active chunk renderer grid.
3. `a(ls, int, double)` issues world render passes and visibility work.
4. `a(bt, yn, float)` renders entities and tile-like objects visible in the frustum.
5. `d()` advances the chunk renderer update scheduler.

## Chunk compilation seam

File: `work/decompiled-cfr/dk.java`

1. `a()` rebuilds one chunk renderer.
2. The rebuild iterates blocks in a 16x16x16 region.
3. The rebuild uses `cv2.b(uu2, i6, i4, i5)` to emit block geometry.
4. Geometry currently goes into the global tessellator `nw.a` and then into GL display lists.

This is the preferred interception seam for terrain porting because block faces are already resolved before OpenGL submission.

## Particle seam

File: `work/decompiled-cfr/dn.java`

1. `a(sn, float)` renders particle layers 0-2.
2. `b(sn, float)` renders layer 3.
3. `a(int, int, int, int, int)` and `a(int, int, int, int)` spawn block-hit and block-break particles.

## Immediate next implementation target

The first Java integration should not try to mirror raw GL calls. Instead:

1. Capture the existing LWJGL window handle after `Display.create()`.
2. Call into JNI from the outer render loop before or in place of `this.t.b(this.T.c)`.
3. Replace chunk display-list generation by intercepting `dk.a()` or the block-geometry path it drives.
4. Feed camera data derived from `px.g(float)` into the native renderer.
