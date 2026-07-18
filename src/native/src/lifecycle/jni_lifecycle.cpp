#include "mcrtx/core/jni_helpers.hpp"
#include "mcrtx/lifecycle/perf_log.hpp"
#include "mcrtx/core/remix_renderer.hpp"
#include "mcrtx/platform/remix_window_internals.hpp"
#include "mcrtx/core/runtime_config.hpp"
#include "mcrtx/core/tracy.hpp"

#if defined(MCRTX_ENABLE_TRACY)
#include <winsock2.h>
#include <ws2tcpip.h>
#endif

#include <jawt.h>
#include <jawt_md.h>
#include <jni.h>

#include <cstdint>
#include <fstream>
#include <string>

namespace {

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

JNIEXPORT jboolean JNICALL Java_mcrtx_bridge_RemixLifecycleBridge_nInitialize(
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
  return mcrtx::jni::toJniBoolean(ok);
}

JNIEXPORT jlong JNICALL Java_mcrtx_bridge_RemixLifecycleBridge_nResolveAwtWindowHandle(
    JNIEnv* env, jclass, jobject canvas) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nResolveAwtWindowHandle");
  return toJniHandle(resolveAwtWindowHandle(env, canvas));
}

JNIEXPORT jboolean JNICALL Java_mcrtx_bridge_RemixLifecycleBridge_nEmbedCompatibilityWindow(
    JNIEnv*, jclass, jlong childHwnd, jlong parentHwnd, jint width, jint height) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nEmbedCompatibilityWindow");
  const bool ok = embedCompatibilityWindow(
      fromJniHandle(childHwnd),
      fromJniHandle(parentHwnd),
      static_cast<int>(width),
      static_cast<int>(height));
  return mcrtx::jni::toJniBoolean(ok);
}

JNIEXPORT jboolean JNICALL Java_mcrtx_bridge_RemixLifecycleBridge_nFocusWindow(
    JNIEnv*, jclass, jlong hwnd) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nFocusWindow");
  return mcrtx::jni::toJniBoolean(focusWindow(fromJniHandle(hwnd)));
}

JNIEXPORT jboolean JNICALL Java_mcrtx_bridge_RemixLifecycleBridge_nIsEmbeddedWindowActive(
    JNIEnv*, jclass, jlong childHwnd, jlong parentHwnd) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nIsEmbeddedWindowActive");
  return mcrtx::jni::toJniBoolean(
      isEmbeddedWindowActive(fromJniHandle(childHwnd), fromJniHandle(parentHwnd)));
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixLifecycleBridge_nShutdown(JNIEnv*, jclass) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nShutdown");
  MCRTX_TRACY_SCOPE("nShutdown");
#if defined(MCRTX_ENABLE_TRACY)
  appendTracyDiagnostic("nShutdown", true);
#endif
  RemixRenderer::instance().shutdown();
  ::mcrtx::perf::shutdown();
}

JNIEXPORT void JNICALL Java_mcrtx_bridge_RemixLifecycleBridge_nResize(
    JNIEnv*, jclass, jint width, jint height) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nResize");
  RemixRenderer::instance().resize(static_cast<std::uint32_t>(width), static_cast<std::uint32_t>(height));
}

JNIEXPORT jint JNICALL Java_mcrtx_bridge_RemixLifecycleBridge_nGetUiState(JNIEnv*, jclass) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nGetUiState");
  return static_cast<jint>(RemixRenderer::instance().getUiState());
}

JNIEXPORT jboolean JNICALL Java_mcrtx_bridge_RemixLifecycleBridge_nSetUiState(
    JNIEnv*, jclass, jint state) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nSetUiState");
  return mcrtx::jni::toJniBoolean(
      RemixRenderer::instance().setUiState(static_cast<remixapi_UIState>(state)));
}

JNIEXPORT jboolean JNICALL Java_mcrtx_bridge_RemixLifecycleBridge_nHasWindowFocus(JNIEnv*, jclass) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nHasWindowFocus");
  return mcrtx::jni::toJniBoolean(RemixRenderer::instance().hasWindowFocus());
}

JNIEXPORT jboolean JNICALL Java_mcrtx_bridge_RemixLifecycleBridge_nIsOutputCloseRequested(JNIEnv*, jclass) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nIsOutputCloseRequested");
  return mcrtx::jni::toJniBoolean(
      ::mcrtx::window_detail::g_outputWindowCloseRequested.load(std::memory_order_relaxed));
}

