import mcrtx.bridge.HookProfiler;
import mcrtx.bridge.RemixDynamicEntityBridge;
import mcrtx.bridge.RemixLifecycleBridge;
import mcrtx.bridge.RemixParticleOverlayBridge;

final class RemixDynamicEntitySession {
    private static final int MAX_DYNAMIC_BONES = 256;
    private static final int TILE_ENTITY_ID_NAMESPACE = 0x40000000;

    private static boolean frameActive;
    private static boolean entityActive;
    private static int activeEntityId = -1;
    private static int activeHurtStage;
    private static int activeCreeperFuseStage;
    private static float activeCreeperFuseProgress;
    private static String activeEntityTexture = "";
    private static int nextBoneIndex;
    private static volatile boolean renderingEnabled = true;
    private static boolean loggedHookFailure;
    private static boolean loggedBoneOverflow;

    private RemixDynamicEntitySession() {
    }

    static void ensureFrame() {
        if (frameActive || !RemixLifecycleBridge.isInitialized()) {
            return;
        }

        long beginFrameStartNanos = System.nanoTime();
        RemixDynamicEntityBridge.beginDynamicEntityFrame();
        RemixParticleOverlayBridge.beginDestroyOverlayFrame();
        RemixParticleOverlayBridge.beginBlockOutlineFrame();
        frameActive = true;
        HookProfiler.record(HookProfiler.SIDE_HOOK, "hook.dynamicEntity.ensureFrame.beginFrame",
                System.nanoTime() - beginFrameStartNanos);
    }

    static void onFramePresented() {
        frameActive = false;
        RemixSignCapture.onFramePresented();
    }

    static boolean canCapture() {
        return renderingEnabled && RemixLifecycleBridge.isInitialized();
    }

    static boolean isRenderingEnabled() {
        return renderingEnabled;
    }

    static void setRenderingEnabled(boolean enabled) {
        renderingEnabled = enabled;
        if (enabled) {
            return;
        }

        clearEntityState();
        RemixItemEntityCapture.resetActiveCapture();
        RemixEntityFireCapture.resetActiveCapture();
        RemixSignCapture.resetActiveCapture();
        RemixFirstPersonCapture.resetActiveCapture();
    }

    static void beginEntity(int entityId, int hurtStage, int creeperFuseStage,
            float creeperFuseProgress, String texture) {
        entityActive = true;
        activeEntityId = entityId;
        preparePresentation(hurtStage, creeperFuseStage, creeperFuseProgress);
        activeEntityTexture = texture == null ? "" : texture;
        RemixDynamicEntityBridge.beginDynamicEntity(entityId, hurtStage, creeperFuseStage);
        if (!activeEntityTexture.isEmpty()) {
            RemixDynamicEntityBridge.setDynamicEntityTexture(activeEntityTexture);
        }
    }

    static void prepareAuxiliaryEntity(int hurtStage, int creeperFuseStage, float creeperFuseProgress) {
        preparePresentation(hurtStage, creeperFuseStage, creeperFuseProgress);
    }

    private static void preparePresentation(int hurtStage, int creeperFuseStage, float creeperFuseProgress) {
        activeHurtStage = hurtStage;
        activeCreeperFuseStage = creeperFuseStage;
        activeCreeperFuseProgress = creeperFuseProgress;
        nextBoneIndex = 0;
    }

    static void endEntity() {
        if (!entityActive) {
            return;
        }
        RemixDynamicEntityBridge.endDynamicEntity();
        clearEntityState();
    }

    static void endAuxiliaryEntity() {
        RemixDynamicEntityBridge.endDynamicEntity();
        clearPresentation();
    }

    static void clearEntityState() {
        entityActive = false;
        activeEntityId = -1;
        activeEntityTexture = "";
        clearPresentation();
    }

    private static void clearPresentation() {
        activeHurtStage = 0;
        activeCreeperFuseStage = 0;
        activeCreeperFuseProgress = 0.0f;
        nextBoneIndex = 0;
    }

    static boolean isEntityActive() {
        return entityActive;
    }

    static int activeEntityId() {
        return activeEntityId;
    }

    static int activeHurtStage() {
        return activeHurtStage;
    }

    static int activeCreeperFuseStage() {
        return activeCreeperFuseStage;
    }

    static float activeCreeperFuseProgress() {
        return activeCreeperFuseProgress;
    }

    static String activeEntityTexture() {
        return activeEntityTexture;
    }

    static String activeCaptureTexture() {
        if (entityActive && !activeEntityTexture.isEmpty()) {
            return activeEntityTexture;
        }
        return RemixFirstPersonCapture.activeTexture();
    }

    static void setEntityTexture(String texture) {
        String normalized = texture == null ? "" : texture;
        if (normalized.isEmpty() || normalized.equals(activeEntityTexture)) {
            return;
        }
        activeEntityTexture = normalized;
        RemixDynamicEntityBridge.setDynamicEntityTexture(normalized);
    }

