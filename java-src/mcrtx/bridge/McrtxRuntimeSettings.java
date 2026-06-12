package mcrtx.bridge;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

public final class McrtxRuntimeSettings {
    public static final String PLAYER_SHADOWS_ENABLED_KEY = "MCRTX_PLAYER_SHADOWS_ENABLED";
    public static final String HELD_TORCH_LIGHTS_ENABLED_KEY = "MCRTX_HELD_TORCH_LIGHTS_ENABLED";
    public static final String DYNAMIC_ENTITY_RENDERING_ENABLED_KEY = "MCRTX_DYNAMIC_ENTITY_RENDERING_ENABLED";
    public static final String LIVING_ENTITY_RENDERING_ENABLED_KEY = "MCRTX_LIVING_ENTITY_RENDERING_ENABLED";
    public static final String ITEM_ENTITY_RENDERING_ENABLED_KEY = "MCRTX_ITEM_ENTITY_RENDERING_ENABLED";
    public static final String PAINTING_VANILLA_SUPPRESSION_ENABLED_KEY = "MCRTX_PAINTING_VANILLA_SUPPRESSION_ENABLED";
    public static final String MOVING_PISTON_VANILLA_SUPPRESSION_ENABLED_KEY = "MCRTX_MOVING_PISTON_VANILLA_SUPPRESSION_ENABLED";
    public static final String WORLD_RASTER_VANILLA_SUPPRESSION_ENABLED_KEY = "MCRTX_WORLD_RASTER_VANILLA_SUPPRESSION_ENABLED";
    public static final String SIGN_CAPTURE_ENABLED_KEY = "MCRTX_SIGN_CAPTURE_ENABLED";
    public static final String SIGN_TEXT_CAPTURE_ENABLED_KEY = "MCRTX_SIGN_TEXT_CAPTURE_ENABLED";
    public static final String SIGN_VANILLA_SUPPRESSION_ENABLED_KEY = "MCRTX_SIGN_VANILLA_SUPPRESSION_ENABLED";
    public static final String GAMEPLAY_FOV_KEY = "MCRTX_GAMEPLAY_FOV";
    public static final String VIEW_MODEL_FOV_KEY = "MCRTX_VIEWMODEL_FOV";
    public static final String NO_CULL_DISTANCE_KEY = "MCRTX_NO_CULL_DISTANCE";
    public static final String UPSCALER_TYPE_KEY = "MCRTX_UPSCALER_TYPE";
    public static final String DLSS_PRESET_KEY = "MCRTX_DLSS_PRESET";
    public static final String XESS_PRESET_KEY = "MCRTX_XESS_PRESET";
    public static final String TAAU_PRESET_KEY = "MCRTX_TAAU_PRESET";
    public static final String RAY_RECONSTRUCTION_ENABLED_KEY = "MCRTX_RAY_RECONSTRUCTION_ENABLED";
    public static final String RT_QUALITY_KEY = "MCRTX_RT_QUALITY";
    public static final String BLOCK_OUTLINE_ENABLED_KEY = "MCRTX_BLOCK_OUTLINE_ENABLED";
    public static final String BLOCK_OUTLINE_STYLE_KEY = "MCRTX_BLOCK_OUTLINE_STYLE";
    public static final String BLOCK_OUTLINE_EMISSIVE_INTENSITY_KEY = "MCRTX_BLOCK_OUTLINE_EMISSIVE_INTENSITY";
    public static final String DISPLACEMENT_FACTOR_KEY = "MCRTX_DISPLACEMENT_FACTOR";
    public static final String SUBSURFACE_MEASUREMENT_DISTANCE_KEY = "MCRTX_SUBSURFACE_MEASUREMENT_DISTANCE";
    public static final String SUBSURFACE_RADIUS_SCALE_KEY = "MCRTX_SUBSURFACE_RADIUS_SCALE";
    public static final String SUBSURFACE_MAX_SAMPLE_RADIUS_KEY = "MCRTX_SUBSURFACE_MAX_SAMPLE_RADIUS";
    public static final String SUBSURFACE_VOLUMETRIC_ANISOTROPY_KEY = "MCRTX_SUBSURFACE_VOLUMETRIC_ANISOTROPY";
    public static final String SUBSURFACE_DIFFUSION_PROFILE_ENABLED_KEY = "MCRTX_SUBSURFACE_DIFFUSION_PROFILE_ENABLED";
    public static final String QUICK_SETTINGS_CATEGORY_KEY = "MCRTX_QUICK_SETTINGS_CATEGORY";
    public static final String WATER_THIN_WALLED_ENABLED_KEY = "MCRTX_WATER_THIN_WALLED_ENABLED";
    public static final String WATER_MATERIAL_THICKNESS_KEY = "MCRTX_WATER_MATERIAL_THICKNESS";
    private static final String WATER_MATERIAL_THICKNESS_MIGRATION_KEY = "MCRTX_WATER_MATERIAL_THICKNESS_MIGRATED";

    public static final int MIN_GAMEPLAY_FOV_DEGREES = 30;
    public static final int MAX_GAMEPLAY_FOV_DEGREES = 120;
    public static final int DEFAULT_GAMEPLAY_FOV_DEGREES = 70;
    public static final int MIN_VIEW_MODEL_FOV_DEGREES = 30;
    public static final int MAX_VIEW_MODEL_FOV_DEGREES = 120;
    public static final int DEFAULT_VIEW_MODEL_FOV_DEGREES = 70;
    public static final int MIN_NO_CULL_DISTANCE_BLOCKS = 0;
    public static final int MAX_NO_CULL_DISTANCE_BLOCKS = 200;
    public static final int DEFAULT_NO_CULL_DISTANCE_BLOCKS = 200;
    public static final int MIN_BLOCK_OUTLINE_EMISSIVE_INTENSITY_TENTHS = 0;
    public static final int MAX_BLOCK_OUTLINE_EMISSIVE_INTENSITY_TENTHS = 100;
    public static final int DEFAULT_BLOCK_OUTLINE_EMISSIVE_INTENSITY_TENTHS = 45;
    public static final int MIN_DISPLACEMENT_FACTOR_HUNDREDTHS = 0;
    public static final int MAX_DISPLACEMENT_FACTOR_HUNDREDTHS = 400;
    public static final int DEFAULT_DISPLACEMENT_FACTOR_HUNDREDTHS = 100;
    public static final int MIN_SUBSURFACE_MEASUREMENT_DISTANCE_HUNDREDTHS = 0;
    public static final int MAX_SUBSURFACE_MEASUREMENT_DISTANCE_HUNDREDTHS = 2500;
    public static final int DEFAULT_SUBSURFACE_MEASUREMENT_DISTANCE_HUNDREDTHS = 100;
    public static final int MIN_SUBSURFACE_RADIUS_SCALE_HUNDREDTHS = 0;
    public static final int MAX_SUBSURFACE_RADIUS_SCALE_HUNDREDTHS = 2500;
    public static final int DEFAULT_SUBSURFACE_RADIUS_SCALE_HUNDREDTHS = 100;
    public static final int MIN_SUBSURFACE_MAX_SAMPLE_RADIUS_HUNDREDTHS = 0;
    public static final int MAX_SUBSURFACE_MAX_SAMPLE_RADIUS_HUNDREDTHS = 25600;
    public static final int DEFAULT_SUBSURFACE_MAX_SAMPLE_RADIUS_HUNDREDTHS = 1600;
    public static final int MIN_SUBSURFACE_VOLUMETRIC_ANISOTROPY_HUNDREDTHS = -100;
    public static final int MAX_SUBSURFACE_VOLUMETRIC_ANISOTROPY_HUNDREDTHS = 99;
    public static final int DEFAULT_SUBSURFACE_VOLUMETRIC_ANISOTROPY_HUNDREDTHS = 0;
    public static final boolean DEFAULT_SUBSURFACE_DIFFUSION_PROFILE_ENABLED = true;
    public static final int QUICK_SETTINGS_CATEGORY_GAMEPLAY = 0;
    public static final int QUICK_SETTINGS_CATEGORY_GRAPHICS = 1;
    public static final int QUICK_SETTINGS_CATEGORY_DEBUG = 2;
    public static final int QUICK_SETTINGS_CATEGORY_MATERIAL = 3;
    public static final int DEFAULT_QUICK_SETTINGS_CATEGORY = QUICK_SETTINGS_CATEGORY_GAMEPLAY;
    public static final boolean DEFAULT_WATER_THIN_WALLED_ENABLED = true;
    public static final int MIN_WATER_MATERIAL_THICKNESS_THOUSANDTHS = 1;
    public static final int MAX_WATER_MATERIAL_THICKNESS_THOUSANDTHS = 5000;
    public static final int DEFAULT_WATER_MATERIAL_THICKNESS_THOUSANDTHS = 1000;
    private static final int LEGACY_DEFAULT_WATER_MATERIAL_THICKNESS_THOUSANDTHS = 1;

    public static final int BLOCK_OUTLINE_STYLE_SUBTLE = 0;
    public static final int BLOCK_OUTLINE_STYLE_BOLD = 1;
    public static final int BLOCK_OUTLINE_STYLE_SOLID = 2;
    public static final int BLOCK_OUTLINE_STYLE_GLOW = 3;
    public static final int BLOCK_OUTLINE_STYLE_RGB = 4;
    public static final int BLOCK_OUTLINE_STYLE_THIN = 5;

