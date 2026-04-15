/*
 * Decompiled with CFR 0.152.
 */
public class ay
extends xa {
    public void a() {
        this.b = new hd(kd.m, 0.5, 0.0);
        this.g = 1;
    }

    public cl b() {
        return new jd(this.a, this.a.s());
    }

    public float a(long l2, float f2) {
        return 0.0f;
    }

    public float[] a(float f2, float f3) {
        return null;
    }

    public bt b(float f2, float f3) {
        int n2 = 0x8080A0;
        float f4 = in.b(f2 * (float)Math.PI * 2.0f) * 2.0f + 0.5f;
        if (f4 < 0.0f) {
            f4 = 0.0f;
        }
        if (f4 > 1.0f) {
            f4 = 1.0f;
        }
        float f5 = (float)(n2 >> 16 & 0xFF) / 255.0f;
        float f6 = (float)(n2 >> 8 & 0xFF) / 255.0f;
        float f7 = (float)(n2 & 0xFF) / 255.0f;
        return bt.b(f5 *= f4 * 0.94f + 0.06f, f6 *= f4 * 0.94f + 0.06f, f7 *= f4 * 0.91f + 0.09f);
    }

    public boolean c() {
        return false;
    }

    public float d() {
        return 8.0f;
    }

    public boolean a(int n2, int n3) {
        int n4 = this.a.a(n2, n3);
        if (n4 == 0) {
            return false;
        }
        return uu.m[n4].bA.c();
    }
}

