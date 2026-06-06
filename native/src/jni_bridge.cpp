#include "mcrtx/remix_renderer.hpp"
#include "mcrtx/perf_log.hpp"
#include "mcrtx/render_internals.hpp"
#include "mcrtx/tracy.hpp"

#if defined(MCRTX_ENABLE_TRACY)
#include <winsock2.h>
#include <ws2tcpip.h>
#endif

#include <jni.h>
#include <jawt.h>
#include <jawt_md.h>

#include <cstdint>
#include <fstream>
#include <string>

namespace {

using mcrtx::CameraState;
using mcrtx::RemixRenderer;

#if defined(MCRTX_ENABLE_TRACY)
constexpr std::uint16_t kCompiledTracyPort = TRACY_PORT;

std::string readProcessEnvironmentVariable(const char* name) {
  char* value = nullptr;
  std::size_t length = 0;
  if (_dupenv_s(&value, &length, name) != 0 || value == nullptr || length == 0) {
    return {};
  }

  std::string result(value);
  std::free(value);
  return result;
}

std::uint16_t parsePortOrZero(const std::string& value) {
  if (value.empty()) {
    return 0;
  }

  try {
    const unsigned long parsed = std::stoul(value);
    if (parsed > 0 && parsed <= 65535) {
      return static_cast<std::uint16_t>(parsed);
    }
  } catch (...) {
  }

  return 0;
}

bool canConnectToLoopbackPort(std::uint16_t port) {
  if (port == 0) {
    return false;
  }

  WSADATA wsaData {};
  if (WSAStartup(MAKEWORD(2, 2), &wsaData) != 0) {
    return false;
  }

  SOCKET socketHandle = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
  if (socketHandle == INVALID_SOCKET) {
    WSACleanup();
    return false;
  }

  sockaddr_in address {};
  address.sin_family = AF_INET;
  address.sin_port = htons(port);
  address.sin_addr.s_addr = htonl(INADDR_LOOPBACK);

  const bool connected = connect(
      socketHandle,
      reinterpret_cast<const sockaddr*>(&address),
      static_cast<int>(sizeof(address))) == 0;

  closesocket(socketHandle);
  WSACleanup();
  return connected;
}

void appendTracyDiagnostic(const char* phase, bool initializeResult) {
  const std::string configuredPort = mcrtx::detail::readEnvironmentVariable("TRACY_PORT");
  const std::string processEnvPort = readProcessEnvironmentVariable("TRACY_PORT");
  const std::uint16_t envPort = parsePortOrZero(processEnvPort);
  const std::uint16_t effectivePort = envPort != 0 ? envPort : kCompiledTracyPort;
  const bool listenerReachable = canConnectToLoopbackPort(effectivePort);

  std::filesystem::path logPath = mcrtx::detail::getRuntimeConfigPath().parent_path();
  if (logPath.empty()) {
    logPath = std::filesystem::current_path();
  }
  logPath /= "mcrtx-tracy.log";

  std::ofstream stream(logPath, std::ios::out | std::ios::app);
  if (!stream.is_open()) {
    return;
  }

  stream
      << "phase=" << phase
      << " pid=" << GetCurrentProcessId()
      << " initOk=" << (initializeResult ? 1 : 0)
      << " compiledPort=" << kCompiledTracyPort
      << " runtimeConfigOrEnvPort=" << (configuredPort.empty() ? "<empty>" : configuredPort)
      << " processEnvPort=" << (processEnvPort.empty() ? "<empty>" : processEnvPort)
      << " effectivePort=" << effectivePort
      << " listenerReachable=" << (listenerReachable ? 1 : 0)
      << " connected=" << (TracyIsConnected ? 1 : 0)
      << '\n';
}
#endif

bool fromJniBoolean(bool value) {
  return value;
}

HWND fromJniHandle(jlong handle) {
  return reinterpret_cast<HWND>(static_cast<intptr_t>(handle));
}

jlong toJniHandle(HWND hwnd) {
  return static_cast<jlong>(reinterpret_cast<intptr_t>(hwnd));
}

HWND resolveAwtWindowHandle(JNIEnv* env, jobject component) {
  if (env == nullptr || component == nullptr) {
    return nullptr;
  }

  JAWT awt {};
  awt.version = JAWT_VERSION_1_4;
  if (JAWT_GetAWT(env, &awt) == JNI_FALSE) {
    return nullptr;
  }

  JAWT_DrawingSurface* drawingSurface = awt.GetDrawingSurface(env, component);
  if (drawingSurface == nullptr) {
    return nullptr;
  }

  HWND hwnd = nullptr;
  const jint lock = drawingSurface->Lock(drawingSurface);
  if ((lock & JAWT_LOCK_ERROR) == 0) {
    JAWT_DrawingSurfaceInfo* drawingSurfaceInfo = drawingSurface->GetDrawingSurfaceInfo(drawingSurface);
    if (drawingSurfaceInfo != nullptr) {
      auto* win32Info = static_cast<JAWT_Win32DrawingSurfaceInfo*>(drawingSurfaceInfo->platformInfo);
      if (win32Info != nullptr) {
        hwnd = win32Info->hwnd;
      }
      drawingSurface->FreeDrawingSurfaceInfo(drawingSurfaceInfo);
    }
    drawingSurface->Unlock(drawingSurface);
  }

  awt.FreeDrawingSurface(drawingSurface);
  return hwnd;
}

bool embedCompatibilityWindow(HWND childWindow, HWND parentWindow, int width, int height) {
  if (childWindow == nullptr || parentWindow == nullptr) {
    return false;
  }
  if (!IsWindow(childWindow) || !IsWindow(parentWindow)) {
    return false;
  }

  SetLastError(0);
  const HWND previousParent = SetParent(childWindow, parentWindow);
  if (previousParent == nullptr && GetLastError() != 0) {
    return false;
  }

  LONG_PTR style = GetWindowLongPtrW(childWindow, GWL_STYLE);
  style &= ~(WS_POPUP | WS_CAPTION | WS_THICKFRAME | WS_MINIMIZE | WS_MAXIMIZE | WS_SYSMENU);
  style |= WS_CHILD | WS_CLIPSIBLINGS | WS_CLIPCHILDREN;
  SetWindowLongPtrW(childWindow, GWL_STYLE, style);

  LONG_PTR exStyle = GetWindowLongPtrW(childWindow, GWL_EXSTYLE);
  exStyle &= ~(WS_EX_APPWINDOW | WS_EX_TOPMOST);
  exStyle |= WS_EX_NOPARENTNOTIFY;
  SetWindowLongPtrW(childWindow, GWL_EXSTYLE, exStyle);

  const int embeddedWidth = width > 0 ? width : 1;
  const int embeddedHeight = height > 0 ? height : 1;
  if (!SetWindowPos(
          childWindow,
          HWND_TOP,
          0,
          0,
          embeddedWidth,
          embeddedHeight,
          SWP_FRAMECHANGED | SWP_SHOWWINDOW)) {
    return false;
  }

  UpdateWindow(childWindow);
  return true;
}

bool focusWindow(HWND window) {
  if (window == nullptr || !IsWindow(window)) {
    return false;
  }

  HWND rootWindow = GetAncestor(window, GA_ROOT);
  if (rootWindow != nullptr) {
    SetActiveWindow(rootWindow);
  }

  SetFocus(window);
  return GetFocus() == window;
}

bool matchesManagedWindow(HWND managedWindow, HWND candidateWindow) {
  if (managedWindow == nullptr || candidateWindow == nullptr) {
    return false;
  }

  if (candidateWindow == managedWindow || IsChild(managedWindow, candidateWindow)) {
    return true;
  }

  const HWND managedRoot = GetAncestor(managedWindow, GA_ROOT);
  const HWND candidateRoot = GetAncestor(candidateWindow, GA_ROOT);
  return managedRoot != nullptr && managedRoot == candidateRoot;
}

bool isEmbeddedWindowActive(HWND childWindow, HWND parentWindow) {
  const auto matchesAnyManagedWindow = [childWindow, parentWindow](HWND candidateWindow) {
    return matchesManagedWindow(childWindow, candidateWindow)
        || matchesManagedWindow(parentWindow, candidateWindow);
  };

  if (matchesAnyManagedWindow(GetForegroundWindow())) {
    return true;
  }

  GUITHREADINFO guiThreadInfo {};
  guiThreadInfo.cbSize = sizeof(guiThreadInfo);
  if (!GetGUIThreadInfo(0, &guiThreadInfo)) {
    return false;
  }

  return matchesAnyManagedWindow(guiThreadInfo.hwndActive)
      || matchesAnyManagedWindow(guiThreadInfo.hwndFocus)
      || matchesAnyManagedWindow(guiThreadInfo.hwndCapture);
}

}  // namespace

