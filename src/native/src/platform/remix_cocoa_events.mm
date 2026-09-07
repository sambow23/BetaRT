#include "mcrtx/platform/remix_cocoa_events.hpp"

#import <Cocoa/Cocoa.h>

#include <utility>
#include <cstdint>
#include <dispatch/dispatch.h>

namespace mcrtx {
namespace {

id eventMonitor = nil;
std::function<void()> pumpEvents;
bool pumpingEvents = false;
std::uint64_t pumpGeneration = 0;

}

void withCocoaEventPump(const std::function<void()>& action) {
  const bool previous = pumpingEvents;
  pumpingEvents = true;
  try {
    action();
  } catch (...) {
    pumpingEvents = previous;
    throw;
  }
  pumpingEvents = previous;
}

void installCocoaEventPump(std::function<void()> pump) {
  removeCocoaEventPump();
  pumpEvents = std::move(pump);
  const std::uint64_t generation = pumpGeneration;
  const NSEventMask mask = NSEventMaskKeyDown | NSEventMaskKeyUp | NSEventMaskFlagsChanged
      | NSEventMaskMouseMoved | NSEventMaskLeftMouseDown | NSEventMaskLeftMouseUp
      | NSEventMaskRightMouseDown | NSEventMaskRightMouseUp | NSEventMaskOtherMouseDown
      | NSEventMaskOtherMouseUp | NSEventMaskLeftMouseDragged | NSEventMaskRightMouseDragged
      | NSEventMaskOtherMouseDragged | NSEventMaskScrollWheel;
  eventMonitor = [NSEvent addLocalMonitorForEventsMatchingMask:mask handler:^NSEvent*(NSEvent* event) {
    if (pumpingEvents || !pumpEvents) {
      return event;
    }
    // SDL can service the main queue while polling. Defer out of AWT's callback
    // so a nested render task cannot reacquire the lock held by this event pump.
    dispatch_async(dispatch_get_main_queue(), ^{
      if (generation != pumpGeneration || !pumpEvents) {
        return;
      }
      withCocoaEventPump([&]() {
        [NSApp postEvent:event atStart:YES];
        pumpEvents();
      });
    });
    return nil;
  }];
}

void removeCocoaEventPump() {
  ++pumpGeneration;
  if (eventMonitor != nil) {
    [NSEvent removeMonitor:eventMonitor];
    eventMonitor = nil;
  }
  pumpEvents = {};
}

}
