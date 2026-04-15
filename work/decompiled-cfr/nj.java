/*
 * Decompiled with CFR 0.152.
 */
public class nj
extends da {
    private da i;
    protected String a = "Video Settings";
    private kv j;
    private static ht[] l = new ht[]{ht.k, ht.e, ht.l, ht.i, ht.g, ht.f, ht.m, ht.h};

    public nj(da da2, kv kv2) {
        this.i = da2;
        this.j = kv2;
    }

    public void b() {
        nh nh2 = nh.a();
        this.a = nh2.a("options.videoTitle");
        int n2 = 0;
        for (ht ht2 : l) {
            if (!ht2.a()) {
                this.e.add(new ab(ht2.c(), this.c / 2 - 155 + n2 % 2 * 160, this.d / 6 + 24 * (n2 >> 1), ht2, this.j.c(ht2)));
            } else {
                this.e.add(new vz(ht2.c(), this.c / 2 - 155 + n2 % 2 * 160, this.d / 6 + 24 * (n2 >> 1), ht2, this.j.c(ht2), this.j.a(ht2)));
            }
            ++n2;
        }
        this.e.add(new ke(200, this.c / 2 - 100, this.d / 6 + 168, nh2.a("gui.done")));
    }

    protected void a(ke ke2) {
        if (!ke2.g) {
            return;
        }
        if (ke2.f < 100 && ke2 instanceof ab) {
            this.j.a(((ab)ke2).a(), 1);
            ke2.e = this.j.c(ht.a(ke2.f));
        }
        if (ke2.f == 200) {
            this.b.z.b();
            this.b.a(this.i);
        }
        qq qq2 = new qq(this.b.z, this.b.d, this.b.e);
        int n2 = qq2.a();
        int n3 = qq2.b();
        this.a(this.b, n2, n3);
    }

    public void a(int n2, int n3, float f2) {
        this.i();
        this.a(this.g, this.a, this.c / 2, 20, 0xFFFFFF);
        super.a(n2, n3, f2);
    }
}

