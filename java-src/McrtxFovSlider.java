import mcrtx.bridge.McrtxRuntimeSettings;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

public final class McrtxFovSlider extends ke {
    private static final int SLIDER_MODE_GAMEPLAY_FOV = 0;
    private static final int SLIDER_MODE_VIEW_MODEL_FOV = 1;
    private static final int SLIDER_MODE_NO_CULL_DISTANCE = 2;
    private static final int SLIDER_MODE_BLOCK_OUTLINE_INTENSITY = 3;
    private static final int SLIDER_MODE_DISPLACEMENT_FACTOR = 4;
    private static final int SLIDER_MODE_SUBSURFACE_MEASUREMENT_DISTANCE = 5;
    private static final int SLIDER_MODE_SUBSURFACE_RADIUS_SCALE = 6;
    private static final int SLIDER_MODE_SUBSURFACE_MAX_SAMPLE_RADIUS = 7;
    private static final int SLIDER_MODE_SUBSURFACE_VOLUMETRIC_ANISOTROPY = 8;
    private static final int SLIDER_MODE_WATER_MATERIAL_THICKNESS = 9;

    private final int minimumValue;
    private final int maximumValue;
    private final int sliderMode;
    private boolean dragging;
    private float sliderPosition;

    public McrtxFovSlider(int buttonId, int x, int y, int width, int height) {
        this(buttonId, x, y, width, height, SLIDER_MODE_GAMEPLAY_FOV);
    }

    public McrtxFovSlider(int buttonId, int x, int y, int width, int height, boolean viewModelSlider) {
        this(buttonId, x, y, width, height, viewModelSlider ? SLIDER_MODE_VIEW_MODEL_FOV : SLIDER_MODE_GAMEPLAY_FOV);
    }

    public static McrtxFovSlider createNoCullDistanceSlider(int buttonId, int x, int y, int width, int height) {
        return new McrtxFovSlider(buttonId, x, y, width, height, SLIDER_MODE_NO_CULL_DISTANCE);
    }

    public static McrtxFovSlider createBlockOutlineIntensitySlider(int buttonId, int x, int y, int width, int height) {
        return new McrtxFovSlider(buttonId, x, y, width, height, SLIDER_MODE_BLOCK_OUTLINE_INTENSITY);
    }

    public static McrtxFovSlider createDisplacementFactorSlider(int buttonId, int x, int y, int width, int height) {
        return new McrtxFovSlider(buttonId, x, y, width, height, SLIDER_MODE_DISPLACEMENT_FACTOR);
    }

    public static McrtxFovSlider createSubsurfaceMeasurementDistanceSlider(int buttonId, int x, int y, int width, int height) {
        return new McrtxFovSlider(buttonId, x, y, width, height, SLIDER_MODE_SUBSURFACE_MEASUREMENT_DISTANCE);
    }

    public static McrtxFovSlider createSubsurfaceRadiusScaleSlider(int buttonId, int x, int y, int width, int height) {
        return new McrtxFovSlider(buttonId, x, y, width, height, SLIDER_MODE_SUBSURFACE_RADIUS_SCALE);
    }

    public static McrtxFovSlider createSubsurfaceMaxSampleRadiusSlider(int buttonId, int x, int y, int width, int height) {
        return new McrtxFovSlider(buttonId, x, y, width, height, SLIDER_MODE_SUBSURFACE_MAX_SAMPLE_RADIUS);
    }

    public static McrtxFovSlider createSubsurfaceVolumetricAnisotropySlider(int buttonId, int x, int y, int width, int height) {
        return new McrtxFovSlider(buttonId, x, y, width, height, SLIDER_MODE_SUBSURFACE_VOLUMETRIC_ANISOTROPY);
    }

    public static McrtxFovSlider createWaterMaterialThicknessSlider(int buttonId, int x, int y, int width, int height) {
        return new McrtxFovSlider(buttonId, x, y, width, height, SLIDER_MODE_WATER_MATERIAL_THICKNESS);
    }