extern "C" {

JNIEXPORT jboolean JNICALL Java_mcrtx_bridge_RemixBridgeNative_nInitialize(
    JNIEnv*, jclass, jlong hwnd, jint width, jint height) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nInitialize");
  MCRTX_TRACY_SCOPE("nInitialize");
  MCRTX_TRACY_SET_THREAD_NAME("mc-rtx JNI/Main");
  auto& renderer = RemixRenderer::instance();
  const bool ok = renderer.initialize(
      reinterpret_cast<HWND>(static_cast<intptr_t>(hwnd)),
      static_cast<std::uint32_t>(width),
      static_cast<std::uint32_t>(height));
#if defined(MCRTX_ENABLE_TRACY)
  appendTracyDiagnostic("nInitialize", ok);
#endif
  return static_cast<jboolean>(fromJniBoolean(ok));
}

JNIEXPORT jlong JNICALL Java_mcrtx_bridge_RemixBridgeNative_nResolveAwtWindowHandle(
    JNIEnv* env, jclass, jobject canvas) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nResolveAwtWindowHandle");
  return toJniHandle(resolveAwtWindowHandle(env, canvas));
}

JNIEXPORT jboolean JNICALL Java_mcrtx_bridge_RemixBridgeNative_nEmbedCompatibilityWindow(
    JNIEnv*, jclass, jlong childHwnd, jlong parentHwnd, jint width, jint height) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nEmbedCompatibilityWindow");
  const bool ok = embedCompatibilityWindow(
      fromJniHandle(childHwnd),
      fromJniHandle(parentHwnd),
      static_cast<int>(width),
      static_cast<int>(height));
  return static_cast<jboolean>(fromJniBoolean(ok));
}

JNIEXPORT jboolean JNICALL Java_mcrtx_bridge_RemixBridgeNative_nFocusWindow(
    JNIEnv*, jclass, jlong hwnd) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nFocusWindow");
  return static_cast<jboolean>(fromJniBoolean(focusWindow(fromJniHandle(hwnd))));
}