    public static final int UPSCALER_TYPE_NONE = 0;
    public static final int UPSCALER_TYPE_DLSS = 1;
    public static final int UPSCALER_TYPE_TAAU = 3;
    public static final int UPSCALER_TYPE_XESS = 4;

    public static final int DLSS_PRESET_AUTO = 4;
    public static final int DLSS_PRESET_QUALITY = 3;
    public static final int DLSS_PRESET_BALANCED = 2;
    public static final int DLSS_PRESET_PERFORMANCE = 1;
    public static final int DLSS_PRESET_ULTRA_PERFORMANCE = 0;
    public static final int DLSS_PRESET_DLAA = 5;

    public static final int XESS_PRESET_ULTRA_PERFORMANCE = 0;
    public static final int XESS_PRESET_PERFORMANCE = 1;
    public static final int XESS_PRESET_BALANCED = 2;
    public static final int XESS_PRESET_QUALITY = 3;
    public static final int XESS_PRESET_ULTRA_QUALITY = 4;
    public static final int XESS_PRESET_ULTRA_QUALITY_PLUS = 5;
    public static final int XESS_PRESET_NATIVE_AA = 6;

    public static final int TAAU_PRESET_ULTRA_PERFORMANCE = 0;
    public static final int TAAU_PRESET_PERFORMANCE = 1;
    public static final int TAAU_PRESET_BALANCED = 2;
    public static final int TAAU_PRESET_QUALITY = 3;
    public static final int TAAU_PRESET_FULLSCREEN = 4;

    public static final int RT_QUALITY_LOW = 0;
    public static final int RT_QUALITY_MEDIUM = 1;
    public static final int RT_QUALITY_HIGH = 2;
    public static final int RT_QUALITY_ULTRA = 3;
    public static final int RT_QUALITY_POTATO = 4;

    private static final Object LOCK = new Object();

    private static boolean loaded;
    private static boolean playerShadowsEnabled = true;
    private static boolean heldTorchLightsEnabled = true;
    private static boolean dynamicEntityRenderingEnabled = true;
    private static boolean livingEntityRenderingEnabled = true;
    private static boolean itemEntityRenderingEnabled = true;
    private static boolean paintingVanillaSuppressionEnabled;
    private static boolean movingPistonVanillaSuppressionEnabled;
    private static boolean worldRasterVanillaSuppressionEnabled;
    private static boolean signCaptureEnabled = true;
    private static boolean signTextCaptureEnabled = true;
    private static boolean signVanillaSuppressionEnabled;
    private static int gameplayFovDegrees = DEFAULT_GAMEPLAY_FOV_DEGREES;
    private static int viewModelFovDegrees = DEFAULT_VIEW_MODEL_FOV_DEGREES;
    private static int noCullDistanceBlocks = DEFAULT_NO_CULL_DISTANCE_BLOCKS;
    private static int upscalerType = UPSCALER_TYPE_DLSS;
    private static int dlssPreset = DLSS_PRESET_AUTO;
    private static int xessPreset = XESS_PRESET_BALANCED;
    private static int taauPreset = TAAU_PRESET_BALANCED;
    private static boolean rayReconstructionEnabled = true;
    private static int rtQuality = RT_QUALITY_HIGH;
    private static boolean blockOutlineEnabled = true;
    private static int blockOutlineStyle = BLOCK_OUTLINE_STYLE_BOLD;
    private static int blockOutlineEmissiveIntensityTenths = DEFAULT_BLOCK_OUTLINE_EMISSIVE_INTENSITY_TENTHS;
    private static int displacementFactorHundredths = DEFAULT_DISPLACEMENT_FACTOR_HUNDREDTHS;
    private static int subsurfaceMeasurementDistanceHundredths = DEFAULT_SUBSURFACE_MEASUREMENT_DISTANCE_HUNDREDTHS;
    private static int subsurfaceRadiusScaleHundredths = DEFAULT_SUBSURFACE_RADIUS_SCALE_HUNDREDTHS;
    private static int subsurfaceMaxSampleRadiusHundredths = DEFAULT_SUBSURFACE_MAX_SAMPLE_RADIUS_HUNDREDTHS;
    private static int subsurfaceVolumetricAnisotropyHundredths = DEFAULT_SUBSURFACE_VOLUMETRIC_ANISOTROPY_HUNDREDTHS;
    private static boolean subsurfaceDiffusionProfileEnabled = DEFAULT_SUBSURFACE_DIFFUSION_PROFILE_ENABLED;
    private static int quickSettingsCategory = DEFAULT_QUICK_SETTINGS_CATEGORY;
    private static boolean waterThinWalledEnabled = DEFAULT_WATER_THIN_WALLED_ENABLED;
    private static int waterMaterialThicknessThousandths = DEFAULT_WATER_MATERIAL_THICKNESS_THOUSANDTHS;

    private McrtxRuntimeSettings() {
    }

    public static boolean isPlayerShadowsEnabled() {
        synchronized (LOCK) {
            ensureLoaded();
            return playerShadowsEnabled;
        }
    }

    public static boolean isHeldTorchLightsEnabled() {
        synchronized (LOCK) {
            ensureLoaded();
            return heldTorchLightsEnabled;
        }
    }

    public static boolean isDynamicEntityRenderingEnabled() {
        synchronized (LOCK) {
            ensureLoaded();
            return dynamicEntityRenderingEnabled;
        }
    }

    public static boolean isLivingEntityRenderingEnabled() {
        synchronized (LOCK) {
            ensureLoaded();
            return livingEntityRenderingEnabled;
        }
    }

    public static boolean isItemEntityRenderingEnabled() {
        synchronized (LOCK) {
            ensureLoaded();
            return itemEntityRenderingEnabled;
        }
    }

    public static boolean isPaintingVanillaSuppressionEnabled() {
        synchronized (LOCK) {
            ensureLoaded();
            return paintingVanillaSuppressionEnabled;
        }
    }

    public static boolean isMovingPistonVanillaSuppressionEnabled() {
        synchronized (LOCK) {
            ensureLoaded();
            return movingPistonVanillaSuppressionEnabled;
        }
    }

    public static boolean isWorldRasterVanillaSuppressionEnabled() {
        synchronized (LOCK) {
            ensureLoaded();
            return worldRasterVanillaSuppressionEnabled;
        }
    }

    public static boolean isSignCaptureEnabled() {
        synchronized (LOCK) {
            ensureLoaded();
            return signCaptureEnabled;
        }
    }

    public static boolean isSignTextCaptureEnabled() {
        synchronized (LOCK) {
            ensureLoaded();
            return signTextCaptureEnabled;
        }
    }

    public static boolean isSignVanillaSuppressionEnabled() {
        synchronized (LOCK) {
            ensureLoaded();
            return signVanillaSuppressionEnabled;
        }
    }

    public static int getQuickSettingsCategory() {
        synchronized (LOCK) {
            ensureLoaded();
            return quickSettingsCategory;
        }
    }

    public static int getGameplayFovDegrees() {
        synchronized (LOCK) {
            ensureLoaded();
            return gameplayFovDegrees;
        }
    }

    public static int getViewModelFovDegrees() {
        synchronized (LOCK) {
            ensureLoaded();
            return viewModelFovDegrees;
        }
    }

    public static int getNoCullDistanceBlocks() {
        synchronized (LOCK) {
            ensureLoaded();
            return noCullDistanceBlocks;
        }
    }

    public static void setQuickSettingsCategory(int category) {
        synchronized (LOCK) {
            ensureLoaded();
            int normalizedCategory = normalizeQuickSettingsCategory(category);
            if (quickSettingsCategory == normalizedCategory) {
                return;
            }
            quickSettingsCategory = normalizedCategory;
            saveLocked();
        }
    }

    public static void setPlayerShadowsEnabled(boolean enabled) {
        synchronized (LOCK) {
            ensureLoaded();
            if (playerShadowsEnabled == enabled) {
                return;
            }
            playerShadowsEnabled = enabled;
            saveLocked();
        }
    }

    public static void setHeldTorchLightsEnabled(boolean enabled) {
        synchronized (LOCK) {
            ensureLoaded();
            if (heldTorchLightsEnabled == enabled) {
                return;
            }
            heldTorchLightsEnabled = enabled;
            saveLocked();
        }
    }

    public static void setDynamicEntityRenderingEnabled(boolean enabled) {
        synchronized (LOCK) {
            ensureLoaded();
            if (dynamicEntityRenderingEnabled == enabled) {
                return;
            }
            dynamicEntityRenderingEnabled = enabled;
            saveLocked();
        }
    }

    public static void setLivingEntityRenderingEnabled(boolean enabled) {
        synchronized (LOCK) {
            ensureLoaded();
            if (livingEntityRenderingEnabled == enabled) {
                return;
            }
            livingEntityRenderingEnabled = enabled;
            saveLocked();
        }
    }

    public static void setItemEntityRenderingEnabled(boolean enabled) {
        synchronized (LOCK) {
            ensureLoaded();
            if (itemEntityRenderingEnabled == enabled) {
                return;
            }
            itemEntityRenderingEnabled = enabled;
            saveLocked();
        }
    }

