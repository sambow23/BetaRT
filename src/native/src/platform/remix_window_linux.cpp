// Native Remix window and input integration for Linux.

#include "mcrtx/core/remix_renderer.hpp"
#include "mcrtx/core/remix_render_common.hpp"

#include <algorithm>
#include <array>
#include <cmath>
#include <cstdint>
#include <utility>

namespace mcrtx {

using namespace mcrtx::detail;

namespace {

constexpr std::uint32_t kVirtualKeyShift = 0x10;
constexpr std::uint32_t kVirtualKeyControl = 0x11;
constexpr std::uint32_t kVirtualKeyMenu = 0x12;
constexpr std::uint32_t kVirtualKeyBack = 0x08;
constexpr std::uint32_t kVirtualKeyTab = 0x09;
constexpr std::uint32_t kVirtualKeyReturn = 0x0d;
constexpr std::uint32_t kVirtualKeyEscape = 0x1b;
constexpr std::uint32_t kVirtualKeySpace = 0x20;
constexpr std::uint32_t kVirtualKeyLeft = 0x25;
constexpr std::uint32_t kVirtualKeyUp = 0x26;
constexpr std::uint32_t kVirtualKeyRight = 0x27;
constexpr std::uint32_t kVirtualKeyDown = 0x28;
constexpr std::uint32_t kVirtualKeyF1 = 0x70;
constexpr std::uint32_t kVirtualKeyLeftShift = 0xa0;
constexpr std::uint32_t kVirtualKeyRightShift = 0xa1;
constexpr std::uint32_t kVirtualKeyLeftControl = 0xa2;
constexpr std::uint32_t kVirtualKeyRightControl = 0xa3;
constexpr std::uint32_t kVirtualKeyLeftMenu = 0xa4;
constexpr std::uint32_t kVirtualKeyRightMenu = 0xa5;

using KeyMapping = std::pair<std::uint32_t, remixapi_Key>;

constexpr std::array<KeyMapping, 63> kKeyMappings {{
    {0x41, REMIXAPI_KEY_A},
    {0x42, REMIXAPI_KEY_B},
    {0x43, REMIXAPI_KEY_C},
    {0x44, REMIXAPI_KEY_D},
    {0x45, REMIXAPI_KEY_E},
    {0x46, REMIXAPI_KEY_F},
    {0x47, REMIXAPI_KEY_G},
    {0x48, REMIXAPI_KEY_H},
    {0x49, REMIXAPI_KEY_I},
    {0x4a, REMIXAPI_KEY_J},
    {0x4b, REMIXAPI_KEY_K},
    {0x4c, REMIXAPI_KEY_L},
    {0x4d, REMIXAPI_KEY_M},
    {0x4e, REMIXAPI_KEY_N},
    {0x4f, REMIXAPI_KEY_O},
    {0x50, REMIXAPI_KEY_P},
    {0x51, REMIXAPI_KEY_Q},
    {0x52, REMIXAPI_KEY_R},
    {0x53, REMIXAPI_KEY_S},
    {0x54, REMIXAPI_KEY_T},
    {0x55, REMIXAPI_KEY_U},
    {0x56, REMIXAPI_KEY_V},
    {0x57, REMIXAPI_KEY_W},
    {0x58, REMIXAPI_KEY_X},
    {0x59, REMIXAPI_KEY_Y},
    {0x5a, REMIXAPI_KEY_Z},
    {0x30, REMIXAPI_KEY_0},
    {0x31, REMIXAPI_KEY_1},
    {0x32, REMIXAPI_KEY_2},
    {0x33, REMIXAPI_KEY_3},
    {0x34, REMIXAPI_KEY_4},
    {0x35, REMIXAPI_KEY_5},
    {0x36, REMIXAPI_KEY_6},
    {0x37, REMIXAPI_KEY_7},
    {0x38, REMIXAPI_KEY_8},
    {0x39, REMIXAPI_KEY_9},
    {kVirtualKeyEscape, REMIXAPI_KEY_ESCAPE},
    {kVirtualKeyReturn, REMIXAPI_KEY_ENTER},
    {kVirtualKeyTab, REMIXAPI_KEY_TAB},
    {kVirtualKeyBack, REMIXAPI_KEY_BACKSPACE},
    {kVirtualKeySpace, REMIXAPI_KEY_SPACE},
    {kVirtualKeyLeft, REMIXAPI_KEY_LEFT},
    {kVirtualKeyRight, REMIXAPI_KEY_RIGHT},
    {kVirtualKeyUp, REMIXAPI_KEY_UP},
    {kVirtualKeyDown, REMIXAPI_KEY_DOWN},
    {kVirtualKeyLeftShift, REMIXAPI_KEY_LEFT_SHIFT},
    {kVirtualKeyRightShift, REMIXAPI_KEY_RIGHT_SHIFT},
    {kVirtualKeyLeftControl, REMIXAPI_KEY_LEFT_CONTROL},
    {kVirtualKeyRightControl, REMIXAPI_KEY_RIGHT_CONTROL},
    {kVirtualKeyLeftMenu, REMIXAPI_KEY_LEFT_ALT},
    {kVirtualKeyRightMenu, REMIXAPI_KEY_RIGHT_ALT},
    {kVirtualKeyF1 + 0, REMIXAPI_KEY_F1},
    {kVirtualKeyF1 + 1, REMIXAPI_KEY_F2},
    {kVirtualKeyF1 + 2, REMIXAPI_KEY_F3},
    {kVirtualKeyF1 + 3, REMIXAPI_KEY_F4},
    {kVirtualKeyF1 + 4, REMIXAPI_KEY_F5},
    {kVirtualKeyF1 + 5, REMIXAPI_KEY_F6},
    {kVirtualKeyF1 + 6, REMIXAPI_KEY_F7},
    {kVirtualKeyF1 + 7, REMIXAPI_KEY_F8},
    {kVirtualKeyF1 + 8, REMIXAPI_KEY_F9},
    {kVirtualKeyF1 + 9, REMIXAPI_KEY_F10},
    {kVirtualKeyF1 + 10, REMIXAPI_KEY_F11},
    {kVirtualKeyF1 + 11, REMIXAPI_KEY_F12},
}};

std::uint32_t toLwjglButtonMask(std::uint32_t remixButtons) {
  std::uint32_t result = 0;
  if ((remixButtons & REMIXAPI_MOUSE_BUTTON_LEFT_BIT) != 0) {
    result |= 1u << 0;
  }
  if ((remixButtons & REMIXAPI_MOUSE_BUTTON_RIGHT_BIT) != 0) {
    result |= 1u << 1;
  }
  if ((remixButtons & REMIXAPI_MOUSE_BUTTON_MIDDLE_BIT) != 0) {
    result |= 1u << 2;
  }
  if ((remixButtons & REMIXAPI_MOUSE_BUTTON_X1_BIT) != 0) {
    result |= 1u << 3;
  }
  if ((remixButtons & REMIXAPI_MOUSE_BUTTON_X2_BIT) != 0) {
    result |= 1u << 4;
  }
  return result;
}

}  // namespace

bool RemixRenderer::createOutputWindow(NativeWindowHandle sourceWindow) {
  (void)sourceWindow;
  return true;
}

void RemixRenderer::destroyOutputWindow() {
}

void RemixRenderer::updateOutputWindowSize() {
}

void RemixRenderer::syncOutputWindowInteractivity(remixapi_UIState uiState) {
  (void)uiState;
}

void RemixRenderer::applyNativeWindowCommandsLocked() {
  if (pendingMouseGrabUpdate_) {
    const remixapi_ErrorCode result = remix_.SetMouseGrabbed(nativeMouseGrabbed_ ? TRUE : FALSE);
    if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
      setError("SetMouseGrabbed failed: " + errorCodeToString(result));
    }
    pendingMouseGrabUpdate_ = false;
    nativeMouseState_.relativeX = 0.0f;
    nativeMouseState_.relativeY = 0.0f;
  }

