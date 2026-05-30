public final class McrtxOptionsScreen extends da {
    private static final int COLUMN_BUTTON_WIDTH = 150;
    private static final int COLUMN_BUTTON_HEIGHT = 20;
    private static final int COLUMN_BUTTON_GAP = 4;
    private static final int FULL_WIDTH_CONTROL_WIDTH = COLUMN_BUTTON_WIDTH * 2 + COLUMN_BUTTON_GAP;
    private static final int HALF_WIDTH_CONTROL_WIDTH = COLUMN_BUTTON_WIDTH;
    private static final int PLAYER_SHADOWS_BUTTON_ID = 1;
    private static final int HELD_TORCH_LIGHTS_BUTTON_ID = 2;
    private static final int UPSCALER_BUTTON_ID = 3;
    private static final int UPSCALER_PRESET_BUTTON_ID = 4;
    private static final int RAY_RECONSTRUCTION_BUTTON_ID = 5;
    private static final int RT_QUALITY_BUTTON_ID = 6;
    private static final int GAMEPLAY_FOV_SLIDER_ID = 7;
    private static final int VIEW_MODEL_FOV_SLIDER_ID = 8;
    private static final int NO_CULL_DISTANCE_SLIDER_ID = 9;
    private static final int BLOCK_OUTLINE_BUTTON_ID = 10;
    private static final int BLOCK_OUTLINE_STYLE_BUTTON_ID = 11;
    private static final int BLOCK_OUTLINE_INTENSITY_SLIDER_ID = 12;
    private static final int DYNAMIC_ENTITY_RENDERING_BUTTON_ID = 13;
    private static final int PAINTING_VANILLA_SUPPRESSION_BUTTON_ID = 14;
    private static final int MOVING_PISTON_VANILLA_SUPPRESSION_BUTTON_ID = 15;
    private static final int WORLD_RASTER_VANILLA_SUPPRESSION_BUTTON_ID = 16;
    private static final int LIVING_ENTITY_RENDERING_BUTTON_ID = 17;
    private static final int ITEM_ENTITY_RENDERING_BUTTON_ID = 18;
    private static final int SIGN_CAPTURE_BUTTON_ID = 19;
    private static final int SIGN_TEXT_CAPTURE_BUTTON_ID = 20;
    private static final int SIGN_VANILLA_SUPPRESSION_BUTTON_ID = 21;
    private static final int DONE_BUTTON_ID = 200;

    private final da parent;

    public McrtxOptionsScreen(da parent) {
        this.parent = parent;
    }

    public void b() {
        this.e.clear();
        int leftColumnX = getLeftColumnX();
        int rightColumnX = getRightColumnX();
        this.e.add(new McrtxFovSlider(
                GAMEPLAY_FOV_SLIDER_ID,
                leftColumnX,
                getGameplaySliderRowY(),
                HALF_WIDTH_CONTROL_WIDTH,
                COLUMN_BUTTON_HEIGHT));
        this.e.add(new McrtxFovSlider(
                VIEW_MODEL_FOV_SLIDER_ID,
                rightColumnX,
                getGameplaySliderRowY(),
                HALF_WIDTH_CONTROL_WIDTH,
                COLUMN_BUTTON_HEIGHT,
                true));
        this.e.add(McrtxFovSlider.createNoCullDistanceSlider(
                NO_CULL_DISTANCE_SLIDER_ID,
                getCenteredHalfWidthControlX(),
                getNoCullDistanceSliderRowY(),
                HALF_WIDTH_CONTROL_WIDTH,
                COLUMN_BUTTON_HEIGHT));
        this.e.add(new ke(PLAYER_SHADOWS_BUTTON_ID, leftColumnX, getFirstButtonRowY(), COLUMN_BUTTON_WIDTH, COLUMN_BUTTON_HEIGHT, MinecraftRemixHooks.getPlayerShadowsButtonLabel()));
        this.e.add(new ke(UPSCALER_BUTTON_ID, rightColumnX, getFirstButtonRowY(), COLUMN_BUTTON_WIDTH, COLUMN_BUTTON_HEIGHT, MinecraftRemixHooks.getUpscalerButtonLabel()));
        this.e.add(new ke(HELD_TORCH_LIGHTS_BUTTON_ID, leftColumnX, getSecondButtonRowY(), COLUMN_BUTTON_WIDTH, COLUMN_BUTTON_HEIGHT, MinecraftRemixHooks.getHeldTorchLightsButtonLabel()));
        this.e.add(new ke(UPSCALER_PRESET_BUTTON_ID, rightColumnX, getSecondButtonRowY(), COLUMN_BUTTON_WIDTH, COLUMN_BUTTON_HEIGHT, MinecraftRemixHooks.getUpscalerPresetButtonLabel()));
        this.e.add(new ke(RT_QUALITY_BUTTON_ID, leftColumnX, getThirdButtonRowY(), COLUMN_BUTTON_WIDTH, COLUMN_BUTTON_HEIGHT, MinecraftRemixHooks.getRtQualityButtonLabel()));
        this.e.add(new ke(RAY_RECONSTRUCTION_BUTTON_ID, rightColumnX, getThirdButtonRowY(), COLUMN_BUTTON_WIDTH, COLUMN_BUTTON_HEIGHT, MinecraftRemixHooks.getRayReconstructionButtonLabel()));
        this.e.add(new ke(BLOCK_OUTLINE_BUTTON_ID, leftColumnX, getFourthButtonRowY(), COLUMN_BUTTON_WIDTH, COLUMN_BUTTON_HEIGHT, MinecraftRemixHooks.getBlockOutlineButtonLabel()));
        this.e.add(new ke(BLOCK_OUTLINE_STYLE_BUTTON_ID, rightColumnX, getFourthButtonRowY(), COLUMN_BUTTON_WIDTH, COLUMN_BUTTON_HEIGHT, MinecraftRemixHooks.getBlockOutlineStyleButtonLabel()));
        this.e.add(new ke(
            DYNAMIC_ENTITY_RENDERING_BUTTON_ID,
            getCenteredFullWidthControlX(),
            getFifthButtonRowY(),
            FULL_WIDTH_CONTROL_WIDTH,
            COLUMN_BUTTON_HEIGHT,
            MinecraftRemixHooks.getDynamicEntityRenderingButtonLabel()));
        this.e.add(new ke(
            LIVING_ENTITY_RENDERING_BUTTON_ID,
            getCenteredFullWidthControlX(),
            getSixthButtonRowY(),
            FULL_WIDTH_CONTROL_WIDTH,
            COLUMN_BUTTON_HEIGHT,
            MinecraftRemixHooks.getLivingEntityRenderingButtonLabel()));
        this.e.add(new ke(
            ITEM_ENTITY_RENDERING_BUTTON_ID,
            getCenteredFullWidthControlX(),
            getSeventhButtonRowY(),
            FULL_WIDTH_CONTROL_WIDTH,
            COLUMN_BUTTON_HEIGHT,
            MinecraftRemixHooks.getItemEntityRenderingButtonLabel()));
        this.e.add(new ke(
            PAINTING_VANILLA_SUPPRESSION_BUTTON_ID,
            getCenteredFullWidthControlX(),
            getEighthButtonRowY(),
            FULL_WIDTH_CONTROL_WIDTH,
            COLUMN_BUTTON_HEIGHT,
            MinecraftRemixHooks.getPaintingVanillaSuppressionButtonLabel()));
        this.e.add(new ke(
            MOVING_PISTON_VANILLA_SUPPRESSION_BUTTON_ID,
            getCenteredFullWidthControlX(),
            getNinthButtonRowY(),
            FULL_WIDTH_CONTROL_WIDTH,
            COLUMN_BUTTON_HEIGHT,
            MinecraftRemixHooks.getMovingPistonVanillaSuppressionButtonLabel()));
        this.e.add(new ke(
            WORLD_RASTER_VANILLA_SUPPRESSION_BUTTON_ID,
            getCenteredFullWidthControlX(),
            getTenthButtonRowY(),
            FULL_WIDTH_CONTROL_WIDTH,
            COLUMN_BUTTON_HEIGHT,
            MinecraftRemixHooks.getWorldRasterVanillaSuppressionButtonLabel()));
        this.e.add(new ke(
            SIGN_CAPTURE_BUTTON_ID,
            getCenteredFullWidthControlX(),
            getEleventhButtonRowY(),
            FULL_WIDTH_CONTROL_WIDTH,
            COLUMN_BUTTON_HEIGHT,
            MinecraftRemixHooks.getSignCaptureButtonLabel()));
        this.e.add(new ke(
            SIGN_TEXT_CAPTURE_BUTTON_ID,
            getCenteredFullWidthControlX(),
            getTwelfthButtonRowY(),
            FULL_WIDTH_CONTROL_WIDTH,
            COLUMN_BUTTON_HEIGHT,
            MinecraftRemixHooks.getSignTextCaptureButtonLabel()));
        this.e.add(new ke(
            SIGN_VANILLA_SUPPRESSION_BUTTON_ID,
            getCenteredFullWidthControlX(),
            getThirteenthButtonRowY(),
            FULL_WIDTH_CONTROL_WIDTH,
            COLUMN_BUTTON_HEIGHT,
            MinecraftRemixHooks.getSignVanillaSuppressionButtonLabel()));
        this.e.add(McrtxFovSlider.createBlockOutlineIntensitySlider(
            BLOCK_OUTLINE_INTENSITY_SLIDER_ID,
            getCenteredFullWidthControlX(),
            getBlockOutlineIntensitySliderRowY(),
            FULL_WIDTH_CONTROL_WIDTH,
            COLUMN_BUTTON_HEIGHT));
        this.e.add(new ke(DONE_BUTTON_ID, this.c / 2 - 100, getDoneButtonY(), "Done"));
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

        if (button.f == BLOCK_OUTLINE_BUTTON_ID) {
            MinecraftRemixHooks.setBlockOutlineEnabled(!MinecraftRemixHooks.isBlockOutlineEnabled());
            refreshButtons();
            return;
        }

        if (button.f == BLOCK_OUTLINE_STYLE_BUTTON_ID) {
            MinecraftRemixHooks.cycleBlockOutlineStyle();
            refreshButtons();
            return;
        }

        if (button.f == DYNAMIC_ENTITY_RENDERING_BUTTON_ID) {
            MinecraftRemixHooks.setDynamicEntityRenderingEnabled(!MinecraftRemixHooks.isDynamicEntityRenderingEnabled());
            refreshButtons();
            return;
        }

        if (button.f == LIVING_ENTITY_RENDERING_BUTTON_ID) {
            MinecraftRemixHooks.setLivingEntityRenderingEnabled(!MinecraftRemixHooks.isLivingEntityRenderingEnabled());
            refreshButtons();
            return;
        }

        if (button.f == ITEM_ENTITY_RENDERING_BUTTON_ID) {
            MinecraftRemixHooks.setItemEntityRenderingEnabled(!MinecraftRemixHooks.isItemEntityRenderingEnabled());
            refreshButtons();
            return;
        }

        if (button.f == PAINTING_VANILLA_SUPPRESSION_BUTTON_ID) {
            MinecraftRemixHooks.setPaintingVanillaSuppressionEnabled(!MinecraftRemixHooks.isPaintingVanillaSuppressionEnabled());
            refreshButtons();
            return;
        }

        if (button.f == MOVING_PISTON_VANILLA_SUPPRESSION_BUTTON_ID) {
            MinecraftRemixHooks.setMovingPistonVanillaSuppressionEnabled(!MinecraftRemixHooks.isMovingPistonVanillaSuppressionEnabled());
            refreshButtons();
            return;
        }

        if (button.f == WORLD_RASTER_VANILLA_SUPPRESSION_BUTTON_ID) {
            MinecraftRemixHooks.setWorldRasterVanillaSuppressionEnabled(!MinecraftRemixHooks.isWorldRasterVanillaSuppressionEnabled());
            refreshButtons();
            return;
        }

        if (button.f == SIGN_CAPTURE_BUTTON_ID) {
            MinecraftRemixHooks.setSignCaptureEnabled(!MinecraftRemixHooks.isSignCaptureEnabled());
            refreshButtons();
            return;
        }

        if (button.f == SIGN_TEXT_CAPTURE_BUTTON_ID) {
            MinecraftRemixHooks.setSignTextCaptureEnabled(!MinecraftRemixHooks.isSignTextCaptureEnabled());
            refreshButtons();
            return;
        }

        if (button.f == SIGN_VANILLA_SUPPRESSION_BUTTON_ID) {
            MinecraftRemixHooks.setSignVanillaSuppressionEnabled(!MinecraftRemixHooks.isSignVanillaSuppressionEnabled());
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

        ke blockOutlineButton = findButton(BLOCK_OUTLINE_BUTTON_ID);
        if (blockOutlineButton != null) {
            blockOutlineButton.e = MinecraftRemixHooks.getBlockOutlineButtonLabel();
        }

        ke dynamicEntityRenderingButton = findButton(DYNAMIC_ENTITY_RENDERING_BUTTON_ID);
        if (dynamicEntityRenderingButton != null) {
            dynamicEntityRenderingButton.e = MinecraftRemixHooks.getDynamicEntityRenderingButtonLabel();
        }

        ke livingEntityRenderingButton = findButton(LIVING_ENTITY_RENDERING_BUTTON_ID);
        if (livingEntityRenderingButton != null) {
            livingEntityRenderingButton.e = MinecraftRemixHooks.getLivingEntityRenderingButtonLabel();
        }

        ke itemEntityRenderingButton = findButton(ITEM_ENTITY_RENDERING_BUTTON_ID);
        if (itemEntityRenderingButton != null) {
            itemEntityRenderingButton.e = MinecraftRemixHooks.getItemEntityRenderingButtonLabel();
        }

        ke paintingVanillaSuppressionButton = findButton(PAINTING_VANILLA_SUPPRESSION_BUTTON_ID);
        if (paintingVanillaSuppressionButton != null) {
            paintingVanillaSuppressionButton.e = MinecraftRemixHooks.getPaintingVanillaSuppressionButtonLabel();
        }

        ke movingPistonVanillaSuppressionButton = findButton(MOVING_PISTON_VANILLA_SUPPRESSION_BUTTON_ID);
        if (movingPistonVanillaSuppressionButton != null) {
            movingPistonVanillaSuppressionButton.e = MinecraftRemixHooks.getMovingPistonVanillaSuppressionButtonLabel();
        }

        ke worldRasterVanillaSuppressionButton = findButton(WORLD_RASTER_VANILLA_SUPPRESSION_BUTTON_ID);
        if (worldRasterVanillaSuppressionButton != null) {
            worldRasterVanillaSuppressionButton.e = MinecraftRemixHooks.getWorldRasterVanillaSuppressionButtonLabel();
        }

        ke signCaptureButton = findButton(SIGN_CAPTURE_BUTTON_ID);
        if (signCaptureButton != null) {
            signCaptureButton.e = MinecraftRemixHooks.getSignCaptureButtonLabel();
        }

        ke signTextCaptureButton = findButton(SIGN_TEXT_CAPTURE_BUTTON_ID);
        if (signTextCaptureButton != null) {
            signTextCaptureButton.e = MinecraftRemixHooks.getSignTextCaptureButtonLabel();
        }

        ke signVanillaSuppressionButton = findButton(SIGN_VANILLA_SUPPRESSION_BUTTON_ID);
        if (signVanillaSuppressionButton != null) {
            signVanillaSuppressionButton.e = MinecraftRemixHooks.getSignVanillaSuppressionButtonLabel();
        }

        boolean showBlockOutlineStyle = MinecraftRemixHooks.isBlockOutlineEnabled();
        ke blockOutlineStyleButton = findButton(BLOCK_OUTLINE_STYLE_BUTTON_ID);
        if (blockOutlineStyleButton != null) {
            blockOutlineStyleButton.e = MinecraftRemixHooks.getBlockOutlineStyleButtonLabel();
            blockOutlineStyleButton.g = showBlockOutlineStyle;
            blockOutlineStyleButton.h = showBlockOutlineStyle;
        }

        boolean showBlockOutlineIntensity = MinecraftRemixHooks.shouldShowBlockOutlineIntensitySlider();
        ke blockOutlineIntensitySlider = findButton(BLOCK_OUTLINE_INTENSITY_SLIDER_ID);
        if (blockOutlineIntensitySlider != null) {
            blockOutlineIntensitySlider.g = showBlockOutlineIntensity;
            blockOutlineIntensitySlider.h = showBlockOutlineIntensity;
        }

        ke doneButton = findButton(DONE_BUTTON_ID);
        if (doneButton != null) {
            doneButton.d = getDoneButtonY();
        }
    }

    private int getLeftColumnX() {
        return this.c / 2 - COLUMN_BUTTON_WIDTH - COLUMN_BUTTON_GAP / 2;
    }

    private int getRightColumnX() {
        return this.c / 2 + COLUMN_BUTTON_GAP / 2;
    }

    private int getCenteredHalfWidthControlX() {
        return this.c / 2 - HALF_WIDTH_CONTROL_WIDTH / 2;
    }

    private int getCenteredFullWidthControlX() {
        return this.c / 2 - FULL_WIDTH_CONTROL_WIDTH / 2;
    }

    private int getGameplaySliderRowY() {
        return this.d / 6 + 64;
    }

    private int getNoCullDistanceSliderRowY() {
        return getGameplaySliderRowY() + 24;
    }

    private int getFirstButtonRowY() {
        return getNoCullDistanceSliderRowY() + 28;
    }

    private int getSecondButtonRowY() {
        return getFirstButtonRowY() + 24;
    }

    private int getThirdButtonRowY() {
        return getSecondButtonRowY() + 24;
    }

    private int getDoneButtonY() {
        return getBlockOutlineIntensitySliderRowY() + 48;
    }

    private int getFourthButtonRowY() {
        return getThirdButtonRowY() + 24;
    }

    private int getFifthButtonRowY() {
        return getFourthButtonRowY() + 24;
    }

    private int getSixthButtonRowY() {
        return getFifthButtonRowY() + 24;
    }

    private int getSeventhButtonRowY() {
        return getSixthButtonRowY() + 24;
    }

    private int getEighthButtonRowY() {
        return getSeventhButtonRowY() + 24;
    }

    private int getNinthButtonRowY() {
        return getEighthButtonRowY() + 24;
    }

    private int getTenthButtonRowY() {
        return getNinthButtonRowY() + 24;
    }

    private int getEleventhButtonRowY() {
        return getTenthButtonRowY() + 24;
    }

    private int getTwelfthButtonRowY() {
        return getEleventhButtonRowY() + 24;
    }

    private int getThirteenthButtonRowY() {
        return getTwelfthButtonRowY() + 24;
    }

    private int getBlockOutlineIntensitySliderRowY() {
        return getThirteenthButtonRowY() + 24;
    }
}