    static void bindEntityTexture(String primaryTexture, String fallbackTexture) {
        if (!entityActive) {
            return;
        }
        String resolvedTexture = normalizeTexturePath(primaryTexture, fallbackTexture);
        if (RemixEntityFireCapture.isActive()) {
            resolvedTexture = RemixEntityFireCapture.textureAlias(
                    resolvedTexture.isEmpty() ? RemixHeldItemCapture.TERRAIN_TEXTURE_PATH : resolvedTexture);
        } else if (RemixFirstPersonCapture.isShadowCaptureActive()) {
            resolvedTexture = RemixFirstPersonCapture.shadowTextureAlias(resolvedTexture);
        }
        setEntityTexture(resolvedTexture);
    }

    private static final java.util.Set<String> downloadedThisSession = java.util.Collections.newSetFromMap(new java.util.concurrent.ConcurrentHashMap<String, Boolean>());
    private static final java.util.Set<String> downloadingSkins = java.util.Collections.newSetFromMap(new java.util.concurrent.ConcurrentHashMap<String, Boolean>());
    private static final java.util.Set<String> existingSkinsCache = java.util.Collections.newSetFromMap(new java.util.concurrent.ConcurrentHashMap<String, Boolean>());

    static String normalizeTexturePath(String primaryTexture, String fallbackTexture) {
        String normalizedPrimary = stripTexturePrefix(primaryTexture);
        if (!normalizedPrimary.isEmpty() && normalizedPrimary.charAt(0) == '/') {
            return normalizedPrimary;
        }

        if (!normalizedPrimary.isEmpty() && (normalizedPrimary.startsWith("http://") || normalizedPrimary.startsWith("https://"))) {
            try {
                java.net.URL url = new java.net.URL(normalizedPrimary);
                String path = url.getPath();
                String fileName = path.substring(path.lastIndexOf('/') + 1);
                if (fileName.endsWith(".png")) {
                    String prefix = path.toLowerCase().contains("cloak") ? "cloak_" : "skin_";
                    String ddsFileName = prefix + fileName.substring(0, fileName.length() - 4) + ".dds";
                    
                    boolean fileExists = existingSkinsCache.contains(ddsFileName);
                    if (!fileExists) {
                        java.io.File skinsDir = new java.io.File("../libraries/mcrtx_assets/skins");
                        if (!skinsDir.exists()) {
                            skinsDir.mkdirs();
                        }
                        java.io.File ddsFile = new java.io.File(skinsDir, ddsFileName);
                        fileExists = ddsFile.exists() && ddsFile.length() > 0;
                        if (fileExists) {
                            existingSkinsCache.add(ddsFileName);
                        }
                    }

                    boolean shouldDownload = !downloadedThisSession.contains(normalizedPrimary);

                    if (shouldDownload && downloadingSkins.add(normalizedPrimary)) {
                        downloadedThisSession.add(normalizedPrimary);
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                System.out.println("[BetaRT] Attempting to download skin from: " + url.toString());
                                try {
                                    java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
                                    conn.setConnectTimeout(5000);
                                    conn.setReadTimeout(5000);
                                    conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                                    
                                    if (conn.getResponseCode() == 200) {
                                        java.io.InputStream in = conn.getInputStream();
                                        java.awt.image.BufferedImage image = javax.imageio.ImageIO.read(in);
                                        in.close();
                                        if (image != null) {
                                            java.io.File skinsDir = new java.io.File("../libraries/mcrtx_assets/skins");
                                            java.io.File ddsFile = new java.io.File(skinsDir, ddsFileName);
                                            java.io.File tempFile = new java.io.File(ddsFile.getAbsolutePath() + ".tmp");
                                            saveAsDDS(image, tempFile);
                                            tempFile.renameTo(ddsFile);
                                            ddsFile.setLastModified(System.currentTimeMillis());
                                            existingSkinsCache.add(ddsFileName);
                                            System.out.println("[BetaRT] Successfully downloaded and converted skin: " + ddsFileName);
                                        } else {
                                            System.out.println("[BetaRT] Failed to parse skin image data from: " + url.toString());
                                        }
                                    } else {
                                        System.out.println("[BetaRT] Failed to download skin from: " + url.toString() + " (HTTP " + conn.getResponseCode() + ")");
                                    }
                                } catch (Exception e) {
                                    System.out.println("[BetaRT] Exception downloading skin from: " + url.toString());
                                    e.printStackTrace();
                                } finally {
                                    downloadingSkins.remove(normalizedPrimary);
                                }
                            }
                        }, "BetaRT-Skin-Downloader").start();
                    }

                    if (fileExists) {
                        return "/skins/" + ddsFileName;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        String normalizedFallback = stripTexturePrefix(fallbackTexture);
        return normalizedFallback.isEmpty() ? "" : normalizedFallback;
    }

    static String stripTexturePrefix(String texturePath) {
        if (texturePath == null || texturePath.isEmpty()) {
            return "";
        }
        String normalized = texturePath;
        while (normalized.startsWith("%clamp%") || normalized.startsWith("%blur%")) {
            if (normalized.startsWith("%clamp%")) {
                normalized = normalized.substring(7);
            } else {
                normalized = normalized.substring(6);
            }
        }
        return normalized;
    }

    static int stableTileEntityId(int x, int y, int z, int salt) {
        int hash = salt;
        hash = 31 * hash + x;
        hash = 31 * hash + y;
        hash = 31 * hash + z;
        return TILE_ENTITY_ID_NAMESPACE | (hash & 0x3FFFFFFF);
    }

    static int allocateBoneIndex() {
        if (nextBoneIndex >= MAX_DYNAMIC_BONES) {
            if (!loggedBoneOverflow) {
                loggedBoneOverflow = true;
                System.err.println("[mcrtx] dynamic capture exceeded Remix bone limit; skipping excess dynamic geometry");
            }
            return -1;
        }

        int boneIndex = nextBoneIndex;
        nextBoneIndex += 1;
        return boneIndex;
    }

    static void submitBoneTransform(int boneIndex, RemixCameraState.PreciseTransform transform) {
        float[] matrix = transform.matrix;
        RemixDynamicEntityBridge.setDynamicEntityBoneTransform(
                boneIndex,
                matrix[0], matrix[4], matrix[8], transform.x,
                matrix[1], matrix[5], matrix[9], transform.y,
                matrix[2], matrix[6], matrix[10], transform.z);
    }

    static void handleFailure(RuntimeException exception) {
        RemixDynamicEntityBridge.endDynamicEntity();
        if (!loggedHookFailure) {
            loggedHookFailure = true;
            System.err.println("[mcrtx] dynamic entity capture disabled after hook failure");
            exception.printStackTrace();
        }
        clearEntityState();
        RemixItemEntityCapture.resetActiveCapture();
        RemixEntityFireCapture.resetActiveCapture();
        RemixSignCapture.resetActiveCapture();
        RemixFirstPersonCapture.resetActiveCapture();
    }

    private static boolean isOverlayPixel(int x, int y) {
        if (y < 16) {
            return x >= 32 && x < 64; // Hat overlay
        } else if (y >= 32 && y < 48) {
            return true; // Modern Body/Right Arm/Right Leg overlays
        } else if (y >= 48 && y < 64) {
            return (x >= 16 && x < 32) || (x >= 48 && x < 64); // Modern Left Leg/Left Arm overlays
        }
        return false;
    }

    private static void saveAsDDS(java.awt.image.BufferedImage image, java.io.File file) throws Exception {
        int width = image.getWidth();
        int height = image.getHeight();
        byte[] header = new byte[128];
        header[0] = 'D'; header[1] = 'D'; header[2] = 'S'; header[3] = ' ';
        header[4] = 124;
        header[8] = 0x0F; header[9] = 0x10; header[10] = 0x08; header[11] = 0x00;
        header[12] = (byte) (height & 0xFF); header[13] = (byte) ((height >> 8) & 0xFF);
        header[16] = (byte) (width & 0xFF); header[17] = (byte) ((width >> 8) & 0xFF);
        int pitch = width * 4;
        header[20] = (byte) (pitch & 0xFF); header[21] = (byte) ((pitch >> 8) & 0xFF);
        header[76] = 32;
        header[80] = 0x41;
        header[88] = 32;
        header[94] = (byte) 0xFF; // R
        header[97] = (byte) 0xFF; // G
        header[100] = (byte) 0xFF; // B
        header[107] = (byte) 0xFF; // A
        header[109] = 0x10;
        int[] pixels = image.getRGB(0, 0, width, height, null, 0, width);
        java.io.FileOutputStream fos = new java.io.FileOutputStream(file);
        try {
            fos.write(header);
            
            byte[] pixelData = new byte[width * height * 4];
            int offset = 0;
            for (int i = 0; i < pixels.length; i++) {
                int argb = pixels[i];
                int x = i % width;
                int y = i / width;
                
                if (!isOverlayPixel(x, y)) {
                    argb |= 0xFF000000;
                }
                
                pixelData[offset++] = (byte) (argb & 0xFF);         // B
                pixelData[offset++] = (byte) ((argb >> 8) & 0xFF);  // G
                pixelData[offset++] = (byte) ((argb >> 16) & 0xFF); // R
                pixelData[offset++] = (byte) ((argb >> 24) & 0xFF); // A
            }
            fos.write(pixelData);
        } finally {
            fos.close();
        }
    }
}
