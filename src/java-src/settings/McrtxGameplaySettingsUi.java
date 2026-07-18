import mcrtx.bridge.McrtxGameplaySettings;
import mcrtx.bridge.McrtxGameplaySettingsNative;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

final class McrtxGameplaySettingsUi implements McrtxSettingsCategoryUi {
    private static final int PLAYER_SHADOWS_BUTTON_ID = 1;
    private static final int HELD_TORCH_LIGHTS_BUTTON_ID = 2;
    private static final int GAMEPLAY_FOV_SLIDER_ID = 7;
    private static final int VIEW_MODEL_FOV_SLIDER_ID = 8;
    private static final int BLOCK_OUTLINE_BUTTON_ID = 10;
    private static final int BLOCK_OUTLINE_STYLE_BUTTON_ID = 11;
    private static final int BLOCK_OUTLINE_INTENSITY_SLIDER_ID = 12;

    public String getName() {
        return "Gameplay";
    }

    public void addControls(McrtxQuickSettingsScreen screen) {
        screen.addControl(new Slider(GAMEPLAY_FOV_SLIDER_ID, screen.getControlX(), screen.takeNextRowY(), screen.getControlWidth(), McrtxQuickSettingsScreen.CONTROL_HEIGHT, Slider.MODE_GAMEPLAY_FOV));
        screen.addControl(new Slider(VIEW_MODEL_FOV_SLIDER_ID, screen.getControlX(), screen.takeNextRowY(), screen.getControlWidth(), McrtxQuickSettingsScreen.CONTROL_HEIGHT, Slider.MODE_VIEW_MODEL_FOV));
        screen.addControl(button(screen, PLAYER_SHADOWS_BUTTON_ID, getPlayerShadowsLabel()));
        screen.addControl(button(screen, HELD_TORCH_LIGHTS_BUTTON_ID, getHeldTorchLightsLabel()));
        screen.addControl(button(screen, BLOCK_OUTLINE_BUTTON_ID, getBlockOutlineLabel()));
        if (McrtxGameplaySettings.isBlockOutlineEnabled()) {
            screen.addControl(button(screen, BLOCK_OUTLINE_STYLE_BUTTON_ID, getBlockOutlineStyleLabel()));
        }
        if (shouldShowBlockOutlineIntensitySlider()) {
            screen.addControl(new Slider(BLOCK_OUTLINE_INTENSITY_SLIDER_ID, screen.getControlX(), screen.takeNextRowY(), screen.getControlWidth(), McrtxQuickSettingsScreen.CONTROL_HEIGHT, Slider.MODE_BLOCK_OUTLINE_INTENSITY));
        }
    }

    public int handleButton(int buttonId) {
        if (buttonId == PLAYER_SHADOWS_BUTTON_ID) {
            setPlayerShadowsEnabled(!McrtxGameplaySettings.isPlayerShadowsEnabled());
            return UPDATE_REFRESH;
        }
        if (buttonId == HELD_TORCH_LIGHTS_BUTTON_ID) {
            setHeldTorchLightsEnabled(!McrtxGameplaySettings.isHeldTorchLightsEnabled());
            return UPDATE_REFRESH;
        }
        if (buttonId == BLOCK_OUTLINE_BUTTON_ID) {
            setBlockOutlineEnabled(!McrtxGameplaySettings.isBlockOutlineEnabled());
            return UPDATE_REBUILD;
        }
        if (buttonId == BLOCK_OUTLINE_STYLE_BUTTON_ID) {
            cycleBlockOutlineStyle();
            return UPDATE_REBUILD;
        }
        return UPDATE_NONE;
    }

    public void refreshButtons(McrtxQuickSettingsScreen screen) {
        setLabel(screen, PLAYER_SHADOWS_BUTTON_ID, getPlayerShadowsLabel());
        setLabel(screen, HELD_TORCH_LIGHTS_BUTTON_ID, getHeldTorchLightsLabel());
        setLabel(screen, BLOCK_OUTLINE_BUTTON_ID, getBlockOutlineLabel());
        setLabel(screen, BLOCK_OUTLINE_STYLE_BUTTON_ID, getBlockOutlineStyleLabel());
    }

    public void applySavedSettings() {
        boolean playerShadows = McrtxGameplaySettings.isPlayerShadowsEnabled();
        boolean heldLights = McrtxGameplaySettings.isHeldTorchLightsEnabled();
        RemixDynamicEntityCapture.setPlayerShadowsEnabled(playerShadows);
        RemixDynamicEntityCapture.setHeldTorchLightsEnabled(heldLights);
        McrtxGameplaySettingsNative.setPlayerShadowsEnabled(playerShadows);
        McrtxGameplaySettingsNative.setHeldTorchLightsEnabled(heldLights);
        McrtxGameplaySettingsNative.setBlockOutlineEnabled(McrtxGameplaySettings.isBlockOutlineEnabled());
        McrtxGameplaySettingsNative.setBlockOutlineStyle(McrtxGameplaySettings.getBlockOutlineStyle());
        McrtxGameplaySettingsNative.setBlockOutlineEmissiveIntensity(McrtxGameplaySettings.getBlockOutlineEmissiveIntensity());
        McrtxGameplaySettingsNative.setViewModelFovDegrees(McrtxGameplaySettings.getViewModelFovDegrees());
    }

