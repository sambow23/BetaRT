/*
 * Decompiled with CFR 0.152.
 */
public class oz
extends da {
    private int a = 0;
    private int i = 0;

    public void b() {
        this.a = 0;
        this.e.clear();
        int n2 = -16;
        this.e.add(new ke(1, this.c / 2 - 100, this.d / 4 + 120 + n2, "Save and quit to title"));
        if (this.b.l()) {
            ((ke)this.e.get((int)0)).e = "Disconnect";
        }
        this.e.add(new ke(4, this.c / 2 - 100, this.d / 4 + 24 + n2, "Back to game"));
        this.e.add(new ke(0, this.c / 2 - 100, this.d / 4 + 96 + n2, "Options..."));
        this.e.add(new ke(5, this.c / 2 - 100, this.d / 4 + 48 + n2, 98, 20, do.a("gui.achievements")));
        this.e.add(new ke(6, this.c / 2 + 2, this.d / 4 + 48 + n2, 98, 20, do.a("gui.stats")));
    }

    protected void a(ke ke2) {
        if (ke2.f == 0) {
            this.b.a(new co(this, this.b.z));
        }
        if (ke2.f == 1) {
            this.b.I.a(jl.j, 1);
            if (this.b.l()) {
                this.b.f.q();
            }
            this.b.a((fd)null);
            this.b.a(new fu());
        }
        if (ke2.f == 4) {
            this.b.a((da)null);
            this.b.g();
        }
        if (ke2.f == 5) {
            this.b.a(new xm(this.b.I));
        }
        if (ke2.f == 6) {
            this.b.a(new dv(this, this.b.I));
        }
    }

    public void a() {
        super.a();
        ++this.i;
    }

    public void a(int n2, int n3, float f2) {
        boolean bl2;
        this.i();
        boolean bl3 = bl2 = !this.b.f.a(this.a++);
        if (bl2 || this.i < 20) {
            float f3 = ((float)(this.i % 10) + f2) / 10.0f;
            f3 = in.a(f3 * (float)Math.PI * 2.0f) * 0.2f + 0.8f;
            int n4 = (int)(255.0f * f3);
            this.b(this.g, "Saving level..", 8, this.d - 16, n4 << 16 | n4 << 8 | n4);
        }
        this.a(this.g, "Game menu", this.c / 2, 40, 0xFFFFFF);
        super.a(n2, n3, f2);
    }
}

