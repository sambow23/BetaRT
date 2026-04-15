/*
 * Decompiled with CFR 0.152.
 */
public class lf {
    private d[] a = new d[1024];
    private int b = 0;

    public d a(d d2) {
        if (d2.d >= 0) {
            throw new IllegalStateException("OW KNOWS!");
        }
        if (this.b == this.a.length) {
            d[] dArray = new d[this.b << 1];
            System.arraycopy(this.a, 0, dArray, 0, this.b);
            this.a = dArray;
        }
        this.a[this.b] = d2;
        d2.d = this.b;
        this.a(this.b++);
        return d2;
    }

    public void a() {
        this.b = 0;
    }

    public d b() {
        d d2 = this.a[0];
        this.a[0] = this.a[--this.b];
        this.a[this.b] = null;
        if (this.b > 0) {
            this.b(0);
        }
        d2.d = -1;
        return d2;
    }

    public void a(d d2, float f2) {
        float f3 = d2.g;
        d2.g = f2;
        if (f2 < f3) {
            this.a(d2.d);
        } else {
            this.b(d2.d);
        }
    }

    private void a(int n2) {
        d d2 = this.a[n2];
        float f2 = d2.g;
        while (n2 > 0) {
            int n3 = n2 - 1 >> 1;
            d d3 = this.a[n3];
            if (!(f2 < d3.g)) break;
            this.a[n2] = d3;
            d3.d = n2;
            n2 = n3;
        }
        this.a[n2] = d2;
        d2.d = n2;
    }

    private void b(int n2) {
        d d2 = this.a[n2];
        float f2 = d2.g;
        while (true) {
            float f3;
            d d3;
            int n3 = 1 + (n2 << 1);
            int n4 = n3 + 1;
            if (n3 >= this.b) break;
            d d4 = this.a[n3];
            float f4 = d4.g;
            if (n4 >= this.b) {
                d3 = null;
                f3 = Float.POSITIVE_INFINITY;
            } else {
                d3 = this.a[n4];
                f3 = d3.g;
            }
            if (f4 < f3) {
                if (!(f4 < f2)) break;
                this.a[n2] = d4;
                d4.d = n2;
                n2 = n3;
                continue;
            }
            if (!(f3 < f2)) break;
            this.a[n2] = d3;
            d3.d = n2;
            n2 = n4;
        }
        this.a[n2] = d2;
        d2.d = n2;
    }

    public boolean c() {
        return this.b == 0;
    }
}

