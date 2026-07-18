import mcrtx.bridge.RemixChunkBridge;

final class RemixChunkBuildSession {
    private static boolean loggedChunkBuild;

    private RemixChunkBuildSession() {
    }

    static boolean begin(
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

        RemixChunkRecaptureQueue.clearSection(originX, originY, originZ);
        RemixChunkWorldState.rememberKnownSection(originX, originY, originZ);

        fd world = RemixChunkWorldState.attachedWorld();
        if (world != null && renderPass == 0) {
            RemixCaveCulling.Pocket[] pockets =
                    RemixCaveCulling.computePockets(world, originX, originY, originZ);
            RemixCaveCulling.setPockets(originX, originY, originZ, pockets);
        }

        if (!RemixCameraState.shouldCaptureChunkSection(originX, originY, originZ)) {
            return false;
        }
        if (!RemixCaveCulling.isVisible(originX, originY, originZ)) {
            return false;
        }

        boolean captureActive = RemixChunkBridge.beginChunkBuild(
                originX, originY, originZ, sizeX, sizeY, sizeZ, renderPass);
        if (captureActive) {
            RemixChunkWorldState.markSectionResident(originX, originY, originZ);
        }
        return captureActive;
    }

    static void captureBlock(
            ew blockAccess,
            int blockX,
            int blockY,
            int blockZ,
            int blockId,
            int blockMetadata,
            int renderType) {
        RemixChunkBlockCapture.capture(
                blockAccess, blockX, blockY, blockZ, blockId, blockMetadata, renderType);
    }

    static void end(boolean emittedGeometry) {
        RemixChunkBridge.endChunkBuild(emittedGeometry, false);
    }
}
