// Output-window creation, placement, fullscreen, and interactivity.

#include "mcrtx/core/remix_renderer.hpp"
#include "mcrtx/platform/remix_window_internals.hpp"
#include "mcrtx/core/remix_render_common.hpp"

#include <array>
#include <cstddef>
#include <cstdint>
#include <sstream>

namespace mcrtx {

using namespace mcrtx::detail;
using namespace mcrtx::window_detail;

namespace {

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
      getRemixWindowTitle(),
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

bool RemixRenderer::setOutputWindowFullscreen(bool fullscreen) {
  if (outputHwnd_ == nullptr) {
    return false;
  }

  if (fullscreen == outputWindowFullscreen_) {
    return true;
  }

  if (fullscreen) {
    outputWindowedStyle_ = GetWindowLongW(outputHwnd_, GWL_STYLE);
    outputWindowedExStyle_ = GetWindowLongW(outputHwnd_, GWL_EXSTYLE);
    GetWindowRect(outputHwnd_, &outputWindowedRect_);

    MONITORINFO monitorInfo {sizeof(MONITORINFO)};
    HMONITOR monitor = MonitorFromWindow(outputHwnd_, MONITOR_DEFAULTTONEAREST);
    if (monitor == nullptr || !GetMonitorInfoW(monitor, &monitorInfo)) {
      return false;
    }

    SetWindowLongW(
        outputHwnd_,
        GWL_STYLE,
        outputWindowedStyle_ & ~(WS_CAPTION | WS_THICKFRAME | WS_MINIMIZEBOX | WS_MAXIMIZEBOX | WS_SYSMENU));
    SetWindowLongW(
        outputHwnd_,
        GWL_EXSTYLE,
        outputWindowedExStyle_ & ~(WS_EX_DLGMODALFRAME | WS_EX_WINDOWEDGE | WS_EX_CLIENTEDGE | WS_EX_STATICEDGE));

    const RECT& monitorRect = monitorInfo.rcMonitor;
    SetWindowPos(
        outputHwnd_,
        HWND_TOP,
        monitorRect.left,
        monitorRect.top,
        monitorRect.right - monitorRect.left,
        monitorRect.bottom - monitorRect.top,
        SWP_NOOWNERZORDER | SWP_FRAMECHANGED | SWP_SHOWWINDOW);

    outputWindowFullscreen_ = true;
  } else {
    SetWindowLongW(outputHwnd_, GWL_STYLE, outputWindowedStyle_);
    SetWindowLongW(outputHwnd_, GWL_EXSTYLE, outputWindowedExStyle_);

    SetWindowPos(
        outputHwnd_,
        HWND_NOTOPMOST,
        outputWindowedRect_.left,
        outputWindowedRect_.top,
        outputWindowedRect_.right - outputWindowedRect_.left,
        outputWindowedRect_.bottom - outputWindowedRect_.top,
        SWP_NOOWNERZORDER | SWP_FRAMECHANGED | SWP_SHOWWINDOW);

    outputWindowFullscreen_ = false;
  }

  return true;
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


}  // namespace mcrtx
