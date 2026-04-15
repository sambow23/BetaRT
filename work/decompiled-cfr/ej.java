/*
 * Decompiled with CFR 0.152.
 */
public class ej
extends fh {
    public void b(float f2, float f3, float f4, float f5, float f6, float f7) {
        super.b(f2, f3, f4, f5, f6, f7);
        float f8 = in.a(this.m * (float)Math.PI);
        float f9 = in.a((1.0f - (1.0f - this.m) * (1.0f - this.m)) * (float)Math.PI);
        this.d.f = 0.0f;
        this.e.f = 0.0f;
        this.d.e = -(0.1f - f8 * 0.6f);
        this.e.e = 0.1f - f8 * 0.6f;
        this.d.d = -1.5707964f;
        this.e.d = -1.5707964f;
        this.d.d -= f8 * 1.2f - f9 * 0.4f;
        this.e.d -= f8 * 1.2f - f9 * 0.4f;
        this.d.f += in.b(f4 * 0.09f) * 0.05f + 0.05f;
        this.e.f -= in.b(f4 * 0.09f) * 0.05f + 0.05f;
        this.d.d += in.a(f4 * 0.067f) * 0.05f;
        this.e.d -= in.a(f4 * 0.067f) * 0.05f;
    }
}

