/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.opengl.GL11
 */
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

public class cv {
    private xp c;
    private int d = -1;
    private boolean e = false;
    private boolean f = false;
    public static boolean a = true;
    public boolean b = true;
    private int g = 0;
    private int h = 0;
    private int i = 0;
    private int j = 0;
    private int k = 0;
    private int l = 0;
    private boolean m;
    private float n;
    private float o;
    private float p;
    private float q;
    private float r;
    private float s;
    private float t;
    private float u;
    private float v;
    private float w;
    private float x;
    private float y;
    private float z;
    private float A;
    private float B;
    private float C;
    private float D;
    private float E;
    private float F;
    private float G;
    private float H;
    private float I;
    private float J;
    private float K;
    private float L;
    private float M;
    private float N;
    private int O = 1;
    private float P;
    private float Q;
    private float R;
    private float S;
    private float T;
    private float U;
    private float V;
    private float W;
    private float X;
    private float Y;
    private float Z;
    private float aa;
    private boolean ab;
    private boolean ac;
    private boolean ad;
    private boolean ae;
    private boolean af;
    private boolean ag;
    private boolean ah;
    private boolean ai;
    private boolean aj;
    private boolean ak;
    private boolean al;
    private boolean am;

    public cv(xp xp2) {
        this.c = xp2;
    }

    public cv() {
    }

    public void a(uu uu2, int n2, int n3, int n4, int n5) {
        this.d = n5;
        this.b(uu2, n2, n3, n4);
        this.d = -1;
    }

    public void a(uu uu2, int n2, int n3, int n4) {
        this.f = true;
        this.b(uu2, n2, n3, n4);
        this.f = false;
    }

    public boolean b(uu uu2, int n2, int n3, int n4) {
        int n5 = uu2.b();
        uu2.a(this.c, n2, n3, n4);
        if (n5 == 0) {
            return this.l(uu2, n2, n3, n4);
        }
        if (n5 == 4) {
            return this.k(uu2, n2, n3, n4);
        }
        if (n5 == 13) {
            return this.m(uu2, n2, n3, n4);
        }
        if (n5 == 1) {
            return this.i(uu2, n2, n3, n4);
        }
        if (n5 == 6) {
            return this.j(uu2, n2, n3, n4);
        }
        if (n5 == 2) {
            return this.c(uu2, n2, n3, n4);
        }
        if (n5 == 3) {
            return this.f(uu2, n2, n3, n4);
        }
        if (n5 == 5) {
            return this.g(uu2, n2, n3, n4);
        }
        if (n5 == 8) {
            return this.h(uu2, n2, n3, n4);
        }
        if (n5 == 7) {
            return this.p(uu2, n2, n3, n4);
        }
        if (n5 == 9) {
            return this.a((pc)uu2, n2, n3, n4);
        }
        if (n5 == 10) {
            return this.o(uu2, n2, n3, n4);
        }
        if (n5 == 11) {
            return this.n(uu2, n2, n3, n4);
        }
        if (n5 == 12) {
            return this.e(uu2, n2, n3, n4);
        }
        if (n5 == 14) {
            return this.q(uu2, n2, n3, n4);
        }
        if (n5 == 15) {
            return this.r(uu2, n2, n3, n4);
        }
        if (n5 == 16) {
            return this.b(uu2, n2, n3, n4, false);
        }
        if (n5 == 17) {
            return this.c(uu2, n2, n3, n4, true);
        }
        return false;
    }

    private boolean q(uu uu2, int n2, int n3, int n4) {
        nw nw2 = nw.a;
        int n5 = this.c.e(n2, n3, n4);
        int n6 = ve.d(n5);
        boolean bl2 = ve.e(n5);
        float f2 = 0.5f;
        float f3 = 1.0f;
        float f4 = 0.8f;
        float f5 = 0.6f;
        float f6 = f3;
        float f7 = f3;
        float f8 = f3;
        float f9 = f2;
        float f10 = f4;
        float f11 = f5;
        float f12 = f2;
        float f13 = f4;
        float f14 = f5;
        float f15 = f2;
        float f16 = f4;
        float f17 = f5;
        float f18 = uu2.d(this.c, n2, n3, n4);
        nw2.a(f9 * f18, f12 * f18, f15 * f18);
        int n7 = uu2.a(this.c, n2, n3, n4, 0);
        int n8 = (n7 & 0xF) << 4;
        int n9 = n7 & 0xF0;
        double d2 = (float)n8 / 256.0f;
        double d3 = ((double)(n8 + 16) - 0.01) / 256.0;
        double d4 = (float)n9 / 256.0f;
        double d5 = ((double)(n9 + 16) - 0.01) / 256.0;
        double d6 = (double)n2 + uu2.bs;
        double d7 = (double)n2 + uu2.bv;
        double d8 = (double)n3 + uu2.bt + 0.1875;
        double d9 = (double)n4 + uu2.bu;
        double d10 = (double)n4 + uu2.bx;
        nw2.a(d6, d8, d10, d2, d5);
        nw2.a(d6, d8, d9, d2, d4);
        nw2.a(d7, d8, d9, d3, d4);
        nw2.a(d7, d8, d10, d3, d5);
        float f19 = uu2.d(this.c, n2, n3 + 1, n4);
        nw2.a(f6 * f19, f7 * f19, f8 * f19);
        n8 = uu2.a(this.c, n2, n3, n4, 1);
        n9 = (n8 & 0xF) << 4;
        int n10 = n8 & 0xF0;
        double d11 = (float)n9 / 256.0f;
        double d12 = ((double)(n9 + 16) - 0.01) / 256.0;
        double d13 = (float)n10 / 256.0f;
        double d14 = ((double)(n10 + 16) - 0.01) / 256.0;
        double d15 = d11;
        double d16 = d12;
        double d17 = d13;
        double d18 = d13;
        double d19 = d11;
        double d20 = d12;
        double d21 = d14;
        double d22 = d14;
        if (n6 == 0) {
            d16 = d11;
            d17 = d14;
            d19 = d12;
            d22 = d13;
        } else if (n6 == 2) {
            d15 = d12;
            d18 = d14;
            d20 = d11;
            d21 = d13;
        } else if (n6 == 3) {
            d15 = d12;
            d18 = d14;
            d20 = d11;
            d21 = d13;
            d16 = d11;
            d17 = d14;
            d19 = d12;
            d22 = d13;
        }
        double d23 = (double)n2 + uu2.bs;
        double d24 = (double)n2 + uu2.bv;
        double d25 = (double)n3 + uu2.bw;
        double d26 = (double)n4 + uu2.bu;
        double d27 = (double)n4 + uu2.bx;
        nw2.a(d24, d25, d27, d19, d21);
        nw2.a(d24, d25, d26, d15, d17);
        nw2.a(d23, d25, d26, d16, d18);
        nw2.a(d23, d25, d27, d20, d22);
        int n11 = jj.a[n6];
        if (bl2) {
            n11 = jj.a[jj.b[n6]];
        }
        n8 = 4;
        switch (n6) {
            case 2: {
                break;
            }
            case 0: {
                n8 = 5;
                break;
            }
            case 3: {
                n8 = 2;
                break;
            }
            case 1: {
                n8 = 3;
            }
        }
        if (n11 != 2 && (this.f || uu2.b(this.c, n2, n3, n4 - 1, 2))) {
            float f20 = uu2.d(this.c, n2, n3, n4 - 1);
            if (uu2.bu > 0.0) {
                f20 = f18;
            }
            nw2.a(f10 * f20, f13 * f20, f16 * f20);
            this.e = n8 == 2;
            this.c(uu2, (double)n2, (double)n3, (double)n4, uu2.a(this.c, n2, n3, n4, 2));
        }
        if (n11 != 3 && (this.f || uu2.b(this.c, n2, n3, n4 + 1, 3))) {
            float f21 = uu2.d(this.c, n2, n3, n4 + 1);
            if (uu2.bx < 1.0) {
                f21 = f18;
            }
            nw2.a(f10 * f21, f13 * f21, f16 * f21);
            this.e = n8 == 3;
            this.d(uu2, n2, n3, n4, uu2.a(this.c, n2, n3, n4, 3));
        }
        if (n11 != 4 && (this.f || uu2.b(this.c, n2 - 1, n3, n4, 4))) {
            float f22 = uu2.d(this.c, n2 - 1, n3, n4);
            if (uu2.bs > 0.0) {
                f22 = f18;
            }
            nw2.a(f11 * f22, f14 * f22, f17 * f22);
            this.e = n8 == 4;
            this.e(uu2, n2, n3, n4, uu2.a(this.c, n2, n3, n4, 4));
        }
        if (n11 != 5 && (this.f || uu2.b(this.c, n2 + 1, n3, n4, 5))) {
            float f23 = uu2.d(this.c, n2 + 1, n3, n4);
            if (uu2.bv < 1.0) {
                f23 = f18;
            }
            nw2.a(f11 * f23, f14 * f23, f17 * f23);
            this.e = n8 == 5;
            this.f(uu2, n2, n3, n4, uu2.a(this.c, n2, n3, n4, 5));
        }
        this.e = false;
        return true;
    }

    public boolean c(uu uu2, int n2, int n3, int n4) {
        int n5 = this.c.e(n2, n3, n4);
        nw nw2 = nw.a;
        float f2 = uu2.d(this.c, n2, n3, n4);
        if (uu.s[uu2.bn] > 0) {
            f2 = 1.0f;
        }
        nw2.a(f2, f2, f2);
        double d2 = 0.4f;
        double d3 = 0.5 - d2;
        double d4 = 0.2f;
        if (n5 == 1) {
            this.a(uu2, (double)n2 - d3, (double)n3 + d4, n4, -d2, 0.0);
        } else if (n5 == 2) {
            this.a(uu2, (double)n2 + d3, (double)n3 + d4, n4, d2, 0.0);
        } else if (n5 == 3) {
            this.a(uu2, n2, (double)n3 + d4, (double)n4 - d3, 0.0, -d2);
        } else if (n5 == 4) {
            this.a(uu2, n2, (double)n3 + d4, (double)n4 + d3, 0.0, d2);
        } else {
            this.a(uu2, n2, n3, n4, 0.0, 0.0);
        }
        return true;
    }

    private boolean r(uu uu2, int n2, int n3, int n4) {
        int n5 = this.c.e(n2, n3, n4);
        int n6 = n5 & 3;
        int n7 = (n5 & 0xC) >> 2;
        this.l(uu2, n2, n3, n4);
        nw nw2 = nw.a;
        float f2 = uu2.d(this.c, n2, n3, n4);
        if (uu.s[uu2.bn] > 0) {
            f2 = (f2 + 1.0f) * 0.5f;
        }
        nw2.a(f2, f2, f2);
        double d2 = -0.1875;
        double d3 = 0.0;
        double d4 = 0.0;
        double d5 = 0.0;
        double d6 = 0.0;
        switch (n6) {
            case 0: {
                d6 = -0.3125;
                d4 = wo.a[n7];
                break;
            }
            case 2: {
                d6 = 0.3125;
                d4 = -wo.a[n7];
                break;
            }
            case 3: {
                d5 = -0.3125;
                d3 = wo.a[n7];
                break;
            }
            case 1: {
                d5 = 0.3125;
                d3 = -wo.a[n7];
            }
        }
        this.a(uu2, (double)n2 + d3, (double)n3 + d2, (double)n4 + d4, 0.0, 0.0);
        this.a(uu2, (double)n2 + d5, (double)n3 + d2, (double)n4 + d6, 0.0, 0.0);
        int n8 = uu2.a(1);
        int n9 = (n8 & 0xF) << 4;
        int n10 = n8 & 0xF0;
        double d7 = (float)n9 / 256.0f;
        double d8 = ((float)n9 + 15.99f) / 256.0f;
        double d9 = (float)n10 / 256.0f;
        double d10 = ((float)n10 + 15.99f) / 256.0f;
        float f3 = 0.125f;
        float f4 = n2 + 1;
        float f5 = n2 + 1;
        float f6 = n2 + 0;
        float f7 = n2 + 0;
        float f8 = n4 + 0;
        float f9 = n4 + 1;
        float f10 = n4 + 1;
        float f11 = n4 + 0;
        float f12 = (float)n3 + f3;
        if (n6 == 2) {
            f4 = f5 = (float)(n2 + 0);
            f6 = f7 = (float)(n2 + 1);
            f8 = f11 = (float)(n4 + 1);
            f9 = f10 = (float)(n4 + 0);
        } else if (n6 == 3) {
            f4 = f7 = (float)(n2 + 0);
            f5 = f6 = (float)(n2 + 1);
            f8 = f9 = (float)(n4 + 0);
            f10 = f11 = (float)(n4 + 1);
        } else if (n6 == 1) {
            f4 = f7 = (float)(n2 + 1);
            f5 = f6 = (float)(n2 + 0);
            f8 = f9 = (float)(n4 + 1);
            f10 = f11 = (float)(n4 + 0);
        }
        nw2.a(f7, f12, f11, d7, d9);
        nw2.a(f6, f12, f10, d7, d10);
        nw2.a(f5, f12, f9, d8, d10);
        nw2.a(f4, f12, f8, d8, d9);
        return true;
    }

    public void d(uu uu2, int n2, int n3, int n4) {
        this.f = true;
        this.b(uu2, n2, n3, n4, true);
        this.f = false;
    }

