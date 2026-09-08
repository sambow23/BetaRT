import mcrtx.bridge.McrtxLodSettings;
import mcrtx.bridge.McrtxLodSettingsNative;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

final class McrtxLodSettingsUi implements McrtxSettingsCategoryUi {
    private static final int LOD_ENABLED_BUTTON_ID = 40;
    private static final int LOD_DEBUG_VIEW_BUTTON_ID = 41;
    private static final int LOD_DISTANCE_SLIDER_ID = 42;
    private static final int LOD_HANDOVER_DISTANCE_SLIDER_ID = 43;
    private static final int LOD_COVERAGE_SLIDER_ID = 44;
    private static final int LOD_BUILD_BUDGET_SLIDER_ID = 45;
    private static final int RESET_DEFAULTS_BUTTON_ID = 46;
    private static final int LOD_EXTENDED_FOG_BUTTON_ID = 47;
    private static final int LOD_CACHE_BUTTON_ID = 48;

    public String getName() { return "Distant Terrain"; }

    public void addControls(McrtxQuickSettingsScreen screen) {
        screen.addControl(button(screen, LOD_ENABLED_BUTTON_ID, getEnabledLabel()));
        if (McrtxLodSettings.isLodEnabled()) {
            addSlider(screen, LOD_DISTANCE_SLIDER_ID, Slider.MODE_DISTANCE);
            addSlider(screen, LOD_HANDOVER_DISTANCE_SLIDER_ID, Slider.MODE_HANDOVER_DISTANCE);
            addSlider(screen, LOD_COVERAGE_SLIDER_ID, Slider.MODE_COVERAGE);
            addSlider(screen, LOD_BUILD_BUDGET_SLIDER_ID, Slider.MODE_BUILD_BUDGET);
            screen.addControl(button(screen, LOD_CACHE_BUTTON_ID, getCacheLabel()));
            screen.addControl(button(screen, LOD_EXTENDED_FOG_BUTTON_ID, getExtendedFogLabel()));
            screen.addControl(button(screen, LOD_DEBUG_VIEW_BUTTON_ID, getDebugViewLabel()));
        }
        screen.addControl(button(screen, RESET_DEFAULTS_BUTTON_ID, "Reset Distant Terrain Defaults"));
    }

    public int handleButton(int id, int direction) {
        if (id == LOD_ENABLED_BUTTON_ID) {
            setLodEnabled(!McrtxLodSettings.isLodEnabled());
            // The remaining rows only exist while it is on.
            return UPDATE_REBUILD;
        }
        if (id == LOD_DEBUG_VIEW_BUTTON_ID) {
            setLodDebugViewEnabled(!McrtxLodSettings.isLodDebugViewEnabled());
            return UPDATE_REFRESH;
        }
        if (id == LOD_CACHE_BUTTON_ID) {
            setCacheEnabled(!McrtxLodSettings.isLodCacheEnabled());
            return UPDATE_REFRESH;
        }
        if (id == LOD_EXTENDED_FOG_BUTTON_ID) {
            McrtxLodSettings.setLodExtendedFogEnabled(!McrtxLodSettings.isLodExtendedFogEnabled());
            // Nothing to push: the fog hook reads the setting on the next frame.
            return UPDATE_REFRESH;
        }
        if (id == RESET_DEFAULTS_BUTTON_ID) {
            resetDefaults();
            return UPDATE_REBUILD;
        }
        return UPDATE_NONE;
    }

    public void refreshButtons(McrtxQuickSettingsScreen screen) {
        setLabel(screen, LOD_ENABLED_BUTTON_ID, getEnabledLabel());
        setLabel(screen, LOD_DEBUG_VIEW_BUTTON_ID, getDebugViewLabel());
        setLabel(screen, LOD_EXTENDED_FOG_BUTTON_ID, getExtendedFogLabel());
        setLabel(screen, LOD_CACHE_BUTTON_ID, getCacheLabel());
    }

    public void applySavedSettings() {
        RemixLodCapture.setCacheEnabled(McrtxLodSettings.isLodCacheEnabled());
        RemixLodCapture.setDistanceBlocks(McrtxLodSettings.getLodDistanceBlocks());
        RemixLodCapture.setHandoverDistanceBlocks(McrtxLodSettings.getLodHandoverDistanceBlocks());
        RemixLodCapture.setCoverageHideFraction(McrtxLodSettings.getLodCoverageHideFraction());
        RemixLodCapture.setBuildBudgetPerTick(McrtxLodSettings.getLodBuildBudgetPerTick());
        McrtxLodSettingsNative.setLodDebugViewEnabled(McrtxLodSettings.isLodDebugViewEnabled());
        RemixLodCapture.setEnabled(McrtxLodSettings.isLodEnabled());
    }

