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
bool RemixRenderer::createOutputWindow(HWND sourceHwnd) {
  if (outputHwnd_ != nullptr) {
    updateOutputWindowSize();
    ShowWindow(outputHwnd_, (standaloneOutputWindow_ || outputWindowInteractive_) ? SW_SHOW : SW_SHOWNOACTIVATE);
    return true;
  }

  if (!ensureOutputWindowClassRegistered()) {
    setError("Failed to register Remix output window class");
    return false;
  }

  RECT sourceClientRect {};
  const bool hasSourceClientRect = getSourceClientRectInScreenSpace(sourceHwnd, sourceClientRect);
  DWORD exStyle = standaloneOutputWindow_ ? 0 : WS_EX_NOACTIVATE;
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

  outputWindowInteractive_ = false;
  g_outputWindowInteractive.store(false, std::memory_order_relaxed);

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
  ShowWindow(outputHwnd_, standaloneOutputWindow_ ? SW_SHOW : SW_SHOWNOACTIVATE);
  UpdateWindow(outputHwnd_);
  log(overlayOutputWindow_
      ? "Created Remix client-area overlay window"
      : (standaloneOutputWindow_
          ? "Created Remix standalone window"
          : "Created Remix detached development window"));
  return true;
}

void RemixRenderer::destroyOutputWindow() {
  if (outputHwnd_ == nullptr) {
    return;
  }

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
  if (standaloneOutputWindow_) {
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