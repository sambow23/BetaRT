// Auto-extracted from remix_renderer.cpp during the monolith split.
// Keep edits here; do not re-merge into remix_renderer.cpp.

#include "mcrtx/remix_renderer.hpp"
#include "mcrtx/render_internals.hpp"

#include <algorithm>
#include <atomic>
#include <bit>
#include <cctype>
#include <cmath>
#include <cstdlib>
#include <cstddef>
#include <iostream>
#include <sstream>
#include <string_view>
#include <vector>

namespace mcrtx {

using namespace mcrtx::detail;

namespace {

bool isVerboseInputLoggingEnabled() {
  static const bool enabled = []() {
    const std::string value = readEnvironmentVariable("MCRTX_VERBOSE_INPUT_LOG");
    return isTruthyEnvValue(value.c_str()) || isVerboseLoggingEnabled();
  }();
  return enabled;
}

void logVerboseInput(const std::string& message) {
  if (!isVerboseInputLoggingEnabled()) {
    return;
  }

  static int remainingLogs = 4000;
  if (remainingLogs <= 0) {
    return;
  }

  --remainingLogs;
  OutputDebugStringA(("[mcrtx] [mouse-native] " + message + "\n").c_str());
  std::cerr << "[mcrtx] [mouse-native] " << message << std::endl;
}

void syncNativeCursorVisibility(HWND mouseWindow, bool hidden) {
  if (!hidden) {
    while (ShowCursor(TRUE) < 0) { }
  }

  const int cursorCounterAfter = ShowCursor(FALSE);
  ShowCursor(TRUE);  // restore counter to original value

  g_nativeMouseCursorHidden.store(hidden, std::memory_order_relaxed);
  SetCursor(hidden ? nullptr : LoadCursorW(nullptr, MAKEINTRESOURCEW(32512)));

  logVerboseInput(std::string("syncNativeCursorVisibility hidden=") + (hidden ? "true" : "false")
      + " ShowCursorCounter=" + std::to_string(cursorCounterAfter)
      + " mouseWindow=0x" + std::to_string(reinterpret_cast<std::uintptr_t>(mouseWindow)));

  if (mouseWindow != nullptr && IsWindow(mouseWindow)) {
    SendMessageW(
        mouseWindow,
        WM_SETCURSOR,
        reinterpret_cast<WPARAM>(mouseWindow),
        MAKELPARAM(HTCLIENT, WM_MOUSEMOVE));
  }
}

  std::string formatWindowHandle(HWND window) {
    std::ostringstream stream;
    stream << "0x" << std::hex << reinterpret_cast<std::uintptr_t>(window) << std::dec;
    return stream.str();
  }

  std::string describeWindow(HWND window) {
    if (window == nullptr) {
      return "<null>";
    }

    std::ostringstream stream;
    stream << formatWindowHandle(window);
    if (!IsWindow(window)) {
      stream << " invalid";
      return stream.str();
    }

    char className[128] {};
    char title[256] {};
    GetClassNameA(window, className, static_cast<int>(std::size(className)));
    GetWindowTextA(window, title, static_cast<int>(std::size(title)));

    DWORD processId = 0;
    const DWORD threadId = GetWindowThreadProcessId(window, &processId);
    const HWND rootWindow = GetAncestor(window, GA_ROOT);
    const HWND ownerWindow = GetWindow(window, GW_OWNER);

    stream << " class='" << className << "'";
    if (title[0] != '\0') {
      stream << " title='" << title << "'";
    }
    stream << " pid=" << processId << " tid=" << threadId;
    if (rootWindow != nullptr && rootWindow != window) {
      stream << " root=" << formatWindowHandle(rootWindow);
    }
    if (ownerWindow != nullptr) {
      stream << " owner=" << formatWindowHandle(ownerWindow);
    }
    return stream.str();
  }

  std::string describeStartupWindowHandoff(const char* phase, HWND sourceWindow, HWND outputWindow) {
    std::ostringstream stream;
    stream << "Startup window handoff " << phase
           << ": source=" << describeWindow(sourceWindow)
           << " output=" << describeWindow(outputWindow)
           << " foreground=" << describeWindow(GetForegroundWindow());
    return stream.str();
  }

