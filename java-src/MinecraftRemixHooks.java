import mcrtx.bridge.MinecraftRenderHooks;
import java.nio.FloatBuffer;
import java.util.Iterator;
import java.util.LinkedHashSet;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

public final class MinecraftRemixHooks {
    private static final int CHUNK_DIMENSION = 16;
    private static final int MAX_RECAPTURE_SECTIONS_PER_PRESENT = 8;
    private static final int MAX_DYNAMIC_BONES = 256;
    private static final int FIRST_PERSON_DYNAMIC_ENTITY_ID = Integer.MAX_VALUE - 1;
    private static final int GL_CURRENT_COLOR = 0x0B00;
    private static final int GL_MODELVIEW_MATRIX = 0x0BA6;
    private static final int WATER_STILL_BLOCK_ID = 8;
    private static final int WATER_FLOWING_BLOCK_ID = 9;
    private static final FloatBuffer MODEL_VIEW_BUFFER = BufferUtils.createFloatBuffer(16);
    private static final FloatBuffer COLOR_BUFFER = BufferUtils.createFloatBuffer(16);
    private static final RemixWorldListener WORLD_LISTENER = new RemixWorldListener();
    private static final LinkedHashSet<DirtyChunkSection> PENDING_RECAPTURE_SECTIONS = new LinkedHashSet<DirtyChunkSection>();
    private static boolean loggedDisplayCreate;
    private static boolean loggedDisplayReset;
    private static boolean loggedPresent;
    private static boolean loggedChunkBuild;
    private static boolean loggedDynamicEntityHookFailure;
    private static boolean loggedDynamicEntityBoneOverflow;
    private static boolean loggedWorldListenerAttach;
    private static float cameraPositionX;
    private static float cameraPositionY;
    private static float cameraPositionZ;
    private static float cameraForwardX = 0.0f;
    private static float cameraForwardY = 0.0f;
    private static float cameraForwardZ = 1.0f;
    private static float cameraUpX = 0.0f;
    private static float cameraUpY = 1.0f;
    private static float cameraUpZ = 0.0f;
    private static float cameraRightX = 1.0f;
    private static float cameraRightY = 0.0f;
    private static float cameraRightZ = 0.0f;
    private static boolean dynamicEntityActive;
    private static int activeDynamicEntityId = -1;
    private static String activeDynamicEntityTexture = "";
    private static int nextDynamicBoneIndex;
    private static boolean firstPersonActive;
    private static String activeFirstPersonTexture = "";
    private static fd attachedWorld;

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
        cameraPositionX = (float) position.a;
        cameraPositionY = (float) (position.b + (double) entity.w());
        cameraPositionZ = (float) position.c;

        float fx = (float) forward.a;
        float fy = (float) forward.b;
        float fz = (float) forward.c;
        float forwardLength = (float) Math.sqrt(fx * fx + fy * fy + fz * fz);
        if (forwardLength > 0.0f) {
            cameraForwardX = fx / forwardLength;
            cameraForwardY = fy / forwardLength;
            cameraForwardZ = fz / forwardLength;
        }

        float upx = 0.0f;
        float upy = 1.0f;
        float upz = 0.0f;
        if (Math.abs(cameraForwardY) > 0.99f) {
            upx = 0.0f;
            upy = 0.0f;
            upz = 1.0f;
        }

        float rx = cameraForwardY * upz - cameraForwardZ * upy;
        float ry = cameraForwardZ * upx - cameraForwardX * upz;
        float rz = cameraForwardX * upy - cameraForwardY * upx;
        float rightLength = (float) Math.sqrt(rx * rx + ry * ry + rz * rz);
        if (rightLength > 0.0f) {
            cameraRightX = rx / rightLength;
            cameraRightY = ry / rightLength;
            cameraRightZ = rz / rightLength;
        }

        float ux = cameraRightY * cameraForwardZ - cameraRightZ * cameraForwardY;
        float uy = cameraRightZ * cameraForwardX - cameraRightX * cameraForwardZ;
        float uz = cameraRightX * cameraForwardY - cameraRightY * cameraForwardX;
        float upLength = (float) Math.sqrt(ux * ux + uy * uy + uz * uz);
        if (upLength > 0.0f) {
            cameraUpX = ux / upLength;
            cameraUpY = uy / upLength;
            cameraUpZ = uz / upLength;
        }

