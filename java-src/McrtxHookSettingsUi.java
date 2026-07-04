import mcrtx.bridge.McrtxCloudMode;
import mcrtx.bridge.McrtxRuntimeSettings;
import mcrtx.bridge.MinecraftRenderHooks;

final class McrtxHookSettingsUi {
    private McrtxHookSettingsUi() {
    }

    static boolean shouldShowWaterMaterialThicknessSlider() {
        return McrtxRuntimeSettings.isWaterThinWalledEnabled();
    }

    static boolean shouldShowBlockOutlineIntensitySlider() {
        return McrtxRuntimeSettings.isBlockOutlineEnabled()
                && isBlockOutlineEmissiveStyle(McrtxRuntimeSettings.getBlockOutlineStyle());
    }

    static String getPlayerShadowsButtonLabel() {
        return "First-Person Player Shadows: " + formatToggleState(McrtxRuntimeSettings.isPlayerShadowsEnabled());
    }

    static String getHeldTorchLightsButtonLabel() {
        return "Held Torch Lights: " + formatToggleState(McrtxRuntimeSettings.isHeldTorchLightsEnabled());
    }

    static String getDynamicEntityRenderingButtonLabel() {
        return "Dynamic Entities: " + formatToggleState(McrtxRuntimeSettings.isDynamicEntityRenderingEnabled());
    }

    static String getLivingEntityRenderingButtonLabel() {
        return "Living Entities: " + formatToggleState(McrtxRuntimeSettings.isLivingEntityRenderingEnabled());
    }

    static String getItemEntityRenderingButtonLabel() {
        return "Item Entities: " + formatToggleState(McrtxRuntimeSettings.isItemEntityRenderingEnabled());
    }

    static String getPaintingVanillaSuppressionButtonLabel() {
        return "Replace Paintings: " + formatToggleState(McrtxRuntimeSettings.isPaintingVanillaSuppressionEnabled());
    }

    static String getMovingPistonVanillaSuppressionButtonLabel() {
        return "Replace Moving Pistons: " + formatToggleState(McrtxRuntimeSettings.isMovingPistonVanillaSuppressionEnabled());
    }

    static String getWorldRasterVanillaSuppressionButtonLabel() {
        return "Suppress World Raster: " + formatToggleState(McrtxRuntimeSettings.isWorldRasterVanillaSuppressionEnabled());
    }

    static String getSignCaptureButtonLabel() {
        return "Capture Signs: " + formatToggleState(McrtxRuntimeSettings.isSignCaptureEnabled());
    }

    static String getSignTextCaptureButtonLabel() {
        return "Capture Sign Text: " + formatToggleState(McrtxRuntimeSettings.isSignTextCaptureEnabled());
    }

    static String getSignVanillaSuppressionButtonLabel() {
        return "Replace Signs: " + formatToggleState(McrtxRuntimeSettings.isSignVanillaSuppressionEnabled());
    }

    static String getRtQualityButtonLabel() {
        return "PT Quality: " + describeRtQuality(McrtxRuntimeSettings.getRtQuality());
    }

    static String getUpscalerButtonLabel() {
        return "Upscaler: " + describeUpscalerType(McrtxRuntimeSettings.getUpscalerType());
    }

    static String getUpscalerPresetButtonLabel() {
        int upscalerType = McrtxRuntimeSettings.getUpscalerType();
        switch (upscalerType) {
            case McrtxRuntimeSettings.UPSCALER_TYPE_DLSS:
                return "Preset: " + describeDlssPreset(McrtxRuntimeSettings.getDlssPreset());
            case McrtxRuntimeSettings.UPSCALER_TYPE_XESS:
                return "Preset: " + describeXessPreset(McrtxRuntimeSettings.getXessPreset());
            case McrtxRuntimeSettings.UPSCALER_TYPE_TAAU:
                return "Preset: " + describeTaauPreset(McrtxRuntimeSettings.getTaauPreset());
            case McrtxRuntimeSettings.UPSCALER_TYPE_NONE:
            default:
                return "Preset: N/A";
        }
    }

    static String getRayReconstructionButtonLabel() {
        return "Ray Reconstruction: " + formatToggleState(McrtxRuntimeSettings.isRayReconstructionEnabled());
    }

