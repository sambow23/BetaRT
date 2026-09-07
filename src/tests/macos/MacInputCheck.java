import mcrtx.bridge.RemixLifecycleBridge;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.input.Keyboard;
public class MacInputCheck {
 static native void send(int code, boolean down, boolean alt);
 static native void activate();
 static native void move(int index);
 static native void pauseMainQueue();
 static void check(boolean ok,String message){if(!ok)throw new AssertionError(message);}
 public static void main(String[] args)throws Exception{
  System.load(args[0]);Display.setDisplayMode(new DisplayMode(854,480));Display.create();
  check(!MinecraftRemixSceneHooks.shouldSuppressWorldRasterDisplayLists(),"Vanilla terrain available before Remix startup");
  check(RemixLifecycleBridge.initializeForCurrentDisplay(854,480),RemixLifecycleBridge.lastError());
  check(MinecraftRemixSceneHooks.shouldSuppressWorldRasterDisplayLists(),"Unsafe macOS terrain display-list replay disabled");
  activate();Thread.sleep(300);RemixLifecycleBridge.setUiState(0);
  check(RemixLifecycleBridge.hasNativeWindowFocus(),"SDL window focus");
  pauseMainQueue();
  long updateStart = System.nanoTime();
  Display.update();Display.getDisplayMode();Display.isActive();
  long updateMillis = (System.nanoTime() - updateStart) / 1000000;
  check(updateMillis < 350,"Display polling blocked on Cocoa queue: " + updateMillis + "ms");
  System.out.println("DISPLAY_POLL_MS=" + updateMillis);
  for(int i=0;i<20;i++){
   send(13,true,false);Display.update();check(Keyboard.isKeyDown(17),"W press "+i);
   check(Keyboard.next()&&Keyboard.getEventKey()==17&&Keyboard.getEventKeyState(),"W down event "+i);
   send(13,false,false);Display.update();check(!Keyboard.isKeyDown(17),"W release "+i);
   check(Keyboard.next()&&Keyboard.getEventKey()==17&&!Keyboard.getEventKeyState(),"W up event "+i);
   check(!Keyboard.next(),"Duplicate keyboard event "+i);
  }
  send(58,true,true);Display.update();check(RemixLifecycleBridge.isNativeVirtualKeyDown(0x12),"Option down");
  send(7,true,true);Thread.sleep(200);send(7,false,true);send(58,false,false);Thread.sleep(200);
  check(!RemixLifecycleBridge.isNativeVirtualKeyDown(0x12),"Option up");
  check(RemixLifecycleBridge.getUiState()!=0,"Option+X opens Remix menu");
  RemixLifecycleBridge.setUiState(0);
  send(58,true,true);send(11,true,true);Display.update();
  check(RemixLifecycleBridge.isNativeVirtualKeyDown(0x12)&&RemixLifecycleBridge.isNativeVirtualKeyDown(0x42),"Option+B visible to BetaRT");
  send(11,false,true);send(58,false,false);
  long beforeMotion = RemixLifecycleBridge.getSubmittedFrameCount();
  for (int i=0;i<500;i++) {
   move(i);Display.update();RemixLifecycleBridge.present();Thread.sleep(2);
  }
  check(RemixLifecycleBridge.getSubmittedFrameCount()>beforeMotion+10,"Rendering stalled during mouse movement");
  System.out.println("MOUSE_FRAMES="+(RemixLifecycleBridge.getSubmittedFrameCount()-beforeMotion));
  System.out.println("PASS_JAVA_INPUT: 20 W cycles, Option+X Remix toggle, Option+B chord, modifier release, 500 mouse movements during rendering");
  RemixLifecycleBridge.shutdown();Display.destroy();System.exit(0);
 }
}
