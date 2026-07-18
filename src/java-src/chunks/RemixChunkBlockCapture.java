import mcrtx.bridge.RemixChunkBridge;

final class RemixChunkBlockCapture {
    private static final int WATER_STILL_BLOCK_ID = 8;
    private static final int WATER_FLOWING_BLOCK_ID = 9;
    private static final int LAVA_STILL_BLOCK_ID = 10;
    private static final int LAVA_FLOWING_BLOCK_ID = 11;

    private RemixChunkBlockCapture() {
    }

    static void capture(
            ew blockAccess,
            int blockX,
            int blockY,
            int blockZ,
            int blockId,
            int blockMetadata,
            int renderType) {
        captureBlock(blockAccess, blockX, blockY, blockZ, blockId, blockMetadata, renderType);
    }

    static void captureWorldBlock(
            fd world,
            int blockX,
            int blockY,
            int blockZ,
            int blockId,
            int blockMetadata,
            int renderType) {
        captureBlock(world, blockX, blockY, blockZ, blockId, blockMetadata, renderType);
    }

    private static void captureBlock(
            xp blockAccess,
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

        blockDefinition.a(blockAccess, blockX, blockY, blockZ);

        int liquidVisibilityMask = 0x3F;
        float liquidHeight0 = 1.0f;
        float liquidHeight1 = 1.0f;
        float liquidHeight2 = 1.0f;
        float liquidHeight3 = 1.0f;
        float liquidFlowAngle = -1000.0f;

        if (isLiquidBlock(blockId) && renderType == 4) {
            liquidVisibilityMask = computeLiquidVisibilityMask(
                    blockDefinition, blockAccess, blockX, blockY, blockZ);
            liquidHeight0 = computeLiquidCornerHeight(
                    blockAccess, blockX, blockY, blockZ, blockDefinition.bA);
            liquidHeight1 = computeLiquidCornerHeight(
                    blockAccess, blockX + 1, blockY, blockZ, blockDefinition.bA);
            liquidHeight2 = computeLiquidCornerHeight(
                    blockAccess, blockX + 1, blockY, blockZ + 1, blockDefinition.bA);
            liquidHeight3 = computeLiquidCornerHeight(
                    blockAccess, blockX, blockY, blockZ + 1, blockDefinition.bA);
            liquidFlowAngle = (float) rp.a(blockAccess, blockX, blockY, blockZ, blockDefinition.bA);
        }

        RemixChunkBridge.captureBlock(
                blockX,
                blockY,
                blockZ,
                blockId,
                blockMetadata,
                renderType,
                blockDefinition.a(blockAccess, blockX, blockY, blockZ, 0),
                blockDefinition.a(blockAccess, blockX, blockY, blockZ, 1),
                blockDefinition.a(blockAccess, blockX, blockY, blockZ, 2),
                blockDefinition.a(blockAccess, blockX, blockY, blockZ, 3),
                blockDefinition.a(blockAccess, blockX, blockY, blockZ, 4),
                blockDefinition.a(blockAccess, blockX, blockY, blockZ, 5),
                (float) blockDefinition.bs,
                (float) blockDefinition.bt,
                (float) blockDefinition.bu,
                (float) blockDefinition.bv,
                (float) blockDefinition.bw,
                (float) blockDefinition.bx,
                blockDefinition.b(blockAccess, blockX, blockY, blockZ),
                liquidVisibilityMask,
                liquidHeight0,
                liquidHeight1,
                liquidHeight2,
                liquidHeight3,
                liquidFlowAngle);
    }

    private static boolean isWaterBlock(int blockId) {
        return blockId == WATER_STILL_BLOCK_ID || blockId == WATER_FLOWING_BLOCK_ID;
    }

    private static boolean isLavaBlock(int blockId) {
        return blockId == LAVA_STILL_BLOCK_ID || blockId == LAVA_FLOWING_BLOCK_ID;
    }

    private static boolean isLiquidBlock(int blockId) {
        return isWaterBlock(blockId) || isLavaBlock(blockId);
    }

    private static int computeLiquidVisibilityMask(
            uu blockDefinition, xp blockAccess, int blockX, int blockY, int blockZ) {
        int visibilityMask = 0;
        if (blockDefinition.b(blockAccess, blockX, blockY - 1, blockZ, 0)) {
            visibilityMask |= 1 << 0;
        }
        if (blockDefinition.b(blockAccess, blockX, blockY + 1, blockZ, 1)) {
            visibilityMask |= 1 << 1;
        }
        if (blockDefinition.b(blockAccess, blockX, blockY, blockZ - 1, 2)) {
            visibilityMask |= 1 << 2;
        }
        if (blockDefinition.b(blockAccess, blockX, blockY, blockZ + 1, 3)) {
            visibilityMask |= 1 << 3;
        }
        if (blockDefinition.b(blockAccess, blockX - 1, blockY, blockZ, 4)) {
            visibilityMask |= 1 << 4;
        }
        if (blockDefinition.b(blockAccess, blockX + 1, blockY, blockZ, 5)) {
            visibilityMask |= 1 << 5;
        }
        return visibilityMask;
    }

    private static float computeLiquidCornerHeight(
            xp blockAccess, int blockX, int blockY, int blockZ, ln material) {
        int sampleWeight = 0;
        float accumulatedHeight = 0.0f;

        for (int sampleIndex = 0; sampleIndex < 4; sampleIndex++) {
            int sampleX = blockX - (sampleIndex & 1);
            int sampleZ = blockZ - (sampleIndex >> 1 & 1);
            if (blockAccess.f(sampleX, blockY + 1, sampleZ) == material) {
                return 1.0f;
            }

            ln sampleMaterial = blockAccess.f(sampleX, blockY, sampleZ);
            if (sampleMaterial == material) {
                int sampleLevel = blockAccess.e(sampleX, blockY, sampleZ);
                if (sampleLevel >= 8 || sampleLevel == 0) {
                    accumulatedHeight += rp.d(sampleLevel) * 10.0f;
                    sampleWeight += 10;
                }
                accumulatedHeight += rp.d(sampleLevel);
                sampleWeight += 1;
                continue;
            }

            if (!sampleMaterial.a()) {
                accumulatedHeight += 1.0f;
                sampleWeight += 1;
            }
        }

        if (sampleWeight == 0) {
            return 1.0f;
        }
        return 1.0f - accumulatedHeight / (float) sampleWeight;
    }
}
