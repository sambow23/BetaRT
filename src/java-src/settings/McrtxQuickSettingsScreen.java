import mcrtx.bridge.McrtxSettingsStore;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

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
    private static final int SCROLLBAR_WIDTH = 6;
    private static final int MIN_SCROLLBAR_THUMB_HEIGHT = 20;
    private static final int GL_SCISSOR_TEST = 3089;
    private static final int CATEGORY_PREVIOUS_BUTTON_ID = 300;
    private static final int CATEGORY_LABEL_BUTTON_ID = 301;
    private static final int CATEGORY_NEXT_BUTTON_ID = 302;
    private static final int CLOSE_BUTTON_ID = 309;

    private static int activeCategory = McrtxSettingsStore.DEFAULT_CATEGORY;
    private static final int[] categoryScrollOffsets = new int[4];

    private final List scrollControls = new ArrayList();
    private int nextControlY;
    private int panelBottomY = PANEL_TOP + 80;
    private int scrollViewportTop;
    private int scrollViewportBottom;
    private int scrollContentBottom;
    private int scrollOffset;
    private int maxScrollOffset;
    private boolean addingScrollControls;
    private boolean draggingScrollbar;
    private int scrollbarDragOffset;

    public void b() {
        this.e.clear();
        scrollControls.clear();
        activeCategory = McrtxSettingsStore.getQuickSettingsCategory();
        nextControlY = PANEL_TOP + HEADER_HEIGHT + CONTROL_GAP;

        addCategorySelectorControls();
        scrollViewportTop = nextControlY;
        addingScrollControls = true;
        activeUi().addControls(this);
        addingScrollControls = false;
        scrollContentBottom = nextControlY - CONTROL_GAP;

        int naturalCloseY = scrollContentBottom + CONTROL_GAP;
        int naturalPanelBottom = naturalCloseY + CONTROL_HEIGHT + PANEL_INSET;
        panelBottomY = Math.min(naturalPanelBottom, Math.max(PANEL_TOP, this.d - PANEL_TOP));
        int closeY = panelBottomY - PANEL_INSET - CONTROL_HEIGHT;
        scrollViewportBottom = closeY;
        maxScrollOffset = Math.max(0, scrollContentBottom - scrollViewportBottom);

        if (maxScrollOffset > 0) {
            for (int index = 0; index < scrollControls.size(); index += 1) {
                ke control = (ke) scrollControls.get(index);
                control.a -= SCROLLBAR_WIDTH + CONTROL_GAP;
            }
        }

        addControl(new ke(
                CLOSE_BUTTON_ID,
                getControlX(),
                closeY,
                getControlWidth(),
                CONTROL_HEIGHT,
                "Close"));

        scrollOffset = 0;
        setScrollOffset(categoryScrollOffsets[activeCategory]);
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
        if (draggingScrollbar) {
            setScrollOffsetFromThumb(mouseY - scrollbarDragOffset);
        }

        int panelRight = PANEL_LEFT + PANEL_WIDTH;
        int panelBottom = panelBottomY;
        this.a(PANEL_LEFT, PANEL_TOP, panelRight, panelBottom, 0xB0101010, 0x90080808);
        this.a(PANEL_LEFT, PANEL_TOP, panelRight, PANEL_TOP + 1, 0x90D0D0D0);
        this.a(PANEL_LEFT, PANEL_TOP + HEADER_HEIGHT - 1, panelRight, PANEL_TOP + HEADER_HEIGHT, 0x60404040);
        this.a(PANEL_LEFT, panelBottom - 1, panelRight, panelBottom, 0x70303030);

        enableScrollScissor();
        drawScrollControls(mouseX, mouseY);
        GL11.glDisable(GL_SCISSOR_TEST);
        drawFixedControls(mouseX, mouseY);
        this.a(this.g, "BetaRT Settings", PANEL_LEFT + PANEL_WIDTH / 2, PANEL_TOP + 8, 0xFFFFFF);
        drawScrollbar(mouseX, mouseY);
    }

    public void f() {
        int wheel = Mouse.getEventDWheel();
        if (wheel != 0 && maxScrollOffset > 0) {
            int mouseX = Mouse.getEventX() * this.c / this.b.d;
            int mouseY = this.d - Mouse.getEventY() * this.d / this.b.e - 1;
            if (isInsidePanel(mouseX, mouseY)) {
                setScrollOffset(scrollOffset + (wheel > 0 ? -(CONTROL_HEIGHT + CONTROL_GAP) : CONTROL_HEIGHT + CONTROL_GAP));
                return;
            }
        }
        super.f();
    }

    protected void a(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && maxScrollOffset > 0 && isInsideScrollbar(mouseX, mouseY)) {
            int thumbTop = getScrollbarThumbTop();
            int thumbBottom = thumbTop + getScrollbarThumbHeight();
            if (mouseY < thumbTop || mouseY >= thumbBottom) {
                setScrollOffsetFromThumb(mouseY - getScrollbarThumbHeight() / 2);
                thumbTop = getScrollbarThumbTop();
            }
            draggingScrollbar = true;
            scrollbarDragOffset = mouseY - thumbTop;
            return;
        }

        if (!isInsideScrollViewport(mouseX, mouseY)) {
            List controlsToRestore = new ArrayList(scrollControls);
            boolean[] visibility = new boolean[controlsToRestore.size()];
            for (int index = 0; index < controlsToRestore.size(); index += 1) {
                ke control = (ke) controlsToRestore.get(index);
                visibility[index] = control.h;
                control.h = false;
            }
            try {
                super.a(mouseX, mouseY, mouseButton);
            } finally {
                for (int index = 0; index < controlsToRestore.size(); index += 1) {
                    ((ke) controlsToRestore.get(index)).h = visibility[index];
                }
            }
            return;
        }

        super.a(mouseX, mouseY, mouseButton);
    }

    protected void b(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton == 0 && draggingScrollbar) {
            draggingScrollbar = false;
            return;
        }
        super.b(mouseX, mouseY, mouseButton);
    }

    public boolean c() {
        return false;
    }

    void addControl(ke control) {
        this.e.add(control);
        if (addingScrollControls) scrollControls.add(control);
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

    private void setScrollOffset(int requestedOffset) {
        int nextOffset = requestedOffset;
        if (nextOffset < 0) nextOffset = 0;
        if (nextOffset > maxScrollOffset) nextOffset = maxScrollOffset;
        if (nextOffset > 0 && nextOffset < maxScrollOffset) {
            int rowHeight = CONTROL_HEIGHT + CONTROL_GAP;
            nextOffset = Math.round((float) nextOffset / (float) rowHeight) * rowHeight;
            if (nextOffset > maxScrollOffset) nextOffset = maxScrollOffset;
        }
        int delta = scrollOffset - nextOffset;

        if (delta != 0) {
            for (int index = 0; index < scrollControls.size(); index += 1) {
                ((ke) scrollControls.get(index)).d += delta;
            }
            scrollOffset = nextOffset;
            categoryScrollOffsets[activeCategory] = scrollOffset;
        }
        updateScrollControlVisibility();
    }

    private void setScrollOffsetFromThumb(int requestedThumbTop) {
        int thumbTravel = getScrollbarTrackHeight() - getScrollbarThumbHeight();
        if (thumbTravel <= 0 || maxScrollOffset <= 0) {
            setScrollOffset(0);
            return;
        }

        int thumbTop = requestedThumbTop;
        if (thumbTop < scrollViewportTop) thumbTop = scrollViewportTop;
        if (thumbTop > scrollViewportTop + thumbTravel) thumbTop = scrollViewportTop + thumbTravel;
        setScrollOffset(Math.round((float) (thumbTop - scrollViewportTop) * (float) maxScrollOffset / (float) thumbTravel));
    }

    private void drawFixedControls(int mouseX, int mouseY) {
        for (int index = 0; index < this.e.size(); index += 1) {
            ke control = (ke) this.e.get(index);
            if (!scrollControls.contains(control)) control.a(this.b, mouseX, mouseY);
        }
    }

    private void drawScrollControls(int mouseX, int mouseY) {
        for (int index = 0; index < scrollControls.size(); index += 1) {
            ke control = (ke) scrollControls.get(index);
            if (control.h) control.a(this.b, mouseX, mouseY);
        }
    }

    private void updateScrollControlVisibility() {
        for (int index = 0; index < scrollControls.size(); index += 1) {
            ke control = (ke) scrollControls.get(index);
            control.h = control.d >= scrollViewportTop
                    && control.d + control.b <= scrollViewportBottom;
        }
    }

    private void enableScrollScissor() {
        int displayWidth = this.b.d;
        int displayHeight = this.b.e;
        int viewportLeft = PANEL_LEFT + PANEL_INSET;
        int viewportRight = PANEL_LEFT + PANEL_WIDTH - PANEL_INSET;
        int scissorLeft = viewportLeft * displayWidth / this.c;
        int scissorRight = (viewportRight * displayWidth + this.c - 1) / this.c;
        int scissorTop = scrollViewportTop * displayHeight / this.d;
        int scissorBottom = (scrollViewportBottom * displayHeight + this.d - 1) / this.d;
        GL11.glEnable(GL_SCISSOR_TEST);
        GL11.glScissor(
                scissorLeft,
                displayHeight - scissorBottom,
                Math.max(0, scissorRight - scissorLeft),
                Math.max(0, scissorBottom - scissorTop));
    }

    private void drawScrollbar(int mouseX, int mouseY) {
        if (maxScrollOffset <= 0) return;
        int trackLeft = getScrollbarTrackLeft();
        int trackRight = trackLeft + SCROLLBAR_WIDTH;
        int thumbTop = getScrollbarThumbTop();
        int thumbBottom = thumbTop + getScrollbarThumbHeight();
        this.a(trackLeft, scrollViewportTop, trackRight, scrollViewportBottom, 0x70000000, 0x70000000);
        int thumbColor = draggingScrollbar || (mouseX >= trackLeft && mouseX < trackRight && mouseY >= thumbTop && mouseY < thumbBottom)
                ? 0xFFD0D0D0
                : 0xFFA0A0A0;
        this.a(trackLeft, thumbTop, trackRight, thumbBottom, thumbColor, thumbColor);
    }

    private int getScrollbarTrackLeft() {
        return PANEL_LEFT + PANEL_WIDTH - PANEL_INSET - SCROLLBAR_WIDTH;
    }

    private int getScrollbarTrackHeight() {
        return Math.max(0, scrollViewportBottom - scrollViewportTop);
    }

    private int getScrollbarThumbHeight() {
        int trackHeight = getScrollbarTrackHeight();
        int contentHeight = Math.max(1, scrollContentBottom - scrollViewportTop);
        return Math.min(trackHeight, Math.max(MIN_SCROLLBAR_THUMB_HEIGHT, trackHeight * trackHeight / contentHeight));
    }

    private int getScrollbarThumbTop() {
        int thumbTravel = getScrollbarTrackHeight() - getScrollbarThumbHeight();
        if (thumbTravel <= 0 || maxScrollOffset <= 0) return scrollViewportTop;
        return scrollViewportTop + Math.round((float) scrollOffset * (float) thumbTravel / (float) maxScrollOffset);
    }

    private boolean isInsidePanel(int mouseX, int mouseY) {
        return mouseX >= PANEL_LEFT
                && mouseX < PANEL_LEFT + PANEL_WIDTH
                && mouseY >= PANEL_TOP
                && mouseY < panelBottomY;
    }

    private boolean isInsideScrollViewport(int mouseX, int mouseY) {
        return mouseX >= PANEL_LEFT + PANEL_INSET
                && mouseX < PANEL_LEFT + PANEL_WIDTH - PANEL_INSET
                && mouseY >= scrollViewportTop
                && mouseY < scrollViewportBottom;
    }

    private boolean isInsideScrollbar(int mouseX, int mouseY) {
        return mouseX >= getScrollbarTrackLeft()
                && mouseX < getScrollbarTrackLeft() + SCROLLBAR_WIDTH
                && mouseY >= scrollViewportTop
                && mouseY < scrollViewportBottom;
    }

    private McrtxSettingsCategoryUi activeUi() {
        return McrtxSettingsCategories.get(activeCategory);
    }
}
