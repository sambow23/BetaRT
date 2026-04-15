/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.opengl.GL11
 */
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.lwjgl.opengl.GL11;

public class dn {
    protected fd a;
    private List[] b = new List[4];
    private ji c;
    private Random d = new Random();

    public dn(fd fd2, ji ji2) {
        if (fd2 != null) {
            this.a = fd2;
        }
        this.c = ji2;
        for (int i2 = 0; i2 < 4; ++i2) {
            this.b[i2] = new ArrayList();
        }
    }

    public void a(xw xw2) {
        int n2 = xw2.c_();
        if (this.b[n2].size() >= 4000) {
            this.b[n2].remove(0);
        }
        this.b[n2].add(xw2);
    }

    public void a() {
        for (int i2 = 0; i2 < 4; ++i2) {
            for (int i3 = 0; i3 < this.b[i2].size(); ++i3) {
                xw xw2 = (xw)this.b[i2].get(i3);
                xw2.w_();
                if (!xw2.be) continue;
                this.b[i2].remove(i3--);
            }
        }
    }

    public void a(sn sn2, float f2) {
        float f3 = in.b(sn2.aS * (float)Math.PI / 180.0f);
        float f4 = in.a(sn2.aS * (float)Math.PI / 180.0f);
        float f5 = -f4 * in.a(sn2.aT * (float)Math.PI / 180.0f);
        float f6 = f3 * in.a(sn2.aT * (float)Math.PI / 180.0f);
        float f7 = in.b(sn2.aT * (float)Math.PI / 180.0f);
        xw.l = sn2.bl + (sn2.aM - sn2.bl) * (double)f2;
        xw.m = sn2.bm + (sn2.aN - sn2.bm) * (double)f2;
        xw.n = sn2.bn + (sn2.aO - sn2.bn) * (double)f2;
        for (int i2 = 0; i2 < 3; ++i2) {
            if (this.b[i2].size() == 0) continue;
            int n2 = 0;
            if (i2 == 0) {
                n2 = this.c.b("/particles.png");
            }
            if (i2 == 1) {
                n2 = this.c.b("/terrain.png");
            }
            if (i2 == 2) {
                n2 = this.c.b("/gui/items.png");
            }
            GL11.glBindTexture((int)3553, (int)n2);
            nw nw2 = nw.a;
            nw2.b();
            for (int i3 = 0; i3 < this.b[i2].size(); ++i3) {
                xw xw2 = (xw)this.b[i2].get(i3);
                xw2.a(nw2, f2, f3, f7, f4, f5, f6);
            }
            nw2.a();
        }
    }

    public void b(sn sn2, float f2) {
        int n2 = 3;
        if (this.b[n2].size() == 0) {
            return;
        }
        nw nw2 = nw.a;
        for (int i2 = 0; i2 < this.b[n2].size(); ++i2) {
            xw xw2 = (xw)this.b[n2].get(i2);
            xw2.a(nw2, f2, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
        }
    }

    public void a(fd fd2) {
        this.a = fd2;
        for (int i2 = 0; i2 < 4; ++i2) {
            this.b[i2].clear();
        }
    }

    public void a(int n2, int n3, int n4, int n5, int n6) {
        if (n5 == 0) {
            return;
        }
        uu uu2 = uu.m[n5];
        int n7 = 4;
        for (int i2 = 0; i2 < n7; ++i2) {
            for (int i3 = 0; i3 < n7; ++i3) {
                for (int i4 = 0; i4 < n7; ++i4) {
                    double d2 = (double)n2 + ((double)i2 + 0.5) / (double)n7;
                    double d3 = (double)n3 + ((double)i3 + 0.5) / (double)n7;
                    double d4 = (double)n4 + ((double)i4 + 0.5) / (double)n7;
                    int n8 = this.d.nextInt(6);
                    this.a(new qm(this.a, d2, d3, d4, d2 - (double)n2 - 0.5, d3 - (double)n3 - 0.5, d4 - (double)n4 - 0.5, uu2, n8, n6).a(n2, n3, n4));
                }
            }
        }
    }

    public void a(int n2, int n3, int n4, int n5) {
        int n6 = this.a.a(n2, n3, n4);
        if (n6 == 0) {
            return;
        }
        uu uu2 = uu.m[n6];
        float f2 = 0.1f;
        double d2 = (double)n2 + this.d.nextDouble() * (uu2.bv - uu2.bs - (double)(f2 * 2.0f)) + (double)f2 + uu2.bs;
        double d3 = (double)n3 + this.d.nextDouble() * (uu2.bw - uu2.bt - (double)(f2 * 2.0f)) + (double)f2 + uu2.bt;
        double d4 = (double)n4 + this.d.nextDouble() * (uu2.bx - uu2.bu - (double)(f2 * 2.0f)) + (double)f2 + uu2.bu;
        if (n5 == 0) {
            d3 = (double)n3 + uu2.bt - (double)f2;
        }
        if (n5 == 1) {
            d3 = (double)n3 + uu2.bw + (double)f2;
        }
        if (n5 == 2) {
            d4 = (double)n4 + uu2.bu - (double)f2;
        }
        if (n5 == 3) {
            d4 = (double)n4 + uu2.bx + (double)f2;
        }
        if (n5 == 4) {
            d2 = (double)n2 + uu2.bs - (double)f2;
        }
        if (n5 == 5) {
            d2 = (double)n2 + uu2.bv + (double)f2;
        }
        this.a(new qm(this.a, d2, d3, d4, 0.0, 0.0, 0.0, uu2, n5, this.a.e(n2, n3, n4)).a(n2, n3, n4).c(0.2f).d(0.6f));
    }

    public String b() {
        return "" + (this.b[0].size() + this.b[1].size() + this.b[2].size());
    }
}

