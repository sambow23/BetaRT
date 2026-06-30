import mcrtx.bridge.McrtxRuntimeSettings;

public final class McrtxQuickSettingsScreen extends da {
    private static final int PANEL_LEFT = 8;
    private static final int PANEL_TOP = 8;
    private static final int PANEL_WIDTH = 220;
    private static final int PANEL_INSET = 8;
    private static final int HEADER_HEIGHT = 24;
    private static final int CONTROL_HEIGHT = 20;
    private static final int CONTROL_GAP = 4;
    private static final int SELECTOR_ARROW_WIDTH = 24;
    private static final int SELECTOR_LABEL_WIDTH = PANEL_WIDTH - PANEL_INSET * 2 - SELECTOR_ARROW_WIDTH * 2 - CONTROL_GAP * 2;
    private static final int CATEGORY_GAMEPLAY = 0;
    private static final int CATEGORY_GRAPHICS = 1;
    private static final int CATEGORY_DEBUG = 2;
    private static final int CATEGORY_MATERIAL = 3;
    private static final int CATEGORY_PREVIOUS_BUTTON_ID = 300;
    private static final int CATEGORY_LABEL_BUTTON_ID = 301;
    private static final int CATEGORY_NEXT_BUTTON_ID = 302;
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
    private static final int DISPLACEMENT_FACTOR_SLIDER_ID = 22;
    private static final int SUBSURFACE_MEASUREMENT_DISTANCE_SLIDER_ID = 23;
    private static final int SUBSURFACE_RADIUS_SCALE_SLIDER_ID = 24;
    private static final int SUBSURFACE_MAX_SAMPLE_RADIUS_SLIDER_ID = 25;
    private static final int SUBSURFACE_VOLUMETRIC_ANISOTROPY_SLIDER_ID = 26;
    private static final int WATER_MATERIAL_THICKNESS_SLIDER_ID = 27;
    private static final int SUBSURFACE_DIFFUSION_PROFILE_BUTTON_ID = 28;
    private static final int WATER_THIN_WALL_BUTTON_ID = 29;
    private static final int REMIX_ATMOSPHERE_CLOUDS_BUTTON_ID = 30;
    private static final int GAME_RAIN_PARTICLES_BUTTON_ID = 31;
    private static final int RESET_DEFAULTS_BUTTON_ID = 308;
    private static final int CLOSE_BUTTON_ID = 309;

    private static int activeCategory = McrtxRuntimeSettings.DEFAULT_QUICK_SETTINGS_CATEGORY;

    private int nextControlY;
    private int panelBottomY = PANEL_TOP + 80;

    public void b() {
        this.e.clear();
        activeCategory = normalizeCategory(McrtxRuntimeSettings.getQuickSettingsCategory());
        nextControlY = PANEL_TOP + HEADER_HEIGHT + CONTROL_GAP;

        addCategorySelectorControls();

        if (activeCategory == CATEGORY_GRAPHICS) {
            addGraphicsControls();
        } else if (activeCategory == CATEGORY_DEBUG) {
            addDebugControls();
        } else if (activeCategory == CATEGORY_MATERIAL) {
            addMaterialControls();
        } else {
            addGameplayControls();
        }

        nextControlY += CONTROL_GAP;
        addControl(new ke(
                CLOSE_BUTTON_ID,
                getControlX(),
                takeNextRowY(),
                getControlWidth(),
                CONTROL_HEIGHT,
                "Close"));
        panelBottomY = nextControlY - CONTROL_GAP + PANEL_INSET + 2;
        refreshButtons();
    }