    static String getBlockOutlineButtonLabel() {
        return "Block Outline: " + formatToggleState(McrtxRuntimeSettings.isBlockOutlineEnabled());
    }

    static String getBlockOutlineStyleButtonLabel() {
        return "Outline Style: " + describeBlockOutlineStyle(McrtxRuntimeSettings.getBlockOutlineStyle());
    }

    static String getSubsurfaceDiffusionProfileButtonLabel() {
        return "SSS Diffusion: " + formatToggleState(McrtxRuntimeSettings.isSubsurfaceDiffusionProfileEnabled());
    }

    static String getWaterThinWallButtonLabel() {
        return McrtxRuntimeSettings.isWaterThinWalledEnabled()
                ? "Water Mode: Thin-Walled"
                : "Water Mode: Refractive";
    }

    static String getRemixAtmosphereCloudsButtonLabel() {
        return McrtxCloudMode.formatButtonLabel(McrtxRuntimeSettings.isRemixAtmosphereCloudsEnabled());
    }

    static String getGameRainParticlesButtonLabel() {
        return "Game Rain: " + formatToggleState(McrtxRuntimeSettings.isGameRainParticlesEnabled());
    }

    static void setPlayerShadowsEnabled(boolean enabled) {
        McrtxRuntimeSettings.setPlayerShadowsEnabled(enabled);
        RemixDynamicEntityCapture.setPlayerShadowsEnabled(enabled);
        MinecraftRenderHooks.setPlayerShadowsEnabled(enabled);
    }

    static void setHeldTorchLightsEnabled(boolean enabled) {
        McrtxRuntimeSettings.setHeldTorchLightsEnabled(enabled);
        RemixDynamicEntityCapture.setHeldTorchLightsEnabled(enabled);
        MinecraftRenderHooks.setHeldTorchLightsEnabled(enabled);
    }

    static void setDynamicEntityRenderingEnabled(boolean enabled) {
        McrtxRuntimeSettings.setDynamicEntityRenderingEnabled(enabled);
        RemixDynamicEntityCapture.setDynamicEntityRenderingEnabled(enabled);
        MinecraftRenderHooks.setDynamicEntityRenderingEnabled(enabled);
    }

    static void setLivingEntityRenderingEnabled(boolean enabled) {
        McrtxRuntimeSettings.setLivingEntityRenderingEnabled(enabled);
        RemixDynamicEntityCapture.setLivingEntityRenderingEnabled(enabled);
    }

    static void setItemEntityRenderingEnabled(boolean enabled) {
        McrtxRuntimeSettings.setItemEntityRenderingEnabled(enabled);
        RemixDynamicEntityCapture.setItemEntityRenderingEnabled(enabled);
    }

    static void setPaintingVanillaSuppressionEnabled(boolean enabled) {
        McrtxRuntimeSettings.setPaintingVanillaSuppressionEnabled(enabled);
    }

    static void setMovingPistonVanillaSuppressionEnabled(boolean enabled) {
        McrtxRuntimeSettings.setMovingPistonVanillaSuppressionEnabled(enabled);
    }

    static void setWorldRasterVanillaSuppressionEnabled(boolean enabled) {
        McrtxRuntimeSettings.setWorldRasterVanillaSuppressionEnabled(enabled);
    }

    static void setSignCaptureEnabled(boolean enabled) {
        McrtxRuntimeSettings.setSignCaptureEnabled(enabled);
        RemixDynamicEntityCapture.setSignCaptureEnabled(enabled);
    }

    static void setSignTextCaptureEnabled(boolean enabled) {
        McrtxRuntimeSettings.setSignTextCaptureEnabled(enabled);
        RemixDynamicEntityCapture.setSignTextCaptureEnabled(enabled);
    }

    static void setSignVanillaSuppressionEnabled(boolean enabled) {
        McrtxRuntimeSettings.setSignVanillaSuppressionEnabled(enabled);
    }

