/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.opengl.ARBOcclusionQuery
 *  org.lwjgl.opengl.GL11
 */
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.ARBOcclusionQuery;
import org.lwjgl.opengl.GL11;

public class n
implements pm {
    public List a = new ArrayList();
    private fd k;
    private ji l;
    private List m = new ArrayList();
    private dk[] n;
    private dk[] o;
    private int p;
    private int q;
    private int r;
    private int s;
    private Minecraft t;
    private cv u;
    private IntBuffer v;
    private boolean w = false;
    private int x = 0;
    private int y;
    private int z;
    private int A;
    private int B;
    private int C;
    private int D;
    private int E;
    private int F;
    private int G;
    private int H = -1;
    private int I = 2;
    private int J;
    private int K;
    private int L;
    int[] b = new int[50000];
    IntBuffer c = ge.d(64);
    private int M;
    private int N;
    private int O;
    private int P;
    private int Q;
    private int R;
    private List S = new ArrayList();
    private tg[] T = new tg[]{new tg(), new tg(), new tg(), new tg()};
    int d = 0;
    int e = ge.a(1);
    double f = -9999.0;
    double g = -9999.0;
    double h = -9999.0;
    public float i;
    int j = 0;

    public n(Minecraft minecraft, ji ji2) {
        int n2;
        int n3;
        this.t = minecraft;
        this.l = ji2;
        int n4 = 64;
        this.s = ge.a(n4 * n4 * n4 * 3);
        this.w = minecraft.n().a();
        if (this.w) {
            this.c.clear();
            this.v = ge.d(n4 * n4 * n4);
            this.v.clear();
            this.v.position(0);
            this.v.limit(n4 * n4 * n4);
            ARBOcclusionQuery.glGenQueriesARB((IntBuffer)this.v);
        }
        this.y = ge.a(3);
        GL11.glPushMatrix();
        GL11.glNewList((int)this.y, (int)4864);
        this.g();
        GL11.glEndList();
        GL11.glPopMatrix();
        nw nw2 = nw.a;
        this.z = this.y + 1;
        GL11.glNewList((int)this.z, (int)4864);
        int n5 = 64;
        int n6 = 256 / n5 + 2;
        float f2 = 16.0f;
        for (n3 = -n5 * n6; n3 <= n5 * n6; n3 += n5) {
            for (n2 = -n5 * n6; n2 <= n5 * n6; n2 += n5) {
                nw2.b();
                nw2.a((double)(n3 + 0), (double)f2, (double)(n2 + 0));
                nw2.a((double)(n3 + n5), (double)f2, (double)(n2 + 0));
                nw2.a((double)(n3 + n5), (double)f2, (double)(n2 + n5));
                nw2.a((double)(n3 + 0), (double)f2, (double)(n2 + n5));
                nw2.a();
            }
        }
        GL11.glEndList();
        this.A = this.y + 2;
        GL11.glNewList((int)this.A, (int)4864);
        f2 = -16.0f;
        nw2.b();
        for (n3 = -n5 * n6; n3 <= n5 * n6; n3 += n5) {
            for (n2 = -n5 * n6; n2 <= n5 * n6; n2 += n5) {
                nw2.a((double)(n3 + n5), (double)f2, (double)(n2 + 0));
                nw2.a((double)(n3 + 0), (double)f2, (double)(n2 + 0));
                nw2.a((double)(n3 + 0), (double)f2, (double)(n2 + n5));
                nw2.a((double)(n3 + n5), (double)f2, (double)(n2 + n5));
            }
        }
        nw2.a();
        GL11.glEndList();
    }

    private void g() {
        Random random = new Random(10842L);
        nw nw2 = nw.a;
        nw2.b();
        for (int i2 = 0; i2 < 1500; ++i2) {
            double d2 = random.nextFloat() * 2.0f - 1.0f;
            double d3 = random.nextFloat() * 2.0f - 1.0f;
            double d4 = random.nextFloat() * 2.0f - 1.0f;
            double d5 = 0.25f + random.nextFloat() * 0.25f;
            double d6 = d2 * d2 + d3 * d3 + d4 * d4;
            if (!(d6 < 1.0) || !(d6 > 0.01)) continue;
            d6 = 1.0 / Math.sqrt(d6);
            double d7 = (d2 *= d6) * 100.0;
            double d8 = (d3 *= d6) * 100.0;
            double d9 = (d4 *= d6) * 100.0;
            double d10 = Math.atan2(d2, d4);
            double d11 = Math.sin(d10);
            double d12 = Math.cos(d10);
            double d13 = Math.atan2(Math.sqrt(d2 * d2 + d4 * d4), d3);
            double d14 = Math.sin(d13);
            double d15 = Math.cos(d13);
            double d16 = random.nextDouble() * Math.PI * 2.0;
            double d17 = Math.sin(d16);
            double d18 = Math.cos(d16);
            for (int i3 = 0; i3 < 4; ++i3) {
                double d19;
                double d20 = 0.0;
                double d21 = (double)((i3 & 2) - 1) * d5;
                double d22 = (double)((i3 + 1 & 2) - 1) * d5;
                double d23 = d20;
                double d24 = d21 * d18 - d22 * d17;
                double d25 = d19 = d22 * d18 + d21 * d17;
                double d26 = d24 * d14 + d23 * d15;
                double d27 = d23 * d14 - d24 * d15;
                double d28 = d27 * d11 - d25 * d12;
                double d29 = d26;
                double d30 = d25 * d11 + d27 * d12;
                nw2.a(d7 + d28, d8 + d29, d9 + d30);
            }
        }
        nw2.a();
    }

    public void a(fd fd2) {
        if (this.k != null) {
            this.k.b(this);
        }
        this.f = -9999.0;
        this.g = -9999.0;
        this.h = -9999.0;
        th.a.a(fd2);
        this.k = fd2;
        this.u = new cv(fd2);
        if (fd2 != null) {
            fd2.a(this);
            this.a();
        }
    }

    public void a() {
        ls ls2;
        int n2;
        int n3;
        uu.L.a(this.t.z.j);
        this.H = this.t.z.e;
        if (this.o != null) {
            for (n3 = 0; n3 < this.o.length; ++n3) {
                this.o[n3].c();
            }
        }
        if ((n3 = 64 << 3 - this.H) > 400) {
            n3 = 400;
        }
        this.p = n3 / 16 + 1;
        this.q = 8;
        this.r = n3 / 16 + 1;
        this.o = new dk[this.p * this.q * this.r];
        this.n = new dk[this.p * this.q * this.r];
        int n4 = 0;
        int n5 = 0;
        this.B = 0;
        this.C = 0;
        this.D = 0;
        this.E = this.p;
        this.F = this.q;
        this.G = this.r;
        for (n2 = 0; n2 < this.m.size(); ++n2) {
            ((dk)this.m.get((int)n2)).u = false;
        }
        this.m.clear();
        this.a.clear();
        for (n2 = 0; n2 < this.p; ++n2) {
            for (int i2 = 0; i2 < this.q; ++i2) {
                for (int i3 = 0; i3 < this.r; ++i3) {
                    this.o[(i3 * this.q + i2) * this.p + n2] = new dk(this.k, this.a, n2 * 16, i2 * 16, i3 * 16, 16, this.s + n4);
                    if (this.w) {
                        this.o[(i3 * this.q + i2) * this.p + n2].z = this.v.get(n5);
                    }
                    this.o[(i3 * this.q + i2) * this.p + n2].y = false;
                    this.o[(i3 * this.q + i2) * this.p + n2].x = true;
                    this.o[(i3 * this.q + i2) * this.p + n2].o = true;
                    this.o[(i3 * this.q + i2) * this.p + n2].w = n5++;
                    this.o[(i3 * this.q + i2) * this.p + n2].f();
                    this.n[(i3 * this.q + i2) * this.p + n2] = this.o[(i3 * this.q + i2) * this.p + n2];
                    this.m.add(this.o[(i3 * this.q + i2) * this.p + n2]);
                    n4 += 3;
                }
            }
        }
        if (this.k != null && (ls2 = this.t.i) != null) {
            this.b(in.b(ls2.aM), in.b(ls2.aN), in.b(ls2.aO));
            Arrays.sort(this.n, new jo(ls2));
        }
        this.I = 2;
    }

    public void a(bt bt2, yn yn2, float f2) {
        sn sn2;
        int n2;
        if (this.I > 0) {
            --this.I;
            return;
        }
        ll.a.a(this.k, this.l, this.t.q, this.t.i, f2);
        th.a.a(this.k, this.l, this.t.q, this.t.i, this.t.z, f2);
        this.J = 0;
        this.K = 0;
        this.L = 0;
        ls ls2 = this.t.i;
        th.b = ls2.bl + (ls2.aM - ls2.bl) * (double)f2;
        th.c = ls2.bm + (ls2.aN - ls2.bm) * (double)f2;
        th.d = ls2.bn + (ls2.aO - ls2.bn) * (double)f2;
        ll.b = ls2.bl + (ls2.aM - ls2.bl) * (double)f2;
        ll.c = ls2.bm + (ls2.aN - ls2.bm) * (double)f2;
        ll.d = ls2.bn + (ls2.aO - ls2.bn) * (double)f2;
        List list = this.k.o();
        this.J = list.size();
        for (n2 = 0; n2 < this.k.e.size(); ++n2) {
            sn2 = (sn)this.k.e.get(n2);
            ++this.K;
            if (!sn2.a(bt2)) continue;
            th.a.a(sn2, f2);
        }
        for (n2 = 0; n2 < list.size(); ++n2) {
            sn2 = (sn)list.get(n2);
            if (!sn2.a(bt2) || !sn2.bM && !yn2.a(sn2.aW) || sn2 == this.t.i && !this.t.z.A && !this.t.i.N()) continue;
            int n3 = in.b(sn2.aN);
            if (n3 < 0) {
                n3 = 0;
            }
            if (n3 >= 128) {
                n3 = 127;
            }
            if (!this.k.i(in.b(sn2.aM), n3, in.b(sn2.aO))) continue;
            ++this.K;
            th.a.a(sn2, f2);
        }
        for (n2 = 0; n2 < this.a.size(); ++n2) {
            ll.a.a((ow)this.a.get(n2), f2);
        }
    }

    public String b() {
        return "C: " + this.P + "/" + this.M + ". F: " + this.N + ", O: " + this.O + ", E: " + this.Q;
    }

    public String c() {
        return "E: " + this.K + "/" + this.J + ". B: " + this.L + ", I: " + (this.J - this.L - this.K);
    }

    private void b(int n2, int n3, int n4) {
        n2 -= 8;
        n3 -= 8;
        n4 -= 8;
        this.B = Integer.MAX_VALUE;
        this.C = Integer.MAX_VALUE;
        this.D = Integer.MAX_VALUE;
        this.E = Integer.MIN_VALUE;
        this.F = Integer.MIN_VALUE;
        this.G = Integer.MIN_VALUE;
        int n5 = this.p * 16;
        int n6 = n5 / 2;
        for (int i2 = 0; i2 < this.p; ++i2) {
            int n7 = i2 * 16;
            int n8 = n7 + n6 - n2;
            if (n8 < 0) {
                n8 -= n5 - 1;
            }
            if ((n7 -= (n8 /= n5) * n5) < this.B) {
                this.B = n7;
            }
            if (n7 > this.E) {
                this.E = n7;
            }
            for (int i3 = 0; i3 < this.r; ++i3) {
                int n9 = i3 * 16;
                int n10 = n9 + n6 - n4;
                if (n10 < 0) {
                    n10 -= n5 - 1;
                }
                if ((n9 -= (n10 /= n5) * n5) < this.D) {
                    this.D = n9;
                }
                if (n9 > this.G) {
                    this.G = n9;
                }
                for (int i4 = 0; i4 < this.q; ++i4) {
                    int n11 = i4 * 16;
                    if (n11 < this.C) {
                        this.C = n11;
                    }
                    if (n11 > this.F) {
                        this.F = n11;
                    }
                    dk dk2 = this.o[(i3 * this.q + i4) * this.p + i2];
                    boolean bl2 = dk2.u;
                    dk2.a(n7, n11, n9);
                    if (bl2 || !dk2.u) continue;
                    this.m.add(dk2);
                }
            }
        }
    }

    public int a(ls ls2, int n2, double d2) {
        for (int i2 = 0; i2 < 10; ++i2) {
            this.R = (this.R + 1) % this.o.length;
            dk dk2 = this.o[this.R];
            if (!dk2.u || this.m.contains(dk2)) continue;
            this.m.add(dk2);
        }
        if (this.t.z.e != this.H) {
            this.a();
        }
        if (n2 == 0) {
            this.M = 0;
            this.N = 0;
            this.O = 0;
            this.P = 0;
            this.Q = 0;
        }
        double d3 = ls2.bl + (ls2.aM - ls2.bl) * d2;
        double d4 = ls2.bm + (ls2.aN - ls2.bm) * d2;
        double d5 = ls2.bn + (ls2.aO - ls2.bn) * d2;
        double d6 = ls2.aM - this.f;
        double d7 = ls2.aN - this.g;
        double d8 = ls2.aO - this.h;
        if (d6 * d6 + d7 * d7 + d8 * d8 > 16.0) {
            this.f = ls2.aM;
            this.g = ls2.aN;
            this.h = ls2.aO;
            this.b(in.b(ls2.aM), in.b(ls2.aN), in.b(ls2.aO));
            Arrays.sort(this.n, new jo(ls2));
        }
        u.a();
        int n3 = 0;
        if (this.w && this.t.z.h && !this.t.z.g && n2 == 0) {
            int n4 = 0;
            int n5 = 16;
            this.a(n4, n5);
            for (int i3 = n4; i3 < n5; ++i3) {
                this.n[i3].x = true;
            }
            n3 += this.a(n4, n5, n2, d2);
            do {
                n4 = n5;
                if ((n5 *= 2) > this.n.length) {
                    n5 = this.n.length;
                }
                GL11.glDisable((int)3553);
                GL11.glDisable((int)2896);
                GL11.glDisable((int)3008);
                GL11.glDisable((int)2912);
                GL11.glColorMask((boolean)false, (boolean)false, (boolean)false, (boolean)false);
                GL11.glDepthMask((boolean)false);
                this.a(n4, n5);
                GL11.glPushMatrix();
                float f2 = 0.0f;
                float f3 = 0.0f;
                float f4 = 0.0f;
                for (int i4 = n4; i4 < n5; ++i4) {
                    float f5;
                    int n6;
                    if (this.n[i4].e()) {
                        this.n[i4].o = false;
                        continue;
                    }
                    if (!this.n[i4].o) {
                        this.n[i4].x = true;
                    }
                    if (!this.n[i4].o || this.n[i4].y || this.x % (n6 = (int)(1.0f + (f5 = in.c(this.n[i4].a(ls2))) / 128.0f)) != i4 % n6) continue;
                    dk dk3 = this.n[i4];
                    float f6 = (float)((double)dk3.i - d3);
                    float f7 = (float)((double)dk3.j - d4);
                    float f8 = (float)((double)dk3.k - d5);
                    float f9 = f6 - f2;
                    float f10 = f7 - f3;
                    float f11 = f8 - f4;
                    if (f9 != 0.0f || f10 != 0.0f || f11 != 0.0f) {
                        GL11.glTranslatef((float)f9, (float)f10, (float)f11);
                        f2 += f9;
                        f3 += f10;
                        f4 += f11;
                    }
                    ARBOcclusionQuery.glBeginQueryARB((int)35092, (int)this.n[i4].z);
                    this.n[i4].d();
                    ARBOcclusionQuery.glEndQueryARB((int)35092);
                    this.n[i4].y = true;
                }
                GL11.glPopMatrix();
                if (this.t.z.g) {
                    if (px.b == 0) {
                        GL11.glColorMask((boolean)false, (boolean)true, (boolean)true, (boolean)true);
                    } else {
                        GL11.glColorMask((boolean)true, (boolean)false, (boolean)false, (boolean)true);
                    }
                } else {
                    GL11.glColorMask((boolean)true, (boolean)true, (boolean)true, (boolean)true);
                }
                GL11.glDepthMask((boolean)true);
                GL11.glEnable((int)3553);
                GL11.glEnable((int)3008);
                GL11.glEnable((int)2912);
                n3 += this.a(n4, n5, n2, d2);
            } while (n5 < this.n.length);
        } else {
            n3 += this.a(0, this.n.length, n2, d2);
        }
        return n3;
    }

    private void a(int n2, int n3) {
        for (int i2 = n2; i2 < n3; ++i2) {
            if (!this.n[i2].y) continue;
            this.c.clear();
            ARBOcclusionQuery.glGetQueryObjectuARB((int)this.n[i2].z, (int)34919, (IntBuffer)this.c);
            if (this.c.get(0) == 0) continue;
            this.n[i2].y = false;
            this.c.clear();
            ARBOcclusionQuery.glGetQueryObjectuARB((int)this.n[i2].z, (int)34918, (IntBuffer)this.c);
            this.n[i2].x = this.c.get(0) != 0;
        }
    }

    private int a(int n2, int n3, int n4, double d2) {
        int n5;
        this.S.clear();
        int n6 = 0;
        for (int i2 = n2; i2 < n3; ++i2) {
            int n7;
            if (n4 == 0) {
                ++this.M;
                if (this.n[i2].p[n4]) {
                    ++this.Q;
                } else if (!this.n[i2].o) {
                    ++this.N;
                } else if (this.w && !this.n[i2].x) {
                    ++this.O;
                } else {
                    ++this.P;
                }
            }
            if (this.n[i2].p[n4] || !this.n[i2].o || this.w && !this.n[i2].x || (n7 = this.n[i2].a(n4)) < 0) continue;
            this.S.add(this.n[i2]);
            ++n6;
        }
        ls ls2 = this.t.i;
        double d3 = ls2.bl + (ls2.aM - ls2.bl) * d2;
        double d4 = ls2.bm + (ls2.aN - ls2.bm) * d2;
        double d5 = ls2.bn + (ls2.aO - ls2.bn) * d2;
        int n8 = 0;
        for (n5 = 0; n5 < this.T.length; ++n5) {
            this.T[n5].b();
        }
        for (n5 = 0; n5 < this.S.size(); ++n5) {
            dk dk2 = (dk)this.S.get(n5);
            int n9 = -1;
            for (int i3 = 0; i3 < n8; ++i3) {
                if (!this.T[i3].a(dk2.i, dk2.j, dk2.k)) continue;
                n9 = i3;
            }
            if (n9 < 0) {
                n9 = n8++;
                this.T[n9].a(dk2.i, dk2.j, dk2.k, d3, d4, d5);
            }
            this.T[n9].a(dk2.a(n4));
        }
        this.a(n4, d2);
        return n6;
    }

    public void a(int n2, double d2) {
        for (int i2 = 0; i2 < this.T.length; ++i2) {
            this.T[i2].a();
        }
    }

    public void d() {
        ++this.x;
    }

    public void a(float f2) {
        float f3;
        float f4;
        float f5;
        float f6;
        if (this.t.f.t.c) {
            return;
        }
        GL11.glDisable((int)3553);
        bt bt2 = this.k.a((sn)this.t.i, f2);
        float f7 = (float)bt2.a;
        float f8 = (float)bt2.b;
        float f9 = (float)bt2.c;
        if (this.t.z.g) {
            float f10 = (f7 * 30.0f + f8 * 59.0f + f9 * 11.0f) / 100.0f;
            float f11 = (f7 * 30.0f + f8 * 70.0f) / 100.0f;
            f6 = (f7 * 30.0f + f9 * 70.0f) / 100.0f;
            f7 = f10;
            f8 = f11;
            f9 = f6;
        }
        GL11.glColor3f((float)f7, (float)f8, (float)f9);
        nw nw2 = nw.a;
        GL11.glDepthMask((boolean)false);
        GL11.glEnable((int)2912);
        GL11.glColor3f((float)f7, (float)f8, (float)f9);
        GL11.glCallList((int)this.z);
        GL11.glDisable((int)2912);
        GL11.glDisable((int)3008);
        GL11.glEnable((int)3042);
        GL11.glBlendFunc((int)770, (int)771);
        u.a();
        float[] fArray = this.k.t.a(this.k.b(f2), f2);
        if (fArray != null) {
            float f12;
            GL11.glDisable((int)3553);
            GL11.glShadeModel((int)7425);
            GL11.glPushMatrix();
            GL11.glRotatef((float)90.0f, (float)1.0f, (float)0.0f, (float)0.0f);
            f6 = this.k.b(f2);
            GL11.glRotatef((float)(f6 > 0.5f ? 180.0f : 0.0f), (float)0.0f, (float)0.0f, (float)1.0f);
            f5 = fArray[0];
            f4 = fArray[1];
            f3 = fArray[2];
            if (this.t.z.g) {
                float f13 = (f5 * 30.0f + f4 * 59.0f + f3 * 11.0f) / 100.0f;
                float f14 = (f5 * 30.0f + f4 * 70.0f) / 100.0f;
                f12 = (f5 * 30.0f + f3 * 70.0f) / 100.0f;
                f5 = f13;
                f4 = f14;
                f3 = f12;
            }
            nw2.a(6);
            nw2.a(f5, f4, f3, fArray[3]);
            nw2.a(0.0, 100.0, 0.0);
            int n2 = 16;
            nw2.a(fArray[0], fArray[1], fArray[2], 0.0f);
            for (int i2 = 0; i2 <= n2; ++i2) {
                f12 = (float)i2 * (float)Math.PI * 2.0f / (float)n2;
                float f15 = in.a(f12);
                float f16 = in.b(f12);
                nw2.a((double)(f15 * 120.0f), (double)(f16 * 120.0f), (double)(-f16 * 40.0f * fArray[3]));
            }
            nw2.a();
            GL11.glPopMatrix();
            GL11.glShadeModel((int)7424);
        }
        GL11.glEnable((int)3553);
        GL11.glBlendFunc((int)770, (int)1);
        GL11.glPushMatrix();
        float f17 = 1.0f - this.k.g(f2);
        f6 = 0.0f;
        f5 = 0.0f;
        f4 = 0.0f;
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)f17);
        GL11.glTranslatef((float)f6, (float)f5, (float)f4);
        GL11.glRotatef((float)0.0f, (float)0.0f, (float)0.0f, (float)1.0f);
        GL11.glRotatef((float)(this.k.b(f2) * 360.0f), (float)1.0f, (float)0.0f, (float)0.0f);
        f3 = 30.0f;
        GL11.glBindTexture((int)3553, (int)this.l.b("/terrain/sun.png"));
        nw2.b();
        nw2.a(-f3, 100.0, -f3, 0.0, 0.0);
        nw2.a(f3, 100.0, -f3, 1.0, 0.0);
        nw2.a(f3, 100.0, f3, 1.0, 1.0);
        nw2.a(-f3, 100.0, f3, 0.0, 1.0);
        nw2.a();
        f3 = 20.0f;
        GL11.glBindTexture((int)3553, (int)this.l.b("/terrain/moon.png"));
        nw2.b();
        nw2.a(-f3, -100.0, f3, 1.0, 1.0);
        nw2.a(f3, -100.0, f3, 0.0, 1.0);
        nw2.a(f3, -100.0, -f3, 0.0, 0.0);
        nw2.a(-f3, -100.0, -f3, 1.0, 0.0);
        nw2.a();
        GL11.glDisable((int)3553);
        float f18 = this.k.e(f2) * f17;
        if (f18 > 0.0f) {
            GL11.glColor4f((float)f18, (float)f18, (float)f18, (float)f18);
            GL11.glCallList((int)this.y);
        }
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        GL11.glDisable((int)3042);
        GL11.glEnable((int)3008);
        GL11.glEnable((int)2912);
        GL11.glPopMatrix();
        if (this.k.t.c()) {
            GL11.glColor3f((float)(f7 * 0.2f + 0.04f), (float)(f8 * 0.2f + 0.04f), (float)(f9 * 0.6f + 0.1f));
        } else {
            GL11.glColor3f((float)f7, (float)f8, (float)f9);
        }
        GL11.glDisable((int)3553);
        GL11.glCallList((int)this.A);
        GL11.glEnable((int)3553);
        GL11.glDepthMask((boolean)true);
    }

    public void b(float f2) {
        float f3;
        if (this.t.f.t.c) {
            return;
        }
        if (this.t.z.j) {
            this.c(f2);
            return;
        }
        GL11.glDisable((int)2884);
        float f4 = (float)(this.t.i.bm + (this.t.i.aN - this.t.i.bm) * (double)f2);
        int n2 = 32;
        int n3 = 256 / n2;
        nw nw2 = nw.a;
        GL11.glBindTexture((int)3553, (int)this.l.b("/environment/clouds.png"));
        GL11.glEnable((int)3042);
        GL11.glBlendFunc((int)770, (int)771);
        bt bt2 = this.k.c(f2);
        float f5 = (float)bt2.a;
        float f6 = (float)bt2.b;
        float f7 = (float)bt2.c;
        if (this.t.z.g) {
            f3 = (f5 * 30.0f + f6 * 59.0f + f7 * 11.0f) / 100.0f;
            float f8 = (f5 * 30.0f + f6 * 70.0f) / 100.0f;
            float f9 = (f5 * 30.0f + f7 * 70.0f) / 100.0f;
            f5 = f3;
            f6 = f8;
            f7 = f9;
        }
        f3 = 4.8828125E-4f;
        double d2 = this.t.i.aJ + (this.t.i.aM - this.t.i.aJ) * (double)f2 + (double)(((float)this.x + f2) * 0.03f);
        double d3 = this.t.i.aL + (this.t.i.aO - this.t.i.aL) * (double)f2;
        int n4 = in.b(d2 / 2048.0);
        int n5 = in.b(d3 / 2048.0);
        float f10 = this.k.t.d() - f4 + 0.33f;
        float f11 = (float)((d2 -= (double)(n4 * 2048)) * (double)f3);
        float f12 = (float)((d3 -= (double)(n5 * 2048)) * (double)f3);
        nw2.b();
        nw2.a(f5, f6, f7, 0.8f);
        for (int i2 = -n2 * n3; i2 < n2 * n3; i2 += n2) {
            for (int i3 = -n2 * n3; i3 < n2 * n3; i3 += n2) {
                nw2.a(i2 + 0, f10, i3 + n2, (float)(i2 + 0) * f3 + f11, (float)(i3 + n2) * f3 + f12);
                nw2.a(i2 + n2, f10, i3 + n2, (float)(i2 + n2) * f3 + f11, (float)(i3 + n2) * f3 + f12);
                nw2.a(i2 + n2, f10, i3 + 0, (float)(i2 + n2) * f3 + f11, (float)(i3 + 0) * f3 + f12);
                nw2.a(i2 + 0, f10, i3 + 0, (float)(i2 + 0) * f3 + f11, (float)(i3 + 0) * f3 + f12);
            }
        }
        nw2.a();
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        GL11.glDisable((int)3042);
        GL11.glEnable((int)2884);
    }

    public boolean a(double d2, double d3, double d4, float f2) {
        return false;
    }

    public void c(float f2) {
        float f3;
        float f4;
        float f5;
        GL11.glDisable((int)2884);
        float f6 = (float)(this.t.i.bm + (this.t.i.aN - this.t.i.bm) * (double)f2);
        nw nw2 = nw.a;
        float f7 = 12.0f;
        float f8 = 4.0f;
        double d2 = (this.t.i.aJ + (this.t.i.aM - this.t.i.aJ) * (double)f2 + (double)(((float)this.x + f2) * 0.03f)) / (double)f7;
        double d3 = (this.t.i.aL + (this.t.i.aO - this.t.i.aL) * (double)f2) / (double)f7 + (double)0.33f;
        float f9 = this.k.t.d() - f6 + 0.33f;
        int n2 = in.b(d2 / 2048.0);
        int n3 = in.b(d3 / 2048.0);
        d2 -= (double)(n2 * 2048);
        d3 -= (double)(n3 * 2048);
        GL11.glBindTexture((int)3553, (int)this.l.b("/environment/clouds.png"));
        GL11.glEnable((int)3042);
        GL11.glBlendFunc((int)770, (int)771);
        bt bt2 = this.k.c(f2);
        float f10 = (float)bt2.a;
        float f11 = (float)bt2.b;
        float f12 = (float)bt2.c;
        if (this.t.z.g) {
            f5 = (f10 * 30.0f + f11 * 59.0f + f12 * 11.0f) / 100.0f;
            f4 = (f10 * 30.0f + f11 * 70.0f) / 100.0f;
            f3 = (f10 * 30.0f + f12 * 70.0f) / 100.0f;
            f10 = f5;
            f11 = f4;
            f12 = f3;
        }
        f5 = (float)(d2 * 0.0);
        f4 = (float)(d3 * 0.0);
        f3 = 0.00390625f;
        f5 = (float)in.b(d2) * f3;
        f4 = (float)in.b(d3) * f3;
        float f13 = (float)(d2 - (double)in.b(d2));
        float f14 = (float)(d3 - (double)in.b(d3));
        int n4 = 8;
        int n5 = 3;
        float f15 = 9.765625E-4f;
        GL11.glScalef((float)f7, (float)1.0f, (float)f7);
        for (int i2 = 0; i2 < 2; ++i2) {
            if (i2 == 0) {
                GL11.glColorMask((boolean)false, (boolean)false, (boolean)false, (boolean)false);
            } else if (this.t.z.g) {
                if (px.b == 0) {
                    GL11.glColorMask((boolean)false, (boolean)true, (boolean)true, (boolean)true);
                } else {
                    GL11.glColorMask((boolean)true, (boolean)false, (boolean)false, (boolean)true);
                }
            } else {
                GL11.glColorMask((boolean)true, (boolean)true, (boolean)true, (boolean)true);
            }
            for (int i3 = -n5 + 1; i3 <= n5; ++i3) {
                for (int i4 = -n5 + 1; i4 <= n5; ++i4) {
                    int n6;
                    nw2.b();
                    float f16 = i3 * n4;
                    float f17 = i4 * n4;
                    float f18 = f16 - f13;
                    float f19 = f17 - f14;
                    if (f9 > -f8 - 1.0f) {
                        nw2.a(f10 * 0.7f, f11 * 0.7f, f12 * 0.7f, 0.8f);
                        nw2.b(0.0f, -1.0f, 0.0f);
                        nw2.a(f18 + 0.0f, f9 + 0.0f, f19 + (float)n4, (f16 + 0.0f) * f3 + f5, (f17 + (float)n4) * f3 + f4);
                        nw2.a(f18 + (float)n4, f9 + 0.0f, f19 + (float)n4, (f16 + (float)n4) * f3 + f5, (f17 + (float)n4) * f3 + f4);
                        nw2.a(f18 + (float)n4, f9 + 0.0f, f19 + 0.0f, (f16 + (float)n4) * f3 + f5, (f17 + 0.0f) * f3 + f4);
                        nw2.a(f18 + 0.0f, f9 + 0.0f, f19 + 0.0f, (f16 + 0.0f) * f3 + f5, (f17 + 0.0f) * f3 + f4);
                    }
                    if (f9 <= f8 + 1.0f) {
                        nw2.a(f10, f11, f12, 0.8f);
                        nw2.b(0.0f, 1.0f, 0.0f);
                        nw2.a(f18 + 0.0f, f9 + f8 - f15, f19 + (float)n4, (f16 + 0.0f) * f3 + f5, (f17 + (float)n4) * f3 + f4);
                        nw2.a(f18 + (float)n4, f9 + f8 - f15, f19 + (float)n4, (f16 + (float)n4) * f3 + f5, (f17 + (float)n4) * f3 + f4);
                        nw2.a(f18 + (float)n4, f9 + f8 - f15, f19 + 0.0f, (f16 + (float)n4) * f3 + f5, (f17 + 0.0f) * f3 + f4);
                        nw2.a(f18 + 0.0f, f9 + f8 - f15, f19 + 0.0f, (f16 + 0.0f) * f3 + f5, (f17 + 0.0f) * f3 + f4);
                    }
                    nw2.a(f10 * 0.9f, f11 * 0.9f, f12 * 0.9f, 0.8f);
                    if (i3 > -1) {
                        nw2.b(-1.0f, 0.0f, 0.0f);
                        for (n6 = 0; n6 < n4; ++n6) {
                            nw2.a(f18 + (float)n6 + 0.0f, f9 + 0.0f, f19 + (float)n4, (f16 + (float)n6 + 0.5f) * f3 + f5, (f17 + (float)n4) * f3 + f4);
                            nw2.a(f18 + (float)n6 + 0.0f, f9 + f8, f19 + (float)n4, (f16 + (float)n6 + 0.5f) * f3 + f5, (f17 + (float)n4) * f3 + f4);
                            nw2.a(f18 + (float)n6 + 0.0f, f9 + f8, f19 + 0.0f, (f16 + (float)n6 + 0.5f) * f3 + f5, (f17 + 0.0f) * f3 + f4);
                            nw2.a(f18 + (float)n6 + 0.0f, f9 + 0.0f, f19 + 0.0f, (f16 + (float)n6 + 0.5f) * f3 + f5, (f17 + 0.0f) * f3 + f4);
                        }
                    }
                    if (i3 <= 1) {
                        nw2.b(1.0f, 0.0f, 0.0f);
                        for (n6 = 0; n6 < n4; ++n6) {
                            nw2.a(f18 + (float)n6 + 1.0f - f15, f9 + 0.0f, f19 + (float)n4, (f16 + (float)n6 + 0.5f) * f3 + f5, (f17 + (float)n4) * f3 + f4);
                            nw2.a(f18 + (float)n6 + 1.0f - f15, f9 + f8, f19 + (float)n4, (f16 + (float)n6 + 0.5f) * f3 + f5, (f17 + (float)n4) * f3 + f4);
                            nw2.a(f18 + (float)n6 + 1.0f - f15, f9 + f8, f19 + 0.0f, (f16 + (float)n6 + 0.5f) * f3 + f5, (f17 + 0.0f) * f3 + f4);
                            nw2.a(f18 + (float)n6 + 1.0f - f15, f9 + 0.0f, f19 + 0.0f, (f16 + (float)n6 + 0.5f) * f3 + f5, (f17 + 0.0f) * f3 + f4);
                        }
                    }
                    nw2.a(f10 * 0.8f, f11 * 0.8f, f12 * 0.8f, 0.8f);
                    if (i4 > -1) {
                        nw2.b(0.0f, 0.0f, -1.0f);
                        for (n6 = 0; n6 < n4; ++n6) {
                            nw2.a(f18 + 0.0f, f9 + f8, f19 + (float)n6 + 0.0f, (f16 + 0.0f) * f3 + f5, (f17 + (float)n6 + 0.5f) * f3 + f4);
                            nw2.a(f18 + (float)n4, f9 + f8, f19 + (float)n6 + 0.0f, (f16 + (float)n4) * f3 + f5, (f17 + (float)n6 + 0.5f) * f3 + f4);
                            nw2.a(f18 + (float)n4, f9 + 0.0f, f19 + (float)n6 + 0.0f, (f16 + (float)n4) * f3 + f5, (f17 + (float)n6 + 0.5f) * f3 + f4);
                            nw2.a(f18 + 0.0f, f9 + 0.0f, f19 + (float)n6 + 0.0f, (f16 + 0.0f) * f3 + f5, (f17 + (float)n6 + 0.5f) * f3 + f4);
                        }
                    }
                    if (i4 <= 1) {
                        nw2.b(0.0f, 0.0f, 1.0f);
                        for (n6 = 0; n6 < n4; ++n6) {
                            nw2.a(f18 + 0.0f, f9 + f8, f19 + (float)n6 + 1.0f - f15, (f16 + 0.0f) * f3 + f5, (f17 + (float)n6 + 0.5f) * f3 + f4);
                            nw2.a(f18 + (float)n4, f9 + f8, f19 + (float)n6 + 1.0f - f15, (f16 + (float)n4) * f3 + f5, (f17 + (float)n6 + 0.5f) * f3 + f4);
                            nw2.a(f18 + (float)n4, f9 + 0.0f, f19 + (float)n6 + 1.0f - f15, (f16 + (float)n4) * f3 + f5, (f17 + (float)n6 + 0.5f) * f3 + f4);
                            nw2.a(f18 + 0.0f, f9 + 0.0f, f19 + (float)n6 + 1.0f - f15, (f16 + 0.0f) * f3 + f5, (f17 + (float)n6 + 0.5f) * f3 + f4);
                        }
                    }
                    nw2.a();
                }
            }
        }
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)1.0f);
        GL11.glDisable((int)3042);
        GL11.glEnable((int)2884);
    }

    public boolean a(ls ls2, boolean bl2) {
        int n2;
        int n3;
        dk dk2;
        int n4;
        boolean bl3 = false;
        if (bl3) {
            Collections.sort(this.m, new md(ls2));
            int n5 = this.m.size() - 1;
            int n6 = this.m.size();
            for (int i2 = 0; i2 < n6; ++i2) {
                dk dk3 = (dk)this.m.get(n5 - i2);
                if (!bl2) {
                    if (dk3.a(ls2) > 256.0f && (dk3.o ? i2 >= 3 : i2 >= 1)) {
                        return false;
                    }
                } else if (!dk3.o) continue;
                dk3.a();
                this.m.remove(dk3);
                dk3.u = false;
            }
            return this.m.size() == 0;
        }
        int n7 = 2;
        md md2 = new md(ls2);
        dk[] dkArray = new dk[n7];
        ArrayList<dk> arrayList = null;
        int n8 = this.m.size();
        int n9 = 0;
        for (n4 = 0; n4 < n8; ++n4) {
            dk2 = (dk)this.m.get(n4);
            if (!bl2) {
                if (dk2.a(ls2) > 256.0f) {
                    int n10;
                    for (n10 = 0; n10 < n7 && (dkArray[n10] == null || md2.a(dkArray[n10], dk2) <= 0); ++n10) {
                    }
                    if (--n10 <= 0) continue;
                    n3 = n10;
                    while (--n3 != 0) {
                        dkArray[n3 - 1] = dkArray[n3];
                    }
                    dkArray[n10] = dk2;
                    continue;
                }
            } else if (!dk2.o) continue;
            if (arrayList == null) {
                arrayList = new ArrayList<dk>();
            }
            ++n9;
            arrayList.add(dk2);
            this.m.set(n4, null);
        }
        if (arrayList != null) {
            if (arrayList.size() > 1) {
                Collections.sort(arrayList, md2);
            }
            for (n4 = arrayList.size() - 1; n4 >= 0; --n4) {
                dk2 = (dk)arrayList.get(n4);
                dk2.a();
                dk2.u = false;
            }
        }
        n4 = 0;
        for (n2 = n7 - 1; n2 >= 0; --n2) {
            dk dk4 = dkArray[n2];
            if (dk4 == null) continue;
            if (!dk4.o && n2 != n7 - 1) {
                dkArray[n2] = null;
                dkArray[0] = null;
                break;
            }
            dkArray[n2].a();
            dkArray[n2].u = false;
            ++n4;
        }
        int n11 = 0;
        n3 = this.m.size();
        for (n2 = 0; n2 != n3; ++n2) {
            dk dk5 = (dk)this.m.get(n2);
            if (dk5 == null) continue;
            boolean bl4 = false;
            for (int i3 = 0; i3 < n7 && !bl4; ++i3) {
                if (dk5 != dkArray[i3]) continue;
                bl4 = true;
            }
            if (bl4) continue;
            if (n11 != n2) {
                this.m.set(n11, dk5);
            }
            ++n11;
        }
        while (--n2 >= n11) {
            this.m.remove(n2);
        }
        return n8 == n9 + n4;
    }

    public void a(gs gs2, vf vf2, int n2, iz iz2, float f2) {
        nw nw2 = nw.a;
        GL11.glEnable((int)3042);
        GL11.glEnable((int)3008);
        GL11.glBlendFunc((int)770, (int)1);
        GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)((in.a((float)System.currentTimeMillis() / 100.0f) * 0.2f + 0.4f) * 0.5f));
        if (n2 == 0) {
            if (this.i > 0.0f) {
                GL11.glBlendFunc((int)774, (int)768);
                int n3 = this.l.b("/terrain.png");
                GL11.glBindTexture((int)3553, (int)n3);
                GL11.glColor4f((float)1.0f, (float)1.0f, (float)1.0f, (float)0.5f);
                GL11.glPushMatrix();
                int n4 = this.k.a(vf2.b, vf2.c, vf2.d);
                uu uu2 = n4 > 0 ? uu.m[n4] : null;
                GL11.glDisable((int)3008);
                GL11.glPolygonOffset((float)-3.0f, (float)-3.0f);
                GL11.glEnable((int)32823);
                double d2 = gs2.bl + (gs2.aM - gs2.bl) * (double)f2;
                double d3 = gs2.bm + (gs2.aN - gs2.bm) * (double)f2;
                double d4 = gs2.bn + (gs2.aO - gs2.bn) * (double)f2;
                if (uu2 == null) {
                    uu2 = uu.u;
                }
                GL11.glEnable((int)3008);
                nw2.b();
                nw2.b(-d2, -d3, -d4);
                nw2.c();
                this.u.a(uu2, vf2.b, vf2.c, vf2.d, 240 + (int)(this.i * 10.0f));
                nw2.a();
                nw2.b(0.0, 0.0, 0.0);
                GL11.glDisable((int)3008);
                GL11.glPolygonOffset((float)0.0f, (float)0.0f);
                GL11.glDisable((int)32823);
                GL11.glEnable((int)3008);
                GL11.glDepthMask((boolean)true);
                GL11.glPopMatrix();
            }
        } else if (iz2 != null) {
            GL11.glBlendFunc((int)770, (int)771);
            float f3 = in.a((float)System.currentTimeMillis() / 100.0f) * 0.2f + 0.8f;
            GL11.glColor4f((float)f3, (float)f3, (float)f3, (float)(in.a((float)System.currentTimeMillis() / 200.0f) * 0.2f + 0.5f));
            int n5 = this.l.b("/terrain.png");
            GL11.glBindTexture((int)3553, (int)n5);
            int n6 = vf2.b;
            int n7 = vf2.c;
            int n8 = vf2.d;
            if (vf2.e == 0) {
                --n7;
            }
            if (vf2.e == 1) {
                ++n7;
            }
            if (vf2.e == 2) {
                --n8;
            }
            if (vf2.e == 3) {
                ++n8;
            }
            if (vf2.e == 4) {
                --n6;
            }
            if (vf2.e == 5) {
                ++n6;
            }
        }
        GL11.glDisable((int)3042);
        GL11.glDisable((int)3008);
    }

    public void b(gs gs2, vf vf2, int n2, iz iz2, float f2) {
        if (n2 == 0 && vf2.a == jg.a) {
            GL11.glEnable((int)3042);
            GL11.glBlendFunc((int)770, (int)771);
            GL11.glColor4f((float)0.0f, (float)0.0f, (float)0.0f, (float)0.4f);
            GL11.glLineWidth((float)2.0f);
            GL11.glDisable((int)3553);
            GL11.glDepthMask((boolean)false);
            float f3 = 0.002f;
            int n3 = this.k.a(vf2.b, vf2.c, vf2.d);
            if (n3 > 0) {
                uu.m[n3].a((xp)this.k, vf2.b, vf2.c, vf2.d);
                double d2 = gs2.bl + (gs2.aM - gs2.bl) * (double)f2;
                double d3 = gs2.bm + (gs2.aN - gs2.bm) * (double)f2;
                double d4 = gs2.bn + (gs2.aO - gs2.bn) * (double)f2;
                this.a(uu.m[n3].f(this.k, vf2.b, vf2.c, vf2.d).b(f3, f3, f3).c(-d2, -d3, -d4));
            }
            GL11.glDepthMask((boolean)true);
            GL11.glEnable((int)3553);
            GL11.glDisable((int)3042);
        }
    }

    private void a(eq eq2) {
        nw nw2 = nw.a;
        nw2.a(3);
        nw2.a(eq2.a, eq2.b, eq2.c);
        nw2.a(eq2.d, eq2.b, eq2.c);
        nw2.a(eq2.d, eq2.b, eq2.f);
        nw2.a(eq2.a, eq2.b, eq2.f);
        nw2.a(eq2.a, eq2.b, eq2.c);
        nw2.a();
        nw2.a(3);
        nw2.a(eq2.a, eq2.e, eq2.c);
        nw2.a(eq2.d, eq2.e, eq2.c);
        nw2.a(eq2.d, eq2.e, eq2.f);
        nw2.a(eq2.a, eq2.e, eq2.f);
        nw2.a(eq2.a, eq2.e, eq2.c);
        nw2.a();
        nw2.a(1);
        nw2.a(eq2.a, eq2.b, eq2.c);
        nw2.a(eq2.a, eq2.e, eq2.c);
        nw2.a(eq2.d, eq2.b, eq2.c);
        nw2.a(eq2.d, eq2.e, eq2.c);
        nw2.a(eq2.d, eq2.b, eq2.f);
        nw2.a(eq2.d, eq2.e, eq2.f);
        nw2.a(eq2.a, eq2.b, eq2.f);
        nw2.a(eq2.a, eq2.e, eq2.f);
        nw2.a();
    }

    public void a(int n2, int n3, int n4, int n5, int n6, int n7) {
        int n8 = in.a(n2, 16);
        int n9 = in.a(n3, 16);
        int n10 = in.a(n4, 16);
        int n11 = in.a(n5, 16);
        int n12 = in.a(n6, 16);
        int n13 = in.a(n7, 16);
        for (int i2 = n8; i2 <= n11; ++i2) {
            int n14 = i2 % this.p;
            if (n14 < 0) {
                n14 += this.p;
            }
            for (int i3 = n9; i3 <= n12; ++i3) {
                int n15 = i3 % this.q;
                if (n15 < 0) {
                    n15 += this.q;
                }
                for (int i4 = n10; i4 <= n13; ++i4) {
                    int n16 = i4 % this.r;
                    if (n16 < 0) {
                        n16 += this.r;
                    }
                    int n17 = (n16 * this.q + n15) * this.p + n14;
                    dk dk2 = this.o[n17];
                    if (dk2.u) continue;
                    this.m.add(dk2);
                    dk2.f();
                }
            }
        }
    }

    public void a(int n2, int n3, int n4) {
        this.a(n2 - 1, n3 - 1, n4 - 1, n2 + 1, n3 + 1, n4 + 1);
    }

    public void b(int n2, int n3, int n4, int n5, int n6, int n7) {
        this.a(n2 - 1, n3 - 1, n4 - 1, n5 + 1, n6 + 1, n7 + 1);
    }

    public void a(yn yn2, float f2) {
        for (int i2 = 0; i2 < this.o.length; ++i2) {
            if (this.o[i2].e() || this.o[i2].o && (i2 + this.j & 0xF) != 0) continue;
            this.o[i2].a(yn2);
        }
        ++this.j;
    }

    public void a(String string, int n2, int n3, int n4) {
        if (string != null) {
            this.t.v.b("C418 - " + string);
        }
        this.t.B.a(string, n2, n3, n4, 1.0f, 1.0f);
    }

    public void a(String string, double d2, double d3, double d4, float f2, float f3) {
        float f4 = 16.0f;
        if (f2 > 1.0f) {
            f4 *= f2;
        }
        if (this.t.i.g(d2, d3, d4) < (double)(f4 * f4)) {
            this.t.B.b(string, (float)d2, (float)d3, (float)d4, f2, f3);
        }
    }

    public void a(String string, double d2, double d3, double d4, double d5, double d6, double d7) {
        if (this.t == null || this.t.i == null || this.t.j == null) {
            return;
        }
        double d8 = this.t.i.aM - d2;
        double d9 = this.t.i.aN - d3;
        double d10 = this.t.i.aO - d4;
        double d11 = 16.0;
        if (d8 * d8 + d9 * d9 + d10 * d10 > d11 * d11) {
            return;
        }
        if (string.equals("bubble")) {
            this.t.j.a(new cr(this.k, d2, d3, d4, d5, d6, d7));
        } else if (string.equals("smoke")) {
            this.t.j.a(new xn(this.k, d2, d3, d4, d5, d6, d7));
        } else if (string.equals("note")) {
            this.t.j.a(new ae(this.k, d2, d3, d4, d5, d6, d7));
        } else if (string.equals("portal")) {
            this.t.j.a(new op(this.k, d2, d3, d4, d5, d6, d7));
        } else if (string.equals("explode")) {
            this.t.j.a(new gy(this.k, d2, d3, d4, d5, d6, d7));
        } else if (string.equals("flame")) {
            this.t.j.a(new qu(this.k, d2, d3, d4, d5, d6, d7));
        } else if (string.equals("lava")) {
            this.t.j.a(new fg(this.k, d2, d3, d4));
        } else if (string.equals("footstep")) {
            this.t.j.a(new gl(this.l, this.k, d2, d3, d4));
        } else if (string.equals("splash")) {
            this.t.j.a(new sy(this.k, d2, d3, d4, d5, d6, d7));
        } else if (string.equals("largesmoke")) {
            this.t.j.a(new xn(this.k, d2, d3, d4, d5, d6, d7, 2.5f));
        } else if (string.equals("reddust")) {
            this.t.j.a(new im(this.k, d2, d3, d4, (float)d5, (float)d6, (float)d7));
        } else if (string.equals("snowballpoof")) {
            this.t.j.a(new pb(this.k, d2, d3, d4, gm.aB));
        } else if (string.equals("snowshovel")) {
            this.t.j.a(new mu(this.k, d2, d3, d4, d5, d6, d7));
        } else if (string.equals("slime")) {
            this.t.j.a(new pb(this.k, d2, d3, d4, gm.aK));
        } else if (string.equals("heart")) {
            this.t.j.a(new kc(this.k, d2, d3, d4, d5, d6, d7));
        }
    }

    public void a(sn sn2) {
        sn2.u_();
        if (sn2.bA != null) {
            this.l.a(sn2.bA, new rr());
        }
        if (sn2.bB != null) {
            this.l.a(sn2.bB, new rr());
        }
    }

    public void b(sn sn2) {
        if (sn2.bA != null) {
            this.l.c(sn2.bA);
        }
        if (sn2.bB != null) {
            this.l.c(sn2.bB);
        }
    }

    public void e() {
        for (int i2 = 0; i2 < this.o.length; ++i2) {
            if (!this.o[i2].A || this.o[i2].u) continue;
            this.m.add(this.o[i2]);
            this.o[i2].f();
        }
    }

    public void a(int n2, int n3, int n4, ow ow2) {
    }

    public void f() {
        ge.b(this.s);
    }

    public void a(gs gs2, int n2, int n3, int n4, int n5, int n6) {
        Random random = this.k.r;
        switch (n2) {
            case 1001: {
                this.k.a((double)n3, (double)n4, (double)n5, "random.click", 1.0f, 1.2f);
                break;
            }
            case 1000: {
                this.k.a((double)n3, (double)n4, (double)n5, "random.click", 1.0f, 1.0f);
                break;
            }
            case 1002: {
                this.k.a((double)n3, (double)n4, (double)n5, "random.bow", 1.0f, 1.2f);
                break;
            }
            case 2000: {
                int n7 = n6 % 3 - 1;
                int n8 = n6 / 3 % 3 - 1;
                double d2 = (double)n3 + (double)n7 * 0.6 + 0.5;
                double d3 = (double)n4 + 0.5;
                double d4 = (double)n5 + (double)n8 * 0.6 + 0.5;
                for (int i2 = 0; i2 < 10; ++i2) {
                    double d5 = random.nextDouble() * 0.2 + 0.01;
                    double d6 = d2 + (double)n7 * 0.01 + (random.nextDouble() - 0.5) * (double)n8 * 0.5;
                    double d7 = d3 + (random.nextDouble() - 0.5) * 0.5;
                    double d8 = d4 + (double)n8 * 0.01 + (random.nextDouble() - 0.5) * (double)n7 * 0.5;
                    double d9 = (double)n7 * d5 + random.nextGaussian() * 0.01;
                    double d10 = -0.03 + random.nextGaussian() * 0.01;
                    double d11 = (double)n8 * d5 + random.nextGaussian() * 0.01;
                    this.a("smoke", d6, d7, d8, d9, d10, d11);
                }
                break;
            }
            case 2001: {
                int n9 = n6 & 0xFF;
                if (n9 > 0) {
                    uu uu2 = uu.m[n9];
                    this.t.B.b(uu2.by.a(), (float)n3 + 0.5f, (float)n4 + 0.5f, (float)n5 + 0.5f, (uu2.by.b() + 1.0f) / 2.0f, uu2.by.c() * 0.8f);
                }
                this.t.j.a(n3, n4, n5, n6 & 0xFF, n6 >> 8 & 0xFF);
                break;
            }
            case 1003: {
                if (Math.random() < 0.5) {
                    this.k.a((double)n3 + 0.5, (double)n4 + 0.5, (double)n5 + 0.5, "random.door_open", 1.0f, this.k.r.nextFloat() * 0.1f + 0.9f);
                    break;
                }
                this.k.a((double)n3 + 0.5, (double)n4 + 0.5, (double)n5 + 0.5, "random.door_close", 1.0f, this.k.r.nextFloat() * 0.1f + 0.9f);
                break;
            }
            case 1004: {
                this.k.a((float)n3 + 0.5f, (double)((float)n4 + 0.5f), (double)((float)n5 + 0.5f), "random.fizz", 0.5f, 2.6f + (random.nextFloat() - random.nextFloat()) * 0.8f);
                break;
            }
            case 1005: {
                if (gm.c[n6] instanceof tr) {
                    this.k.a(((tr)gm.c[n6]).a, n3, n4, n5);
                    break;
                }
                this.k.a((String)null, n3, n4, n5);
            }
        }
    }
}

