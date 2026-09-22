# BetaRT Portable Linux

In Prism Launcher, choose **Add Instance → Import**, then select the supplied ZIP.
Use your own Minecraft account. The instance opens at the main menu; create a world
or enter your server address normally.

This build targets **Linux x86_64 with glibc 2.28 or newer**, including CachyOS.
Prism needs **64-bit Java 8**. Enable automatic Java selection/download in Prism's
Java settings, or select a local Java 8 installation for this instance. Java, game
assets and LWJGL audio libraries are downloaded by Prism; the first launch needs
internet access. For Flatpak Prism, allow the normal graphics/display access and
use the graphics driver extension matching the host driver.

The machine needs its Vulkan loader and graphics driver. SDL3, the native bridge,
renderer, PBR assets, matching DLSS 310.9.1 runtime libraries and license notices
are included. Do not copy GPU drivers or system glibc libraries between machines.
The renderer and bridge include their C++ runtimes, so the host does not need the
build machine's version of libstdc++.

Path tracing requires Vulkan ray-query support. DLSS additionally needs supported
NVIDIA hardware and drivers. Automatic mode chooses DLSS with Ray Reconstruction,
then DLSS Super Resolution, then native rendering as capabilities permit. F12
shows the active mode, fallback reason and resolution. FSR remains selectable.
The default output size is 1920×1080, with DLSS Quality when available.

The instance does not require a launch wrapper, Python, MangoHud, KDE or a fixed
home-directory layout. Keep the JAR and native libraries together in `libraries/`.
Do not add absolute `MCRTX_JNI_PATH` or `MCRTX_REMIX_PATH` values to
`minecraft/mcrtx-runtime.env`. The older instance's “Remix API failed: 2” meant
the renderer library could not be loaded. This bridge reports the failing path
and the dynamic loader's explanation, such as a missing dependency or symbol
version. Include that full message when reporting a startup failure.

To export again, right-click this instance in Prism and select **Export**. Include
`libraries`, `patches`, `mmc-pack.json`, `instance.cfg` and the runtime settings.
The provided `.packignore` excludes worlds, screenshots, logs and caches by
default. Check the export selection and settings for any personal changes before
sharing. On a different machine, let Prism select its local Java 8 installation.

The supplied ZIP contains no accounts, worlds, server list, player statistics or
machine-specific renderer paths. `portable-manifest.json` records SHA-256 hashes
of the original package files; preferences may change during normal use.
