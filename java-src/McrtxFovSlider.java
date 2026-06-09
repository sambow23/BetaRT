import mcrtx.bridge.McrtxRuntimeSettings;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

public final class McrtxFovSlider extends ke {
    private static final int SLIDER_MODE_GAMEPLAY_FOV = 0;
    private static final int SLIDER_MODE_VIEW_MODEL_FOV = 1;
    private static final int SLIDER_MODE_NO_CULL_DISTANCE = 2;
    private static final int SLIDER_MODE_BLOCK_OUTLINE_INTENSITY = 3;
    private static final int SLIDER_MODE_DISPLACEMENT_FACTOR = 4;

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
        this.e = "FOV: " + sliderValue;
    }

    private static String formatTenthsValue(int tenthsValue) {
        return Integer.toString(tenthsValue / 10) + "." + Integer.toString(tenthsValue % 10);
    }

    private static String formatHundredthsValue(int hundredthsValue) {
        return Integer.toString(hundredthsValue / 100)
            + "."
            + (hundredthsValue % 100 < 10 ? "0" : "")
            + Integer.toString(hundredthsValue % 100);
    }
}