    private static ke button(McrtxQuickSettingsScreen screen, int id, String label) {
        return new ke(id, screen.getControlX(), screen.takeNextRowY(), screen.getControlWidth(), McrtxQuickSettingsScreen.CONTROL_HEIGHT, label);
    }
    private static void addSlider(McrtxQuickSettingsScreen screen, int id, int mode) {
        screen.addControl(new Slider(id, screen.getControlX(), screen.takeNextRowY(), screen.getControlWidth(), McrtxQuickSettingsScreen.CONTROL_HEIGHT, mode));
    }
    private static void setLabel(McrtxQuickSettingsScreen screen, int id, String label) { ke button = screen.findButton(id); if (button != null) button.e = label; }
    private static String toggle(boolean enabled) { return enabled ? "ON" : "OFF"; }
    private static String getEnabledLabel() { return "Distant Terrain: " + toggle(McrtxLodSettings.isLodEnabled()); }
    private static String getDebugViewLabel() { return "Debug View: " + toggle(McrtxLodSettings.isLodDebugViewEnabled()); }
    private static String getExtendedFogLabel() { return "Extended Fog: " + toggle(McrtxLodSettings.isLodExtendedFogEnabled()); }
    private static String getCacheLabel() { return "Terrain Cache: " + toggle(McrtxLodSettings.isLodCacheEnabled()); }

    private static void setLodEnabled(boolean enabled) {
        McrtxLodSettings.setLodEnabled(enabled);
        RemixLodCapture.setEnabled(enabled);
    }

    /**
     * The cache is what fills the rings past the range a client is ever sent
     * chunks for, so switching it drops the field and rebuilds it from whichever
     * sources are left.
     */
    private static void setCacheEnabled(boolean enabled) {
        McrtxLodSettings.setLodCacheEnabled(enabled);
        RemixLodCapture.setCacheEnabled(enabled);
    }

    private static void setLodDebugViewEnabled(boolean enabled) {
        if (McrtxLodSettings.isLodDebugViewEnabled() == enabled) return;
        McrtxLodSettings.setLodDebugViewEnabled(enabled);
        McrtxLodSettingsNative.setLodDebugViewEnabled(enabled);
        // The material is baked into each region mesh, so existing regions have
        // to be rebuilt before the change is visible.
        RemixLodCapture.rebuildAll();
    }

    private static void setDistance(int blocks) {
        McrtxLodSettings.setLodDistanceBlocks(blocks);
        RemixLodCapture.setDistanceBlocks(McrtxLodSettings.getLodDistanceBlocks());
    }

    private static void setHandoverDistance(int blocks) {
        McrtxLodSettings.setLodHandoverDistanceBlocks(blocks);
        RemixLodCapture.setHandoverDistanceBlocks(McrtxLodSettings.getLodHandoverDistanceBlocks());
    }

    private static void setCoverageHide(int hundredths) {
        McrtxLodSettings.setLodCoverageHideHundredths(hundredths);
        RemixLodCapture.setCoverageHideFraction(McrtxLodSettings.getLodCoverageHideFraction());
    }

    private static void setBuildBudget(int budget) {
        McrtxLodSettings.setLodBuildBudgetPerTick(budget);
        RemixLodCapture.setBuildBudgetPerTick(McrtxLodSettings.getLodBuildBudgetPerTick());
    }

    private static void resetDefaults() {
        setLodEnabled(McrtxLodSettings.DEFAULT_LOD_ENABLED);
        setLodDebugViewEnabled(McrtxLodSettings.DEFAULT_LOD_DEBUG_VIEW_ENABLED);
        McrtxLodSettings.setLodExtendedFogEnabled(McrtxLodSettings.DEFAULT_LOD_EXTENDED_FOG);
        setCacheEnabled(McrtxLodSettings.DEFAULT_LOD_CACHE_ENABLED);
        setDistance(McrtxLodSettings.DEFAULT_LOD_DISTANCE_BLOCKS);
        setHandoverDistance(McrtxLodSettings.DEFAULT_LOD_HANDOVER_DISTANCE_BLOCKS);
        setCoverageHide(McrtxLodSettings.DEFAULT_LOD_COVERAGE_HIDE_HUNDREDTHS);
        setBuildBudget(McrtxLodSettings.DEFAULT_LOD_BUILD_BUDGET);
    }

    private static final class Slider extends ke {
        static final int MODE_DISTANCE = 0;
        static final int MODE_HANDOVER_DISTANCE = 1;
        static final int MODE_COVERAGE = 2;
        static final int MODE_BUILD_BUDGET = 3;

        /**
         * What each distance stop costs, nearest first, indexed the same way
         * {@link McrtxLodSettings#lodDistanceStopAt} is.
         *
         * <p>These are cost tiers rather than quality tiers, and they are not
         * evenly spaced by accident: the region count runs 36, 91, 139, 187,
         * 235, so the first three notches are cheap and the last two are where
         * the field roughly doubles the geometry it has already paid for.
         * "Extreme" also carries a caveat the row has no space to spell out --
         * a 4096-block field needs 8 km of generated world under it, which a
         * save only has if the player walked it and which a server never sends.
         */
        private static final String[] COST_LABELS = {
            "Lowest", "Low", "Medium", "High", "Extreme"
        };

        /**
         * Tier for a stop, clamped rather than indexed directly, so adding or
         * removing a ring changes the ladder without also being able to throw
         * from a settings screen.
         */
        private static int costLabelIndex(int stopIndex) {
            if (stopIndex < 0) return 0;
            if (stopIndex >= COST_LABELS.length) return COST_LABELS.length - 1;
            return stopIndex;
        }