    public static void setPaintingVanillaSuppressionEnabled(boolean enabled) {
        synchronized (LOCK) {
            ensureLoaded();
            if (paintingVanillaSuppressionEnabled == enabled) {
                return;
            }
            paintingVanillaSuppressionEnabled = enabled;
            saveLocked();
        }
    }

    public static void setMovingPistonVanillaSuppressionEnabled(boolean enabled) {
        synchronized (LOCK) {
            ensureLoaded();
            if (movingPistonVanillaSuppressionEnabled == enabled) {
                return;
            }
            movingPistonVanillaSuppressionEnabled = enabled;
            saveLocked();
        }
    }

    public static void setWorldRasterVanillaSuppressionEnabled(boolean enabled) {
        synchronized (LOCK) {
            ensureLoaded();
            if (worldRasterVanillaSuppressionEnabled == enabled) {
                return;
            }
            worldRasterVanillaSuppressionEnabled = enabled;
            saveLocked();
        }
    }

    public static void setSignCaptureEnabled(boolean enabled) {
        synchronized (LOCK) {
            ensureLoaded();
            if (signCaptureEnabled == enabled) {
                return;
            }
            signCaptureEnabled = enabled;
            saveLocked();
        }
    }

    public static void setSignTextCaptureEnabled(boolean enabled) {
        synchronized (LOCK) {
            ensureLoaded();
            if (signTextCaptureEnabled == enabled) {
                return;
            }
            signTextCaptureEnabled = enabled;
            saveLocked();
        }
    }

    public static void setSignVanillaSuppressionEnabled(boolean enabled) {
        synchronized (LOCK) {
            ensureLoaded();
            if (signVanillaSuppressionEnabled == enabled) {
                return;
            }
            signVanillaSuppressionEnabled = enabled;
            saveLocked();
        }
    }

    public static void setGameplayFovDegrees(int fovDegrees) {
        synchronized (LOCK) {
            ensureLoaded();
            int normalizedFovDegrees = normalizeGameplayFovDegrees(fovDegrees);
            if (gameplayFovDegrees == normalizedFovDegrees) {
                return;
            }
            gameplayFovDegrees = normalizedFovDegrees;
            saveLocked();
        }
    }

    public static void setViewModelFovDegrees(int fovDegrees) {
        synchronized (LOCK) {
            ensureLoaded();
            int normalizedFovDegrees = normalizeViewModelFovDegrees(fovDegrees);
            if (viewModelFovDegrees == normalizedFovDegrees) {
                return;
            }
            viewModelFovDegrees = normalizedFovDegrees;
            saveLocked();
        }
    }

    public static void setNoCullDistanceBlocks(int blockDistance) {
        synchronized (LOCK) {
            ensureLoaded();
            int normalizedBlockDistance = normalizeNoCullDistanceBlocks(blockDistance);
            if (noCullDistanceBlocks == normalizedBlockDistance) {
                return;
            }
            noCullDistanceBlocks = normalizedBlockDistance;
            saveLocked();
        }
    }

    public static int getUpscalerType() {
        synchronized (LOCK) {
            ensureLoaded();
            return upscalerType;
        }
    }

    public static void setUpscalerType(int type) {
        synchronized (LOCK) {
            ensureLoaded();
            int normalizedType = normalizeUpscalerType(type);
            if (upscalerType == normalizedType) {
                return;
            }
            upscalerType = normalizedType;
            saveLocked();
        }
    }

    public static int getDlssPreset() {
        synchronized (LOCK) {
            ensureLoaded();
            return dlssPreset;
        }
    }

    public static void setDlssPreset(int preset) {
        synchronized (LOCK) {
            ensureLoaded();
            int normalizedPreset = normalizeDlssPreset(preset);
            if (dlssPreset == normalizedPreset) {
                return;
            }
            dlssPreset = normalizedPreset;
            saveLocked();
        }
    }

    public static int getXessPreset() {
        synchronized (LOCK) {
            ensureLoaded();
            return xessPreset;
        }
    }

    public static void setXessPreset(int preset) {
        synchronized (LOCK) {
            ensureLoaded();
            int normalizedPreset = normalizeXessPreset(preset);
            if (xessPreset == normalizedPreset) {
                return;
            }
            xessPreset = normalizedPreset;
            saveLocked();
        }
    }

    public static int getTaauPreset() {
        synchronized (LOCK) {
            ensureLoaded();
            return taauPreset;
        }
    }

    public static void setTaauPreset(int preset) {
        synchronized (LOCK) {
            ensureLoaded();
            int normalizedPreset = normalizeTaauPreset(preset);
            if (taauPreset == normalizedPreset) {
                return;
            }
            taauPreset = normalizedPreset;
            saveLocked();
        }
    }

    public static boolean isRayReconstructionEnabled() {
        synchronized (LOCK) {
            ensureLoaded();
            return rayReconstructionEnabled;
        }
    }

    public static int getRtQuality() {
        synchronized (LOCK) {
            ensureLoaded();
            return rtQuality;
        }
    }

    public static boolean isBlockOutlineEnabled() {
        synchronized (LOCK) {
            ensureLoaded();
            return blockOutlineEnabled;
        }
    }

    public static int getBlockOutlineStyle() {
        synchronized (LOCK) {
            ensureLoaded();
            return blockOutlineStyle;
        }
    }

    public static int getBlockOutlineEmissiveIntensityTenths() {
        synchronized (LOCK) {
            ensureLoaded();
            return blockOutlineEmissiveIntensityTenths;
        }
    }

    public static float getBlockOutlineEmissiveIntensity() {
        synchronized (LOCK) {
            ensureLoaded();
            return (float) blockOutlineEmissiveIntensityTenths / 10.0f;
        }
    }

    public static int getDisplacementFactorHundredths() {
        synchronized (LOCK) {
            ensureLoaded();
            return displacementFactorHundredths;
        }
    }

    public static float getDisplacementFactor() {
        synchronized (LOCK) {
            ensureLoaded();
            return (float) displacementFactorHundredths / 100.0f;
        }
    }

    public static int getSubsurfaceMeasurementDistanceHundredths() {
        synchronized (LOCK) {
            ensureLoaded();
            return subsurfaceMeasurementDistanceHundredths;
        }
    }

    public static float getSubsurfaceMeasurementDistance() {
        synchronized (LOCK) {
            ensureLoaded();
            return (float) subsurfaceMeasurementDistanceHundredths / 100.0f;
        }
    }

    public static int getSubsurfaceRadiusScaleHundredths() {
        synchronized (LOCK) {
            ensureLoaded();
            return subsurfaceRadiusScaleHundredths;
        }
    }

    public static float getSubsurfaceRadiusScale() {
        synchronized (LOCK) {
            ensureLoaded();
            return (float) subsurfaceRadiusScaleHundredths / 100.0f;
        }
    }

    public static int getSubsurfaceMaxSampleRadiusHundredths() {
        synchronized (LOCK) {
            ensureLoaded();
            return subsurfaceMaxSampleRadiusHundredths;
        }
    }

    public static float getSubsurfaceMaxSampleRadius() {
        synchronized (LOCK) {
            ensureLoaded();
            return (float) subsurfaceMaxSampleRadiusHundredths / 100.0f;
        }
    }

    public static int getSubsurfaceVolumetricAnisotropyHundredths() {
        synchronized (LOCK) {
            ensureLoaded();
            return subsurfaceVolumetricAnisotropyHundredths;
        }
    }

    public static float getSubsurfaceVolumetricAnisotropy() {
        synchronized (LOCK) {
            ensureLoaded();
            return (float) subsurfaceVolumetricAnisotropyHundredths / 100.0f;
        }
    }

    public static boolean isSubsurfaceDiffusionProfileEnabled() {
        synchronized (LOCK) {
            ensureLoaded();
            return subsurfaceDiffusionProfileEnabled;
        }
    }

    public static int getWaterMaterialThicknessThousandths() {
        synchronized (LOCK) {
            ensureLoaded();
            return waterMaterialThicknessThousandths;
        }
    }

    public static float getWaterMaterialThickness() {
        synchronized (LOCK) {
            ensureLoaded();
            return (float) waterMaterialThicknessThousandths / 1000.0f;
        }
    }

    public static boolean isWaterThinWalledEnabled() {
        synchronized (LOCK) {
            ensureLoaded();
            return waterThinWalledEnabled;
        }
    }

    public static void setRayReconstructionEnabled(boolean enabled) {
        synchronized (LOCK) {
            ensureLoaded();
            if (rayReconstructionEnabled == enabled) {
                return;
            }
            rayReconstructionEnabled = enabled;
            saveLocked();
        }
    }

    public static void setRtQuality(int quality) {
        synchronized (LOCK) {
            ensureLoaded();
            int normalizedQuality = normalizeRtQuality(quality);
            if (rtQuality == normalizedQuality) {
                return;
            }
            rtQuality = normalizedQuality;
            saveLocked();
        }
    }

    public static void setBlockOutlineEnabled(boolean enabled) {
        synchronized (LOCK) {
            ensureLoaded();
            if (blockOutlineEnabled == enabled) {
                return;
            }
            blockOutlineEnabled = enabled;
            saveLocked();
        }
    }

    public static void setBlockOutlineStyle(int style) {
        synchronized (LOCK) {
            ensureLoaded();
            int normalizedStyle = normalizeBlockOutlineStyle(style);
            if (blockOutlineStyle == normalizedStyle) {
                return;
            }
            blockOutlineStyle = normalizedStyle;
            saveLocked();
        }
    }

