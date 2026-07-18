// Native focus, keyboard, mouse polling, grab, and cursor behavior.

#include "mcrtx/core/remix_renderer.hpp"
#include "mcrtx/platform/remix_window_internals.hpp"
#include "mcrtx/core/runtime_config.hpp"

#include <algorithm>
#include <atomic>
#include <cstddef>
#include <cstdint>
#include <iostream>
#include <string>

namespace mcrtx {

using namespace mcrtx::detail;
using namespace mcrtx::window_detail;

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

}  // namespace

void window_detail::syncNativeCursorVisibility(HWND mouseWindow, bool hidden) {
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
