/*
 * Decompiled with CFR 0.152.
 */
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class qx {
    public boolean a = false;
    private Random h = new Random();
    private fd i;
    public double b;
    public double c;
    public double d;
    public sn e;
    public float f;
    public Set g = new HashSet();

    public qx(fd fd2, sn sn2, double d2, double d3, double d4, float f2) {
        this.i = fd2;
        this.e = sn2;
        this.f = f2;
        this.b = d2;
        this.c = d3;
        this.d = d4;
    }

    public void a() {
        double d2;
        double d3;
        double d4;
        int n2;
        int n3;
        int n4;
        float f2 = this.f;
        int n5 = 16;
        for (n4 = 0; n4 < n5; ++n4) {
            for (n3 = 0; n3 < n5; ++n3) {
                for (n2 = 0; n2 < n5; ++n2) {
                    if (n4 != 0 && n4 != n5 - 1 && n3 != 0 && n3 != n5 - 1 && n2 != 0 && n2 != n5 - 1) continue;
                    double d5 = (float)n4 / ((float)n5 - 1.0f) * 2.0f - 1.0f;
                    double d6 = (float)n3 / ((float)n5 - 1.0f) * 2.0f - 1.0f;
                    double d7 = (float)n2 / ((float)n5 - 1.0f) * 2.0f - 1.0f;
                    double d8 = Math.sqrt(d5 * d5 + d6 * d6 + d7 * d7);
                    d5 /= d8;
                    d6 /= d8;
                    d7 /= d8;
                    d4 = this.b;
                    d3 = this.c;
                    d2 = this.d;
                    float f3 = 0.3f;
                    for (float f4 = this.f * (0.7f + this.i.r.nextFloat() * 0.6f); f4 > 0.0f; f4 -= f3 * 0.75f) {
                        int n6;
                        int n7;
                        int n8 = in.b(d4);
                        int n9 = this.i.a(n8, n7 = in.b(d3), n6 = in.b(d2));
                        if (n9 > 0) {
                            f4 -= (uu.m[n9].a(this.e) + 0.3f) * f3;
                        }
                        if (f4 > 0.0f) {
                            this.g.add(new wf(n8, n7, n6));
                        }
                        d4 += d5 * (double)f3;
                        d3 += d6 * (double)f3;
                        d2 += d7 * (double)f3;
                    }
                }
            }
        }
        this.f *= 2.0f;
        n4 = in.b(this.b - (double)this.f - 1.0);
        n3 = in.b(this.b + (double)this.f + 1.0);
        n2 = in.b(this.c - (double)this.f - 1.0);
        int n10 = in.b(this.c + (double)this.f + 1.0);
        int n11 = in.b(this.d - (double)this.f - 1.0);
        int n12 = in.b(this.d + (double)this.f + 1.0);
        List list = this.i.b(this.e, eq.b(n4, n2, n11, n3, n10, n12));
        bt bt2 = bt.b(this.b, this.c, this.d);
        for (int i2 = 0; i2 < list.size(); ++i2) {
            sn sn2 = (sn)list.get(i2);
            double d9 = sn2.h(this.b, this.c, this.d) / (double)this.f;
            if (!(d9 <= 1.0)) continue;
            d4 = sn2.aM - this.b;
            d3 = sn2.aN - this.c;
            d2 = sn2.aO - this.d;
            double d10 = in.a(d4 * d4 + d3 * d3 + d2 * d2);
            d4 /= d10;
            d3 /= d10;
            d2 /= d10;
            double d11 = this.i.a(bt2, sn2.aW);
            double d12 = (1.0 - d9) * d11;
            sn2.a(this.e, (int)((d12 * d12 + d12) / 2.0 * 8.0 * (double)this.f + 1.0));
            double d13 = d12;
            sn2.aP += d4 * d13;
            sn2.aQ += d3 * d13;
            sn2.aR += d2 * d13;
        }
        this.f = f2;
        ArrayList arrayList = new ArrayList();
        arrayList.addAll(this.g);
        if (this.a) {
            for (int i3 = arrayList.size() - 1; i3 >= 0; --i3) {
                wf wf2 = (wf)arrayList.get(i3);
                int n13 = wf2.a;
                int n14 = wf2.b;
                int n15 = wf2.c;
                int n16 = this.i.a(n13, n14, n15);
                int n17 = this.i.a(n13, n14 - 1, n15);
                if (n16 != 0 || !uu.o[n17] || this.h.nextInt(3) != 0) continue;
                this.i.f(n13, n14, n15, uu.as.bn);
            }
        }
    }

    public void a(boolean bl2) {
        this.i.a(this.b, this.c, this.d, "random.explode", 4.0f, (1.0f + (this.i.r.nextFloat() - this.i.r.nextFloat()) * 0.2f) * 0.7f);
        ArrayList arrayList = new ArrayList();
        arrayList.addAll(this.g);
        for (int i2 = arrayList.size() - 1; i2 >= 0; --i2) {
            wf wf2 = (wf)arrayList.get(i2);
            int n2 = wf2.a;
            int n3 = wf2.b;
            int n4 = wf2.c;
            int n5 = this.i.a(n2, n3, n4);
            if (bl2) {
                double d2 = (float)n2 + this.i.r.nextFloat();
                double d3 = (float)n3 + this.i.r.nextFloat();
                double d4 = (float)n4 + this.i.r.nextFloat();
                double d5 = d2 - this.b;
                double d6 = d3 - this.c;
                double d7 = d4 - this.d;
                double d8 = in.a(d5 * d5 + d6 * d6 + d7 * d7);
                d5 /= d8;
                d6 /= d8;
                d7 /= d8;
                double d9 = 0.5 / (d8 / (double)this.f + 0.1);
                this.i.a("explode", (d2 + this.b * 1.0) / 2.0, (d3 + this.c * 1.0) / 2.0, (d4 + this.d * 1.0) / 2.0, d5 *= (d9 *= (double)(this.i.r.nextFloat() * this.i.r.nextFloat() + 0.3f)), d6 *= d9, d7 *= d9);
                this.i.a("smoke", d2, d3, d4, d5, d6, d7);
            }
            if (n5 <= 0) continue;
            uu.m[n5].a(this.i, n2, n3, n4, this.i.e(n2, n3, n4), 0.3f);
            this.i.f(n2, n3, n4, 0);
            uu.m[n5].d(this.i, n2, n3, n4);
        }
    }
}

