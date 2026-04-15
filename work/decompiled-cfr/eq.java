/*
 * Decompiled with CFR 0.152.
 */
import java.util.ArrayList;
import java.util.List;

public class eq {
    private static List g = new ArrayList();
    private static int h = 0;
    public double a;
    public double b;
    public double c;
    public double d;
    public double e;
    public double f;

    public static eq a(double d2, double d3, double d4, double d5, double d6, double d7) {
        return new eq(d2, d3, d4, d5, d6, d7);
    }

    public static void a() {
        g.clear();
        h = 0;
    }

    public static void b() {
        h = 0;
    }

    public static eq b(double d2, double d3, double d4, double d5, double d6, double d7) {
        if (h >= g.size()) {
            g.add(eq.a(0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
        }
        return ((eq)g.get(h++)).c(d2, d3, d4, d5, d6, d7);
    }

    private eq(double d2, double d3, double d4, double d5, double d6, double d7) {
        this.a = d2;
        this.b = d3;
        this.c = d4;
        this.d = d5;
        this.e = d6;
        this.f = d7;
    }

    public eq c(double d2, double d3, double d4, double d5, double d6, double d7) {
        this.a = d2;
        this.b = d3;
        this.c = d4;
        this.d = d5;
        this.e = d6;
        this.f = d7;
        return this;
    }

    public eq a(double d2, double d3, double d4) {
        double d5 = this.a;
        double d6 = this.b;
        double d7 = this.c;
        double d8 = this.d;
        double d9 = this.e;
        double d10 = this.f;
        if (d2 < 0.0) {
            d5 += d2;
        }
        if (d2 > 0.0) {
            d8 += d2;
        }
        if (d3 < 0.0) {
            d6 += d3;
        }
        if (d3 > 0.0) {
            d9 += d3;
        }
        if (d4 < 0.0) {
            d7 += d4;
        }
        if (d4 > 0.0) {
            d10 += d4;
        }
        return eq.b(d5, d6, d7, d8, d9, d10);
    }

    public eq b(double d2, double d3, double d4) {
        double d5 = this.a - d2;
        double d6 = this.b - d3;
        double d7 = this.c - d4;
        double d8 = this.d + d2;
        double d9 = this.e + d3;
        double d10 = this.f + d4;
        return eq.b(d5, d6, d7, d8, d9, d10);
    }

    public eq c(double d2, double d3, double d4) {
        return eq.b(this.a + d2, this.b + d3, this.c + d4, this.d + d2, this.e + d3, this.f + d4);
    }

    public double a(eq eq2, double d2) {
        double d3;
        if (eq2.e <= this.b || eq2.b >= this.e) {
            return d2;
        }
        if (eq2.f <= this.c || eq2.c >= this.f) {
            return d2;
        }
        if (d2 > 0.0 && eq2.d <= this.a && (d3 = this.a - eq2.d) < d2) {
            d2 = d3;
        }
        if (d2 < 0.0 && eq2.a >= this.d && (d3 = this.d - eq2.a) > d2) {
            d2 = d3;
        }
        return d2;
    }

    public double b(eq eq2, double d2) {
        double d3;
        if (eq2.d <= this.a || eq2.a >= this.d) {
            return d2;
        }
        if (eq2.f <= this.c || eq2.c >= this.f) {
            return d2;
        }
        if (d2 > 0.0 && eq2.e <= this.b && (d3 = this.b - eq2.e) < d2) {
            d2 = d3;
        }
        if (d2 < 0.0 && eq2.b >= this.e && (d3 = this.e - eq2.b) > d2) {
            d2 = d3;
        }
        return d2;
    }

    public double c(eq eq2, double d2) {
        double d3;
        if (eq2.d <= this.a || eq2.a >= this.d) {
            return d2;
        }
        if (eq2.e <= this.b || eq2.b >= this.e) {
            return d2;
        }
        if (d2 > 0.0 && eq2.f <= this.c && (d3 = this.c - eq2.f) < d2) {
            d2 = d3;
        }
        if (d2 < 0.0 && eq2.c >= this.f && (d3 = this.f - eq2.c) > d2) {
            d2 = d3;
        }
        return d2;
    }

    public boolean a(eq eq2) {
        if (eq2.d <= this.a || eq2.a >= this.d) {
            return false;
        }
        if (eq2.e <= this.b || eq2.b >= this.e) {
            return false;
        }
        return !(eq2.f <= this.c) && !(eq2.c >= this.f);
    }

    public eq d(double d2, double d3, double d4) {
        this.a += d2;
        this.b += d3;
        this.c += d4;
        this.d += d2;
        this.e += d3;
        this.f += d4;
        return this;
    }

    public boolean a(bt bt2) {
        if (bt2.a <= this.a || bt2.a >= this.d) {
            return false;
        }
        if (bt2.b <= this.b || bt2.b >= this.e) {
            return false;
        }
        return !(bt2.c <= this.c) && !(bt2.c >= this.f);
    }

    public double c() {
        double d2 = this.d - this.a;
        double d3 = this.e - this.b;
        double d4 = this.f - this.c;
        return (d2 + d3 + d4) / 3.0;
    }

    public eq e(double d2, double d3, double d4) {
        double d5 = this.a + d2;
        double d6 = this.b + d3;
        double d7 = this.c + d4;
        double d8 = this.d - d2;
        double d9 = this.e - d3;
        double d10 = this.f - d4;
        return eq.b(d5, d6, d7, d8, d9, d10);
    }

    public eq d() {
        return eq.b(this.a, this.b, this.c, this.d, this.e, this.f);
    }

    public vf a(bt bt2, bt bt3) {
        bt bt4 = bt2.a(bt3, this.a);
        bt bt5 = bt2.a(bt3, this.d);
        bt bt6 = bt2.b(bt3, this.b);
        bt bt7 = bt2.b(bt3, this.e);
        bt bt8 = bt2.c(bt3, this.c);
        bt bt9 = bt2.c(bt3, this.f);
        if (!this.b(bt4)) {
            bt4 = null;
        }
        if (!this.b(bt5)) {
            bt5 = null;
        }
        if (!this.c(bt6)) {
            bt6 = null;
        }
        if (!this.c(bt7)) {
            bt7 = null;
        }
        if (!this.d(bt8)) {
            bt8 = null;
        }
        if (!this.d(bt9)) {
            bt9 = null;
        }
        bt bt10 = null;
        if (bt4 != null && (bt10 == null || bt2.d(bt4) < bt2.d(bt10))) {
            bt10 = bt4;
        }
        if (bt5 != null && (bt10 == null || bt2.d(bt5) < bt2.d(bt10))) {
            bt10 = bt5;
        }
        if (bt6 != null && (bt10 == null || bt2.d(bt6) < bt2.d(bt10))) {
            bt10 = bt6;
        }
        if (bt7 != null && (bt10 == null || bt2.d(bt7) < bt2.d(bt10))) {
            bt10 = bt7;
        }
        if (bt8 != null && (bt10 == null || bt2.d(bt8) < bt2.d(bt10))) {
            bt10 = bt8;
        }
        if (bt9 != null && (bt10 == null || bt2.d(bt9) < bt2.d(bt10))) {
            bt10 = bt9;
        }
        if (bt10 == null) {
            return null;
        }
        int n2 = -1;
        if (bt10 == bt4) {
            n2 = 4;
        }
        if (bt10 == bt5) {
            n2 = 5;
        }
        if (bt10 == bt6) {
            n2 = 0;
        }
        if (bt10 == bt7) {
            n2 = 1;
        }
        if (bt10 == bt8) {
            n2 = 2;
        }
        if (bt10 == bt9) {
            n2 = 3;
        }
        return new vf(0, 0, 0, n2, bt10);
    }

    private boolean b(bt bt2) {
        if (bt2 == null) {
            return false;
        }
        return bt2.b >= this.b && bt2.b <= this.e && bt2.c >= this.c && bt2.c <= this.f;
    }

    private boolean c(bt bt2) {
        if (bt2 == null) {
            return false;
        }
        return bt2.a >= this.a && bt2.a <= this.d && bt2.c >= this.c && bt2.c <= this.f;
    }

    private boolean d(bt bt2) {
        if (bt2 == null) {
            return false;
        }
        return bt2.a >= this.a && bt2.a <= this.d && bt2.b >= this.b && bt2.b <= this.e;
    }

    public void b(eq eq2) {
        this.a = eq2.a;
        this.b = eq2.b;
        this.c = eq2.c;
        this.d = eq2.d;
        this.e = eq2.e;
        this.f = eq2.f;
    }

    public String toString() {
        return "box[" + this.a + ", " + this.b + ", " + this.c + " -> " + this.d + ", " + this.e + ", " + this.f + "]";
    }
}

