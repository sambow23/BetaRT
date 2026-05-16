public final class McrtxOptionsScreen extends da {
    private static final int PLAYER_SHADOWS_BUTTON_ID = 1;
    private static final int HELD_TORCH_LIGHTS_BUTTON_ID = 2;
    private static final int DONE_BUTTON_ID = 200;

    private final da parent;

    public McrtxOptionsScreen(da parent) {
        this.parent = parent;
    }

    public void b() {
        this.e.clear();
        int centerX = this.c / 2 - 100;
        int baseY = this.d / 6;
        this.e.add(new ke(PLAYER_SHADOWS_BUTTON_ID, centerX, baseY + 48, MinecraftRemixHooks.getPlayerShadowsButtonLabel()));
        this.e.add(new ke(HELD_TORCH_LIGHTS_BUTTON_ID, centerX, baseY + 72, MinecraftRemixHooks.getHeldTorchLightsButtonLabel()));
        this.e.add(new ke(DONE_BUTTON_ID, centerX, baseY + 168, "Done"));
    }

    protected void a(ke button) {
        if (button == null || !button.g) {
            return;
        }

        if (button.f == PLAYER_SHADOWS_BUTTON_ID) {
            MinecraftRemixHooks.setPlayerShadowsEnabled(!MinecraftRemixHooks.isPlayerShadowsEnabled());
            button.e = MinecraftRemixHooks.getPlayerShadowsButtonLabel();
            return;
        }

        if (button.f == HELD_TORCH_LIGHTS_BUTTON_ID) {
            MinecraftRemixHooks.setHeldTorchLightsEnabled(!MinecraftRemixHooks.isHeldTorchLightsEnabled());
            button.e = MinecraftRemixHooks.getHeldTorchLightsButtonLabel();
            return;
        }

        if (button.f == DONE_BUTTON_ID) {
            this.b.a(this.parent);
        }
    }

    protected void a(char character, int keyCode) {
        if (keyCode == 1) {
            this.b.a(this.parent);
            return;
        }
        super.a(character, keyCode);
    }

    public void a(int mouseX, int mouseY, float partialTicks) {
        this.i();
        this.a(this.g, "BetaRT settings", this.c / 2, 20, 0xFFFFFF);
        super.a(mouseX, mouseY, partialTicks);
    }
}