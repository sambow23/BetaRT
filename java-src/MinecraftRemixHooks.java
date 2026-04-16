import mcrtx.bridge.MinecraftRenderHooks;
import mcrtx.bridge.UiOverlayCapture;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;

/**
 * Thin dispatcher that receives every bytecode-patched callback from the Beta
 * client and routes it to the corresponding capture subsystem. The set of
 * public static methods on this class is the ABI the patcher targets and must
 * not change without updating {@code ClientPatchTool} in lockstep.
 */
public final class MinecraftRemixHooks {
    private static final int DEFAULT_REMIX_UI_STATE = MinecraftRenderHooks.REMIX_UI_STATE_ADVANCED;

    private static boolean loggedDisplayCreate;
    private static boolean loggedDisplayReset;
    private static boolean loggedPresent;
    private static boolean remixUiOpen;
    private static boolean remixUiHotkeyHeld;
    private static int preferredRemixUiState = DEFAULT_REMIX_UI_STATE;

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
        resetRemixUiTracking();
        UiOverlayCapture.reset();
        MinecraftRenderHooks.initializeForCurrentDisplay(width, height);
    }

    public static void onShutdown() {
        System.out.println("[mcrtx] onShutdown");
        resetRemixUiTracking();
        UiOverlayCapture.reset();
        MinecraftRenderHooks.shutdown();
    }

    public static void onDisplayReset(int width, int height) {
        if (!loggedDisplayReset) {
            loggedDisplayReset = true;
            System.out.println("[mcrtx] onDisplayReset width=" + width + " height=" + height);
        }
        resetRemixUiTracking();
        UiOverlayCapture.reset();
        MinecraftRenderHooks.reinitializeForCurrentDisplay(width, height);
    }

    public static void onResize(int width, int height) {
        MinecraftRenderHooks.resize(width, height);
    }

    public static void onCamera(ls entity, float partialTicks, int width, int height, float farPlane) {
        RemixCameraState.onCamera(entity, partialTicks, width, height, farPlane);
    }

    public static void onPresent() {
        RemixChunkCapture.flushPendingChunkRecaptures();
        if (!loggedPresent) {
            loggedPresent = true;
            System.out.println("[mcrtx] onPresent");
        }
        MinecraftRenderHooks.present();
        RemixDynamicEntityCapture.onFramePresented();
    }

    public static void onUiRenderBegin(int width, int height) {
        UiOverlayCapture.begin(width, height);
    }

    public static void onUiRenderEnd() {
        UiOverlayCapture.end();
    }

    public static void onRemixUiTick(net.minecraft.client.Minecraft minecraft) {
        syncRemixUiInput(minecraft, true);
    }

    public static boolean isWindowInteractionActive() {
        return Display.isActive() || remixUiOpen;
    }

    public static void onWorldChanged(fd world) {
        RemixChunkCapture.onWorldChanged(world);
    }

    public static void onCloudRender(net.minecraft.client.Minecraft minecraft, fd world, int cloudTick, float partialTicks, boolean fancy) {
        RemixCloudCapture.onCloudRender(minecraft, world, cloudTick, partialTicks, fancy);
    }

    public static void onLivingEntityFrameBegin() {
        RemixDynamicEntityCapture.onLivingEntityFrameBegin();
    }

    public static void onDestroyOverlayRender(int blockX, int blockY, int blockZ, float destroyProgress) {
        RemixDestroyOverlayCapture.onDestroyOverlayRender(blockX, blockY, blockZ, destroyProgress);
    }

    public static void onParticleRender(xw particle, float partialTicks, float f3, float f4, float f5, float f6, float f7) {
        RemixParticleCapture.onParticleRender(particle, partialTicks, f3, f4, f5, f6, f7);
    }

    public static void onLivingEntityRenderStart(sn entity) {
        RemixDynamicEntityCapture.onLivingEntityRenderStart(entity);
    }

    public static void onLivingEntityRenderEnd() {
        RemixDynamicEntityCapture.onLivingEntityRenderEnd();
    }

    public static void onSignRenderStart(yk sign) {
        RemixDynamicEntityCapture.onSignRenderStart(sign);
    }

    public static void onSignRenderEnd() {
        RemixDynamicEntityCapture.onSignRenderEnd();
    }

    public static void onSignTextRender(String text, int x, int y, int colorRgba, boolean shadow, int[] characterWidths) {
        RemixDynamicEntityCapture.onSignTextRender(text, x, y, colorRgba, shadow, characterWidths);
    }

    public static void onFirstPersonRenderStart() {
        RemixDynamicEntityCapture.onFirstPersonRenderStart();
    }

    public static void onFirstPersonRenderEnd() {
        RemixDynamicEntityCapture.onFirstPersonRenderEnd();
    }

    public static void onFirstPersonItemRender(iz itemStack) {
        RemixDynamicEntityCapture.onFirstPersonItemRender(itemStack);
    }

    public static void onEntityTextureBind(String primaryTexture, String fallbackTexture) {
        RemixDynamicEntityCapture.onEntityTextureBind(primaryTexture, fallbackTexture);
    }

    public static void onModelPartRender(tz[] polygons, float scale) {
        RemixDynamicEntityCapture.onModelPartRender(polygons, scale);
    }

    public static void onFirstPersonTessellatorDraw(
            int[] rawVertexData,
            int vertexCount,
            int drawMode,
            boolean hasTexture,
            boolean hasColor) {
        RemixDynamicEntityCapture.onFirstPersonTessellatorDraw(rawVertexData, vertexCount, drawMode, hasTexture, hasColor);
    }

    public static boolean onChunkBuildBegin(
            int originX,
            int originY,
            int originZ,
            int sizeX,
            int sizeY,
            int sizeZ,
            int renderPass) {
        return RemixChunkCapture.onChunkBuildBegin(originX, originY, originZ, sizeX, sizeY, sizeZ, renderPass);
    }

    public static void onChunkBlock(
            ew blockAccess,
            int blockX,
            int blockY,
            int blockZ,
            int blockId,
            int blockMetadata,
            int renderType) {
        RemixChunkCapture.onChunkBlock(blockAccess, blockX, blockY, blockZ, blockId, blockMetadata, renderType);
    }

    public static void onChunkBuildEnd(boolean emittedGeometry) {
        RemixChunkCapture.onChunkBuildEnd(emittedGeometry);
    }

    private static void resetRemixUiTracking() {
        remixUiOpen = false;
        remixUiHotkeyHeld = false;
        preferredRemixUiState = DEFAULT_REMIX_UI_STATE;
    }

    private static boolean syncRemixUiInput(net.minecraft.client.Minecraft minecraft, boolean allowHotkeyToggle) {
        int uiState = MinecraftRenderHooks.getUiState();
        boolean altDown = Keyboard.isKeyDown(Keyboard.KEY_LMENU) || Keyboard.isKeyDown(Keyboard.KEY_RMENU);
        boolean xDown = Keyboard.isKeyDown(Keyboard.KEY_X);
        boolean hotkeyHeld = altDown && xDown;

        if (allowHotkeyToggle && hotkeyHeld && !remixUiHotkeyHeld) {
            int targetState = uiState == MinecraftRenderHooks.REMIX_UI_STATE_NONE
                    ? preferredRemixUiState
                    : MinecraftRenderHooks.REMIX_UI_STATE_NONE;
            if (MinecraftRenderHooks.setUiState(targetState)) {
                uiState = targetState;
                System.out.println("[mcrtx] Remix UI hotkey toggled state=" + uiState);
            } else {
                System.out.println("[mcrtx] Remix UI hotkey failed: " + MinecraftRenderHooks.lastError());
            }
        }

        remixUiHotkeyHeld = hotkeyHeld;
        if (uiState != MinecraftRenderHooks.REMIX_UI_STATE_NONE) {
            preferredRemixUiState = uiState;
        }

        boolean uiOpen = uiState != MinecraftRenderHooks.REMIX_UI_STATE_NONE;
        if (minecraft != null) {
            if (uiOpen) {
                minecraft.h();
            } else if (remixUiOpen && minecraft.r == null) {
                minecraft.g();
            }
        }

        remixUiOpen = uiOpen;
        return uiOpen;
    }
}
