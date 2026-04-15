/*
 * Decompiled with CFR 0.152.
 */
public class sr
implements yn {
    private dr a = w.a();
    private double b;
    private double c;
    private double d;

    public void a(double d2, double d3, double d4) {
        this.b = d2;
        this.c = d3;
        this.d = d4;
    }

    public boolean a(double d2, double d3, double d4, double d5, double d6, double d7) {
        return this.a.a(d2 - this.b, d3 - this.c, d4 - this.d, d5 - this.b, d6 - this.c, d7 - this.d);
    }

    public boolean a(eq eq2) {
        return this.a(eq2.a, eq2.b, eq2.c, eq2.d, eq2.e, eq2.f);
    }
}

