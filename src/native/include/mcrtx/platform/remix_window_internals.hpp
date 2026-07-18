#pragma once

#include <atomic>

#include <windows.h>

namespace mcrtx::window_detail {

HMODULE getCurrentModuleHandle();
bool shouldUseSingleNativeOutputWindow();
bool shouldUseStandaloneOutputWindow();
bool shouldUseOverlayOutputWindow(bool* usedLegacyEnvVar = nullptr);
bool shouldUseRawMouseInput();
bool getSourceClientRectInScreenSpace(HWND sourceHwnd, RECT& rect);
LRESULT CALLBACK remixOutputWindowProc(HWND hwnd, UINT message, WPARAM wParam, LPARAM lParam);
bool ensureOutputWindowClassRegistered();
void accumulateRawMouseInput(LPARAM lParam);
HWND ensureRawMouseInputWindow();
bool ensureRawMouseRegistrationOwned();
void syncNativeCursorVisibility(HWND mouseWindow, bool hidden);

extern std::atomic_bool g_outputWindowInteractive;
extern std::atomic_bool g_nativeMouseCursorHidden;
extern std::atomic_long g_nativeMouseWheelDelta;
extern std::atomic_long g_rawMouseDeltaX;
extern std::atomic_long g_rawMouseDeltaY;
extern std::atomic_long g_rawMouseInputEvents;
extern std::atomic_bool g_outputWindowCloseRequested;
extern std::atomic_int g_outputWindowClientWidth;
extern std::atomic_int g_outputWindowClientHeight;
extern const wchar_t kRemixWindowClassName[];
const wchar_t* getRemixWindowTitle();

}  // namespace mcrtx::window_detail
