package mcrtx.bridge;

public interface MinecraftPlatform {
    String backendId();

    long resolveCurrentWindowHandle();

    boolean isWindowActive();

    boolean isKeyDown(MinecraftPlatformKey key);
}