    static void setRemixAtmosphereCloudsEnabled(boolean enabled) {
        boolean previousEnabled = McrtxRuntimeSettings.isRemixAtmosphereCloudsEnabled();
        McrtxRuntimeSettings.setRemixAtmosphereCloudsEnabled(enabled);
        if (McrtxCloudMode.shouldClearGameCloudLayerAfterToggle(previousEnabled, enabled)) {
            MinecraftRenderHooks.clearCloudLayer();
        }
        MinecraftRenderHooks.setRemixAtmosphereCloudsEnabled(enabled);
    }

    static void setGameRainParticlesEnabled(boolean enabled) {
        McrtxRuntimeSettings.setGameRainParticlesEnabled(enabled);
    }

    static void setGameplayFovDegrees(int fovDegrees) {
        McrtxRuntimeSettings.setGameplayFovDegrees(fovDegrees);
    }

    static void setViewModelFovDegrees(int fovDegrees) {
        McrtxRuntimeSettings.setViewModelFovDegrees(fovDegrees);
        MinecraftRenderHooks.setViewModelFovDegrees(fovDegrees);
    }

    static void setNoCullDistanceBlocks(int blockDistance) {
        McrtxRuntimeSettings.setNoCullDistanceBlocks(blockDistance);
        RemixCameraState.setNoCullDistanceBlocks(blockDistance);
    }

    static void setBlockOutlineEnabled(boolean enabled) {
        McrtxRuntimeSettings.setBlockOutlineEnabled(enabled);
        MinecraftRenderHooks.setBlockOutlineEnabled(enabled);
    }

    static void setBlockOutlineEmissiveIntensityTenths(int intensityTenths) {
        McrtxRuntimeSettings.setBlockOutlineEmissiveIntensityTenths(intensityTenths);
        MinecraftRenderHooks.setBlockOutlineEmissiveIntensity(McrtxRuntimeSettings.getBlockOutlineEmissiveIntensity());
    }

    static void setDisplacementFactorHundredths(int factorHundredths) {
        McrtxRuntimeSettings.setDisplacementFactorHundredths(factorHundredths);
        MinecraftRenderHooks.setDisplacementFactor(McrtxRuntimeSettings.getDisplacementFactor());
    }

    static void setSubsurfaceMeasurementDistanceHundredths(int distanceHundredths) {
        McrtxRuntimeSettings.setSubsurfaceMeasurementDistanceHundredths(distanceHundredths);
        MinecraftRenderHooks.setSubsurfaceMeasurementDistance(McrtxRuntimeSettings.getSubsurfaceMeasurementDistance());
    }

    static void setSubsurfaceRadiusScaleHundredths(int scaleHundredths) {
        McrtxRuntimeSettings.setSubsurfaceRadiusScaleHundredths(scaleHundredths);
        MinecraftRenderHooks.setSubsurfaceRadiusScale(McrtxRuntimeSettings.getSubsurfaceRadiusScale());
    }

    static void setSubsurfaceMaxSampleRadiusHundredths(int radiusHundredths) {
        McrtxRuntimeSettings.setSubsurfaceMaxSampleRadiusHundredths(radiusHundredths);
        MinecraftRenderHooks.setSubsurfaceMaxSampleRadius(McrtxRuntimeSettings.getSubsurfaceMaxSampleRadius());
    }

    static void setSubsurfaceVolumetricAnisotropyHundredths(int anisotropyHundredths) {
        McrtxRuntimeSettings.setSubsurfaceVolumetricAnisotropyHundredths(anisotropyHundredths);
        MinecraftRenderHooks.setSubsurfaceVolumetricAnisotropy(McrtxRuntimeSettings.getSubsurfaceVolumetricAnisotropy());
    }

    static void setSubsurfaceDiffusionProfileEnabled(boolean enabled) {
        McrtxRuntimeSettings.setSubsurfaceDiffusionProfileEnabled(enabled);
        MinecraftRenderHooks.setSubsurfaceDiffusionProfileEnabled(enabled);
    }

    static void setWaterThinWalledEnabled(boolean enabled) {
        McrtxRuntimeSettings.setWaterThinWalledEnabled(enabled);
        MinecraftRenderHooks.setWaterThinWalledEnabled(enabled);
    }

    static void setWaterMaterialThicknessThousandths(int thicknessThousandths) {
        McrtxRuntimeSettings.setWaterMaterialThicknessThousandths(thicknessThousandths);
        MinecraftRenderHooks.setWaterMaterialThickness(McrtxRuntimeSettings.getWaterMaterialThickness());
    }

