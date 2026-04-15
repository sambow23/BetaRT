/*
 * Decompiled with CFR 0.152.
 */
public class ip
extends gv {
    public ip(ko ko2, float f2) {
        super(ko2, f2);
    }

    public void a(ww ww2, double d2, double d3, double d4, float f2, float f3) {
        super.a((ls)ww2, d2, d3, d4, f2, f3);
    }

    protected float a(ww ww2, float f2) {
        float f3 = ww2.g + (ww2.b - ww2.g) * f2;
        float f4 = ww2.f + (ww2.c - ww2.f) * f2;
        return (in.a(f3) + 1.0f) * f4;
    }
}