  void bringWindowToForeground(HWND window) {
    if (window == nullptr || !IsWindow(window)) {
      return;
    }

    const HWND rootWindow = GetAncestor(window, GA_ROOT);
    const HWND targetWindow = rootWindow != nullptr ? rootWindow : window;
    const HWND foregroundWindow = GetForegroundWindow();
    const DWORD currentThreadId = GetCurrentThreadId();
    const DWORD foregroundThreadId = foregroundWindow != nullptr
        ? GetWindowThreadProcessId(foregroundWindow, nullptr)
        : 0;
    const bool attachedInput = foregroundThreadId != 0 && foregroundThreadId != currentThreadId
        && AttachThreadInput(foregroundThreadId, currentThreadId, TRUE);

    ShowWindow(targetWindow, SW_SHOW);
    BringWindowToTop(targetWindow);
    SetForegroundWindow(targetWindow);
    SetActiveWindow(targetWindow);
    SetFocus(targetWindow);

    if (attachedInput) {
      AttachThreadInput(foregroundThreadId, currentThreadId, FALSE);
    }
  }

  void raiseOverlayWindow(HWND window) {
    if (window == nullptr || !IsWindow(window)) {
      return;
    }

    SetWindowPos(
        window,
        HWND_TOPMOST,
        0,
        0,
        0,
        0,
        SWP_NOMOVE | SWP_NOSIZE | SWP_NOACTIVATE | SWP_SHOWWINDOW);
  }

}  // namespace

bool RemixRenderer::createOutputWindow(HWND sourceHwnd) {
  if (outputHwnd_ != nullptr) {
    updateOutputWindowSize();
    ShowWindow(outputHwnd_, (standaloneOutputWindow_ || singleNativeOutputWindow_ || outputWindowInteractive_) ? SW_SHOW : SW_SHOWNOACTIVATE);
    return true;
  }

  if (!ensureOutputWindowClassRegistered()) {
    setError("Failed to register Remix output window class");
    return false;
  }

  RECT sourceClientRect {};
  const bool hasSourceClientRect = getSourceClientRectInScreenSpace(sourceHwnd, sourceClientRect);
  DWORD exStyle = (standaloneOutputWindow_ || singleNativeOutputWindow_) ? 0 : WS_EX_NOACTIVATE;
  DWORD style = WS_POPUP;
  HWND parentHwnd = sourceHwnd;
  int windowX = hasSourceClientRect ? sourceClientRect.left : CW_USEDEFAULT;
  int windowY = hasSourceClientRect ? sourceClientRect.top : CW_USEDEFAULT;
  int outerWidth = hasSourceClientRect
      ? sourceClientRect.right - sourceClientRect.left
      : static_cast<int>(width_);
  int outerHeight = hasSourceClientRect
      ? sourceClientRect.bottom - sourceClientRect.top
      : static_cast<int>(height_);

  if (overlayOutputWindow_) {
    exStyle |= WS_EX_TOOLWINDOW;
  } else if (singleNativeOutputWindow_) {
    exStyle |= WS_EX_APPWINDOW;
    style = WS_OVERLAPPEDWINDOW;
    parentHwnd = nullptr;

    RECT windowRect {0, 0, static_cast<LONG>(width_), static_cast<LONG>(height_)};
    AdjustWindowRectEx(&windowRect, style, FALSE, exStyle);
    outerWidth = windowRect.right - windowRect.left;
    outerHeight = windowRect.bottom - windowRect.top;
  } else {
    exStyle |= WS_EX_APPWINDOW;
    style = WS_OVERLAPPEDWINDOW;
    parentHwnd = nullptr;

    RECT windowRect {0, 0, static_cast<LONG>(width_), static_cast<LONG>(height_)};
    AdjustWindowRectEx(&windowRect, style, FALSE, exStyle);
    outerWidth = windowRect.right - windowRect.left;
    outerHeight = windowRect.bottom - windowRect.top;

    if (hasSourceClientRect) {
      windowX = sourceClientRect.right + 32;
      windowY = sourceClientRect.top;
    } else {
      windowX = CW_USEDEFAULT;
      windowY = CW_USEDEFAULT;
    }
  }

  outputHwnd_ = CreateWindowExW(
      exStyle,
      kRemixWindowClassName,
      kRemixWindowTitle,
      style,
      windowX,
      windowY,
      outerWidth,
      outerHeight,
      parentHwnd,
      nullptr,
      getCurrentModuleHandle(),
      nullptr);

  if (outputHwnd_ == nullptr) {
    setError("Failed to create Remix output window");
    return false;
  }

  outputWindowInteractive_ = standaloneOutputWindow_ || singleNativeOutputWindow_;
  g_outputWindowInteractive.store(outputWindowInteractive_, std::memory_order_relaxed);

  if (overlayOutputWindow_) {
    SetWindowPos(
        outputHwnd_,
        HWND_TOPMOST,
        windowX,
        windowY,
        outerWidth,
        outerHeight,
        SWP_NOACTIVATE | SWP_SHOWWINDOW);
  }
  ShowWindow(outputHwnd_, (standaloneOutputWindow_ || singleNativeOutputWindow_) ? SW_SHOW : SW_SHOWNOACTIVATE);
  UpdateWindow(outputHwnd_);
  if (overlayOutputWindow_) {
    log(describeStartupWindowHandoff("before", sourceHwnd, outputHwnd_));
    bringWindowToForeground(sourceHwnd);
    raiseOverlayWindow(outputHwnd_);
    log(describeStartupWindowHandoff("after", sourceHwnd, outputHwnd_));
  } else if (singleNativeOutputWindow_) {
    log(describeStartupWindowHandoff("before", sourceHwnd, outputHwnd_));
    bringWindowToForeground(outputHwnd_);
    log(describeStartupWindowHandoff("after", sourceHwnd, outputHwnd_));
  } else if (!standaloneOutputWindow_ && !singleNativeOutputWindow_) {
    log(describeStartupWindowHandoff("before", sourceHwnd, outputHwnd_));
    bringWindowToForeground(outputHwnd_);
    log(describeStartupWindowHandoff("after", sourceHwnd, outputHwnd_));
  }
  log(overlayOutputWindow_
      ? "Created Remix client-area overlay window"
      : (singleNativeOutputWindow_
          ? "Created Remix single-native window"
          : (standaloneOutputWindow_
          ? "Created Remix standalone window"
          : "Created Remix detached development window")));
  return true;
}

void RemixRenderer::destroyOutputWindow() {
  if (outputHwnd_ == nullptr) {
    return;
  }

  releaseNativeMouseGrabLocked(resolveNativeMouseWindowLocked());
  nativeMouseGrabbed_ = false;
  nativeMouseLastCursorValid_ = false;
  outputWindowInteractive_ = false;
  g_outputWindowInteractive.store(false, std::memory_order_relaxed);

  DestroyWindow(outputHwnd_);
  outputHwnd_ = nullptr;
}

void RemixRenderer::pumpOutputWindowMessages() {
  if (outputHwnd_ == nullptr) {
    return;
  }

  MSG message {};
  while (PeekMessageW(&message, outputHwnd_, 0, 0, PM_REMOVE)) {
    TranslateMessage(&message);
    DispatchMessageW(&message);
  }
}

void RemixRenderer::updateOutputWindowSize() {
  if (outputHwnd_ == nullptr || sourceHwnd_ == nullptr) {
    return;
  }

  if (standaloneOutputWindow_) {
    return;
  }

  if (singleNativeOutputWindow_) {
    // The user owns the single-native window's size and position via the normal
    // title bar and resize borders. The render resolution follows the window
    // through WM_SIZE -> game resize -> resize(), so this must not fight the
    // user by repositioning the window every frame.
    return;
  }

  if (!overlayOutputWindow_) {
    RECT outputRect {};
    if (!GetWindowRect(outputHwnd_, &outputRect)) {
      return;
    }

    const DWORD style = static_cast<DWORD>(GetWindowLongPtrW(outputHwnd_, GWL_STYLE));
    const DWORD exStyle = static_cast<DWORD>(GetWindowLongPtrW(outputHwnd_, GWL_EXSTYLE));
    RECT desiredWindowRect {0, 0, static_cast<LONG>(width_), static_cast<LONG>(height_)};
    AdjustWindowRectEx(&desiredWindowRect, style, FALSE, exStyle);

    SetWindowPos(
        outputHwnd_,
        HWND_NOTOPMOST,
        outputRect.left,
        outputRect.top,
        desiredWindowRect.right - desiredWindowRect.left,
        desiredWindowRect.bottom - desiredWindowRect.top,
        (outputWindowInteractive_ ? SWP_SHOWWINDOW : (SWP_NOACTIVATE | SWP_SHOWWINDOW)));
    return;
  }

  RECT sourceClientRect {};
  if (!getSourceClientRectInScreenSpace(sourceHwnd_, sourceClientRect)) {
    return;
  }

  const int outerWidth = sourceClientRect.right - sourceClientRect.left;
  const int outerHeight = sourceClientRect.bottom - sourceClientRect.top;

  SetWindowPos(
      outputHwnd_,
      HWND_TOPMOST,
      sourceClientRect.left,
      sourceClientRect.top,
      outerWidth,
      outerHeight,
      (outputWindowInteractive_ ? SWP_SHOWWINDOW : (SWP_NOACTIVATE | SWP_SHOWWINDOW)));
}

void RemixRenderer::syncOutputWindowInteractivity(remixapi_UIState uiState) {
  if (standaloneOutputWindow_ || singleNativeOutputWindow_) {
    return;
  }

  if (outputHwnd_ == nullptr || sourceHwnd_ == nullptr) {
    return;
  }

  const bool interactive = uiState != REMIXAPI_UI_STATE_NONE;
  if (interactive == outputWindowInteractive_) {
    return;
  }

  outputWindowInteractive_ = interactive;
  g_outputWindowInteractive.store(interactive, std::memory_order_relaxed);

  LONG_PTR exStyle = GetWindowLongPtrW(outputHwnd_, GWL_EXSTYLE);
  if (interactive) {
    exStyle &= ~static_cast<LONG_PTR>(WS_EX_NOACTIVATE);
  } else {
    exStyle |= static_cast<LONG_PTR>(WS_EX_NOACTIVATE);
  }
  SetWindowLongPtrW(outputHwnd_, GWL_EXSTYLE, exStyle);

  updateOutputWindowSize();
  SetWindowPos(
      outputHwnd_,
      (overlayOutputWindow_ ? HWND_TOPMOST : HWND_NOTOPMOST),
      0,
      0,
      0,
      0,
      (interactive ? SWP_NOMOVE | SWP_NOSIZE | SWP_SHOWWINDOW | SWP_FRAMECHANGED
                   : SWP_NOMOVE | SWP_NOSIZE | SWP_NOACTIVATE | SWP_SHOWWINDOW | SWP_FRAMECHANGED));

  if (interactive) {
    ShowWindow(outputHwnd_, SW_SHOW);
    SetForegroundWindow(outputHwnd_);
    SetActiveWindow(outputHwnd_);
    SetFocus(outputHwnd_);
    syncNativeCursorVisibility(outputHwnd_, false);
    log("Remix overlay window input enabled");
    return;
  }

  ShowWindow(outputHwnd_, SW_SHOWNOACTIVATE);
  SetForegroundWindow(sourceHwnd_);
  SetActiveWindow(sourceHwnd_);
  SetFocus(sourceHwnd_);
  log("Remix overlay window input released");
}

bool RemixRenderer::hasWindowFocusLocked() const {
  const HWND foregroundWindow = GetForegroundWindow();
  if (foregroundWindow == nullptr) {
    return false;
  }

  const auto matchesManagedWindow = [foregroundWindow](HWND managedWindow) {
    if (managedWindow == nullptr) {
      return false;
    }

    if (foregroundWindow == managedWindow || IsChild(managedWindow, foregroundWindow)) {
      return true;
    }

    const HWND managedRoot = GetAncestor(managedWindow, GA_ROOT);
    const HWND foregroundRoot = GetAncestor(foregroundWindow, GA_ROOT);
    return managedRoot != nullptr && managedRoot == foregroundRoot;
  };

  return matchesManagedWindow(outputHwnd_) || matchesManagedWindow(sourceHwnd_);
}

bool RemixRenderer::hasWindowFocus() const {
  std::scoped_lock lock(mutex_);
  return hasWindowFocusLocked();
}

bool RemixRenderer::isVirtualKeyDown(std::uint32_t virtualKey) const {
  if (virtualKey > 0xFFu) {
    return false;
  }

  std::scoped_lock lock(mutex_);
  if (!hasWindowFocusLocked()) {
    return false;
  }

  return (GetAsyncKeyState(static_cast<int>(virtualKey)) & 0x8000) != 0;
}

HWND RemixRenderer::resolveNativeMouseWindowLocked() const {
  if (outputHwnd_ != nullptr && (singleNativeOutputWindow_ || standaloneOutputWindow_ || outputWindowInteractive_)) {
    return outputHwnd_;
  }
  return sourceHwnd_;
}

bool RemixRenderer::getNativeMouseClientRectLocked(
    HWND mouseWindow,
    RECT& clientRect,
    RECT& clientRectScreenSpace) const {
  if (mouseWindow == nullptr || !IsWindow(mouseWindow)) {
    return false;
  }

  if (!GetClientRect(mouseWindow, &clientRect)) {
    return false;
  }

  POINT topLeft {clientRect.left, clientRect.top};
  POINT bottomRight {clientRect.right, clientRect.bottom};
  if (!ClientToScreen(mouseWindow, &topLeft) || !ClientToScreen(mouseWindow, &bottomRight)) {
    return false;
  }

  clientRectScreenSpace.left = topLeft.x;
  clientRectScreenSpace.top = topLeft.y;
  clientRectScreenSpace.right = bottomRight.x;
  clientRectScreenSpace.bottom = bottomRight.y;
  return true;
}

bool RemixRenderer::pollNativeMouseState(
    std::int32_t& x,
    std::int32_t& y,
    std::int32_t& deltaX,
    std::int32_t& deltaY,
    std::int32_t& dWheel,
    std::uint32_t& buttonsMask,
    std::int32_t& windowHeight) {
  std::scoped_lock lock(mutex_);

  // Raw input is created and drained HERE, on the game thread (this JNI poll runs
  // on the LWJGL/AWT input thread). The Remix present worker thread is not a real
  // desktop input thread, so WM_INPUT bound to a window it owns never delivered.
  // Creating the raw window on this thread binds it to a true UI input queue.
  if (shouldUseRawMouseInput()) {
    if (ensureRawMouseInputWindow() != nullptr) {
      MSG rawInputMessage {};
      while (PeekMessageW(&rawInputMessage, nullptr, WM_INPUT, WM_INPUT, PM_REMOVE)) {
        TranslateMessage(&rawInputMessage);
        DispatchMessageW(&rawInputMessage);
      }
    }
  }

  HWND mouseWindow = resolveNativeMouseWindowLocked();
  RECT clientRect {};
  RECT clientRectScreenSpace {};
  if (!getNativeMouseClientRectLocked(mouseWindow, clientRect, clientRectScreenSpace)) {
    logVerboseInput("poll failed to resolve client rect mouseWindow=0x" + std::to_string(reinterpret_cast<std::uintptr_t>(mouseWindow)));
    return false;
  }

  const int clientWidth = std::max(1, static_cast<int>(clientRect.right - clientRect.left));
  const int clientHeight = std::max(1, static_cast<int>(clientRect.bottom - clientRect.top));
  windowHeight = clientHeight;
  deltaX = 0;
  deltaY = 0;
  dWheel = static_cast<std::int32_t>(g_nativeMouseWheelDelta.exchange(0, std::memory_order_relaxed));
  buttonsMask = 0;

  if (!hasWindowFocusLocked()) {
    releaseNativeMouseGrabLocked(mouseWindow);
    nativeMouseLastCursorValid_ = false;
    dWheel = 0;
    x = 0;
    y = clientHeight - 1;
    // INPUTSINK keeps WM_INPUT flowing while unfocused; drop that motion so it
    // can't surface as a camera jump on refocus.
    g_rawMouseDeltaX.store(0, std::memory_order_relaxed);
    g_rawMouseDeltaY.store(0, std::memory_order_relaxed);
    logVerboseInput("poll unfocused mouseWindow=0x" + std::to_string(reinterpret_cast<std::uintptr_t>(mouseWindow))
        + " clientWidth=" + std::to_string(clientWidth)
        + " clientHeight=" + std::to_string(clientHeight));
    return true;
  }

  if ((GetAsyncKeyState(VK_LBUTTON) & 0x8000) != 0) {
    buttonsMask |= 1u << 0;
  }
  if ((GetAsyncKeyState(VK_RBUTTON) & 0x8000) != 0) {
    buttonsMask |= 1u << 1;
  }
  if ((GetAsyncKeyState(VK_MBUTTON) & 0x8000) != 0) {
    buttonsMask |= 1u << 2;
  }
  if ((GetAsyncKeyState(VK_XBUTTON1) & 0x8000) != 0) {
    buttonsMask |= 1u << 3;
  }
  if ((GetAsyncKeyState(VK_XBUTTON2) & 0x8000) != 0) {
    buttonsMask |= 1u << 4;
  }

  POINT cursorScreen {};
  if (!GetCursorPos(&cursorScreen)) {
    logVerboseInput("poll GetCursorPos failed");
    return false;
  }

  POINT cursorClient {cursorScreen.x, cursorScreen.y};
  if (!ScreenToClient(mouseWindow, &cursorClient)) {
    logVerboseInput("poll ScreenToClient failed mouseWindow=0x" + std::to_string(reinterpret_cast<std::uintptr_t>(mouseWindow)));
    return false;
  }

  cursorClient.x = static_cast<LONG>(std::clamp<int>(static_cast<int>(cursorClient.x), 0, clientWidth - 1));
  cursorClient.y = static_cast<LONG>(std::clamp<int>(static_cast<int>(cursorClient.y), 0, clientHeight - 1));

  if (nativeMouseGrabbed_) {
    const bool useRawMouse = shouldUseRawMouseInput();
    const bool reactivatedGrab = !nativeMouseGrabActive_ && applyNativeMouseGrabLocked(mouseWindow, clientRect, clientRectScreenSpace);
    POINT centerClient {clientWidth / 2, clientHeight / 2};

    if (useRawMouse) {
      // GLFW steals the process-wide raw mouse registration when it disables the
      // cursor; reclaim it each grabbed poll so WM_INPUT keeps flowing to us.
      ensureRawMouseRegistrationOwned();
    }

    if (useRawMouse) {
      // Raw input accumulates relative motion in g_rawMouseDelta* via WM_INPUT.
      // Deltas come purely from raw motion (no pointer acceleration); the cursor
      // is still recentered each poll only to keep it parked inside the window so
      // it cannot escape or click other windows.
      const long rawDeltaX = g_rawMouseDeltaX.exchange(0, std::memory_order_relaxed);
      const long rawDeltaY = g_rawMouseDeltaY.exchange(0, std::memory_order_relaxed);
      if (reactivatedGrab) {
        deltaX = 0;
        deltaY = 0;
      } else {
        deltaX = static_cast<std::int32_t>(rawDeltaX);
        deltaY = static_cast<std::int32_t>(-rawDeltaY);
      }
      POINT centerScreen {centerClient.x, centerClient.y};
      if (ClientToScreen(mouseWindow, &centerScreen)) {
        SetCursorPos(centerScreen.x, centerScreen.y);
      }
    } else {
      if (reactivatedGrab) {
        cursorClient = centerClient;
        deltaX = 0;
        deltaY = 0;
      } else {
        deltaX = cursorClient.x - centerClient.x;
        deltaY = centerClient.y - cursorClient.y;
      }
      POINT centerScreen {centerClient.x, centerClient.y};
      if (ClientToScreen(mouseWindow, &centerScreen)) {
        SetCursorPos(centerScreen.x, centerScreen.y);
      }
    }

    x = centerClient.x;
    y = clientHeight - 1 - centerClient.y;

    nativeMouseLastCursorPos_ = centerClient;
    nativeMouseLastCursorValid_ = true;
    if (useRawMouse || deltaX != 0 || deltaY != 0 || dWheel != 0 || buttonsMask != 0) {
      logVerboseInput(
          "poll grabbed x=" + std::to_string(x)
          + " y=" + std::to_string(y)
          + " dx=" + std::to_string(deltaX)
          + " dy=" + std::to_string(deltaY)
          + " dWheel=" + std::to_string(dWheel)
          + " buttonsMask=" + std::to_string(buttonsMask)
          + " rawEvents=" + std::to_string(g_rawMouseInputEvents.load(std::memory_order_relaxed))
          + " cursorClientX=" + std::to_string(cursorClient.x)
          + " cursorClientY=" + std::to_string(cursorClient.y));
    }
    return true;
  }

  if (nativeMouseLastCursorValid_) {
    deltaX = cursorClient.x - nativeMouseLastCursorPos_.x;
    deltaY = nativeMouseLastCursorPos_.y - cursorClient.y;
  }

  nativeMouseLastCursorPos_ = cursorClient;
  nativeMouseLastCursorValid_ = true;
  x = cursorClient.x;
  y = clientHeight - 1 - cursorClient.y;
  if (!nativeMouseLastCursorValid_ || deltaX != 0 || deltaY != 0 || dWheel != 0 || buttonsMask != 0) {
    logVerboseInput(
        "poll ungrabbed x=" + std::to_string(x)
        + " y=" + std::to_string(y)
        + " dx=" + std::to_string(deltaX)
        + " dy=" + std::to_string(deltaY)
        + " dWheel=" + std::to_string(dWheel)
        + " buttonsMask=" + std::to_string(buttonsMask)
        + " cursorClientX=" + std::to_string(cursorClient.x)
        + " cursorClientY=" + std::to_string(cursorClient.y));
  }
  return true;
}

void RemixRenderer::releaseNativeMouseGrabLocked(HWND mouseWindow) {
  if (GetCapture() == mouseWindow || GetCapture() == outputHwnd_ || GetCapture() == sourceHwnd_) {
    ReleaseCapture();
  }

  if (nativeMouseGrabActive_) {
    ClipCursor(nullptr);
  }

  const int cursorCounter = ShowCursor(FALSE);
  ShowCursor(TRUE);  // restore
  logVerboseInput(std::string("releaseNativeMouseGrabLocked ShowCursorCounter=") + std::to_string(cursorCounter));

  syncNativeCursorVisibility(mouseWindow, false);
  nativeMouseGrabActive_ = false;
}

bool RemixRenderer::applyNativeMouseGrabLocked(
    HWND mouseWindow,
    const RECT& clientRect,
    const RECT& clientRectScreenSpace) {
  if (mouseWindow == nullptr || !IsWindow(mouseWindow)) {
    return false;
  }

  const int cursorCounter = ShowCursor(FALSE);
  ShowCursor(TRUE);  // restore
  logVerboseInput(std::string("applyNativeMouseGrabLocked ShowCursorCounter=") + std::to_string(cursorCounter));

  const int clientWidth = std::max(1, static_cast<int>(clientRect.right - clientRect.left));
  const int clientHeight = std::max(1, static_cast<int>(clientRect.bottom - clientRect.top));
  POINT centerClient {clientWidth / 2, clientHeight / 2};
  POINT centerScreen {centerClient.x, centerClient.y};

  SetForegroundWindow(mouseWindow);
  SetActiveWindow(mouseWindow);
  SetFocus(mouseWindow);
  SetCapture(mouseWindow);
  ClipCursor(&clientRectScreenSpace);
  syncNativeCursorVisibility(mouseWindow, true);
  if (ClientToScreen(mouseWindow, &centerScreen)) {
    SetCursorPos(centerScreen.x, centerScreen.y);
  }

  nativeMouseLastCursorPos_ = centerClient;
  nativeMouseLastCursorValid_ = true;
  nativeMouseGrabActive_ = true;
  g_rawMouseDeltaX.store(0, std::memory_order_relaxed);
  g_rawMouseDeltaY.store(0, std::memory_order_relaxed);
  return true;
}

bool RemixRenderer::setNativeMouseGrabbed(bool grabbed) {
  std::scoped_lock lock(mutex_);

  HWND mouseWindow = resolveNativeMouseWindowLocked();
  RECT clientRect {};
  RECT clientRectScreenSpace {};
  if (!getNativeMouseClientRectLocked(mouseWindow, clientRect, clientRectScreenSpace)) {
    return false;
  }

  const int cursorCounter = ShowCursor(FALSE);
  ShowCursor(TRUE);  // restore
  nativeMouseGrabbed_ = grabbed;
  g_nativeMouseWheelDelta.exchange(0, std::memory_order_relaxed);
  logVerboseInput(
      std::string("setGrabbed grabbed=") + (grabbed ? "true" : "false")
      + " ShowCursorCounter=" + std::to_string(cursorCounter)
      + " mouseWindow=0x" + std::to_string(reinterpret_cast<std::uintptr_t>(mouseWindow)));

  if (!grabbed) {
    releaseNativeMouseGrabLocked(mouseWindow);
    nativeMouseLastCursorValid_ = false;
    return true;
  }

  return applyNativeMouseGrabLocked(mouseWindow, clientRect, clientRectScreenSpace);
}

bool RemixRenderer::setNativeCursorPosition(std::int32_t x, std::int32_t y) {
  std::scoped_lock lock(mutex_);

  HWND mouseWindow = resolveNativeMouseWindowLocked();
  RECT clientRect {};
  RECT clientRectScreenSpace {};
  if (!getNativeMouseClientRectLocked(mouseWindow, clientRect, clientRectScreenSpace)) {
    logVerboseInput("setCursorPosition failed to resolve client rect mouseWindow=0x" + std::to_string(reinterpret_cast<std::uintptr_t>(mouseWindow)));
    return false;
  }

  const int clientWidth = std::max(1, static_cast<int>(clientRect.right - clientRect.left));
  const int clientHeight = std::max(1, static_cast<int>(clientRect.bottom - clientRect.top));
    POINT cursorClient {
      static_cast<LONG>(std::clamp<int>(static_cast<int>(x), 0, clientWidth - 1)),
      static_cast<LONG>(std::clamp<int>(clientHeight - 1 - static_cast<int>(y), 0, clientHeight - 1))};
  POINT cursorScreen {cursorClient.x, cursorClient.y};
  if (!ClientToScreen(mouseWindow, &cursorScreen)) {
    logVerboseInput("setCursorPosition ClientToScreen failed mouseWindow=0x" + std::to_string(reinterpret_cast<std::uintptr_t>(mouseWindow)));
    return false;
  }
  if (!SetCursorPos(cursorScreen.x, cursorScreen.y)) {
    logVerboseInput("setCursorPosition SetCursorPos failed x=" + std::to_string(cursorScreen.x) + " y=" + std::to_string(cursorScreen.y));
    return false;
  }

  nativeMouseLastCursorPos_ = cursorClient;
  nativeMouseLastCursorValid_ = true;
  logVerboseInput(
      "setCursorPosition x=" + std::to_string(x)
      + " y=" + std::to_string(y)
      + " clientX=" + std::to_string(cursorClient.x)
      + " clientY=" + std::to_string(cursorClient.y));
  return true;
}


}  // namespace mcrtx