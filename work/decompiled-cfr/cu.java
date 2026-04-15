/*
 * Decompiled with CFR 0.152.
 */
public class cu {
    private float a;
    private float b;
    private float c;

    public float a(float f2, float f3) {
        this.a += f2;
        f2 = (this.a - this.b) * f3;
        this.c += (f2 - this.c) * 0.5f;
        if (f2 > 0.0f && f2 > this.c || f2 < 0.0f && f2 < this.c) {
            f2 = this.c;
        }
        this.b += f2;
        return f2;
    }
}

