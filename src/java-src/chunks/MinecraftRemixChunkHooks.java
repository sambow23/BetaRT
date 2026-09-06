import mcrtx.bridge.HookProfiler;

public final class MinecraftRemixChunkHooks {
    private MinecraftRemixChunkHooks() {
    }

    public static void onChunkSectionPosition(dk renderer) {
        RemixChunkWorldState.position(renderer);
    }

    public static void onChunkSectionUnload(dk renderer) {
        RemixChunkWorldState.unload(renderer);
    }

    public static void onChunkUpdateStart(int originX, int originY, int originZ) {
        long __perf = HookProfiler.begin();
        try {
            RemixChunkCapture.onChunkUpdateStart(originX, originY, originZ);
        } finally {
            HookProfiler.endHook("hook.onChunkUpdateStart", __perf);
        }
    }

}