    private boolean b(uu uu2, int n2, int n3, int n4, boolean bl2) {
        int n5 = this.c.e(n2, n3, n4);
        boolean bl3 = bl2 || (n5 & 8) != 0;
        int n6 = jq.d(n5);
        if (bl3) {
            switch (n6) {
                case 0: {
                    this.g = 3;
                    this.h = 3;
                    this.i = 3;
                    this.j = 3;
                    uu2.a(0.0f, 0.25f, 0.0f, 1.0f, 1.0f, 1.0f);
                    break;
                }
                case 1: {
                    uu2.a(0.0f, 0.0f, 0.0f, 1.0f, 0.75f, 1.0f);
                    break;
                }
                case 2: {
                    this.i = 1;
                    this.j = 2;
                    uu2.a(0.0f, 0.0f, 0.25f, 1.0f, 1.0f, 1.0f);
                    break;
                }
                case 3: {
                    this.i = 2;
                    this.j = 1;
                    this.k = 3;
                    this.l = 3;
                    uu2.a(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.75f);
                    break;
                }
                case 4: {
                    this.g = 1;
                    this.h = 2;
                    this.k = 2;
                    this.l = 1;
                    uu2.a(0.25f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
                    break;
                }
                case 5: {
                    this.g = 2;
                    this.h = 1;
                    this.k = 1;
                    this.l = 2;
                    uu2.a(0.0f, 0.0f, 0.0f, 0.75f, 1.0f, 1.0f);
                }
            }
            this.l(uu2, n2, n3, n4);
            this.g = 0;
            this.h = 0;
            this.i = 0;
            this.j = 0;
            this.k = 0;
            this.l = 0;
            uu2.a(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
        } else {
            switch (n6) {
                case 0: {
                    this.g = 3;
                    this.h = 3;
                    this.i = 3;
                    this.j = 3;
                    break;
                }
                case 1: {
                    break;
                }
                case 2: {
                    this.i = 1;
                    this.j = 2;
                    break;
                }
                case 3: {
                    this.i = 2;
                    this.j = 1;
                    this.k = 3;
                    this.l = 3;
                    break;
                }
                case 4: {
                    this.g = 1;
                    this.h = 2;
                    this.k = 2;
                    this.l = 1;
                    break;
                }
                case 5: {
                    this.g = 2;
                    this.h = 1;
                    this.k = 1;
                    this.l = 2;
                }
            }
            this.l(uu2, n2, n3, n4);
            this.g = 0;
            this.h = 0;
            this.i = 0;
            this.j = 0;
            this.k = 0;
            this.l = 0;
        }
        return true;
    }

    private void a(double d2, double d3, double d4, double d5, double d6, double d7, float f2, double d8) {
        int n2 = 108;
        if (this.d >= 0) {
            n2 = this.d;
        }
        int n3 = (n2 & 0xF) << 4;
        int n4 = n2 & 0xF0;
        nw nw2 = nw.a;
        double d9 = (float)(n3 + 0) / 256.0f;
        double d10 = (float)(n4 + 0) / 256.0f;
        double d11 = ((double)n3 + d8 - 0.01) / 256.0;
        double d12 = ((double)((float)n4 + 4.0f) - 0.01) / 256.0;
        nw2.a(f2, f2, f2);
        nw2.a(d2, d5, d6, d11, d10);
        nw2.a(d2, d4, d6, d9, d10);
        nw2.a(d3, d4, d7, d9, d12);
        nw2.a(d3, d5, d7, d11, d12);
    }

    private void b(double d2, double d3, double d4, double d5, double d6, double d7, float f2, double d8) {
        int n2 = 108;
        if (this.d >= 0) {
            n2 = this.d;
        }
        int n3 = (n2 & 0xF) << 4;
        int n4 = n2 & 0xF0;
        nw nw2 = nw.a;
        double d9 = (float)(n3 + 0) / 256.0f;
        double d10 = (float)(n4 + 0) / 256.0f;
        double d11 = ((double)n3 + d8 - 0.01) / 256.0;
        double d12 = ((double)((float)n4 + 4.0f) - 0.01) / 256.0;
        nw2.a(f2, f2, f2);
        nw2.a(d2, d4, d7, d11, d10);
        nw2.a(d2, d4, d6, d9, d10);
        nw2.a(d3, d5, d6, d9, d12);
        nw2.a(d3, d5, d7, d11, d12);
    }

    private void c(double d2, double d3, double d4, double d5, double d6, double d7, float f2, double d8) {
        int n2 = 108;
        if (this.d >= 0) {
            n2 = this.d;
        }
        int n3 = (n2 & 0xF) << 4;
        int n4 = n2 & 0xF0;
        nw nw2 = nw.a;
        double d9 = (float)(n3 + 0) / 256.0f;
        double d10 = (float)(n4 + 0) / 256.0f;
        double d11 = ((double)n3 + d8 - 0.01) / 256.0;
        double d12 = ((double)((float)n4 + 4.0f) - 0.01) / 256.0;
        nw2.a(f2, f2, f2);
        nw2.a(d3, d4, d6, d11, d10);
        nw2.a(d2, d4, d6, d9, d10);
        nw2.a(d2, d5, d7, d9, d12);
        nw2.a(d3, d5, d7, d11, d12);
    }

    public void a(uu uu2, int n2, int n3, int n4, boolean bl2) {
        this.f = true;
        this.c(uu2, n2, n3, n4, bl2);
        this.f = false;
    }

    private boolean c(uu uu2, int n2, int n3, int n4, boolean bl2) {
        int n5 = this.c.e(n2, n3, n4);
        int n6 = h.c(n5);
        float f2 = uu2.d(this.c, n2, n3, n4);
        float f3 = bl2 ? 1.0f : 0.5f;
        double d2 = bl2 ? 16.0 : 8.0;
        switch (n6) {
            case 0: {
                this.g = 3;
                this.h = 3;
                this.i = 3;
                this.j = 3;
                uu2.a(0.0f, 0.0f, 0.0f, 1.0f, 0.25f, 1.0f);
                this.l(uu2, n2, n3, n4);
                this.a((float)n2 + 0.375f, (float)n2 + 0.625f, (float)n3 + 0.25f, (float)n3 + 0.25f + f3, (float)n4 + 0.625f, (float)n4 + 0.625f, f2 * 0.8f, d2);
                this.a((float)n2 + 0.625f, (float)n2 + 0.375f, (float)n3 + 0.25f, (float)n3 + 0.25f + f3, (float)n4 + 0.375f, (float)n4 + 0.375f, f2 * 0.8f, d2);
                this.a((float)n2 + 0.375f, (float)n2 + 0.375f, (float)n3 + 0.25f, (float)n3 + 0.25f + f3, (float)n4 + 0.375f, (float)n4 + 0.625f, f2 * 0.6f, d2);
                this.a((float)n2 + 0.625f, (float)n2 + 0.625f, (float)n3 + 0.25f, (float)n3 + 0.25f + f3, (float)n4 + 0.625f, (float)n4 + 0.375f, f2 * 0.6f, d2);
                break;
            }
            case 1: {
                uu2.a(0.0f, 0.75f, 0.0f, 1.0f, 1.0f, 1.0f);
                this.l(uu2, n2, n3, n4);
                this.a((float)n2 + 0.375f, (float)n2 + 0.625f, (float)n3 - 0.25f + 1.0f - f3, (float)n3 - 0.25f + 1.0f, (float)n4 + 0.625f, (float)n4 + 0.625f, f2 * 0.8f, d2);
                this.a((float)n2 + 0.625f, (float)n2 + 0.375f, (float)n3 - 0.25f + 1.0f - f3, (float)n3 - 0.25f + 1.0f, (float)n4 + 0.375f, (float)n4 + 0.375f, f2 * 0.8f, d2);
                this.a((float)n2 + 0.375f, (float)n2 + 0.375f, (float)n3 - 0.25f + 1.0f - f3, (float)n3 - 0.25f + 1.0f, (float)n4 + 0.375f, (float)n4 + 0.625f, f2 * 0.6f, d2);
                this.a((float)n2 + 0.625f, (float)n2 + 0.625f, (float)n3 - 0.25f + 1.0f - f3, (float)n3 - 0.25f + 1.0f, (float)n4 + 0.625f, (float)n4 + 0.375f, f2 * 0.6f, d2);
                break;
            }
            case 2: {
                this.i = 1;
                this.j = 2;
                uu2.a(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.25f);
                this.l(uu2, n2, n3, n4);
                this.b((float)n2 + 0.375f, (float)n2 + 0.375f, (float)n3 + 0.625f, (float)n3 + 0.375f, (float)n4 + 0.25f, (float)n4 + 0.25f + f3, f2 * 0.6f, d2);
                this.b((float)n2 + 0.625f, (float)n2 + 0.625f, (float)n3 + 0.375f, (float)n3 + 0.625f, (float)n4 + 0.25f, (float)n4 + 0.25f + f3, f2 * 0.6f, d2);
                this.b((float)n2 + 0.375f, (float)n2 + 0.625f, (float)n3 + 0.375f, (float)n3 + 0.375f, (float)n4 + 0.25f, (float)n4 + 0.25f + f3, f2 * 0.5f, d2);
                this.b((float)n2 + 0.625f, (float)n2 + 0.375f, (float)n3 + 0.625f, (float)n3 + 0.625f, (float)n4 + 0.25f, (float)n4 + 0.25f + f3, f2, d2);
                break;
            }
            case 3: {
                this.i = 2;
                this.j = 1;
                this.k = 3;
                this.l = 3;
                uu2.a(0.0f, 0.0f, 0.75f, 1.0f, 1.0f, 1.0f);
                this.l(uu2, n2, n3, n4);
                this.b((float)n2 + 0.375f, (float)n2 + 0.375f, (float)n3 + 0.625f, (float)n3 + 0.375f, (float)n4 - 0.25f + 1.0f - f3, (float)n4 - 0.25f + 1.0f, f2 * 0.6f, d2);
                this.b((float)n2 + 0.625f, (float)n2 + 0.625f, (float)n3 + 0.375f, (float)n3 + 0.625f, (float)n4 - 0.25f + 1.0f - f3, (float)n4 - 0.25f + 1.0f, f2 * 0.6f, d2);
                this.b((float)n2 + 0.375f, (float)n2 + 0.625f, (float)n3 + 0.375f, (float)n3 + 0.375f, (float)n4 - 0.25f + 1.0f - f3, (float)n4 - 0.25f + 1.0f, f2 * 0.5f, d2);
                this.b((float)n2 + 0.625f, (float)n2 + 0.375f, (float)n3 + 0.625f, (float)n3 + 0.625f, (float)n4 - 0.25f + 1.0f - f3, (float)n4 - 0.25f + 1.0f, f2, d2);
                break;
            }
            case 4: {
                this.g = 1;
                this.h = 2;
                this.k = 2;
                this.l = 1;
                uu2.a(0.0f, 0.0f, 0.0f, 0.25f, 1.0f, 1.0f);
                this.l(uu2, n2, n3, n4);
                this.c((float)n2 + 0.25f, (float)n2 + 0.25f + f3, (float)n3 + 0.375f, (float)n3 + 0.375f, (float)n4 + 0.625f, (float)n4 + 0.375f, f2 * 0.5f, d2);
                this.c((float)n2 + 0.25f, (float)n2 + 0.25f + f3, (float)n3 + 0.625f, (float)n3 + 0.625f, (float)n4 + 0.375f, (float)n4 + 0.625f, f2, d2);
                this.c((float)n2 + 0.25f, (float)n2 + 0.25f + f3, (float)n3 + 0.375f, (float)n3 + 0.625f, (float)n4 + 0.375f, (float)n4 + 0.375f, f2 * 0.6f, d2);
                this.c((float)n2 + 0.25f, (float)n2 + 0.25f + f3, (float)n3 + 0.625f, (float)n3 + 0.375f, (float)n4 + 0.625f, (float)n4 + 0.625f, f2 * 0.6f, d2);
                break;
            }
            case 5: {
                this.g = 2;
                this.h = 1;
                this.k = 1;
                this.l = 2;
                uu2.a(0.75f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
                this.l(uu2, n2, n3, n4);
                this.c((float)n2 - 0.25f + 1.0f - f3, (float)n2 - 0.25f + 1.0f, (float)n3 + 0.375f, (float)n3 + 0.375f, (float)n4 + 0.625f, (float)n4 + 0.375f, f2 * 0.5f, d2);
                this.c((float)n2 - 0.25f + 1.0f - f3, (float)n2 - 0.25f + 1.0f, (float)n3 + 0.625f, (float)n3 + 0.625f, (float)n4 + 0.375f, (float)n4 + 0.625f, f2, d2);
                this.c((float)n2 - 0.25f + 1.0f - f3, (float)n2 - 0.25f + 1.0f, (float)n3 + 0.375f, (float)n3 + 0.625f, (float)n4 + 0.375f, (float)n4 + 0.375f, f2 * 0.6f, d2);
                this.c((float)n2 - 0.25f + 1.0f - f3, (float)n2 - 0.25f + 1.0f, (float)n3 + 0.625f, (float)n3 + 0.375f, (float)n4 + 0.625f, (float)n4 + 0.625f, f2 * 0.6f, d2);
            }
        }
        this.g = 0;
        this.h = 0;
        this.i = 0;
        this.j = 0;
        this.k = 0;
        this.l = 0;
        uu2.a(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
        return true;
    }

    public boolean e(uu uu2, int n2, int n3, int n4) {
        boolean bl2;
        int n5 = this.c.e(n2, n3, n4);
        int n6 = n5 & 7;
        boolean bl3 = (n5 & 8) > 0;
        nw nw2 = nw.a;
        boolean bl4 = bl2 = this.d >= 0;
        if (!bl2) {
            this.d = uu.x.bm;
        }
        float f2 = 0.25f;
        float f3 = 0.1875f;
        float f4 = 0.1875f;
        if (n6 == 5) {
            uu2.a(0.5f - f3, 0.0f, 0.5f - f2, 0.5f + f3, f4, 0.5f + f2);
        } else if (n6 == 6) {
            uu2.a(0.5f - f2, 0.0f, 0.5f - f3, 0.5f + f2, f4, 0.5f + f3);
        } else if (n6 == 4) {
            uu2.a(0.5f - f3, 0.5f - f2, 1.0f - f4, 0.5f + f3, 0.5f + f2, 1.0f);
        } else if (n6 == 3) {
            uu2.a(0.5f - f3, 0.5f - f2, 0.0f, 0.5f + f3, 0.5f + f2, f4);
        } else if (n6 == 2) {
            uu2.a(1.0f - f4, 0.5f - f2, 0.5f - f3, 1.0f, 0.5f + f2, 0.5f + f3);
        } else if (n6 == 1) {
            uu2.a(0.0f, 0.5f - f2, 0.5f - f3, f4, 0.5f + f2, 0.5f + f3);
        }
        this.l(uu2, n2, n3, n4);
        if (!bl2) {
            this.d = -1;
        }
        float f5 = uu2.d(this.c, n2, n3, n4);
        if (uu.s[uu2.bn] > 0) {
            f5 = 1.0f;
        }
        nw2.a(f5, f5, f5);
        int n7 = uu2.a(0);
        if (this.d >= 0) {
            n7 = this.d;
        }
        int n8 = (n7 & 0xF) << 4;
        int n9 = n7 & 0xF0;
        float f6 = (float)n8 / 256.0f;
        float f7 = ((float)n8 + 15.99f) / 256.0f;
        float f8 = (float)n9 / 256.0f;
        float f9 = ((float)n9 + 15.99f) / 256.0f;
        bt[] btArray = new bt[8];
        float f10 = 0.0625f;
        float f11 = 0.0625f;
        float f12 = 0.625f;
        btArray[0] = bt.b(-f10, 0.0, -f11);
        btArray[1] = bt.b(f10, 0.0, -f11);
        btArray[2] = bt.b(f10, 0.0, f11);
        btArray[3] = bt.b(-f10, 0.0, f11);
        btArray[4] = bt.b(-f10, f12, -f11);
        btArray[5] = bt.b(f10, f12, -f11);
        btArray[6] = bt.b(f10, f12, f11);
        btArray[7] = bt.b(-f10, f12, f11);
        for (int i2 = 0; i2 < 8; ++i2) {
            if (bl3) {
                btArray[i2].c -= 0.0625;
                btArray[i2].a(0.69813174f);
            } else {
                btArray[i2].c += 0.0625;
                btArray[i2].a(-0.69813174f);
            }
            if (n6 == 6) {
                btArray[i2].b(1.5707964f);
            }
            if (n6 < 5) {
                btArray[i2].b -= 0.375;
                btArray[i2].a(1.5707964f);
                if (n6 == 4) {
                    btArray[i2].b(0.0f);
                }
                if (n6 == 3) {
                    btArray[i2].b((float)Math.PI);
                }
                if (n6 == 2) {
                    btArray[i2].b(1.5707964f);
                }
                if (n6 == 1) {
                    btArray[i2].b(-1.5707964f);
                }
                btArray[i2].a += (double)n2 + 0.5;
                btArray[i2].b += (double)((float)n3 + 0.5f);
                btArray[i2].c += (double)n4 + 0.5;
                continue;
            }
            btArray[i2].a += (double)n2 + 0.5;
            btArray[i2].b += (double)((float)n3 + 0.125f);
            btArray[i2].c += (double)n4 + 0.5;
        }
        bt bt2 = null;
        bt bt3 = null;
        bt bt4 = null;
        bt bt5 = null;
        for (int i3 = 0; i3 < 6; ++i3) {
            if (i3 == 0) {
                f6 = (float)(n8 + 7) / 256.0f;
                f7 = ((float)(n8 + 9) - 0.01f) / 256.0f;
                f8 = (float)(n9 + 6) / 256.0f;
                f9 = ((float)(n9 + 8) - 0.01f) / 256.0f;
            } else if (i3 == 2) {
                f6 = (float)(n8 + 7) / 256.0f;
                f7 = ((float)(n8 + 9) - 0.01f) / 256.0f;
                f8 = (float)(n9 + 6) / 256.0f;
                f9 = ((float)(n9 + 16) - 0.01f) / 256.0f;
            }
            if (i3 == 0) {
                bt2 = btArray[0];
                bt3 = btArray[1];
                bt4 = btArray[2];
                bt5 = btArray[3];
            } else if (i3 == 1) {
                bt2 = btArray[7];
                bt3 = btArray[6];
                bt4 = btArray[5];
                bt5 = btArray[4];
            } else if (i3 == 2) {
                bt2 = btArray[1];
                bt3 = btArray[0];
                bt4 = btArray[4];
                bt5 = btArray[5];
            } else if (i3 == 3) {
                bt2 = btArray[2];
                bt3 = btArray[1];
                bt4 = btArray[5];
                bt5 = btArray[6];
            } else if (i3 == 4) {
                bt2 = btArray[3];
                bt3 = btArray[2];
                bt4 = btArray[6];
                bt5 = btArray[7];
            } else if (i3 == 5) {
                bt2 = btArray[0];
                bt3 = btArray[3];
                bt4 = btArray[7];
                bt5 = btArray[4];
            }
            nw2.a(bt2.a, bt2.b, bt2.c, f6, f9);
            nw2.a(bt3.a, bt3.b, bt3.c, f7, f9);
            nw2.a(bt4.a, bt4.b, bt4.c, f7, f8);
            nw2.a(bt5.a, bt5.b, bt5.c, f6, f8);
        }
        return true;
    }

    public boolean f(uu uu2, int n2, int n3, int n4) {
        nw nw2 = nw.a;
        int n5 = uu2.a(0);
        if (this.d >= 0) {
            n5 = this.d;
        }
        float f2 = uu2.d(this.c, n2, n3, n4);
        nw2.a(f2, f2, f2);
        int n6 = (n5 & 0xF) << 4;
        int n7 = n5 & 0xF0;
        double d2 = (float)n6 / 256.0f;
        double d3 = ((float)n6 + 15.99f) / 256.0f;
        double d4 = (float)n7 / 256.0f;
        double d5 = ((float)n7 + 15.99f) / 256.0f;
        float f3 = 1.4f;
        if (this.c.h(n2, n3 - 1, n4) || uu.as.c(this.c, n2, n3 - 1, n4)) {
            double d6 = (double)n2 + 0.5 + 0.2;
            double d7 = (double)n2 + 0.5 - 0.2;
            double d8 = (double)n4 + 0.5 + 0.2;
            double d9 = (double)n4 + 0.5 - 0.2;
            double d10 = (double)n2 + 0.5 - 0.3;
            double d11 = (double)n2 + 0.5 + 0.3;
            double d12 = (double)n4 + 0.5 - 0.3;
            double d13 = (double)n4 + 0.5 + 0.3;
            nw2.a(d10, (float)n3 + f3, n4 + 1, d3, d4);
            nw2.a(d6, n3 + 0, n4 + 1, d3, d5);
            nw2.a(d6, n3 + 0, n4 + 0, d2, d5);
            nw2.a(d10, (float)n3 + f3, n4 + 0, d2, d4);
            nw2.a(d11, (float)n3 + f3, n4 + 0, d3, d4);
            nw2.a(d7, n3 + 0, n4 + 0, d3, d5);
            nw2.a(d7, n3 + 0, n4 + 1, d2, d5);
            nw2.a(d11, (float)n3 + f3, n4 + 1, d2, d4);
            d2 = (float)n6 / 256.0f;
            d3 = ((float)n6 + 15.99f) / 256.0f;
            d4 = (float)(n7 + 16) / 256.0f;
            d5 = ((float)n7 + 15.99f + 16.0f) / 256.0f;
            nw2.a(n2 + 1, (float)n3 + f3, d13, d3, d4);
            nw2.a(n2 + 1, n3 + 0, d9, d3, d5);
            nw2.a(n2 + 0, n3 + 0, d9, d2, d5);
            nw2.a(n2 + 0, (float)n3 + f3, d13, d2, d4);
            nw2.a(n2 + 0, (float)n3 + f3, d12, d3, d4);
            nw2.a(n2 + 0, n3 + 0, d8, d3, d5);
            nw2.a(n2 + 1, n3 + 0, d8, d2, d5);
            nw2.a(n2 + 1, (float)n3 + f3, d12, d2, d4);
            d6 = (double)n2 + 0.5 - 0.5;
            d7 = (double)n2 + 0.5 + 0.5;
            d8 = (double)n4 + 0.5 - 0.5;
            d9 = (double)n4 + 0.5 + 0.5;
            d10 = (double)n2 + 0.5 - 0.4;
            d11 = (double)n2 + 0.5 + 0.4;
            d12 = (double)n4 + 0.5 - 0.4;
            d13 = (double)n4 + 0.5 + 0.4;
            nw2.a(d10, (float)n3 + f3, n4 + 0, d2, d4);
            nw2.a(d6, n3 + 0, n4 + 0, d2, d5);
            nw2.a(d6, n3 + 0, n4 + 1, d3, d5);
            nw2.a(d10, (float)n3 + f3, n4 + 1, d3, d4);
            nw2.a(d11, (float)n3 + f3, n4 + 1, d2, d4);
            nw2.a(d7, n3 + 0, n4 + 1, d2, d5);
            nw2.a(d7, n3 + 0, n4 + 0, d3, d5);
            nw2.a(d11, (float)n3 + f3, n4 + 0, d3, d4);
            d2 = (float)n6 / 256.0f;
            d3 = ((float)n6 + 15.99f) / 256.0f;
            d4 = (float)n7 / 256.0f;
            d5 = ((float)n7 + 15.99f) / 256.0f;
            nw2.a(n2 + 0, (float)n3 + f3, d13, d2, d4);
            nw2.a(n2 + 0, n3 + 0, d9, d2, d5);
            nw2.a(n2 + 1, n3 + 0, d9, d3, d5);
            nw2.a(n2 + 1, (float)n3 + f3, d13, d3, d4);
            nw2.a(n2 + 1, (float)n3 + f3, d12, d2, d4);
            nw2.a(n2 + 1, n3 + 0, d8, d2, d5);
            nw2.a(n2 + 0, n3 + 0, d8, d3, d5);
            nw2.a(n2 + 0, (float)n3 + f3, d12, d3, d4);
        } else {
            double d14;
            float f4 = 0.2f;
            float f5 = 0.0625f;
            if ((n2 + n3 + n4 & 1) == 1) {
                d2 = (float)n6 / 256.0f;
                d3 = ((float)n6 + 15.99f) / 256.0f;
                d4 = (float)(n7 + 16) / 256.0f;
                d5 = ((float)n7 + 15.99f + 16.0f) / 256.0f;
            }
            if ((n2 / 2 + n3 / 2 + n4 / 2 & 1) == 1) {
                d14 = d3;
                d3 = d2;
                d2 = d14;
            }
            if (uu.as.c(this.c, n2 - 1, n3, n4)) {
                nw2.a((float)n2 + f4, (float)n3 + f3 + f5, n4 + 1, d3, d4);
                nw2.a(n2 + 0, (float)(n3 + 0) + f5, n4 + 1, d3, d5);
                nw2.a(n2 + 0, (float)(n3 + 0) + f5, n4 + 0, d2, d5);
                nw2.a((float)n2 + f4, (float)n3 + f3 + f5, n4 + 0, d2, d4);
                nw2.a((float)n2 + f4, (float)n3 + f3 + f5, n4 + 0, d2, d4);
                nw2.a(n2 + 0, (float)(n3 + 0) + f5, n4 + 0, d2, d5);
                nw2.a(n2 + 0, (float)(n3 + 0) + f5, n4 + 1, d3, d5);
                nw2.a((float)n2 + f4, (float)n3 + f3 + f5, n4 + 1, d3, d4);
            }
            if (uu.as.c(this.c, n2 + 1, n3, n4)) {
                nw2.a((float)(n2 + 1) - f4, (float)n3 + f3 + f5, n4 + 0, d2, d4);
                nw2.a(n2 + 1 - 0, (float)(n3 + 0) + f5, n4 + 0, d2, d5);
                nw2.a(n2 + 1 - 0, (float)(n3 + 0) + f5, n4 + 1, d3, d5);
                nw2.a((float)(n2 + 1) - f4, (float)n3 + f3 + f5, n4 + 1, d3, d4);
                nw2.a((float)(n2 + 1) - f4, (float)n3 + f3 + f5, n4 + 1, d3, d4);
                nw2.a(n2 + 1 - 0, (float)(n3 + 0) + f5, n4 + 1, d3, d5);
                nw2.a(n2 + 1 - 0, (float)(n3 + 0) + f5, n4 + 0, d2, d5);
                nw2.a((float)(n2 + 1) - f4, (float)n3 + f3 + f5, n4 + 0, d2, d4);
            }
            if (uu.as.c(this.c, n2, n3, n4 - 1)) {
                nw2.a(n2 + 0, (float)n3 + f3 + f5, (float)n4 + f4, d3, d4);
                nw2.a(n2 + 0, (float)(n3 + 0) + f5, n4 + 0, d3, d5);
                nw2.a(n2 + 1, (float)(n3 + 0) + f5, n4 + 0, d2, d5);
                nw2.a(n2 + 1, (float)n3 + f3 + f5, (float)n4 + f4, d2, d4);
                nw2.a(n2 + 1, (float)n3 + f3 + f5, (float)n4 + f4, d2, d4);
                nw2.a(n2 + 1, (float)(n3 + 0) + f5, n4 + 0, d2, d5);
                nw2.a(n2 + 0, (float)(n3 + 0) + f5, n4 + 0, d3, d5);
                nw2.a(n2 + 0, (float)n3 + f3 + f5, (float)n4 + f4, d3, d4);
            }
            if (uu.as.c(this.c, n2, n3, n4 + 1)) {
                nw2.a(n2 + 1, (float)n3 + f3 + f5, (float)(n4 + 1) - f4, d2, d4);
                nw2.a(n2 + 1, (float)(n3 + 0) + f5, n4 + 1 - 0, d2, d5);
                nw2.a(n2 + 0, (float)(n3 + 0) + f5, n4 + 1 - 0, d3, d5);
                nw2.a(n2 + 0, (float)n3 + f3 + f5, (float)(n4 + 1) - f4, d3, d4);
                nw2.a(n2 + 0, (float)n3 + f3 + f5, (float)(n4 + 1) - f4, d3, d4);
                nw2.a(n2 + 0, (float)(n3 + 0) + f5, n4 + 1 - 0, d3, d5);
                nw2.a(n2 + 1, (float)(n3 + 0) + f5, n4 + 1 - 0, d2, d5);
                nw2.a(n2 + 1, (float)n3 + f3 + f5, (float)(n4 + 1) - f4, d2, d4);
            }
            if (uu.as.c(this.c, n2, n3 + 1, n4)) {
                d14 = (double)n2 + 0.5 + 0.5;
                double d15 = (double)n2 + 0.5 - 0.5;
                double d16 = (double)n4 + 0.5 + 0.5;
                double d17 = (double)n4 + 0.5 - 0.5;
                double d18 = (double)n2 + 0.5 - 0.5;
                double d19 = (double)n2 + 0.5 + 0.5;
                double d20 = (double)n4 + 0.5 - 0.5;
                double d21 = (double)n4 + 0.5 + 0.5;
                d2 = (float)n6 / 256.0f;
                d3 = ((float)n6 + 15.99f) / 256.0f;
                d4 = (float)n7 / 256.0f;
                d5 = ((float)n7 + 15.99f) / 256.0f;
                f3 = -0.2f;
                if ((n2 + ++n3 + n4 & 1) == 0) {
                    nw2.a(d18, (float)n3 + f3, n4 + 0, d3, d4);
                    nw2.a(d14, n3 + 0, n4 + 0, d3, d5);
                    nw2.a(d14, n3 + 0, n4 + 1, d2, d5);
                    nw2.a(d18, (float)n3 + f3, n4 + 1, d2, d4);
                    d2 = (float)n6 / 256.0f;
                    d3 = ((float)n6 + 15.99f) / 256.0f;
                    d4 = (float)(n7 + 16) / 256.0f;
                    d5 = ((float)n7 + 15.99f + 16.0f) / 256.0f;
                    nw2.a(d19, (float)n3 + f3, n4 + 1, d3, d4);
                    nw2.a(d15, n3 + 0, n4 + 1, d3, d5);
                    nw2.a(d15, n3 + 0, n4 + 0, d2, d5);
                    nw2.a(d19, (float)n3 + f3, n4 + 0, d2, d4);
                } else {
                    nw2.a(n2 + 0, (float)n3 + f3, d21, d3, d4);
                    nw2.a(n2 + 0, n3 + 0, d17, d3, d5);
                    nw2.a(n2 + 1, n3 + 0, d17, d2, d5);
                    nw2.a(n2 + 1, (float)n3 + f3, d21, d2, d4);
                    d2 = (float)n6 / 256.0f;
                    d3 = ((float)n6 + 15.99f) / 256.0f;
                    d4 = (float)(n7 + 16) / 256.0f;
                    d5 = ((float)n7 + 15.99f + 16.0f) / 256.0f;
                    nw2.a(n2 + 1, (float)n3 + f3, d20, d3, d4);
                    nw2.a(n2 + 1, n3 + 0, d16, d3, d5);
                    nw2.a(n2 + 0, n3 + 0, d16, d2, d5);
                    nw2.a(n2 + 0, (float)n3 + f3, d20, d2, d4);
                }
            }
        }
        return true;
    }

    public boolean g(uu uu2, int n2, int n3, int n4) {
        boolean bl2;
        nw nw2 = nw.a;
        int n5 = this.c.e(n2, n3, n4);
        int n6 = uu2.a(1, n5);
        if (this.d >= 0) {
            n6 = this.d;
        }
        float f2 = uu2.d(this.c, n2, n3, n4);
        float f3 = (float)n5 / 15.0f;
        float f4 = f3 * 0.6f + 0.4f;
        if (n5 == 0) {
            f4 = 0.3f;
        }
        float f5 = f3 * f3 * 0.7f - 0.5f;
        float f6 = f3 * f3 * 0.6f - 0.7f;
        if (f5 < 0.0f) {
            f5 = 0.0f;
        }
        if (f6 < 0.0f) {
            f6 = 0.0f;
        }
        nw2.a(f2 * f4, f2 * f5, f2 * f6);
        int n7 = (n6 & 0xF) << 4;
        int n8 = n6 & 0xF0;
        double d2 = (float)n7 / 256.0f;
        double d3 = ((float)n7 + 15.99f) / 256.0f;
        double d4 = (float)n8 / 256.0f;
        double d5 = ((float)n8 + 15.99f) / 256.0f;
        boolean bl3 = sm.e(this.c, n2 - 1, n3, n4, 1) || !this.c.h(n2 - 1, n3, n4) && sm.e(this.c, n2 - 1, n3 - 1, n4, -1);
        boolean bl4 = sm.e(this.c, n2 + 1, n3, n4, 3) || !this.c.h(n2 + 1, n3, n4) && sm.e(this.c, n2 + 1, n3 - 1, n4, -1);
        boolean bl5 = sm.e(this.c, n2, n3, n4 - 1, 2) || !this.c.h(n2, n3, n4 - 1) && sm.e(this.c, n2, n3 - 1, n4 - 1, -1);
        boolean bl6 = bl2 = sm.e(this.c, n2, n3, n4 + 1, 0) || !this.c.h(n2, n3, n4 + 1) && sm.e(this.c, n2, n3 - 1, n4 + 1, -1);
        if (!this.c.h(n2, n3 + 1, n4)) {
            if (this.c.h(n2 - 1, n3, n4) && sm.e(this.c, n2 - 1, n3 + 1, n4, -1)) {
                bl3 = true;
            }
            if (this.c.h(n2 + 1, n3, n4) && sm.e(this.c, n2 + 1, n3 + 1, n4, -1)) {
                bl4 = true;
            }
            if (this.c.h(n2, n3, n4 - 1) && sm.e(this.c, n2, n3 + 1, n4 - 1, -1)) {
                bl5 = true;
            }
            if (this.c.h(n2, n3, n4 + 1) && sm.e(this.c, n2, n3 + 1, n4 + 1, -1)) {
                bl2 = true;
            }
        }
        float f7 = n2 + 0;
        float f8 = n2 + 1;
        float f9 = n4 + 0;
        float f10 = n4 + 1;
        int n9 = 0;
        if ((bl3 || bl4) && !bl5 && !bl2) {
            n9 = 1;
        }
        if ((bl5 || bl2) && !bl4 && !bl3) {
            n9 = 2;
        }
        if (n9 != 0) {
            d2 = (float)(n7 + 16) / 256.0f;
            d3 = ((float)(n7 + 16) + 15.99f) / 256.0f;
            d4 = (float)n8 / 256.0f;
            d5 = ((float)n8 + 15.99f) / 256.0f;
        }
        if (n9 == 0) {
            if (bl4 || bl5 || bl2 || bl3) {
                if (!bl3) {
                    f7 += 0.3125f;
                }
                if (!bl3) {
                    d2 += 0.01953125;
                }
                if (!bl4) {
                    f8 -= 0.3125f;
                }
                if (!bl4) {
                    d3 -= 0.01953125;
                }
                if (!bl5) {
                    f9 += 0.3125f;
                }
                if (!bl5) {
                    d4 += 0.01953125;
                }
                if (!bl2) {
                    f10 -= 0.3125f;
                }
                if (!bl2) {
                    d5 -= 0.01953125;
                }
            }
            nw2.a(f8, (float)n3 + 0.015625f, f10, d3, d5);
            nw2.a(f8, (float)n3 + 0.015625f, f9, d3, d4);
            nw2.a(f7, (float)n3 + 0.015625f, f9, d2, d4);
            nw2.a(f7, (float)n3 + 0.015625f, f10, d2, d5);
            nw2.a(f2, f2, f2);
            nw2.a(f8, (float)n3 + 0.015625f, f10, d3, d5 + 0.0625);
            nw2.a(f8, (float)n3 + 0.015625f, f9, d3, d4 + 0.0625);
            nw2.a(f7, (float)n3 + 0.015625f, f9, d2, d4 + 0.0625);
            nw2.a(f7, (float)n3 + 0.015625f, f10, d2, d5 + 0.0625);
        } else if (n9 == 1) {
            nw2.a(f8, (float)n3 + 0.015625f, f10, d3, d5);
            nw2.a(f8, (float)n3 + 0.015625f, f9, d3, d4);
            nw2.a(f7, (float)n3 + 0.015625f, f9, d2, d4);
            nw2.a(f7, (float)n3 + 0.015625f, f10, d2, d5);
            nw2.a(f2, f2, f2);
            nw2.a(f8, (float)n3 + 0.015625f, f10, d3, d5 + 0.0625);
            nw2.a(f8, (float)n3 + 0.015625f, f9, d3, d4 + 0.0625);
            nw2.a(f7, (float)n3 + 0.015625f, f9, d2, d4 + 0.0625);
            nw2.a(f7, (float)n3 + 0.015625f, f10, d2, d5 + 0.0625);
        } else if (n9 == 2) {
            nw2.a(f8, (float)n3 + 0.015625f, f10, d3, d5);
            nw2.a(f8, (float)n3 + 0.015625f, f9, d2, d5);
            nw2.a(f7, (float)n3 + 0.015625f, f9, d2, d4);
            nw2.a(f7, (float)n3 + 0.015625f, f10, d3, d4);
            nw2.a(f2, f2, f2);
            nw2.a(f8, (float)n3 + 0.015625f, f10, d3, d5 + 0.0625);
            nw2.a(f8, (float)n3 + 0.015625f, f9, d2, d5 + 0.0625);
            nw2.a(f7, (float)n3 + 0.015625f, f9, d2, d4 + 0.0625);
            nw2.a(f7, (float)n3 + 0.015625f, f10, d3, d4 + 0.0625);
        }
        if (!this.c.h(n2, n3 + 1, n4)) {
            d2 = (float)(n7 + 16) / 256.0f;
            d3 = ((float)(n7 + 16) + 15.99f) / 256.0f;
            d4 = (float)n8 / 256.0f;
            d5 = ((float)n8 + 15.99f) / 256.0f;
            if (this.c.h(n2 - 1, n3, n4) && this.c.a(n2 - 1, n3 + 1, n4) == uu.aw.bn) {
                nw2.a(f2 * f4, f2 * f5, f2 * f6);
                nw2.a((float)n2 + 0.015625f, (float)(n3 + 1) + 0.021875f, n4 + 1, d3, d4);
                nw2.a((float)n2 + 0.015625f, n3 + 0, n4 + 1, d2, d4);
                nw2.a((float)n2 + 0.015625f, n3 + 0, n4 + 0, d2, d5);
                nw2.a((float)n2 + 0.015625f, (float)(n3 + 1) + 0.021875f, n4 + 0, d3, d5);
                nw2.a(f2, f2, f2);
                nw2.a((float)n2 + 0.015625f, (float)(n3 + 1) + 0.021875f, n4 + 1, d3, d4 + 0.0625);
                nw2.a((float)n2 + 0.015625f, n3 + 0, n4 + 1, d2, d4 + 0.0625);
                nw2.a((float)n2 + 0.015625f, n3 + 0, n4 + 0, d2, d5 + 0.0625);
                nw2.a((float)n2 + 0.015625f, (float)(n3 + 1) + 0.021875f, n4 + 0, d3, d5 + 0.0625);
            }
            if (this.c.h(n2 + 1, n3, n4) && this.c.a(n2 + 1, n3 + 1, n4) == uu.aw.bn) {
                nw2.a(f2 * f4, f2 * f5, f2 * f6);
                nw2.a((float)(n2 + 1) - 0.015625f, n3 + 0, n4 + 1, d2, d5);
                nw2.a((float)(n2 + 1) - 0.015625f, (float)(n3 + 1) + 0.021875f, n4 + 1, d3, d5);
                nw2.a((float)(n2 + 1) - 0.015625f, (float)(n3 + 1) + 0.021875f, n4 + 0, d3, d4);
                nw2.a((float)(n2 + 1) - 0.015625f, n3 + 0, n4 + 0, d2, d4);
                nw2.a(f2, f2, f2);
                nw2.a((float)(n2 + 1) - 0.015625f, n3 + 0, n4 + 1, d2, d5 + 0.0625);
                nw2.a((float)(n2 + 1) - 0.015625f, (float)(n3 + 1) + 0.021875f, n4 + 1, d3, d5 + 0.0625);
                nw2.a((float)(n2 + 1) - 0.015625f, (float)(n3 + 1) + 0.021875f, n4 + 0, d3, d4 + 0.0625);
                nw2.a((float)(n2 + 1) - 0.015625f, n3 + 0, n4 + 0, d2, d4 + 0.0625);
            }
            if (this.c.h(n2, n3, n4 - 1) && this.c.a(n2, n3 + 1, n4 - 1) == uu.aw.bn) {
                nw2.a(f2 * f4, f2 * f5, f2 * f6);
                nw2.a(n2 + 1, n3 + 0, (float)n4 + 0.015625f, d2, d5);
                nw2.a(n2 + 1, (float)(n3 + 1) + 0.021875f, (float)n4 + 0.015625f, d3, d5);
                nw2.a(n2 + 0, (float)(n3 + 1) + 0.021875f, (float)n4 + 0.015625f, d3, d4);
                nw2.a(n2 + 0, n3 + 0, (float)n4 + 0.015625f, d2, d4);
                nw2.a(f2, f2, f2);
                nw2.a(n2 + 1, n3 + 0, (float)n4 + 0.015625f, d2, d5 + 0.0625);
                nw2.a(n2 + 1, (float)(n3 + 1) + 0.021875f, (float)n4 + 0.015625f, d3, d5 + 0.0625);
                nw2.a(n2 + 0, (float)(n3 + 1) + 0.021875f, (float)n4 + 0.015625f, d3, d4 + 0.0625);
                nw2.a(n2 + 0, n3 + 0, (float)n4 + 0.015625f, d2, d4 + 0.0625);
            }
            if (this.c.h(n2, n3, n4 + 1) && this.c.a(n2, n3 + 1, n4 + 1) == uu.aw.bn) {
                nw2.a(f2 * f4, f2 * f5, f2 * f6);
                nw2.a(n2 + 1, (float)(n3 + 1) + 0.021875f, (float)(n4 + 1) - 0.015625f, d3, d4);
                nw2.a(n2 + 1, n3 + 0, (float)(n4 + 1) - 0.015625f, d2, d4);
                nw2.a(n2 + 0, n3 + 0, (float)(n4 + 1) - 0.015625f, d2, d5);
                nw2.a(n2 + 0, (float)(n3 + 1) + 0.021875f, (float)(n4 + 1) - 0.015625f, d3, d5);
                nw2.a(f2, f2, f2);
                nw2.a(n2 + 1, (float)(n3 + 1) + 0.021875f, (float)(n4 + 1) - 0.015625f, d3, d4 + 0.0625);
                nw2.a(n2 + 1, n3 + 0, (float)(n4 + 1) - 0.015625f, d2, d4 + 0.0625);
                nw2.a(n2 + 0, n3 + 0, (float)(n4 + 1) - 0.015625f, d2, d5 + 0.0625);
                nw2.a(n2 + 0, (float)(n3 + 1) + 0.021875f, (float)(n4 + 1) - 0.015625f, d3, d5 + 0.0625);
            }
        }
        return true;
    }

    public boolean a(pc pc2, int n2, int n3, int n4) {
        nw nw2 = nw.a;
        int n5 = this.c.e(n2, n3, n4);
        int n6 = pc2.a(0, n5);
        if (this.d >= 0) {
            n6 = this.d;
        }
        if (pc2.i()) {
            n5 &= 7;
        }
        float f2 = pc2.d(this.c, n2, n3, n4);
        nw2.a(f2, f2, f2);
        int n7 = (n6 & 0xF) << 4;
        int n8 = n6 & 0xF0;
        double d2 = (float)n7 / 256.0f;
        double d3 = ((float)n7 + 15.99f) / 256.0f;
        double d4 = (float)n8 / 256.0f;
        double d5 = ((float)n8 + 15.99f) / 256.0f;
        float f3 = 0.0625f;
        float f4 = n2 + 1;
        float f5 = n2 + 1;
        float f6 = n2 + 0;
        float f7 = n2 + 0;
        float f8 = n4 + 0;
        float f9 = n4 + 1;
        float f10 = n4 + 1;
        float f11 = n4 + 0;
        float f12 = (float)n3 + f3;
        float f13 = (float)n3 + f3;
        float f14 = (float)n3 + f3;
        float f15 = (float)n3 + f3;
        if (n5 == 1 || n5 == 2 || n5 == 3 || n5 == 7) {
            f4 = f7 = (float)(n2 + 1);
            f5 = f6 = (float)(n2 + 0);
            f8 = f9 = (float)(n4 + 1);
            f10 = f11 = (float)(n4 + 0);
        } else if (n5 == 8) {
            f4 = f5 = (float)(n2 + 0);
            f6 = f7 = (float)(n2 + 1);
            f8 = f11 = (float)(n4 + 1);
            f9 = f10 = (float)(n4 + 0);
        } else if (n5 == 9) {
            f4 = f7 = (float)(n2 + 0);
            f5 = f6 = (float)(n2 + 1);
            f8 = f9 = (float)(n4 + 0);
            f10 = f11 = (float)(n4 + 1);
        }
        if (n5 == 2 || n5 == 4) {
            f12 += 1.0f;
            f15 += 1.0f;
        } else if (n5 == 3 || n5 == 5) {
            f13 += 1.0f;
            f14 += 1.0f;
        }
        nw2.a(f4, f12, f8, d3, d4);
        nw2.a(f5, f13, f9, d3, d5);
        nw2.a(f6, f14, f10, d2, d5);
        nw2.a(f7, f15, f11, d2, d4);
        nw2.a(f7, f15, f11, d2, d4);
        nw2.a(f6, f14, f10, d2, d5);
        nw2.a(f5, f13, f9, d3, d5);
        nw2.a(f4, f12, f8, d3, d4);
        return true;
    }

    public boolean h(uu uu2, int n2, int n3, int n4) {
        nw nw2 = nw.a;
        int n5 = uu2.a(0);
        if (this.d >= 0) {
            n5 = this.d;
        }
        float f2 = uu2.d(this.c, n2, n3, n4);
        nw2.a(f2, f2, f2);
        int n6 = (n5 & 0xF) << 4;
        int n7 = n5 & 0xF0;
        double d2 = (float)n6 / 256.0f;
        double d3 = ((float)n6 + 15.99f) / 256.0f;
        double d4 = (float)n7 / 256.0f;
        double d5 = ((float)n7 + 15.99f) / 256.0f;
        int n8 = this.c.e(n2, n3, n4);
        float f3 = 0.0f;
        float f4 = 0.05f;
        if (n8 == 5) {
            nw2.a((float)n2 + f4, (float)(n3 + 1) + f3, (float)(n4 + 1) + f3, d2, d4);
            nw2.a((float)n2 + f4, (float)(n3 + 0) - f3, (float)(n4 + 1) + f3, d2, d5);
            nw2.a((float)n2 + f4, (float)(n3 + 0) - f3, (float)(n4 + 0) - f3, d3, d5);
            nw2.a((float)n2 + f4, (float)(n3 + 1) + f3, (float)(n4 + 0) - f3, d3, d4);
        }
        if (n8 == 4) {
            nw2.a((float)(n2 + 1) - f4, (float)(n3 + 0) - f3, (float)(n4 + 1) + f3, d3, d5);
            nw2.a((float)(n2 + 1) - f4, (float)(n3 + 1) + f3, (float)(n4 + 1) + f3, d3, d4);
            nw2.a((float)(n2 + 1) - f4, (float)(n3 + 1) + f3, (float)(n4 + 0) - f3, d2, d4);
            nw2.a((float)(n2 + 1) - f4, (float)(n3 + 0) - f3, (float)(n4 + 0) - f3, d2, d5);
        }
        if (n8 == 3) {
            nw2.a((float)(n2 + 1) + f3, (float)(n3 + 0) - f3, (float)n4 + f4, d3, d5);
            nw2.a((float)(n2 + 1) + f3, (float)(n3 + 1) + f3, (float)n4 + f4, d3, d4);
            nw2.a((float)(n2 + 0) - f3, (float)(n3 + 1) + f3, (float)n4 + f4, d2, d4);
            nw2.a((float)(n2 + 0) - f3, (float)(n3 + 0) - f3, (float)n4 + f4, d2, d5);
        }
        if (n8 == 2) {
            nw2.a((float)(n2 + 1) + f3, (float)(n3 + 1) + f3, (float)(n4 + 1) - f4, d2, d4);
            nw2.a((float)(n2 + 1) + f3, (float)(n3 + 0) - f3, (float)(n4 + 1) - f4, d2, d5);
            nw2.a((float)(n2 + 0) - f3, (float)(n3 + 0) - f3, (float)(n4 + 1) - f4, d3, d5);
            nw2.a((float)(n2 + 0) - f3, (float)(n3 + 1) + f3, (float)(n4 + 1) - f4, d3, d4);
        }
        return true;
    }

    public boolean i(uu uu2, int n2, int n3, int n4) {
        nw nw2 = nw.a;
        float f2 = uu2.d(this.c, n2, n3, n4);
        int n5 = uu2.b(this.c, n2, n3, n4);
        float f3 = (float)(n5 >> 16 & 0xFF) / 255.0f;
        float f4 = (float)(n5 >> 8 & 0xFF) / 255.0f;
        float f5 = (float)(n5 & 0xFF) / 255.0f;
        if (px.a) {
            float f6 = (f3 * 30.0f + f4 * 59.0f + f5 * 11.0f) / 100.0f;
            float f7 = (f3 * 30.0f + f4 * 70.0f) / 100.0f;
            float f8 = (f3 * 30.0f + f5 * 70.0f) / 100.0f;
            f3 = f6;
            f4 = f7;
            f5 = f8;
        }
        nw2.a(f2 * f3, f2 * f4, f2 * f5);
        double d2 = n2;
        double d3 = n3;
        double d4 = n4;
        if (uu2 == uu.Y) {
            long l2 = (long)(n2 * 3129871) ^ (long)n4 * 116129781L ^ (long)n3;
            l2 = l2 * l2 * 42317861L + l2 * 11L;
            d2 += ((double)((float)(l2 >> 16 & 0xFL) / 15.0f) - 0.5) * 0.5;
            d3 += ((double)((float)(l2 >> 20 & 0xFL) / 15.0f) - 1.0) * 0.2;
            d4 += ((double)((float)(l2 >> 24 & 0xFL) / 15.0f) - 0.5) * 0.5;
        }
        this.a(uu2, this.c.e(n2, n3, n4), d2, d3, d4);
        return true;
    }

    public boolean j(uu uu2, int n2, int n3, int n4) {
        nw nw2 = nw.a;
        float f2 = uu2.d(this.c, n2, n3, n4);
        nw2.a(f2, f2, f2);
        this.b(uu2, this.c.e(n2, n3, n4), (double)n2, (double)((float)n3 - 0.0625f), (double)n4);
        return true;
    }

    public void a(uu uu2, double d2, double d3, double d4, double d5, double d6) {
        nw nw2 = nw.a;
        int n2 = uu2.a(0);
        if (this.d >= 0) {
            n2 = this.d;
        }
        int n3 = (n2 & 0xF) << 4;
        int n4 = n2 & 0xF0;
        float f2 = (float)n3 / 256.0f;
        float f3 = ((float)n3 + 15.99f) / 256.0f;
        float f4 = (float)n4 / 256.0f;
        float f5 = ((float)n4 + 15.99f) / 256.0f;
        double d7 = (double)f2 + 0.02734375;
        double d8 = (double)f4 + 0.0234375;
        double d9 = (double)f2 + 0.03515625;
        double d10 = (double)f4 + 0.03125;
        double d11 = (d2 += 0.5) - 0.5;
        double d12 = d2 + 0.5;
        double d13 = (d4 += 0.5) - 0.5;
        double d14 = d4 + 0.5;
        double d15 = 0.0625;
        double d16 = 0.625;
        nw2.a(d2 + d5 * (1.0 - d16) - d15, d3 + d16, d4 + d6 * (1.0 - d16) - d15, d7, d8);
        nw2.a(d2 + d5 * (1.0 - d16) - d15, d3 + d16, d4 + d6 * (1.0 - d16) + d15, d7, d10);
        nw2.a(d2 + d5 * (1.0 - d16) + d15, d3 + d16, d4 + d6 * (1.0 - d16) + d15, d9, d10);
        nw2.a(d2 + d5 * (1.0 - d16) + d15, d3 + d16, d4 + d6 * (1.0 - d16) - d15, d9, d8);
        nw2.a(d2 - d15, d3 + 1.0, d13, f2, f4);
        nw2.a(d2 - d15 + d5, d3 + 0.0, d13 + d6, f2, f5);
        nw2.a(d2 - d15 + d5, d3 + 0.0, d14 + d6, f3, f5);
        nw2.a(d2 - d15, d3 + 1.0, d14, f3, f4);
        nw2.a(d2 + d15, d3 + 1.0, d14, f2, f4);
        nw2.a(d2 + d5 + d15, d3 + 0.0, d14 + d6, f2, f5);
        nw2.a(d2 + d5 + d15, d3 + 0.0, d13 + d6, f3, f5);
        nw2.a(d2 + d15, d3 + 1.0, d13, f3, f4);
        nw2.a(d11, d3 + 1.0, d4 + d15, f2, f4);
        nw2.a(d11 + d5, d3 + 0.0, d4 + d15 + d6, f2, f5);
        nw2.a(d12 + d5, d3 + 0.0, d4 + d15 + d6, f3, f5);
        nw2.a(d12, d3 + 1.0, d4 + d15, f3, f4);
        nw2.a(d12, d3 + 1.0, d4 - d15, f2, f4);
        nw2.a(d12 + d5, d3 + 0.0, d4 - d15 + d6, f2, f5);
        nw2.a(d11 + d5, d3 + 0.0, d4 - d15 + d6, f3, f5);
        nw2.a(d11, d3 + 1.0, d4 - d15, f3, f4);
    }

    public void a(uu uu2, int n2, double d2, double d3, double d4) {
        nw nw2 = nw.a;
        int n3 = uu2.a(0, n2);
        if (this.d >= 0) {
            n3 = this.d;
        }
        int n4 = (n3 & 0xF) << 4;
        int n5 = n3 & 0xF0;
        double d5 = (float)n4 / 256.0f;
        double d6 = ((float)n4 + 15.99f) / 256.0f;
        double d7 = (float)n5 / 256.0f;
        double d8 = ((float)n5 + 15.99f) / 256.0f;
        double d9 = d2 + 0.5 - (double)0.45f;
        double d10 = d2 + 0.5 + (double)0.45f;
        double d11 = d4 + 0.5 - (double)0.45f;
        double d12 = d4 + 0.5 + (double)0.45f;
        nw2.a(d9, d3 + 1.0, d11, d5, d7);
        nw2.a(d9, d3 + 0.0, d11, d5, d8);
        nw2.a(d10, d3 + 0.0, d12, d6, d8);
        nw2.a(d10, d3 + 1.0, d12, d6, d7);
        nw2.a(d10, d3 + 1.0, d12, d5, d7);
        nw2.a(d10, d3 + 0.0, d12, d5, d8);
        nw2.a(d9, d3 + 0.0, d11, d6, d8);
        nw2.a(d9, d3 + 1.0, d11, d6, d7);
        nw2.a(d9, d3 + 1.0, d12, d5, d7);
        nw2.a(d9, d3 + 0.0, d12, d5, d8);
        nw2.a(d10, d3 + 0.0, d11, d6, d8);
        nw2.a(d10, d3 + 1.0, d11, d6, d7);
        nw2.a(d10, d3 + 1.0, d11, d5, d7);
        nw2.a(d10, d3 + 0.0, d11, d5, d8);
        nw2.a(d9, d3 + 0.0, d12, d6, d8);
        nw2.a(d9, d3 + 1.0, d12, d6, d7);
    }

    public void b(uu uu2, int n2, double d2, double d3, double d4) {
        nw nw2 = nw.a;
        int n3 = uu2.a(0, n2);
        if (this.d >= 0) {
            n3 = this.d;
        }
        int n4 = (n3 & 0xF) << 4;
        int n5 = n3 & 0xF0;
        double d5 = (float)n4 / 256.0f;
        double d6 = ((float)n4 + 15.99f) / 256.0f;
        double d7 = (float)n5 / 256.0f;
        double d8 = ((float)n5 + 15.99f) / 256.0f;
        double d9 = d2 + 0.5 - 0.25;
        double d10 = d2 + 0.5 + 0.25;
        double d11 = d4 + 0.5 - 0.5;
        double d12 = d4 + 0.5 + 0.5;
        nw2.a(d9, d3 + 1.0, d11, d5, d7);
        nw2.a(d9, d3 + 0.0, d11, d5, d8);
        nw2.a(d9, d3 + 0.0, d12, d6, d8);
        nw2.a(d9, d3 + 1.0, d12, d6, d7);
        nw2.a(d9, d3 + 1.0, d12, d5, d7);
        nw2.a(d9, d3 + 0.0, d12, d5, d8);
        nw2.a(d9, d3 + 0.0, d11, d6, d8);
        nw2.a(d9, d3 + 1.0, d11, d6, d7);
        nw2.a(d10, d3 + 1.0, d12, d5, d7);
        nw2.a(d10, d3 + 0.0, d12, d5, d8);
        nw2.a(d10, d3 + 0.0, d11, d6, d8);
        nw2.a(d10, d3 + 1.0, d11, d6, d7);
        nw2.a(d10, d3 + 1.0, d11, d5, d7);
        nw2.a(d10, d3 + 0.0, d11, d5, d8);
        nw2.a(d10, d3 + 0.0, d12, d6, d8);
        nw2.a(d10, d3 + 1.0, d12, d6, d7);
        d9 = d2 + 0.5 - 0.5;
        d10 = d2 + 0.5 + 0.5;
        d11 = d4 + 0.5 - 0.25;
        d12 = d4 + 0.5 + 0.25;
        nw2.a(d9, d3 + 1.0, d11, d5, d7);
        nw2.a(d9, d3 + 0.0, d11, d5, d8);
        nw2.a(d10, d3 + 0.0, d11, d6, d8);
        nw2.a(d10, d3 + 1.0, d11, d6, d7);
        nw2.a(d10, d3 + 1.0, d11, d5, d7);
        nw2.a(d10, d3 + 0.0, d11, d5, d8);
        nw2.a(d9, d3 + 0.0, d11, d6, d8);
        nw2.a(d9, d3 + 1.0, d11, d6, d7);
        nw2.a(d10, d3 + 1.0, d12, d5, d7);
        nw2.a(d10, d3 + 0.0, d12, d5, d8);
        nw2.a(d9, d3 + 0.0, d12, d6, d8);
        nw2.a(d9, d3 + 1.0, d12, d6, d7);
        nw2.a(d9, d3 + 1.0, d12, d5, d7);
        nw2.a(d9, d3 + 0.0, d12, d5, d8);
        nw2.a(d10, d3 + 0.0, d12, d6, d8);
        nw2.a(d10, d3 + 1.0, d12, d6, d7);
    }

    public boolean k(uu uu2, int n2, int n3, int n4) {
        float f2;
        float f3;
        float f4;
        int n5;
        int n6;
        nw nw2 = nw.a;
        int n7 = uu2.b(this.c, n2, n3, n4);
        float f5 = (float)(n7 >> 16 & 0xFF) / 255.0f;
        float f6 = (float)(n7 >> 8 & 0xFF) / 255.0f;
        float f7 = (float)(n7 & 0xFF) / 255.0f;
        boolean bl2 = uu2.b(this.c, n2, n3 + 1, n4, 1);
        boolean bl3 = uu2.b(this.c, n2, n3 - 1, n4, 0);
        boolean[] blArray = new boolean[]{uu2.b(this.c, n2, n3, n4 - 1, 2), uu2.b(this.c, n2, n3, n4 + 1, 3), uu2.b(this.c, n2 - 1, n3, n4, 4), uu2.b(this.c, n2 + 1, n3, n4, 5)};
        if (!(bl2 || bl3 || blArray[0] || blArray[1] || blArray[2] || blArray[3])) {
            return false;
        }
        boolean bl4 = false;
        float f8 = 0.5f;
        float f9 = 1.0f;
        float f10 = 0.8f;
        float f11 = 0.6f;
        double d2 = 0.0;
        double d3 = 1.0;
        ln ln2 = uu2.bA;
        int n8 = this.c.e(n2, n3, n4);
        float f12 = this.a(n2, n3, n4, ln2);
        float f13 = this.a(n2, n3, n4 + 1, ln2);
        float f14 = this.a(n2 + 1, n3, n4 + 1, ln2);
        float f15 = this.a(n2 + 1, n3, n4, ln2);
        if (this.f || bl2) {
            bl4 = true;
            int n9 = uu2.a(1, n8);
            float f16 = (float)rp.a(this.c, n2, n3, n4, ln2);
            if (f16 > -999.0f) {
                n9 = uu2.a(2, n8);
            }
            n6 = (n9 & 0xF) << 4;
            n5 = n9 & 0xF0;
            double d4 = ((double)n6 + 8.0) / 256.0;
            double d5 = ((double)n5 + 8.0) / 256.0;
            if (f16 < -999.0f) {
                f16 = 0.0f;
            } else {
                d4 = (float)(n6 + 16) / 256.0f;
                d5 = (float)(n5 + 16) / 256.0f;
            }
            f4 = in.a(f16) * 8.0f / 256.0f;
            f3 = in.b(f16) * 8.0f / 256.0f;
            f2 = uu2.d(this.c, n2, n3, n4);
            nw2.a(f9 * f2 * f5, f9 * f2 * f6, f9 * f2 * f7);
            nw2.a(n2 + 0, (float)n3 + f12, n4 + 0, d4 - (double)f3 - (double)f4, d5 - (double)f3 + (double)f4);
            nw2.a(n2 + 0, (float)n3 + f13, n4 + 1, d4 - (double)f3 + (double)f4, d5 + (double)f3 + (double)f4);
            nw2.a(n2 + 1, (float)n3 + f14, n4 + 1, d4 + (double)f3 + (double)f4, d5 + (double)f3 - (double)f4);
            nw2.a(n2 + 1, (float)n3 + f15, n4 + 0, d4 + (double)f3 - (double)f4, d5 - (double)f3 - (double)f4);
        }
        if (this.f || bl3) {
            float f17 = uu2.d(this.c, n2, n3 - 1, n4);
            nw2.a(f8 * f17, f8 * f17, f8 * f17);
            this.a(uu2, (double)n2, (double)n3, (double)n4, uu2.a(0));
            bl4 = true;
        }
        for (int i2 = 0; i2 < 4; ++i2) {
            float f18;
            float f19;
            float f20;
            int n10 = n2;
            n6 = n3;
            n5 = n4;
            if (i2 == 0) {
                --n5;
            }
            if (i2 == 1) {
                ++n5;
            }
            if (i2 == 2) {
                --n10;
            }
            if (i2 == 3) {
                ++n10;
            }
            int n11 = uu2.a(i2 + 2, n8);
            int n12 = (n11 & 0xF) << 4;
            int n13 = n11 & 0xF0;
            if (!this.f && !blArray[i2]) continue;
            if (i2 == 0) {
                f20 = f12;
                f4 = f15;
                f3 = n2;
                f19 = n2 + 1;
                f2 = n4;
                f18 = n4;
            } else if (i2 == 1) {
                f20 = f14;
                f4 = f13;
                f3 = n2 + 1;
                f19 = n2;
                f2 = n4 + 1;
                f18 = n4 + 1;
            } else if (i2 == 2) {
                f20 = f13;
                f4 = f12;
                f3 = n2;
                f19 = n2;
                f2 = n4 + 1;
                f18 = n4;
            } else {
                f20 = f15;
                f4 = f14;
                f3 = n2 + 1;
                f19 = n2 + 1;
                f2 = n4;
                f18 = n4 + 1;
            }
            bl4 = true;
            double d6 = (float)(n12 + 0) / 256.0f;
            double d7 = ((double)(n12 + 16) - 0.01) / 256.0;
            double d8 = ((float)n13 + (1.0f - f20) * 16.0f) / 256.0f;
            double d9 = ((float)n13 + (1.0f - f4) * 16.0f) / 256.0f;
            double d10 = ((double)(n13 + 16) - 0.01) / 256.0;
            float f21 = uu2.d(this.c, n10, n6, n5);
            f21 = i2 < 2 ? (f21 *= f10) : (f21 *= f11);
            nw2.a(f9 * f21 * f5, f9 * f21 * f6, f9 * f21 * f7);
            nw2.a(f3, (float)n3 + f20, f2, d6, d8);
            nw2.a(f19, (float)n3 + f4, f18, d7, d9);
            nw2.a(f19, n3 + 0, f18, d7, d10);
            nw2.a(f3, n3 + 0, f2, d6, d10);
        }
        uu2.bt = d2;
        uu2.bw = d3;
        return bl4;
    }

    private float a(int n2, int n3, int n4, ln ln2) {
        int n5 = 0;
        float f2 = 0.0f;
        for (int i2 = 0; i2 < 4; ++i2) {
            int n6 = n2 - (i2 & 1);
            int n7 = n3;
            int n8 = n4 - (i2 >> 1 & 1);
            if (this.c.f(n6, n7 + 1, n8) == ln2) {
                return 1.0f;
            }
            ln ln3 = this.c.f(n6, n7, n8);
            if (ln3 == ln2) {
                int n9 = this.c.e(n6, n7, n8);
                if (n9 >= 8 || n9 == 0) {
                    f2 += rp.d(n9) * 10.0f;
                    n5 += 10;
                }
                f2 += rp.d(n9);
                ++n5;
                continue;
            }
            if (ln3.a()) continue;
            f2 += 1.0f;
            ++n5;
        }
        return 1.0f - f2 / (float)n5;
    }

    public void a(uu uu2, fd fd2, int n2, int n3, int n4) {
        float f2 = 0.5f;
        float f3 = 1.0f;
        float f4 = 0.8f;
        float f5 = 0.6f;
        nw nw2 = nw.a;
        nw2.b();
        float f6 = uu2.d((xp)fd2, n2, n3, n4);
        float f7 = uu2.d((xp)fd2, n2, n3 - 1, n4);
        if (f7 < f6) {
            f7 = f6;
        }
        nw2.a(f2 * f7, f2 * f7, f2 * f7);
        this.a(uu2, -0.5, -0.5, -0.5, uu2.a(0));
        f7 = uu2.d((xp)fd2, n2, n3 + 1, n4);
        if (f7 < f6) {
            f7 = f6;
        }
        nw2.a(f3 * f7, f3 * f7, f3 * f7);
        this.b(uu2, -0.5, -0.5, -0.5, uu2.a(1));
        f7 = uu2.d((xp)fd2, n2, n3, n4 - 1);
        if (f7 < f6) {
            f7 = f6;
        }
        nw2.a(f4 * f7, f4 * f7, f4 * f7);
        this.c(uu2, -0.5, -0.5, -0.5, uu2.a(2));
        f7 = uu2.d((xp)fd2, n2, n3, n4 + 1);
        if (f7 < f6) {
            f7 = f6;
        }
        nw2.a(f4 * f7, f4 * f7, f4 * f7);
        this.d(uu2, -0.5, -0.5, -0.5, uu2.a(3));
        f7 = uu2.d((xp)fd2, n2 - 1, n3, n4);
        if (f7 < f6) {
            f7 = f6;
        }
        nw2.a(f5 * f7, f5 * f7, f5 * f7);
        this.e(uu2, -0.5, -0.5, -0.5, uu2.a(4));
        f7 = uu2.d((xp)fd2, n2 + 1, n3, n4);
        if (f7 < f6) {
            f7 = f6;
        }
        nw2.a(f5 * f7, f5 * f7, f5 * f7);
        this.f(uu2, -0.5, -0.5, -0.5, uu2.a(5));
        nw2.a();
    }

    public boolean l(uu uu2, int n2, int n3, int n4) {
        int n5 = uu2.b(this.c, n2, n3, n4);
        float f2 = (float)(n5 >> 16 & 0xFF) / 255.0f;
        float f3 = (float)(n5 >> 8 & 0xFF) / 255.0f;
        float f4 = (float)(n5 & 0xFF) / 255.0f;
        if (px.a) {
            float f5 = (f2 * 30.0f + f3 * 59.0f + f4 * 11.0f) / 100.0f;
            float f6 = (f2 * 30.0f + f3 * 70.0f) / 100.0f;
            float f7 = (f2 * 30.0f + f4 * 70.0f) / 100.0f;
            f2 = f5;
            f3 = f6;
            f4 = f7;
        }
        if (Minecraft.v()) {
            return this.a(uu2, n2, n3, n4, f2, f3, f4);
        }
        return this.b(uu2, n2, n3, n4, f2, f3, f4);
    }

    public boolean a(uu uu2, int n2, int n3, int n4, float f2, float f3, float f4) {
        int n5;
        this.m = true;
        boolean bl2 = false;
        float f5 = this.n;
        float f6 = this.n;
        float f7 = this.n;
        float f8 = this.n;
        boolean bl3 = true;
        boolean bl4 = true;
        boolean bl5 = true;
        boolean bl6 = true;
        boolean bl7 = true;
        boolean bl8 = true;
        this.n = uu2.d(this.c, n2, n3, n4);
        this.o = uu2.d(this.c, n2 - 1, n3, n4);
        this.p = uu2.d(this.c, n2, n3 - 1, n4);
        this.q = uu2.d(this.c, n2, n3, n4 - 1);
        this.r = uu2.d(this.c, n2 + 1, n3, n4);
        this.s = uu2.d(this.c, n2, n3 + 1, n4);
        this.t = uu2.d(this.c, n2, n3, n4 + 1);
        this.ac = uu.r[this.c.a(n2 + 1, n3 + 1, n4)];
        this.ak = uu.r[this.c.a(n2 + 1, n3 - 1, n4)];
        this.ag = uu.r[this.c.a(n2 + 1, n3, n4 + 1)];
        this.ai = uu.r[this.c.a(n2 + 1, n3, n4 - 1)];
        this.ad = uu.r[this.c.a(n2 - 1, n3 + 1, n4)];
        this.al = uu.r[this.c.a(n2 - 1, n3 - 1, n4)];
        this.af = uu.r[this.c.a(n2 - 1, n3, n4 - 1)];
        this.ah = uu.r[this.c.a(n2 - 1, n3, n4 + 1)];
        this.ae = uu.r[this.c.a(n2, n3 + 1, n4 + 1)];
        this.ab = uu.r[this.c.a(n2, n3 + 1, n4 - 1)];
        this.am = uu.r[this.c.a(n2, n3 - 1, n4 + 1)];
        this.aj = uu.r[this.c.a(n2, n3 - 1, n4 - 1)];
        if (uu2.bm == 3) {
            bl8 = false;
            bl7 = false;
            bl6 = false;
            bl5 = false;
            bl3 = false;
        }
        if (this.d >= 0) {
            bl8 = false;
            bl7 = false;
            bl6 = false;
            bl5 = false;
            bl3 = false;
        }
        if (this.f || uu2.b(this.c, n2, n3 - 1, n4, 0)) {
            if (this.O > 0) {
                this.v = uu2.d(this.c, n2 - 1, --n3, n4);
                this.x = uu2.d(this.c, n2, n3, n4 - 1);
                this.y = uu2.d(this.c, n2, n3, n4 + 1);
                this.A = uu2.d(this.c, n2 + 1, n3, n4);
                this.u = this.aj || this.al ? uu2.d(this.c, n2 - 1, n3, n4 - 1) : this.v;
                this.w = this.am || this.al ? uu2.d(this.c, n2 - 1, n3, n4 + 1) : this.v;
                this.z = this.aj || this.ak ? uu2.d(this.c, n2 + 1, n3, n4 - 1) : this.A;
                this.B = this.am || this.ak ? uu2.d(this.c, n2 + 1, n3, n4 + 1) : this.A;
                ++n3;
                f5 = (this.w + this.v + this.y + this.p) / 4.0f;
                f8 = (this.y + this.p + this.B + this.A) / 4.0f;
                f7 = (this.p + this.x + this.A + this.z) / 4.0f;
                f6 = (this.v + this.u + this.p + this.x) / 4.0f;
            } else {
                f7 = f8 = this.p;
                f6 = f8;
                f5 = f8;
            }
            this.R = this.S = (bl3 ? f2 : 1.0f) * 0.5f;
            this.Q = this.S;
            this.P = this.S;
            this.V = this.W = (bl3 ? f3 : 1.0f) * 0.5f;
            this.U = this.W;
            this.T = this.W;
            this.Z = this.aa = (bl3 ? f4 : 1.0f) * 0.5f;
            this.Y = this.aa;
            this.X = this.aa;
            this.P *= f5;
            this.T *= f5;
            this.X *= f5;
            this.Q *= f6;
            this.U *= f6;
            this.Y *= f6;
            this.R *= f7;
            this.V *= f7;
            this.Z *= f7;
            this.S *= f8;
            this.W *= f8;
            this.aa *= f8;
            this.a(uu2, (double)n2, (double)n3, (double)n4, uu2.a(this.c, n2, n3, n4, 0));
            bl2 = true;
        }
        if (this.f || uu2.b(this.c, n2, n3 + 1, n4, 1)) {
            if (this.O > 0) {
                this.D = uu2.d(this.c, n2 - 1, ++n3, n4);
                this.H = uu2.d(this.c, n2 + 1, n3, n4);
                this.F = uu2.d(this.c, n2, n3, n4 - 1);
                this.I = uu2.d(this.c, n2, n3, n4 + 1);
                this.C = this.ab || this.ad ? uu2.d(this.c, n2 - 1, n3, n4 - 1) : this.D;
                this.G = this.ab || this.ac ? uu2.d(this.c, n2 + 1, n3, n4 - 1) : this.H;
                this.E = this.ae || this.ad ? uu2.d(this.c, n2 - 1, n3, n4 + 1) : this.D;
                this.J = this.ae || this.ac ? uu2.d(this.c, n2 + 1, n3, n4 + 1) : this.H;
                --n3;
                f8 = (this.E + this.D + this.I + this.s) / 4.0f;
                f5 = (this.I + this.s + this.J + this.H) / 4.0f;
                f6 = (this.s + this.F + this.H + this.G) / 4.0f;
                f7 = (this.D + this.C + this.s + this.F) / 4.0f;
            } else {
                f7 = f8 = this.s;
                f6 = f8;
                f5 = f8;
            }
            this.S = bl4 ? f2 : 1.0f;
            this.R = this.S;
            this.Q = this.S;
            this.P = this.S;
            this.W = bl4 ? f3 : 1.0f;
            this.V = this.W;
            this.U = this.W;
            this.T = this.W;
            this.aa = bl4 ? f4 : 1.0f;
            this.Z = this.aa;
            this.Y = this.aa;
            this.X = this.aa;
            this.P *= f5;
            this.T *= f5;
            this.X *= f5;
            this.Q *= f6;
            this.U *= f6;
            this.Y *= f6;
            this.R *= f7;
            this.V *= f7;
            this.Z *= f7;
            this.S *= f8;
            this.W *= f8;
            this.aa *= f8;
            this.b(uu2, (double)n2, (double)n3, (double)n4, uu2.a(this.c, n2, n3, n4, 1));
            bl2 = true;
        }
        if (this.f || uu2.b(this.c, n2, n3, n4 - 1, 2)) {
            if (this.O > 0) {
                this.K = uu2.d(this.c, n2 - 1, n3, --n4);
                this.x = uu2.d(this.c, n2, n3 - 1, n4);
                this.F = uu2.d(this.c, n2, n3 + 1, n4);
                this.L = uu2.d(this.c, n2 + 1, n3, n4);
                this.u = this.af || this.aj ? uu2.d(this.c, n2 - 1, n3 - 1, n4) : this.K;
                this.C = this.af || this.ab ? uu2.d(this.c, n2 - 1, n3 + 1, n4) : this.K;
                this.z = this.ai || this.aj ? uu2.d(this.c, n2 + 1, n3 - 1, n4) : this.L;
                this.G = this.ai || this.ab ? uu2.d(this.c, n2 + 1, n3 + 1, n4) : this.L;
                ++n4;
                f5 = (this.K + this.C + this.q + this.F) / 4.0f;
                f6 = (this.q + this.F + this.L + this.G) / 4.0f;
                f7 = (this.x + this.q + this.z + this.L) / 4.0f;
                f8 = (this.u + this.K + this.x + this.q) / 4.0f;
            } else {
                f7 = f8 = this.q;
                f6 = f8;
                f5 = f8;
            }
            this.R = this.S = (bl5 ? f2 : 1.0f) * 0.8f;
            this.Q = this.S;
            this.P = this.S;
            this.V = this.W = (bl5 ? f3 : 1.0f) * 0.8f;
            this.U = this.W;
            this.T = this.W;
            this.Z = this.aa = (bl5 ? f4 : 1.0f) * 0.8f;
            this.Y = this.aa;
            this.X = this.aa;
            this.P *= f5;
            this.T *= f5;
            this.X *= f5;
            this.Q *= f6;
            this.U *= f6;
            this.Y *= f6;
            this.R *= f7;
            this.V *= f7;
            this.Z *= f7;
            this.S *= f8;
            this.W *= f8;
            this.aa *= f8;
            n5 = uu2.a(this.c, n2, n3, n4, 2);
            this.c(uu2, (double)n2, (double)n3, (double)n4, n5);
            if (a && n5 == 3 && this.d < 0) {
                this.P *= f2;
                this.Q *= f2;
                this.R *= f2;
                this.S *= f2;
                this.T *= f3;
                this.U *= f3;
                this.V *= f3;
                this.W *= f3;
                this.X *= f4;
                this.Y *= f4;
                this.Z *= f4;
                this.aa *= f4;
                this.c(uu2, (double)n2, (double)n3, (double)n4, 38);
            }
            bl2 = true;
        }
        if (this.f || uu2.b(this.c, n2, n3, n4 + 1, 3)) {
            if (this.O > 0) {
                this.M = uu2.d(this.c, n2 - 1, n3, ++n4);
                this.N = uu2.d(this.c, n2 + 1, n3, n4);
                this.y = uu2.d(this.c, n2, n3 - 1, n4);
                this.I = uu2.d(this.c, n2, n3 + 1, n4);
                this.w = this.ah || this.am ? uu2.d(this.c, n2 - 1, n3 - 1, n4) : this.M;
                this.E = this.ah || this.ae ? uu2.d(this.c, n2 - 1, n3 + 1, n4) : this.M;
                this.B = this.ag || this.am ? uu2.d(this.c, n2 + 1, n3 - 1, n4) : this.N;
                this.J = this.ag || this.ae ? uu2.d(this.c, n2 + 1, n3 + 1, n4) : this.N;
                --n4;
                f5 = (this.M + this.E + this.t + this.I) / 4.0f;
                f8 = (this.t + this.I + this.N + this.J) / 4.0f;
                f7 = (this.y + this.t + this.B + this.N) / 4.0f;
                f6 = (this.w + this.M + this.y + this.t) / 4.0f;
            } else {
                f7 = f8 = this.t;
                f6 = f8;
                f5 = f8;
            }
            this.R = this.S = (bl6 ? f2 : 1.0f) * 0.8f;
            this.Q = this.S;
            this.P = this.S;
            this.V = this.W = (bl6 ? f3 : 1.0f) * 0.8f;
            this.U = this.W;
            this.T = this.W;
            this.Z = this.aa = (bl6 ? f4 : 1.0f) * 0.8f;
            this.Y = this.aa;
            this.X = this.aa;
            this.P *= f5;
            this.T *= f5;
            this.X *= f5;
            this.Q *= f6;
            this.U *= f6;
            this.Y *= f6;
            this.R *= f7;
            this.V *= f7;
            this.Z *= f7;
            this.S *= f8;
            this.W *= f8;
            this.aa *= f8;
            n5 = uu2.a(this.c, n2, n3, n4, 3);
            this.d(uu2, n2, n3, n4, uu2.a(this.c, n2, n3, n4, 3));
            if (a && n5 == 3 && this.d < 0) {
                this.P *= f2;
                this.Q *= f2;
                this.R *= f2;
                this.S *= f2;
                this.T *= f3;
                this.U *= f3;
                this.V *= f3;
                this.W *= f3;
                this.X *= f4;
                this.Y *= f4;
                this.Z *= f4;
                this.aa *= f4;
                this.d(uu2, n2, n3, n4, 38);
            }
            bl2 = true;
        }
        if (this.f || uu2.b(this.c, n2 - 1, n3, n4, 4)) {
            if (this.O > 0) {
                this.v = uu2.d(this.c, --n2, n3 - 1, n4);
                this.K = uu2.d(this.c, n2, n3, n4 - 1);
                this.M = uu2.d(this.c, n2, n3, n4 + 1);
                this.D = uu2.d(this.c, n2, n3 + 1, n4);
                this.u = this.af || this.al ? uu2.d(this.c, n2, n3 - 1, n4 - 1) : this.K;
                this.w = this.ah || this.al ? uu2.d(this.c, n2, n3 - 1, n4 + 1) : this.M;
                this.C = this.af || this.ad ? uu2.d(this.c, n2, n3 + 1, n4 - 1) : this.K;
                this.E = this.ah || this.ad ? uu2.d(this.c, n2, n3 + 1, n4 + 1) : this.M;
                ++n2;
                f8 = (this.v + this.w + this.o + this.M) / 4.0f;
                f5 = (this.o + this.M + this.D + this.E) / 4.0f;
                f6 = (this.K + this.o + this.C + this.D) / 4.0f;
                f7 = (this.u + this.v + this.K + this.o) / 4.0f;
            } else {
                f7 = f8 = this.o;
                f6 = f8;
                f5 = f8;
            }
            this.R = this.S = (bl7 ? f2 : 1.0f) * 0.6f;
            this.Q = this.S;
            this.P = this.S;
            this.V = this.W = (bl7 ? f3 : 1.0f) * 0.6f;
            this.U = this.W;
            this.T = this.W;
            this.Z = this.aa = (bl7 ? f4 : 1.0f) * 0.6f;
            this.Y = this.aa;
            this.X = this.aa;
            this.P *= f5;
            this.T *= f5;
            this.X *= f5;
            this.Q *= f6;
            this.U *= f6;
            this.Y *= f6;
            this.R *= f7;
            this.V *= f7;
            this.Z *= f7;
            this.S *= f8;
            this.W *= f8;
            this.aa *= f8;
            n5 = uu2.a(this.c, n2, n3, n4, 4);
            this.e(uu2, n2, n3, n4, n5);
            if (a && n5 == 3 && this.d < 0) {
                this.P *= f2;
                this.Q *= f2;
                this.R *= f2;
                this.S *= f2;
                this.T *= f3;
                this.U *= f3;
                this.V *= f3;
                this.W *= f3;
                this.X *= f4;
                this.Y *= f4;
                this.Z *= f4;
                this.aa *= f4;
                this.e(uu2, n2, n3, n4, 38);
            }
            bl2 = true;
        }
        if (this.f || uu2.b(this.c, n2 + 1, n3, n4, 5)) {
            if (this.O > 0) {
                this.A = uu2.d(this.c, ++n2, n3 - 1, n4);
                this.L = uu2.d(this.c, n2, n3, n4 - 1);
                this.N = uu2.d(this.c, n2, n3, n4 + 1);
                this.H = uu2.d(this.c, n2, n3 + 1, n4);
                this.z = this.ak || this.ai ? uu2.d(this.c, n2, n3 - 1, n4 - 1) : this.L;
                this.B = this.ak || this.ag ? uu2.d(this.c, n2, n3 - 1, n4 + 1) : this.N;
                this.G = this.ac || this.ai ? uu2.d(this.c, n2, n3 + 1, n4 - 1) : this.L;
                this.J = this.ac || this.ag ? uu2.d(this.c, n2, n3 + 1, n4 + 1) : this.N;
                --n2;
                f5 = (this.A + this.B + this.r + this.N) / 4.0f;
                f8 = (this.r + this.N + this.H + this.J) / 4.0f;
                f7 = (this.L + this.r + this.G + this.H) / 4.0f;
                f6 = (this.z + this.A + this.L + this.r) / 4.0f;
            } else {
                f7 = f8 = this.r;
                f6 = f8;
                f5 = f8;
            }
            this.R = this.S = (bl8 ? f2 : 1.0f) * 0.6f;
            this.Q = this.S;
            this.P = this.S;
            this.V = this.W = (bl8 ? f3 : 1.0f) * 0.6f;
            this.U = this.W;
            this.T = this.W;
            this.Z = this.aa = (bl8 ? f4 : 1.0f) * 0.6f;
            this.Y = this.aa;
            this.X = this.aa;
            this.P *= f5;
            this.T *= f5;
            this.X *= f5;
            this.Q *= f6;
            this.U *= f6;
            this.Y *= f6;
            this.R *= f7;
            this.V *= f7;
            this.Z *= f7;
            this.S *= f8;
            this.W *= f8;
            this.aa *= f8;
            n5 = uu2.a(this.c, n2, n3, n4, 5);
            this.f(uu2, n2, n3, n4, n5);
            if (a && n5 == 3 && this.d < 0) {
                this.P *= f2;
                this.Q *= f2;
                this.R *= f2;
                this.S *= f2;
                this.T *= f3;
                this.U *= f3;
                this.V *= f3;
                this.W *= f3;
                this.X *= f4;
                this.Y *= f4;
                this.Z *= f4;
                this.aa *= f4;
                this.f(uu2, n2, n3, n4, 38);
            }
            bl2 = true;
        }
        this.m = false;
        return bl2;
    }

    public boolean b(uu uu2, int n2, int n3, int n4, float f2, float f3, float f4) {
        int n5;
        float f5;
        this.m = false;
        nw nw2 = nw.a;
        boolean bl2 = false;
        float f6 = 0.5f;
        float f7 = 1.0f;
        float f8 = 0.8f;
        float f9 = 0.6f;
        float f10 = f7 * f2;
        float f11 = f7 * f3;
        float f12 = f7 * f4;
        float f13 = f6;
        float f14 = f8;
        float f15 = f9;
        float f16 = f6;
        float f17 = f8;
        float f18 = f9;
        float f19 = f6;
        float f20 = f8;
        float f21 = f9;
        if (uu2 != uu.v) {
            f13 *= f2;
            f14 *= f2;
            f15 *= f2;
            f16 *= f3;
            f17 *= f3;
            f18 *= f3;
            f19 *= f4;
            f20 *= f4;
            f21 *= f4;
        }
        float f22 = uu2.d(this.c, n2, n3, n4);
        if (this.f || uu2.b(this.c, n2, n3 - 1, n4, 0)) {
            f5 = uu2.d(this.c, n2, n3 - 1, n4);
            nw2.a(f13 * f5, f16 * f5, f19 * f5);
            this.a(uu2, (double)n2, (double)n3, (double)n4, uu2.a(this.c, n2, n3, n4, 0));
            bl2 = true;
        }
        if (this.f || uu2.b(this.c, n2, n3 + 1, n4, 1)) {
            f5 = uu2.d(this.c, n2, n3 + 1, n4);
            if (uu2.bw != 1.0 && !uu2.bA.d()) {
                f5 = f22;
            }
            nw2.a(f10 * f5, f11 * f5, f12 * f5);
            this.b(uu2, (double)n2, (double)n3, (double)n4, uu2.a(this.c, n2, n3, n4, 1));
            bl2 = true;
        }
        if (this.f || uu2.b(this.c, n2, n3, n4 - 1, 2)) {
            f5 = uu2.d(this.c, n2, n3, n4 - 1);
            if (uu2.bu > 0.0) {
                f5 = f22;
            }
            nw2.a(f14 * f5, f17 * f5, f20 * f5);
            n5 = uu2.a(this.c, n2, n3, n4, 2);
            this.c(uu2, (double)n2, (double)n3, (double)n4, n5);
            if (a && n5 == 3 && this.d < 0) {
                nw2.a(f14 * f5 * f2, f17 * f5 * f3, f20 * f5 * f4);
                this.c(uu2, (double)n2, (double)n3, (double)n4, 38);
            }
            bl2 = true;
        }
        if (this.f || uu2.b(this.c, n2, n3, n4 + 1, 3)) {
            f5 = uu2.d(this.c, n2, n3, n4 + 1);
            if (uu2.bx < 1.0) {
                f5 = f22;
            }
            nw2.a(f14 * f5, f17 * f5, f20 * f5);
            n5 = uu2.a(this.c, n2, n3, n4, 3);
            this.d(uu2, n2, n3, n4, n5);
            if (a && n5 == 3 && this.d < 0) {
                nw2.a(f14 * f5 * f2, f17 * f5 * f3, f20 * f5 * f4);
                this.d(uu2, n2, n3, n4, 38);
            }
            bl2 = true;
        }
        if (this.f || uu2.b(this.c, n2 - 1, n3, n4, 4)) {
            f5 = uu2.d(this.c, n2 - 1, n3, n4);
            if (uu2.bs > 0.0) {
                f5 = f22;
            }
            nw2.a(f15 * f5, f18 * f5, f21 * f5);
            n5 = uu2.a(this.c, n2, n3, n4, 4);
            this.e(uu2, n2, n3, n4, n5);
            if (a && n5 == 3 && this.d < 0) {
                nw2.a(f15 * f5 * f2, f18 * f5 * f3, f21 * f5 * f4);
                this.e(uu2, n2, n3, n4, 38);
            }
            bl2 = true;
        }
        if (this.f || uu2.b(this.c, n2 + 1, n3, n4, 5)) {
            f5 = uu2.d(this.c, n2 + 1, n3, n4);
            if (uu2.bv < 1.0) {
                f5 = f22;
            }
            nw2.a(f15 * f5, f18 * f5, f21 * f5);
            n5 = uu2.a(this.c, n2, n3, n4, 5);
            this.f(uu2, n2, n3, n4, n5);
            if (a && n5 == 3 && this.d < 0) {
                nw2.a(f15 * f5 * f2, f18 * f5 * f3, f21 * f5 * f4);
                this.f(uu2, n2, n3, n4, 38);
            }
            bl2 = true;
        }
        return bl2;
    }

    public boolean m(uu uu2, int n2, int n3, int n4) {
        int n5 = uu2.b(this.c, n2, n3, n4);
        float f2 = (float)(n5 >> 16 & 0xFF) / 255.0f;
        float f3 = (float)(n5 >> 8 & 0xFF) / 255.0f;
        float f4 = (float)(n5 & 0xFF) / 255.0f;
        if (px.a) {
            float f5 = (f2 * 30.0f + f3 * 59.0f + f4 * 11.0f) / 100.0f;
            float f6 = (f2 * 30.0f + f3 * 70.0f) / 100.0f;
            float f7 = (f2 * 30.0f + f4 * 70.0f) / 100.0f;
            f2 = f5;
            f3 = f6;
            f4 = f7;
        }
        return this.c(uu2, n2, n3, n4, f2, f3, f4);
    }

    public boolean c(uu uu2, int n2, int n3, int n4, float f2, float f3, float f4) {
        float f5;
        nw nw2 = nw.a;
        boolean bl2 = false;
        float f6 = 0.5f;
        float f7 = 1.0f;
        float f8 = 0.8f;
        float f9 = 0.6f;
        float f10 = f6 * f2;
        float f11 = f7 * f2;
        float f12 = f8 * f2;
        float f13 = f9 * f2;
        float f14 = f6 * f3;
        float f15 = f7 * f3;
        float f16 = f8 * f3;
        float f17 = f9 * f3;
        float f18 = f6 * f4;
        float f19 = f7 * f4;
        float f20 = f8 * f4;
        float f21 = f9 * f4;
        float f22 = 0.0625f;
        float f23 = uu2.d(this.c, n2, n3, n4);
        if (this.f || uu2.b(this.c, n2, n3 - 1, n4, 0)) {
            f5 = uu2.d(this.c, n2, n3 - 1, n4);
            nw2.a(f10 * f5, f14 * f5, f18 * f5);
            this.a(uu2, (double)n2, (double)n3, (double)n4, uu2.a(this.c, n2, n3, n4, 0));
            bl2 = true;
        }
        if (this.f || uu2.b(this.c, n2, n3 + 1, n4, 1)) {
            f5 = uu2.d(this.c, n2, n3 + 1, n4);
            if (uu2.bw != 1.0 && !uu2.bA.d()) {
                f5 = f23;
            }
            nw2.a(f11 * f5, f15 * f5, f19 * f5);
            this.b(uu2, (double)n2, (double)n3, (double)n4, uu2.a(this.c, n2, n3, n4, 1));
            bl2 = true;
        }
        if (this.f || uu2.b(this.c, n2, n3, n4 - 1, 2)) {
            f5 = uu2.d(this.c, n2, n3, n4 - 1);
            if (uu2.bu > 0.0) {
                f5 = f23;
            }
            nw2.a(f12 * f5, f16 * f5, f20 * f5);
            nw2.c(0.0f, 0.0f, f22);
            this.c(uu2, (double)n2, (double)n3, (double)n4, uu2.a(this.c, n2, n3, n4, 2));
            nw2.c(0.0f, 0.0f, -f22);
            bl2 = true;
        }
        if (this.f || uu2.b(this.c, n2, n3, n4 + 1, 3)) {
            f5 = uu2.d(this.c, n2, n3, n4 + 1);
            if (uu2.bx < 1.0) {
                f5 = f23;
            }
            nw2.a(f12 * f5, f16 * f5, f20 * f5);
            nw2.c(0.0f, 0.0f, -f22);
            this.d(uu2, n2, n3, n4, uu2.a(this.c, n2, n3, n4, 3));
            nw2.c(0.0f, 0.0f, f22);
            bl2 = true;
        }
        if (this.f || uu2.b(this.c, n2 - 1, n3, n4, 4)) {
            f5 = uu2.d(this.c, n2 - 1, n3, n4);
            if (uu2.bs > 0.0) {
                f5 = f23;
            }
            nw2.a(f13 * f5, f17 * f5, f21 * f5);
            nw2.c(f22, 0.0f, 0.0f);
            this.e(uu2, n2, n3, n4, uu2.a(this.c, n2, n3, n4, 4));
            nw2.c(-f22, 0.0f, 0.0f);
            bl2 = true;
        }
        if (this.f || uu2.b(this.c, n2 + 1, n3, n4, 5)) {
            f5 = uu2.d(this.c, n2 + 1, n3, n4);
            if (uu2.bv < 1.0) {
                f5 = f23;
            }
            nw2.a(f13 * f5, f17 * f5, f21 * f5);
            nw2.c(-f22, 0.0f, 0.0f);
            this.f(uu2, n2, n3, n4, uu2.a(this.c, n2, n3, n4, 5));
            nw2.c(f22, 0.0f, 0.0f);
            bl2 = true;
        }
        return bl2;
    }

    public boolean n(uu uu2, int n2, int n3, int n4) {
        float f2;
        boolean bl2;
        boolean bl3 = false;
        float f3 = 0.375f;
        float f4 = 0.625f;
        uu2.a(f3, 0.0f, f3, f4, 1.0f, f4);
        this.l(uu2, n2, n3, n4);
        bl3 = true;
        boolean bl4 = false;
        boolean bl5 = false;
        if (this.c.a(n2 - 1, n3, n4) == uu2.bn || this.c.a(n2 + 1, n3, n4) == uu2.bn) {
            bl4 = true;
        }
        if (this.c.a(n2, n3, n4 - 1) == uu2.bn || this.c.a(n2, n3, n4 + 1) == uu2.bn) {
            bl5 = true;
        }
        boolean bl6 = this.c.a(n2 - 1, n3, n4) == uu2.bn;
        boolean bl7 = this.c.a(n2 + 1, n3, n4) == uu2.bn;
        boolean bl8 = this.c.a(n2, n3, n4 - 1) == uu2.bn;
        boolean bl9 = bl2 = this.c.a(n2, n3, n4 + 1) == uu2.bn;
        if (!bl4 && !bl5) {
            bl4 = true;
        }
        f3 = 0.4375f;
        f4 = 0.5625f;
        float f5 = 0.75f;
        float f6 = 0.9375f;
        float f7 = bl6 ? 0.0f : f3;
        float f8 = bl7 ? 1.0f : f4;
        float f9 = bl8 ? 0.0f : f3;
        float f10 = f2 = bl2 ? 1.0f : f4;
        if (bl4) {
            uu2.a(f7, f5, f3, f8, f6, f4);
            this.l(uu2, n2, n3, n4);
            bl3 = true;
        }
        if (bl5) {
            uu2.a(f3, f5, f9, f4, f6, f2);
            this.l(uu2, n2, n3, n4);
            bl3 = true;
        }
        f5 = 0.375f;
        f6 = 0.5625f;
        if (bl4) {
            uu2.a(f7, f5, f3, f8, f6, f4);
            this.l(uu2, n2, n3, n4);
            bl3 = true;
        }
        if (bl5) {
            uu2.a(f3, f5, f9, f4, f6, f2);
            this.l(uu2, n2, n3, n4);
            bl3 = true;
        }
        uu2.a(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
        return bl3;
    }

    public boolean o(uu uu2, int n2, int n3, int n4) {
        boolean bl2 = false;
        int n5 = this.c.e(n2, n3, n4);
        if (n5 == 0) {
            uu2.a(0.0f, 0.0f, 0.0f, 0.5f, 0.5f, 1.0f);
            this.l(uu2, n2, n3, n4);
            uu2.a(0.5f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
            this.l(uu2, n2, n3, n4);
            bl2 = true;
        } else if (n5 == 1) {
            uu2.a(0.0f, 0.0f, 0.0f, 0.5f, 1.0f, 1.0f);
            this.l(uu2, n2, n3, n4);
            uu2.a(0.5f, 0.0f, 0.0f, 1.0f, 0.5f, 1.0f);
            this.l(uu2, n2, n3, n4);
            bl2 = true;
        } else if (n5 == 2) {
            uu2.a(0.0f, 0.0f, 0.0f, 1.0f, 0.5f, 0.5f);
            this.l(uu2, n2, n3, n4);
            uu2.a(0.0f, 0.0f, 0.5f, 1.0f, 1.0f, 1.0f);
            this.l(uu2, n2, n3, n4);
            bl2 = true;
        } else if (n5 == 3) {
            uu2.a(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.5f);
            this.l(uu2, n2, n3, n4);
            uu2.a(0.0f, 0.0f, 0.5f, 1.0f, 0.5f, 1.0f);
            this.l(uu2, n2, n3, n4);
            bl2 = true;
        }
        uu2.a(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
        return bl2;
    }

    public boolean p(uu uu2, int n2, int n3, int n4) {
        nw nw2 = nw.a;
        le le2 = (le)uu2;
        boolean bl2 = false;
        float f2 = 0.5f;
        float f3 = 1.0f;
        float f4 = 0.8f;
        float f5 = 0.6f;
        float f6 = uu2.d(this.c, n2, n3, n4);
        float f7 = uu2.d(this.c, n2, n3 - 1, n4);
        if (le2.bt > 0.0) {
            f7 = f6;
        }
        if (uu.s[uu2.bn] > 0) {
            f7 = 1.0f;
        }
        nw2.a(f2 * f7, f2 * f7, f2 * f7);
        this.a(uu2, (double)n2, (double)n3, (double)n4, uu2.a(this.c, n2, n3, n4, 0));
        bl2 = true;
        f7 = uu2.d(this.c, n2, n3 + 1, n4);
        if (le2.bw < 1.0) {
            f7 = f6;
        }
        if (uu.s[uu2.bn] > 0) {
            f7 = 1.0f;
        }
        nw2.a(f3 * f7, f3 * f7, f3 * f7);
        this.b(uu2, (double)n2, (double)n3, (double)n4, uu2.a(this.c, n2, n3, n4, 1));
        bl2 = true;
        f7 = uu2.d(this.c, n2, n3, n4 - 1);
        if (le2.bu > 0.0) {
            f7 = f6;
        }
        if (uu.s[uu2.bn] > 0) {
            f7 = 1.0f;
        }
        nw2.a(f4 * f7, f4 * f7, f4 * f7);
        int n5 = uu2.a(this.c, n2, n3, n4, 2);
        if (n5 < 0) {
            this.e = true;
            n5 = -n5;
        }
        this.c(uu2, (double)n2, (double)n3, (double)n4, n5);
        bl2 = true;
        this.e = false;
        f7 = uu2.d(this.c, n2, n3, n4 + 1);
        if (le2.bx < 1.0) {
            f7 = f6;
        }
        if (uu.s[uu2.bn] > 0) {
            f7 = 1.0f;
        }
        nw2.a(f4 * f7, f4 * f7, f4 * f7);
        n5 = uu2.a(this.c, n2, n3, n4, 3);
        if (n5 < 0) {
            this.e = true;
            n5 = -n5;
        }
        this.d(uu2, n2, n3, n4, n5);
        bl2 = true;
        this.e = false;
        f7 = uu2.d(this.c, n2 - 1, n3, n4);
        if (le2.bs > 0.0) {
            f7 = f6;
        }
        if (uu.s[uu2.bn] > 0) {
            f7 = 1.0f;
        }
        nw2.a(f5 * f7, f5 * f7, f5 * f7);
        n5 = uu2.a(this.c, n2, n3, n4, 4);
        if (n5 < 0) {
            this.e = true;
            n5 = -n5;
        }
        this.e(uu2, n2, n3, n4, n5);
        bl2 = true;
        this.e = false;
        f7 = uu2.d(this.c, n2 + 1, n3, n4);
        if (le2.bv < 1.0) {
            f7 = f6;
        }
        if (uu.s[uu2.bn] > 0) {
            f7 = 1.0f;
        }
        nw2.a(f5 * f7, f5 * f7, f5 * f7);
        n5 = uu2.a(this.c, n2, n3, n4, 5);
        if (n5 < 0) {
            this.e = true;
            n5 = -n5;
        }
        this.f(uu2, n2, n3, n4, n5);
        bl2 = true;
        this.e = false;
        return bl2;
    }

    public void a(uu uu2, double d2, double d3, double d4, int n2) {
        nw nw2 = nw.a;
        if (this.d >= 0) {
            n2 = this.d;
        }
        int n3 = (n2 & 0xF) << 4;
        int n4 = n2 & 0xF0;
        double d5 = ((double)n3 + uu2.bs * 16.0) / 256.0;
        double d6 = ((double)n3 + uu2.bv * 16.0 - 0.01) / 256.0;
        double d7 = ((double)n4 + uu2.bu * 16.0) / 256.0;
        double d8 = ((double)n4 + uu2.bx * 16.0 - 0.01) / 256.0;
        if (uu2.bs < 0.0 || uu2.bv > 1.0) {
            d5 = ((float)n3 + 0.0f) / 256.0f;
            d6 = ((float)n3 + 15.99f) / 256.0f;
        }
        if (uu2.bu < 0.0 || uu2.bx > 1.0) {
            d7 = ((float)n4 + 0.0f) / 256.0f;
            d8 = ((float)n4 + 15.99f) / 256.0f;
        }
        double d9 = d6;
        double d10 = d5;
        double d11 = d7;
        double d12 = d8;
        if (this.l == 2) {
            d5 = ((double)n3 + uu2.bu * 16.0) / 256.0;
            d7 = ((double)(n4 + 16) - uu2.bv * 16.0) / 256.0;
            d6 = ((double)n3 + uu2.bx * 16.0) / 256.0;
            d8 = ((double)(n4 + 16) - uu2.bs * 16.0) / 256.0;
            d9 = d6;
            d10 = d5;
            d11 = d7;
            d12 = d8;
            d9 = d5;
            d10 = d6;
            d7 = d8;
            d8 = d11;
        } else if (this.l == 1) {
            d5 = ((double)(n3 + 16) - uu2.bx * 16.0) / 256.0;
            d7 = ((double)n4 + uu2.bs * 16.0) / 256.0;
            d6 = ((double)(n3 + 16) - uu2.bu * 16.0) / 256.0;
            d8 = ((double)n4 + uu2.bv * 16.0) / 256.0;
            d9 = d6;
            d10 = d5;
            d11 = d7;
            d12 = d8;
            d5 = d9;
            d6 = d10;
            d11 = d8;
            d12 = d7;
        } else if (this.l == 3) {
            d5 = ((double)(n3 + 16) - uu2.bs * 16.0) / 256.0;
            d6 = ((double)(n3 + 16) - uu2.bv * 16.0 - 0.01) / 256.0;
            d7 = ((double)(n4 + 16) - uu2.bu * 16.0) / 256.0;
            d8 = ((double)(n4 + 16) - uu2.bx * 16.0 - 0.01) / 256.0;
            d9 = d6;
            d10 = d5;
            d11 = d7;
            d12 = d8;
        }
        double d13 = d2 + uu2.bs;
        double d14 = d2 + uu2.bv;
        double d15 = d3 + uu2.bt;
        double d16 = d4 + uu2.bu;
        double d17 = d4 + uu2.bx;
        if (this.m) {
            nw2.a(this.P, this.T, this.X);
            nw2.a(d13, d15, d17, d10, d12);
            nw2.a(this.Q, this.U, this.Y);
            nw2.a(d13, d15, d16, d5, d7);
            nw2.a(this.R, this.V, this.Z);
            nw2.a(d14, d15, d16, d9, d11);
            nw2.a(this.S, this.W, this.aa);
            nw2.a(d14, d15, d17, d6, d8);
        } else {
            nw2.a(d13, d15, d17, d10, d12);
            nw2.a(d13, d15, d16, d5, d7);
            nw2.a(d14, d15, d16, d9, d11);
            nw2.a(d14, d15, d17, d6, d8);
        }
    }

    public void b(uu uu2, double d2, double d3, double d4, int n2) {
        nw nw2 = nw.a;
        if (this.d >= 0) {
            n2 = this.d;
        }
        int n3 = (n2 & 0xF) << 4;
        int n4 = n2 & 0xF0;
        double d5 = ((double)n3 + uu2.bs * 16.0) / 256.0;
        double d6 = ((double)n3 + uu2.bv * 16.0 - 0.01) / 256.0;
        double d7 = ((double)n4 + uu2.bu * 16.0) / 256.0;
        double d8 = ((double)n4 + uu2.bx * 16.0 - 0.01) / 256.0;
        if (uu2.bs < 0.0 || uu2.bv > 1.0) {
            d5 = ((float)n3 + 0.0f) / 256.0f;
            d6 = ((float)n3 + 15.99f) / 256.0f;
        }
        if (uu2.bu < 0.0 || uu2.bx > 1.0) {
            d7 = ((float)n4 + 0.0f) / 256.0f;
            d8 = ((float)n4 + 15.99f) / 256.0f;
        }
        double d9 = d6;
        double d10 = d5;
        double d11 = d7;
        double d12 = d8;
        if (this.k == 1) {
            d5 = ((double)n3 + uu2.bu * 16.0) / 256.0;
            d7 = ((double)(n4 + 16) - uu2.bv * 16.0) / 256.0;
            d6 = ((double)n3 + uu2.bx * 16.0) / 256.0;
            d8 = ((double)(n4 + 16) - uu2.bs * 16.0) / 256.0;
            d9 = d6;
            d10 = d5;
            d11 = d7;
            d12 = d8;
            d9 = d5;
            d10 = d6;
            d7 = d8;
            d8 = d11;
        } else if (this.k == 2) {
            d5 = ((double)(n3 + 16) - uu2.bx * 16.0) / 256.0;
            d7 = ((double)n4 + uu2.bs * 16.0) / 256.0;
            d6 = ((double)(n3 + 16) - uu2.bu * 16.0) / 256.0;
            d8 = ((double)n4 + uu2.bv * 16.0) / 256.0;
            d9 = d6;
            d10 = d5;
            d11 = d7;
            d12 = d8;
            d5 = d9;
            d6 = d10;
            d11 = d8;
            d12 = d7;
        } else if (this.k == 3) {
            d5 = ((double)(n3 + 16) - uu2.bs * 16.0) / 256.0;
            d6 = ((double)(n3 + 16) - uu2.bv * 16.0 - 0.01) / 256.0;
            d7 = ((double)(n4 + 16) - uu2.bu * 16.0) / 256.0;
            d8 = ((double)(n4 + 16) - uu2.bx * 16.0 - 0.01) / 256.0;
            d9 = d6;
            d10 = d5;
            d11 = d7;
            d12 = d8;
        }
        double d13 = d2 + uu2.bs;
        double d14 = d2 + uu2.bv;
        double d15 = d3 + uu2.bw;
        double d16 = d4 + uu2.bu;
        double d17 = d4 + uu2.bx;
        if (this.m) {
            nw2.a(this.P, this.T, this.X);
            nw2.a(d14, d15, d17, d6, d8);
            nw2.a(this.Q, this.U, this.Y);
            nw2.a(d14, d15, d16, d9, d11);
            nw2.a(this.R, this.V, this.Z);
            nw2.a(d13, d15, d16, d5, d7);
            nw2.a(this.S, this.W, this.aa);
            nw2.a(d13, d15, d17, d10, d12);
        } else {
            nw2.a(d14, d15, d17, d6, d8);
            nw2.a(d14, d15, d16, d9, d11);
            nw2.a(d13, d15, d16, d5, d7);
            nw2.a(d13, d15, d17, d10, d12);
        }
    }

    public void c(uu uu2, double d2, double d3, double d4, int n2) {
        double d5;
        nw nw2 = nw.a;
        if (this.d >= 0) {
            n2 = this.d;
        }
        int n3 = (n2 & 0xF) << 4;
        int n4 = n2 & 0xF0;
        double d6 = ((double)n3 + uu2.bs * 16.0) / 256.0;
        double d7 = ((double)n3 + uu2.bv * 16.0 - 0.01) / 256.0;
        double d8 = ((double)(n4 + 16) - uu2.bw * 16.0) / 256.0;
        double d9 = ((double)(n4 + 16) - uu2.bt * 16.0 - 0.01) / 256.0;
        if (this.e) {
            d5 = d6;
            d6 = d7;
            d7 = d5;
        }
        if (uu2.bs < 0.0 || uu2.bv > 1.0) {
            d6 = ((float)n3 + 0.0f) / 256.0f;
            d7 = ((float)n3 + 15.99f) / 256.0f;
        }
        if (uu2.bt < 0.0 || uu2.bw > 1.0) {
            d8 = ((float)n4 + 0.0f) / 256.0f;
            d9 = ((float)n4 + 15.99f) / 256.0f;
        }
        d5 = d7;
        double d10 = d6;
        double d11 = d8;
        double d12 = d9;
        if (this.g == 2) {
            d6 = ((double)n3 + uu2.bt * 16.0) / 256.0;
            d8 = ((double)(n4 + 16) - uu2.bs * 16.0) / 256.0;
            d7 = ((double)n3 + uu2.bw * 16.0) / 256.0;
            d9 = ((double)(n4 + 16) - uu2.bv * 16.0) / 256.0;
            d5 = d7;
            d10 = d6;
            d11 = d8;
            d12 = d9;
            d5 = d6;
            d10 = d7;
            d8 = d9;
            d9 = d11;
        } else if (this.g == 1) {
            d6 = ((double)(n3 + 16) - uu2.bw * 16.0) / 256.0;
            d8 = ((double)n4 + uu2.bv * 16.0) / 256.0;
            d7 = ((double)(n3 + 16) - uu2.bt * 16.0) / 256.0;
            d9 = ((double)n4 + uu2.bs * 16.0) / 256.0;
            d5 = d7;
            d10 = d6;
            d11 = d8;
            d12 = d9;
            d6 = d5;
            d7 = d10;
            d11 = d9;
            d12 = d8;
        } else if (this.g == 3) {
            d6 = ((double)(n3 + 16) - uu2.bs * 16.0) / 256.0;
            d7 = ((double)(n3 + 16) - uu2.bv * 16.0 - 0.01) / 256.0;
            d8 = ((double)n4 + uu2.bw * 16.0) / 256.0;
            d9 = ((double)n4 + uu2.bt * 16.0 - 0.01) / 256.0;
            d5 = d7;
            d10 = d6;
            d11 = d8;
            d12 = d9;
        }
        double d13 = d2 + uu2.bs;
        double d14 = d2 + uu2.bv;
        double d15 = d3 + uu2.bt;
        double d16 = d3 + uu2.bw;
        double d17 = d4 + uu2.bu;
        if (this.m) {
            nw2.a(this.P, this.T, this.X);
            nw2.a(d13, d16, d17, d5, d11);
            nw2.a(this.Q, this.U, this.Y);
            nw2.a(d14, d16, d17, d6, d8);
            nw2.a(this.R, this.V, this.Z);
            nw2.a(d14, d15, d17, d10, d12);
            nw2.a(this.S, this.W, this.aa);
            nw2.a(d13, d15, d17, d7, d9);
        } else {
            nw2.a(d13, d16, d17, d5, d11);
            nw2.a(d14, d16, d17, d6, d8);
            nw2.a(d14, d15, d17, d10, d12);
            nw2.a(d13, d15, d17, d7, d9);
        }
    }

    public void d(uu uu2, double d2, double d3, double d4, int n2) {
        double d5;
        nw nw2 = nw.a;
        if (this.d >= 0) {
            n2 = this.d;
        }
        int n3 = (n2 & 0xF) << 4;
        int n4 = n2 & 0xF0;
        double d6 = ((double)n3 + uu2.bs * 16.0) / 256.0;
        double d7 = ((double)n3 + uu2.bv * 16.0 - 0.01) / 256.0;
        double d8 = ((double)(n4 + 16) - uu2.bw * 16.0) / 256.0;
        double d9 = ((double)(n4 + 16) - uu2.bt * 16.0 - 0.01) / 256.0;
        if (this.e) {
            d5 = d6;
            d6 = d7;
            d7 = d5;
        }
        if (uu2.bs < 0.0 || uu2.bv > 1.0) {
            d6 = ((float)n3 + 0.0f) / 256.0f;
            d7 = ((float)n3 + 15.99f) / 256.0f;
        }
        if (uu2.bt < 0.0 || uu2.bw > 1.0) {
            d8 = ((float)n4 + 0.0f) / 256.0f;
            d9 = ((float)n4 + 15.99f) / 256.0f;
        }
        d5 = d7;
        double d10 = d6;
        double d11 = d8;
        double d12 = d9;
        if (this.h == 1) {
            d6 = ((double)n3 + uu2.bt * 16.0) / 256.0;
            d9 = ((double)(n4 + 16) - uu2.bs * 16.0) / 256.0;
            d7 = ((double)n3 + uu2.bw * 16.0) / 256.0;
            d8 = ((double)(n4 + 16) - uu2.bv * 16.0) / 256.0;
            d5 = d7;
            d10 = d6;
            d11 = d8;
            d12 = d9;
            d5 = d6;
            d10 = d7;
            d8 = d9;
            d9 = d11;
        } else if (this.h == 2) {
            d6 = ((double)(n3 + 16) - uu2.bw * 16.0) / 256.0;
            d8 = ((double)n4 + uu2.bs * 16.0) / 256.0;
            d7 = ((double)(n3 + 16) - uu2.bt * 16.0) / 256.0;
            d9 = ((double)n4 + uu2.bv * 16.0) / 256.0;
            d5 = d7;
            d10 = d6;
            d11 = d8;
            d12 = d9;
            d6 = d5;
            d7 = d10;
            d11 = d9;
            d12 = d8;
        } else if (this.h == 3) {
            d6 = ((double)(n3 + 16) - uu2.bs * 16.0) / 256.0;
            d7 = ((double)(n3 + 16) - uu2.bv * 16.0 - 0.01) / 256.0;
            d8 = ((double)n4 + uu2.bw * 16.0) / 256.0;
            d9 = ((double)n4 + uu2.bt * 16.0 - 0.01) / 256.0;
            d5 = d7;
            d10 = d6;
            d11 = d8;
            d12 = d9;
        }
        double d13 = d2 + uu2.bs;
        double d14 = d2 + uu2.bv;
        double d15 = d3 + uu2.bt;
        double d16 = d3 + uu2.bw;
        double d17 = d4 + uu2.bx;
        if (this.m) {
            nw2.a(this.P, this.T, this.X);
            nw2.a(d13, d16, d17, d6, d8);
            nw2.a(this.Q, this.U, this.Y);
            nw2.a(d13, d15, d17, d10, d12);
            nw2.a(this.R, this.V, this.Z);
            nw2.a(d14, d15, d17, d7, d9);
            nw2.a(this.S, this.W, this.aa);
            nw2.a(d14, d16, d17, d5, d11);
        } else {
            nw2.a(d13, d16, d17, d6, d8);
            nw2.a(d13, d15, d17, d10, d12);
            nw2.a(d14, d15, d17, d7, d9);
            nw2.a(d14, d16, d17, d5, d11);
        }
    }

    public void e(uu uu2, double d2, double d3, double d4, int n2) {
        double d5;
        nw nw2 = nw.a;
        if (this.d >= 0) {
            n2 = this.d;
        }
        int n3 = (n2 & 0xF) << 4;
        int n4 = n2 & 0xF0;
        double d6 = ((double)n3 + uu2.bu * 16.0) / 256.0;
        double d7 = ((double)n3 + uu2.bx * 16.0 - 0.01) / 256.0;
        double d8 = ((double)(n4 + 16) - uu2.bw * 16.0) / 256.0;
        double d9 = ((double)(n4 + 16) - uu2.bt * 16.0 - 0.01) / 256.0;
        if (this.e) {
            d5 = d6;
            d6 = d7;
            d7 = d5;
        }
        if (uu2.bu < 0.0 || uu2.bx > 1.0) {
            d6 = ((float)n3 + 0.0f) / 256.0f;
            d7 = ((float)n3 + 15.99f) / 256.0f;
        }
        if (uu2.bt < 0.0 || uu2.bw > 1.0) {
            d8 = ((float)n4 + 0.0f) / 256.0f;
            d9 = ((float)n4 + 15.99f) / 256.0f;
        }
        d5 = d7;
        double d10 = d6;
        double d11 = d8;
        double d12 = d9;
        if (this.j == 1) {
            d6 = ((double)n3 + uu2.bt * 16.0) / 256.0;
            d8 = ((double)(n4 + 16) - uu2.bx * 16.0) / 256.0;
            d7 = ((double)n3 + uu2.bw * 16.0) / 256.0;
            d9 = ((double)(n4 + 16) - uu2.bu * 16.0) / 256.0;
            d5 = d7;
            d10 = d6;
            d11 = d8;
            d12 = d9;
            d5 = d6;
            d10 = d7;
            d8 = d9;
            d9 = d11;
        } else if (this.j == 2) {
            d6 = ((double)(n3 + 16) - uu2.bw * 16.0) / 256.0;
            d8 = ((double)n4 + uu2.bu * 16.0) / 256.0;
            d7 = ((double)(n3 + 16) - uu2.bt * 16.0) / 256.0;
            d9 = ((double)n4 + uu2.bx * 16.0) / 256.0;
            d5 = d7;
            d10 = d6;
            d11 = d8;
            d12 = d9;
            d6 = d5;
            d7 = d10;
            d11 = d9;
            d12 = d8;
        } else if (this.j == 3) {
            d6 = ((double)(n3 + 16) - uu2.bu * 16.0) / 256.0;
            d7 = ((double)(n3 + 16) - uu2.bx * 16.0 - 0.01) / 256.0;
            d8 = ((double)n4 + uu2.bw * 16.0) / 256.0;
            d9 = ((double)n4 + uu2.bt * 16.0 - 0.01) / 256.0;
            d5 = d7;
            d10 = d6;
            d11 = d8;
            d12 = d9;
        }
        double d13 = d2 + uu2.bs;
        double d14 = d3 + uu2.bt;
        double d15 = d3 + uu2.bw;
        double d16 = d4 + uu2.bu;
        double d17 = d4 + uu2.bx;
        if (this.m) {
            nw2.a(this.P, this.T, this.X);
            nw2.a(d13, d15, d17, d5, d11);
            nw2.a(this.Q, this.U, this.Y);
            nw2.a(d13, d15, d16, d6, d8);
            nw2.a(this.R, this.V, this.Z);
            nw2.a(d13, d14, d16, d10, d12);
            nw2.a(this.S, this.W, this.aa);
            nw2.a(d13, d14, d17, d7, d9);
        } else {
            nw2.a(d13, d15, d17, d5, d11);
            nw2.a(d13, d15, d16, d6, d8);
            nw2.a(d13, d14, d16, d10, d12);
            nw2.a(d13, d14, d17, d7, d9);
        }
    }

    public void f(uu uu2, double d2, double d3, double d4, int n2) {
        double d5;
        nw nw2 = nw.a;
        if (this.d >= 0) {
            n2 = this.d;
        }
        int n3 = (n2 & 0xF) << 4;
        int n4 = n2 & 0xF0;
        double d6 = ((double)n3 + uu2.bu * 16.0) / 256.0;
        double d7 = ((double)n3 + uu2.bx * 16.0 - 0.01) / 256.0;
        double d8 = ((double)(n4 + 16) - uu2.bw * 16.0) / 256.0;
        double d9 = ((double)(n4 + 16) - uu2.bt * 16.0 - 0.01) / 256.0;
        if (this.e) {
            d5 = d6;
            d6 = d7;
            d7 = d5;
        }
        if (uu2.bu < 0.0 || uu2.bx > 1.0) {
            d6 = ((float)n3 + 0.0f) / 256.0f;
            d7 = ((float)n3 + 15.99f) / 256.0f;
        }
        if (uu2.bt < 0.0 || uu2.bw > 1.0) {
            d8 = ((float)n4 + 0.0f) / 256.0f;
            d9 = ((float)n4 + 15.99f) / 256.0f;
        }
        d5 = d7;
        double d10 = d6;
        double d11 = d8;
        double d12 = d9;
        if (this.i == 2) {
            d6 = ((double)n3 + uu2.bt * 16.0) / 256.0;
            d8 = ((double)(n4 + 16) - uu2.bu * 16.0) / 256.0;
            d7 = ((double)n3 + uu2.bw * 16.0) / 256.0;
            d9 = ((double)(n4 + 16) - uu2.bx * 16.0) / 256.0;
            d5 = d7;
            d10 = d6;
            d11 = d8;
            d12 = d9;
            d5 = d6;
            d10 = d7;
            d8 = d9;
            d9 = d11;
        } else if (this.i == 1) {
            d6 = ((double)(n3 + 16) - uu2.bw * 16.0) / 256.0;
            d8 = ((double)n4 + uu2.bx * 16.0) / 256.0;
            d7 = ((double)(n3 + 16) - uu2.bt * 16.0) / 256.0;
            d9 = ((double)n4 + uu2.bu * 16.0) / 256.0;
            d5 = d7;
            d10 = d6;
            d11 = d8;
            d12 = d9;
            d6 = d5;
            d7 = d10;
            d11 = d9;
            d12 = d8;
        } else if (this.i == 3) {
            d6 = ((double)(n3 + 16) - uu2.bu * 16.0) / 256.0;
            d7 = ((double)(n3 + 16) - uu2.bx * 16.0 - 0.01) / 256.0;
            d8 = ((double)n4 + uu2.bw * 16.0) / 256.0;
            d9 = ((double)n4 + uu2.bt * 16.0 - 0.01) / 256.0;
            d5 = d7;
            d10 = d6;
            d11 = d8;
            d12 = d9;
        }
        double d13 = d2 + uu2.bv;
        double d14 = d3 + uu2.bt;
        double d15 = d3 + uu2.bw;
        double d16 = d4 + uu2.bu;
        double d17 = d4 + uu2.bx;
        if (this.m) {
            nw2.a(this.P, this.T, this.X);
            nw2.a(d13, d14, d17, d10, d12);
            nw2.a(this.Q, this.U, this.Y);
            nw2.a(d13, d14, d16, d7, d9);
            nw2.a(this.R, this.V, this.Z);
            nw2.a(d13, d15, d16, d5, d11);
            nw2.a(this.S, this.W, this.aa);
            nw2.a(d13, d15, d17, d6, d8);
        } else {
            nw2.a(d13, d14, d17, d10, d12);
            nw2.a(d13, d14, d16, d7, d9);
            nw2.a(d13, d15, d16, d5, d11);
            nw2.a(d13, d15, d17, d6, d8);
        }
    }

    public void a(uu uu2, int n2, float f2) {
        float f3;
        float f4;
        int n3;
        nw nw2 = nw.a;
        if (this.b) {
            n3 = uu2.b(n2);
            f4 = (float)(n3 >> 16 & 0xFF) / 255.0f;
            f3 = (float)(n3 >> 8 & 0xFF) / 255.0f;
            float f5 = (float)(n3 & 0xFF) / 255.0f;
            GL11.glColor4f((float)(f4 * f2), (float)(f3 * f2), (float)(f5 * f2), (float)1.0f);
        }
        if ((n3 = uu2.b()) == 0 || n3 == 16) {
            if (n3 == 16) {
                n2 = 1;
            }
            uu2.g();
            GL11.glTranslatef((float)-0.5f, (float)-0.5f, (float)-0.5f);
            nw2.b();
            nw2.b(0.0f, -1.0f, 0.0f);
            this.a(uu2, 0.0, 0.0, 0.0, uu2.a(0, n2));
            nw2.a();
            nw2.b();
            nw2.b(0.0f, 1.0f, 0.0f);
            this.b(uu2, 0.0, 0.0, 0.0, uu2.a(1, n2));
            nw2.a();
            nw2.b();
            nw2.b(0.0f, 0.0f, -1.0f);
            this.c(uu2, 0.0, 0.0, 0.0, uu2.a(2, n2));
            nw2.a();
            nw2.b();
            nw2.b(0.0f, 0.0f, 1.0f);
            this.d(uu2, 0.0, 0.0, 0.0, uu2.a(3, n2));
            nw2.a();
            nw2.b();
            nw2.b(-1.0f, 0.0f, 0.0f);
            this.e(uu2, 0.0, 0.0, 0.0, uu2.a(4, n2));
            nw2.a();
            nw2.b();
            nw2.b(1.0f, 0.0f, 0.0f);
            this.f(uu2, 0.0, 0.0, 0.0, uu2.a(5, n2));
            nw2.a();
            GL11.glTranslatef((float)0.5f, (float)0.5f, (float)0.5f);
        } else if (n3 == 1) {
            nw2.b();
            nw2.b(0.0f, -1.0f, 0.0f);
            this.a(uu2, n2, -0.5, -0.5, -0.5);
            nw2.a();
        } else if (n3 == 13) {
            uu2.g();
            GL11.glTranslatef((float)-0.5f, (float)-0.5f, (float)-0.5f);
            f4 = 0.0625f;
            nw2.b();
            nw2.b(0.0f, -1.0f, 0.0f);
            this.a(uu2, 0.0, 0.0, 0.0, uu2.a(0));
            nw2.a();
            nw2.b();
            nw2.b(0.0f, 1.0f, 0.0f);
            this.b(uu2, 0.0, 0.0, 0.0, uu2.a(1));
            nw2.a();
            nw2.b();
            nw2.b(0.0f, 0.0f, -1.0f);
            nw2.c(0.0f, 0.0f, f4);
            this.c(uu2, 0.0, 0.0, 0.0, uu2.a(2));
            nw2.c(0.0f, 0.0f, -f4);
            nw2.a();
            nw2.b();
            nw2.b(0.0f, 0.0f, 1.0f);
            nw2.c(0.0f, 0.0f, -f4);
            this.d(uu2, 0.0, 0.0, 0.0, uu2.a(3));
            nw2.c(0.0f, 0.0f, f4);
            nw2.a();
            nw2.b();
            nw2.b(-1.0f, 0.0f, 0.0f);
            nw2.c(f4, 0.0f, 0.0f);
            this.e(uu2, 0.0, 0.0, 0.0, uu2.a(4));
            nw2.c(-f4, 0.0f, 0.0f);
            nw2.a();
            nw2.b();
            nw2.b(1.0f, 0.0f, 0.0f);
            nw2.c(-f4, 0.0f, 0.0f);
            this.f(uu2, 0.0, 0.0, 0.0, uu2.a(5));
            nw2.c(f4, 0.0f, 0.0f);
            nw2.a();
            GL11.glTranslatef((float)0.5f, (float)0.5f, (float)0.5f);
        } else if (n3 == 6) {
            nw2.b();
            nw2.b(0.0f, -1.0f, 0.0f);
            this.b(uu2, n2, -0.5, -0.5, -0.5);
            nw2.a();
        } else if (n3 == 2) {
            nw2.b();
            nw2.b(0.0f, -1.0f, 0.0f);
            this.a(uu2, -0.5, -0.5, -0.5, 0.0, 0.0);
            nw2.a();
        } else if (n3 == 10) {
            for (int i2 = 0; i2 < 2; ++i2) {
                if (i2 == 0) {
                    uu2.a(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.5f);
                }
                if (i2 == 1) {
                    uu2.a(0.0f, 0.0f, 0.5f, 1.0f, 0.5f, 1.0f);
                }
                GL11.glTranslatef((float)-0.5f, (float)-0.5f, (float)-0.5f);
                nw2.b();
                nw2.b(0.0f, -1.0f, 0.0f);
                this.a(uu2, 0.0, 0.0, 0.0, uu2.a(0));
                nw2.a();
                nw2.b();
                nw2.b(0.0f, 1.0f, 0.0f);
                this.b(uu2, 0.0, 0.0, 0.0, uu2.a(1));
                nw2.a();
                nw2.b();
                nw2.b(0.0f, 0.0f, -1.0f);
                this.c(uu2, 0.0, 0.0, 0.0, uu2.a(2));
                nw2.a();
                nw2.b();
                nw2.b(0.0f, 0.0f, 1.0f);
                this.d(uu2, 0.0, 0.0, 0.0, uu2.a(3));
                nw2.a();
                nw2.b();
                nw2.b(-1.0f, 0.0f, 0.0f);
                this.e(uu2, 0.0, 0.0, 0.0, uu2.a(4));
                nw2.a();
                nw2.b();
                nw2.b(1.0f, 0.0f, 0.0f);
                this.f(uu2, 0.0, 0.0, 0.0, uu2.a(5));
                nw2.a();
                GL11.glTranslatef((float)0.5f, (float)0.5f, (float)0.5f);
            }
        } else if (n3 == 11) {
            for (int i3 = 0; i3 < 4; ++i3) {
                f3 = 0.125f;
                if (i3 == 0) {
                    uu2.a(0.5f - f3, 0.0f, 0.0f, 0.5f + f3, 1.0f, f3 * 2.0f);
                }
                if (i3 == 1) {
                    uu2.a(0.5f - f3, 0.0f, 1.0f - f3 * 2.0f, 0.5f + f3, 1.0f, 1.0f);
                }
                f3 = 0.0625f;
                if (i3 == 2) {
                    uu2.a(0.5f - f3, 1.0f - f3 * 3.0f, -f3 * 2.0f, 0.5f + f3, 1.0f - f3, 1.0f + f3 * 2.0f);
                }
                if (i3 == 3) {
                    uu2.a(0.5f - f3, 0.5f - f3 * 3.0f, -f3 * 2.0f, 0.5f + f3, 0.5f - f3, 1.0f + f3 * 2.0f);
                }
                GL11.glTranslatef((float)-0.5f, (float)-0.5f, (float)-0.5f);
                nw2.b();
                nw2.b(0.0f, -1.0f, 0.0f);
                this.a(uu2, 0.0, 0.0, 0.0, uu2.a(0));
                nw2.a();
                nw2.b();
                nw2.b(0.0f, 1.0f, 0.0f);
                this.b(uu2, 0.0, 0.0, 0.0, uu2.a(1));
                nw2.a();
                nw2.b();
                nw2.b(0.0f, 0.0f, -1.0f);
                this.c(uu2, 0.0, 0.0, 0.0, uu2.a(2));
                nw2.a();
                nw2.b();
                nw2.b(0.0f, 0.0f, 1.0f);
                this.d(uu2, 0.0, 0.0, 0.0, uu2.a(3));
                nw2.a();
                nw2.b();
                nw2.b(-1.0f, 0.0f, 0.0f);
                this.e(uu2, 0.0, 0.0, 0.0, uu2.a(4));
                nw2.a();
                nw2.b();
                nw2.b(1.0f, 0.0f, 0.0f);
                this.f(uu2, 0.0, 0.0, 0.0, uu2.a(5));
                nw2.a();
                GL11.glTranslatef((float)0.5f, (float)0.5f, (float)0.5f);
            }
            uu2.a(0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f);
        }
    }

    public static boolean a(int n2) {
        if (n2 == 0) {
            return true;
        }
        if (n2 == 13) {
            return true;
        }
        if (n2 == 10) {
            return true;
        }
        if (n2 == 11) {
            return true;
        }
        return n2 == 16;
    }
}