    static void resetSubsurfaceSettingsToDefaults() {
        setSubsurfaceMeasurementDistanceHundredths(McrtxRuntimeSettings.DEFAULT_SUBSURFACE_MEASUREMENT_DISTANCE_HUNDREDTHS);
        setSubsurfaceRadiusScaleHundredths(McrtxRuntimeSettings.DEFAULT_SUBSURFACE_RADIUS_SCALE_HUNDREDTHS);
        setSubsurfaceMaxSampleRadiusHundredths(McrtxRuntimeSettings.DEFAULT_SUBSURFACE_MAX_SAMPLE_RADIUS_HUNDREDTHS);
        setSubsurfaceVolumetricAnisotropyHundredths(McrtxRuntimeSettings.DEFAULT_SUBSURFACE_VOLUMETRIC_ANISOTROPY_HUNDREDTHS);
        setSubsurfaceDiffusionProfileEnabled(McrtxRuntimeSettings.DEFAULT_SUBSURFACE_DIFFUSION_PROFILE_ENABLED);
        setWaterThinWalledEnabled(McrtxRuntimeSettings.DEFAULT_WATER_THIN_WALLED_ENABLED);
        setWaterMaterialThicknessThousandths(McrtxRuntimeSettings.DEFAULT_WATER_MATERIAL_THICKNESS_THOUSANDTHS);
    }

    static void cycleBlockOutlineStyle() {
        int style = McrtxRuntimeSettings.getBlockOutlineStyle();
        switch (style) {
            case McrtxRuntimeSettings.BLOCK_OUTLINE_STYLE_SUBTLE:
                McrtxRuntimeSettings.setBlockOutlineStyle(McrtxRuntimeSettings.BLOCK_OUTLINE_STYLE_THIN);
                break;
            case McrtxRuntimeSettings.BLOCK_OUTLINE_STYLE_THIN:
                McrtxRuntimeSettings.setBlockOutlineStyle(McrtxRuntimeSettings.BLOCK_OUTLINE_STYLE_GLOW);
                break;
            case McrtxRuntimeSettings.BLOCK_OUTLINE_STYLE_GLOW:
                McrtxRuntimeSettings.setBlockOutlineStyle(McrtxRuntimeSettings.BLOCK_OUTLINE_STYLE_RGB);
                break;
            case McrtxRuntimeSettings.BLOCK_OUTLINE_STYLE_RGB:
                McrtxRuntimeSettings.setBlockOutlineStyle(McrtxRuntimeSettings.BLOCK_OUTLINE_STYLE_BOLD);
                break;
            case McrtxRuntimeSettings.BLOCK_OUTLINE_STYLE_BOLD:
                McrtxRuntimeSettings.setBlockOutlineStyle(McrtxRuntimeSettings.BLOCK_OUTLINE_STYLE_SOLID);
                break;
            case McrtxRuntimeSettings.BLOCK_OUTLINE_STYLE_SOLID:
            default:
                McrtxRuntimeSettings.setBlockOutlineStyle(McrtxRuntimeSettings.BLOCK_OUTLINE_STYLE_SUBTLE);
                break;
        }
        MinecraftRenderHooks.setBlockOutlineStyle(McrtxRuntimeSettings.getBlockOutlineStyle());
    }

    static void cycleRtQuality() {
        int rtQuality = McrtxRuntimeSettings.getRtQuality();
        switch (rtQuality) {
            case McrtxRuntimeSettings.RT_QUALITY_LOW:
                McrtxRuntimeSettings.setRtQuality(McrtxRuntimeSettings.RT_QUALITY_MEDIUM);
                break;
            case McrtxRuntimeSettings.RT_QUALITY_MEDIUM:
                McrtxRuntimeSettings.setRtQuality(McrtxRuntimeSettings.RT_QUALITY_HIGH);
                break;
            case McrtxRuntimeSettings.RT_QUALITY_HIGH:
                McrtxRuntimeSettings.setRtQuality(McrtxRuntimeSettings.RT_QUALITY_ULTRA);
                break;
            case McrtxRuntimeSettings.RT_QUALITY_ULTRA:
                McrtxRuntimeSettings.setRtQuality(McrtxRuntimeSettings.RT_QUALITY_POTATO);
                break;
            case McrtxRuntimeSettings.RT_QUALITY_POTATO:
            default:
                McrtxRuntimeSettings.setRtQuality(McrtxRuntimeSettings.RT_QUALITY_LOW);
                break;
        }
        applyRtQualitySettings();
    }

