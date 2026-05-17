public final class McrtxOptionsScreen extends da {
    private static final int COLUMN_BUTTON_WIDTH = 150;
    private static final int COLUMN_BUTTON_HEIGHT = 20;
    private static final int COLUMN_BUTTON_GAP = 4;
    private static final int PLAYER_SHADOWS_BUTTON_ID = 1;
    private static final int HELD_TORCH_LIGHTS_BUTTON_ID = 2;
    private static final int UPSCALER_BUTTON_ID = 3;
    private static final int UPSCALER_PRESET_BUTTON_ID = 4;
    private static final int RAY_RECONSTRUCTION_BUTTON_ID = 5;
    private static final int RT_QUALITY_BUTTON_ID = 6;
    private static final int DONE_BUTTON_ID = 200;

    private final da parent;

    public McrtxOptionsScreen(da parent) {
        this.parent = parent;
    }

    public void b() {
        this.e.clear();
        int leftColumnX = getLeftColumnX();
        int rightColumnX = getRightColumnX();
        int firstRowY = getFirstRowY();
        int secondRowY = getSecondRowY();
        this.e.add(new ke(PLAYER_SHADOWS_BUTTON_ID, leftColumnX, firstRowY, COLUMN_BUTTON_WIDTH, COLUMN_BUTTON_HEIGHT, MinecraftRemixHooks.getPlayerShadowsButtonLabel()));
        this.e.add(new ke(UPSCALER_BUTTON_ID, rightColumnX, firstRowY, COLUMN_BUTTON_WIDTH, COLUMN_BUTTON_HEIGHT, MinecraftRemixHooks.getUpscalerButtonLabel()));
        this.e.add(new ke(HELD_TORCH_LIGHTS_BUTTON_ID, leftColumnX, secondRowY, COLUMN_BUTTON_WIDTH, COLUMN_BUTTON_HEIGHT, MinecraftRemixHooks.getHeldTorchLightsButtonLabel()));
        this.e.add(new ke(UPSCALER_PRESET_BUTTON_ID, rightColumnX, secondRowY, COLUMN_BUTTON_WIDTH, COLUMN_BUTTON_HEIGHT, MinecraftRemixHooks.getUpscalerPresetButtonLabel()));
        this.e.add(new ke(RT_QUALITY_BUTTON_ID, leftColumnX, getThirdRowY(), COLUMN_BUTTON_WIDTH, COLUMN_BUTTON_HEIGHT, MinecraftRemixHooks.getRtQualityButtonLabel()));
        this.e.add(new ke(RAY_RECONSTRUCTION_BUTTON_ID, rightColumnX, getThirdRowY(), COLUMN_BUTTON_WIDTH, COLUMN_BUTTON_HEIGHT, MinecraftRemixHooks.getRayReconstructionButtonLabel()));
        this.e.add(new ke(DONE_BUTTON_ID, this.c / 2 - 100, getThirdRowY() + 48, "Done"));
        refreshButtons();
    }

    protected void a(ke button) {
        if (button == null || !button.g) {
            return;
        }

        if (button.f == PLAYER_SHADOWS_BUTTON_ID) {
            MinecraftRemixHooks.setPlayerShadowsEnabled(!MinecraftRemixHooks.isPlayerShadowsEnabled());
            button.e = MinecraftRemixHooks.getPlayerShadowsButtonLabel();
            return;
        }

        if (button.f == HELD_TORCH_LIGHTS_BUTTON_ID) {
            MinecraftRemixHooks.setHeldTorchLightsEnabled(!MinecraftRemixHooks.isHeldTorchLightsEnabled());
            button.e = MinecraftRemixHooks.getHeldTorchLightsButtonLabel();
            return;
        }

        if (button.f == RT_QUALITY_BUTTON_ID) {
            MinecraftRemixHooks.cycleRtQuality();
            refreshButtons();
            return;
        }

        if (button.f == UPSCALER_BUTTON_ID) {
            MinecraftRemixHooks.cycleUpscalerType();
            refreshButtons();
            return;
        }

        if (button.f == UPSCALER_PRESET_BUTTON_ID) {
            MinecraftRemixHooks.cycleUpscalerPreset();
            refreshButtons();
            return;
        }

        if (button.f == RAY_RECONSTRUCTION_BUTTON_ID) {
            MinecraftRemixHooks.toggleRayReconstructionEnabled();
            refreshButtons();
            return;
        }

        if (button.f == DONE_BUTTON_ID) {
            this.b.a(this.parent);
        }
    }

    protected void a(char character, int keyCode) {
        if (keyCode == 1) {
            this.b.a(this.parent);
            return;
        }
        super.a(character, keyCode);
    }

    public void a(int mouseX, int mouseY, float partialTicks) {
        this.i();
        this.a(this.g, "BetaRT Settings", this.c / 2, 20, 0xFFFFFF);
        super.a(mouseX, mouseY, partialTicks);
    }

    private ke findButton(int buttonId) {
        for (Object entry : this.e) {
            if (entry instanceof ke) {
                ke button = (ke) entry;
                if (button.f == buttonId) {
                    return button;
                }
            }
        }
        return null;
    }

    private void refreshButtons() {
        ke rtQualityButton = findButton(RT_QUALITY_BUTTON_ID);
        if (rtQualityButton != null) {
            rtQualityButton.e = MinecraftRemixHooks.getRtQualityButtonLabel();
        }

        ke upscalerButton = findButton(UPSCALER_BUTTON_ID);
        if (upscalerButton != null) {
            upscalerButton.e = MinecraftRemixHooks.getUpscalerButtonLabel();
        }

        ke presetButton = findButton(UPSCALER_PRESET_BUTTON_ID);
        if (presetButton != null) {
            presetButton.e = MinecraftRemixHooks.getUpscalerPresetButtonLabel();
        }

        boolean showRayReconstruction = MinecraftRemixHooks.shouldShowRayReconstructionOption();
        ke rayReconstructionButton = findButton(RAY_RECONSTRUCTION_BUTTON_ID);
        if (rayReconstructionButton != null) {
            rayReconstructionButton.e = MinecraftRemixHooks.getRayReconstructionButtonLabel();
            rayReconstructionButton.g = showRayReconstruction;
            rayReconstructionButton.h = showRayReconstruction;
        }

        ke doneButton = findButton(DONE_BUTTON_ID);
        if (doneButton != null) {
            doneButton.d = getThirdRowY() + 48;
        }
    }

    private int getLeftColumnX() {
        return this.c / 2 - COLUMN_BUTTON_WIDTH - COLUMN_BUTTON_GAP / 2;
    }

    private int getRightColumnX() {
        return this.c / 2 + COLUMN_BUTTON_GAP / 2;
    }

    private int getFirstRowY() {
        return this.d / 6 + 64;
    }

    private int getSecondRowY() {
        return getFirstRowY() + 24;
    }

    private int getThirdRowY() {
        return getSecondRowY() + 24;
    }
}