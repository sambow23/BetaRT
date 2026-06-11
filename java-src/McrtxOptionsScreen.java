public final class McrtxOptionsScreen extends da {
    private static final int COLUMN_BUTTON_WIDTH = 150;
    private static final int COLUMN_BUTTON_HEIGHT = 20;
    private static final int COLUMN_BUTTON_GAP = 4;
    private static final int FULL_WIDTH_CONTROL_WIDTH = COLUMN_BUTTON_WIDTH * 2 + COLUMN_BUTTON_GAP;
    private static final int HALF_WIDTH_CONTROL_WIDTH = COLUMN_BUTTON_WIDTH;
    private static final int TAB_GENERAL = 0;
    private static final int TAB_DEBUG = 1;
    private static final int TAB_MATERIALS = 2;
    private static final int TAB_BUTTON_WIDTH = (FULL_WIDTH_CONTROL_WIDTH - COLUMN_BUTTON_GAP * 2) / 3;
    private static final int GENERAL_TAB_BUTTON_ID = 30;
    private static final int DEBUG_TAB_BUTTON_ID = 31;
    private static final int MATERIALS_TAB_BUTTON_ID = 32;
    private static final int PLAYER_SHADOWS_BUTTON_ID = 1;
    private static final int HELD_TORCH_LIGHTS_BUTTON_ID = 2;
    private static final int UPSCALER_BUTTON_ID = 3;
    private static final int UPSCALER_PRESET_BUTTON_ID = 4;
    private static final int RAY_RECONSTRUCTION_BUTTON_ID = 5;
    private static final int RT_QUALITY_BUTTON_ID = 6;
    private static final int GAMEPLAY_FOV_SLIDER_ID = 7;
    private static final int VIEW_MODEL_FOV_SLIDER_ID = 8;
    private static final int NO_CULL_DISTANCE_SLIDER_ID = 9;
    private static final int DISPLACEMENT_FACTOR_SLIDER_ID = 22;
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
    private static final int SUBSURFACE_MEASUREMENT_DISTANCE_SLIDER_ID = 23;
    private static final int SUBSURFACE_RADIUS_SCALE_SLIDER_ID = 24;
    private static final int SUBSURFACE_MAX_SAMPLE_RADIUS_SLIDER_ID = 25;
    private static final int SUBSURFACE_VOLUMETRIC_ANISOTROPY_SLIDER_ID = 26;
    private static final int SUBSURFACE_DIFFUSION_PROFILE_BUTTON_ID = 27;

    private final da parent;
    private int activeTab = TAB_GENERAL;

    public McrtxOptionsScreen(da parent) {
        this.parent = parent;
    }

    public void b() {
        this.e.clear();
        addTabButtons();

        if (activeTab == TAB_DEBUG) {
            addDebugControls();
        } else if (activeTab == TAB_MATERIALS) {
            addMaterialControls();
        } else {
            addGeneralControls();
        }

        this.e.add(new ke(DONE_BUTTON_ID, this.c / 2 - 100, getDoneButtonY(), "Done"));
        refreshButtons();
    }

    private void addTabButtons() {
        int tabLeftX = getTabLeftX();
        int middleTabX = tabLeftX + TAB_BUTTON_WIDTH + COLUMN_BUTTON_GAP;
        int rightTabX = middleTabX + TAB_BUTTON_WIDTH + COLUMN_BUTTON_GAP;
        this.e.add(new ke(GENERAL_TAB_BUTTON_ID, tabLeftX, getTabRowY(), TAB_BUTTON_WIDTH, COLUMN_BUTTON_HEIGHT, "General"));
        this.e.add(new ke(DEBUG_TAB_BUTTON_ID, middleTabX, getTabRowY(), TAB_BUTTON_WIDTH, COLUMN_BUTTON_HEIGHT, "Debug"));
        this.e.add(new ke(MATERIALS_TAB_BUTTON_ID, rightTabX, getTabRowY(), TAB_BUTTON_WIDTH, COLUMN_BUTTON_HEIGHT, "Materials"));
    }

    private void addGeneralControls() {
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
        this.e.add(McrtxFovSlider.createBlockOutlineIntensitySlider(
            BLOCK_OUTLINE_INTENSITY_SLIDER_ID,
            getCenteredFullWidthControlX(),
            getGeneralBlockOutlineIntensitySliderRowY(),
            FULL_WIDTH_CONTROL_WIDTH,
            COLUMN_BUTTON_HEIGHT));
    }

    private void addMaterialControls() {
        this.e.add(McrtxFovSlider.createDisplacementFactorSlider(
            DISPLACEMENT_FACTOR_SLIDER_ID,
            getCenteredFullWidthControlX(),
            getDisplacementFactorSliderRowY(),
            FULL_WIDTH_CONTROL_WIDTH,
            COLUMN_BUTTON_HEIGHT));
        this.e.add(McrtxFovSlider.createSubsurfaceMeasurementDistanceSlider(
            SUBSURFACE_MEASUREMENT_DISTANCE_SLIDER_ID,
            getCenteredFullWidthControlX(),
            getSubsurfaceMeasurementDistanceSliderRowY(),
            FULL_WIDTH_CONTROL_WIDTH,
            COLUMN_BUTTON_HEIGHT));
        this.e.add(McrtxFovSlider.createSubsurfaceRadiusScaleSlider(
            SUBSURFACE_RADIUS_SCALE_SLIDER_ID,
            getCenteredFullWidthControlX(),
            getSubsurfaceRadiusScaleSliderRowY(),
            FULL_WIDTH_CONTROL_WIDTH,
            COLUMN_BUTTON_HEIGHT));
        this.e.add(McrtxFovSlider.createSubsurfaceMaxSampleRadiusSlider(
            SUBSURFACE_MAX_SAMPLE_RADIUS_SLIDER_ID,
            getCenteredFullWidthControlX(),
            getSubsurfaceMaxSampleRadiusSliderRowY(),
            FULL_WIDTH_CONTROL_WIDTH,
            COLUMN_BUTTON_HEIGHT));
        this.e.add(McrtxFovSlider.createSubsurfaceVolumetricAnisotropySlider(
            SUBSURFACE_VOLUMETRIC_ANISOTROPY_SLIDER_ID,
            getCenteredFullWidthControlX(),
            getSubsurfaceVolumetricAnisotropySliderRowY(),
            FULL_WIDTH_CONTROL_WIDTH,
            COLUMN_BUTTON_HEIGHT));
        this.e.add(new ke(
            SUBSURFACE_DIFFUSION_PROFILE_BUTTON_ID,
            getCenteredFullWidthControlX(),
            getSubsurfaceDiffusionProfileButtonRowY(),
            FULL_WIDTH_CONTROL_WIDTH,
            COLUMN_BUTTON_HEIGHT,
            MinecraftRemixHooks.getSubsurfaceDiffusionProfileButtonLabel()));
    }

    private void addDebugControls() {
        this.e.add(new ke(
            DYNAMIC_ENTITY_RENDERING_BUTTON_ID,
            getCenteredFullWidthControlX(),
            getDebugFirstButtonRowY(),
            FULL_WIDTH_CONTROL_WIDTH,
            COLUMN_BUTTON_HEIGHT,
            MinecraftRemixHooks.getDynamicEntityRenderingButtonLabel()));
        this.e.add(new ke(
            LIVING_ENTITY_RENDERING_BUTTON_ID,
            getCenteredFullWidthControlX(),
            getDebugSecondButtonRowY(),
            FULL_WIDTH_CONTROL_WIDTH,
            COLUMN_BUTTON_HEIGHT,
            MinecraftRemixHooks.getLivingEntityRenderingButtonLabel()));
        this.e.add(new ke(
            ITEM_ENTITY_RENDERING_BUTTON_ID,
            getCenteredFullWidthControlX(),
            getDebugThirdButtonRowY(),
            FULL_WIDTH_CONTROL_WIDTH,
            COLUMN_BUTTON_HEIGHT,
            MinecraftRemixHooks.getItemEntityRenderingButtonLabel()));
        this.e.add(new ke(
            PAINTING_VANILLA_SUPPRESSION_BUTTON_ID,
            getCenteredFullWidthControlX(),
            getDebugFourthButtonRowY(),
            FULL_WIDTH_CONTROL_WIDTH,
            COLUMN_BUTTON_HEIGHT,
            MinecraftRemixHooks.getPaintingVanillaSuppressionButtonLabel()));
        this.e.add(new ke(
            MOVING_PISTON_VANILLA_SUPPRESSION_BUTTON_ID,
            getCenteredFullWidthControlX(),
            getDebugFifthButtonRowY(),
            FULL_WIDTH_CONTROL_WIDTH,
            COLUMN_BUTTON_HEIGHT,
            MinecraftRemixHooks.getMovingPistonVanillaSuppressionButtonLabel()));
        this.e.add(new ke(
            WORLD_RASTER_VANILLA_SUPPRESSION_BUTTON_ID,
            getCenteredFullWidthControlX(),
            getDebugSixthButtonRowY(),
            FULL_WIDTH_CONTROL_WIDTH,
            COLUMN_BUTTON_HEIGHT,
            MinecraftRemixHooks.getWorldRasterVanillaSuppressionButtonLabel()));
        this.e.add(new ke(
            SIGN_CAPTURE_BUTTON_ID,
            getCenteredFullWidthControlX(),
            getDebugSeventhButtonRowY(),
            FULL_WIDTH_CONTROL_WIDTH,
            COLUMN_BUTTON_HEIGHT,
            MinecraftRemixHooks.getSignCaptureButtonLabel()));
        this.e.add(new ke(
            SIGN_TEXT_CAPTURE_BUTTON_ID,
            getCenteredFullWidthControlX(),
            getDebugEighthButtonRowY(),
            FULL_WIDTH_CONTROL_WIDTH,
            COLUMN_BUTTON_HEIGHT,
            MinecraftRemixHooks.getSignTextCaptureButtonLabel()));
        this.e.add(new ke(
            SIGN_VANILLA_SUPPRESSION_BUTTON_ID,
            getCenteredFullWidthControlX(),
            getDebugNinthButtonRowY(),
            FULL_WIDTH_CONTROL_WIDTH,
            COLUMN_BUTTON_HEIGHT,
            MinecraftRemixHooks.getSignVanillaSuppressionButtonLabel()));
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

        if (button.f == GENERAL_TAB_BUTTON_ID) {
            activeTab = TAB_GENERAL;
            b();
            return;
        }

        if (button.f == DEBUG_TAB_BUTTON_ID) {
            activeTab = TAB_DEBUG;
            b();
            return;
        }

        if (button.f == MATERIALS_TAB_BUTTON_ID) {
            activeTab = TAB_MATERIALS;
            b();
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

        if (button.f == SUBSURFACE_DIFFUSION_PROFILE_BUTTON_ID) {
            MinecraftRemixHooks.setSubsurfaceDiffusionProfileEnabled(!MinecraftRemixHooks.isSubsurfaceDiffusionProfileEnabled());
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
        this.a(this.g, getScreenTitle(), this.c / 2, 20, 0xFFFFFF);
        super.a(mouseX, mouseY, partialTicks);
    }

    private String getScreenTitle() {
        if (activeTab == TAB_DEBUG) {
            return "BetaRT Settings - Debug";
        }
        if (activeTab == TAB_MATERIALS) {
            return "BetaRT Settings - Materials";
        }
        return "BetaRT Settings";
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
        ke generalTabButton = findButton(GENERAL_TAB_BUTTON_ID);
        if (generalTabButton != null) {
            generalTabButton.g = activeTab != TAB_GENERAL;
            generalTabButton.h = true;
        }

        ke debugTabButton = findButton(DEBUG_TAB_BUTTON_ID);
        if (debugTabButton != null) {
            debugTabButton.g = activeTab != TAB_DEBUG;
            debugTabButton.h = true;
        }

        ke materialsTabButton = findButton(MATERIALS_TAB_BUTTON_ID);
        if (materialsTabButton != null) {
            materialsTabButton.g = activeTab != TAB_MATERIALS;
            materialsTabButton.h = true;
        }

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

        ke subsurfaceDiffusionProfileButton = findButton(SUBSURFACE_DIFFUSION_PROFILE_BUTTON_ID);
        if (subsurfaceDiffusionProfileButton != null) {
            subsurfaceDiffusionProfileButton.e = MinecraftRemixHooks.getSubsurfaceDiffusionProfileButtonLabel();
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

    private int getTabLeftX() {
        return this.c / 2 - FULL_WIDTH_CONTROL_WIDTH / 2;
    }

    private int getTabRowY() {
        return this.d / 6 + 28;
    }

    private int getGameplaySliderRowY() {
        return getTabRowY() + 36;
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
        if (activeTab == TAB_DEBUG) {
            return getDebugNinthButtonRowY() + 48;
        }
        if (activeTab == TAB_MATERIALS) {
            return getSubsurfaceDiffusionProfileButtonRowY() + 48;
        }
        return getGeneralBlockOutlineIntensitySliderRowY() + 48;
    }

    private int getFourthButtonRowY() {
        return getThirdButtonRowY() + 24;
    }

    private int getDebugFirstButtonRowY() {
        return getGameplaySliderRowY();
    }

    private int getDebugSecondButtonRowY() {
        return getDebugFirstButtonRowY() + 24;
    }

    private int getDebugThirdButtonRowY() {
        return getDebugSecondButtonRowY() + 24;
    }

    private int getDebugFourthButtonRowY() {
        return getDebugThirdButtonRowY() + 24;
    }

    private int getDebugFifthButtonRowY() {
        return getDebugFourthButtonRowY() + 24;
    }

    private int getDebugSixthButtonRowY() {
        return getDebugFifthButtonRowY() + 24;
    }

    private int getDebugSeventhButtonRowY() {
        return getDebugSixthButtonRowY() + 24;
    }

    private int getDebugEighthButtonRowY() {
        return getDebugSeventhButtonRowY() + 24;
    }

    private int getDebugNinthButtonRowY() {
        return getDebugEighthButtonRowY() + 24;
    }

    private int getGeneralBlockOutlineIntensitySliderRowY() {
        return getFourthButtonRowY() + 24;
    }

    private int getDisplacementFactorSliderRowY() {
        return getGameplaySliderRowY();
    }

    private int getSubsurfaceMeasurementDistanceSliderRowY() {
        return getDisplacementFactorSliderRowY() + 24;
    }

    private int getSubsurfaceRadiusScaleSliderRowY() {
        return getSubsurfaceMeasurementDistanceSliderRowY() + 24;
    }

    private int getSubsurfaceMaxSampleRadiusSliderRowY() {
        return getSubsurfaceRadiusScaleSliderRowY() + 24;
    }

    private int getSubsurfaceVolumetricAnisotropySliderRowY() {
        return getSubsurfaceMaxSampleRadiusSliderRowY() + 24;
    }

    private int getSubsurfaceDiffusionProfileButtonRowY() {
        return getSubsurfaceVolumetricAnisotropySliderRowY() + 24;
    }
}