    public static void setBlockOutlineEmissiveIntensityTenths(int intensityTenths) {
        synchronized (LOCK) {
            ensureLoaded();
            int normalizedIntensityTenths = normalizeBlockOutlineEmissiveIntensityTenths(intensityTenths);
            if (blockOutlineEmissiveIntensityTenths == normalizedIntensityTenths) {
                return;
            }
            blockOutlineEmissiveIntensityTenths = normalizedIntensityTenths;
            saveLocked();
        }
    }

    public static void setDisplacementFactorHundredths(int factorHundredths) {
        synchronized (LOCK) {
            ensureLoaded();
            int normalizedFactorHundredths = normalizeDisplacementFactorHundredths(factorHundredths);
            if (displacementFactorHundredths == normalizedFactorHundredths) {
                return;
            }
            displacementFactorHundredths = normalizedFactorHundredths;
            saveLocked();
        }
    }

    public static void setSubsurfaceMeasurementDistanceHundredths(int distanceHundredths) {
        synchronized (LOCK) {
            ensureLoaded();
            int normalizedDistanceHundredths = normalizeSubsurfaceMeasurementDistanceHundredths(distanceHundredths);
            if (subsurfaceMeasurementDistanceHundredths == normalizedDistanceHundredths) {
                return;
            }
            subsurfaceMeasurementDistanceHundredths = normalizedDistanceHundredths;
            saveLocked();
        }
    }

    public static void setSubsurfaceRadiusScaleHundredths(int scaleHundredths) {
        synchronized (LOCK) {
            ensureLoaded();
            int normalizedScaleHundredths = normalizeSubsurfaceRadiusScaleHundredths(scaleHundredths);
            if (subsurfaceRadiusScaleHundredths == normalizedScaleHundredths) {
                return;
            }
            subsurfaceRadiusScaleHundredths = normalizedScaleHundredths;
            saveLocked();
        }
    }

    public static void setSubsurfaceMaxSampleRadiusHundredths(int radiusHundredths) {
        synchronized (LOCK) {
            ensureLoaded();
            int normalizedRadiusHundredths = normalizeSubsurfaceMaxSampleRadiusHundredths(radiusHundredths);
            if (subsurfaceMaxSampleRadiusHundredths == normalizedRadiusHundredths) {
                return;
            }
            subsurfaceMaxSampleRadiusHundredths = normalizedRadiusHundredths;
            saveLocked();
        }
    }

    public static void setSubsurfaceVolumetricAnisotropyHundredths(int anisotropyHundredths) {
        synchronized (LOCK) {
            ensureLoaded();
            int normalizedAnisotropyHundredths = normalizeSubsurfaceVolumetricAnisotropyHundredths(anisotropyHundredths);
            if (subsurfaceVolumetricAnisotropyHundredths == normalizedAnisotropyHundredths) {
                return;
            }
            subsurfaceVolumetricAnisotropyHundredths = normalizedAnisotropyHundredths;
            saveLocked();
        }
    }

    public static void setSubsurfaceDiffusionProfileEnabled(boolean enabled) {
        synchronized (LOCK) {
            ensureLoaded();
            if (subsurfaceDiffusionProfileEnabled == enabled) {
                return;
            }
            subsurfaceDiffusionProfileEnabled = enabled;
            saveLocked();
        }
    }

    public static void setWaterThinWalledEnabled(boolean enabled) {
        synchronized (LOCK) {
            ensureLoaded();
            if (waterThinWalledEnabled == enabled) {
                return;
            }
            waterThinWalledEnabled = enabled;
            saveLocked();
        }
    }

    public static void setWaterMaterialThicknessThousandths(int thicknessThousandths) {
        synchronized (LOCK) {
            ensureLoaded();
            int normalizedThicknessThousandths = normalizeWaterMaterialThicknessThousandths(thicknessThousandths);
            if (waterMaterialThicknessThousandths == normalizedThicknessThousandths) {
                return;
            }
            waterMaterialThicknessThousandths = normalizedThicknessThousandths;
            saveLocked();
        }
    }

    private static void ensureLoaded() {
        if (loaded) {
            return;
        }

        Map<String, String> fileValues = McrtxRuntimeConfig.loadFileValuesSnapshot();
        playerShadowsEnabled = readBooleanSetting(fileValues, PLAYER_SHADOWS_ENABLED_KEY, true);
        heldTorchLightsEnabled = readBooleanSetting(fileValues, HELD_TORCH_LIGHTS_ENABLED_KEY, true);
        dynamicEntityRenderingEnabled = readBooleanSetting(fileValues, DYNAMIC_ENTITY_RENDERING_ENABLED_KEY, true);
        livingEntityRenderingEnabled = readBooleanSetting(fileValues, LIVING_ENTITY_RENDERING_ENABLED_KEY, true);
        itemEntityRenderingEnabled = readBooleanSetting(fileValues, ITEM_ENTITY_RENDERING_ENABLED_KEY, true);
        paintingVanillaSuppressionEnabled = readBooleanSetting(fileValues, PAINTING_VANILLA_SUPPRESSION_ENABLED_KEY, false);
        movingPistonVanillaSuppressionEnabled = readBooleanSetting(fileValues, MOVING_PISTON_VANILLA_SUPPRESSION_ENABLED_KEY, false);
        worldRasterVanillaSuppressionEnabled = readBooleanSetting(fileValues, WORLD_RASTER_VANILLA_SUPPRESSION_ENABLED_KEY, false);
        signCaptureEnabled = readBooleanSetting(fileValues, SIGN_CAPTURE_ENABLED_KEY, true);
        signTextCaptureEnabled = readBooleanSetting(fileValues, SIGN_TEXT_CAPTURE_ENABLED_KEY, true);
        signVanillaSuppressionEnabled = readBooleanSetting(fileValues, SIGN_VANILLA_SUPPRESSION_ENABLED_KEY, false);
        gameplayFovDegrees = readGameplayFovSetting(fileValues, GAMEPLAY_FOV_KEY, DEFAULT_GAMEPLAY_FOV_DEGREES);
        viewModelFovDegrees = readViewModelFovSetting(fileValues, VIEW_MODEL_FOV_KEY, DEFAULT_VIEW_MODEL_FOV_DEGREES);
        noCullDistanceBlocks = readNoCullDistanceSetting(fileValues, NO_CULL_DISTANCE_KEY, DEFAULT_NO_CULL_DISTANCE_BLOCKS);
        upscalerType = readUpscalerTypeSetting(fileValues, UPSCALER_TYPE_KEY, deriveDefaultUpscalerType(fileValues));
        dlssPreset = readDlssPresetSetting(fileValues, DLSS_PRESET_KEY, DLSS_PRESET_AUTO);
        xessPreset = readXessPresetSetting(fileValues, XESS_PRESET_KEY, XESS_PRESET_BALANCED);
        taauPreset = readTaauPresetSetting(fileValues, TAAU_PRESET_KEY, TAAU_PRESET_BALANCED);
        rayReconstructionEnabled = readBooleanSetting(fileValues, RAY_RECONSTRUCTION_ENABLED_KEY, true);
        rtQuality = readRtQualitySetting(fileValues, RT_QUALITY_KEY, RT_QUALITY_HIGH);
        blockOutlineEnabled = readBooleanSetting(fileValues, BLOCK_OUTLINE_ENABLED_KEY, true);
        blockOutlineStyle = readBlockOutlineStyleSetting(fileValues, BLOCK_OUTLINE_STYLE_KEY, BLOCK_OUTLINE_STYLE_BOLD);
        blockOutlineEmissiveIntensityTenths = readBlockOutlineEmissiveIntensityTenthsSetting(
            fileValues,
            BLOCK_OUTLINE_EMISSIVE_INTENSITY_KEY,
            DEFAULT_BLOCK_OUTLINE_EMISSIVE_INTENSITY_TENTHS);
        displacementFactorHundredths = readDisplacementFactorHundredthsSetting(
            fileValues,
            DISPLACEMENT_FACTOR_KEY,
            DEFAULT_DISPLACEMENT_FACTOR_HUNDREDTHS);
        subsurfaceMeasurementDistanceHundredths = readPositiveHundredthsSetting(
            fileValues,
            SUBSURFACE_MEASUREMENT_DISTANCE_KEY,
            DEFAULT_SUBSURFACE_MEASUREMENT_DISTANCE_HUNDREDTHS,
            MIN_SUBSURFACE_MEASUREMENT_DISTANCE_HUNDREDTHS,
            MAX_SUBSURFACE_MEASUREMENT_DISTANCE_HUNDREDTHS);
        subsurfaceRadiusScaleHundredths = readPositiveHundredthsSetting(
            fileValues,
            SUBSURFACE_RADIUS_SCALE_KEY,
            DEFAULT_SUBSURFACE_RADIUS_SCALE_HUNDREDTHS,
            MIN_SUBSURFACE_RADIUS_SCALE_HUNDREDTHS,
            MAX_SUBSURFACE_RADIUS_SCALE_HUNDREDTHS);
        subsurfaceMaxSampleRadiusHundredths = readPositiveHundredthsSetting(
            fileValues,
            SUBSURFACE_MAX_SAMPLE_RADIUS_KEY,
            DEFAULT_SUBSURFACE_MAX_SAMPLE_RADIUS_HUNDREDTHS,
            MIN_SUBSURFACE_MAX_SAMPLE_RADIUS_HUNDREDTHS,
            MAX_SUBSURFACE_MAX_SAMPLE_RADIUS_HUNDREDTHS);
        subsurfaceVolumetricAnisotropyHundredths = readSignedHundredthsSetting(
            fileValues,
            SUBSURFACE_VOLUMETRIC_ANISOTROPY_KEY,
            DEFAULT_SUBSURFACE_VOLUMETRIC_ANISOTROPY_HUNDREDTHS,
            MIN_SUBSURFACE_VOLUMETRIC_ANISOTROPY_HUNDREDTHS,
            MAX_SUBSURFACE_VOLUMETRIC_ANISOTROPY_HUNDREDTHS);
        subsurfaceDiffusionProfileEnabled = readBooleanSetting(
            fileValues,
            SUBSURFACE_DIFFUSION_PROFILE_ENABLED_KEY,
            DEFAULT_SUBSURFACE_DIFFUSION_PROFILE_ENABLED);
        quickSettingsCategory = readQuickSettingsCategorySetting(
            fileValues,
            QUICK_SETTINGS_CATEGORY_KEY,
            DEFAULT_QUICK_SETTINGS_CATEGORY);
        waterThinWalledEnabled = readBooleanSetting(
            fileValues,
            WATER_THIN_WALLED_ENABLED_KEY,
            DEFAULT_WATER_THIN_WALLED_ENABLED);
        waterMaterialThicknessThousandths = readPositiveThousandthsSetting(
            fileValues,
            WATER_MATERIAL_THICKNESS_KEY,
            DEFAULT_WATER_MATERIAL_THICKNESS_THOUSANDTHS,
            MIN_WATER_MATERIAL_THICKNESS_THOUSANDTHS,
            MAX_WATER_MATERIAL_THICKNESS_THOUSANDTHS);
        final boolean migrateLegacyWaterMaterialThickness = !readBooleanSetting(fileValues, WATER_MATERIAL_THICKNESS_MIGRATION_KEY, false)
                && fileValues.containsKey(WATER_MATERIAL_THICKNESS_KEY)
                && waterMaterialThicknessThousandths == LEGACY_DEFAULT_WATER_MATERIAL_THICKNESS_THOUSANDTHS;
        if (migrateLegacyWaterMaterialThickness) {
            waterMaterialThicknessThousandths = DEFAULT_WATER_MATERIAL_THICKNESS_THOUSANDTHS;
        }
        loaded = true;
        if (migrateLegacyWaterMaterialThickness) {
            saveLocked();
        }
    }

