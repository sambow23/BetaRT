public final class McrtxSssQuickScreen extends da {
    private static final int PANEL_LEFT = 8;
    private static final int PANEL_TOP = 8;
    private static final int PANEL_WIDTH = 220;
    private static final int PANEL_INSET = 8;
    private static final int CONTROL_HEIGHT = 20;
    private static final int CONTROL_GAP = 4;
    private static final int SUBSURFACE_MEASUREMENT_DISTANCE_SLIDER_ID = 301;
    private static final int SUBSURFACE_RADIUS_SCALE_SLIDER_ID = 302;
    private static final int SUBSURFACE_MAX_SAMPLE_RADIUS_SLIDER_ID = 303;
    private static final int SUBSURFACE_VOLUMETRIC_ANISOTROPY_SLIDER_ID = 304;
    private static final int SUBSURFACE_DIFFUSION_PROFILE_BUTTON_ID = 305;
    private static final int CLOSE_BUTTON_ID = 306;

    public void b() {
        this.e.clear();
        this.e.add(McrtxFovSlider.createSubsurfaceMeasurementDistanceSlider(
                SUBSURFACE_MEASUREMENT_DISTANCE_SLIDER_ID,
                getControlX(),
                getMeasurementDistanceSliderY(),
                getControlWidth(),
                CONTROL_HEIGHT));
        this.e.add(McrtxFovSlider.createSubsurfaceRadiusScaleSlider(
                SUBSURFACE_RADIUS_SCALE_SLIDER_ID,
                getControlX(),
                getRadiusScaleSliderY(),
                getControlWidth(),
                CONTROL_HEIGHT));
        this.e.add(McrtxFovSlider.createSubsurfaceMaxSampleRadiusSlider(
                SUBSURFACE_MAX_SAMPLE_RADIUS_SLIDER_ID,
                getControlX(),
                getMaxSampleRadiusSliderY(),
                getControlWidth(),
                CONTROL_HEIGHT));
        this.e.add(McrtxFovSlider.createSubsurfaceVolumetricAnisotropySlider(
                SUBSURFACE_VOLUMETRIC_ANISOTROPY_SLIDER_ID,
                getControlX(),
                getVolumetricAnisotropySliderY(),
                getControlWidth(),
                CONTROL_HEIGHT));
        this.e.add(new ke(
                SUBSURFACE_DIFFUSION_PROFILE_BUTTON_ID,
                getControlX(),
                getDiffusionProfileButtonY(),
                getControlWidth(),
                CONTROL_HEIGHT,
                MinecraftRemixHooks.getSubsurfaceDiffusionProfileButtonLabel()));
        this.e.add(new ke(
                CLOSE_BUTTON_ID,
                getControlX(),
                getCloseButtonY(),
                getControlWidth(),
                CONTROL_HEIGHT,
                "Close"));
        refreshButtons();
    }

    protected void a(ke button) {
        if (button == null || !button.g) {
            return;
        }

        if (button.f == SUBSURFACE_DIFFUSION_PROFILE_BUTTON_ID) {
            MinecraftRemixHooks.setSubsurfaceDiffusionProfileEnabled(!MinecraftRemixHooks.isSubsurfaceDiffusionProfileEnabled());
            refreshButtons();
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
        int panelBottom = getPanelBottomY();
        this.a(PANEL_LEFT, PANEL_TOP, panelRight, panelBottom, 0xB0101010, 0x90080808);
        this.a(PANEL_LEFT, PANEL_TOP, panelRight, PANEL_TOP + 1, 0x90D0D0D0);
        this.a(PANEL_LEFT, panelBottom - 1, panelRight, panelBottom, 0x70303030);
        this.a(this.g, "BetaRT SSS", PANEL_LEFT + PANEL_WIDTH / 2, PANEL_TOP + 8, 0xFFFFFF);
        this.b(this.g, "Alt+B or Esc closes", getControlX(), PANEL_TOP + 22, 0xB8B8B8);
        super.a(mouseX, mouseY, partialTicks);
    }

    public boolean c() {
        return false;
    }

    private void refreshButtons() {
        ke diffusionProfileButton = findButton(SUBSURFACE_DIFFUSION_PROFILE_BUTTON_ID);
        if (diffusionProfileButton != null) {
            diffusionProfileButton.e = MinecraftRemixHooks.getSubsurfaceDiffusionProfileButtonLabel();
        }
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

    private int getMeasurementDistanceSliderY() {
        return PANEL_TOP + 40;
    }

    private int getRadiusScaleSliderY() {
        return getMeasurementDistanceSliderY() + CONTROL_HEIGHT + CONTROL_GAP;
    }

    private int getMaxSampleRadiusSliderY() {
        return getRadiusScaleSliderY() + CONTROL_HEIGHT + CONTROL_GAP;
    }

    private int getVolumetricAnisotropySliderY() {
        return getMaxSampleRadiusSliderY() + CONTROL_HEIGHT + CONTROL_GAP;
    }

    private int getDiffusionProfileButtonY() {
        return getVolumetricAnisotropySliderY() + CONTROL_HEIGHT + CONTROL_GAP;
    }

    private int getCloseButtonY() {
        return getDiffusionProfileButtonY() + CONTROL_HEIGHT + CONTROL_GAP;
    }

    private int getPanelBottomY() {
        return getCloseButtonY() + CONTROL_HEIGHT + PANEL_INSET;
    }
}