    private static ke button(McrtxQuickSettingsScreen screen, int id, String label) {
        return new ke(id, screen.getControlX(), screen.takeNextRowY(), screen.getControlWidth(), McrtxQuickSettingsScreen.CONTROL_HEIGHT, label);
    }

    private static void setLabel(McrtxQuickSettingsScreen screen, int id, String label) {
        ke button = screen.findButton(id);
        if (button != null) button.e = label;
    }

    private static String getPlayerShadowsLabel() { return "First-Person Player Shadows: " + toggle(McrtxGameplaySettings.isPlayerShadowsEnabled()); }
    private static String getHeldTorchLightsLabel() { return "Held Torch Lights: " + toggle(McrtxGameplaySettings.isHeldTorchLightsEnabled()); }
    private static String getBlockOutlineLabel() { return "Block Outline: " + toggle(McrtxGameplaySettings.isBlockOutlineEnabled()); }
    private static String getBlockOutlineStyleLabel() { return "Outline Style: " + describeBlockOutlineStyle(McrtxGameplaySettings.getBlockOutlineStyle()); }
    private static String toggle(boolean enabled) { return enabled ? "ON" : "OFF"; }

    private static boolean shouldShowBlockOutlineIntensitySlider() {
        int style = McrtxGameplaySettings.getBlockOutlineStyle();
        return McrtxGameplaySettings.isBlockOutlineEnabled()
                && (style == McrtxGameplaySettings.BLOCK_OUTLINE_STYLE_GLOW || style == McrtxGameplaySettings.BLOCK_OUTLINE_STYLE_RGB);
    }

    private static void setPlayerShadowsEnabled(boolean enabled) {
        McrtxGameplaySettings.setPlayerShadowsEnabled(enabled);
        RemixDynamicEntityCapture.setPlayerShadowsEnabled(enabled);
        McrtxGameplaySettingsNative.setPlayerShadowsEnabled(enabled);
    }

    private static void setHeldTorchLightsEnabled(boolean enabled) {
        McrtxGameplaySettings.setHeldTorchLightsEnabled(enabled);
        RemixDynamicEntityCapture.setHeldTorchLightsEnabled(enabled);
        McrtxGameplaySettingsNative.setHeldTorchLightsEnabled(enabled);
    }

    private static void setBlockOutlineEnabled(boolean enabled) {
        McrtxGameplaySettings.setBlockOutlineEnabled(enabled);
        McrtxGameplaySettingsNative.setBlockOutlineEnabled(enabled);
    }

    private static void setViewModelFovDegrees(int value) {
        McrtxGameplaySettings.setViewModelFovDegrees(value);
        McrtxGameplaySettingsNative.setViewModelFovDegrees(value);
    }

    private static void setBlockOutlineIntensity(int value) {
        McrtxGameplaySettings.setBlockOutlineEmissiveIntensityHundredths(value);
        McrtxGameplaySettingsNative.setBlockOutlineEmissiveIntensity(McrtxGameplaySettings.getBlockOutlineEmissiveIntensity());
    }

    private static void cycleBlockOutlineStyle() {
        int style = McrtxGameplaySettings.getBlockOutlineStyle();
        if (style == McrtxGameplaySettings.BLOCK_OUTLINE_STYLE_SUBTLE) style = McrtxGameplaySettings.BLOCK_OUTLINE_STYLE_THIN;
        else if (style == McrtxGameplaySettings.BLOCK_OUTLINE_STYLE_THIN) style = McrtxGameplaySettings.BLOCK_OUTLINE_STYLE_GLOW;
        else if (style == McrtxGameplaySettings.BLOCK_OUTLINE_STYLE_GLOW) style = McrtxGameplaySettings.BLOCK_OUTLINE_STYLE_RGB;
        else if (style == McrtxGameplaySettings.BLOCK_OUTLINE_STYLE_RGB) style = McrtxGameplaySettings.BLOCK_OUTLINE_STYLE_BOLD;
        else if (style == McrtxGameplaySettings.BLOCK_OUTLINE_STYLE_BOLD) style = McrtxGameplaySettings.BLOCK_OUTLINE_STYLE_SOLID;
        else style = McrtxGameplaySettings.BLOCK_OUTLINE_STYLE_SUBTLE;
        McrtxGameplaySettings.setBlockOutlineStyle(style);
        McrtxGameplaySettingsNative.setBlockOutlineStyle(style);
    }

