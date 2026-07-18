import mcrtx.bridge.HookProfiler;

public final class MinecraftRemixChunkHooks {
    private MinecraftRemixChunkHooks() {
    }

    public static void onChunkSectionUnload(int originX, int originY, int originZ) {
        long __perf = HookProfiler.begin();
        try {
            RemixChunkCapture.onChunkSectionUnload(originX, originY, originZ);
        } finally {
            HookProfiler.endHook("hook.onChunkSectionUnload", __perf);
        }
    }

    public static boolean onChunkBuildBegin(
            int originX,
            int originY,
            int originZ,
            int sizeX,
            int sizeY,
            int sizeZ,
            int renderPass) {
        long __perf = HookProfiler.begin();
        try {
            return RemixChunkCapture.onChunkBuildBegin(originX, originY, originZ, sizeX, sizeY, sizeZ, renderPass);
        } finally {
            HookProfiler.endHook("hook.onChunkBuildBegin", __perf);
        }
    }

    public static void onChunkBlock(
            ew blockAccess,
            int blockX,
            int blockY,
            int blockZ,
            int blockId,
            int blockMetadata,
            int renderType) {
        long __perf = HookProfiler.begin();
        try {
            RemixChunkCapture.onChunkBlock(blockAccess, blockX, blockY, blockZ, blockId, blockMetadata, renderType);
        } finally {
            HookProfiler.endHook("hook.onChunkBlock", __perf);
        }
    }

    public static void onChunkBuildEnd(boolean emittedGeometry) {
        long __perf = HookProfiler.begin();
        try {
            RemixChunkCapture.onChunkBuildEnd(emittedGeometry);
        } finally {
            HookProfiler.endHook("hook.onChunkBuildEnd", __perf);
        }
    }
}