JNIEXPORT jboolean JNICALL Java_mcrtx_bridge_RemixBridgeNative_nIsEmbeddedWindowActive(
    JNIEnv*, jclass, jlong childHwnd, jlong parentHwnd) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nIsEmbeddedWindowActive");
  return static_cast<jboolean>(fromJniBoolean(
      isEmbeddedWindowActive(fromJniHandle(childHwnd), fromJniHandle(parentHwnd))));
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nShutdown(JNIEnv*, jclass) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nShutdown");
  MCRTX_TRACY_SCOPE("nShutdown");
#if defined(MCRTX_ENABLE_TRACY)
  appendTracyDiagnostic("nShutdown", true);
#endif
  RemixRenderer::instance().shutdown();
  ::mcrtx::perf::shutdown();
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nResize(
    JNIEnv*, jclass, jint width, jint height) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nResize");
  RemixRenderer::instance().resize(static_cast<std::uint32_t>(width), static_cast<std::uint32_t>(height));
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nUpdateCamera(
    JNIEnv*, jclass,
    jfloat px, jfloat py, jfloat pz,
    jfloat fx, jfloat fy, jfloat fz,
    jfloat ux, jfloat uy, jfloat uz,
    jfloat rx, jfloat ry, jfloat rz,
    jfloat fovYDegrees,
    jfloat aspect,
    jfloat nearPlane,
    jfloat farPlane) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nUpdateCamera");
  CameraState camera;
  camera.position[0] = px;
  camera.position[1] = py;
  camera.position[2] = pz;
  camera.forward[0] = fx;
  camera.forward[1] = fy;
  camera.forward[2] = fz;
  camera.up[0] = ux;
  camera.up[1] = uy;
  camera.up[2] = uz;
  camera.right[0] = rx;
  camera.right[1] = ry;
  camera.right[2] = rz;
  camera.fovYDegrees = fovYDegrees;
  camera.aspect = aspect;
  camera.nearPlane = nearPlane;
  camera.farPlane = farPlane;
  RemixRenderer::instance().updateCamera(camera);
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nUpdateCloudLayer(
    JNIEnv*, jclass,
    jboolean fancy,
    jfloat cameraX,
    jfloat cameraY,
    jfloat cameraZ,
    jfloat cloudHeight,
    jfloat cloudScroll,
    jfloat celestialAngle,
    jfloat colorR,
    jfloat colorG,
    jfloat colorB) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nUpdateCloudLayer");
  RemixRenderer::instance().updateCloudLayer(
      fancy == JNI_TRUE,
      cameraX,
      cameraY,
      cameraZ,
      cloudHeight,
      cloudScroll,
      celestialAngle,
      colorR,
      colorG,
      colorB);
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nUpdateAtmosphereState(
    JNIEnv*, jclass, jfloat celestialAngle, jboolean forceDarkAtmosphere) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nUpdateAtmosphereState");
  RemixRenderer::instance().updateAtmosphereState(celestialAngle, forceDarkAtmosphere == JNI_TRUE);
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nUpdateFogState(
    JNIEnv*, jclass,
    jint fogMode,
    jfloat colorR,
    jfloat colorG,
    jfloat colorB,
    jfloat fogScale,
    jfloat fogEnd,
    jfloat fogDensity) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nUpdateFogState");
  RemixRenderer::instance().updateFogState(
      static_cast<std::uint32_t>(fogMode),
      colorR,
      colorG,
      colorB,
      fogScale,
      fogEnd,
      fogDensity);
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nClearCloudLayer(JNIEnv*, jclass) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nClearCloudLayer");
  RemixRenderer::instance().clearCloudLayer();
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nBeginDynamicEntityFrame(JNIEnv*, jclass) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nBeginDynamicEntityFrame");
  RemixRenderer::instance().beginDynamicEntityFrame();
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nBeginDynamicEntity(
    JNIEnv*, jclass, jint entityId, jint hurtStage, jint creeperFuseStage) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nBeginDynamicEntity");
  const std::uint32_t clampedHurtStage = hurtStage < 0 ? 0u : static_cast<std::uint32_t>(hurtStage);
  const std::uint32_t clampedCreeperFuseStage = creeperFuseStage < 0 ? 0u : static_cast<std::uint32_t>(creeperFuseStage);
  RemixRenderer::instance().beginDynamicEntity(
      static_cast<int>(entityId),
      clampedHurtStage,
      clampedCreeperFuseStage);
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nSetDynamicEntityTexture(
    JNIEnv* env, jclass, jstring texturePath) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nSetDynamicEntityTexture");
  if (texturePath == nullptr) {
    return;
  }

  const char* utfChars = env->GetStringUTFChars(texturePath, nullptr);
  if (utfChars == nullptr) {
    return;
  }

  RemixRenderer::instance().setDynamicEntityTexture(utfChars);
  env->ReleaseStringUTFChars(texturePath, utfChars);
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nSetFirstPersonHeldItem(
    JNIEnv*, jclass, jint itemId) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nSetFirstPersonHeldItem");
  RemixRenderer::instance().setFirstPersonHeldItem(itemId);
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nSetEntityHeldTorch(
    JNIEnv*, jclass, jint entityId, jfloat worldX, jfloat worldY, jfloat worldZ, jint itemId) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nSetEntityHeldTorch");
  RemixRenderer::instance().setEntityHeldTorch(entityId, worldX, worldY, worldZ, itemId);
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nSetPlayerShadowsEnabled(
    JNIEnv*, jclass, jboolean enabled) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nSetPlayerShadowsEnabled");
  RemixRenderer::instance().setPlayerShadowsEnabled(enabled == JNI_TRUE);
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nSetHeldTorchLightsEnabled(
    JNIEnv*, jclass, jboolean enabled) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nSetHeldTorchLightsEnabled");
  RemixRenderer::instance().setHeldTorchLightsEnabled(enabled == JNI_TRUE);
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nSetDynamicEntityRenderingEnabled(
    JNIEnv*, jclass, jboolean enabled) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nSetDynamicEntityRenderingEnabled");
  RemixRenderer::instance().setDynamicEntityRenderingEnabled(enabled == JNI_TRUE);
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nSetBlockOutlineEnabled(
    JNIEnv*, jclass, jboolean enabled) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nSetBlockOutlineEnabled");
  RemixRenderer::instance().setBlockOutlineEnabled(enabled == JNI_TRUE);
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nSetBlockOutlineStyle(
    JNIEnv*, jclass, jint style) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nSetBlockOutlineStyle");
  RemixRenderer::instance().setBlockOutlineStyle(static_cast<int>(style));
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nSetBlockOutlineEmissiveIntensity(
    JNIEnv*, jclass, jfloat intensity) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nSetBlockOutlineEmissiveIntensity");
  RemixRenderer::instance().setBlockOutlineEmissiveIntensity(intensity);
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nSetViewModelFovDegrees(
    JNIEnv*, jclass, jfloat fovYDegrees) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nSetViewModelFovDegrees");
  RemixRenderer::instance().setViewModelFovDegrees(fovYDegrees);
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nSetRtQuality(
    JNIEnv*, jclass, jint rtQuality) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nSetRtQuality");
  RemixRenderer::instance().setRtQuality(static_cast<int>(rtQuality));
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nSetUpscalerConfig(
    JNIEnv*, jclass, jint upscalerType, jint dlssPreset, jint xessPreset, jint taauPreset, jboolean rayReconstructionEnabled) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nSetUpscalerConfig");
  RemixRenderer::instance().setUpscalerConfig(
      static_cast<int>(upscalerType),
      static_cast<int>(dlssPreset),
      static_cast<int>(xessPreset),
      static_cast<int>(taauPreset),
      rayReconstructionEnabled == JNI_TRUE);
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nSetDynamicEntityBoneTransform(
    JNIEnv*, jclass,
    jint boneIndex,
    jfloat m00, jfloat m01, jfloat m02, jfloat m03,
    jfloat m10, jfloat m11, jfloat m12, jfloat m13,
    jfloat m20, jfloat m21, jfloat m22, jfloat m23) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nSetDynamicEntityBoneTransform");
  if (boneIndex < 0) {
    return;
  }

  remixapi_Transform transform {};
  transform.matrix[0][0] = m00;
  transform.matrix[0][1] = m01;
  transform.matrix[0][2] = m02;
  transform.matrix[0][3] = m03;
  transform.matrix[1][0] = m10;
  transform.matrix[1][1] = m11;
  transform.matrix[1][2] = m12;
  transform.matrix[1][3] = m13;
  transform.matrix[2][0] = m20;
  transform.matrix[2][1] = m21;
  transform.matrix[2][2] = m22;
  transform.matrix[2][3] = m23;
  RemixRenderer::instance().setDynamicEntityBoneTransform(static_cast<std::uint32_t>(boneIndex), transform);
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nCaptureDynamicEntityQuad(
    JNIEnv*, jclass,
    jfloat x0, jfloat y0, jfloat z0, jfloat u0, jfloat v0,
    jfloat x1, jfloat y1, jfloat z1, jfloat u1, jfloat v1,
    jfloat x2, jfloat y2, jfloat z2, jfloat u2, jfloat v2,
    jfloat x3, jfloat y3, jfloat z3, jfloat u3, jfloat v3,
    jint colorRgba,
    jboolean blendEnabled,
    jint boneIndex) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nCaptureDynamicEntityQuad");
  if (boneIndex < 0) {
    return;
  }

  RemixRenderer::instance().captureDynamicEntityQuad(
      x0, y0, z0, u0, v0,
      x1, y1, z1, u1, v1,
      x2, y2, z2, u2, v2,
      x3, y3, z3, u3, v3,
      static_cast<std::uint32_t>(colorRgba),
      blendEnabled == JNI_TRUE,
      static_cast<std::uint32_t>(boneIndex));
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nCaptureDynamicEntityQuadBatch(
    JNIEnv* env, jclass,
    jfloatArray vertices,
    jint quadCount,
    jint colorRgba,
    jboolean blendEnabled,
    jint boneIndex) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nCaptureDynamicEntityQuadBatch");
  if (boneIndex < 0 || quadCount <= 0 || vertices == nullptr) {
    return;
  }

  const jsize available = env->GetArrayLength(vertices);
  const jlong required = static_cast<jlong>(quadCount) * 20;
  if (required > available) {
    return;
  }

  auto* data = static_cast<jfloat*>(env->GetPrimitiveArrayCritical(vertices, nullptr));
  if (data == nullptr) {
    return;
  }

  RemixRenderer::instance().captureDynamicEntityQuadBatch(
      reinterpret_cast<const float*>(data),
      static_cast<std::uint32_t>(quadCount),
      static_cast<std::uint32_t>(colorRgba),
      blendEnabled == JNI_TRUE,
      static_cast<std::uint32_t>(boneIndex));

  env->ReleasePrimitiveArrayCritical(vertices, data, JNI_ABORT);
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nEndDynamicEntity(JNIEnv*, jclass) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nEndDynamicEntity");
  RemixRenderer::instance().endDynamicEntity();
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nBeginDestroyOverlayFrame(JNIEnv*, jclass) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nBeginDestroyOverlayFrame");
  RemixRenderer::instance().beginDestroyOverlayFrame();
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nBeginBlockOutlineFrame(JNIEnv*, jclass) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nBeginBlockOutlineFrame");
  RemixRenderer::instance().beginBlockOutlineFrame();
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nCaptureDestroyOverlay(
    JNIEnv*, jclass,
    jint blockX,
    jint blockY,
    jint blockZ,
    jint blockId,
    jint blockMetadata,
    jint renderType,
    jint destroyStage) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nCaptureDestroyOverlay");
  RemixRenderer::instance().captureDestroyOverlay(
      blockX,
      blockY,
      blockZ,
      blockId,
      blockMetadata,
      renderType,
      destroyStage);
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nCaptureBlockOutline(
    JNIEnv*, jclass,
    jint blockX,
    jint blockY,
    jint blockZ) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nCaptureBlockOutline");
  RemixRenderer::instance().captureBlockOutline(blockX, blockY, blockZ);
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nBeginParticleFrame(JNIEnv*, jclass) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nBeginParticleFrame");
  RemixRenderer::instance().beginParticleFrame();
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nCaptureParticleQuad(
    JNIEnv*, jclass,
    jfloat x0, jfloat y0, jfloat z0, jfloat u0, jfloat v0,
    jfloat x1, jfloat y1, jfloat z1, jfloat u1, jfloat v1,
    jfloat x2, jfloat y2, jfloat z2, jfloat u2, jfloat v2,
    jfloat x3, jfloat y3, jfloat z3, jfloat u3, jfloat v3,
    jint colorRgba,
    jint textureKind) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nCaptureParticleQuad");
  if (textureKind < 0) {
    return;
  }

  RemixRenderer::instance().captureParticleQuad(
      x0, y0, z0, u0, v0,
      x1, y1, z1, u1, v1,
      x2, y2, z2, u2, v2,
      x3, y3, z3, u3, v3,
      static_cast<std::uint32_t>(colorRgba),
      static_cast<std::uint32_t>(textureKind));
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nClearWorldScene(JNIEnv*, jclass) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nClearWorldScene");
  RemixRenderer::instance().clearWorldScene();
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nUnloadChunkSection(
    JNIEnv*, jclass, jint originX, jint originY, jint originZ) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nUnloadChunkSection");
  RemixRenderer::instance().unloadChunkSection(
      static_cast<int>(originX),
      static_cast<int>(originY),
      static_cast<int>(originZ));
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nSetChunkSectionHidden(
  JNIEnv*, jclass, jint originX, jint originY, jint originZ, jboolean hidden) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nSetChunkSectionHidden");
  RemixRenderer::instance().setChunkSectionHidden(
    static_cast<int>(originX),
    static_cast<int>(originY),
    static_cast<int>(originZ),
    hidden != JNI_FALSE);
}