JNIEXPORT jint JNICALL Java_mcrtx_bridge_RemixLifecycleBridge_nGetOutputWindowWidth(JNIEnv*, jclass) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nGetOutputWindowWidth");
  return static_cast<jint>(::mcrtx::window_detail::g_outputWindowClientWidth.load(std::memory_order_relaxed));
}

JNIEXPORT jint JNICALL Java_mcrtx_bridge_RemixLifecycleBridge_nGetOutputWindowHeight(JNIEnv*, jclass) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nGetOutputWindowHeight");
  return static_cast<jint>(::mcrtx::window_detail::g_outputWindowClientHeight.load(std::memory_order_relaxed));
}

JNIEXPORT jboolean JNICALL Java_mcrtx_bridge_RemixLifecycleBridge_nSetOutputWindowFullscreen(
    JNIEnv*, jclass, jboolean fullscreen) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nSetOutputWindowFullscreen");
  return mcrtx::jni::toJniBoolean(
      RemixRenderer::instance().setOutputWindowFullscreen(fullscreen == JNI_TRUE));
}

JNIEXPORT jboolean JNICALL Java_mcrtx_bridge_RemixLifecycleBridge_nIsVirtualKeyDown(
    JNIEnv*, jclass, jint virtualKey) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nIsVirtualKeyDown");
  if (virtualKey < 0) {
    return JNI_FALSE;
  }
  return mcrtx::jni::toJniBoolean(
      RemixRenderer::instance().isVirtualKeyDown(static_cast<std::uint32_t>(virtualKey)));
}

JNIEXPORT jboolean JNICALL Java_mcrtx_bridge_RemixLifecycleBridge_nPollNativeMouseState(
    JNIEnv* env, jclass, jintArray stateOut) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nPollNativeMouseState");
  if (env == nullptr || stateOut == nullptr || env->GetArrayLength(stateOut) < 7) {
    return JNI_FALSE;
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
    return JNI_FALSE;
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
  return JNI_TRUE;
}

JNIEXPORT jboolean JNICALL Java_mcrtx_bridge_RemixLifecycleBridge_nSetNativeMouseGrabbed(
    JNIEnv*, jclass, jboolean grabbed) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nSetNativeMouseGrabbed");
  return mcrtx::jni::toJniBoolean(
      RemixRenderer::instance().setNativeMouseGrabbed(grabbed == JNI_TRUE));
}

JNIEXPORT jboolean JNICALL Java_mcrtx_bridge_RemixLifecycleBridge_nSetNativeCursorPosition(
    JNIEnv*, jclass, jint x, jint y) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nSetNativeCursorPosition");
  return mcrtx::jni::toJniBoolean(
      RemixRenderer::instance().setNativeCursorPosition(static_cast<std::int32_t>(x), static_cast<std::int32_t>(y)));
}

JNIEXPORT jboolean JNICALL Java_mcrtx_bridge_RemixLifecycleBridge_nPresent(JNIEnv*, jclass) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nPresent");
  return mcrtx::jni::toJniBoolean(RemixRenderer::instance().present());
}

JNIEXPORT jboolean JNICALL Java_mcrtx_bridge_RemixLifecycleBridge_nRequestPresentedScreenshot(
    JNIEnv* env,
    jclass,
    jstring absolutePath) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nRequestPresentedScreenshot");
  if (absolutePath == nullptr) {
    return JNI_FALSE;
  }

  const char* utfChars = env->GetStringUTFChars(absolutePath, nullptr);
  if (utfChars == nullptr) {
    return JNI_FALSE;
  }

  const std::string path(utfChars);
  env->ReleaseStringUTFChars(absolutePath, utfChars);
  return mcrtx::jni::toJniBoolean(RemixRenderer::instance().requestPresentedScreenshot(path));
}

JNIEXPORT jstring JNICALL Java_mcrtx_bridge_RemixLifecycleBridge_nGetLastError(JNIEnv* env, jclass) {
  MCRTX_PERF_SCOPE(::mcrtx::perf::Side::Jni, "nGetLastError");
  const auto message = RemixRenderer::instance().lastError();
  return env->NewStringUTF(message.c_str());
}

}  // extern "C"
