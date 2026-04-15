/*
 * Decompiled with CFR 0.152.
 */
import java.util.List;

public class yl
extends sn
implements lw {
    private iz[] h = new iz[36];
    public int a = 0;
    public int b = 0;
    public int c = 1;
    private boolean i = false;
    public int d;
    public int e;
    public double f;
    public double g;
    private static final int[][][] j = new int[][][]{new int[][]{{0, 0, -1}, {0, 0, 1}}, new int[][]{{-1, 0, 0}, {1, 0, 0}}, new int[][]{{-1, -1, 0}, {1, 0, 0}}, new int[][]{{-1, 0, 0}, {1, -1, 0}}, new int[][]{{0, 0, -1}, {0, -1, 1}}, new int[][]{{0, -1, -1}, {0, 0, 1}}, new int[][]{{0, 0, 1}, {1, 0, 0}}, new int[][]{{0, 0, 1}, {-1, 0, 0}}, new int[][]{{0, 0, -1}, {-1, 0, 0}}, new int[][]{{0, 0, -1}, {1, 0, 0}}};
    private int k;
    private double l;
    private double m;
    private double n;
    private double o;
    private double p;
    private double q;
    private double r;
    private double s;

    public yl(fd fd2) {
        super(fd2);
        this.aF = true;
        this.b(0.98f, 0.7f);
        this.bf = this.bh / 2.0f;
    }

    protected boolean n() {
        return false;
    }

    protected void b() {
    }

    public eq a(sn sn2) {
        return sn2.aW;
    }

    public eq f() {
        return null;
    }

    public boolean i_() {
        return true;
    }

    public yl(fd fd2, double d2, double d3, double d4, int n2) {
        this(fd2);
        this.e(d2, d3 + (double)this.bf, d4);
        this.aP = 0.0;
        this.aQ = 0.0;
        this.aR = 0.0;
        this.aJ = d2;
        this.aK = d3;
        this.aL = d4;
        this.d = n2;
    }

    public double m() {
        return (double)this.bh * 0.0 - (double)0.3f;
    }

    public boolean a(sn sn2, int n2) {
        if (this.aI.B || this.be) {
            return true;
        }
        this.c = -this.c;
        this.b = 10;
        this.ai();
        this.a += n2 * 10;
        if (this.a > 40) {
            if (this.aG != null) {
                this.aG.i(this);
            }
            this.K();
            this.a(gm.ax.bf, 1, 0.0f);
            if (this.d == 1) {
                yl yl2 = this;
                for (int i2 = 0; i2 < yl2.a(); ++i2) {
                    iz iz2 = yl2.f_(i2);
                    if (iz2 == null) continue;
                    float f2 = this.bs.nextFloat() * 0.8f + 0.1f;
                    float f3 = this.bs.nextFloat() * 0.8f + 0.1f;
                    float f4 = this.bs.nextFloat() * 0.8f + 0.1f;
                    while (iz2.a > 0) {
                        int n3 = this.bs.nextInt(21) + 10;
                        if (n3 > iz2.a) {
                            n3 = iz2.a;
                        }
                        iz2.a -= n3;
                        hl hl2 = new hl(this.aI, this.aM + (double)f2, this.aN + (double)f3, this.aO + (double)f4, new iz(iz2.c, n3, iz2.i()));
                        float f5 = 0.05f;
                        hl2.aP = (float)this.bs.nextGaussian() * f5;
                        hl2.aQ = (float)this.bs.nextGaussian() * f5 + 0.2f;
                        hl2.aR = (float)this.bs.nextGaussian() * f5;
                        this.aI.b(hl2);
                    }
                }
                this.a(uu.av.bn, 1, 0.0f);
            } else if (this.d == 2) {
                this.a(uu.aC.bn, 1, 0.0f);
            }
        }
        return true;
    }

    public void h() {
        System.out.println("Animating hurt");
        this.c = -this.c;
        this.b = 10;
        this.a += this.a * 10;
    }

    public boolean h_() {
        return !this.be;
    }

    public void K() {
        for (int i2 = 0; i2 < this.a(); ++i2) {
            iz iz2 = this.f_(i2);
            if (iz2 == null) continue;
            float f2 = this.bs.nextFloat() * 0.8f + 0.1f;
            float f3 = this.bs.nextFloat() * 0.8f + 0.1f;
            float f4 = this.bs.nextFloat() * 0.8f + 0.1f;
            while (iz2.a > 0) {
                int n2 = this.bs.nextInt(21) + 10;
                if (n2 > iz2.a) {
                    n2 = iz2.a;
                }
                iz2.a -= n2;
                hl hl2 = new hl(this.aI, this.aM + (double)f2, this.aN + (double)f3, this.aO + (double)f4, new iz(iz2.c, n2, iz2.i()));
                float f5 = 0.05f;
                hl2.aP = (float)this.bs.nextGaussian() * f5;
                hl2.aQ = (float)this.bs.nextGaussian() * f5 + 0.2f;
                hl2.aR = (float)this.bs.nextGaussian() * f5;
                this.aI.b(hl2);
            }
        }
        super.K();
    }

    public void w_() {
        double d2;
        int n2;
        int n3;
        if (this.b > 0) {
            --this.b;
        }
        if (this.a > 0) {
            --this.a;
        }
        if (this.aI.B && this.k > 0) {
            if (this.k > 0) {
                double d3;
                double d4 = this.aM + (this.l - this.aM) / (double)this.k;
                double d5 = this.aN + (this.m - this.aN) / (double)this.k;
                double d6 = this.aO + (this.n - this.aO) / (double)this.k;
                for (d3 = this.o - (double)this.aS; d3 < -180.0; d3 += 360.0) {
                }
                while (d3 >= 180.0) {
                    d3 -= 360.0;
                }
                this.aS = (float)((double)this.aS + d3 / (double)this.k);
                this.aT = (float)((double)this.aT + (this.p - (double)this.aT) / (double)this.k);
                --this.k;
                this.e(d4, d5, d6);
                this.c(this.aS, this.aT);
            } else {
                this.e(this.aM, this.aN, this.aO);
                this.c(this.aS, this.aT);
            }
            return;
        }
        this.aJ = this.aM;
        this.aK = this.aN;
        this.aL = this.aO;
        this.aQ -= (double)0.04f;
        int n4 = in.b(this.aM);
        if (pc.h(this.aI, n4, (n3 = in.b(this.aN)) - 1, n2 = in.b(this.aO))) {
            --n3;
        }
        double d7 = 0.4;
        boolean bl2 = false;
        double d8 = 0.0078125;
        int n5 = this.aI.a(n4, n3, n2);
        if (pc.d(n5)) {
            double d9;
            double d10;
            double d11;
            double d12;
            double d13;
            bt bt2 = this.i(this.aM, this.aN, this.aO);
            int n6 = this.aI.e(n4, n3, n2);
            this.aN = n3;
            boolean bl3 = false;
            boolean bl4 = false;
            if (n5 == uu.U.bn) {
                bl3 = (n6 & 8) != 0;
                boolean bl5 = bl4 = !bl3;
            }
            if (((pc)uu.m[n5]).i()) {
                n6 &= 7;
            }
            if (n6 >= 2 && n6 <= 5) {
                this.aN = n3 + 1;
            }
            if (n6 == 2) {
                this.aP -= d8;
            }
            if (n6 == 3) {
                this.aP += d8;
            }
            if (n6 == 4) {
                this.aR += d8;
            }
            if (n6 == 5) {
                this.aR -= d8;
            }
            int[][] nArray = j[n6];
            double d14 = nArray[1][0] - nArray[0][0];
            double d15 = nArray[1][2] - nArray[0][2];
            double d16 = Math.sqrt(d14 * d14 + d15 * d15);
            double d17 = this.aP * d14 + this.aR * d15;
            if (d17 < 0.0) {
                d14 = -d14;
                d15 = -d15;
            }
            double d18 = Math.sqrt(this.aP * this.aP + this.aR * this.aR);
            this.aP = d18 * d14 / d16;
            this.aR = d18 * d15 / d16;
            if (bl4) {
                d13 = Math.sqrt(this.aP * this.aP + this.aR * this.aR);
                if (d13 < 0.03) {
                    this.aP *= 0.0;
                    this.aQ *= 0.0;
                    this.aR *= 0.0;
                } else {
                    this.aP *= 0.5;
                    this.aQ *= 0.0;
                    this.aR *= 0.5;
                }
            }
            d13 = 0.0;
            double d19 = (double)n4 + 0.5 + (double)nArray[0][0] * 0.5;
            double d20 = (double)n2 + 0.5 + (double)nArray[0][2] * 0.5;
            double d21 = (double)n4 + 0.5 + (double)nArray[1][0] * 0.5;
            double d22 = (double)n2 + 0.5 + (double)nArray[1][2] * 0.5;
            d14 = d21 - d19;
            d15 = d22 - d20;
            if (d14 == 0.0) {
                this.aM = (double)n4 + 0.5;
                d13 = this.aO - (double)n2;
            } else if (d15 == 0.0) {
                this.aO = (double)n2 + 0.5;
                d13 = this.aM - (double)n4;
            } else {
                d12 = this.aM - d19;
                d11 = this.aO - d20;
                d13 = d10 = (d12 * d14 + d11 * d15) * 2.0;
            }
            this.aM = d19 + d14 * d13;
            this.aO = d20 + d15 * d13;
            this.e(this.aM, this.aN + (double)this.bf, this.aO);
            d12 = this.aP;
            d11 = this.aR;
            if (this.aG != null) {
                d12 *= 0.75;
                d11 *= 0.75;
            }
            if (d12 < -d7) {
                d12 = -d7;
            }
            if (d12 > d7) {
                d12 = d7;
            }
            if (d11 < -d7) {
                d11 = -d7;
            }
            if (d11 > d7) {
                d11 = d7;
            }
            this.b(d12, 0.0, d11);
            if (nArray[0][1] != 0 && in.b(this.aM) - n4 == nArray[0][0] && in.b(this.aO) - n2 == nArray[0][2]) {
                this.e(this.aM, this.aN + (double)nArray[0][1], this.aO);
            } else if (nArray[1][1] != 0 && in.b(this.aM) - n4 == nArray[1][0] && in.b(this.aO) - n2 == nArray[1][2]) {
                this.e(this.aM, this.aN + (double)nArray[1][1], this.aO);
            }
            if (this.aG != null) {
                this.aP *= (double)0.997f;
                this.aQ *= 0.0;
                this.aR *= (double)0.997f;
            } else {
                if (this.d == 2) {
                    d10 = in.a(this.f * this.f + this.g * this.g);
                    if (d10 > 0.01) {
                        bl2 = true;
                        this.f /= d10;
                        this.g /= d10;
                        double d23 = 0.04;
                        this.aP *= (double)0.8f;
                        this.aQ *= 0.0;
                        this.aR *= (double)0.8f;
                        this.aP += this.f * d23;
                        this.aR += this.g * d23;
                    } else {
                        this.aP *= (double)0.9f;
                        this.aQ *= 0.0;
                        this.aR *= (double)0.9f;
                    }
                }
                this.aP *= (double)0.96f;
                this.aQ *= 0.0;
                this.aR *= (double)0.96f;
            }
            bt bt3 = this.i(this.aM, this.aN, this.aO);
            if (bt3 != null && bt2 != null) {
                double d24 = (bt2.b - bt3.b) * 0.05;
                d18 = Math.sqrt(this.aP * this.aP + this.aR * this.aR);
                if (d18 > 0.0) {
                    this.aP = this.aP / d18 * (d18 + d24);
                    this.aR = this.aR / d18 * (d18 + d24);
                }
                this.e(this.aM, bt3.b, this.aO);
            }
            int n7 = in.b(this.aM);
            int n8 = in.b(this.aO);
            if (n7 != n4 || n8 != n2) {
                d18 = Math.sqrt(this.aP * this.aP + this.aR * this.aR);
                this.aP = d18 * (double)(n7 - n4);
                this.aR = d18 * (double)(n8 - n2);
            }
            if (this.d == 2 && (d9 = (double)in.a(this.f * this.f + this.g * this.g)) > 0.01 && this.aP * this.aP + this.aR * this.aR > 0.001) {
                this.f /= d9;
                this.g /= d9;
                if (this.f * this.aP + this.g * this.aR < 0.0) {
                    this.f = 0.0;
                    this.g = 0.0;
                } else {
                    this.f = this.aP;
                    this.g = this.aR;
                }
            }
            if (bl3) {
                d9 = Math.sqrt(this.aP * this.aP + this.aR * this.aR);
                if (d9 > 0.01) {
                    double d25 = 0.06;
                    this.aP += this.aP / d9 * d25;
                    this.aR += this.aR / d9 * d25;
                } else if (n6 == 1) {
                    if (this.aI.h(n4 - 1, n3, n2)) {
                        this.aP = 0.02;
                    } else if (this.aI.h(n4 + 1, n3, n2)) {
                        this.aP = -0.02;
                    }
                } else if (n6 == 0) {
                    if (this.aI.h(n4, n3, n2 - 1)) {
                        this.aR = 0.02;
                    } else if (this.aI.h(n4, n3, n2 + 1)) {
                        this.aR = -0.02;
                    }
                }
            }
        } else {
            if (this.aP < -d7) {
                this.aP = -d7;
            }
            if (this.aP > d7) {
                this.aP = d7;
            }
            if (this.aR < -d7) {
                this.aR = -d7;
            }
            if (this.aR > d7) {
                this.aR = d7;
            }
            if (this.aX) {
                this.aP *= 0.5;
                this.aQ *= 0.5;
                this.aR *= 0.5;
            }
            this.b(this.aP, this.aQ, this.aR);
            if (!this.aX) {
                this.aP *= (double)0.95f;
                this.aQ *= (double)0.95f;
                this.aR *= (double)0.95f;
            }
        }
        this.aT = 0.0f;
        double d26 = this.aJ - this.aM;
        double d27 = this.aL - this.aO;
        if (d26 * d26 + d27 * d27 > 0.001) {
            this.aS = (float)(Math.atan2(d27, d26) * 180.0 / Math.PI);
            if (this.i) {
                this.aS += 180.0f;
            }
        }
        for (d2 = (double)(this.aS - this.aU); d2 >= 180.0; d2 -= 360.0) {
        }
        while (d2 < -180.0) {
            d2 += 360.0;
        }
        if (d2 < -170.0 || d2 >= 170.0) {
            this.aS += 180.0f;
            this.i = !this.i;
        }
        this.c(this.aS, this.aT);
        List list = this.aI.b(this, this.aW.b(0.2f, 0.0, 0.2f));
        if (list != null && list.size() > 0) {
            for (int i2 = 0; i2 < list.size(); ++i2) {
                sn sn2 = (sn)list.get(i2);
                if (sn2 == this.aG || !sn2.i_() || !(sn2 instanceof yl)) continue;
                sn2.h(this);
            }
        }
        if (this.aG != null && this.aG.be) {
            this.aG = null;
        }
        if (bl2 && this.bs.nextInt(4) == 0) {
            --this.e;
            if (this.e < 0) {
                this.g = 0.0;
                this.f = 0.0;
            }
            this.aI.a("largesmoke", this.aM, this.aN + 0.8, this.aO, 0.0, 0.0, 0.0);
        }
    }

    public bt a(double d2, double d3, double d4, double d5) {
        int n2;
        int n3;
        int n4;
        int n5 = in.b(d2);
        if (pc.h(this.aI, n5, (n4 = in.b(d3)) - 1, n3 = in.b(d4))) {
            --n4;
        }
        if (pc.d(n2 = this.aI.a(n5, n4, n3))) {
            int n6 = this.aI.e(n5, n4, n3);
            if (((pc)uu.m[n2]).i()) {
                n6 &= 7;
            }
            d3 = n4;
            if (n6 >= 2 && n6 <= 5) {
                d3 = n4 + 1;
            }
            int[][] nArray = j[n6];
            double d6 = nArray[1][0] - nArray[0][0];
            double d7 = nArray[1][2] - nArray[0][2];
            double d8 = Math.sqrt(d6 * d6 + d7 * d7);
            if (nArray[0][1] != 0 && in.b(d2 += (d6 /= d8) * d5) - n5 == nArray[0][0] && in.b(d4 += (d7 /= d8) * d5) - n3 == nArray[0][2]) {
                d3 += (double)nArray[0][1];
            } else if (nArray[1][1] != 0 && in.b(d2) - n5 == nArray[1][0] && in.b(d4) - n3 == nArray[1][2]) {
                d3 += (double)nArray[1][1];
            }
            return this.i(d2, d3, d4);
        }
        return null;
    }

    public bt i(double d2, double d3, double d4) {
        int n2;
        int n3;
        int n4;
        int n5 = in.b(d2);
        if (pc.h(this.aI, n5, (n4 = in.b(d3)) - 1, n3 = in.b(d4))) {
            --n4;
        }
        if (pc.d(n2 = this.aI.a(n5, n4, n3))) {
            int n6 = this.aI.e(n5, n4, n3);
            d3 = n4;
            if (((pc)uu.m[n2]).i()) {
                n6 &= 7;
            }
            if (n6 >= 2 && n6 <= 5) {
                d3 = n4 + 1;
            }
            int[][] nArray = j[n6];
            double d5 = 0.0;
            double d6 = (double)n5 + 0.5 + (double)nArray[0][0] * 0.5;
            double d7 = (double)n4 + 0.5 + (double)nArray[0][1] * 0.5;
            double d8 = (double)n3 + 0.5 + (double)nArray[0][2] * 0.5;
            double d9 = (double)n5 + 0.5 + (double)nArray[1][0] * 0.5;
            double d10 = (double)n4 + 0.5 + (double)nArray[1][1] * 0.5;
            double d11 = (double)n3 + 0.5 + (double)nArray[1][2] * 0.5;
            double d12 = d9 - d6;
            double d13 = (d10 - d7) * 2.0;
            double d14 = d11 - d8;
            if (d12 == 0.0) {
                d2 = (double)n5 + 0.5;
                d5 = d4 - (double)n3;
            } else if (d14 == 0.0) {
                d4 = (double)n3 + 0.5;
                d5 = d2 - (double)n5;
            } else {
                double d15;
                double d16 = d2 - d6;
                double d17 = d4 - d8;
                d5 = d15 = (d16 * d12 + d17 * d14) * 2.0;
            }
            d2 = d6 + d12 * d5;
            d3 = d7 + d13 * d5;
            d4 = d8 + d14 * d5;
            if (d13 < 0.0) {
                d3 += 1.0;
            }
            if (d13 > 0.0) {
                d3 += 0.5;
            }
            return bt.b(d2, d3, d4);
        }
        return null;
    }

    protected void b(nu nu2) {
        nu2.a("Type", this.d);
        if (this.d == 2) {
            nu2.a("PushX", this.f);
            nu2.a("PushZ", this.g);
            nu2.a("Fuel", (short)this.e);
        } else if (this.d == 1) {
            sp sp2 = new sp();
            for (int i2 = 0; i2 < this.h.length; ++i2) {
                if (this.h[i2] == null) continue;
                nu nu3 = new nu();
                nu3.a("Slot", (byte)i2);
                this.h[i2].a(nu3);
                sp2.a(nu3);
            }
            nu2.a("Items", sp2);
        }
    }

    protected void a(nu nu2) {
        this.d = nu2.e("Type");
        if (this.d == 2) {
            this.f = nu2.h("PushX");
            this.g = nu2.h("PushZ");
            this.e = nu2.d("Fuel");
        } else if (this.d == 1) {
            sp sp2 = nu2.l("Items");
            this.h = new iz[this.a()];
            for (int i2 = 0; i2 < sp2.c(); ++i2) {
                nu nu3 = (nu)sp2.a(i2);
                int n2 = nu3.c("Slot") & 0xFF;
                if (n2 < 0 || n2 >= this.h.length) continue;
                this.h[n2] = new iz(nu3);
            }
        }
    }

    public float x_() {
        return 0.0f;
    }

    public void h(sn sn2) {
        double d2;
        double d3;
        double d4;
        if (this.aI.B) {
            return;
        }
        if (sn2 == this.aG) {
            return;
        }
        if (sn2 instanceof ls && !(sn2 instanceof gs) && this.d == 0 && this.aP * this.aP + this.aR * this.aR > 0.01 && this.aG == null && sn2.aH == null) {
            sn2.i(this);
        }
        if ((d4 = (d3 = sn2.aM - this.aM) * d3 + (d2 = sn2.aO - this.aO) * d2) >= (double)1.0E-4f) {
            d4 = in.a(d4);
            d3 /= d4;
            d2 /= d4;
            double d5 = 1.0 / d4;
            if (d5 > 1.0) {
                d5 = 1.0;
            }
            d3 *= d5;
            d2 *= d5;
            d3 *= (double)0.1f;
            d2 *= (double)0.1f;
            d3 *= (double)(1.0f - this.br);
            d2 *= (double)(1.0f - this.br);
            d3 *= 0.5;
            d2 *= 0.5;
            if (sn2 instanceof yl) {
                double d6 = sn2.aM - this.aM;
                double d7 = sn2.aO - this.aO;
                double d8 = d6 * sn2.aR + d7 * sn2.aJ;
                if ((d8 *= d8) > 5.0) {
                    return;
                }
                double d9 = sn2.aP + this.aP;
                double d10 = sn2.aR + this.aR;
                if (((yl)sn2).d == 2 && this.d != 2) {
                    this.aP *= (double)0.2f;
                    this.aR *= (double)0.2f;
                    this.d(sn2.aP - d3, 0.0, sn2.aR - d2);
                    sn2.aP *= (double)0.7f;
                    sn2.aR *= (double)0.7f;
                } else if (((yl)sn2).d != 2 && this.d == 2) {
                    sn2.aP *= (double)0.2f;
                    sn2.aR *= (double)0.2f;
                    sn2.d(this.aP + d3, 0.0, this.aR + d2);
                    this.aP *= (double)0.7f;
                    this.aR *= (double)0.7f;
                } else {
                    this.aP *= (double)0.2f;
                    this.aR *= (double)0.2f;
                    this.d((d9 /= 2.0) - d3, 0.0, (d10 /= 2.0) - d2);
                    sn2.aP *= (double)0.2f;
                    sn2.aR *= (double)0.2f;
                    sn2.d(d9 + d3, 0.0, d10 + d2);
                }
            } else {
                this.d(-d3, 0.0, -d2);
                sn2.d(d3 / 4.0, 0.0, d2 / 4.0);
            }
        }
    }

    public int a() {
        return 27;
    }

    public iz f_(int n2) {
        return this.h[n2];
    }

    public iz a(int n2, int n3) {
        if (this.h[n2] != null) {
            if (this.h[n2].a <= n3) {
                iz iz2 = this.h[n2];
                this.h[n2] = null;
                return iz2;
            }
            iz iz3 = this.h[n2].a(n3);
            if (this.h[n2].a == 0) {
                this.h[n2] = null;
            }
            return iz3;
        }
        return null;
    }

    public void a(int n2, iz iz2) {
        this.h[n2] = iz2;
        if (iz2 != null && iz2.a > this.d()) {
            iz2.a = this.d();
        }
    }

    public String c() {
        return "Minecart";
    }

    public int d() {
        return 64;
    }

    public void y_() {
    }

    public boolean a(gs gs2) {
        if (this.d == 0) {
            if (this.aG != null && this.aG instanceof gs && this.aG != gs2) {
                return true;
            }
            if (!this.aI.B) {
                gs2.i(this);
            }
        } else if (this.d == 1) {
            if (!this.aI.B) {
                gs2.a(this);
            }
        } else if (this.d == 2) {
            iz iz2 = gs2.c.b();
            if (iz2 != null && iz2.c == gm.k.bf) {
                if (--iz2.a == 0) {
                    gs2.c.a(gs2.c.c, null);
                }
                this.e += 1200;
            }
            this.f = this.aM - gs2.aM;
            this.g = this.aO - gs2.aO;
        }
        return true;
    }

    public void a(double d2, double d3, double d4, float f2, float f3, int n2) {
        this.l = d2;
        this.m = d3;
        this.n = d4;
        this.o = f2;
        this.p = f3;
        this.k = n2 + 2;
        this.aP = this.q;
        this.aQ = this.r;
        this.aR = this.s;
    }

    public void a(double d2, double d3, double d4) {
        this.q = this.aP = d2;
        this.r = this.aQ = d3;
        this.s = this.aR = d4;
    }

    public boolean a_(gs gs2) {
        if (this.be) {
            return false;
        }
        return !(gs2.g(this) > 64.0);
    }
}