    private static String describeBlockOutlineStyle(int style) {
        if (style == McrtxGameplaySettings.BLOCK_OUTLINE_STYLE_SUBTLE) return "Subtle";
        if (style == McrtxGameplaySettings.BLOCK_OUTLINE_STYLE_THIN) return "Thin";
        if (style == McrtxGameplaySettings.BLOCK_OUTLINE_STYLE_GLOW) return "Glow";
        if (style == McrtxGameplaySettings.BLOCK_OUTLINE_STYLE_RGB) return "RGB";
        if (style == McrtxGameplaySettings.BLOCK_OUTLINE_STYLE_SOLID) return "Solid Fill";
        return "Bold";
    }

    private static String formatHundredths(int value) {
        int absolute = Math.abs(value);
        String result = Integer.toString(absolute / 100) + "." + (absolute % 100 < 10 ? "0" : "") + Integer.toString(absolute % 100);
        return value < 0 ? "-" + result : result;
    }

    private static final class Slider extends ke {
        static final int MODE_GAMEPLAY_FOV = 0;
        static final int MODE_VIEW_MODEL_FOV = 1;
        static final int MODE_BLOCK_OUTLINE_INTENSITY = 2;

        private final int mode;
        private final int minimum;
        private final int maximum;
        private boolean dragging;
        private float position;

        Slider(int id, int x, int y, int width, int height, int mode) {
            super(id, x, y, width, height, "");
            this.mode = mode;
            if (mode == MODE_BLOCK_OUTLINE_INTENSITY) {
                minimum = McrtxGameplaySettings.MIN_BLOCK_OUTLINE_EMISSIVE_INTENSITY_HUNDREDTHS;
                maximum = McrtxGameplaySettings.MAX_BLOCK_OUTLINE_EMISSIVE_INTENSITY_HUNDREDTHS;
            } else if (mode == MODE_VIEW_MODEL_FOV) {
                minimum = McrtxGameplaySettings.MIN_VIEW_MODEL_FOV_DEGREES;
                maximum = McrtxGameplaySettings.MAX_VIEW_MODEL_FOV_DEGREES;
            } else {
                minimum = McrtxGameplaySettings.MIN_GAMEPLAY_FOV_DEGREES;
                maximum = McrtxGameplaySettings.MAX_GAMEPLAY_FOV_DEGREES;
            }
            sync();
        }

        protected int a(boolean hovered) { return 0; }
        protected void b(Minecraft minecraft, int mouseX, int mouseY) {
            if (!this.h) return;
            if (dragging) update(mouseX); else sync();
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            int thumbX = this.c + (int) (position * (float) (this.a - 8));
            this.b(thumbX, this.d, 0, 66, 4, 20);
            this.b(thumbX + 4, this.d, 196, 66, 4, 20);
        }
        public boolean c(Minecraft minecraft, int mouseX, int mouseY) { if (!super.c(minecraft, mouseX, mouseY)) return false; dragging = true; update(mouseX); return true; }
        public void a(int mouseX, int mouseY) { dragging = false; sync(); }

        private void sync() {
            int value = mode == MODE_VIEW_MODEL_FOV ? McrtxGameplaySettings.getViewModelFovDegrees()
                    : mode == MODE_BLOCK_OUTLINE_INTENSITY ? McrtxGameplaySettings.getBlockOutlineEmissiveIntensityHundredths()
                    : McrtxGameplaySettings.getGameplayFovDegrees();
            position = (float) (value - minimum) / (float) (maximum - minimum);
            label(value);
        }
        private void update(int mouseX) {
            float next = (float) (mouseX - (this.c + 4)) / (float) (this.a - 8);
            if (next < 0.0f) next = 0.0f;
            if (next > 1.0f) next = 1.0f;
            int value = minimum + Math.round(next * (float) (maximum - minimum));
            position = (float) (value - minimum) / (float) (maximum - minimum);
            if (mode == MODE_VIEW_MODEL_FOV) setViewModelFovDegrees(value);
            else if (mode == MODE_BLOCK_OUTLINE_INTENSITY) setBlockOutlineIntensity(value);
            else McrtxGameplaySettings.setGameplayFovDegrees(value);
            label(value);
        }
        private void label(int value) {
            if (mode == MODE_VIEW_MODEL_FOV) this.e = "Hand FOV: " + value;
            else if (mode == MODE_BLOCK_OUTLINE_INTENSITY) this.e = "Outline Intensity: " + formatHundredths(value);
            else this.e = "FOV: " + value;
        }
    }
}