    private static boolean readBooleanSetting(Map<String, String> fileValues, String key, boolean defaultValue) {
        String configuredValue = fileValues.get(key);
        if (configuredValue == null || configuredValue.isEmpty()) {
            String environmentValue = System.getenv(key);
            if (environmentValue != null && !environmentValue.isEmpty()) {
                configuredValue = environmentValue.trim();
            }
        }

        if (configuredValue == null || configuredValue.isEmpty()) {
            return defaultValue;
        }

        return McrtxRuntimeConfig.isTruthyValue(configuredValue);
    }

    private static int readDlssPresetSetting(Map<String, String> fileValues, String key, int defaultValue) {
        String configuredValue = fileValues.get(key);
        if (configuredValue == null || configuredValue.isEmpty()) {
            String environmentValue = System.getenv(key);
            if (environmentValue != null && !environmentValue.isEmpty()) {
                configuredValue = environmentValue.trim();
            }
        }

        if (configuredValue == null || configuredValue.isEmpty()) {
            return defaultValue;
        }

        String trimmed = configuredValue.trim();
        if (trimmed.equalsIgnoreCase("on") || trimmed.equalsIgnoreCase("auto") || trimmed.equals("4")) {
            return DLSS_PRESET_AUTO;
        }
        if (trimmed.equalsIgnoreCase("quality") || trimmed.equalsIgnoreCase("maxquality") || trimmed.equals("3")) {
            return DLSS_PRESET_QUALITY;
        }
        if (trimmed.equalsIgnoreCase("balanced") || trimmed.equals("2")) {
            return DLSS_PRESET_BALANCED;
        }
        if (trimmed.equalsIgnoreCase("performance") || trimmed.equalsIgnoreCase("maxperf") || trimmed.equals("1")) {
            return DLSS_PRESET_PERFORMANCE;
        }
        if (trimmed.equalsIgnoreCase("ultraperformance") || trimmed.equalsIgnoreCase("ultra performance") || trimmed.equalsIgnoreCase("ultraperf") || trimmed.equals("0")) {
            return DLSS_PRESET_ULTRA_PERFORMANCE;
        }
        if (trimmed.equalsIgnoreCase("dlaa") || trimmed.equalsIgnoreCase("fullresolution") || trimmed.equalsIgnoreCase("full resolution") || trimmed.equals("5")) {
            return DLSS_PRESET_DLAA;
        }
        if (trimmed.equalsIgnoreCase("custom") || trimmed.equalsIgnoreCase("off")) {
            return DLSS_PRESET_AUTO;
        }
        return defaultValue;
    }

    private static int readGameplayFovSetting(Map<String, String> fileValues, String key, int defaultValue) {
        String configuredValue = fileValues.get(key);
        if (configuredValue == null || configuredValue.isEmpty()) {
            String environmentValue = System.getenv(key);
            if (environmentValue != null && !environmentValue.isEmpty()) {
                configuredValue = environmentValue.trim();
            }
        }

        if (configuredValue == null || configuredValue.isEmpty()) {
            return normalizeGameplayFovDegrees(defaultValue);
        }

        try {
            return normalizeGameplayFovDegrees(Math.round(Float.parseFloat(configuredValue.trim())));
        } catch (NumberFormatException exception) {
            return normalizeGameplayFovDegrees(defaultValue);
        }
    }

    private static int readViewModelFovSetting(Map<String, String> fileValues, String key, int defaultValue) {
        String configuredValue = fileValues.get(key);
        if (configuredValue == null || configuredValue.isEmpty()) {
            String environmentValue = System.getenv(key);
            if (environmentValue != null && !environmentValue.isEmpty()) {
                configuredValue = environmentValue.trim();
            }
        }

        if (configuredValue == null || configuredValue.isEmpty()) {
            return normalizeViewModelFovDegrees(defaultValue);
        }

        try {
            return normalizeViewModelFovDegrees(Math.round(Float.parseFloat(configuredValue.trim())));
        } catch (NumberFormatException exception) {
            return normalizeViewModelFovDegrees(defaultValue);
        }
    }

    private static int readNoCullDistanceSetting(Map<String, String> fileValues, String key, int defaultValue) {
        String configuredValue = fileValues.get(key);
        if (configuredValue == null || configuredValue.isEmpty()) {
            String environmentValue = System.getenv(key);
            if (environmentValue != null && !environmentValue.isEmpty()) {
                configuredValue = environmentValue.trim();
            }
        }

        if (configuredValue == null || configuredValue.isEmpty()) {
            return normalizeNoCullDistanceBlocks(defaultValue);
        }

        try {
            return normalizeNoCullDistanceBlocks((int) Math.round(Double.parseDouble(configuredValue.trim())));
        } catch (NumberFormatException exception) {
            return normalizeNoCullDistanceBlocks(defaultValue);
        }
    }

    private static int readUpscalerTypeSetting(Map<String, String> fileValues, String key, int defaultValue) {
        String configuredValue = fileValues.get(key);
        if (configuredValue == null || configuredValue.isEmpty()) {
            String environmentValue = System.getenv(key);
            if (environmentValue != null && !environmentValue.isEmpty()) {
                configuredValue = environmentValue.trim();
            }
        }

        if (configuredValue == null || configuredValue.isEmpty()) {
            return defaultValue;
        }

        String trimmed = configuredValue.trim();
        if (trimmed.equalsIgnoreCase("none") || trimmed.equals("0")) {
            return UPSCALER_TYPE_NONE;
        }
        if (trimmed.equalsIgnoreCase("dlss") || trimmed.equals("1")) {
            return UPSCALER_TYPE_DLSS;
        }
        if (trimmed.equalsIgnoreCase("taau") || trimmed.equals("3")) {
            return UPSCALER_TYPE_TAAU;
        }
        if (trimmed.equalsIgnoreCase("xess") || trimmed.equals("4")) {
            return UPSCALER_TYPE_XESS;
        }
        return defaultValue;
    }