JNIEXPORT jboolean JNICALL Java_mcrtx_bridge_RemixBridgeNative_nBeginChunkBuild(
    JNIEnv*, jclass,
    jint originX, jint originY, jint originZ,
    jint sizeX, jint sizeY, jint sizeZ,
    jint dirtyMinX, jint dirtyMinY, jint dirtyMinZ,
    jint dirtyMaxX, jint dirtyMaxY, jint dirtyMaxZ,
    jint renderPass) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nBeginChunkBuild");
  const bool ok = RemixRenderer::instance().beginChunkBuild(
      originX,
      originY,
      originZ,
      sizeX,
      sizeY,
      sizeZ,
      dirtyMinX,
      dirtyMinY,
      dirtyMinZ,
      dirtyMaxX,
      dirtyMaxY,
      dirtyMaxZ,
      renderPass);
  return static_cast<jboolean>(fromJniBoolean(ok));
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nCaptureBlock(
    JNIEnv*, jclass,
    jint blockX, jint blockY, jint blockZ,
    jint blockId, jint blockMetadata, jint renderType,
    jint texture0, jint texture1, jint texture2,
  jint texture3, jint texture4, jint texture5,
  jfloat boundsMinX,
  jfloat boundsMinY,
  jfloat boundsMinZ,
  jfloat boundsMaxX,
  jfloat boundsMaxY,
  jfloat boundsMaxZ,
  jint blockColorRgb,
  jint liquidVisibilityMask,
  jfloat liquidHeight0,
  jfloat liquidHeight1,
  jfloat liquidHeight2,
  jfloat liquidHeight3,
  jfloat liquidFlowAngle) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nCaptureBlock");
  RemixRenderer::instance().captureBlock(
      blockX,
      blockY,
      blockZ,
      blockId,
      blockMetadata,
      renderType,
      texture0,
      texture1,
      texture2,
      texture3,
      texture4,
      texture5,
      boundsMinX,
      boundsMinY,
      boundsMinZ,
      boundsMaxX,
      boundsMaxY,
      boundsMaxZ,
      blockColorRgb,
      liquidVisibilityMask,
      liquidHeight0,
      liquidHeight1,
      liquidHeight2,
      liquidHeight3,
      liquidFlowAngle);
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nEndChunkBuild(
    JNIEnv*, jclass, jboolean emittedGeometry, jboolean deferNeighborRefresh, jboolean allowNeighborRefresh) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nEndChunkBuild");
  RemixRenderer::instance().endChunkBuild(
      emittedGeometry == JNI_TRUE,
      deferNeighborRefresh == JNI_TRUE,
      allowNeighborRefresh == JNI_TRUE);
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nFlushChunkNeighborRefreshes(
    JNIEnv*, jclass) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nFlushChunkNeighborRefreshes");
  RemixRenderer::instance().flushChunkNeighborRefreshes();
}

