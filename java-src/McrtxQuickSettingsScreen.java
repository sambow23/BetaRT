import mcrtx.bridge.McrtxSettingsStore;

public final class McrtxQuickSettingsScreen extends da {
    private static final int PANEL_LEFT = 8;
    private static final int PANEL_TOP = 8;
    private static final int PANEL_WIDTH = 220;
    private static final int PANEL_INSET = 8;
    private static final int HEADER_HEIGHT = 24;
    static final int CONTROL_HEIGHT = 20;
    private static final int CONTROL_GAP = 4;
    private static final int SELECTOR_ARROW_WIDTH = 24;
    private static final int SELECTOR_LABEL_WIDTH = PANEL_WIDTH - PANEL_INSET * 2 - SELECTOR_ARROW_WIDTH * 2 - CONTROL_GAP * 2;
    private static final int CATEGORY_PREVIOUS_BUTTON_ID = 300;
    private static final int CATEGORY_LABEL_BUTTON_ID = 301;
    private static final int CATEGORY_NEXT_BUTTON_ID = 302;
    private static final int CLOSE_BUTTON_ID = 309;

    private static int activeCategory = McrtxSettingsStore.DEFAULT_CATEGORY;

    private int nextControlY;
    private int panelBottomY = PANEL_TOP + 80;

    public void b() {
        this.e.clear();
        activeCategory = McrtxSettingsStore.getQuickSettingsCategory();
        nextControlY = PANEL_TOP + HEADER_HEIGHT + CONTROL_GAP;

        addCategorySelectorControls();
        activeUi().addControls(this);

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
        if (button == null || !button.g) return;

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
        if (button.f == CLOSE_BUTTON_ID) {
            this.b.a((da) null);
            return;
        }

        int update = activeUi().handleButton(button.f);
        if (update == McrtxSettingsCategoryUi.UPDATE_REBUILD) b();
        else if (update == McrtxSettingsCategoryUi.UPDATE_REFRESH) refreshButtons();
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

    void addControl(ke control) {
        this.e.add(control);
    }

    int takeNextRowY() {
        int rowY = nextControlY;
        nextControlY += CONTROL_HEIGHT + CONTROL_GAP;
        return rowY;
    }

    int getControlX() {
        return PANEL_LEFT + PANEL_INSET;
    }

    int getControlWidth() {
        return PANEL_WIDTH - PANEL_INSET * 2;
    }

    ke findButton(int buttonId) {
        for (Object entry : this.e) {
            if (entry instanceof ke) {
                ke button = (ke) entry;
                if (button.f == buttonId) return button;
            }
        }
        return null;
    }

    private void refreshButtons() {
        ke categoryLabelButton = findButton(CATEGORY_LABEL_BUTTON_ID);
        if (categoryLabelButton != null) categoryLabelButton.e = "Category: " + activeUi().getName();
        activeUi().refreshButtons(this);
    }

    private void addCategorySelectorControls() {
        int rowY = takeNextRowY();
        int controlX = getControlX();
        int labelX = controlX + SELECTOR_ARROW_WIDTH + CONTROL_GAP;
        int nextButtonX = labelX + SELECTOR_LABEL_WIDTH + CONTROL_GAP;
        addControl(new ke(CATEGORY_PREVIOUS_BUTTON_ID, controlX, rowY, SELECTOR_ARROW_WIDTH, CONTROL_HEIGHT, "<"));
        addControl(new ke(CATEGORY_LABEL_BUTTON_ID, labelX, rowY, SELECTOR_LABEL_WIDTH, CONTROL_HEIGHT, "Category: " + activeUi().getName()));
        addControl(new ke(CATEGORY_NEXT_BUTTON_ID, nextButtonX, rowY, SELECTOR_ARROW_WIDTH, CONTROL_HEIGHT, ">"));
    }

    private void cycleCategory(int delta) {
        activeCategory = (activeCategory + delta + 4) % 4;
        McrtxSettingsStore.setQuickSettingsCategory(activeCategory);
    }

    private McrtxSettingsCategoryUi activeUi() {
        return McrtxSettingsCategories.get(activeCategory);
    }
}