        private final int mode;
        private final int minimum;
        private final int maximum;
        private boolean dragging;
        private float position;

        Slider(int id, int x, int y, int width, int height, int mode) {
            super(id, x, y, width, height, "");
            this.mode = mode;
            if (mode == MODE_HANDOVER_DISTANCE) {
                minimum = McrtxLodSettings.MIN_LOD_HANDOVER_DISTANCE_BLOCKS;
                maximum = McrtxLodSettings.MAX_LOD_HANDOVER_DISTANCE_BLOCKS;
            } else if (mode == MODE_COVERAGE) {
                minimum = McrtxLodSettings.MIN_LOD_COVERAGE_HIDE_HUNDREDTHS;
                maximum = McrtxLodSettings.MAX_LOD_COVERAGE_HIDE_HUNDREDTHS;
            } else if (mode == MODE_BUILD_BUDGET) {
                minimum = McrtxLodSettings.MIN_LOD_BUILD_BUDGET;
                maximum = McrtxLodSettings.MAX_LOD_BUILD_BUDGET;
            } else {
                // Stops, not blocks. The distance ladder is geometric -- 192,
                // 512, 1024, 2048, 4096 -- so a track laid out in blocks puts
                // the three cheapest settings inside its left eighth and gives
                // the most expensive one half the width. In stops every notch
                // is one detail ring, which is both easier to hit and a truer
                // picture of what the control does.
                minimum = 0;
                maximum = McrtxLodSettings.lodDistanceStopCount() - 1;
            }
            sync();
        }

        protected int a(boolean hovered) { return 0; }
        protected void b(Minecraft minecraft, int mouseX, int mouseY) {
            if (!this.h) return;
            if (dragging) update(mouseX); else sync();
            GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
            int thumbX = this.c + (int) (position * (float) (this.a - 8));
            this.b(thumbX, this.d, 0, 66, 4, 20);
            this.b(thumbX + 4, this.d, 196, 66, 4, 20);
        }
        public boolean c(Minecraft minecraft, int mouseX, int mouseY) { if (!super.c(minecraft, mouseX, mouseY)) return false; dragging = true; update(mouseX); return true; }
        public void a(int mouseX, int mouseY) { dragging = false; sync(); }

        private void sync() {
            // For MODE_DISTANCE this is a stop index; for everything else it is
            // the setting's own value. The two only ever meet in `position`.
            int value = mode == MODE_HANDOVER_DISTANCE ? McrtxLodSettings.getLodHandoverDistanceBlocks()
                    : mode == MODE_COVERAGE ? McrtxLodSettings.getLodCoverageHideHundredths()
                    : mode == MODE_BUILD_BUDGET ? McrtxLodSettings.getLodBuildBudgetPerTick()
                    : McrtxLodSettings.lodDistanceStopIndex(McrtxLodSettings.getLodDistanceBlocks());
            position = (float) (value - minimum) / (float) (maximum - minimum);
            label(value);
        }
        private void update(int mouseX) {
            float next = (float) (mouseX - (this.c + 4)) / (float) (this.a - 8);
            if (next < 0.0f) next = 0.0f;
            if (next > 1.0f) next = 1.0f;
            int value = minimum + Math.round(next * (float) (maximum - minimum));
            position = (float) (value - minimum) / (float) (maximum - minimum);
            if (mode == MODE_HANDOVER_DISTANCE) setHandoverDistance(value);
            else if (mode == MODE_COVERAGE) setCoverageHide(value);
            else if (mode == MODE_BUILD_BUDGET) setBuildBudget(value);
            // Rounding to a whole stop already snapped the distance, so the
            // setting is handed the ring edge itself rather than a radius for
            // it to snap again.
            else setDistance(McrtxLodSettings.lodDistanceStopAt(value));
            label(value);
        }
        private void label(int value) {
            if (mode == MODE_HANDOVER_DISTANCE) this.e = "Handover Distance: " + value + " Blocks";
            else if (mode == MODE_COVERAGE) this.e = "Coverage Handover: " + value + "%";
            else if (mode == MODE_BUILD_BUDGET) this.e = "Region Builds Per Tick: " + value;
            // The distance picks a detail ring rather than a free radius, so the
            // label has to say something that changes at every notch -- else the
            // slider looks broken when it jumps between stops. It says the cost
            // rather than the ring count, because the ring count is a fact about
            // the implementation and the cost is the thing being chosen: the
            // outer rings are where the triangles and the memory go, and the
            // last one is where the world usually runs out of generated ground.
            //
            // The panel is 204 px wide, which is about thirty-five characters,
            // so this is as much as fits on the row. The rest of the story --
            // what each stop costs and why the far ones are often empty -- is in
            // the release notes, where there is room to tell it properly.
            else this.e = "LOD Distance: " + McrtxLodSettings.lodDistanceStopAt(value)
                    + " Blocks (" + COST_LABELS[costLabelIndex(value)] + ")";
        }
    }
}