JNIEXPORT jboolean JNICALL Java_mcrtx_bridge_RemixBridgeNative_nDrawScreenOverlay(
    JNIEnv* env,
    jclass,
    jobject pixelBuffer,
    jint width,
    jint height,
    jint format,
    jfloat opacity) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nDrawScreenOverlay");
  if (pixelBuffer == nullptr) {
    return static_cast<jboolean>(fromJniBoolean(false));
  }

  void* pixelData = env->GetDirectBufferAddress(pixelBuffer);
  if (pixelData == nullptr) {
    return static_cast<jboolean>(fromJniBoolean(false));
  }

  const bool ok = RemixRenderer::instance().drawScreenOverlay(
      pixelData,
      static_cast<std::uint32_t>(width),
      static_cast<std::uint32_t>(height),
      static_cast<remixapi_Format>(format),
      opacity);
  return static_cast<jboolean>(fromJniBoolean(ok));
}

JNIEXPORT jboolean JNICALL Java_mcrtx_bridge_RemixBridgeNative_nClearScreenOverlay(JNIEnv*, jclass) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nClearScreenOverlay");
  return static_cast<jboolean>(fromJniBoolean(RemixRenderer::instance().clearScreenOverlay()));
}

JNIEXPORT jboolean JNICALL Java_mcrtx_bridge_RemixBridgeNative_nRegisterUiTexture(
    JNIEnv* env,
    jclass,
    jobject pixelBuffer,
    jlong id,
    jint width,
    jint height,
    jint format) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nRegisterUiTexture");
  if (pixelBuffer == nullptr || width <= 0 || height <= 0) {
    return static_cast<jboolean>(fromJniBoolean(false));
  }

  void* pixelData = env->GetDirectBufferAddress(pixelBuffer);
  if (pixelData == nullptr) {
    return static_cast<jboolean>(fromJniBoolean(false));
  }

  const std::uint64_t dataSize =
      static_cast<std::uint64_t>(width) * static_cast<std::uint64_t>(height) * 4ull;
  const bool ok = RemixRenderer::instance().registerUiTexture(
      static_cast<std::uint64_t>(id),
      static_cast<std::uint32_t>(width),
      static_cast<std::uint32_t>(height),
      static_cast<remixapi_Format>(format),
      pixelData,
      dataSize);
  return static_cast<jboolean>(fromJniBoolean(ok));
}

