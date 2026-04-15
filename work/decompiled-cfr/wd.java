/*
 * Decompiled with CFR 0.152.
 */
public class wd
extends xa {
    public void a() {
        this.b = new hd(kd.l, 1.0, 0.0);
        this.c = true;
        this.d = true;
        this.e = true;
        this.g = -1;
    }

    public bt b(float f2, float f3) {
        return bt.b(0.2f, 0.03f, 0.03f);
    }

    protected void e() {
        float f2 = 0.1f;
        for (int i2 = 0; i2 <= 15; ++i2) {
            float f3 = 1.0f - (float)i2 / 15.0f;
            this.f[i2] = (1.0f - f3) / (f3 * 3.0f + 1.0f) * (1.0f - f2) + f2;
        }
    }

    public cl b() {
        return new qn(this.a, this.a.s());
    }

    public boolean a(int n2, int n3) {
        int n4 = this.a.a(n2, n3);
        if (n4 == uu.A.bn) {
            return false;
        }
        if (n4 == 0) {
            return false;
        }
        return uu.o[n4];
    }

    public float a(long l2, float f2) {
        return 0.5f;
    }

    public boolean f() {
        return false;
    }
}