    private static int readXessPresetSetting(Map<String, String> fileValues, String key, int defaultValue) {
        String configuredValue = fileValues.get(key);
        if (configuredValue == null || configuredValue.isEmpty()) {
            String environmentValue = System.getenv(key);
            if (environmentValue != null && !environmentValue.isEmpty()) {
                configuredValue = environmentValue.trim();
            }
        }

        if (configuredValue == null || configuredValue.isEmpty()) {
            return defaultValue;
        }

        String trimmed = configuredValue.trim();
        if (trimmed.equalsIgnoreCase("ultraperformance") || trimmed.equalsIgnoreCase("ultra performance") || trimmed.equals("0")) {
            return XESS_PRESET_ULTRA_PERFORMANCE;
        }
        if (trimmed.equalsIgnoreCase("performance") || trimmed.equals("1")) {
            return XESS_PRESET_PERFORMANCE;
        }
        if (trimmed.equalsIgnoreCase("balanced") || trimmed.equals("2")) {
            return XESS_PRESET_BALANCED;
        }
        if (trimmed.equalsIgnoreCase("quality") || trimmed.equals("3")) {
            return XESS_PRESET_QUALITY;
        }
        if (trimmed.equalsIgnoreCase("ultraquality") || trimmed.equalsIgnoreCase("ultra quality") || trimmed.equals("4")) {
            return XESS_PRESET_ULTRA_QUALITY;
        }
        if (trimmed.equalsIgnoreCase("ultraqualityplus") || trimmed.equalsIgnoreCase("ultra quality plus") || trimmed.equals("5")) {
            return XESS_PRESET_ULTRA_QUALITY_PLUS;
        }
        if (trimmed.equalsIgnoreCase("nativeaa") || trimmed.equalsIgnoreCase("native aa") || trimmed.equals("6")) {
            return XESS_PRESET_NATIVE_AA;
        }
        return defaultValue;
    }

    private static int readTaauPresetSetting(Map<String, String> fileValues, String key, int defaultValue) {
        String configuredValue = fileValues.get(key);
        if (configuredValue == null || configuredValue.isEmpty()) {
            String environmentValue = System.getenv(key);
            if (environmentValue != null && !environmentValue.isEmpty()) {
                configuredValue = environmentValue.trim();
            }
        }

        if (configuredValue == null || configuredValue.isEmpty()) {
            return defaultValue;
        }

        String trimmed = configuredValue.trim();
        if (trimmed.equalsIgnoreCase("ultraperformance") || trimmed.equalsIgnoreCase("ultra performance") || trimmed.equals("0")) {
            return TAAU_PRESET_ULTRA_PERFORMANCE;
        }
        if (trimmed.equalsIgnoreCase("performance") || trimmed.equals("1")) {
            return TAAU_PRESET_PERFORMANCE;
        }
        if (trimmed.equalsIgnoreCase("balanced") || trimmed.equals("2")) {
            return TAAU_PRESET_BALANCED;
        }
        if (trimmed.equalsIgnoreCase("quality") || trimmed.equals("3")) {
            return TAAU_PRESET_QUALITY;
        }
        if (trimmed.equalsIgnoreCase("fullscreen") || trimmed.equals("4")) {
            return TAAU_PRESET_FULLSCREEN;
        }
        return defaultValue;
    }

    private static int readRtQualitySetting(Map<String, String> fileValues, String key, int defaultValue) {
        String configuredValue = fileValues.get(key);
        if (configuredValue == null || configuredValue.isEmpty()) {
            String environmentValue = System.getenv(key);
            if (environmentValue != null && !environmentValue.isEmpty()) {
                configuredValue = environmentValue.trim();
            }
        }

        if (configuredValue == null || configuredValue.isEmpty()) {
            return defaultValue;
        }

        String trimmed = configuredValue.trim();
        if (trimmed.equalsIgnoreCase("low") || trimmed.equals("0")) {
            return RT_QUALITY_LOW;
        }
        if (trimmed.equalsIgnoreCase("medium") || trimmed.equals("1")) {
            return RT_QUALITY_MEDIUM;
        }
        if (trimmed.equalsIgnoreCase("high") || trimmed.equals("2")) {
            return RT_QUALITY_HIGH;
        }
        if (trimmed.equalsIgnoreCase("ultra") || trimmed.equals("3")) {
            return RT_QUALITY_ULTRA;
        }
        if (trimmed.equalsIgnoreCase("potato") || trimmed.equals("4")) {
            return RT_QUALITY_POTATO;
        }
        return defaultValue;
    }

    private static int readBlockOutlineStyleSetting(Map<String, String> fileValues, String key, int defaultValue) {
        String configuredValue = fileValues.get(key);
        if (configuredValue == null || configuredValue.isEmpty()) {
            String environmentValue = System.getenv(key);
            if (environmentValue != null && !environmentValue.isEmpty()) {
                configuredValue = environmentValue.trim();
            }
        }

        if (configuredValue == null || configuredValue.isEmpty()) {
            return defaultValue;
        }

        String trimmed = configuredValue.trim();
        if (trimmed.equalsIgnoreCase("subtle") || trimmed.equalsIgnoreCase("classic") || trimmed.equals("0")) {
            return BLOCK_OUTLINE_STYLE_SUBTLE;
        }
        if (trimmed.equalsIgnoreCase("bold") || trimmed.equals("1")) {
            return BLOCK_OUTLINE_STYLE_BOLD;
        }
        if (trimmed.equalsIgnoreCase("solid") || trimmed.equals("2")) {
            return BLOCK_OUTLINE_STYLE_SOLID;
        }
        if (trimmed.equalsIgnoreCase("glow") || trimmed.equals("3")) {
            return BLOCK_OUTLINE_STYLE_GLOW;
        }
        if (trimmed.equalsIgnoreCase("rgb") || trimmed.equals("4")) {
            return BLOCK_OUTLINE_STYLE_RGB;
        }
        if (trimmed.equalsIgnoreCase("thin") || trimmed.equals("5")) {
            return BLOCK_OUTLINE_STYLE_THIN;
        }
        return defaultValue;
    }

    private static int readBlockOutlineEmissiveIntensityTenthsSetting(Map<String, String> fileValues, String key, int defaultValue) {
        String configuredValue = fileValues.get(key);
        if (configuredValue == null || configuredValue.isEmpty()) {
            String environmentValue = System.getenv(key);
            if (environmentValue != null && !environmentValue.isEmpty()) {
                configuredValue = environmentValue.trim();
            }
        }

        if (configuredValue == null || configuredValue.isEmpty()) {
            return normalizeBlockOutlineEmissiveIntensityTenths(defaultValue);
        }

        try {
            double parsedValue = Double.parseDouble(configuredValue.trim());
            if (parsedValue > (double) MAX_BLOCK_OUTLINE_EMISSIVE_INTENSITY_TENTHS / 10.0
                    && parsedValue <= (double) MAX_BLOCK_OUTLINE_EMISSIVE_INTENSITY_TENTHS) {
                return normalizeBlockOutlineEmissiveIntensityTenths((int) Math.round(parsedValue));
            }
            return normalizeBlockOutlineEmissiveIntensityTenths((int) Math.round(parsedValue * 10.0));
        } catch (NumberFormatException exception) {
            return normalizeBlockOutlineEmissiveIntensityTenths(defaultValue);
        }
    }

    private static int readDisplacementFactorHundredthsSetting(Map<String, String> fileValues, String key, int defaultValue) {
        String configuredValue = fileValues.get(key);
        if (configuredValue == null || configuredValue.isEmpty()) {
            String environmentValue = System.getenv(key);
            if (environmentValue != null && !environmentValue.isEmpty()) {
                configuredValue = environmentValue.trim();
            }
        }

        if (configuredValue == null || configuredValue.isEmpty()) {
            return normalizeDisplacementFactorHundredths(defaultValue);
        }

        try {
            double parsedValue = Double.parseDouble(configuredValue.trim());
            if (parsedValue > (double) MAX_DISPLACEMENT_FACTOR_HUNDREDTHS / 100.0
                    && parsedValue <= 40.0
                    && Math.rint(parsedValue) == parsedValue) {
                return normalizeDisplacementFactorHundredths((int) Math.round(parsedValue * 10.0));
            }
            return normalizeDisplacementFactorHundredths((int) Math.round(parsedValue * 100.0));
        } catch (NumberFormatException exception) {
            return normalizeDisplacementFactorHundredths(defaultValue);
        }
    }

    private static int readPositiveHundredthsSetting(Map<String, String> fileValues, String key, int defaultValue, int minimumValue, int maximumValue) {
        String configuredValue = fileValues.get(key);
        if (configuredValue == null || configuredValue.isEmpty()) {
            String environmentValue = System.getenv(key);
            if (environmentValue != null && !environmentValue.isEmpty()) {
                configuredValue = environmentValue.trim();
            }
        }

        if (configuredValue == null || configuredValue.isEmpty()) {
            return normalizeHundredths(defaultValue, minimumValue, maximumValue);
        }

        try {
            return normalizeHundredths((int) Math.round(Double.parseDouble(configuredValue.trim()) * 100.0), minimumValue, maximumValue);
        } catch (NumberFormatException exception) {
            return normalizeHundredths(defaultValue, minimumValue, maximumValue);
        }
    }

    private static int readSignedHundredthsSetting(Map<String, String> fileValues, String key, int defaultValue, int minimumValue, int maximumValue) {
        return readPositiveHundredthsSetting(fileValues, key, defaultValue, minimumValue, maximumValue);
    }

    private static int readQuickSettingsCategorySetting(Map<String, String> fileValues, String key, int defaultValue) {
        String configuredValue = fileValues.get(key);
        if (configuredValue == null || configuredValue.isEmpty()) {
            String environmentValue = System.getenv(key);
            if (environmentValue != null && !environmentValue.isEmpty()) {
                configuredValue = environmentValue.trim();
            }
        }

        if (configuredValue == null || configuredValue.isEmpty()) {
            return normalizeQuickSettingsCategory(defaultValue);
        }

        try {
            return normalizeQuickSettingsCategory(Integer.parseInt(configuredValue.trim()));
        } catch (NumberFormatException exception) {
            return normalizeQuickSettingsCategory(defaultValue);
        }
    }

