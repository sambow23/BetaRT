/*
 * Decompiled with CFR 0.152.
 */
public class qh
extends da {
    private int a = 0;

    public void a() {
        ++this.a;
    }

    public void b() {
        this.e.clear();
        this.e.add(new ke(0, this.c / 2 - 100, this.d / 4 + 120 + 12, "Back to title screen"));
    }

    protected void a(ke ke2) {
        if (!ke2.g) {
            return;
        }
        if (ke2.f == 0) {
            this.b.a(new fu());
        }
    }

    public void a(int n2, int n3, float f2) {
        this.i();
        this.a(this.g, "Level save conflict", this.c / 2, this.d / 4 - 60 + 20, 0xFFFFFF);
        this.b(this.g, "Minecraft detected a conflict in the level save data.", this.c / 2 - 140, this.d / 4 - 60 + 60 + 0, 0xA0A0A0);
        this.b(this.g, "This could be caused by two copies of the game", this.c / 2 - 140, this.d / 4 - 60 + 60 + 18, 0xA0A0A0);
        this.b(this.g, "accessing the same level.", this.c / 2 - 140, this.d / 4 - 60 + 60 + 27, 0xA0A0A0);
        this.b(this.g, "To prevent level corruption, the current game has quit.", this.c / 2 - 140, this.d / 4 - 60 + 60 + 45, 0xA0A0A0);
        super.a(n2, n3, f2);
    }
}

