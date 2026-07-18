final class RemixEntityFireCapture {
    private static final int FIRE_ALTERNATE_TERRAIN_TILE_INDEX = 47;
    private static final int FIRE_ANIMATION_FRAME_COUNT = 16;
    private static final long FIRE_ANIMATION_FRAME_INTERVAL_MILLISECONDS = 50L;
    private static final int TERRAIN_ATLAS_TILE_COLUMNS = 16;
    private static final float TERRAIN_ATLAS_TILE_SPAN = 1.0f / TERRAIN_ATLAS_TILE_COLUMNS;
    private static final float ATLAS_ROW_COUNT = 2.0f;
    private static final String TEXTURE_ALIAS_PREFIX = "/mcrtx_alias/entity_fire_overlay/";
    private static final String TEXTURE_PATH = "/mcrtx_alias/entity_fire_overlay/terrain.png";

    private static boolean active;

    private RemixEntityFireCapture() {
    }

    static void onRenderStart(sn entity) {
        if (!RemixLivingEntityCapture.canCapture()
                || entity == null
                || !RemixLivingEntityCapture.isTrackedLivingEntity(entity)) {
            return;
        }
        RemixDynamicEntitySession.ensureFrame();
        active = true;
        RemixDynamicEntitySession.beginEntity(entity.aD, 0, 0, 0.0f, TEXTURE_PATH);
    }

    static void onRenderEnd() {
        if (!active) {
            return;
        }
        active = false;
        RemixLivingEntityCapture.onRenderEnd();
    }

    static boolean isActive() {
        return active;
    }

    static void resetActiveCapture() {
        active = false;
    }

    static String textureAlias(String texturePath) {
        String normalized = RemixDynamicEntitySession.stripTexturePrefix(texturePath);
        if (normalized.isEmpty()) {
            return "";
        }
        if (normalized.startsWith(TEXTURE_ALIAS_PREFIX)) {
            return normalized;
        }
        if (normalized.charAt(0) == '/') {
            normalized = normalized.substring(1);
        }
        return TEXTURE_ALIAS_PREFIX + normalized;
    }

    static float[] remapUv(float u, float v) {
        int tileX = clampInt((int) Math.floor(u / TERRAIN_ATLAS_TILE_SPAN), 0, TERRAIN_ATLAS_TILE_COLUMNS - 1);
        int tileY = clampInt((int) Math.floor(v / TERRAIN_ATLAS_TILE_SPAN), 0, TERRAIN_ATLAS_TILE_COLUMNS - 1);
        int terrainTileIndex = tileX + tileY * TERRAIN_ATLAS_TILE_COLUMNS;
        int fireRow = terrainTileIndex == FIRE_ALTERNATE_TERRAIN_TILE_INDEX ? 1 : 0;
        int fireFrame = (int) ((System.currentTimeMillis() / FIRE_ANIMATION_FRAME_INTERVAL_MILLISECONDS)
                % FIRE_ANIMATION_FRAME_COUNT);
        float tileMinU = tileX * TERRAIN_ATLAS_TILE_SPAN;
        float tileMinV = tileY * TERRAIN_ATLAS_TILE_SPAN;
        float normalizedU = clamp01((u - tileMinU) / TERRAIN_ATLAS_TILE_SPAN);
        float normalizedV = clamp01((v - tileMinV) / TERRAIN_ATLAS_TILE_SPAN);
        return new float[] {
                (fireFrame + normalizedU) / FIRE_ANIMATION_FRAME_COUNT,
                (fireRow + normalizedV) / ATLAS_ROW_COUNT
        };
    }

    private static int clampInt(int value, int minValue, int maxValue) {
        return Math.max(minValue, Math.min(maxValue, value));
    }

    private static float clamp01(float value) {
        return Math.max(0.0f, Math.min(1.0f, value));
    }
}
