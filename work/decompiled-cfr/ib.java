/*
 * Decompiled with CFR 0.152.
 */
public class ib {
    public bt a;
    public float b;
    public float c;

    public ib(float f2, float f3, float f4, float f5, float f6) {
        this(bt.a(f2, f3, f4), f5, f6);
    }

    public ib a(float f2, float f3) {
        return new ib(this, f2, f3);
    }

    public ib(ib ib2, float f2, float f3) {
        this.a = ib2.a;
        this.b = f2;
        this.c = f3;
    }

    public ib(bt bt2, float f2, float f3) {
        this.a = bt2;
        this.b = f2;
        this.c = f3;
    }
}

