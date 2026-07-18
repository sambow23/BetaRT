## Building

### Requirements

- Visual Studio 2022 with the **Desktop development with C++** workload and the Windows SDK
- CMake 3.24 or newer
- JDK 9 or newer containing `java`, `javac`, and `jar`, **JDK 21 recommended**
- PrismLauncher with an existing vanilla Beta 1.7.3 instance
- [`dxvk-remix-gmod`](https://github.com/sambow23/dxvk-remix-gmod/tree/b173-upd) next to the mc-rtx repository

The scripts default to `%APPDATA%\PrismLauncher` and the `b1.7.3` instance. They
resolve the Minecraft, LWJGL, and ASM jars from PrismLauncher's library tree.
The Java toolchain is selected from `-JavaHome`, `JAVA_HOME`, or the first
complete JDK containing `javac` on `PATH`, in that order. The source is compiled
with `--release 8` so the patched client remains compatible with Java 8.


Tracy instrumentation is disabled by default. Configure with `-DMCRTX_ENABLE_TRACY=ON` to enable Tracy; it uses port
`8087` by default, or the port selected with `-DMCRTX_TRACY_PORT=<port>`.

### Configure the native build

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