    private McrtxFovSlider(int buttonId, int x, int y, int width, int height, int sliderMode) {
        super(buttonId, x, y, width, height, "");
        this.sliderMode = sliderMode;
        if (sliderMode == SLIDER_MODE_VIEW_MODEL_FOV) {
            this.minimumValue = McrtxRuntimeSettings.MIN_VIEW_MODEL_FOV_DEGREES;
            this.maximumValue = McrtxRuntimeSettings.MAX_VIEW_MODEL_FOV_DEGREES;
        } else if (sliderMode == SLIDER_MODE_NO_CULL_DISTANCE) {
            this.minimumValue = McrtxRuntimeSettings.MIN_NO_CULL_DISTANCE_BLOCKS;
            this.maximumValue = McrtxRuntimeSettings.MAX_NO_CULL_DISTANCE_BLOCKS;
        } else if (sliderMode == SLIDER_MODE_BLOCK_OUTLINE_INTENSITY) {
            this.minimumValue = McrtxRuntimeSettings.MIN_BLOCK_OUTLINE_EMISSIVE_INTENSITY_TENTHS;
            this.maximumValue = McrtxRuntimeSettings.MAX_BLOCK_OUTLINE_EMISSIVE_INTENSITY_TENTHS;
        } else if (sliderMode == SLIDER_MODE_DISPLACEMENT_FACTOR) {
            this.minimumValue = McrtxRuntimeSettings.MIN_DISPLACEMENT_FACTOR_HUNDREDTHS;
            this.maximumValue = McrtxRuntimeSettings.MAX_DISPLACEMENT_FACTOR_HUNDREDTHS;
        } else if (sliderMode == SLIDER_MODE_SUBSURFACE_MEASUREMENT_DISTANCE) {
            this.minimumValue = McrtxRuntimeSettings.MIN_SUBSURFACE_MEASUREMENT_DISTANCE_HUNDREDTHS;
            this.maximumValue = McrtxRuntimeSettings.MAX_SUBSURFACE_MEASUREMENT_DISTANCE_HUNDREDTHS;
        } else if (sliderMode == SLIDER_MODE_SUBSURFACE_RADIUS_SCALE) {
            this.minimumValue = McrtxRuntimeSettings.MIN_SUBSURFACE_RADIUS_SCALE_HUNDREDTHS;
            this.maximumValue = McrtxRuntimeSettings.MAX_SUBSURFACE_RADIUS_SCALE_HUNDREDTHS;
        } else if (sliderMode == SLIDER_MODE_SUBSURFACE_MAX_SAMPLE_RADIUS) {
            this.minimumValue = McrtxRuntimeSettings.MIN_SUBSURFACE_MAX_SAMPLE_RADIUS_HUNDREDTHS;
            this.maximumValue = McrtxRuntimeSettings.MAX_SUBSURFACE_MAX_SAMPLE_RADIUS_HUNDREDTHS;
        } else if (sliderMode == SLIDER_MODE_SUBSURFACE_VOLUMETRIC_ANISOTROPY) {
            this.minimumValue = McrtxRuntimeSettings.MIN_SUBSURFACE_VOLUMETRIC_ANISOTROPY_HUNDREDTHS;
            this.maximumValue = McrtxRuntimeSettings.MAX_SUBSURFACE_VOLUMETRIC_ANISOTROPY_HUNDREDTHS;
        } else if (sliderMode == SLIDER_MODE_WATER_MATERIAL_THICKNESS) {
            this.minimumValue = McrtxRuntimeSettings.MIN_WATER_MATERIAL_THICKNESS_THOUSANDTHS;
            this.maximumValue = McrtxRuntimeSettings.MAX_WATER_MATERIAL_THICKNESS_THOUSANDTHS;
        } else {
            this.minimumValue = McrtxRuntimeSettings.MIN_GAMEPLAY_FOV_DEGREES;
            this.maximumValue = McrtxRuntimeSettings.MAX_GAMEPLAY_FOV_DEGREES;
        }
        syncFromSettings();
    }

    protected int a(boolean hovered) {
        return 0;
    }

