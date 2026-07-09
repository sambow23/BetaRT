import mcrtx.bridge.McrtxRuntimeSettings;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

public final class McrtxWaterTransmissionSlider extends ke {
    private static final int SLIDER_MODE_TRANSMITTANCE_RED = 0;
    private static final int SLIDER_MODE_TRANSMITTANCE_GREEN = 1;
    private static final int SLIDER_MODE_TRANSMITTANCE_BLUE = 2;
    private static final int SLIDER_MODE_TRANSMITTANCE_DISTANCE = 3;
    private static final int SLIDER_MODE_REFRACTIVE_INDEX = 4;
    private static final int SLIDER_MODE_THICKNESS = 5;

    private final int minimumValue;
    private final int maximumValue;
    private final int sliderMode;
    private boolean dragging;
    private float sliderPosition;

    public static McrtxWaterTransmissionSlider createTransmittanceRedSlider(
            int buttonId, int x, int y, int width, int height) {
        return new McrtxWaterTransmissionSlider(
                buttonId, x, y, width, height, SLIDER_MODE_TRANSMITTANCE_RED);
    }

    public static McrtxWaterTransmissionSlider createTransmittanceGreenSlider(
            int buttonId, int x, int y, int width, int height) {
        return new McrtxWaterTransmissionSlider(
                buttonId, x, y, width, height, SLIDER_MODE_TRANSMITTANCE_GREEN);
    }

    public static McrtxWaterTransmissionSlider createTransmittanceBlueSlider(
            int buttonId, int x, int y, int width, int height) {
        return new McrtxWaterTransmissionSlider(
                buttonId, x, y, width, height, SLIDER_MODE_TRANSMITTANCE_BLUE);
    }

    public static McrtxWaterTransmissionSlider createTransmittanceDistanceSlider(
            int buttonId, int x, int y, int width, int height) {
        return new McrtxWaterTransmissionSlider(
                buttonId, x, y, width, height, SLIDER_MODE_TRANSMITTANCE_DISTANCE);
    }

    public static McrtxWaterTransmissionSlider createRefractiveIndexSlider(
            int buttonId, int x, int y, int width, int height) {
        return new McrtxWaterTransmissionSlider(
                buttonId, x, y, width, height, SLIDER_MODE_REFRACTIVE_INDEX);
    }

    public static McrtxWaterTransmissionSlider createThicknessSlider(
            int buttonId, int x, int y, int width, int height) {
        return new McrtxWaterTransmissionSlider(
                buttonId, x, y, width, height, SLIDER_MODE_THICKNESS);
    }

    private McrtxWaterTransmissionSlider(
            int buttonId, int x, int y, int width, int height, int sliderMode) {
        super(buttonId, x, y, width, height, "");
        this.sliderMode = sliderMode;
        if (sliderMode == SLIDER_MODE_REFRACTIVE_INDEX) {
            this.minimumValue = McrtxRuntimeSettings.MIN_WATER_REFRACTIVE_INDEX_THOUSANDTHS;
            this.maximumValue = McrtxRuntimeSettings.MAX_WATER_REFRACTIVE_INDEX_THOUSANDTHS;
        } else if (sliderMode == SLIDER_MODE_THICKNESS) {
            this.minimumValue = McrtxRuntimeSettings.MIN_WATER_MATERIAL_THICKNESS_THOUSANDTHS;
            this.maximumValue = McrtxRuntimeSettings.MAX_WATER_MATERIAL_THICKNESS_THOUSANDTHS;
        } else if (sliderMode == SLIDER_MODE_TRANSMITTANCE_DISTANCE) {
            this.minimumValue = McrtxRuntimeSettings.MIN_WATER_TRANSMITTANCE_DISTANCE_HUNDREDTHS;
            this.maximumValue = McrtxRuntimeSettings.MAX_WATER_TRANSMITTANCE_DISTANCE_HUNDREDTHS;
        } else {
            this.minimumValue = McrtxRuntimeSettings.MIN_WATER_TRANSMITTANCE_COLOR_HUNDREDTHS;
            this.maximumValue = McrtxRuntimeSettings.MAX_WATER_TRANSMITTANCE_COLOR_HUNDREDTHS;
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
        if (this.sliderMode == SLIDER_MODE_TRANSMITTANCE_RED) {
            sliderValue = MinecraftRemixHooks.getWaterTransmittanceRedHundredths();
        } else if (this.sliderMode == SLIDER_MODE_TRANSMITTANCE_GREEN) {
            sliderValue = MinecraftRemixHooks.getWaterTransmittanceGreenHundredths();
        } else if (this.sliderMode == SLIDER_MODE_TRANSMITTANCE_BLUE) {
            sliderValue = MinecraftRemixHooks.getWaterTransmittanceBlueHundredths();
        } else if (this.sliderMode == SLIDER_MODE_TRANSMITTANCE_DISTANCE) {
            sliderValue = MinecraftRemixHooks.getWaterTransmittanceDistanceHundredths();
        } else if (this.sliderMode == SLIDER_MODE_REFRACTIVE_INDEX) {
            sliderValue = MinecraftRemixHooks.getWaterRefractiveIndexThousandths();
        } else {
            sliderValue = MinecraftRemixHooks.getWaterMaterialThicknessThousandths();
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
        if (this.sliderMode == SLIDER_MODE_TRANSMITTANCE_RED) {
            MinecraftRemixHooks.setWaterTransmittanceRedHundredths(sliderValue);
        } else if (this.sliderMode == SLIDER_MODE_TRANSMITTANCE_GREEN) {
            MinecraftRemixHooks.setWaterTransmittanceGreenHundredths(sliderValue);
        } else if (this.sliderMode == SLIDER_MODE_TRANSMITTANCE_BLUE) {
            MinecraftRemixHooks.setWaterTransmittanceBlueHundredths(sliderValue);
        } else if (this.sliderMode == SLIDER_MODE_TRANSMITTANCE_DISTANCE) {
            MinecraftRemixHooks.setWaterTransmittanceDistanceHundredths(sliderValue);
        } else if (this.sliderMode == SLIDER_MODE_REFRACTIVE_INDEX) {
            MinecraftRemixHooks.setWaterRefractiveIndexThousandths(sliderValue);
        } else {
            MinecraftRemixHooks.setWaterMaterialThicknessThousandths(sliderValue);
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
        if (this.sliderMode == SLIDER_MODE_TRANSMITTANCE_RED) {
            this.e = "Water Red: " + formatHundredthsValue(sliderValue);
        } else if (this.sliderMode == SLIDER_MODE_TRANSMITTANCE_GREEN) {
            this.e = "Water Green: " + formatHundredthsValue(sliderValue);
        } else if (this.sliderMode == SLIDER_MODE_TRANSMITTANCE_BLUE) {
            this.e = "Water Blue: " + formatHundredthsValue(sliderValue);
        } else if (this.sliderMode == SLIDER_MODE_TRANSMITTANCE_DISTANCE) {
            this.e = "Water Distance: " + formatHundredthsValue(sliderValue);
        } else if (this.sliderMode == SLIDER_MODE_REFRACTIVE_INDEX) {
            this.e = "Water IOR: " + formatThousandthsValue(sliderValue);
        } else {
            this.e = "Water Thickness: " + formatThousandthsValue(sliderValue);
        }
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
