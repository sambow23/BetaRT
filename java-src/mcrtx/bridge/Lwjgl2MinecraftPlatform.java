package mcrtx.bridge;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

final class Lwjgl2MinecraftPlatform implements MinecraftPlatform {
    @Override
    public String backendId() {
        return "lwjgl2";
    }

    @Override
    public long resolveCurrentWindowHandle() {
        return LwjglWindowHandleResolver.resolveCurrentHwnd();
    }

    @Override
    public boolean isWindowActive() {
        return Display.isActive();
    }

    @Override
    public boolean isKeyDown(MinecraftPlatformKey key) {
        if (key == null) {
            return false;
        }

        switch (key) {
            case LEFT_ALT:
                return Keyboard.isKeyDown(Keyboard.KEY_LMENU);
            case RIGHT_ALT:
                return Keyboard.isKeyDown(Keyboard.KEY_RMENU);
            case X:
                return Keyboard.isKeyDown(Keyboard.KEY_X);
            default:
                return false;
        }
    }
}