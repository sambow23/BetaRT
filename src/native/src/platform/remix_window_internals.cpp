// Win32 output-window and raw-input infrastructure.

#include "mcrtx/platform/remix_window_internals.hpp"
#include "mcrtx/core/build_version.hpp"
#include "mcrtx/core/runtime_config.hpp"

#include <atomic>
#include <cstddef>
#include <cstdint>
#include <iostream>
#include <string>
#include <string_view>
#include <vector>

namespace mcrtx::window_detail {

using namespace mcrtx::detail;

namespace {

std::wstring widenAscii(std::string_view value) {
  std::wstring widened;
  widened.reserve(value.size());
  for (const char character : value) {
    widened.push_back(static_cast<wchar_t>(static_cast<unsigned char>(character)));
  }
  return widened;
}

std::wstring makeRemixWindowTitle() {
  std::wstring title = widenAscii(mcrtx::build::kProductName);
  constexpr std::string_view buildId = mcrtx::build::kBuildId;
  if (!buildId.empty() && buildId != "unknown") {
    title += L" (";
    title += widenAscii(buildId);
    title += L")";
  }
  return title;
}

const std::wstring& remixWindowTitleStorage() {
  static const std::wstring title = makeRemixWindowTitle();
  return title;
}

}  // namespace

std::atomic_bool g_outputWindowInteractive {false};
std::atomic_bool g_nativeMouseCursorHidden {false};
std::atomic_long g_nativeMouseWheelDelta {0};
std::atomic_long g_rawMouseDeltaX {0};
std::atomic_long g_rawMouseDeltaY {0};
std::atomic_long g_rawMouseInputEvents {0};
std::atomic_bool g_outputWindowCloseRequested {false};
std::atomic_int g_outputWindowClientWidth {0};
std::atomic_int g_outputWindowClientHeight {0};
const wchar_t kRemixWindowClassName[] = L"MCRTXRemixOutputWindow";
const wchar_t* getRemixWindowTitle() {
  return remixWindowTitleStorage().c_str();
}
HMODULE getCurrentModuleHandle() {
  HMODULE moduleHandle = nullptr;
  if (!GetModuleHandleExW(
          GET_MODULE_HANDLE_EX_FLAG_FROM_ADDRESS | GET_MODULE_HANDLE_EX_FLAG_UNCHANGED_REFCOUNT,
          reinterpret_cast<LPCWSTR>(&getCurrentModuleHandle),
          &moduleHandle)) {
    return nullptr;
  }
  return moduleHandle;
}
bool shouldUseSingleNativeOutputWindow() {
  const std::string configuredWindowMode = readEnvironmentVariable("MCRTX_WINDOW_MODE");
  return !configuredWindowMode.empty() && equalsIgnoreCase(configuredWindowMode, "single-native");
}

bool shouldUseOverlayOutputWindow(bool* usedLegacyEnvVar) {
  if (usedLegacyEnvVar != nullptr) {
    *usedLegacyEnvVar = false;
  }

  const std::string configuredWindowMode = readEnvironmentVariable("MCRTX_WINDOW_MODE");
  if (!configuredWindowMode.empty()) {
    const std::string_view mode(configuredWindowMode);
    if (equalsIgnoreCase(mode, "dual")
        || equalsIgnoreCase(mode, "detached")
        || equalsIgnoreCase(mode, "separate")
        || equalsIgnoreCase(mode, "standalone")
        || equalsIgnoreCase(mode, "single-native")) {
      return false;
    }

    if (equalsIgnoreCase(mode, "single")
        || equalsIgnoreCase(mode, "overlay")
        || equalsIgnoreCase(mode, "attached")) {
      return true;
    }
  }

  const std::string legacySourceWindowFlag = readEnvironmentVariable("MCRTX_USE_SOURCE_WINDOW");
  if (isTruthyEnvValue(legacySourceWindowFlag.c_str())) {
    if (usedLegacyEnvVar != nullptr) {
      *usedLegacyEnvVar = true;
    }

    return false;
  }

  return true;
}

bool shouldUseStandaloneOutputWindow() {
  const std::string configuredWindowMode = readEnvironmentVariable("MCRTX_WINDOW_MODE");
  return !configuredWindowMode.empty()
      && equalsIgnoreCase(configuredWindowMode, "standalone");
}

bool shouldUseRawMouseInput() {
  static const bool enabled = []() {
    const std::string value = readEnvironmentVariable("MCRTX_RAW_MOUSE");
    return value.empty() || isTruthyEnvValue(value.c_str());
  }();
  return enabled;
}
bool getSourceClientRectInScreenSpace(HWND sourceHwnd, RECT& rect) {
  RECT clientRect {};
  if (!GetClientRect(sourceHwnd, &clientRect)) {
    return false;
  }

  POINT topLeft {clientRect.left, clientRect.top};
  POINT bottomRight {clientRect.right, clientRect.bottom};
  if (!ClientToScreen(sourceHwnd, &topLeft) || !ClientToScreen(sourceHwnd, &bottomRight)) {
    return false;
  }

  rect.left = topLeft.x;
  rect.top = topLeft.y;
  rect.right = bottomRight.x;
  rect.bottom = bottomRight.y;
  return true;
}

LRESULT CALLBACK remixOutputWindowProc(HWND hwnd, UINT message, WPARAM wParam, LPARAM lParam) {
  switch (message) {
    case WM_SETCURSOR:
      if (g_nativeMouseCursorHidden.load(std::memory_order_relaxed)) {
        SetCursor(nullptr);
        return TRUE;
      }
      return DefWindowProcW(hwnd, message, wParam, lParam);
    case WM_MOUSEWHEEL:
      g_nativeMouseWheelDelta.fetch_add(GET_WHEEL_DELTA_WPARAM(wParam), std::memory_order_relaxed);
      return 0;
    case WM_INPUT:
      accumulateRawMouseInput(lParam);
      return DefWindowProcW(hwnd, message, wParam, lParam);
    case WM_NCHITTEST:
      if (!g_outputWindowInteractive.load(std::memory_order_relaxed)) {
        return HTTRANSPARENT;
      }
      return DefWindowProcW(hwnd, message, wParam, lParam);
    case WM_SIZE:
      if (wParam != SIZE_MINIMIZED) {
        g_outputWindowClientWidth.store(LOWORD(lParam), std::memory_order_relaxed);
        g_outputWindowClientHeight.store(HIWORD(lParam), std::memory_order_relaxed);
      }
      return DefWindowProcW(hwnd, message, wParam, lParam);
    case WM_CLOSE:
      g_outputWindowCloseRequested.store(true, std::memory_order_relaxed);
      ShowWindow(hwnd, SW_HIDE);
      return 0;
    default:
      return DefWindowProcW(hwnd, message, wParam, lParam);
  }
}
void accumulateRawMouseInput(LPARAM lParam) {
  g_rawMouseInputEvents.fetch_add(1, std::memory_order_relaxed);
  UINT rawDataSize = 0;
  if (GetRawInputData(reinterpret_cast<HRAWINPUT>(lParam), RID_INPUT, nullptr, &rawDataSize, sizeof(RAWINPUTHEADER)) == 0
      && rawDataSize != 0 && rawDataSize <= sizeof(RAWINPUT)) {
    RAWINPUT rawInput {};
    if (GetRawInputData(reinterpret_cast<HRAWINPUT>(lParam), RID_INPUT, &rawInput, &rawDataSize, sizeof(RAWINPUTHEADER)) == rawDataSize

        && rawInput.header.dwType == RIM_TYPEMOUSE
        && (rawInput.data.mouse.usFlags & MOUSE_MOVE_ABSOLUTE) == 0) {
      g_rawMouseDeltaX.fetch_add(rawInput.data.mouse.lLastX, std::memory_order_relaxed);
      g_rawMouseDeltaY.fetch_add(rawInput.data.mouse.lLastY, std::memory_order_relaxed);
    }
  }
}

namespace {
HWND g_rawMouseInputWindow = nullptr;

LRESULT CALLBACK rawMouseInputWindowProc(HWND hwnd, UINT message, WPARAM wParam, LPARAM lParam) {
  if (message == WM_INPUT) {
    accumulateRawMouseInput(lParam);
  }
  return DefWindowProcW(hwnd, message, wParam, lParam);
}

// Returns the hwnd the process's mouse raw-input is currently registered to, or
// nullptr if no mouse device is registered / the query fails.
HWND getRegisteredMouseTargetHwnd() {
  UINT deviceCount = 0;
  if (GetRegisteredRawInputDevices(nullptr, &deviceCount, sizeof(RAWINPUTDEVICE)) != 0 || deviceCount == 0) {
    return nullptr;
  }
  std::vector<RAWINPUTDEVICE> devices(deviceCount);
  const UINT written = GetRegisteredRawInputDevices(devices.data(), &deviceCount, sizeof(RAWINPUTDEVICE));
  if (written == static_cast<UINT>(-1)) {
    return nullptr;
  }
  for (UINT i = 0; i < written; ++i) {
    if (devices[i].usUsagePage == 0x01 && devices[i].usUsage == 0x02) {
      return devices[i].hwndTarget;
    }
  }
  return nullptr;
}
}  // namespace
HWND ensureRawMouseInputWindow() {
  // A real (hidden, never-shown, never-destroyed) top-level window owned by the
  // calling (pump) thread. Raw input is bound to this window's lifetime,
  // decoupling it from the Remix output window, which is destroyed and recreated
  // across re-initialization (binding raw input to a window that later dies
  // silently stops all WM_INPUT delivery). A message-only (HWND_MESSAGE) window
  // does NOT receive RIDEV_INPUTSINK raw input, so this must be a normal window.
  static const wchar_t kRawInputClassName[] = L"MCRTXRawMouseInput";
  if (g_rawMouseInputWindow != nullptr) {
    return g_rawMouseInputWindow;
  }

  static bool classRegistered = false;
  if (!classRegistered) {
    WNDCLASSEXW windowClass {};
    windowClass.cbSize = sizeof(windowClass);
    windowClass.lpfnWndProc = rawMouseInputWindowProc;
    windowClass.hInstance = getCurrentModuleHandle();
    windowClass.lpszClassName = kRawInputClassName;
    RegisterClassExW(&windowClass);
    classRegistered = true;
  }

  g_rawMouseInputWindow = CreateWindowExW(
      WS_EX_NOACTIVATE | WS_EX_TOOLWINDOW,
      kRawInputClassName,
      L"mcrtx raw mouse input",
      WS_POPUP,
      -32000, -32000, 1, 1,
      nullptr,
      nullptr,
      getCurrentModuleHandle(),
      nullptr);
  if (g_rawMouseInputWindow == nullptr) {
    OutputDebugStringA("[mcrtx] Raw mouse input window creation failed\n");
    return nullptr;
  }

  // INPUTSINK delivery has only ever been observed to a SHOWN window in this
  // process. Show it off-screen, no-activate, so it never appears to the user
  // or steals focus but still qualifies for raw input delivery.

  ShowWindow(g_rawMouseInputWindow, SW_SHOWNA);

  RAWINPUTDEVICE rawMouseDevice {};
  rawMouseDevice.usUsagePage = 0x01;  // Generic Desktop Controls
  rawMouseDevice.usUsage = 0x02;      // Mouse
  rawMouseDevice.dwFlags = RIDEV_INPUTSINK;
  rawMouseDevice.hwndTarget = g_rawMouseInputWindow;
  const bool registered = RegisterRawInputDevices(&rawMouseDevice, 1, sizeof(rawMouseDevice)) != FALSE;
  std::string readyMessage = std::string("[mcrtx] Raw mouse input window ready hwnd=0x")
      + std::to_string(reinterpret_cast<std::uintptr_t>(g_rawMouseInputWindow))
      + " ownerTid=" + std::to_string(GetCurrentThreadId())
      + " registered=" + (registered ? "true" : ("false err=" + std::to_string(GetLastError()))) + "\n";
  OutputDebugStringA(readyMessage.c_str());
  std::cerr << readyMessage;
  g_rawMouseDeltaX.store(0, std::memory_order_relaxed);
  g_rawMouseDeltaY.store(0, std::memory_order_relaxed);
  return g_rawMouseInputWindow;
}
bool ensureRawMouseRegistrationOwned() {
  // lwjgl3/GLFW registers raw mouse input to its own window when it disables the
  // cursor for mouse-look. A later RegisterRawInputDevices for the same
  // usage page/usage replaces the earlier target process-wide, so GLFW silently
  // steals WM_INPUT delivery from our window. Reclaim it whenever we detect the
  // registration is no longer ours; this is idempotent and self-healing.
  if (g_rawMouseInputWindow == nullptr) {
    return false;
  }
  if (getRegisteredMouseTargetHwnd() == g_rawMouseInputWindow) {
    return true;
  }
  RAWINPUTDEVICE rawMouseDevice {};
  rawMouseDevice.usUsagePage = 0x01;
  rawMouseDevice.usUsage = 0x02;
  rawMouseDevice.dwFlags = RIDEV_INPUTSINK;
  rawMouseDevice.hwndTarget = g_rawMouseInputWindow;
  const bool reclaimed = RegisterRawInputDevices(&rawMouseDevice, 1, sizeof(rawMouseDevice)) != FALSE;
  std::string message = std::string("[mcrtx] Reclaimed raw mouse registration to hwnd=0x")
      + std::to_string(reinterpret_cast<std::uintptr_t>(g_rawMouseInputWindow))
      + " ok=" + (reclaimed ? "true" : "false") + "\n";
  OutputDebugStringA(message.c_str());
  std::cerr << message;
  return reclaimed;
}
bool ensureOutputWindowClassRegistered() {
  static bool registered = false;
  if (registered) {
    return true;
  }

  WNDCLASSEXW windowClass {};
  windowClass.cbSize = sizeof(windowClass);
  windowClass.lpfnWndProc = remixOutputWindowProc;
  windowClass.hInstance = getCurrentModuleHandle();
  windowClass.hCursor = LoadCursorW(nullptr, MAKEINTRESOURCEW(32512));
  windowClass.hbrBackground = reinterpret_cast<HBRUSH>(COLOR_WINDOW + 1);
  windowClass.lpszClassName = kRemixWindowClassName;

  const ATOM atom = RegisterClassExW(&windowClass);
  if (atom == 0 && GetLastError() != ERROR_CLASS_ALREADY_EXISTS) {
    return false;
  }

  registered = true;
  return true;
}
}  // namespace mcrtx::window_detail
