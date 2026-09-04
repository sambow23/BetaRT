## Building

### Requirements

- CMake 3.24 or newer
- JDK 9 or newer containing `java`, `javac`, and `jar`, **JDK 21 recommended**
- PrismLauncher with an existing vanilla Beta 1.7.3 instance
- [`dxvk-remix-gmod`](https://github.com/sambow23/dxvk-remix-gmod/tree/betart-numos3) next to the mc-rtx repository

Windows builds additionally require Visual Studio 2022 with the **Desktop
development with C++** workload and the Windows SDK. Linux builds require an
x86-64 host, Clang or GCC with C++20 support, Ninja, and a Vulkan-capable NVIDIA
driver. Only x86-64 native binaries are supported.

The scripts default to `%APPDATA%\PrismLauncher` and the `b1.7.3` instance. They
resolve the Minecraft, LWJGL, and ASM jars from PrismLauncher's library tree.
The Java toolchain is selected from `-JavaHome`, `JAVA_HOME`, or the first
complete JDK containing `javac` on `PATH`, in that order. The source is compiled
with `--release 8` so the patched client remains compatible with Java 8.


Tracy instrumentation is disabled by default. Configure with `-DMCRTX_ENABLE_TRACY=ON` to enable Tracy; it uses port
`8087` by default, or the port selected with `-DMCRTX_TRACY_PORT=<port>`.

### Build the Windows native bridge

From a Developer PowerShell prompt in the repository root:

```powershell
cmake -S . -B build `
  -G "Visual Studio 17 2022" `
  -A x64
```

### Build the native bridge

```powershell
cmake --build build --config Release --target mcrtx_jni
```

The resulting DLL is written to `build/native/Release/mcrtx_jni.dll`.

### Build the Linux native bridge and renderer

The Linux build uses the native Remix renderer. It does not build or load D3D9,
Wine, or the 32-bit bridge. First build and package the pinned renderer
dependencies from the adjacent `dxvk-remix-gmod` checkout:

```bash
DXVK_REMIX_ROOT=../dxvk-remix-gmod
"$DXVK_REMIX_ROOT/scripts-linux/build-dependencies.sh" out/remix-deps
"$DXVK_REMIX_ROOT/scripts-linux/build-remix.sh" \
  out/remix-build out/remix-deps/prefix
"$DXVK_REMIX_ROOT/scripts-linux/package-remix.sh" \
  out/remix-build out/remix-deps/prefix out/remix-runtime
```

Build the JNI bridge against that checkout, then assemble the colocated native
runtime directory:

```bash
./scripts-linux/build-native.sh build-linux "$DXVK_REMIX_ROOT" Release
./scripts-linux/package-native.sh \
  build-linux out/remix-runtime out/linux-native
```

`out/linux-native/` contains `libmcrtx_jni.so`, `libremix.so.0`, the pinned SDL3
shared library, and the pinned NVIDIA NGX feature libraries for DLSS Super
Resolution, Ray Reconstruction, and Frame Generation. The renderer uses an
`$ORIGIN` runpath, so these files can remain together without installing them
system-wide. Vulkan and the NGX driver components are loaded from the host's
NVIDIA driver. Feature availability still depends on the installed driver and
GPU; unsupported controls are disabled in BetaRT's graphics settings.

Place `libmcrtx_jni.so` next to the patched client jar, add its directory to
`java.library.path`, or set `MCRTX_JNI_PATH` to its absolute path. Keep the
other shared libraries beside it; alternatively, set `MCRTX_REMIX_DLL` to the
absolute `libremix.so.0` path. On Linux, Remix owns the SDL3 window and BetaRT
forwards that window's size, focus, keyboard, mouse, cursor-grab, fullscreen,
and close state to the legacy LWJGL-facing Java hooks.

The existing patched-client and PrismLauncher deployment scripts below are
PowerShell/Windows workflows. A jar produced by them is platform-independent;
for Linux, deploy that jar with the contents of `out/linux-native/` instead of
`mcrtx_jni.dll`.

### Build the patched client bundle

After building the native bridge:

```powershell
.\scripts\build-patched-client.ps1 -Configuration Release
```

This compiles the Java bridge for Java 8, patches the Beta 1.7.3 client jar,
generates the runtime texture assets, and stages the jar and native DLL under
`out/patched-client/`.

For a non-default PrismLauncher location, instance, or JDK:

```powershell
.\scripts\build-patched-client.ps1 `
  -Configuration Release `
  -PrismRoot "D:\PrismLauncher" `
  -InstanceName "b1.7.3" `
  -JavaHome "C:\Program Files\Java\jdk-21"
```

The individual dependency and Java executable parameters remain available for
unusual layouts.

### Build and deploy to PrismLauncher

Once the CMake build directory has been configured, the development deployment
script builds both parts and installs them into a PrismLauncher instance:

```powershell
.\scripts\deploy-test-build.ps1 -Configuration Release -Build
```

Use `-PrismRoot`, `-InstanceName`, or `-JavaHome` with the deployment command in
the same way. `-InstanceRoot` and `-MinecraftLibraryJar` remain available for
nonstandard layouts. The script keeps a one-time vanilla jar backup under
`out/deploy-state/`; restore it with:

```powershell
.\scripts\deploy-test-build.ps1 -Restore
```
