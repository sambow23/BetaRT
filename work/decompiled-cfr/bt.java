/*
 * Decompiled with CFR 0.152.
 */
import java.util.ArrayList;
import java.util.List;

public class bt {
    private static List d = new ArrayList();
    private static int e = 0;
    public double a;
    public double b;
    public double c;

    public static bt a(double d2, double d3, double d4) {
        return new bt(d2, d3, d4);
    }

    public static void a() {
        d.clear();
        e = 0;
    }

    public static void b() {
        e = 0;
    }

    public static bt b(double d2, double d3, double d4) {
        if (e >= d.size()) {
            d.add(bt.a(0.0, 0.0, 0.0));
        }
        return ((bt)d.get(e++)).e(d2, d3, d4);
    }

    private bt(double d2, double d3, double d4) {
        if (d2 == -0.0) {
            d2 = 0.0;
        }
        if (d3 == -0.0) {
            d3 = 0.0;
        }
        if (d4 == -0.0) {
            d4 = 0.0;
        }
        this.a = d2;
        this.b = d3;
        this.c = d4;
    }

    private bt e(double d2, double d3, double d4) {
        this.a = d2;
        this.b = d3;
        this.c = d4;
        return this;
    }

    public bt a(bt bt2) {
        return bt.b(bt2.a - this.a, bt2.b - this.b, bt2.c - this.c);
    }

    public bt c() {
        double d2 = in.a(this.a * this.a + this.b * this.b + this.c * this.c);
        if (d2 < 1.0E-4) {
            return bt.b(0.0, 0.0, 0.0);
        }
        return bt.b(this.a / d2, this.b / d2, this.c / d2);
    }

    public bt b(bt bt2) {
        return bt.b(this.b * bt2.c - this.c * bt2.b, this.c * bt2.a - this.a * bt2.c, this.a * bt2.b - this.b * bt2.a);
    }

    public bt c(double d2, double d3, double d4) {
        return bt.b(this.a + d2, this.b + d3, this.c + d4);
    }

    public double c(bt bt2) {
        double d2 = bt2.a - this.a;
        double d3 = bt2.b - this.b;
        double d4 = bt2.c - this.c;
        return in.a(d2 * d2 + d3 * d3 + d4 * d4);
    }

    public double d(bt bt2) {
        double d2 = bt2.a - this.a;
        double d3 = bt2.b - this.b;
        double d4 = bt2.c - this.c;
        return d2 * d2 + d3 * d3 + d4 * d4;
    }

    public double d(double d2, double d3, double d4) {
        double d5 = d2 - this.a;
        double d6 = d3 - this.b;
        double d7 = d4 - this.c;
        return d5 * d5 + d6 * d6 + d7 * d7;
    }

    public double d() {
        return in.a(this.a * this.a + this.b * this.b + this.c * this.c);
    }

    public bt a(bt bt2, double d2) {
        double d3 = bt2.a - this.a;
        double d4 = bt2.b - this.b;
        double d5 = bt2.c - this.c;
        if (d3 * d3 < (double)1.0E-7f) {
            return null;
        }
        double d6 = (d2 - this.a) / d3;
        if (d6 < 0.0 || d6 > 1.0) {
            return null;
        }
        return bt.b(this.a + d3 * d6, this.b + d4 * d6, this.c + d5 * d6);
    }

    public bt b(bt bt2, double d2) {
        double d3 = bt2.a - this.a;
        double d4 = bt2.b - this.b;
        double d5 = bt2.c - this.c;
        if (d4 * d4 < (double)1.0E-7f) {
            return null;
        }
        double d6 = (d2 - this.b) / d4;
        if (d6 < 0.0 || d6 > 1.0) {
            return null;
        }
        return bt.b(this.a + d3 * d6, this.b + d4 * d6, this.c + d5 * d6);
    }

    public bt c(bt bt2, double d2) {
        double d3 = bt2.a - this.a;
        double d4 = bt2.b - this.b;
        double d5 = bt2.c - this.c;
        if (d5 * d5 < (double)1.0E-7f) {
            return null;
        }
        double d6 = (d2 - this.c) / d5;
        if (d6 < 0.0 || d6 > 1.0) {
            return null;
        }
        return bt.b(this.a + d3 * d6, this.b + d4 * d6, this.c + d5 * d6);
    }

    public String toString() {
        return "(" + this.a + ", " + this.b + ", " + this.c + ")";
    }

    public void a(float f2) {
        float f3 = in.b(f2);
        float f4 = in.a(f2);
        double d2 = this.a;
        double d3 = this.b * (double)f3 + this.c * (double)f4;
        double d4 = this.c * (double)f3 - this.b * (double)f4;
        this.a = d2;
        this.b = d3;
        this.c = d4;
    }

    public void b(float f2) {
        float f3 = in.b(f2);
        float f4 = in.a(f2);
        double d2 = this.a * (double)f3 + this.c * (double)f4;
        double d3 = this.b;
        double d4 = this.c * (double)f3 - this.a * (double)f4;
        this.a = d2;
        this.b = d3;
        this.c = d4;
    }
}