    private static int readPositiveThousandthsSetting(Map<String, String> fileValues, String key, int defaultValue, int minimumValue, int maximumValue) {
        String configuredValue = fileValues.get(key);
        if (configuredValue == null || configuredValue.isEmpty()) {
            String environmentValue = System.getenv(key);
            if (environmentValue != null && !environmentValue.isEmpty()) {
                configuredValue = environmentValue.trim();
            }
        }

        if (configuredValue == null || configuredValue.isEmpty()) {
            return normalizeThousandths(defaultValue, minimumValue, maximumValue);
        }

        try {
            return normalizeThousandths((int) Math.round(Double.parseDouble(configuredValue.trim()) * 1000.0), minimumValue, maximumValue);
        } catch (NumberFormatException exception) {
            return normalizeThousandths(defaultValue, minimumValue, maximumValue);
        }
    }

    private static void saveLocked() {
        Map<String, String> fileValues = new TreeMap<String, String>(McrtxRuntimeConfig.loadFileValuesSnapshot());
        fileValues.put(PLAYER_SHADOWS_ENABLED_KEY, formatBoolean(playerShadowsEnabled));
        fileValues.put(HELD_TORCH_LIGHTS_ENABLED_KEY, formatBoolean(heldTorchLightsEnabled));
        fileValues.put(DYNAMIC_ENTITY_RENDERING_ENABLED_KEY, formatBoolean(dynamicEntityRenderingEnabled));
        fileValues.put(LIVING_ENTITY_RENDERING_ENABLED_KEY, formatBoolean(livingEntityRenderingEnabled));
        fileValues.put(ITEM_ENTITY_RENDERING_ENABLED_KEY, formatBoolean(itemEntityRenderingEnabled));
        fileValues.put(PAINTING_VANILLA_SUPPRESSION_ENABLED_KEY, formatBoolean(paintingVanillaSuppressionEnabled));
        fileValues.put(MOVING_PISTON_VANILLA_SUPPRESSION_ENABLED_KEY, formatBoolean(movingPistonVanillaSuppressionEnabled));
        fileValues.put(WORLD_RASTER_VANILLA_SUPPRESSION_ENABLED_KEY, formatBoolean(worldRasterVanillaSuppressionEnabled));
        fileValues.put(SIGN_CAPTURE_ENABLED_KEY, formatBoolean(signCaptureEnabled));
        fileValues.put(SIGN_TEXT_CAPTURE_ENABLED_KEY, formatBoolean(signTextCaptureEnabled));
        fileValues.put(SIGN_VANILLA_SUPPRESSION_ENABLED_KEY, formatBoolean(signVanillaSuppressionEnabled));
        fileValues.put(GAMEPLAY_FOV_KEY, Integer.toString(gameplayFovDegrees));
        fileValues.put(VIEW_MODEL_FOV_KEY, Integer.toString(viewModelFovDegrees));
        fileValues.put(NO_CULL_DISTANCE_KEY, Integer.toString(noCullDistanceBlocks));
        fileValues.put(UPSCALER_TYPE_KEY, formatUpscalerType(upscalerType));
        fileValues.put(DLSS_PRESET_KEY, formatDlssPreset(dlssPreset));
        fileValues.put(XESS_PRESET_KEY, formatXessPreset(xessPreset));
        fileValues.put(TAAU_PRESET_KEY, formatTaauPreset(taauPreset));
        fileValues.put(RAY_RECONSTRUCTION_ENABLED_KEY, formatBoolean(rayReconstructionEnabled));
        fileValues.put(RT_QUALITY_KEY, formatRtQuality(rtQuality));
        fileValues.put(BLOCK_OUTLINE_ENABLED_KEY, formatBoolean(blockOutlineEnabled));
        fileValues.put(BLOCK_OUTLINE_STYLE_KEY, formatBlockOutlineStyle(blockOutlineStyle));
        fileValues.put(
            BLOCK_OUTLINE_EMISSIVE_INTENSITY_KEY,
            formatBlockOutlineEmissiveIntensityTenths(blockOutlineEmissiveIntensityTenths));
        fileValues.put(DISPLACEMENT_FACTOR_KEY, formatDisplacementFactorHundredths(displacementFactorHundredths));
        fileValues.put(
            SUBSURFACE_MEASUREMENT_DISTANCE_KEY,
            formatHundredthsValue(subsurfaceMeasurementDistanceHundredths));
        fileValues.put(
            SUBSURFACE_RADIUS_SCALE_KEY,
            formatHundredthsValue(subsurfaceRadiusScaleHundredths));
        fileValues.put(
            SUBSURFACE_MAX_SAMPLE_RADIUS_KEY,
            formatHundredthsValue(subsurfaceMaxSampleRadiusHundredths));
        fileValues.put(
            SUBSURFACE_VOLUMETRIC_ANISOTROPY_KEY,
            formatHundredthsValue(subsurfaceVolumetricAnisotropyHundredths));
        fileValues.put(
            SUBSURFACE_DIFFUSION_PROFILE_ENABLED_KEY,
            formatBoolean(subsurfaceDiffusionProfileEnabled));
        fileValues.put(
            QUICK_SETTINGS_CATEGORY_KEY,
            Integer.toString(quickSettingsCategory));
        fileValues.put(
            WATER_THIN_WALLED_ENABLED_KEY,
            formatBoolean(waterThinWalledEnabled));
        fileValues.put(
            WATER_MATERIAL_THICKNESS_KEY,
            formatThousandthsValue(waterMaterialThicknessThousandths));
        fileValues.put(WATER_MATERIAL_THICKNESS_MIGRATION_KEY, formatBoolean(true));
        writeFileValues(fileValues);
    }