    static void cycleUpscalerType() {
        int upscalerType = McrtxRuntimeSettings.getUpscalerType();
        switch (upscalerType) {
            case McrtxRuntimeSettings.UPSCALER_TYPE_NONE:
                McrtxRuntimeSettings.setUpscalerType(McrtxRuntimeSettings.UPSCALER_TYPE_DLSS);
                break;
            case McrtxRuntimeSettings.UPSCALER_TYPE_DLSS:
                McrtxRuntimeSettings.setUpscalerType(McrtxRuntimeSettings.UPSCALER_TYPE_XESS);
                break;
            case McrtxRuntimeSettings.UPSCALER_TYPE_XESS:
                McrtxRuntimeSettings.setUpscalerType(McrtxRuntimeSettings.UPSCALER_TYPE_TAAU);
                break;
            case McrtxRuntimeSettings.UPSCALER_TYPE_TAAU:
            default:
                McrtxRuntimeSettings.setUpscalerType(McrtxRuntimeSettings.UPSCALER_TYPE_NONE);
                break;
        }
        applyUpscalerSettings();
    }

    static void cycleUpscalerPreset() {
        int upscalerType = McrtxRuntimeSettings.getUpscalerType();
        switch (upscalerType) {
            case McrtxRuntimeSettings.UPSCALER_TYPE_DLSS:
                cycleDlssPreset();
                return;
            case McrtxRuntimeSettings.UPSCALER_TYPE_XESS:
                cycleXessPreset();
                return;
            case McrtxRuntimeSettings.UPSCALER_TYPE_TAAU:
                cycleTaauPreset();
                return;
            case McrtxRuntimeSettings.UPSCALER_TYPE_NONE:
            default:
                return;
        }
    }

    static boolean shouldShowRayReconstructionOption() {
        return McrtxRuntimeSettings.getUpscalerType() == McrtxRuntimeSettings.UPSCALER_TYPE_DLSS;
    }

    static void toggleRayReconstructionEnabled() {
        if (!shouldShowRayReconstructionOption()) {
            return;
        }
        McrtxRuntimeSettings.setRayReconstructionEnabled(!McrtxRuntimeSettings.isRayReconstructionEnabled());
        applyUpscalerSettings();
    }

