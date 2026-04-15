/*
 * Decompiled with CFR 0.152.
 */
public class up
extends da {
    private da i;
    protected String a = "Controls";
    private kv j;
    private int l = -1;

    public up(da da2, kv kv2) {
        this.i = da2;
        this.j = kv2;
    }

    private int k() {
        return this.c / 2 - 155;
    }

    public void b() {
        nh nh2 = nh.a();
        int n2 = this.k();
        for (int i2 = 0; i2 < this.j.w.length; ++i2) {
            this.e.add(new ab(i2, n2 + i2 % 2 * 160, this.d / 6 + 24 * (i2 >> 1), 70, 20, this.j.b(i2)));
        }
        this.e.add(new ke(200, this.c / 2 - 100, this.d / 6 + 168, nh2.a("gui.done")));
        this.a = nh2.a("controls.title");
    }

    protected void a(ke ke2) {
        for (int i2 = 0; i2 < this.j.w.length; ++i2) {
            ((ke)this.e.get((int)i2)).e = this.j.b(i2);
        }
        if (ke2.f == 200) {
            this.b.a(this.i);
        } else {
            this.l = ke2.f;
            ke2.e = "> " + this.j.b(ke2.f) + " <";
        }
    }

    protected void a(char c2, int n2) {
        if (this.l >= 0) {
            this.j.a(this.l, n2);
            ((ke)this.e.get((int)this.l)).e = this.j.b(this.l);
            this.l = -1;
        } else {
            super.a(c2, n2);
        }
    }

    public void a(int n2, int n3, float f2) {
        this.i();
        this.a(this.g, this.a, this.c / 2, 20, 0xFFFFFF);
        int n4 = this.k();
        for (int i2 = 0; i2 < this.j.w.length; ++i2) {
            this.b(this.g, this.j.a(i2), n4 + i2 % 2 * 160 + 70 + 6, this.d / 6 + 24 * (i2 >> 1) + 7, -1);
        }
        super.a(n2, n3, f2);
    }
}