JNIEXPORT jboolean JNICALL Java_mcrtx_bridge_RemixBridgeNative_nFreeUiTexture(
    JNIEnv*, jclass, jlong id) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nFreeUiTexture");
  return static_cast<jboolean>(fromJniBoolean(
      RemixRenderer::instance().freeUiTexture(static_cast<std::uint64_t>(id))));
}

JNIEXPORT jboolean JNICALL Java_mcrtx_bridge_RemixBridgeNative_nSubmitUiDrawList(
    JNIEnv* env,
    jclass,
    jfloatArray vertexXYZUV,
    jintArray vertexColor,
    jint vertexCount,
    jlongArray cmdTextureIds,
    jintArray cmdQuadCounts,
    jintArray cmdFlags,
    jint cmdCount,
    jint displayWidth,
    jint displayHeight) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nSubmitUiDrawList");

  // Empty list (clear) — no arrays needed.
  if (vertexCount <= 0 || cmdCount <= 0) {
    return static_cast<jboolean>(fromJniBoolean(
        RemixRenderer::instance().submitUiDrawListFromArrays(
            nullptr, nullptr, 0, nullptr, nullptr, nullptr, 0,
            static_cast<std::uint32_t>(displayWidth),
            static_cast<std::uint32_t>(displayHeight))));
  }

  if (vertexXYZUV == nullptr || vertexColor == nullptr
      || cmdTextureIds == nullptr || cmdQuadCounts == nullptr || cmdFlags == nullptr) {
    return static_cast<jboolean>(fromJniBoolean(false));
  }
  if (env->GetArrayLength(vertexXYZUV) < static_cast<jsize>(vertexCount) * 5
      || env->GetArrayLength(vertexColor) < static_cast<jsize>(vertexCount)
      || env->GetArrayLength(cmdTextureIds) < static_cast<jsize>(cmdCount)
      || env->GetArrayLength(cmdQuadCounts) < static_cast<jsize>(cmdCount)
      || env->GetArrayLength(cmdFlags) < static_cast<jsize>(cmdCount)) {
    return static_cast<jboolean>(fromJniBoolean(false));
  }

  auto* xyzuv = static_cast<jfloat*>(env->GetPrimitiveArrayCritical(vertexXYZUV, nullptr));
  auto* colors = static_cast<jint*>(env->GetPrimitiveArrayCritical(vertexColor, nullptr));
  auto* texIds = static_cast<jlong*>(env->GetPrimitiveArrayCritical(cmdTextureIds, nullptr));
  auto* quadCounts = static_cast<jint*>(env->GetPrimitiveArrayCritical(cmdQuadCounts, nullptr));
  auto* flags = static_cast<jint*>(env->GetPrimitiveArrayCritical(cmdFlags, nullptr));

  bool ok = false;
  if (xyzuv != nullptr && colors != nullptr && texIds != nullptr
      && quadCounts != nullptr && flags != nullptr) {
    ok = RemixRenderer::instance().submitUiDrawListFromArrays(
        reinterpret_cast<const float*>(xyzuv),
        reinterpret_cast<const std::uint32_t*>(colors),
        static_cast<std::uint32_t>(vertexCount),
        reinterpret_cast<const std::uint64_t*>(texIds),
        reinterpret_cast<const std::int32_t*>(quadCounts),
        reinterpret_cast<const std::uint32_t*>(flags),
        static_cast<std::uint32_t>(cmdCount),
        static_cast<std::uint32_t>(displayWidth),
        static_cast<std::uint32_t>(displayHeight));
  }

  if (flags != nullptr) {
    env->ReleasePrimitiveArrayCritical(cmdFlags, flags, JNI_ABORT);
  }
  if (quadCounts != nullptr) {
    env->ReleasePrimitiveArrayCritical(cmdQuadCounts, quadCounts, JNI_ABORT);
  }
  if (texIds != nullptr) {
    env->ReleasePrimitiveArrayCritical(cmdTextureIds, texIds, JNI_ABORT);
  }
  if (colors != nullptr) {
    env->ReleasePrimitiveArrayCritical(vertexColor, colors, JNI_ABORT);
  }
  if (xyzuv != nullptr) {
    env->ReleasePrimitiveArrayCritical(vertexXYZUV, xyzuv, JNI_ABORT);
  }

  return static_cast<jboolean>(fromJniBoolean(ok));
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nSetScreenTint(
    JNIEnv*, jclass, jfloat r, jfloat g, jfloat b, jfloat a) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nSetScreenTint");
  RemixRenderer::instance().setScreenTint(r, g, b, a);
}