        float aspect = height <= 0 ? 1.0f : (float) width / (float) height;
        MinecraftRenderHooks.updateCamera(
                cameraPositionX,
                cameraPositionY,
                cameraPositionZ,
                cameraForwardX,
                cameraForwardY,
                cameraForwardZ,
                70.0f,
                aspect,
                0.05f,
                farPlane * 2.0f);
    }

    public static void onPresent() {
        flushPendingChunkRecaptures();
        if (!loggedPresent) {
            loggedPresent = true;
            System.out.println("[mcrtx] onPresent");
        }
        MinecraftRenderHooks.present();
    }

    public static void onWorldChanged(fd world) {
        if (attachedWorld == world) {
            return;
        }

        PENDING_RECAPTURE_SECTIONS.clear();
        MinecraftRenderHooks.clearWorldScene();

        if (attachedWorld != null) {
            attachedWorld.b(WORLD_LISTENER);
        }

        attachedWorld = world;
        if (attachedWorld != null) {
            attachedWorld.a(WORLD_LISTENER);
            if (!loggedWorldListenerAttach) {
                loggedWorldListenerAttach = true;
                System.out.println("[mcrtx] world listener attached");
            }
        }
    }

    public static void onCloudRender(net.minecraft.client.Minecraft minecraft, fd world, int cloudTick, float partialTicks, boolean fancy) {
        if (!MinecraftRenderHooks.isInitialized()) {
            return;
        }

        if (minecraft == null || world == null || world.t == null || world.t.c || minecraft.i == null) {
            MinecraftRenderHooks.clearCloudLayer();
            return;
        }

        bt cloudColor = world.c(partialTicks);
        float colorR = (float) cloudColor.a;
        float colorG = (float) cloudColor.b;
        float colorB = (float) cloudColor.c;
        if (minecraft.z.g) {
            float grayscale = (colorR * 30.0f + colorG * 59.0f + colorB * 11.0f) / 100.0f;
            float greenWeighted = (colorR * 30.0f + colorG * 70.0f) / 100.0f;
            float blueWeighted = (colorR * 30.0f + colorB * 70.0f) / 100.0f;
            colorR = grayscale;
            colorG = greenWeighted;
            colorB = blueWeighted;
        }

        ls entity = minecraft.i;
        float cameraX = (float) (entity.aJ + (entity.aM - entity.aJ) * (double) partialTicks);
        float cameraY = (float) (entity.bm + (entity.aN - entity.bm) * (double) partialTicks);
        float cameraZ = (float) (entity.aL + (entity.aO - entity.aL) * (double) partialTicks);
        float cloudHeight = world.t.d() + 0.33f;
        float cloudScroll = ((float) cloudTick + partialTicks) * 0.03f;

        MinecraftRenderHooks.updateCloudLayer(
                fancy,
                cameraX,
                cameraY,
                cameraZ,
                cloudHeight,
                cloudScroll,
                colorR,
                colorG,
                colorB);
    }

    public static void onLivingEntityFrameBegin() {
        MinecraftRenderHooks.beginDynamicEntityFrame();
        MinecraftRenderHooks.beginDestroyOverlayFrame();
        MinecraftRenderHooks.beginParticleFrame();
    }

    public static void onDestroyOverlayRender(int blockX, int blockY, int blockZ, float destroyProgress) {
        if (!MinecraftRenderHooks.isInitialized() || attachedWorld == null) {
            return;
        }

        int blockId = attachedWorld.a(blockX, blockY, blockZ);
        if (blockId <= 0 || blockId >= uu.m.length) {
            return;
        }

        uu blockDefinition = uu.m[blockId];
        if (blockDefinition == null) {
            return;
        }

        int destroyStage = (int) (destroyProgress * 10.0f);
        if (destroyStage < 0) {
            destroyStage = 0;
        } else if (destroyStage > 9) {
            destroyStage = 9;
        }

        MinecraftRenderHooks.captureDestroyOverlay(
                blockX,
                blockY,
                blockZ,
                blockId,
                attachedWorld.e(blockX, blockY, blockZ),
                blockDefinition.b(),
                destroyStage);
    }

    public static void onParticleRender(xw particle, float partialTicks, float f3, float f4, float f5, float f6, float f7) {
        if (!MinecraftRenderHooks.isInitialized() || particle == null) {
            return;
        }

        int textureKind = particle.c_();
        if (textureKind < 0 || textureKind > 3) {
            return;
        }

        float minU = (float) (particle.b % 16) / 16.0f;
        float maxU = minU + 0.0624375f;
        float minV = (float) (particle.b / 16) / 16.0f;
        float maxV = minV + 0.0624375f;
        float particleScale = 0.1f * particle.g;
        float originX = (float) (particle.aJ + (particle.aM - particle.aJ) * (double) partialTicks - xw.l);
        float originY = (float) (particle.aK + (particle.aN - particle.aK) * (double) partialTicks - xw.m);
        float originZ = (float) (particle.aL + (particle.aO - particle.aL) * (double) partialTicks - xw.n);
        float brightness = particle.a(partialTicks);
        int colorRgba = packColor(particle.i * brightness, particle.j * brightness, particle.k * brightness, 1.0f);

        MinecraftRenderHooks.captureParticleQuad(
                originX - f3 * particleScale - f6 * particleScale,
                originY - f4 * particleScale,
                originZ - f5 * particleScale - f7 * particleScale,
                maxU,
                maxV,
                originX - f3 * particleScale + f6 * particleScale,
                originY + f4 * particleScale,
                originZ - f5 * particleScale + f7 * particleScale,
                maxU,
                minV,
                originX + f3 * particleScale + f6 * particleScale,
                originY + f4 * particleScale,
                originZ + f5 * particleScale + f7 * particleScale,
                minU,
                minV,
                originX + f3 * particleScale - f6 * particleScale,
                originY - f4 * particleScale,
                originZ + f5 * particleScale - f7 * particleScale,
                minU,
                maxV,
                colorRgba,
                textureKind);
    }

    public static void onLivingEntityRenderStart(sn entity) {
        if (!MinecraftRenderHooks.isInitialized() || entity == null) {
            return;
        }
        dynamicEntityActive = true;
        activeDynamicEntityId = entity.aD;
        activeDynamicEntityTexture = "";
        nextDynamicBoneIndex = 0;
        MinecraftRenderHooks.beginDynamicEntity(entity.aD);
    }

    public static void onLivingEntityRenderEnd() {
        if (!dynamicEntityActive) {
            return;
        }
        MinecraftRenderHooks.endDynamicEntity();
        dynamicEntityActive = false;
        activeDynamicEntityId = -1;
        activeDynamicEntityTexture = "";
        nextDynamicBoneIndex = 0;
    }

    public static void onFirstPersonRenderStart() {
        if (!MinecraftRenderHooks.isInitialized()) {
            return;
        }

        firstPersonActive = true;
        activeFirstPersonTexture = "/mob/char.png";
        nextDynamicBoneIndex = 0;
        MinecraftRenderHooks.beginDynamicEntity(FIRST_PERSON_DYNAMIC_ENTITY_ID);
        MinecraftRenderHooks.setDynamicEntityTexture(activeFirstPersonTexture);
    }

    public static void onFirstPersonRenderEnd() {
        if (!firstPersonActive) {
            return;
        }

        MinecraftRenderHooks.endDynamicEntity();
        firstPersonActive = false;
        activeFirstPersonTexture = "";
        nextDynamicBoneIndex = 0;
    }

    public static void onFirstPersonItemRender(iz itemStack) {
        if (!firstPersonActive || itemStack == null) {
            return;
        }

        activeFirstPersonTexture = itemStack.c < 256 ? "/terrain.png" : "/gui/items.png";
        MinecraftRenderHooks.setDynamicEntityTexture(activeFirstPersonTexture);
    }

    public static void onEntityTextureBind(String primaryTexture, String fallbackTexture) {
        if (!dynamicEntityActive) {
            return;
        }
        String resolvedTexture = normalizeDynamicTexturePath(primaryTexture, fallbackTexture);
        if (resolvedTexture.isEmpty() || resolvedTexture.equals(activeDynamicEntityTexture)) {
            return;
        }
        activeDynamicEntityTexture = resolvedTexture;
        MinecraftRenderHooks.setDynamicEntityTexture(resolvedTexture);
    }

    public static void onModelPartRender(tz[] polygons, float scale) {
        String activeTexture = activeCaptureTexture();
        if (activeTexture.isEmpty() || polygons == null || polygons.length == 0) {
            return;
        }
        if (!GL11.glIsEnabled(GL11.GL_TEXTURE_2D)) {
            return;
        }
        try {
            MODEL_VIEW_BUFFER.clear();
            GL11.glGetFloat(GL_MODELVIEW_MATRIX, MODEL_VIEW_BUFFER);
            float[] modelView = new float[16];
            MODEL_VIEW_BUFFER.get(modelView);

            COLOR_BUFFER.clear();
            GL11.glGetFloat(GL_CURRENT_COLOR, COLOR_BUFFER);
            float[] color = new float[4];
            COLOR_BUFFER.get(color);

            float[] modelToWorld = multiplyColumnMajor(buildInverseViewMatrix(), modelView);
            float[] capturedColor = sanitizeDynamicEntityColor(color[0], color[1], color[2], color[3]);
            int colorRgba = packColor(capturedColor[0], capturedColor[1], capturedColor[2], capturedColor[3]);
            int boneIndex = allocateDynamicBoneIndex();
            if (boneIndex < 0) {
                return;
            }
            submitDynamicBoneTransform(boneIndex, modelToWorld);

            for (tz polygon : polygons) {
                if (polygon == null || polygon.a == null || polygon.a.length != 4) {
                    continue;
                }

                float[][] positions = new float[4][3];
                float[][] texcoords = new float[4][2];
                for (int vertexIndex = 0; vertexIndex < 4; vertexIndex++) {
                    ib vertex = polygon.a[vertexIndex];
                    if (vertex == null || vertex.a == null) {
                        continue;
                    }
                    positions[vertexIndex][0] = (float) vertex.a.a * scale;
                    positions[vertexIndex][1] = (float) vertex.a.b * scale;
                    positions[vertexIndex][2] = (float) vertex.a.c * scale;
                    texcoords[vertexIndex][0] = vertex.b;
                    texcoords[vertexIndex][1] = vertex.c;
                }

                MinecraftRenderHooks.captureDynamicEntityQuad(
                        positions[0][0], positions[0][1], positions[0][2], texcoords[0][0], texcoords[0][1],
                        positions[1][0], positions[1][1], positions[1][2], texcoords[1][0], texcoords[1][1],
                        positions[2][0], positions[2][1], positions[2][2], texcoords[2][0], texcoords[2][1],
                        positions[3][0], positions[3][1], positions[3][2], texcoords[3][0], texcoords[3][1],
                        colorRgba,
                        boneIndex);
            }
        } catch (RuntimeException exception) {
            MinecraftRenderHooks.endDynamicEntity();
            if (!loggedDynamicEntityHookFailure) {
                loggedDynamicEntityHookFailure = true;
                System.err.println("[mcrtx] dynamic entity capture disabled after hook failure");
                exception.printStackTrace();
            }
            dynamicEntityActive = false;
            activeDynamicEntityId = -1;
            activeDynamicEntityTexture = "";
            firstPersonActive = false;
            activeFirstPersonTexture = "";
        }
    }

    public static void onFirstPersonTessellatorDraw(
            int[] rawVertexData,
            int vertexCount,
            int drawMode,
            boolean hasTexture,
            boolean hasColor) {
        if (!firstPersonActive || activeFirstPersonTexture.isEmpty()) {
            return;
        }
        if (rawVertexData == null || vertexCount < 6 || drawMode != 7 || !hasTexture) {
            return;
        }
        if (vertexCount % 6 != 0) {
            return;
        }
        if (!GL11.glIsEnabled(GL11.GL_TEXTURE_2D)) {
            return;
        }

        try {
            MODEL_VIEW_BUFFER.clear();
            GL11.glGetFloat(GL_MODELVIEW_MATRIX, MODEL_VIEW_BUFFER);
            float[] modelView = new float[16];
            MODEL_VIEW_BUFFER.get(modelView);

            COLOR_BUFFER.clear();
            GL11.glGetFloat(GL_CURRENT_COLOR, COLOR_BUFFER);
            float[] currentColor = new float[4];
            COLOR_BUFFER.get(currentColor);

            float[] modelToWorld = multiplyColumnMajor(buildInverseViewMatrix(), modelView);
            int fallbackColorRgba = sanitizePackedColor(packColor(currentColor[0], currentColor[1], currentColor[2], currentColor[3]));
            int boneIndex = allocateDynamicBoneIndex();
            if (boneIndex < 0) {
                return;
            }
            submitDynamicBoneTransform(boneIndex, modelToWorld);

            for (int vertexIndex = 0; vertexIndex + 5 < vertexCount; vertexIndex += 6) {
                int quadColor = hasColor ? sanitizePackedColor(unpackTessellatorColor(rawVertexData, vertexIndex * 8 + 5)) : fallbackColorRgba;

                float p0x = Float.intBitsToFloat(rawVertexData[vertexIndex * 8]);
                float p0y = Float.intBitsToFloat(rawVertexData[vertexIndex * 8 + 1]);
                float p0z = Float.intBitsToFloat(rawVertexData[vertexIndex * 8 + 2]);
                float p1x = Float.intBitsToFloat(rawVertexData[(vertexIndex + 1) * 8]);
                float p1y = Float.intBitsToFloat(rawVertexData[(vertexIndex + 1) * 8 + 1]);
                float p1z = Float.intBitsToFloat(rawVertexData[(vertexIndex + 1) * 8 + 2]);
                float p2x = Float.intBitsToFloat(rawVertexData[(vertexIndex + 2) * 8]);
                float p2y = Float.intBitsToFloat(rawVertexData[(vertexIndex + 2) * 8 + 1]);
                float p2z = Float.intBitsToFloat(rawVertexData[(vertexIndex + 2) * 8 + 2]);
                float p3x = Float.intBitsToFloat(rawVertexData[(vertexIndex + 5) * 8]);
                float p3y = Float.intBitsToFloat(rawVertexData[(vertexIndex + 5) * 8 + 1]);
                float p3z = Float.intBitsToFloat(rawVertexData[(vertexIndex + 5) * 8 + 2]);

                MinecraftRenderHooks.captureDynamicEntityQuad(
                        p0x, p0y, p0z, Float.intBitsToFloat(rawVertexData[vertexIndex * 8 + 3]), Float.intBitsToFloat(rawVertexData[vertexIndex * 8 + 4]),
                        p1x, p1y, p1z, Float.intBitsToFloat(rawVertexData[(vertexIndex + 1) * 8 + 3]), Float.intBitsToFloat(rawVertexData[(vertexIndex + 1) * 8 + 4]),
                        p2x, p2y, p2z, Float.intBitsToFloat(rawVertexData[(vertexIndex + 2) * 8 + 3]), Float.intBitsToFloat(rawVertexData[(vertexIndex + 2) * 8 + 4]),
                        p3x, p3y, p3z, Float.intBitsToFloat(rawVertexData[(vertexIndex + 5) * 8 + 3]), Float.intBitsToFloat(rawVertexData[(vertexIndex + 5) * 8 + 4]),
                        quadColor,
                        boneIndex);
            }
        } catch (RuntimeException exception) {
            MinecraftRenderHooks.endDynamicEntity();
            if (!loggedDynamicEntityHookFailure) {
                loggedDynamicEntityHookFailure = true;
                System.err.println("[mcrtx] dynamic entity capture disabled after hook failure");
                exception.printStackTrace();
            }
            dynamicEntityActive = false;
            activeDynamicEntityId = -1;
            activeDynamicEntityTexture = "";
            firstPersonActive = false;
            activeFirstPersonTexture = "";
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

        blockDefinition.a(blockAccess, blockX, blockY, blockZ);

        int liquidVisibilityMask = 0x3F;
        float liquidHeight0 = 1.0f;
        float liquidHeight1 = 1.0f;
        float liquidHeight2 = 1.0f;
        float liquidHeight3 = 1.0f;
        float liquidFlowAngle = -1000.0f;

        if (isWaterBlock(blockId) && renderType == 4) {
            liquidVisibilityMask = computeLiquidVisibilityMask(blockDefinition, blockAccess, blockX, blockY, blockZ);
            liquidHeight0 = computeLiquidCornerHeight(blockAccess, blockX, blockY, blockZ, blockDefinition.bA);
            liquidHeight1 = computeLiquidCornerHeight(blockAccess, blockX + 1, blockY, blockZ, blockDefinition.bA);
            liquidHeight2 = computeLiquidCornerHeight(blockAccess, blockX + 1, blockY, blockZ + 1, blockDefinition.bA);
            liquidHeight3 = computeLiquidCornerHeight(blockAccess, blockX, blockY, blockZ + 1, blockDefinition.bA);
            liquidFlowAngle = (float) rp.a(blockAccess, blockX, blockY, blockZ, blockDefinition.bA);
        }

        MinecraftRenderHooks.captureBlock(
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

    private static void captureWorldBlock(
            fd world,
            int blockX,
            int blockY,
            int blockZ,
            int blockId,
            int blockMetadata,
            int renderType) {
        uu blockDefinition = blockId >= 0 && blockId < uu.m.length ? uu.m[blockId] : null;
        if (blockDefinition == null || world == null) {
            return;
        }

        xp blockAccess = world;
    blockDefinition.a(blockAccess, blockX, blockY, blockZ);

        int liquidVisibilityMask = 0x3F;
        float liquidHeight0 = 1.0f;
        float liquidHeight1 = 1.0f;
        float liquidHeight2 = 1.0f;
        float liquidHeight3 = 1.0f;
        float liquidFlowAngle = -1000.0f;

        if (isWaterBlock(blockId) && renderType == 4) {
            liquidVisibilityMask = computeLiquidVisibilityMask(blockDefinition, world, blockX, blockY, blockZ);
            liquidHeight0 = computeLiquidCornerHeight(world, blockX, blockY, blockZ, blockDefinition.bA);
            liquidHeight1 = computeLiquidCornerHeight(world, blockX + 1, blockY, blockZ, blockDefinition.bA);
            liquidHeight2 = computeLiquidCornerHeight(world, blockX + 1, blockY, blockZ + 1, blockDefinition.bA);
            liquidHeight3 = computeLiquidCornerHeight(world, blockX, blockY, blockZ + 1, blockDefinition.bA);
            liquidFlowAngle = (float) rp.a(world, blockX, blockY, blockZ, blockDefinition.bA);
        }

        MinecraftRenderHooks.captureBlock(
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

    private static int computeLiquidVisibilityMask(uu blockDefinition, xp blockAccess, int blockX, int blockY, int blockZ) {
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

    private static float computeLiquidCornerHeight(xp blockAccess, int blockX, int blockY, int blockZ, ln material) {
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

    public static void onChunkBuildEnd(boolean emittedGeometry) {
        MinecraftRenderHooks.endChunkBuild(emittedGeometry);
    }

    private static boolean recaptureChunkSectionPass(fd world, int originX, int originY, int originZ, int renderPass) {
        if (world == null || !MinecraftRenderHooks.isInitialized() || MinecraftRenderHooks.isChunkBuildCaptureActive()) {
            return false;
        }

        boolean emittedGeometry = false;
        if (!MinecraftRenderHooks.beginChunkBuild(originX, originY, originZ, CHUNK_DIMENSION, CHUNK_DIMENSION, CHUNK_DIMENSION, renderPass)) {
            return false;
        }

        for (int blockY = originY; blockY < originY + CHUNK_DIMENSION; ++blockY) {
            if (blockY < 0 || blockY >= 128) {
                continue;
            }
            for (int blockZ = originZ; blockZ < originZ + CHUNK_DIMENSION; ++blockZ) {
                for (int blockX = originX; blockX < originX + CHUNK_DIMENSION; ++blockX) {
                    int blockId = world.a(blockX, blockY, blockZ);
                    if (blockId <= 0 || blockId >= uu.m.length) {
                        continue;
                    }

                    uu blockDefinition = uu.m[blockId];
                    if (blockDefinition == null) {
                        continue;
                    }

                    if (blockDefinition.b_() != renderPass) {
                        continue;
                    }

                    emittedGeometry = true;
                    captureWorldBlock(
                            world,
                            blockX,
                            blockY,
                            blockZ,
                            blockId,
                            world.e(blockX, blockY, blockZ),
                            blockDefinition.b());
                }
            }
        }

        MinecraftRenderHooks.endChunkBuild(emittedGeometry);
        return true;
    }

    private static boolean recaptureChunkSection(fd world, int originX, int originY, int originZ) {
        if (!recaptureChunkSectionPass(world, originX, originY, originZ, 0)) {
            return false;
        }

        return recaptureChunkSectionPass(world, originX, originY, originZ, 1);
    }

    private static void queueRecaptureRegion(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        if (attachedWorld == null) {
            return;
        }

        minX -= 1;
        minY -= 1;
        minZ -= 1;
        maxX += 1;
        maxY += 1;
        maxZ += 1;

        if (maxY < 0 || minY >= 128) {
            return;
        }

        minY = Math.max(minY, 0);
        maxY = Math.min(maxY, 127);

        int originMinX = (minX >> 4) << 4;
        int originMinY = (minY >> 4) << 4;
        int originMinZ = (minZ >> 4) << 4;
        int originMaxX = (maxX >> 4) << 4;
        int originMaxY = (maxY >> 4) << 4;
        int originMaxZ = (maxZ >> 4) << 4;

        for (int originY = originMinY; originY <= originMaxY; originY += CHUNK_DIMENSION) {
            for (int originZ = originMinZ; originZ <= originMaxZ; originZ += CHUNK_DIMENSION) {
                for (int originX = originMinX; originX <= originMaxX; originX += CHUNK_DIMENSION) {
                    PENDING_RECAPTURE_SECTIONS.add(new DirtyChunkSection(originX, originY, originZ));
                }
            }
        }
    }

    private static void flushPendingChunkRecaptures() {
        if (attachedWorld == null || !MinecraftRenderHooks.isInitialized() || PENDING_RECAPTURE_SECTIONS.isEmpty()) {
            return;
        }

        int remainingSections = MAX_RECAPTURE_SECTIONS_PER_PRESENT;
        Iterator<DirtyChunkSection> iterator = PENDING_RECAPTURE_SECTIONS.iterator();
        while (iterator.hasNext() && remainingSections > 0) {
            if (MinecraftRenderHooks.isChunkBuildCaptureActive()) {
                return;
            }

            DirtyChunkSection section = iterator.next();
            if (!recaptureChunkSection(attachedWorld, section.originX, section.originY, section.originZ)) {
                return;
            }

            iterator.remove();
            remainingSections -= 1;
        }
    }

    private static final class RemixWorldListener implements pm {
        @Override
        public void a(int x, int y, int z) {
            queueRecaptureRegion(x, y, z, x, y, z);
        }

        @Override
        public void b(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
            queueRecaptureRegion(minX, minY, minZ, maxX, maxY, maxZ);
        }

        @Override
        public void a(String name, double x, double y, double z, float volume, float pitch) {
        }

        @Override
        public void a(String name, double x, double y, double z, double velocityX, double velocityY, double velocityZ) {
        }

        @Override
        public void a(sn entity) {
        }

        @Override
        public void b(sn entity) {
        }

        @Override
        public void e() {
        }

        @Override
        public void a(String name, int x, int y, int z) {
        }

        @Override
        public void a(int x, int y, int z, ow tileEntity) {
        }

        @Override
        public void a(gs player, int x, int y, int z, int direction, int itemId) {
        }
    }

    private static final class DirtyChunkSection {
        private final int originX;
        private final int originY;
        private final int originZ;

        private DirtyChunkSection(int originX, int originY, int originZ) {
            this.originX = originX;
            this.originY = originY;
            this.originZ = originZ;
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (!(object instanceof DirtyChunkSection)) {
                return false;
            }

            DirtyChunkSection other = (DirtyChunkSection) object;
            return originX == other.originX && originY == other.originY && originZ == other.originZ;
        }

        @Override
        public int hashCode() {
            int result = originX;
            result = 31 * result + originY;
            result = 31 * result + originZ;
            return result;
        }
    }

    private static String normalizeDynamicTexturePath(String primaryTexture, String fallbackTexture) {
        String normalizedPrimary = stripTexturePrefix(primaryTexture);
        if (!normalizedPrimary.isEmpty() && normalizedPrimary.charAt(0) == '/') {
            return normalizedPrimary;
        }

        String normalizedFallback = stripTexturePrefix(fallbackTexture);
        if (!normalizedFallback.isEmpty()) {
            return normalizedFallback;
        }

        return "";
    }

    private static String stripTexturePrefix(String texturePath) {
        if (texturePath == null || texturePath.isEmpty()) {
            return "";
        }
        String normalized = texturePath;
        while (normalized.startsWith("%clamp%") || normalized.startsWith("%blur%")) {
            if (normalized.startsWith("%clamp%")) {
                normalized = normalized.substring(7);
            } else if (normalized.startsWith("%blur%")) {
                normalized = normalized.substring(6);
            }
        }
        return normalized;
    }

    private static String activeCaptureTexture() {
        if (dynamicEntityActive && !activeDynamicEntityTexture.isEmpty()) {
            return activeDynamicEntityTexture;
        }
        if (firstPersonActive && !activeFirstPersonTexture.isEmpty()) {
            return activeFirstPersonTexture;
        }
        return "";
    }

    private static float[] buildInverseViewMatrix() {
        float[] matrix = new float[16];
        matrix[0] = cameraRightX;
        matrix[1] = cameraRightY;
        matrix[2] = cameraRightZ;
        matrix[3] = 0.0f;
        matrix[4] = cameraUpX;
        matrix[5] = cameraUpY;
        matrix[6] = cameraUpZ;
        matrix[7] = 0.0f;
        matrix[8] = -cameraForwardX;
        matrix[9] = -cameraForwardY;
        matrix[10] = -cameraForwardZ;
        matrix[11] = 0.0f;
        matrix[12] = cameraPositionX;
        matrix[13] = cameraPositionY;
        matrix[14] = cameraPositionZ;
        matrix[15] = 1.0f;
        return matrix;
    }

    private static float[] multiplyColumnMajor(float[] left, float[] right) {
        float[] result = new float[16];
        for (int column = 0; column < 4; column++) {
            for (int row = 0; row < 4; row++) {
                result[column * 4 + row] =
                        left[0 * 4 + row] * right[column * 4 + 0]
                                + left[1 * 4 + row] * right[column * 4 + 1]
                                + left[2 * 4 + row] * right[column * 4 + 2]
                                + left[3 * 4 + row] * right[column * 4 + 3];
            }
        }
        return result;
    }

    private static int allocateDynamicBoneIndex() {
        if (nextDynamicBoneIndex >= MAX_DYNAMIC_BONES) {
            if (!loggedDynamicEntityBoneOverflow) {
                loggedDynamicEntityBoneOverflow = true;
                System.err.println("[mcrtx] dynamic capture exceeded Remix bone limit; skipping excess dynamic geometry");
            }
            return -1;
        }

        int boneIndex = nextDynamicBoneIndex;
        nextDynamicBoneIndex += 1;
        return boneIndex;
    }

    private static void submitDynamicBoneTransform(int boneIndex, float[] columnMajorMatrix) {
        MinecraftRenderHooks.setDynamicEntityBoneTransform(
                boneIndex,
                columnMajorMatrix[0], columnMajorMatrix[4], columnMajorMatrix[8], columnMajorMatrix[12],
                columnMajorMatrix[1], columnMajorMatrix[5], columnMajorMatrix[9], columnMajorMatrix[13],
                columnMajorMatrix[2], columnMajorMatrix[6], columnMajorMatrix[10], columnMajorMatrix[14]);
    }

    private static int unpackTessellatorColor(int[] rawVertexData, int colorIndex) {
        int packedColor = rawVertexData[colorIndex];
        int red = packedColor & 0xFF;
        int green = (packedColor >> 8) & 0xFF;
        int blue = (packedColor >> 16) & 0xFF;
        int alpha = (packedColor >> 24) & 0xFF;
        return (alpha << 24) | (red << 16) | (green << 8) | blue;
    }

    private static int sanitizePackedColor(int packedColorRgba) {
        float alpha = ((packedColorRgba >> 24) & 0xFF) / 255.0f;
        float red = ((packedColorRgba >> 16) & 0xFF) / 255.0f;
        float green = ((packedColorRgba >> 8) & 0xFF) / 255.0f;
        float blue = (packedColorRgba & 0xFF) / 255.0f;
        float[] sanitizedColor = sanitizeDynamicEntityColor(red, green, blue, alpha);
        return packColor(sanitizedColor[0], sanitizedColor[1], sanitizedColor[2], sanitizedColor[3]);
    }

    private static int packColor(float red, float green, float blue, float alpha) {
        int alphaByte = clampColor(alpha);
        int redByte = clampColor(red);
        int greenByte = clampColor(green);
        int blueByte = clampColor(blue);
        return (alphaByte << 24) | (redByte << 16) | (greenByte << 8) | blueByte;
    }

    private static float[] sanitizeDynamicEntityColor(float red, float green, float blue, float alpha) {
        float maxChannelDelta = Math.max(Math.abs(red - green), Math.max(Math.abs(red - blue), Math.abs(green - blue)));
        if (alpha >= 0.999f && maxChannelDelta <= 0.01f) {
            return new float[]{1.0f, 1.0f, 1.0f, alpha};
        }
        return new float[]{red, green, blue, alpha};
    }

    private static int clampColor(float value) {
        if (value <= 0.0f) {
            return 0;
        }
        if (value >= 1.0f) {
            return 255;
        }
        return Math.round(value * 255.0f);
    }
}