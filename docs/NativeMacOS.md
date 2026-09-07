# Native macOS BetaRT

This integration uses the adjacent renderer's experimental arm64 MoltenVK package.
It retains a hidden LWJGL 3 OpenGL compatibility context and presents through
Remix's SDL3 window. Cocoa/GLFW window operations and Remix startup, frame
submission, event pumping, and shutdown are dispatched to the JVM launcher's
main run loop. Do not use `-XstartOnFirstThread`; the instance wrapper removes it.

The tested machine is an Apple M5 running macOS 27 beta. See the renderer's
`docs/native-macos.md` for its driver requirements and remaining compatibility
limits. BetaRT keeps RTXDI disabled on macOS. Unsupported DLSS features fall
back to TAAU; this target uses NRD rather than Ray Reconstruction.

## Build

Requirements: JDK 17 or newer, CMake, Ninja, Python 3, Homebrew Bash and coreutils,
and Prism's downloaded Beta 1.7.3/LWJGL 2/ASM dependency jars. Deployment also uses
Prism's LWJGL 3.3.3 core, GLFW, OpenGL, OpenAL and macOS arm64 native jars.
The Java client targets Java 8; the existing arm64 Prism Java 8 runtime works.

From the BetaRT repository root, with a new output directory:

```sh
python3 scripts-macos/build.py --output out/macos
ctest --test-dir out/macos/native-build --output-on-failure
python3 scripts-macos/deploy-instance.py \
  --client out/macos/client/minecraft-b1.7.3-client-mcrtx.jar \
  --bridge out/macos/native-build/native/libmcrtx_jni.dylib \
  --assets out/macos/client/mcrtx_assets \
  --runtime ../dxvk-remix-gmod/_package-macos
```

The build downloads checksum-verified ASM tree 9.6 and installs Pillow into an
output-local virtual environment. It exports DDS textures from the original jar
and runs vanilla texture-effect classes without a graphics context to generate
water, lava, portal, and fire animation frames. These use vanilla simulation
frames, not the Windows script's custom liquid simulation/normal maps. The water
normal atlas is flat. No Minecraft assets are committed to the repository.

Close the game and Prism before deploying. Deployment creates a timestamped
runtime bundle inside the instance, local client and LWJGL libraries, and a
backup of the original launcher metadata. It disables the LegacyFix agent, whose
LWJGL 2/de-AWT bytecode patches conflict with this client. It does not replace the globally
cached Minecraft jar. Launch `b1.7.3` normally in Prism afterward.

Each backup contains `restore.py`. Close the game and Prism, then run that script
to restore the original client/library selection and instance settings. Generated
bundles and local assets remain available but inactive after restoration.

## Verification on 2026-09-07

All nine native tests passed, including the standalone renderer test with a
macOS main dispatch queue. Java 8 behavior tests and terrain queue fixtures
passed; the existing `McrtxSourceOrganizationTest` fails because it expects
`appendDoubleSidedTexturedQuad` in `remix_geometry_common.cpp`, where the original
checkout does not define it.

A JNI/GLFW smoke test submitted 38 frames and shut down cleanly. A separate
vanilla-client smoke test reached the Minecraft title screen, submitted 954
frames before its screenshot request, saved a Remix screenshot, and shut down
cleanly. Its old Minecraft resource-download URL returned HTTP 404. These tests
do not establish world-rendering, multiplayer, or long-session stability.

The deployed Prism `b1.7.3` instance also reached its presentation loop with
OpenAL initialized after disabling LegacyFix. Runtime logs are saved as
`game-<timestamp>.log` inside the instance's `betart-macos-<timestamp>` bundle.

## macOS input integration

AWT can consume Cocoa events before SDL's polling loop sees them. The native
bridge installs a local Cocoa event monitor while Remix is running. Input
consumed outside SDL is deferred to the main dispatch queue and requeued through
the SDL pump exactly once, so SDL's
keyboard state and ImGui receive the same press, release, and modifier events.
The hidden GLFW window stops pumping Cocoa events once Remix is initialized.
Focus loss clears BetaRT's cached keys.

The renderer also needs the native `GetUIState` fix: it must return the live
ImGui menu state rather than the last value passed to `SetUIState`. Otherwise
opening Remix with Option+X does not suppress gameplay input correctly.

Enable the Cocoa/SDL regression test with
`-DMCRTX_BUILD_COCOA_INPUT_TESTS=ON`, rebuild, and run CTest. This test opens a
small window, uses no GPU rendering, and checks 20 alternating Cocoa/SDL
press/release cycles plus Option+X/B. Passing `--without-bridge` to the test
executable reproduces the lost-key failure. The optional Java/Remix test sources
are in `src/tests/macos`; they inject events only into their own Cocoa process.

Verified after the input fix: all ten BetaRT native tests and an end-to-end Java
input check passed. The Java check verifies 20 W press/release cycles without
duplicates, Option+X opening Remix, Option+B reaching the bridge, and modifier
release. These tests do not replace interactive gameplay testing.

The mouse-hang regression test also verifies that SDL is never pumped inline
from an AWT event callback. Inline pumping allowed Cocoa to service a queued
render callback while the event callback still held the renderer mutex, causing
a self-deadlock. Deferred callbacks are canceled when their renderer lifetime
ends. The test fails against the previous deployed bridge and passes against the
updated bridge. The Java integration check sent 500 mouse-motion events while
2,116 frames were submitted, and retained its keyboard/hotkey checks.

The macOS display shim reads the active SDL window state after initialization,
instead of hiding and querying the inactive GLFW host every Java frame. A live
sample of the previous client found 1,243 of 1,741 Minecraft-thread samples
waiting for GLFW main-queue dispatch, behind native rendering/presentation.
The Java input check now holds the Cocoa queue for 500 ms and verifies that
display polling completes within 350 ms; the updated client completed in 2 ms.
The input injector explicitly drains deferred key delivery before assertions,
so the test does not depend on incidental GLFW synchronization.

On macOS, initialized Remix also suppresses vanilla terrain display-list lookup
through the existing `dk.a(I)I` hook. The reported crash in `hs_err_pid14060.log`
occurred on the Minecraft thread in `tg.a` / `glCallLists`, inside Apple's
`GLDContextRec::setRenderProgramUniforms`. Terrain meshes are captured separately
for Remix; replaying their OpenGL display lists into the hidden compatibility
window is unnecessary. This guard leaves font display lists and pre-initialization
fallback behavior intact. The Java integration check covers the guard before
and after renderer initialization.

A separate seeded-world smoke run submitted 2,167 Remix frames over 60 seconds
in-world and exited normally, with no OpenGL error reports or native crash.
Terrain, entities, and the HUD remained visible in its captured screenshot.