    static void applySavedMcrtxSettings() {
        boolean playerShadowsEnabled = McrtxRuntimeSettings.isPlayerShadowsEnabled();
        boolean heldTorchLightsEnabled = McrtxRuntimeSettings.isHeldTorchLightsEnabled();
        boolean dynamicEntityRenderingEnabled = McrtxRuntimeSettings.isDynamicEntityRenderingEnabled();
        boolean livingEntityRenderingEnabled = McrtxRuntimeSettings.isLivingEntityRenderingEnabled();
        boolean itemEntityRenderingEnabled = McrtxRuntimeSettings.isItemEntityRenderingEnabled();
        boolean signCaptureEnabled = McrtxRuntimeSettings.isSignCaptureEnabled();
        boolean signTextCaptureEnabled = McrtxRuntimeSettings.isSignTextCaptureEnabled();
        RemixDynamicEntityCapture.setPlayerShadowsEnabled(playerShadowsEnabled);
        RemixDynamicEntityCapture.setHeldTorchLightsEnabled(heldTorchLightsEnabled);
        RemixDynamicEntityCapture.setDynamicEntityRenderingEnabled(dynamicEntityRenderingEnabled);
        RemixDynamicEntityCapture.setLivingEntityRenderingEnabled(livingEntityRenderingEnabled);
        RemixDynamicEntityCapture.setItemEntityRenderingEnabled(itemEntityRenderingEnabled);
        RemixDynamicEntityCapture.setSignCaptureEnabled(signCaptureEnabled);
        RemixDynamicEntityCapture.setSignTextCaptureEnabled(signTextCaptureEnabled);
        MinecraftRenderHooks.setPlayerShadowsEnabled(playerShadowsEnabled);
        MinecraftRenderHooks.setHeldTorchLightsEnabled(heldTorchLightsEnabled);
        MinecraftRenderHooks.setDynamicEntityRenderingEnabled(dynamicEntityRenderingEnabled);
        MinecraftRenderHooks.setBlockOutlineEnabled(McrtxRuntimeSettings.isBlockOutlineEnabled());
        MinecraftRenderHooks.setBlockOutlineStyle(McrtxRuntimeSettings.getBlockOutlineStyle());
        MinecraftRenderHooks.setBlockOutlineEmissiveIntensity(McrtxRuntimeSettings.getBlockOutlineEmissiveIntensity());
        MinecraftRenderHooks.setDisplacementFactor(McrtxRuntimeSettings.getDisplacementFactor());
        MinecraftRenderHooks.setSubsurfaceMeasurementDistance(McrtxRuntimeSettings.getSubsurfaceMeasurementDistance());
        MinecraftRenderHooks.setSubsurfaceRadiusScale(McrtxRuntimeSettings.getSubsurfaceRadiusScale());
        MinecraftRenderHooks.setSubsurfaceMaxSampleRadius(McrtxRuntimeSettings.getSubsurfaceMaxSampleRadius());
        MinecraftRenderHooks.setSubsurfaceVolumetricAnisotropy(McrtxRuntimeSettings.getSubsurfaceVolumetricAnisotropy());
        MinecraftRenderHooks.setSubsurfaceDiffusionProfileEnabled(McrtxRuntimeSettings.isSubsurfaceDiffusionProfileEnabled());
        MinecraftRenderHooks.setWaterThinWalledEnabled(McrtxRuntimeSettings.isWaterThinWalledEnabled());
        MinecraftRenderHooks.setWaterMaterialThickness(McrtxRuntimeSettings.getWaterMaterialThickness());
        MinecraftRenderHooks.setRemixAtmosphereCloudsEnabled(McrtxRuntimeSettings.isRemixAtmosphereCloudsEnabled());
        RemixCameraState.setNoCullDistanceBlocks(McrtxRuntimeSettings.getNoCullDistanceBlocks());
        MinecraftRenderHooks.setViewModelFovDegrees(McrtxRuntimeSettings.getViewModelFovDegrees());
        applyRtQualitySettings();
        applyUpscalerSettings();
    }

    static String formatToggleState(boolean enabled) {
        return enabled ? "ON" : "OFF";
    }

    private static String describeBlockOutlineStyle(int style) {
        switch (style) {
            case McrtxRuntimeSettings.BLOCK_OUTLINE_STYLE_SUBTLE:
                return "Subtle";
            case McrtxRuntimeSettings.BLOCK_OUTLINE_STYLE_THIN:
                return "Thin";
            case McrtxRuntimeSettings.BLOCK_OUTLINE_STYLE_GLOW:
                return "Glow";
            case McrtxRuntimeSettings.BLOCK_OUTLINE_STYLE_RGB:
                return "RGB";
            case McrtxRuntimeSettings.BLOCK_OUTLINE_STYLE_SOLID:
                return "Solid Fill";
            case McrtxRuntimeSettings.BLOCK_OUTLINE_STYLE_BOLD:
            default:
                return "Bold";
        }
    }

    private static boolean isBlockOutlineEmissiveStyle(int style) {
        return style == McrtxRuntimeSettings.BLOCK_OUTLINE_STYLE_GLOW
                || style == McrtxRuntimeSettings.BLOCK_OUTLINE_STYLE_RGB;
    }