    private static void writeFileValues(Map<String, String> values) {
        File configFile = McrtxRuntimeConfig.resolveConfigFile();
        File parentDirectory = configFile.getParentFile();
        if (parentDirectory != null && !parentDirectory.isDirectory()) {
            parentDirectory.mkdirs();
        }

        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(configFile), StandardCharsets.US_ASCII));
            for (Map.Entry<String, String> entry : values.entrySet()) {
                String key = entry.getKey();
                if (key == null) {
                    continue;
                }

                key = key.trim();
                if (key.isEmpty()) {
                    continue;
                }

                String value = entry.getValue();
                if (value == null) {
                    value = "";
                }

                writer.write(key);
                writer.write('=');
                writer.write(value.trim());
                writer.newLine();
            }
        } catch (IOException exception) {
            System.out.println("[mcrtx] Failed to save runtime settings: " + exception);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException exception) {
                }
            }
        }
    }

    private static String formatBoolean(boolean enabled) {
        return enabled ? "1" : "0";
    }

    private static int deriveDefaultUpscalerType(Map<String, String> fileValues) {
        String legacyDlssSetting = fileValues.get(DLSS_PRESET_KEY);
        if (legacyDlssSetting != null && legacyDlssSetting.trim().equalsIgnoreCase("off")) {
            return UPSCALER_TYPE_NONE;
        }
        return UPSCALER_TYPE_DLSS;
    }

    private static int normalizeUpscalerType(int type) {
        if (type == UPSCALER_TYPE_NONE || type == UPSCALER_TYPE_DLSS || type == UPSCALER_TYPE_TAAU || type == UPSCALER_TYPE_XESS) {
            return type;
        }
        return UPSCALER_TYPE_DLSS;
    }

    private static int normalizeQuickSettingsCategory(int category) {
        if (category < QUICK_SETTINGS_CATEGORY_GAMEPLAY || category > QUICK_SETTINGS_CATEGORY_MATERIAL) {
            return DEFAULT_QUICK_SETTINGS_CATEGORY;
        }
        return category;
    }

    private static int normalizeGameplayFovDegrees(int fovDegrees) {
        if (fovDegrees < MIN_GAMEPLAY_FOV_DEGREES) {
            return MIN_GAMEPLAY_FOV_DEGREES;
        }
        if (fovDegrees > MAX_GAMEPLAY_FOV_DEGREES) {
            return MAX_GAMEPLAY_FOV_DEGREES;
        }
        return fovDegrees;
    }

    private static int normalizeViewModelFovDegrees(int fovDegrees) {
        if (fovDegrees < MIN_VIEW_MODEL_FOV_DEGREES) {
            return MIN_VIEW_MODEL_FOV_DEGREES;
        }
        if (fovDegrees > MAX_VIEW_MODEL_FOV_DEGREES) {
            return MAX_VIEW_MODEL_FOV_DEGREES;
        }
        return fovDegrees;
    }

    private static int normalizeNoCullDistanceBlocks(int blockDistance) {
        if (blockDistance < MIN_NO_CULL_DISTANCE_BLOCKS) {
            return MIN_NO_CULL_DISTANCE_BLOCKS;
        }
        if (blockDistance > MAX_NO_CULL_DISTANCE_BLOCKS) {
            return MAX_NO_CULL_DISTANCE_BLOCKS;
        }
        return blockDistance;
    }

    private static int normalizeDlssPreset(int preset) {
        if (preset >= DLSS_PRESET_ULTRA_PERFORMANCE && preset <= DLSS_PRESET_DLAA) {
            return preset;
        }
        return DLSS_PRESET_AUTO;
    }

    private static int normalizeXessPreset(int preset) {
        if (preset >= XESS_PRESET_ULTRA_PERFORMANCE && preset <= XESS_PRESET_NATIVE_AA) {
            return preset;
        }
        return XESS_PRESET_BALANCED;
    }

    private static int normalizeTaauPreset(int preset) {
        if (preset >= TAAU_PRESET_ULTRA_PERFORMANCE && preset <= TAAU_PRESET_FULLSCREEN) {
            return preset;
        }
        return TAAU_PRESET_BALANCED;
    }

    private static int normalizeRtQuality(int quality) {
        if ((quality >= RT_QUALITY_LOW && quality <= RT_QUALITY_ULTRA) || quality == RT_QUALITY_POTATO) {
            return quality;
        }
        return RT_QUALITY_HIGH;
    }

    private static int normalizeBlockOutlineStyle(int style) {
        if (style >= BLOCK_OUTLINE_STYLE_SUBTLE && style <= BLOCK_OUTLINE_STYLE_THIN) {
            return style;
        }
        return BLOCK_OUTLINE_STYLE_BOLD;
    }

    private static int normalizeBlockOutlineEmissiveIntensityTenths(int intensityTenths) {
        if (intensityTenths < MIN_BLOCK_OUTLINE_EMISSIVE_INTENSITY_TENTHS) {
            return MIN_BLOCK_OUTLINE_EMISSIVE_INTENSITY_TENTHS;
        }
        if (intensityTenths > MAX_BLOCK_OUTLINE_EMISSIVE_INTENSITY_TENTHS) {
            return MAX_BLOCK_OUTLINE_EMISSIVE_INTENSITY_TENTHS;
        }
        return intensityTenths;
    }

    private static int normalizeDisplacementFactorHundredths(int factorHundredths) {
        if (factorHundredths < MIN_DISPLACEMENT_FACTOR_HUNDREDTHS) {
            return MIN_DISPLACEMENT_FACTOR_HUNDREDTHS;
        }
        if (factorHundredths > MAX_DISPLACEMENT_FACTOR_HUNDREDTHS) {
            return MAX_DISPLACEMENT_FACTOR_HUNDREDTHS;
        }
        return factorHundredths;
    }

    private static int normalizeSubsurfaceMeasurementDistanceHundredths(int distanceHundredths) {
        return normalizeHundredths(
            distanceHundredths,
            MIN_SUBSURFACE_MEASUREMENT_DISTANCE_HUNDREDTHS,
            MAX_SUBSURFACE_MEASUREMENT_DISTANCE_HUNDREDTHS);
    }

    private static int normalizeSubsurfaceRadiusScaleHundredths(int scaleHundredths) {
        return normalizeHundredths(
            scaleHundredths,
            MIN_SUBSURFACE_RADIUS_SCALE_HUNDREDTHS,
            MAX_SUBSURFACE_RADIUS_SCALE_HUNDREDTHS);
    }

    private static int normalizeSubsurfaceMaxSampleRadiusHundredths(int radiusHundredths) {
        return normalizeHundredths(
            radiusHundredths,
            MIN_SUBSURFACE_MAX_SAMPLE_RADIUS_HUNDREDTHS,
            MAX_SUBSURFACE_MAX_SAMPLE_RADIUS_HUNDREDTHS);
    }

    private static int normalizeSubsurfaceVolumetricAnisotropyHundredths(int anisotropyHundredths) {
        return normalizeHundredths(
            anisotropyHundredths,
            MIN_SUBSURFACE_VOLUMETRIC_ANISOTROPY_HUNDREDTHS,
            MAX_SUBSURFACE_VOLUMETRIC_ANISOTROPY_HUNDREDTHS);
    }

    private static int normalizeWaterMaterialThicknessThousandths(int thicknessThousandths) {
        return normalizeThousandths(
            thicknessThousandths,
            MIN_WATER_MATERIAL_THICKNESS_THOUSANDTHS,
            MAX_WATER_MATERIAL_THICKNESS_THOUSANDTHS);
    }

    private static int normalizeHundredths(int value, int minimumValue, int maximumValue) {
        if (value < minimumValue) {
            return minimumValue;
        }
        if (value > maximumValue) {
            return maximumValue;
        }
        return value;
    }

    private static int normalizeThousandths(int value, int minimumValue, int maximumValue) {
        if (value < minimumValue) {
            return minimumValue;
        }
        if (value > maximumValue) {
            return maximumValue;
        }
        return value;
    }

    private static String formatThousandthsValue(int thousandthsValue) {
        int absoluteThousandthsValue = Math.abs(thousandthsValue);
        String formattedValue = Integer.toString(absoluteThousandthsValue / 1000)
            + "."
            + (absoluteThousandthsValue % 1000 < 100 ? "0" : "")
            + (absoluteThousandthsValue % 1000 < 10 ? "0" : "")
            + Integer.toString(absoluteThousandthsValue % 1000);
        if (thousandthsValue < 0) {
            return "-" + formattedValue;
        }
        return formattedValue;
    }

    private static String formatUpscalerType(int type) {
        switch (normalizeUpscalerType(type)) {
            case UPSCALER_TYPE_NONE:
                return "None";
            case UPSCALER_TYPE_TAAU:
                return "TAAU";
            case UPSCALER_TYPE_XESS:
                return "XeSS";
            case UPSCALER_TYPE_DLSS:
            default:
                return "DLSS";
        }
    }

    private static String formatDlssPreset(int preset) {
        switch (normalizeDlssPreset(preset)) {
            case DLSS_PRESET_QUALITY:
                return "MaxQuality";
            case DLSS_PRESET_BALANCED:
                return "Balanced";
            case DLSS_PRESET_PERFORMANCE:
                return "MaxPerf";
            case DLSS_PRESET_ULTRA_PERFORMANCE:
                return "UltraPerf";
            case DLSS_PRESET_DLAA:
                return "FullResolution";
            case DLSS_PRESET_AUTO:
            default:
                return "Auto";
        }
    }

    private static String formatXessPreset(int preset) {
        switch (normalizeXessPreset(preset)) {
            case XESS_PRESET_ULTRA_PERFORMANCE:
                return "UltraPerf";
            case XESS_PRESET_PERFORMANCE:
                return "Performance";
            case XESS_PRESET_QUALITY:
                return "Quality";
            case XESS_PRESET_ULTRA_QUALITY:
                return "UltraQuality";
            case XESS_PRESET_ULTRA_QUALITY_PLUS:
                return "UltraQualityPlus";
            case XESS_PRESET_NATIVE_AA:
                return "NativeAA";
            case XESS_PRESET_BALANCED:
            default:
                return "Balanced";
        }
    }

    private static String formatTaauPreset(int preset) {
        switch (normalizeTaauPreset(preset)) {
            case TAAU_PRESET_ULTRA_PERFORMANCE:
                return "UltraPerformance";
            case TAAU_PRESET_PERFORMANCE:
                return "Performance";
            case TAAU_PRESET_QUALITY:
                return "Quality";
            case TAAU_PRESET_FULLSCREEN:
                return "Fullscreen";
            case TAAU_PRESET_BALANCED:
            default:
                return "Balanced";
        }
    }

    private static String formatRtQuality(int quality) {
        switch (normalizeRtQuality(quality)) {
            case RT_QUALITY_POTATO:
                return "Potato";
            case RT_QUALITY_LOW:
                return "Low";
            case RT_QUALITY_MEDIUM:
                return "Medium";
            case RT_QUALITY_ULTRA:
                return "Ultra";
            case RT_QUALITY_HIGH:
            default:
                return "High";
        }
    }

    private static String formatBlockOutlineStyle(int style) {
        switch (normalizeBlockOutlineStyle(style)) {
            case BLOCK_OUTLINE_STYLE_SUBTLE:
                return "Subtle";
            case BLOCK_OUTLINE_STYLE_GLOW:
                return "Glow";
            case BLOCK_OUTLINE_STYLE_RGB:
                return "RGB";
            case BLOCK_OUTLINE_STYLE_THIN:
                return "Thin";
            case BLOCK_OUTLINE_STYLE_SOLID:
                return "Solid";
            case BLOCK_OUTLINE_STYLE_BOLD:
            default:
                return "Bold";
        }
    }

    private static String formatBlockOutlineEmissiveIntensityTenths(int intensityTenths) {
        int normalizedIntensityTenths = normalizeBlockOutlineEmissiveIntensityTenths(intensityTenths);
        return Integer.toString(normalizedIntensityTenths / 10) + "." + Integer.toString(normalizedIntensityTenths % 10);
    }

    private static String formatDisplacementFactorHundredths(int factorHundredths) {
        int normalizedFactorHundredths = normalizeDisplacementFactorHundredths(factorHundredths);
        return formatHundredthsValue(normalizedFactorHundredths);
    }

    private static String formatHundredthsValue(int hundredthsValue) {
        int absoluteHundredthsValue = Math.abs(hundredthsValue);
        int wholeValue = absoluteHundredthsValue / 100;
        int fractionalValue = absoluteHundredthsValue % 100;
        String formattedValue = Integer.toString(wholeValue)
            + "."
            + (fractionalValue < 10 ? "0" : "")
            + Integer.toString(fractionalValue);
        if (hundredthsValue < 0) {
            return "-" + formattedValue;
        }
        return formattedValue;
    }
}