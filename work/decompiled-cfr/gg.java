/*
 * Decompiled with CFR 0.152.
 */
public class gg
extends da {
    private nb a;
    private int i = 0;

    public gg(nb nb2) {
        this.a = nb2;
    }

    protected void a(char c2, int n2) {
    }

    public void b() {
        this.e.clear();
    }

    public void a() {
        ++this.i;
        if (this.i % 20 == 0) {
            this.a.b(new lz());
        }
        if (this.a != null) {
            this.a.a();
        }
    }

    protected void a(ke ke2) {
    }

    public void a(int n2, int n3, float f2) {
        this.b(0);
        nh nh2 = nh.a();
        this.a(this.g, nh2.a("multiplayer.downloadingTerrain"), this.c / 2, this.d / 2 - 50, 0xFFFFFF);
        super.a(n2, n3, f2);
    }
}