    private static void cycleDlssPreset() {
        int preset = McrtxRuntimeSettings.getDlssPreset();
        switch (preset) {
            case McrtxRuntimeSettings.DLSS_PRESET_AUTO:
                McrtxRuntimeSettings.setDlssPreset(McrtxRuntimeSettings.DLSS_PRESET_QUALITY);
                break;
            case McrtxRuntimeSettings.DLSS_PRESET_QUALITY:
                McrtxRuntimeSettings.setDlssPreset(McrtxRuntimeSettings.DLSS_PRESET_BALANCED);
                break;
            case McrtxRuntimeSettings.DLSS_PRESET_BALANCED:
                McrtxRuntimeSettings.setDlssPreset(McrtxRuntimeSettings.DLSS_PRESET_PERFORMANCE);
                break;
            case McrtxRuntimeSettings.DLSS_PRESET_PERFORMANCE:
                McrtxRuntimeSettings.setDlssPreset(McrtxRuntimeSettings.DLSS_PRESET_ULTRA_PERFORMANCE);
                break;
            case McrtxRuntimeSettings.DLSS_PRESET_ULTRA_PERFORMANCE:
                McrtxRuntimeSettings.setDlssPreset(McrtxRuntimeSettings.DLSS_PRESET_DLAA);
                break;
            case McrtxRuntimeSettings.DLSS_PRESET_DLAA:
            default:
                McrtxRuntimeSettings.setDlssPreset(McrtxRuntimeSettings.DLSS_PRESET_AUTO);
                break;
        }
        applyUpscalerSettings();
    }

    private static void cycleXessPreset() {
        int preset = McrtxRuntimeSettings.getXessPreset();
        switch (preset) {
            case McrtxRuntimeSettings.XESS_PRESET_ULTRA_PERFORMANCE:
                McrtxRuntimeSettings.setXessPreset(McrtxRuntimeSettings.XESS_PRESET_PERFORMANCE);
                break;
            case McrtxRuntimeSettings.XESS_PRESET_PERFORMANCE:
                McrtxRuntimeSettings.setXessPreset(McrtxRuntimeSettings.XESS_PRESET_BALANCED);
                break;
            case McrtxRuntimeSettings.XESS_PRESET_BALANCED:
                McrtxRuntimeSettings.setXessPreset(McrtxRuntimeSettings.XESS_PRESET_QUALITY);
                break;
            case McrtxRuntimeSettings.XESS_PRESET_QUALITY:
                McrtxRuntimeSettings.setXessPreset(McrtxRuntimeSettings.XESS_PRESET_ULTRA_QUALITY);
                break;
            case McrtxRuntimeSettings.XESS_PRESET_ULTRA_QUALITY:
                McrtxRuntimeSettings.setXessPreset(McrtxRuntimeSettings.XESS_PRESET_ULTRA_QUALITY_PLUS);
                break;
            case McrtxRuntimeSettings.XESS_PRESET_ULTRA_QUALITY_PLUS:
                McrtxRuntimeSettings.setXessPreset(McrtxRuntimeSettings.XESS_PRESET_NATIVE_AA);
                break;
            case McrtxRuntimeSettings.XESS_PRESET_NATIVE_AA:
            default:
                McrtxRuntimeSettings.setXessPreset(McrtxRuntimeSettings.XESS_PRESET_ULTRA_PERFORMANCE);
                break;
        }
        applyUpscalerSettings();
    }

    private static void cycleTaauPreset() {
        int preset = McrtxRuntimeSettings.getTaauPreset();
        switch (preset) {
            case McrtxRuntimeSettings.TAAU_PRESET_ULTRA_PERFORMANCE:
                McrtxRuntimeSettings.setTaauPreset(McrtxRuntimeSettings.TAAU_PRESET_PERFORMANCE);
                break;
            case McrtxRuntimeSettings.TAAU_PRESET_PERFORMANCE:
                McrtxRuntimeSettings.setTaauPreset(McrtxRuntimeSettings.TAAU_PRESET_BALANCED);
                break;
            case McrtxRuntimeSettings.TAAU_PRESET_BALANCED:
                McrtxRuntimeSettings.setTaauPreset(McrtxRuntimeSettings.TAAU_PRESET_QUALITY);
                break;
            case McrtxRuntimeSettings.TAAU_PRESET_QUALITY:
                McrtxRuntimeSettings.setTaauPreset(McrtxRuntimeSettings.TAAU_PRESET_FULLSCREEN);
                break;
            case McrtxRuntimeSettings.TAAU_PRESET_FULLSCREEN:
            default:
                McrtxRuntimeSettings.setTaauPreset(McrtxRuntimeSettings.TAAU_PRESET_ULTRA_PERFORMANCE);
                break;
        }
        applyUpscalerSettings();
    }

