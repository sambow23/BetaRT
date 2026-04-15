/*
 * Decompiled with CFR 0.152.
 */
public class fw {
    private xp a;
    private lf b = new lf();
    private jx c = new jx();
    private d[] d = new d[32];

    public fw(xp xp2) {
        this.a = xp2;
    }

    public dh a(sn sn2, sn sn3, float f2) {
        return this.a(sn2, sn3.aM, sn3.aW.b, sn3.aO, f2);
    }

    public dh a(sn sn2, int n2, int n3, int n4, float f2) {
        return this.a(sn2, (float)n2 + 0.5f, (float)n3 + 0.5f, (float)n4 + 0.5f, f2);
    }

    private dh a(sn sn2, double d2, double d3, double d4, float f2) {
        this.b.a();
        this.c.a();
        d d5 = this.a(in.b(sn2.aW.a), in.b(sn2.aW.b), in.b(sn2.aW.c));
        d d6 = this.a(in.b(d2 - (double)(sn2.bg / 2.0f)), in.b(d3), in.b(d4 - (double)(sn2.bg / 2.0f)));
        d d7 = new d(in.d(sn2.bg + 1.0f), in.d(sn2.bh + 1.0f), in.d(sn2.bg + 1.0f));
        dh dh2 = this.a(sn2, d5, d6, d7, f2);
        return dh2;
    }

    private dh a(sn sn2, d d2, d d3, d d4, float f2) {
        d2.e = 0.0f;
        d2.g = d2.f = d2.a(d3);
        this.b.a();
        this.b.a(d2);
        d d5 = d2;
        while (!this.b.c()) {
            d d6 = this.b.b();
            if (d6.equals(d3)) {
                return this.a(d2, d3);
            }
            if (d6.a(d3) < d5.a(d3)) {
                d5 = d6;
            }
            d6.i = true;
            int n2 = this.b(sn2, d6, d4, d3, f2);
            for (int i2 = 0; i2 < n2; ++i2) {
                d d7 = this.d[i2];
                float f3 = d6.e + d6.a(d7);
                if (d7.a() && !(f3 < d7.e)) continue;
                d7.h = d6;
                d7.e = f3;
                d7.f = d7.a(d3);
                if (d7.a()) {
                    this.b.a(d7, d7.e + d7.f);
                    continue;
                }
                d7.g = d7.e + d7.f;
                this.b.a(d7);
            }
        }
        if (d5 == d2) {
            return null;
        }
        return this.a(d2, d5);
    }

    private int b(sn sn2, d d2, d d3, d d4, float f2) {
        int n2 = 0;
        int n3 = 0;
        if (this.a(sn2, d2.a, d2.b + 1, d2.c, d3) == 1) {
            n3 = 1;
        }
        d d5 = this.a(sn2, d2.a, d2.b, d2.c + 1, d3, n3);
        d d6 = this.a(sn2, d2.a - 1, d2.b, d2.c, d3, n3);
        d d7 = this.a(sn2, d2.a + 1, d2.b, d2.c, d3, n3);
        d d8 = this.a(sn2, d2.a, d2.b, d2.c - 1, d3, n3);
        if (d5 != null && !d5.i && d5.a(d4) < f2) {
            this.d[n2++] = d5;
        }
        if (d6 != null && !d6.i && d6.a(d4) < f2) {
            this.d[n2++] = d6;
        }
        if (d7 != null && !d7.i && d7.a(d4) < f2) {
            this.d[n2++] = d7;
        }
        if (d8 != null && !d8.i && d8.a(d4) < f2) {
            this.d[n2++] = d8;
        }
        return n2;
    }

    private d a(sn sn2, int n2, int n3, int n4, d d2, int n5) {
        d d3 = null;
        if (this.a(sn2, n2, n3, n4, d2) == 1) {
            d3 = this.a(n2, n3, n4);
        }
        if (d3 == null && n5 > 0 && this.a(sn2, n2, n3 + n5, n4, d2) == 1) {
            d3 = this.a(n2, n3 + n5, n4);
            n3 += n5;
        }
        if (d3 != null) {
            int n6 = 0;
            int n7 = 0;
            while (n3 > 0 && (n7 = this.a(sn2, n2, n3 - 1, n4, d2)) == 1) {
                if (++n6 >= 4) {
                    return null;
                }
                if (--n3 <= 0) continue;
                d3 = this.a(n2, n3, n4);
            }
            if (n7 == -2) {
                return null;
            }
        }
        return d3;
    }

    private final d a(int n2, int n3, int n4) {
        int n5 = d.a(n2, n3, n4);
        d d2 = (d)this.c.a(n5);
        if (d2 == null) {
            d2 = new d(n2, n3, n4);
            this.c.a(n5, d2);
        }
        return d2;
    }

    private int a(sn sn2, int n2, int n3, int n4, d d2) {
        for (int i2 = n2; i2 < n2 + d2.a; ++i2) {
            for (int i3 = n3; i3 < n3 + d2.b; ++i3) {
                for (int i4 = n4; i4 < n4 + d2.c; ++i4) {
                    int n5 = this.a.a(i2, i3, i4);
                    if (n5 <= 0) continue;
                    if (n5 == uu.aM.bn || n5 == uu.aF.bn) {
                        int n6 = this.a.e(i2, i3, i4);
                        if (le.f(n6)) continue;
                        return 0;
                    }
                    ln ln2 = uu.m[n5].bA;
                    if (ln2.c()) {
                        return 0;
                    }
                    if (ln2 == ln.g) {
                        return -1;
                    }
                    if (ln2 != ln.h) continue;
                    return -2;
                }
            }
        }
        return 1;
    }

    private dh a(d d2, d d3) {
        int n2 = 1;
        d d4 = d3;
        while (d4.h != null) {
            ++n2;
            d4 = d4.h;
        }
        d[] dArray = new d[n2];
        d4 = d3;
        dArray[--n2] = d4;
        while (d4.h != null) {
            d4 = d4.h;
            dArray[--n2] = d4;
        }
        return new dh(dArray);
    }
}

