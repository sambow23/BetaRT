/*
 * Decompiled with CFR 0.152.
 */
import java.util.Arrays;

public class hd
extends xv {
    private kd e;
    private double f;
    private double g;

    public hd(kd kd2, double d2, double d3) {
        this.e = kd2;
        this.f = d2;
        this.g = d3;
    }

    public kd a(yy yy2) {
        return this.e;
    }

    public kd a(int n2, int n3) {
        return this.e;
    }

    public double b(int n2, int n3) {
        return this.f;
    }

    public kd[] a(int n2, int n3, int n4, int n5) {
        this.d = this.a(this.d, n2, n3, n4, n5);
        return this.d;
    }

    public double[] a(double[] dArray, int n2, int n3, int n4, int n5) {
        if (dArray == null || dArray.length < n4 * n5) {
            dArray = new double[n4 * n5];
        }
        Arrays.fill(dArray, 0, n4 * n5, this.f);
        return dArray;
    }

    public kd[] a(kd[] kdArray, int n2, int n3, int n4, int n5) {
        if (kdArray == null || kdArray.length < n4 * n5) {
            kdArray = new kd[n4 * n5];
        }
        if (this.a == null || this.a.length < n4 * n5) {
            this.a = new double[n4 * n5];
            this.b = new double[n4 * n5];
        }
        Arrays.fill(kdArray, 0, n4 * n5, this.e);
        Arrays.fill(this.b, 0, n4 * n5, this.g);
        Arrays.fill(this.a, 0, n4 * n5, this.f);
        return kdArray;
    }
}

