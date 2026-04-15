/*
 * Decompiled with CFR 0.152.
 */
import java.util.Random;

public class xv {
    private ug e;
    private ug f;
    private ug g;
    public double[] a;
    public double[] b;
    public double[] c;
    public kd[] d;

    protected xv() {
    }

    public xv(fd fd2) {
        this.e = new ug(new Random(fd2.s() * 9871L), 4);
        this.f = new ug(new Random(fd2.s() * 39811L), 4);
        this.g = new ug(new Random(fd2.s() * 543321L), 2);
    }

    public kd a(yy yy2) {
        return this.a(yy2.a << 4, yy2.b << 4);
    }

    public kd a(int n2, int n3) {
        return this.a(n2, n3, 1, 1)[0];
    }

    public double b(int n2, int n3) {
        this.a = this.e.a(this.a, n2, n3, 1, 1, 0.025f, 0.025f, 0.5);
        return this.a[0];
    }

    public kd[] a(int n2, int n3, int n4, int n5) {
        this.d = this.a(this.d, n2, n3, n4, n5);
        return this.d;
    }

    public double[] a(double[] dArray, int n2, int n3, int n4, int n5) {
        if (dArray == null || dArray.length < n4 * n5) {
            dArray = new double[n4 * n5];
        }
        dArray = this.e.a(dArray, n2, n3, n4, n5, 0.025f, 0.025f, 0.25);
        this.c = this.g.a(this.c, n2, n3, n4, n5, 0.25, 0.25, 0.5882352941176471);
        int n6 = 0;
        for (int i2 = 0; i2 < n4; ++i2) {
            for (int i3 = 0; i3 < n5; ++i3) {
                double d2 = this.c[n6] * 1.1 + 0.5;
                double d3 = 0.01;
                double d4 = 1.0 - d3;
                double d5 = (dArray[n6] * 0.15 + 0.7) * d4 + d2 * d3;
                if ((d5 = 1.0 - (1.0 - d5) * (1.0 - d5)) < 0.0) {
                    d5 = 0.0;
                }
                if (d5 > 1.0) {
                    d5 = 1.0;
                }
                dArray[n6] = d5;
                ++n6;
            }
        }
        return dArray;
    }

    public kd[] a(kd[] kdArray, int n2, int n3, int n4, int n5) {
        if (kdArray == null || kdArray.length < n4 * n5) {
            kdArray = new kd[n4 * n5];
        }
        this.a = this.e.a(this.a, n2, n3, n4, n4, 0.025f, 0.025f, 0.25);
        this.b = this.f.a(this.b, n2, n3, n4, n4, 0.05f, 0.05f, 0.3333333333333333);
        this.c = this.g.a(this.c, n2, n3, n4, n4, 0.25, 0.25, 0.5882352941176471);
        int n6 = 0;
        for (int i2 = 0; i2 < n4; ++i2) {
            for (int i3 = 0; i3 < n5; ++i3) {
                double d2 = this.c[n6] * 1.1 + 0.5;
                double d3 = 0.01;
                double d4 = 1.0 - d3;
                double d5 = (this.a[n6] * 0.15 + 0.7) * d4 + d2 * d3;
                d3 = 0.002;
                d4 = 1.0 - d3;
                double d6 = (this.b[n6] * 0.15 + 0.5) * d4 + d2 * d3;
                if ((d5 = 1.0 - (1.0 - d5) * (1.0 - d5)) < 0.0) {
                    d5 = 0.0;
                }
                if (d6 < 0.0) {
                    d6 = 0.0;
                }
                if (d5 > 1.0) {
                    d5 = 1.0;
                }
                if (d6 > 1.0) {
                    d6 = 1.0;
                }
                this.a[n6] = d5;
                this.b[n6] = d6;
                kdArray[n6++] = kd.a(d5, d6);
            }
        }
        return kdArray;
    }
}