    protected void b(Minecraft minecraft, int mouseX, int mouseY) {
        if (!this.h) {
            return;
        }

        if (this.dragging) {
            updateFromMouse(mouseX);
        } else {
            syncFromSettings();
        }

        GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        int thumbX = this.c + (int) (this.sliderPosition * (float) (this.a - 8));
        this.b(thumbX, this.d, 0, 66, 4, 20);
        this.b(thumbX + 4, this.d, 196, 66, 4, 20);
    }

    public boolean c(Minecraft minecraft, int mouseX, int mouseY) {
        if (!super.c(minecraft, mouseX, mouseY)) {
            return false;
        }

        this.dragging = true;
        updateFromMouse(mouseX);
        return true;
    }

    public void a(int mouseX, int mouseY) {
        this.dragging = false;
        syncFromSettings();
    }

    private void syncFromSettings() {
        int sliderValue;
        if (this.sliderMode == SLIDER_MODE_VIEW_MODEL_FOV) {
            sliderValue = MinecraftRemixHooks.getViewModelFovDegrees();
        } else if (this.sliderMode == SLIDER_MODE_NO_CULL_DISTANCE) {
            sliderValue = MinecraftRemixHooks.getNoCullDistanceBlocks();
        } else if (this.sliderMode == SLIDER_MODE_BLOCK_OUTLINE_INTENSITY) {
            sliderValue = MinecraftRemixHooks.getBlockOutlineEmissiveIntensityTenths();
        } else if (this.sliderMode == SLIDER_MODE_DISPLACEMENT_FACTOR) {
            sliderValue = MinecraftRemixHooks.getDisplacementFactorHundredths();
        } else if (this.sliderMode == SLIDER_MODE_SUBSURFACE_MEASUREMENT_DISTANCE) {
            sliderValue = MinecraftRemixHooks.getSubsurfaceMeasurementDistanceHundredths();
        } else if (this.sliderMode == SLIDER_MODE_SUBSURFACE_RADIUS_SCALE) {
            sliderValue = MinecraftRemixHooks.getSubsurfaceRadiusScaleHundredths();
        } else if (this.sliderMode == SLIDER_MODE_SUBSURFACE_MAX_SAMPLE_RADIUS) {
            sliderValue = MinecraftRemixHooks.getSubsurfaceMaxSampleRadiusHundredths();
        } else if (this.sliderMode == SLIDER_MODE_SUBSURFACE_VOLUMETRIC_ANISOTROPY) {
            sliderValue = MinecraftRemixHooks.getSubsurfaceVolumetricAnisotropyHundredths();
        } else if (this.sliderMode == SLIDER_MODE_WATER_MATERIAL_THICKNESS) {
            sliderValue = MinecraftRemixHooks.getWaterMaterialThicknessThousandths();
        } else {
            sliderValue = MinecraftRemixHooks.getGameplayFovDegrees();
        }
        this.sliderPosition = toSliderPosition(sliderValue);
        updateLabel(sliderValue);
    }

    private void updateFromMouse(int mouseX) {
        float newSliderPosition = (float) (mouseX - (this.c + 4)) / (float) (this.a - 8);
        if (newSliderPosition < 0.0f) {
            newSliderPosition = 0.0f;
        }
        if (newSliderPosition > 1.0f) {
            newSliderPosition = 1.0f;
        }

        int sliderValue = toSliderValue(newSliderPosition);
        this.sliderPosition = toSliderPosition(sliderValue);
        if (this.sliderMode == SLIDER_MODE_VIEW_MODEL_FOV) {
            MinecraftRemixHooks.setViewModelFovDegrees(sliderValue);
        } else if (this.sliderMode == SLIDER_MODE_NO_CULL_DISTANCE) {
            MinecraftRemixHooks.setNoCullDistanceBlocks(sliderValue);
        } else if (this.sliderMode == SLIDER_MODE_BLOCK_OUTLINE_INTENSITY) {
            MinecraftRemixHooks.setBlockOutlineEmissiveIntensityTenths(sliderValue);
        } else if (this.sliderMode == SLIDER_MODE_DISPLACEMENT_FACTOR) {
            MinecraftRemixHooks.setDisplacementFactorHundredths(sliderValue);
        } else if (this.sliderMode == SLIDER_MODE_SUBSURFACE_MEASUREMENT_DISTANCE) {
            MinecraftRemixHooks.setSubsurfaceMeasurementDistanceHundredths(sliderValue);
        } else if (this.sliderMode == SLIDER_MODE_SUBSURFACE_RADIUS_SCALE) {
            MinecraftRemixHooks.setSubsurfaceRadiusScaleHundredths(sliderValue);
        } else if (this.sliderMode == SLIDER_MODE_SUBSURFACE_MAX_SAMPLE_RADIUS) {
            MinecraftRemixHooks.setSubsurfaceMaxSampleRadiusHundredths(sliderValue);
        } else if (this.sliderMode == SLIDER_MODE_SUBSURFACE_VOLUMETRIC_ANISOTROPY) {
            MinecraftRemixHooks.setSubsurfaceVolumetricAnisotropyHundredths(sliderValue);
        } else if (this.sliderMode == SLIDER_MODE_WATER_MATERIAL_THICKNESS) {
            MinecraftRemixHooks.setWaterMaterialThicknessThousandths(sliderValue);
        } else {
            MinecraftRemixHooks.setGameplayFovDegrees(sliderValue);
        }
        updateLabel(sliderValue);
    }