JNIEXPORT jint JNICALL Java_mcrtx_bridge_RemixBridgeNative_nGetUiState(JNIEnv*, jclass) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nGetUiState");
  return static_cast<jint>(RemixRenderer::instance().getUiState());
}

JNIEXPORT jboolean JNICALL Java_mcrtx_bridge_RemixBridgeNative_nSetUiState(
    JNIEnv*, jclass, jint state) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nSetUiState");
  return static_cast<jboolean>(fromJniBoolean(
      RemixRenderer::instance().setUiState(static_cast<remixapi_UIState>(state))));
}

JNIEXPORT jboolean JNICALL Java_mcrtx_bridge_RemixBridgeNative_nHasWindowFocus(JNIEnv*, jclass) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nHasWindowFocus");
  return static_cast<jboolean>(fromJniBoolean(RemixRenderer::instance().hasWindowFocus()));
}

JNIEXPORT jboolean JNICALL Java_mcrtx_bridge_RemixBridgeNative_nIsOutputCloseRequested(JNIEnv*, jclass) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nIsOutputCloseRequested");
  return static_cast<jboolean>(fromJniBoolean(
      ::mcrtx::detail::g_outputWindowCloseRequested.load(std::memory_order_relaxed)));
}

JNIEXPORT jint JNICALL Java_mcrtx_bridge_RemixBridgeNative_nGetOutputWindowWidth(JNIEnv*, jclass) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nGetOutputWindowWidth");
  return static_cast<jint>(::mcrtx::detail::g_outputWindowClientWidth.load(std::memory_order_relaxed));
}

JNIEXPORT jint JNICALL Java_mcrtx_bridge_RemixBridgeNative_nGetOutputWindowHeight(JNIEnv*, jclass) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nGetOutputWindowHeight");
  return static_cast<jint>(::mcrtx::detail::g_outputWindowClientHeight.load(std::memory_order_relaxed));
}

JNIEXPORT jboolean JNICALL Java_mcrtx_bridge_RemixBridgeNative_nIsVirtualKeyDown(
    JNIEnv*, jclass, jint virtualKey) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nIsVirtualKeyDown");
  if (virtualKey < 0) {
    return static_cast<jboolean>(fromJniBoolean(false));
  }
  return static_cast<jboolean>(fromJniBoolean(
      RemixRenderer::instance().isVirtualKeyDown(static_cast<std::uint32_t>(virtualKey))));
}

JNIEXPORT jboolean JNICALL Java_mcrtx_bridge_RemixBridgeNative_nPollNativeMouseState(
    JNIEnv* env, jclass, jintArray stateOut) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nPollNativeMouseState");
  if (env == nullptr || stateOut == nullptr || env->GetArrayLength(stateOut) < 7) {
    return static_cast<jboolean>(fromJniBoolean(false));
  }

  std::int32_t x = 0;
  std::int32_t y = 0;
  std::int32_t deltaX = 0;
  std::int32_t deltaY = 0;
  std::int32_t dWheel = 0;
  std::uint32_t buttonsMask = 0;
  std::int32_t windowHeight = 0;
  if (!RemixRenderer::instance().pollNativeMouseState(
          x,
          y,
          deltaX,
          deltaY,
          dWheel,
          buttonsMask,
          windowHeight)) {
    return static_cast<jboolean>(fromJniBoolean(false));
  }

  const jint state[7] = {
      static_cast<jint>(x),
      static_cast<jint>(y),
      static_cast<jint>(deltaX),
      static_cast<jint>(deltaY),
      static_cast<jint>(dWheel),
      static_cast<jint>(buttonsMask),
      static_cast<jint>(windowHeight)};
  env->SetIntArrayRegion(stateOut, 0, 7, state);
  return static_cast<jboolean>(fromJniBoolean(true));
}

