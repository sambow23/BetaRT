/*
 * Decompiled with CFR 0.152.
 */
public abstract class xa {
    public fd a;
    public xv b;
    public boolean c = false;
    public boolean d = false;
    public boolean e = false;
    public float[] f = new float[16];
    public int g = 0;
    private float[] h = new float[4];

    public final void a(fd fd2) {
        this.a = fd2;
        this.a();
        this.e();
    }

    protected void e() {
        float f2 = 0.05f;
        for (int i2 = 0; i2 <= 15; ++i2) {
            float f3 = 1.0f - (float)i2 / 15.0f;
            this.f[i2] = (1.0f - f3) / (f3 * 3.0f + 1.0f) * (1.0f - f2) + f2;
        }
    }

    protected void a() {
        this.b = new xv(this.a);
    }

    public cl b() {
        return new yf(this.a, this.a.s());
    }

    public boolean a(int n2, int n3) {
        int n4 = this.a.a(n2, n3);
        return n4 == uu.F.bn;
    }

    public float a(long l2, float f2) {
        int n2 = (int)(l2 % 24000L);
        float f3 = ((float)n2 + f2) / 24000.0f - 0.25f;
        if (f3 < 0.0f) {
            f3 += 1.0f;
        }
        if (f3 > 1.0f) {
            f3 -= 1.0f;
        }
        float f4 = f3;
        f3 = 1.0f - (float)((Math.cos((double)f3 * Math.PI) + 1.0) / 2.0);
        f3 = f4 + (f3 - f4) / 3.0f;
        return f3;
    }

    public float[] a(float f2, float f3) {
        float f4;
        float f5 = 0.4f;
        float f6 = in.b(f2 * (float)Math.PI * 2.0f) - 0.0f;
        if (f6 >= (f4 = -0.0f) - f5 && f6 <= f4 + f5) {
            float f7 = (f6 - f4) / f5 * 0.5f + 0.5f;
            float f8 = 1.0f - (1.0f - in.a(f7 * (float)Math.PI)) * 0.99f;
            f8 *= f8;
            this.h[0] = f7 * 0.3f + 0.7f;
            this.h[1] = f7 * f7 * 0.7f + 0.2f;
            this.h[2] = f7 * f7 * 0.0f + 0.2f;
            this.h[3] = f8;
            return this.h;
        }
        return null;
    }

    public bt b(float f2, float f3) {
        float f4 = in.b(f2 * (float)Math.PI * 2.0f) * 2.0f + 0.5f;
        if (f4 < 0.0f) {
            f4 = 0.0f;
        }
        if (f4 > 1.0f) {
            f4 = 1.0f;
        }
        float f5 = 0.7529412f;
        float f6 = 0.84705883f;
        float f7 = 1.0f;
        return bt.b(f5 *= f4 * 0.94f + 0.06f, f6 *= f4 * 0.94f + 0.06f, f7 *= f4 * 0.91f + 0.09f);
    }

    public boolean f() {
        return true;
    }

    public static xa a(int n2) {
        if (n2 == -1) {
            return new wd();
        }
        if (n2 == 0) {
            return new rh();
        }
        if (n2 == 1) {
            return new ay();
        }
        return null;
    }

    public float d() {
        return 108.0f;
    }

    public boolean c() {
        return true;
    }
}

