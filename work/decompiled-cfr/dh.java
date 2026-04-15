/*
 * Decompiled with CFR 0.152.
 */
public class dh {
    private final d[] b;
    public final int a;
    private int c;

    public dh(d[] dArray) {
        this.b = dArray;
        this.a = dArray.length;
    }

    public void a() {
        ++this.c;
    }

    public boolean b() {
        return this.c >= this.b.length;
    }

    public d c() {
        if (this.a > 0) {
            return this.b[this.a - 1];
        }
        return null;
    }

    public bt a(sn sn2) {
        double d2 = (double)this.b[this.c].a + (double)((int)(sn2.bg + 1.0f)) * 0.5;
        double d3 = this.b[this.c].b;
        double d4 = (double)this.b[this.c].c + (double)((int)(sn2.bg + 1.0f)) * 0.5;
        return bt.b(d2, d3, d4);
    }
}