    private static void applyRtQualitySettings() {
        MinecraftRenderHooks.setRtQuality(McrtxRuntimeSettings.getRtQuality());
    }

    private static void applyUpscalerSettings() {
        MinecraftRenderHooks.setUpscalerConfig(
                McrtxRuntimeSettings.getUpscalerType(),
                McrtxRuntimeSettings.getDlssPreset(),
                McrtxRuntimeSettings.getXessPreset(),
                McrtxRuntimeSettings.getTaauPreset(),
                McrtxRuntimeSettings.isRayReconstructionEnabled());
    }

    private static String describeRtQuality(int rtQuality) {
        switch (rtQuality) {
            case McrtxRuntimeSettings.RT_QUALITY_POTATO:
                return "Potato";
            case McrtxRuntimeSettings.RT_QUALITY_LOW:
                return "Low";
            case McrtxRuntimeSettings.RT_QUALITY_MEDIUM:
                return "Medium";
            case McrtxRuntimeSettings.RT_QUALITY_ULTRA:
                return "Ultra";
            case McrtxRuntimeSettings.RT_QUALITY_HIGH:
            default:
                return "High";
        }
    }

    private static String describeUpscalerType(int upscalerType) {
        switch (upscalerType) {
            case McrtxRuntimeSettings.UPSCALER_TYPE_NONE:
                return "None";
            case McrtxRuntimeSettings.UPSCALER_TYPE_XESS:
                return "XeSS";
            case McrtxRuntimeSettings.UPSCALER_TYPE_TAAU:
                return "TAAU";
            case McrtxRuntimeSettings.UPSCALER_TYPE_DLSS:
            default:
                return "DLSS";
        }
    }

    private static String describeDlssPreset(int preset) {
        switch (preset) {
            case McrtxRuntimeSettings.DLSS_PRESET_QUALITY:
                return "Quality";
            case McrtxRuntimeSettings.DLSS_PRESET_BALANCED:
                return "Balanced";
            case McrtxRuntimeSettings.DLSS_PRESET_PERFORMANCE:
                return "Performance";
            case McrtxRuntimeSettings.DLSS_PRESET_ULTRA_PERFORMANCE:
                return "Ultra Performance";
            case McrtxRuntimeSettings.DLSS_PRESET_DLAA:
                return "DLAA";
            case McrtxRuntimeSettings.DLSS_PRESET_AUTO:
            default:
                return "Auto";
        }
    }

    private static String describeXessPreset(int preset) {
        switch (preset) {
            case McrtxRuntimeSettings.XESS_PRESET_ULTRA_PERFORMANCE:
                return "Ultra Performance";
            case McrtxRuntimeSettings.XESS_PRESET_PERFORMANCE:
                return "Performance";
            case McrtxRuntimeSettings.XESS_PRESET_QUALITY:
                return "Quality";
            case McrtxRuntimeSettings.XESS_PRESET_ULTRA_QUALITY:
                return "Ultra Quality";
            case McrtxRuntimeSettings.XESS_PRESET_ULTRA_QUALITY_PLUS:
                return "Ultra Quality Plus";
            case McrtxRuntimeSettings.XESS_PRESET_NATIVE_AA:
                return "Native AA";
            case McrtxRuntimeSettings.XESS_PRESET_BALANCED:
            default:
                return "Balanced";
        }
    }

    private static String describeTaauPreset(int preset) {
        switch (preset) {
            case McrtxRuntimeSettings.TAAU_PRESET_ULTRA_PERFORMANCE:
                return "Ultra Performance";
            case McrtxRuntimeSettings.TAAU_PRESET_PERFORMANCE:
                return "Performance";
            case McrtxRuntimeSettings.TAAU_PRESET_QUALITY:
                return "Quality";
            case McrtxRuntimeSettings.TAAU_PRESET_FULLSCREEN:
                return "Fullscreen";
            case McrtxRuntimeSettings.TAAU_PRESET_BALANCED:
            default:
                return "Balanced";
        }
    }
}