JNIEXPORT jboolean JNICALL Java_mcrtx_bridge_RemixBridgeNative_nSetNativeMouseGrabbed(
    JNIEnv*, jclass, jboolean grabbed) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nSetNativeMouseGrabbed");
  return static_cast<jboolean>(fromJniBoolean(
      RemixRenderer::instance().setNativeMouseGrabbed(grabbed == JNI_TRUE)));
}

JNIEXPORT jboolean JNICALL Java_mcrtx_bridge_RemixBridgeNative_nSetNativeCursorPosition(
    JNIEnv*, jclass, jint x, jint y) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nSetNativeCursorPosition");
  return static_cast<jboolean>(fromJniBoolean(
      RemixRenderer::instance().setNativeCursorPosition(static_cast<std::int32_t>(x), static_cast<std::int32_t>(y))));
}

JNIEXPORT jboolean JNICALL Java_mcrtx_bridge_RemixBridgeNative_nPresent(JNIEnv*, jclass) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nPresent");
  return static_cast<jboolean>(fromJniBoolean(RemixRenderer::instance().present()));
}

JNIEXPORT jstring JNICALL Java_mcrtx_bridge_RemixBridgeNative_nGetLastError(JNIEnv* env, jclass) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nGetLastError");
  const auto message = RemixRenderer::instance().lastError();
  return env->NewStringUTF(message.c_str());
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nRecordJavaSample(
    JNIEnv* env, jclass, jint side, jstring site, jlong nanoseconds) {
  if (site == nullptr) {
    return;
  }
  const char* utfChars = env->GetStringUTFChars(site, nullptr);
  if (utfChars == nullptr) {
    return;
  }
  const ::mcrtx::perf::Side sideEnum = (side == 0)
      ? ::mcrtx::perf::Side::Hook
      : ::mcrtx::perf::Side::Call;
  ::mcrtx::perf::recordDuration(sideEnum, utfChars, static_cast<std::uint64_t>(nanoseconds));
  env->ReleaseStringUTFChars(site, utfChars);
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nFlushJavaFrame(JNIEnv*, jclass) {
  // Currently a no-op — Java samples are recorded synchronously by
  // nRecordJavaSample. Kept as a JNI entry point so the Java helper can call
  // it on frame boundaries without needing ABI changes if batching is added
  // later.
}

JNIEXPORT jint JNICALL Java_mcrtx_bridge_RemixBridgeNative_nRegisterPerfSite(
    JNIEnv* env, jclass, jint side, jstring site) {
  if (site == nullptr) return -1;
  const char* utfChars = env->GetStringUTFChars(site, nullptr);
  if (utfChars == nullptr) return -1;
  ::mcrtx::perf::Side sideEnum;
  switch (side) {
    case 0: sideEnum = ::mcrtx::perf::Side::Hook; break;
    case 1: sideEnum = ::mcrtx::perf::Side::Call; break;
    case 2: sideEnum = ::mcrtx::perf::Side::Jni; break;
    case 3: sideEnum = ::mcrtx::perf::Side::Native; break;
    case 4: sideEnum = ::mcrtx::perf::Side::Remix; break;
    default: sideEnum = ::mcrtx::perf::Side::Hook; break;
  }
  const int id = ::mcrtx::perf::registerSite(sideEnum, utfChars);
  env->ReleaseStringUTFChars(site, utfChars);
  return static_cast<jint>(id);
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nRecordJavaSampleBatch(
    JNIEnv* env, jclass, jintArray ids, jlongArray nanos, jint count) {
  if (ids == nullptr || nanos == nullptr || count <= 0) return;
  void* idsPtr = env->GetPrimitiveArrayCritical(ids, nullptr);
  if (idsPtr == nullptr) return;
  void* nanosPtr = env->GetPrimitiveArrayCritical(nanos, nullptr);
  if (nanosPtr == nullptr) {
    env->ReleasePrimitiveArrayCritical(ids, idsPtr, JNI_ABORT);
    return;
  }
  static_assert(sizeof(jlong) == sizeof(std::uint64_t),
                "jlong must be 64-bit for the perf batch fast path");
  ::mcrtx::perf::recordDurationsBatch(
      reinterpret_cast<const int*>(idsPtr),
      reinterpret_cast<const std::uint64_t*>(nanosPtr),
      static_cast<std::size_t>(count));
  env->ReleasePrimitiveArrayCritical(nanos, nanosPtr, JNI_ABORT);
  env->ReleasePrimitiveArrayCritical(ids, idsPtr, JNI_ABORT);
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixBridgeNative_nRecordJavaCount(
    JNIEnv* env, jclass, jint side, jstring site, jlong count) {
  if (site == nullptr) {
    return;
  }
  const char* utfChars = env->GetStringUTFChars(site, nullptr);
  if (utfChars == nullptr) {
    return;
  }
  const ::mcrtx::perf::Side sideEnum = (side == 0)
      ? ::mcrtx::perf::Side::Hook
      : ::mcrtx::perf::Side::Call;
  ::mcrtx::perf::recordCount(sideEnum, utfChars, static_cast<std::uint64_t>(count));
  env->ReleaseStringUTFChars(site, utfChars);
}

}  // extern "C"
