/*
 * Decompiled with CFR 0.152.
 */
import java.util.Random;

public class cc {
    private static int[][] d = new int[][]{{1, 1, 0}, {-1, 1, 0}, {1, -1, 0}, {-1, -1, 0}, {1, 0, 1}, {-1, 0, 1}, {1, 0, -1}, {-1, 0, -1}, {0, 1, 1}, {0, -1, 1}, {0, 1, -1}, {0, -1, -1}};
    private int[] e = new int[512];
    public double a;
    public double b;
    public double c;
    private static final double f = 0.5 * (Math.sqrt(3.0) - 1.0);
    private static final double g = (3.0 - Math.sqrt(3.0)) / 6.0;

    public cc() {
        this(new Random());
    }

    public cc(Random random) {
        int n2;
        this.a = random.nextDouble() * 256.0;
        this.b = random.nextDouble() * 256.0;
        this.c = random.nextDouble() * 256.0;
        for (n2 = 0; n2 < 256; ++n2) {
            this.e[n2] = n2;
        }
        for (n2 = 0; n2 < 256; ++n2) {
            int n3 = random.nextInt(256 - n2) + n2;
            int n4 = this.e[n2];
            this.e[n2] = this.e[n3];
            this.e[n3] = n4;
            this.e[n2 + 256] = this.e[n2];
        }
    }

    private static int a(double d2) {
        return d2 > 0.0 ? (int)d2 : (int)d2 - 1;
    }

    private static double a(int[] nArray, double d2, double d3) {
        return (double)nArray[0] * d2 + (double)nArray[1] * d3;
    }

    public void a(double[] dArray, double d2, double d3, int n2, int n3, double d4, double d5, double d6) {
        int n4 = 0;
        for (int i2 = 0; i2 < n2; ++i2) {
            double d7 = (d2 + (double)i2) * d4 + this.a;
            for (int i3 = 0; i3 < n3; ++i3) {
                double d8;
                double d9;
                double d10;
                int n5;
                int n6;
                double d11;
                double d12;
                int n7;
                double d13;
                double d14 = (d3 + (double)i3) * d5 + this.b;
                double d15 = (d7 + d14) * f;
                int n8 = cc.a(d7 + d15);
                double d16 = (double)n8 - (d13 = (double)(n8 + (n7 = cc.a(d14 + d15))) * g);
                double d17 = d7 - d16;
                if (d17 > (d12 = d14 - (d11 = (double)n7 - d13))) {
                    n6 = 1;
                    n5 = 0;
                } else {
                    n6 = 0;
                    n5 = 1;
                }
                double d18 = d17 - (double)n6 + g;
                double d19 = d12 - (double)n5 + g;
                double d20 = d17 - 1.0 + 2.0 * g;
                double d21 = d12 - 1.0 + 2.0 * g;
                int n9 = n8 & 0xFF;
                int n10 = n7 & 0xFF;
                int n11 = this.e[n9 + this.e[n10]] % 12;
                int n12 = this.e[n9 + n6 + this.e[n10 + n5]] % 12;
                int n13 = this.e[n9 + 1 + this.e[n10 + 1]] % 12;
                double d22 = 0.5 - d17 * d17 - d12 * d12;
                if (d22 < 0.0) {
                    d10 = 0.0;
                } else {
                    d22 *= d22;
                    d10 = d22 * d22 * cc.a(d[n11], d17, d12);
                }
                double d23 = 0.5 - d18 * d18 - d19 * d19;
                if (d23 < 0.0) {
                    d9 = 0.0;
                } else {
                    d23 *= d23;
                    d9 = d23 * d23 * cc.a(d[n12], d18, d19);
                }
                double d24 = 0.5 - d20 * d20 - d21 * d21;
                if (d24 < 0.0) {
                    d8 = 0.0;
                } else {
                    d24 *= d24;
                    d8 = d24 * d24 * cc.a(d[n13], d20, d21);
                }
                int n14 = n4++;
                dArray[n14] = dArray[n14] + 70.0 * (d10 + d9 + d8) * d6;
            }
        }
    }
}

