FROM quay.io/pypa/manylinux_2_28_x86_64@sha256:531d7aa844bbb0c131d4ab011d3db741c4abc8d498cd5ccc86121046f62303b4

# Compile with Clang against glibc 2.28. GPU drivers remain host dependencies.
RUN dnf install -y clang vulkan-loader-devel wayland-devel wayland-protocols-devel \
      libxkbcommon-devel libXcursor-devel libXi-devel libXrandr-devel libXfixes-devel \
      libXext-devel libXrender-devel libXtst-devel libXScrnSaver-devel libusb1-devel alsa-lib-devel \
      pulseaudio-libs-devel libudev-devel && dnf clean all
ENV PATH="/opt/python/cp312-cp312/bin:${PATH}"
RUN python -m pip install --no-cache-dir meson==1.12.0 ninja==1.13.0
