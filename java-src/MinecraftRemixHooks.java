import mcrtx.bridge.MinecraftRenderHooks;

public final class MinecraftRemixHooks {
    private static boolean loggedDisplayCreate;
    private static boolean loggedDisplayReset;
    private static boolean loggedPresent;
    private static boolean loggedChunkBuild;

    static {
        System.out.println("[mcrtx] MinecraftRemixHooks loaded");
    }

    private MinecraftRemixHooks() {
    }

    public static void onDisplayCreated(int width, int height) {
        if (!loggedDisplayCreate) {
            loggedDisplayCreate = true;
            System.out.println("[mcrtx] onDisplayCreated width=" + width + " height=" + height);
        }
        MinecraftRenderHooks.initializeForCurrentDisplay(width, height);
    }

    public static void onShutdown() {
        System.out.println("[mcrtx] onShutdown");
        MinecraftRenderHooks.shutdown();
    }

    public static void onDisplayReset(int width, int height) {
        if (!loggedDisplayReset) {
            loggedDisplayReset = true;
            System.out.println("[mcrtx] onDisplayReset width=" + width + " height=" + height);
        }
        MinecraftRenderHooks.reinitializeForCurrentDisplay(width, height);
    }

    public static void onResize(int width, int height) {
        MinecraftRenderHooks.resize(width, height);
    }

    public static void onCamera(ls entity, float partialTicks, int width, int height, float farPlane) {
        if (entity == null) {
            return;
        }
        bt position = entity.e(partialTicks);
        bt forward = entity.f(partialTicks);
        float aspect = height <= 0 ? 1.0f : (float) width / (float) height;
        MinecraftRenderHooks.updateCamera(
                (float) position.a,
                (float) (position.b + (double) entity.w()),
                (float) position.c,
                (float) forward.a,
                (float) forward.b,
                (float) forward.c,
                70.0f,
                aspect,
                0.05f,
                farPlane * 2.0f);
    }

    public static void onPresent() {
        if (!loggedPresent) {
            loggedPresent = true;
            System.out.println("[mcrtx] onPresent");
        }
        MinecraftRenderHooks.present();
    }

    public static boolean onChunkBuildBegin(
            int originX,
            int originY,
            int originZ,
            int sizeX,
            int sizeY,
            int sizeZ,
            int renderPass) {
        if (!loggedChunkBuild) {
            loggedChunkBuild = true;
            System.out.println("[mcrtx] chunk build hook active");
        }
        return MinecraftRenderHooks.beginChunkBuild(originX, originY, originZ, sizeX, sizeY, sizeZ, renderPass);
    }

    public static void onChunkBlock(
            ew blockAccess,
            int blockX,
            int blockY,
            int blockZ,
            int blockId,
            int blockMetadata,
            int renderType) {
        uu blockDefinition = blockId >= 0 && blockId < uu.m.length ? uu.m[blockId] : null;
        if (blockDefinition == null || blockAccess == null) {
            return;
        }

        MinecraftRenderHooks.captureBlock(
                blockX,
                blockY,
                blockZ,
                blockId,
                blockMetadata,
                renderType,
                blockDefinition.a(blockAccess, blockX, blockY, blockZ, 0) & 0xFF,
                blockDefinition.a(blockAccess, blockX, blockY, blockZ, 1) & 0xFF,
                blockDefinition.a(blockAccess, blockX, blockY, blockZ, 2) & 0xFF,
                blockDefinition.a(blockAccess, blockX, blockY, blockZ, 3) & 0xFF,
                blockDefinition.a(blockAccess, blockX, blockY, blockZ, 4) & 0xFF,
                blockDefinition.a(blockAccess, blockX, blockY, blockZ, 5) & 0xFF,
                blockDefinition.b(blockAccess, blockX, blockY, blockZ));
    }

    public static void onChunkBuildEnd(boolean emittedGeometry) {
        MinecraftRenderHooks.endChunkBuild(emittedGeometry);
    }
}