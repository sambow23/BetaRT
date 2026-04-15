/*
 * Decompiled with CFR 0.152.
 */
import java.util.Random;

public class ug
extends df {
    private cc[] a;
    private int b;

    public ug(Random random, int n2) {
        this.b = n2;
        this.a = new cc[n2];
        for (int i2 = 0; i2 < n2; ++i2) {
            this.a[i2] = new cc(random);
        }
    }

    public double[] a(double[] dArray, double d2, double d3, int n2, int n3, double d4, double d5, double d6) {
        return this.a(dArray, d2, d3, n2, n3, d4, d5, d6, 0.5);
    }

    public double[] a(double[] dArray, double d2, double d3, int n2, int n3, double d4, double d5, double d6, double d7) {
        d4 /= 1.5;
        d5 /= 1.5;
        if (dArray == null || dArray.length < n2 * n3) {
            dArray = new double[n2 * n3];
        } else {
            for (int i2 = 0; i2 < dArray.length; ++i2) {
                dArray[i2] = 0.0;
            }
        }
        double d8 = 1.0;
        double d9 = 1.0;
        for (int i3 = 0; i3 < this.b; ++i3) {
            this.a[i3].a(dArray, d2, d3, n2, n3, d4 * d9, d5 * d9, 0.55 / d8);
            d9 *= d6;
            d8 *= d7;
        }
        return dArray;
    }
}

