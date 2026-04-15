/*
 * Decompiled with CFR 0.152.
 */
import java.util.Random;

public class uf
extends df {
    private am[] a;
    private int b;

    public uf(Random random, int n2) {
        this.b = n2;
        this.a = new am[n2];
        for (int i2 = 0; i2 < n2; ++i2) {
            this.a[i2] = new am(random);
        }
    }

    public double a(double d2, double d3) {
        double d4 = 0.0;
        double d5 = 1.0;
        for (int i2 = 0; i2 < this.b; ++i2) {
            d4 += this.a[i2].a(d2 * d5, d3 * d5) / d5;
            d5 /= 2.0;
        }
        return d4;
    }

    public double[] a(double[] dArray, double d2, double d3, double d4, int n2, int n3, int n4, double d5, double d6, double d7) {
        if (dArray == null) {
            dArray = new double[n2 * n3 * n4];
        } else {
            for (int i2 = 0; i2 < dArray.length; ++i2) {
                dArray[i2] = 0.0;
            }
        }
        double d8 = 1.0;
        for (int i3 = 0; i3 < this.b; ++i3) {
            this.a[i3].a(dArray, d2, d3, d4, n2, n3, n4, d5 * d8, d6 * d8, d7 * d8, d8);
            d8 /= 2.0;
        }
        return dArray;
    }

    public double[] a(double[] dArray, int n2, int n3, int n4, int n5, double d2, double d3, double d4) {
        return this.a(dArray, n2, 10.0, n3, n4, 1, n5, d2, 1.0, d3);
    }
}