    protected void a(ke button) {
        if (button == null || !button.g) {
            return;
        }

        if (button.f == CATEGORY_PREVIOUS_BUTTON_ID) {
            cycleCategory(-1);
            b();
            return;
        }

        if (button.f == CATEGORY_NEXT_BUTTON_ID || button.f == CATEGORY_LABEL_BUTTON_ID) {
            cycleCategory(1);
            b();
            return;
        }

        if (button.f == PLAYER_SHADOWS_BUTTON_ID) {
            MinecraftRemixHooks.setPlayerShadowsEnabled(!MinecraftRemixHooks.isPlayerShadowsEnabled());
            refreshButtons();
            return;
        }

        if (button.f == HELD_TORCH_LIGHTS_BUTTON_ID) {
            MinecraftRemixHooks.setHeldTorchLightsEnabled(!MinecraftRemixHooks.isHeldTorchLightsEnabled());
            refreshButtons();
            return;
        }

        if (button.f == UPSCALER_BUTTON_ID) {
            MinecraftRemixHooks.cycleUpscalerType();
            b();
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

        if (button.f == RT_QUALITY_BUTTON_ID) {
            MinecraftRemixHooks.cycleRtQuality();
            refreshButtons();
            return;
        }

        if (button.f == REMIX_ATMOSPHERE_CLOUDS_BUTTON_ID) {
            MinecraftRemixHooks.setRemixAtmosphereCloudsEnabled(!MinecraftRemixHooks.isRemixAtmosphereCloudsEnabled());
            refreshButtons();
            return;
        }

        if (button.f == GAME_RAIN_PARTICLES_BUTTON_ID) {
            MinecraftRemixHooks.setGameRainParticlesEnabled(!MinecraftRemixHooks.isGameRainParticlesEnabled());
            refreshButtons();
            return;
        }

        if (button.f == BLOCK_OUTLINE_BUTTON_ID) {
            MinecraftRemixHooks.setBlockOutlineEnabled(!MinecraftRemixHooks.isBlockOutlineEnabled());
            b();
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

        if (button.f == WATER_THIN_WALL_BUTTON_ID) {
            MinecraftRemixHooks.setWaterThinWalledEnabled(!MinecraftRemixHooks.isWaterThinWalledEnabled());
            b();
            return;
        }

        if (button.f == RESET_DEFAULTS_BUTTON_ID) {
            MinecraftRemixHooks.resetSubsurfaceSettingsToDefaults();
            b();
            return;
        }

        if (button.f == CLOSE_BUTTON_ID) {
            this.b.a((da) null);
        }
    }

    protected void a(char character, int keyCode) {
        if (keyCode == 1) {
            this.b.a((da) null);
            return;
        }
        super.a(character, keyCode);
    }

    public void a(int mouseX, int mouseY, float partialTicks) {
        int panelRight = PANEL_LEFT + PANEL_WIDTH;
        int panelBottom = panelBottomY;
        this.a(PANEL_LEFT, PANEL_TOP, panelRight, panelBottom, 0xB0101010, 0x90080808);
        this.a(PANEL_LEFT, PANEL_TOP, panelRight, PANEL_TOP + 1, 0x90D0D0D0);
        this.a(PANEL_LEFT, PANEL_TOP + HEADER_HEIGHT - 1, panelRight, PANEL_TOP + HEADER_HEIGHT, 0x60404040);
        this.a(PANEL_LEFT, panelBottom - 1, panelRight, panelBottom, 0x70303030);
        this.a(this.g, "BetaRT Settings", PANEL_LEFT + PANEL_WIDTH / 2, PANEL_TOP + 8, 0xFFFFFF);
        super.a(mouseX, mouseY, partialTicks);
    }

    public boolean c() {
        return false;
    }

    private void refreshButtons() {
        ke categoryLabelButton = findButton(CATEGORY_LABEL_BUTTON_ID);
        if (categoryLabelButton != null) {
            categoryLabelButton.e = getCategoryLabel();
        }

        ke playerShadowsButton = findButton(PLAYER_SHADOWS_BUTTON_ID);
        if (playerShadowsButton != null) {
            playerShadowsButton.e = MinecraftRemixHooks.getPlayerShadowsButtonLabel();
        }

        ke heldTorchLightsButton = findButton(HELD_TORCH_LIGHTS_BUTTON_ID);
        if (heldTorchLightsButton != null) {
            heldTorchLightsButton.e = MinecraftRemixHooks.getHeldTorchLightsButtonLabel();
        }

        ke upscalerButton = findButton(UPSCALER_BUTTON_ID);
        if (upscalerButton != null) {
            upscalerButton.e = MinecraftRemixHooks.getUpscalerButtonLabel();
        }

        ke upscalerPresetButton = findButton(UPSCALER_PRESET_BUTTON_ID);
        if (upscalerPresetButton != null) {
            upscalerPresetButton.e = MinecraftRemixHooks.getUpscalerPresetButtonLabel();
        }

        ke rayReconstructionButton = findButton(RAY_RECONSTRUCTION_BUTTON_ID);
        if (rayReconstructionButton != null) {
            rayReconstructionButton.e = MinecraftRemixHooks.getRayReconstructionButtonLabel();
        }

        ke rtQualityButton = findButton(RT_QUALITY_BUTTON_ID);
        if (rtQualityButton != null) {
            rtQualityButton.e = MinecraftRemixHooks.getRtQualityButtonLabel();
        }

        ke remixAtmosphereCloudsButton = findButton(REMIX_ATMOSPHERE_CLOUDS_BUTTON_ID);
        if (remixAtmosphereCloudsButton != null) {
            remixAtmosphereCloudsButton.e = MinecraftRemixHooks.getRemixAtmosphereCloudsButtonLabel();
        }

        ke gameRainParticlesButton = findButton(GAME_RAIN_PARTICLES_BUTTON_ID);
        if (gameRainParticlesButton != null) {
            gameRainParticlesButton.e = MinecraftRemixHooks.getGameRainParticlesButtonLabel();
        }

        ke blockOutlineButton = findButton(BLOCK_OUTLINE_BUTTON_ID);
        if (blockOutlineButton != null) {
            blockOutlineButton.e = MinecraftRemixHooks.getBlockOutlineButtonLabel();
        }

        ke blockOutlineStyleButton = findButton(BLOCK_OUTLINE_STYLE_BUTTON_ID);
        if (blockOutlineStyleButton != null) {
            blockOutlineStyleButton.e = MinecraftRemixHooks.getBlockOutlineStyleButtonLabel();
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

        ke waterThinWallButton = findButton(WATER_THIN_WALL_BUTTON_ID);
        if (waterThinWallButton != null) {
            waterThinWallButton.e = MinecraftRemixHooks.getWaterThinWallButtonLabel();
        }

        ke diffusionProfileButton = findButton(SUBSURFACE_DIFFUSION_PROFILE_BUTTON_ID);
        if (diffusionProfileButton != null) {
            diffusionProfileButton.e = MinecraftRemixHooks.getSubsurfaceDiffusionProfileButtonLabel();
        }
    }

    private void addCategorySelectorControls() {
        int rowY = takeNextRowY();
        int controlX = getControlX();
        int labelX = controlX + SELECTOR_ARROW_WIDTH + CONTROL_GAP;
        int nextButtonX = labelX + SELECTOR_LABEL_WIDTH + CONTROL_GAP;

        addControl(new ke(
                CATEGORY_PREVIOUS_BUTTON_ID,
                controlX,
                rowY,
                SELECTOR_ARROW_WIDTH,
                CONTROL_HEIGHT,
                "<"));
        addControl(new ke(
                CATEGORY_LABEL_BUTTON_ID,
                labelX,
                rowY,
                SELECTOR_LABEL_WIDTH,
                CONTROL_HEIGHT,
                getCategoryLabel()));
        addControl(new ke(
                CATEGORY_NEXT_BUTTON_ID,
                nextButtonX,
                rowY,
                SELECTOR_ARROW_WIDTH,
                CONTROL_HEIGHT,
                ">"));
    }

    private void addGameplayControls() {
        addControl(new McrtxFovSlider(
                GAMEPLAY_FOV_SLIDER_ID,
                getControlX(),
                takeNextRowY(),
                getControlWidth(),
                CONTROL_HEIGHT));
        addControl(new McrtxFovSlider(
                VIEW_MODEL_FOV_SLIDER_ID,
                getControlX(),
                takeNextRowY(),
                getControlWidth(),
                CONTROL_HEIGHT,
                true));
        addControl(new ke(
                PLAYER_SHADOWS_BUTTON_ID,
                getControlX(),
                takeNextRowY(),
                getControlWidth(),
                CONTROL_HEIGHT,
                MinecraftRemixHooks.getPlayerShadowsButtonLabel()));
        addControl(new ke(
                HELD_TORCH_LIGHTS_BUTTON_ID,
                getControlX(),
                takeNextRowY(),
                getControlWidth(),
                CONTROL_HEIGHT,
                MinecraftRemixHooks.getHeldTorchLightsButtonLabel()));
        addControl(new ke(
                BLOCK_OUTLINE_BUTTON_ID,
                getControlX(),
                takeNextRowY(),
                getControlWidth(),
                CONTROL_HEIGHT,
                MinecraftRemixHooks.getBlockOutlineButtonLabel()));

        if (MinecraftRemixHooks.isBlockOutlineEnabled()) {
            addControl(new ke(
                    BLOCK_OUTLINE_STYLE_BUTTON_ID,
                    getControlX(),
                    takeNextRowY(),
                    getControlWidth(),
                    CONTROL_HEIGHT,
                    MinecraftRemixHooks.getBlockOutlineStyleButtonLabel()));
            addControl(McrtxFovSlider.createBlockOutlineIntensitySlider(
                    BLOCK_OUTLINE_INTENSITY_SLIDER_ID,
                    getControlX(),
                    takeNextRowY(),
                    getControlWidth(),
                    CONTROL_HEIGHT));
        }
    }

    private void addGraphicsControls() {
        addControl(new ke(
                UPSCALER_BUTTON_ID,
                getControlX(),
                takeNextRowY(),
                getControlWidth(),
                CONTROL_HEIGHT,
                MinecraftRemixHooks.getUpscalerButtonLabel()));
        addControl(new ke(
                UPSCALER_PRESET_BUTTON_ID,
                getControlX(),
                takeNextRowY(),
                getControlWidth(),
                CONTROL_HEIGHT,
                MinecraftRemixHooks.getUpscalerPresetButtonLabel()));
        if (MinecraftRemixHooks.shouldShowRayReconstructionOption()) {
            addControl(new ke(
                    RAY_RECONSTRUCTION_BUTTON_ID,
                    getControlX(),
                    takeNextRowY(),
                    getControlWidth(),
                    CONTROL_HEIGHT,
                    MinecraftRemixHooks.getRayReconstructionButtonLabel()));
        }
        addControl(new ke(
                RT_QUALITY_BUTTON_ID,
                getControlX(),
                takeNextRowY(),
                getControlWidth(),
                CONTROL_HEIGHT,
                MinecraftRemixHooks.getRtQualityButtonLabel()));
        addControl(new ke(
                REMIX_ATMOSPHERE_CLOUDS_BUTTON_ID,
                getControlX(),
                takeNextRowY(),
                getControlWidth(),
                CONTROL_HEIGHT,
                MinecraftRemixHooks.getRemixAtmosphereCloudsButtonLabel()));
        addControl(new ke(
                GAME_RAIN_PARTICLES_BUTTON_ID,
                getControlX(),
                takeNextRowY(),
                getControlWidth(),
                CONTROL_HEIGHT,
                MinecraftRemixHooks.getGameRainParticlesButtonLabel()));
        addControl(McrtxFovSlider.createNoCullDistanceSlider(
                NO_CULL_DISTANCE_SLIDER_ID,
                getControlX(),
                takeNextRowY(),
                getControlWidth(),
                CONTROL_HEIGHT));
    }

    private void addDebugControls() {
        addControl(new ke(
                DYNAMIC_ENTITY_RENDERING_BUTTON_ID,
                getControlX(),
                takeNextRowY(),
                getControlWidth(),
                CONTROL_HEIGHT,
                MinecraftRemixHooks.getDynamicEntityRenderingButtonLabel()));
        addControl(new ke(
                LIVING_ENTITY_RENDERING_BUTTON_ID,
                getControlX(),
                takeNextRowY(),
                getControlWidth(),
                CONTROL_HEIGHT,
                MinecraftRemixHooks.getLivingEntityRenderingButtonLabel()));
        addControl(new ke(
                ITEM_ENTITY_RENDERING_BUTTON_ID,
                getControlX(),
                takeNextRowY(),
                getControlWidth(),
                CONTROL_HEIGHT,
                MinecraftRemixHooks.getItemEntityRenderingButtonLabel()));
        addControl(new ke(
                PAINTING_VANILLA_SUPPRESSION_BUTTON_ID,
                getControlX(),
                takeNextRowY(),
                getControlWidth(),
                CONTROL_HEIGHT,
                MinecraftRemixHooks.getPaintingVanillaSuppressionButtonLabel()));
        addControl(new ke(
                MOVING_PISTON_VANILLA_SUPPRESSION_BUTTON_ID,
                getControlX(),
                takeNextRowY(),
                getControlWidth(),
                CONTROL_HEIGHT,
                MinecraftRemixHooks.getMovingPistonVanillaSuppressionButtonLabel()));
        addControl(new ke(
                WORLD_RASTER_VANILLA_SUPPRESSION_BUTTON_ID,
                getControlX(),
                takeNextRowY(),
                getControlWidth(),
                CONTROL_HEIGHT,
                MinecraftRemixHooks.getWorldRasterVanillaSuppressionButtonLabel()));
        addControl(new ke(
                SIGN_CAPTURE_BUTTON_ID,
                getControlX(),
                takeNextRowY(),
                getControlWidth(),
                CONTROL_HEIGHT,
                MinecraftRemixHooks.getSignCaptureButtonLabel()));
        addControl(new ke(
                SIGN_TEXT_CAPTURE_BUTTON_ID,
                getControlX(),
                takeNextRowY(),
                getControlWidth(),
                CONTROL_HEIGHT,
                MinecraftRemixHooks.getSignTextCaptureButtonLabel()));
        addControl(new ke(
                SIGN_VANILLA_SUPPRESSION_BUTTON_ID,
                getControlX(),
                takeNextRowY(),
                getControlWidth(),
                CONTROL_HEIGHT,
                MinecraftRemixHooks.getSignVanillaSuppressionButtonLabel()));
    }

    private void addMaterialControls() {
        addControl(McrtxFovSlider.createDisplacementFactorSlider(
                DISPLACEMENT_FACTOR_SLIDER_ID,
                getControlX(),
                takeNextRowY(),
                getControlWidth(),
                CONTROL_HEIGHT));
        addControl(McrtxFovSlider.createSubsurfaceMeasurementDistanceSlider(
                SUBSURFACE_MEASUREMENT_DISTANCE_SLIDER_ID,
                getControlX(),
                takeNextRowY(),
                getControlWidth(),
                CONTROL_HEIGHT));
        addControl(McrtxFovSlider.createSubsurfaceRadiusScaleSlider(
                SUBSURFACE_RADIUS_SCALE_SLIDER_ID,
                getControlX(),
                takeNextRowY(),
                getControlWidth(),
                CONTROL_HEIGHT));
        addControl(McrtxFovSlider.createSubsurfaceMaxSampleRadiusSlider(
                SUBSURFACE_MAX_SAMPLE_RADIUS_SLIDER_ID,
                getControlX(),
                takeNextRowY(),
                getControlWidth(),
                CONTROL_HEIGHT));
        addControl(McrtxFovSlider.createSubsurfaceVolumetricAnisotropySlider(
                SUBSURFACE_VOLUMETRIC_ANISOTROPY_SLIDER_ID,
                getControlX(),
                takeNextRowY(),
                getControlWidth(),
                CONTROL_HEIGHT));
        addControl(new ke(
                SUBSURFACE_DIFFUSION_PROFILE_BUTTON_ID,
                getControlX(),
                takeNextRowY(),
                getControlWidth(),
                CONTROL_HEIGHT,
                MinecraftRemixHooks.getSubsurfaceDiffusionProfileButtonLabel()));
        addControl(new ke(
                WATER_THIN_WALL_BUTTON_ID,
                getControlX(),
                takeNextRowY(),
                getControlWidth(),
                CONTROL_HEIGHT,
                MinecraftRemixHooks.getWaterThinWallButtonLabel()));
        if (MinecraftRemixHooks.shouldShowWaterMaterialThicknessSlider()) {
            addControl(McrtxFovSlider.createWaterMaterialThicknessSlider(
                    WATER_MATERIAL_THICKNESS_SLIDER_ID,
                    getControlX(),
                    takeNextRowY(),
                    getControlWidth(),
                    CONTROL_HEIGHT));
        }
        addControl(new ke(
                RESET_DEFAULTS_BUTTON_ID,
                getControlX(),
                takeNextRowY(),
                getControlWidth(),
                CONTROL_HEIGHT,
                "Reset Material Defaults"));
    }

    private void addControl(ke control) {
        this.e.add(control);
    }

    private int takeNextRowY() {
        int rowY = nextControlY;
        nextControlY += CONTROL_HEIGHT + CONTROL_GAP;
        return rowY;
    }

    private void cycleCategory(int delta) {
        activeCategory = (activeCategory + delta + 4) % 4;
        McrtxRuntimeSettings.setQuickSettingsCategory(activeCategory);
    }

    private String getCategoryLabel() {
        return "Category: " + getCategoryName(activeCategory);
    }

    private String getCategoryName(int category) {
        if (category == CATEGORY_GRAPHICS) {
            return "Graphics";
        }
        if (category == CATEGORY_DEBUG) {
            return "Debug";
        }
        if (category == CATEGORY_MATERIAL) {
            return "Material";
        }
        return "Gameplay";
    }

    private ke findButton(int buttonId) {
        for (Object entry : this.e) {
            if (!(entry instanceof ke)) {
                continue;
            }

            ke button = (ke) entry;
            if (button.f == buttonId) {
                return button;
            }
        }
        return null;
    }

    private int getControlX() {
        return PANEL_LEFT + PANEL_INSET;
    }

    private int getControlWidth() {
        return PANEL_WIDTH - PANEL_INSET * 2;
    }

    private int normalizeCategory(int category) {
        if (category < CATEGORY_GAMEPLAY || category > CATEGORY_MATERIAL) {
            return CATEGORY_GAMEPLAY;
        }
        return category;
    }
}