  if (pendingCursorPositionUpdate_) {
    const remixapi_ErrorCode result = remix_.SetCursorPosition(pendingCursorX_, pendingCursorY_);
    if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
      setError("SetCursorPosition failed: " + errorCodeToString(result));
    }
    pendingCursorPositionUpdate_ = false;
  }

  if (pendingFullscreenUpdate_) {
    const remixapi_ErrorCode result = remix_.SetFullscreen(pendingFullscreen_ ? TRUE : FALSE);
    if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
      setError("SetFullscreen failed: " + errorCodeToString(result));
    } else {
      outputWindowFullscreen_ = pendingFullscreen_;
    }
    pendingFullscreenUpdate_ = false;
  }
}

void RemixRenderer::updateNativeKeyboardStateLocked() {
  nativeVirtualKeyDown_.fill(false);
  for (const KeyMapping& mapping : kKeyMappings) {
    if (mapping.first == 0) {
      continue;
    }
    remixapi_Bool isDown = FALSE;
    if (remix_.IsKeyDown(mapping.second, &isDown) == REMIXAPI_ERROR_CODE_SUCCESS) {
      nativeVirtualKeyDown_[mapping.first] = isDown != FALSE;
    }
  }
  nativeVirtualKeyDown_[kVirtualKeyShift] = nativeVirtualKeyDown_[kVirtualKeyLeftShift]
      || nativeVirtualKeyDown_[kVirtualKeyRightShift];
  nativeVirtualKeyDown_[kVirtualKeyControl] = nativeVirtualKeyDown_[kVirtualKeyLeftControl]
      || nativeVirtualKeyDown_[kVirtualKeyRightControl];
  nativeVirtualKeyDown_[kVirtualKeyMenu] = nativeVirtualKeyDown_[kVirtualKeyLeftMenu]
      || nativeVirtualKeyDown_[kVirtualKeyRightMenu];
}

