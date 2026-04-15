/*
 * Decompiled with CFR 0.152.
 */
import java.util.Random;

public class am
extends df {
    private int[] d = new int[512];
    public double a;
    public double b;
    public double c;

    public am() {
        this(new Random());
    }

    public am(Random random) {
        int n2;
        this.a = random.nextDouble() * 256.0;
        this.b = random.nextDouble() * 256.0;
        this.c = random.nextDouble() * 256.0;
        for (n2 = 0; n2 < 256; ++n2) {
            this.d[n2] = n2;
        }
        for (n2 = 0; n2 < 256; ++n2) {
            int n3 = random.nextInt(256 - n2) + n2;
            int n4 = this.d[n2];
            this.d[n2] = this.d[n3];
            this.d[n3] = n4;
            this.d[n2 + 256] = this.d[n2];
        }
    }

    public double a(double d2, double d3, double d4) {
        double d5 = d2 + this.a;
        double d6 = d3 + this.b;
        double d7 = d4 + this.c;
        int n2 = (int)d5;
        int n3 = (int)d6;
        int n4 = (int)d7;
        if (d5 < (double)n2) {
            --n2;
        }
        if (d6 < (double)n3) {
            --n3;
        }
        if (d7 < (double)n4) {
            --n4;
        }
        int n5 = n2 & 0xFF;
        int n6 = n3 & 0xFF;
        int n7 = n4 & 0xFF;
        double d8 = (d5 -= (double)n2) * d5 * d5 * (d5 * (d5 * 6.0 - 15.0) + 10.0);
        double d9 = (d6 -= (double)n3) * d6 * d6 * (d6 * (d6 * 6.0 - 15.0) + 10.0);
        double d10 = (d7 -= (double)n4) * d7 * d7 * (d7 * (d7 * 6.0 - 15.0) + 10.0);
        int n8 = this.d[n5] + n6;
        int n9 = this.d[n8] + n7;
        int n10 = this.d[n8 + 1] + n7;
        int n11 = this.d[n5 + 1] + n6;
        int n12 = this.d[n11] + n7;
        int n13 = this.d[n11 + 1] + n7;
        return this.b(d10, this.b(d9, this.b(d8, this.a(this.d[n9], d5, d6, d7), this.a(this.d[n12], d5 - 1.0, d6, d7)), this.b(d8, this.a(this.d[n10], d5, d6 - 1.0, d7), this.a(this.d[n13], d5 - 1.0, d6 - 1.0, d7))), this.b(d9, this.b(d8, this.a(this.d[n9 + 1], d5, d6, d7 - 1.0), this.a(this.d[n12 + 1], d5 - 1.0, d6, d7 - 1.0)), this.b(d8, this.a(this.d[n10 + 1], d5, d6 - 1.0, d7 - 1.0), this.a(this.d[n13 + 1], d5 - 1.0, d6 - 1.0, d7 - 1.0))));
    }

    public final double b(double d2, double d3, double d4) {
        return d3 + d2 * (d4 - d3);
    }

    public final double a(int n2, double d2, double d3) {
        int n3 = n2 & 0xF;
        double d4 = (double)(1 - ((n3 & 8) >> 3)) * d2;
        double d5 = n3 < 4 ? 0.0 : (n3 == 12 || n3 == 14 ? d2 : d3);
        return ((n3 & 1) == 0 ? d4 : -d4) + ((n3 & 2) == 0 ? d5 : -d5);
    }

    public final double a(int n2, double d2, double d3, double d4) {
        double d5;
        int n3 = n2 & 0xF;
        double d6 = d5 = n3 < 8 ? d2 : d3;
        double d7 = n3 < 4 ? d3 : (n3 == 12 || n3 == 14 ? d2 : d4);
        return ((n3 & 1) == 0 ? d5 : -d5) + ((n3 & 2) == 0 ? d7 : -d7);
    }

    public double a(double d2, double d3) {
        return this.a(d2, d3, 0.0);
    }

    public void a(double[] dArray, double d2, double d3, double d4, int n2, int n3, int n4, double d5, double d6, double d7, double d8) {
        if (n3 == 1) {
            int n5 = 0;
            int n6 = 0;
            int n7 = 0;
            int n8 = 0;
            double d9 = 0.0;
            double d10 = 0.0;
            int n9 = 0;
            double d11 = 1.0 / d8;
            for (int i2 = 0; i2 < n2; ++i2) {
                double d12 = (d2 + (double)i2) * d5 + this.a;
                int n10 = (int)d12;
                if (d12 < (double)n10) {
                    --n10;
                }
                int n11 = n10 & 0xFF;
                double d13 = (d12 -= (double)n10) * d12 * d12 * (d12 * (d12 * 6.0 - 15.0) + 10.0);
                for (int i3 = 0; i3 < n4; ++i3) {
                    double d14 = (d4 + (double)i3) * d7 + this.c;
                    int n12 = (int)d14;
                    if (d14 < (double)n12) {
                        --n12;
                    }
                    int n13 = n12 & 0xFF;
                    double d15 = (d14 -= (double)n12) * d14 * d14 * (d14 * (d14 * 6.0 - 15.0) + 10.0);
                    n5 = this.d[n11] + 0;
                    n6 = this.d[n5] + n13;
                    n7 = this.d[n11 + 1] + 0;
                    n8 = this.d[n7] + n13;
                    d9 = this.b(d13, this.a(this.d[n6], d12, d14), this.a(this.d[n8], d12 - 1.0, 0.0, d14));
                    d10 = this.b(d13, this.a(this.d[n6 + 1], d12, 0.0, d14 - 1.0), this.a(this.d[n8 + 1], d12 - 1.0, 0.0, d14 - 1.0));
                    double d16 = this.b(d15, d9, d10);
                    int n14 = n9++;
                    dArray[n14] = dArray[n14] + d16 * d11;
                }
            }
            return;
        }
        int n15 = 0;
        double d17 = 1.0 / d8;
        int n16 = -1;
        int n17 = 0;
        int n18 = 0;
        int n19 = 0;
        int n20 = 0;
        int n21 = 0;
        int n22 = 0;
        double d18 = 0.0;
        double d19 = 0.0;
        double d20 = 0.0;
        double d21 = 0.0;
        for (int i4 = 0; i4 < n2; ++i4) {
            double d22 = (d2 + (double)i4) * d5 + this.a;
            int n23 = (int)d22;
            if (d22 < (double)n23) {
                --n23;
            }
            int n24 = n23 & 0xFF;
            double d23 = (d22 -= (double)n23) * d22 * d22 * (d22 * (d22 * 6.0 - 15.0) + 10.0);
            for (int i5 = 0; i5 < n4; ++i5) {
                double d24 = (d4 + (double)i5) * d7 + this.c;
                int n25 = (int)d24;
                if (d24 < (double)n25) {
                    --n25;
                }
                int n26 = n25 & 0xFF;
                double d25 = (d24 -= (double)n25) * d24 * d24 * (d24 * (d24 * 6.0 - 15.0) + 10.0);
                for (int i6 = 0; i6 < n3; ++i6) {
                    double d26 = (d3 + (double)i6) * d6 + this.b;
                    int n27 = (int)d26;
                    if (d26 < (double)n27) {
                        --n27;
                    }
                    int n28 = n27 & 0xFF;
                    double d27 = (d26 -= (double)n27) * d26 * d26 * (d26 * (d26 * 6.0 - 15.0) + 10.0);
                    if (i6 == 0 || n28 != n16) {
                        n16 = n28;
                        n17 = this.d[n24] + n28;
                        n18 = this.d[n17] + n26;
                        n19 = this.d[n17 + 1] + n26;
                        n20 = this.d[n24 + 1] + n28;
                        n21 = this.d[n20] + n26;
                        n22 = this.d[n20 + 1] + n26;
                        d18 = this.b(d23, this.a(this.d[n18], d22, d26, d24), this.a(this.d[n21], d22 - 1.0, d26, d24));
                        d19 = this.b(d23, this.a(this.d[n19], d22, d26 - 1.0, d24), this.a(this.d[n22], d22 - 1.0, d26 - 1.0, d24));
                        d20 = this.b(d23, this.a(this.d[n18 + 1], d22, d26, d24 - 1.0), this.a(this.d[n21 + 1], d22 - 1.0, d26, d24 - 1.0));
                        d21 = this.b(d23, this.a(this.d[n19 + 1], d22, d26 - 1.0, d24 - 1.0), this.a(this.d[n22 + 1], d22 - 1.0, d26 - 1.0, d24 - 1.0));
                    }
                    double d28 = this.b(d27, d18, d19);
                    double d29 = this.b(d27, d20, d21);
                    double d30 = this.b(d25, d28, d29);
                    int n29 = n15++;
                    dArray[n29] = dArray[n29] + d30 * d17;
                }
            }
        }
    }
}