    private int toSliderValue(float normalizedPosition) {
        return this.minimumValue + Math.round(normalizedPosition * (float) (this.maximumValue - this.minimumValue));
    }

    private float toSliderPosition(int sliderValue) {
        return (float) (sliderValue - this.minimumValue) / (float) (this.maximumValue - this.minimumValue);
    }

    private void updateLabel(int sliderValue) {
        if (this.sliderMode == SLIDER_MODE_VIEW_MODEL_FOV) {
            this.e = "Hand FOV: " + sliderValue;
            return;
        }
        if (this.sliderMode == SLIDER_MODE_NO_CULL_DISTANCE) {
            this.e = "Anti-Cull Distance: " + sliderValue + " Blocks";
            return;
        }
        if (this.sliderMode == SLIDER_MODE_BLOCK_OUTLINE_INTENSITY) {
            this.e = "Outline Intensity: " + formatTenthsValue(sliderValue);
            return;
        }
        if (this.sliderMode == SLIDER_MODE_DISPLACEMENT_FACTOR) {
            this.e = "Displacement Factor: " + formatHundredthsValue(sliderValue) + "x";
            return;
        }
        if (this.sliderMode == SLIDER_MODE_SUBSURFACE_MEASUREMENT_DISTANCE) {
            this.e = "SSS Distance: " + formatHundredthsValue(sliderValue);
            return;
        }
        if (this.sliderMode == SLIDER_MODE_SUBSURFACE_RADIUS_SCALE) {
            this.e = "SSS Radius Scale: " + formatHundredthsValue(sliderValue) + "x";
            return;
        }
        if (this.sliderMode == SLIDER_MODE_SUBSURFACE_MAX_SAMPLE_RADIUS) {
            this.e = "SSS Max Radius: " + formatHundredthsValue(sliderValue);
            return;
        }
        if (this.sliderMode == SLIDER_MODE_SUBSURFACE_VOLUMETRIC_ANISOTROPY) {
            this.e = "SSS Anisotropy: " + formatHundredthsValue(sliderValue);
            return;
        }
        if (this.sliderMode == SLIDER_MODE_WATER_MATERIAL_THICKNESS) {
            this.e = "Water Thickness: " + formatThousandthsValue(sliderValue);
            return;
        }
        this.e = "FOV: " + sliderValue;
    }

    private static String formatTenthsValue(int tenthsValue) {
        return Integer.toString(tenthsValue / 10) + "." + Integer.toString(tenthsValue % 10);
    }

    private static String formatHundredthsValue(int hundredthsValue) {
        int absoluteHundredthsValue = Math.abs(hundredthsValue);
        String formattedValue = Integer.toString(absoluteHundredthsValue / 100)
            + "."
            + (absoluteHundredthsValue % 100 < 10 ? "0" : "")
            + Integer.toString(absoluteHundredthsValue % 100);
        if (hundredthsValue < 0) {
            return "-" + formattedValue;
        }
        return formattedValue;
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
}