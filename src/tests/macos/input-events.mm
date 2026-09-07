#import <Cocoa/Cocoa.h>
#include <jni.h>
#include <dispatch/dispatch.h>
extern "C" JNIEXPORT void JNICALL Java_MacInputCheck_pauseMainQueue(JNIEnv*, jclass) {
 dispatch_semaphore_t started = dispatch_semaphore_create(0);
 dispatch_async(dispatch_get_main_queue(), ^{
  dispatch_semaphore_signal(started);
  [NSThread sleepForTimeInterval:0.5];
 });
 dispatch_semaphore_wait(started, DISPATCH_TIME_FOREVER);
}
extern "C" JNIEXPORT void JNICALL Java_MacInputCheck_send(JNIEnv*, jclass, jint code, jboolean down, jboolean alt) {
 dispatch_sync(dispatch_get_main_queue(), ^{
  NSWindow* window = NSApp.keyWindow;
  NSEvent* event = [NSEvent keyEventWithType:code == 58 ? NSEventTypeFlagsChanged : (down ? NSEventTypeKeyDown : NSEventTypeKeyUp)
    location:NSZeroPoint modifierFlags:alt ? NSEventModifierFlagOption : 0 timestamp:NSProcessInfo.processInfo.systemUptime
    windowNumber:window.windowNumber context:nil characters:code==13 ? @"w" : (code==7 ? @"x" : @"b")
    charactersIgnoringModifiers:code==13 ? @"w" : (code==7 ? @"x" : @"b") isARepeat:NO keyCode:code];
  [NSApp sendEvent:event];
 });
 // The event monitor queues SDL delivery. Wait for that delivery explicitly;
 // Display.update no longer waits on unrelated GLFW calls to drain Cocoa.
 dispatch_sync(dispatch_get_main_queue(), ^{});
}
extern "C" JNIEXPORT void JNICALL Java_MacInputCheck_activate(JNIEnv*, jclass) {
 dispatch_sync(dispatch_get_main_queue(), ^{
  [NSApp activateIgnoringOtherApps:YES];
  for(NSWindow* window in NSApp.windows) {if(window.visible){[window makeKeyAndOrderFront:nil];break;}}
 });
}

extern "C" JNIEXPORT void JNICALL Java_MacInputCheck_move(JNIEnv*, jclass, jint index) {
 dispatch_sync(dispatch_get_main_queue(), ^{
  NSWindow* window = NSApp.keyWindow;
  NSEvent* event = [NSEvent mouseEventWithType:NSEventTypeMouseMoved
    location:NSMakePoint(50 + index % 300, 100 + index % 150) modifierFlags:0
    timestamp:NSProcessInfo.processInfo.systemUptime windowNumber:window.windowNumber
    context:nil eventNumber:index clickCount:0 pressure:0];
  // Let AWT consume this outside a main dispatch callback, as real motion does.
  [NSApp postEvent:event atStart:NO];
 });
}