bool RemixRenderer::updateNativeWindowStateLocked() {
  remixapi_WindowState windowState {};
  remixapi_ErrorCode result = remix_.GetWindowState(&windowState);
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("GetWindowState failed: " + errorCodeToString(result));
    return false;
  }

  remixapi_MouseState mouseState {};
  result = remix_.PollMouseState(&mouseState);
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("PollMouseState failed: " + errorCodeToString(result));
    return false;
  }

  nativeWindowState_ = windowState;
  nativeMouseState_.x = mouseState.x;
  nativeMouseState_.y = mouseState.y;
  nativeMouseState_.relativeX += mouseState.relativeX;
  nativeMouseState_.relativeY += mouseState.relativeY;
  nativeMouseState_.wheelX += mouseState.wheelX;
  nativeMouseState_.wheelY += mouseState.wheelY;
  nativeMouseState_.buttons = mouseState.buttons;
  outputWindowFullscreen_ = windowState.fullscreen != FALSE;
  if (windowState.drawableWidth != 0 && windowState.drawableHeight != 0) {
    width_ = windowState.drawableWidth;
    height_ = windowState.drawableHeight;
    camera_.aspect = static_cast<float>(width_) / static_cast<float>(height_);
  }
  updateNativeKeyboardStateLocked();
  return true;
}

void RemixRenderer::pumpOutputWindowMessages() {
  const remixapi_ErrorCode result = remix_.PumpEvents();
  if (result != REMIXAPI_ERROR_CODE_SUCCESS) {
    setError("PumpEvents failed: " + errorCodeToString(result));
    return;
  }
  applyNativeWindowCommandsLocked();
  updateNativeWindowStateLocked();
}

bool RemixRenderer::hasWindowFocusLocked() const {
  return nativeWindowState_.focused != FALSE;
}

bool RemixRenderer::hasWindowFocus() const {
  std::scoped_lock lock(mutex_);
  return initialized_ && hasWindowFocusLocked();
}

bool RemixRenderer::isVirtualKeyDown(std::uint32_t virtualKey) const {
  std::scoped_lock lock(mutex_);
  return initialized_
      && virtualKey < nativeVirtualKeyDown_.size()
      && nativeVirtualKeyDown_[virtualKey];
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
  if (!initialized_) {
    return false;
  }

  const std::int32_t logicalHeight = std::max<std::int32_t>(1, static_cast<std::int32_t>(height_));
  x = static_cast<std::int32_t>(std::lround(nativeMouseState_.x));
  y = logicalHeight - 1 - static_cast<std::int32_t>(std::lround(nativeMouseState_.y));
  deltaX = static_cast<std::int32_t>(std::lround(nativeMouseState_.relativeX));
  deltaY = -static_cast<std::int32_t>(std::lround(nativeMouseState_.relativeY));
  dWheel = static_cast<std::int32_t>(std::lround(nativeMouseState_.wheelY * 120.0f));
  buttonsMask = toLwjglButtonMask(nativeMouseState_.buttons);
  windowHeight = logicalHeight;
  nativeMouseState_.relativeX = 0.0f;
  nativeMouseState_.relativeY = 0.0f;
  nativeMouseState_.wheelX = 0.0f;
  nativeMouseState_.wheelY = 0.0f;
  return true;
}

bool RemixRenderer::setNativeMouseGrabbed(bool grabbed) {
  std::scoped_lock lock(mutex_);
  if (!initialized_ || remix_.SetMouseGrabbed == nullptr) {
    return false;
  }
  nativeMouseGrabbed_ = grabbed;
  pendingMouseGrabUpdate_ = true;
  standaloneWorkerPresentRequested_ = true;
  standaloneWorkerEvent_.notify_all();
  return true;
}

bool RemixRenderer::setNativeCursorPosition(std::int32_t x, std::int32_t y) {
  std::scoped_lock lock(mutex_);
  if (!initialized_ || remix_.SetCursorPosition == nullptr) {
    return false;
  }
  pendingCursorX_ = static_cast<float>(x);
  pendingCursorY_ = static_cast<float>(std::max<std::int32_t>(0, static_cast<std::int32_t>(height_) - 1 - y));
  pendingCursorPositionUpdate_ = true;
  standaloneWorkerPresentRequested_ = true;
  standaloneWorkerEvent_.notify_all();
  return true;
}

bool RemixRenderer::setOutputWindowFullscreen(bool fullscreen) {
  std::scoped_lock lock(mutex_);
  if (!initialized_ || remix_.SetFullscreen == nullptr) {
    return false;
  }
  pendingFullscreen_ = fullscreen;
  pendingFullscreenUpdate_ = true;
  standaloneWorkerPresentRequested_ = true;
  standaloneWorkerEvent_.notify_all();
  return true;
}

bool RemixRenderer::isOutputCloseRequested() const {
  std::scoped_lock lock(mutex_);
  return initialized_ && nativeWindowState_.closeRequested != FALSE;
}

std::uint32_t RemixRenderer::getOutputWindowWidth() const {
  std::scoped_lock lock(mutex_);
  return nativeWindowState_.drawableWidth != 0 ? nativeWindowState_.drawableWidth : width_;
}

std::uint32_t RemixRenderer::getOutputWindowHeight() const {
  std::scoped_lock lock(mutex_);
  return nativeWindowState_.drawableHeight != 0 ? nativeWindowState_.drawableHeight : height_;
}

}  // namespace mcrtx
