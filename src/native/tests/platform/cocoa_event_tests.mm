#include "mcrtx/platform/remix_cocoa_events.hpp"
#include <SDL3/SDL.h>
#import <Cocoa/Cocoa.h>
#include <cstdlib>
#include <iostream>
#include <vector>

static void require(bool condition, const char* message) {
  if (!condition) {
    std::cerr << message << ": " << SDL_GetError() << '\n';
    std::exit(1);
  }
}

int main(int argc, char**) {
  @autoreleasepool {
    // Match embedding in AWT/GLFW: SDL does not own the NSApplication subclass.
    [NSApplication sharedApplication];
    [NSApp setActivationPolicy:NSApplicationActivationPolicyRegular];
    [NSApp finishLaunching];
    require(SDL_Init(SDL_INIT_VIDEO), "SDL startup failed");
    SDL_Window* window = SDL_CreateWindow("BetaRT input regression test", 320, 200, 0);
    require(window != nullptr, "Window creation failed");
    NSWindow* cocoa = (__bridge NSWindow*)SDL_GetPointerProperty(
        SDL_GetWindowProperties(window), SDL_PROP_WINDOW_COCOA_WINDOW_POINTER, nullptr);
    [cocoa makeKeyAndOrderFront:nil];
    [NSApp activateIgnoringOtherApps:YES];
    std::vector<SDL_Event> keys;
    bool inExternalDispatch = false;
    unsigned int pumpCount = 0;
    auto pump = [&]() {
      require(!inExternalDispatch, "SDL pump reentered an external Cocoa callback");
      ++pumpCount;
      SDL_Event event;
      while (SDL_PollEvent(&event)) {
        if (event.type == SDL_EVENT_KEY_DOWN || event.type == SDL_EVENT_KEY_UP) {
          keys.push_back(event);
        }
      }
    };
    for (int i = 0; i < 100 && SDL_GetKeyboardFocus() != window; ++i) {
      mcrtx::withCocoaEventPump(pump);
      [NSRunLoop.currentRunLoop runUntilDate:[NSDate dateWithTimeIntervalSinceNow:0.01]];
    }
    require(SDL_GetKeyboardFocus() == window, "Test window did not receive focus");
    if (argc == 1) {
      mcrtx::installCocoaEventPump(pump);
    }
    auto send = [&](NSEventType type, unsigned short code, NSEventModifierFlags flags, NSString* characters, bool external) {
      NSEvent* event = [NSEvent keyEventWithType:type location:NSZeroPoint modifierFlags:flags
          timestamp:NSProcessInfo.processInfo.systemUptime windowNumber:cocoa.windowNumber
          context:nil characters:characters charactersIgnoringModifiers:characters isARepeat:NO keyCode:code];
      if (external) {
        // Simulate another framework consuming the event before SDL_PollEvent.
        const auto previousCount = pumpCount;
        inExternalDispatch = true;
        [NSApp sendEvent:event];
        inExternalDispatch = false;
        for (int i = 0; i < 100 && pumpCount == previousCount; ++i) {
          [NSRunLoop.currentRunLoop runUntilDate:[NSDate dateWithTimeIntervalSinceNow:0.001]];
        }
      } else {
        [NSApp postEvent:event atStart:YES];
        mcrtx::withCocoaEventPump(pump);
      }
    };
    keys.clear();
    for (int i = 0; i < 20; ++i) {
      send(NSEventTypeKeyDown, 13, 0, @"w", (i % 2) == 0);
      require(SDL_GetKeyboardState(nullptr)[SDL_SCANCODE_W], "W press was lost");
      send(NSEventTypeKeyUp, 13, 0, @"w", (i % 2) != 0);
      require(!SDL_GetKeyboardState(nullptr)[SDL_SCANCODE_W], "W release was lost");
    }
    require(keys.size() == 40, "Keys were duplicated or dropped between event pumps");
    send(NSEventTypeFlagsChanged, 58, NSEventModifierFlagOption, @"", true);
    require(SDL_GetKeyboardState(nullptr)[SDL_SCANCODE_LALT], "Option modifier was lost");
    for (auto pair : {std::pair<unsigned short, NSString*>{7, @"x"}, {11, @"b"}}) {
      send(NSEventTypeKeyDown, pair.first, NSEventModifierFlagOption, pair.second, true);
      require((keys.back().key.mod & SDL_KMOD_ALT) != 0, "Menu hotkey lost its Alt modifier");
      send(NSEventTypeKeyUp, pair.first, NSEventModifierFlagOption, pair.second, false);
    }
    send(NSEventTypeFlagsChanged, 58, 0, @"", true);
    require(!SDL_GetKeyboardState(nullptr)[SDL_SCANCODE_LALT], "Option release was lost");
    // Mouse events take the same external AWT path that triggered the deadlock.
    for (int i = 0; i < 100; ++i) {
      NSEvent* motion = [NSEvent mouseEventWithType:NSEventTypeMouseMoved
          location:NSMakePoint(20 + i, 30) modifierFlags:0
          timestamp:NSProcessInfo.processInfo.systemUptime windowNumber:cocoa.windowNumber
          context:nil eventNumber:i clickCount:0 pressure:0];
      inExternalDispatch = true;
      [NSApp sendEvent:motion];
      inExternalDispatch = false;
    }
    const auto previousCount = pumpCount;
    for (int i = 0; i < 100 && pumpCount < previousCount + 100; ++i) {
      [NSRunLoop.currentRunLoop runUntilDate:[NSDate dateWithTimeIntervalSinceNow:0.001]];
    }
    require(pumpCount >= previousCount + 100, "Deferred mouse events did not drain");
    // Pending work from a removed monitor must not reach a new renderer lifetime.
    NSEvent* pending = [NSEvent keyEventWithType:NSEventTypeKeyDown location:NSZeroPoint modifierFlags:0
        timestamp:NSProcessInfo.processInfo.systemUptime windowNumber:cocoa.windowNumber
        context:nil characters:@"w" charactersIgnoringModifiers:@"w" isARepeat:NO keyCode:13];
    [NSApp sendEvent:pending];
    const auto beforeRemoval = pumpCount;
    mcrtx::removeCocoaEventPump();
    [NSRunLoop.currentRunLoop runUntilDate:[NSDate dateWithTimeIntervalSinceNow:0.01]];
    require(pumpCount == beforeRemoval, "Removed monitor retained a pending callback");
    SDL_DestroyWindow(window);
    SDL_Quit();
    std::cout << "PASS: 20 mixed-pump press/release cycles and Option+X/B, without duplicates; 100 deferred mouse events and shutdown cancellation\n";
  }
}
