/*
 * Decompiled with CFR 0.152.
 */
public class tz {
    public ib[] a;
    public int b = 0;
    private boolean c = false;

    public tz(ib[] ibArray) {
        this.a = ibArray;
        this.b = ibArray.length;
    }

    public tz(ib[] ibArray, int n2, int n3, int n4, int n5) {
        this(ibArray);
        float f2 = 0.0015625f;
        float f3 = 0.003125f;
        ibArray[0] = ibArray[0].a((float)n4 / 64.0f - f2, (float)n3 / 32.0f + f3);
        ibArray[1] = ibArray[1].a((float)n2 / 64.0f + f2, (float)n3 / 32.0f + f3);
        ibArray[2] = ibArray[2].a((float)n2 / 64.0f + f2, (float)n5 / 32.0f - f3);
        ibArray[3] = ibArray[3].a((float)n4 / 64.0f - f2, (float)n5 / 32.0f - f3);
    }

    public void a() {
        ib[] ibArray = new ib[this.a.length];
        for (int i2 = 0; i2 < this.a.length; ++i2) {
            ibArray[i2] = this.a[this.a.length - i2 - 1];
        }
        this.a = ibArray;
    }

    public void a(nw nw2, float f2) {
        bt bt2 = this.a[1].a.a(this.a[0].a);
        bt bt3 = this.a[1].a.a(this.a[2].a);
        bt bt4 = bt3.b(bt2).c();
        nw2.b();
        if (this.c) {
            nw2.b(-((float)bt4.a), -((float)bt4.b), -((float)bt4.c));
        } else {
            nw2.b((float)bt4.a, (float)bt4.b, (float)bt4.c);
        }
        for (int i2 = 0; i2 < 4; ++i2) {
            ib ib2 = this.a[i2];
            nw2.a((float)ib2.a.a * f2, (float)ib2.a.b * f2, (float)ib2.a.c * f2, ib2.b, ib2.c);
        }
        nw2.a();
    }
}

