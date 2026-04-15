/*
 * Decompiled with CFR 0.152.
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class lm {
    public static boolean a;
    public byte[] b;
    public boolean c;
    public fd d;
    public wi e;
    public wi f;
    public wi g;
    public byte[] h;
    public int i;
    public final int j;
    public final int k;
    public Map l = new HashMap();
    public List[] m = new List[8];
    public boolean n = false;
    public boolean o = false;
    public boolean p;
    public boolean q = false;
    public long r = 0L;

    public lm(fd fd2, int n2, int n3) {
        this.d = fd2;
        this.j = n2;
        this.k = n3;
        this.h = new byte[256];
        for (int i2 = 0; i2 < this.m.length; ++i2) {
            this.m[i2] = new ArrayList();
        }
    }

    public lm(fd fd2, byte[] byArray, int n2, int n3) {
        this(fd2, n2, n3);
        this.b = byArray;
        this.e = new wi(byArray.length);
        this.f = new wi(byArray.length);
        this.g = new wi(byArray.length);
    }

    public boolean a(int n2, int n3) {
        return n2 == this.j && n3 == this.k;
    }

    public int b(int n2, int n3) {
        return this.h[n3 << 4 | n2] & 0xFF;
    }

    public void a() {
    }

    public void b() {
        int n2 = 127;
        for (int i2 = 0; i2 < 16; ++i2) {
            for (int i3 = 0; i3 < 16; ++i3) {
                int n3;
                int n4 = i2 << 11 | i3 << 7;
                for (n3 = 127; n3 > 0 && uu.q[this.b[n4 + n3 - 1] & 0xFF] == 0; --n3) {
                }
                this.h[i3 << 4 | i2] = (byte)n3;
                if (n3 >= n2) continue;
                n2 = n3;
            }
        }
        this.i = n2;
        this.o = true;
    }

    public void c() {
        int n2;
        int n3;
        int n4 = 127;
        for (n3 = 0; n3 < 16; ++n3) {
            for (n2 = 0; n2 < 16; ++n2) {
                int n5;
                int n6 = n3 << 11 | n2 << 7;
                for (n5 = 127; n5 > 0 && uu.q[this.b[n6 + n5 - 1] & 0xFF] == 0; --n5) {
                }
                this.h[n2 << 4 | n3] = (byte)n5;
                if (n5 < n4) {
                    n4 = n5;
                }
                if (this.d.t.e) continue;
                int n7 = 15;
                int n8 = 127;
                do {
                    if ((n7 -= uu.q[this.b[n6 + n8] & 0xFF]) <= 0) continue;
                    this.f.a(n3, n8, n2, n7);
                } while (--n8 > 0 && n7 > 0);
            }
        }
        this.i = n4;
        for (n3 = 0; n3 < 16; ++n3) {
            for (n2 = 0; n2 < 16; ++n2) {
                this.c(n3, n2);
            }
        }
        this.o = true;
    }

    public void d() {
    }

    private void c(int n2, int n3) {
        int n4 = this.b(n2, n3);
        int n5 = this.j * 16 + n2;
        int n6 = this.k * 16 + n3;
        this.f(n5 - 1, n6, n4);
        this.f(n5 + 1, n6, n4);
        this.f(n5, n6 - 1, n4);
        this.f(n5, n6 + 1, n4);
    }

    private void f(int n2, int n3, int n4) {
        int n5 = this.d.d(n2, n3);
        if (n5 > n4) {
            this.d.a(eb.a, n2, n4, n3, n2, n5, n3);
            this.o = true;
        } else if (n5 < n4) {
            this.d.a(eb.a, n2, n5, n3, n2, n4, n3);
            this.o = true;
        }
    }

    private void g(int n2, int n3, int n4) {
        int n5;
        int n6;
        int n7;
        int n8;
        int n9 = n8 = this.h[n4 << 4 | n2] & 0xFF;
        if (n3 > n8) {
            n9 = n3;
        }
        int n10 = n2 << 11 | n4 << 7;
        while (n9 > 0 && uu.q[this.b[n10 + n9 - 1] & 0xFF] == 0) {
            --n9;
        }
        if (n9 == n8) {
            return;
        }
        this.d.h(n2, n4, n9, n8);
        this.h[n4 << 4 | n2] = (byte)n9;
        if (n9 < this.i) {
            this.i = n9;
        } else {
            n7 = 127;
            for (n6 = 0; n6 < 16; ++n6) {
                for (n5 = 0; n5 < 16; ++n5) {
                    if ((this.h[n5 << 4 | n6] & 0xFF) >= n7) continue;
                    n7 = this.h[n5 << 4 | n6] & 0xFF;
                }
            }
            this.i = n7;
        }
        n7 = this.j * 16 + n2;
        n6 = this.k * 16 + n4;
        if (n9 < n8) {
            for (n5 = n9; n5 < n8; ++n5) {
                this.f.a(n2, n5, n4, 15);
            }
        } else {
            this.d.a(eb.a, n7, n8, n6, n7, n9, n6);
            for (n5 = n8; n5 < n9; ++n5) {
                this.f.a(n2, n5, n4, 0);
            }
        }
        n5 = 15;
        int n11 = n9;
        while (n9 > 0 && n5 > 0) {
            int n12;
            if ((n12 = uu.q[this.a(n2, --n9, n4)]) == 0) {
                n12 = 1;
            }
            if ((n5 -= n12) < 0) {
                n5 = 0;
            }
            this.f.a(n2, n9, n4, n5);
        }
        while (n9 > 0 && uu.q[this.a(n2, n9 - 1, n4)] == 0) {
            --n9;
        }
        if (n9 != n11) {
            this.d.a(eb.a, n7 - 1, n9, n6 - 1, n7 + 1, n11, n6 + 1);
        }
        this.o = true;
    }

    public int a(int n2, int n3, int n4) {
        return this.b[n2 << 11 | n4 << 7 | n3] & 0xFF;
    }

    public boolean a(int n2, int n3, int n4, int n5, int n6) {
        byte by2 = (byte)n5;
        int n7 = this.h[n4 << 4 | n2] & 0xFF;
        int n8 = this.b[n2 << 11 | n4 << 7 | n3] & 0xFF;
        if (n8 == n5 && this.e.a(n2, n3, n4) == n6) {
            return false;
        }
        int n9 = this.j * 16 + n2;
        int n10 = this.k * 16 + n4;
        this.b[n2 << 11 | n4 << 7 | n3] = (byte)(by2 & 0xFF);
        if (n8 != 0 && !this.d.B) {
            uu.m[n8].b(this.d, n9, n3, n10);
        }
        this.e.a(n2, n3, n4, n6);
        if (!this.d.t.e) {
            if (uu.q[by2 & 0xFF] != 0) {
                if (n3 >= n7) {
                    this.g(n2, n3 + 1, n4);
                }
            } else if (n3 == n7 - 1) {
                this.g(n2, n3, n4);
            }
            this.d.a(eb.a, n9, n3, n10, n9, n3, n10);
        }
        this.d.a(eb.b, n9, n3, n10, n9, n3, n10);
        this.c(n2, n4);
        this.e.a(n2, n3, n4, n6);
        if (n5 != 0) {
            uu.m[n5].c(this.d, n9, n3, n10);
        }
        this.o = true;
        return true;
    }

    public boolean a(int n2, int n3, int n4, int n5) {
        byte by2 = (byte)n5;
        int n6 = this.h[n4 << 4 | n2] & 0xFF;
        int n7 = this.b[n2 << 11 | n4 << 7 | n3] & 0xFF;
        if (n7 == n5) {
            return false;
        }
        int n8 = this.j * 16 + n2;
        int n9 = this.k * 16 + n4;
        this.b[n2 << 11 | n4 << 7 | n3] = (byte)(by2 & 0xFF);
        if (n7 != 0) {
            uu.m[n7].b(this.d, n8, n3, n9);
        }
        this.e.a(n2, n3, n4, 0);
        if (uu.q[by2 & 0xFF] != 0) {
            if (n3 >= n6) {
                this.g(n2, n3 + 1, n4);
            }
        } else if (n3 == n6 - 1) {
            this.g(n2, n3, n4);
        }
        this.d.a(eb.a, n8, n3, n9, n8, n3, n9);
        this.d.a(eb.b, n8, n3, n9, n8, n3, n9);
        this.c(n2, n4);
        if (n5 != 0 && !this.d.B) {
            uu.m[n5].c(this.d, n8, n3, n9);
        }
        this.o = true;
        return true;
    }

    public int b(int n2, int n3, int n4) {
        return this.e.a(n2, n3, n4);
    }

    public void b(int n2, int n3, int n4, int n5) {
        this.o = true;
        this.e.a(n2, n3, n4, n5);
    }

    public int a(eb eb2, int n2, int n3, int n4) {
        if (eb2 == eb.a) {
            return this.f.a(n2, n3, n4);
        }
        if (eb2 == eb.b) {
            return this.g.a(n2, n3, n4);
        }
        return 0;
    }

    public void a(eb eb2, int n2, int n3, int n4, int n5) {
        this.o = true;
        if (eb2 == eb.a) {
            this.f.a(n2, n3, n4, n5);
        } else if (eb2 == eb.b) {
            this.g.a(n2, n3, n4, n5);
        } else {
            return;
        }
    }

    public int c(int n2, int n3, int n4, int n5) {
        int n6;
        int n7 = this.f.a(n2, n3, n4);
        if (n7 > 0) {
            a = true;
        }
        if ((n6 = this.g.a(n2, n3, n4)) > (n7 -= n5)) {
            n7 = n6;
        }
        return n7;
    }

    public void a(sn sn2) {
        int n2;
        this.q = true;
        int n3 = in.b(sn2.aM / 16.0);
        int n4 = in.b(sn2.aO / 16.0);
        if (n3 != this.j || n4 != this.k) {
            System.out.println("Wrong location! " + sn2);
            Thread.dumpStack();
        }
        if ((n2 = in.b(sn2.aN / 16.0)) < 0) {
            n2 = 0;
        }
        if (n2 >= this.m.length) {
            n2 = this.m.length - 1;
        }
        sn2.bF = true;
        sn2.bG = this.j;
        sn2.bH = n2;
        sn2.bI = this.k;
        this.m[n2].add(sn2);
    }

    public void b(sn sn2) {
        this.a(sn2, sn2.bH);
    }

    public void a(sn sn2, int n2) {
        if (n2 < 0) {
            n2 = 0;
        }
        if (n2 >= this.m.length) {
            n2 = this.m.length - 1;
        }
        this.m[n2].remove(sn2);
    }

    public boolean c(int n2, int n3, int n4) {
        return n3 >= (this.h[n4 << 4 | n2] & 0xFF);
    }

    public ow d(int n2, int n3, int n4) {
        wf wf2 = new wf(n2, n3, n4);
        ow ow2 = (ow)this.l.get(wf2);
        if (ow2 == null) {
            int n5 = this.a(n2, n3, n4);
            if (!uu.p[n5]) {
                return null;
            }
            rw rw2 = (rw)uu.m[n5];
            rw2.c(this.d, this.j * 16 + n2, n3, this.k * 16 + n4);
            ow2 = (ow)this.l.get(wf2);
        }
        if (ow2 != null && ow2.g()) {
            this.l.remove(wf2);
            return null;
        }
        return ow2;
    }

    public void a(ow ow2) {
        int n2 = ow2.e - this.j * 16;
        int n3 = ow2.f;
        int n4 = ow2.g - this.k * 16;
        this.a(n2, n3, n4, ow2);
        if (this.c) {
            this.d.c.add(ow2);
        }
    }

    public void a(int n2, int n3, int n4, ow ow2) {
        wf wf2 = new wf(n2, n3, n4);
        ow2.d = this.d;
        ow2.e = this.j * 16 + n2;
        ow2.f = n3;
        ow2.g = this.k * 16 + n4;
        if (this.a(n2, n3, n4) == 0 || !(uu.m[this.a(n2, n3, n4)] instanceof rw)) {
            System.out.println("Attempted to place a tile entity where there was no entity tile!");
            return;
        }
        ow2.j();
        this.l.put(wf2, ow2);
    }

    public void e(int n2, int n3, int n4) {
        ow ow2;
        wf wf2 = new wf(n2, n3, n4);
        if (this.c && (ow2 = (ow)this.l.remove(wf2)) != null) {
            ow2.i();
        }
    }

    public void e() {
        this.c = true;
        this.d.a(this.l.values());
        for (int i2 = 0; i2 < this.m.length; ++i2) {
            this.d.a(this.m[i2]);
        }
    }

    public void f() {
        this.c = false;
        for (ow ow2 : this.l.values()) {
            ow2.i();
        }
        for (int i2 = 0; i2 < this.m.length; ++i2) {
            this.d.b(this.m[i2]);
        }
    }

    public void g() {
        this.o = true;
    }

    public void a(sn sn2, eq eq2, List list) {
        int n2 = in.b((eq2.b - 2.0) / 16.0);
        int n3 = in.b((eq2.e + 2.0) / 16.0);
        if (n2 < 0) {
            n2 = 0;
        }
        if (n3 >= this.m.length) {
            n3 = this.m.length - 1;
        }
        for (int i2 = n2; i2 <= n3; ++i2) {
            List list2 = this.m[i2];
            for (int i3 = 0; i3 < list2.size(); ++i3) {
                sn sn3 = (sn)list2.get(i3);
                if (sn3 == sn2 || !sn3.aW.a(eq2)) continue;
                list.add(sn3);
            }
        }
    }

    public void a(Class clazz, eq eq2, List list) {
        int n2 = in.b((eq2.b - 2.0) / 16.0);
        int n3 = in.b((eq2.e + 2.0) / 16.0);
        if (n2 < 0) {
            n2 = 0;
        }
        if (n3 >= this.m.length) {
            n3 = this.m.length - 1;
        }
        for (int i2 = n2; i2 <= n3; ++i2) {
            List list2 = this.m[i2];
            for (int i3 = 0; i3 < list2.size(); ++i3) {
                sn sn2 = (sn)list2.get(i3);
                if (!clazz.isAssignableFrom(sn2.getClass()) || !sn2.aW.a(eq2)) continue;
                list.add(sn2);
            }
        }
    }

    public boolean a(boolean bl2) {
        if (this.p) {
            return false;
        }
        if (bl2 ? this.q && this.d.t() != this.r : this.q && this.d.t() >= this.r + 600L) {
            return true;
        }
        return this.o;
    }

    public int a(byte[] byArray, int n2, int n3, int n4, int n5, int n6, int n7, int n8) {
        int n9;
        int n10;
        int n11;
        int n12;
        for (n12 = n2; n12 < n5; ++n12) {
            for (n11 = n4; n11 < n7; ++n11) {
                n10 = n12 << 11 | n11 << 7 | n3;
                n9 = n6 - n3;
                System.arraycopy(byArray, n8, this.b, n10, n9);
                n8 += n9;
            }
        }
        this.b();
        for (n12 = n2; n12 < n5; ++n12) {
            for (n11 = n4; n11 < n7; ++n11) {
                n10 = (n12 << 11 | n11 << 7 | n3) >> 1;
                n9 = (n6 - n3) / 2;
                System.arraycopy(byArray, n8, this.e.a, n10, n9);
                n8 += n9;
            }
        }
        for (n12 = n2; n12 < n5; ++n12) {
            for (n11 = n4; n11 < n7; ++n11) {
                n10 = (n12 << 11 | n11 << 7 | n3) >> 1;
                n9 = (n6 - n3) / 2;
                System.arraycopy(byArray, n8, this.g.a, n10, n9);
                n8 += n9;
            }
        }
        for (n12 = n2; n12 < n5; ++n12) {
            for (n11 = n4; n11 < n7; ++n11) {
                n10 = (n12 << 11 | n11 << 7 | n3) >> 1;
                n9 = (n6 - n3) / 2;
                System.arraycopy(byArray, n8, this.f.a, n10, n9);
                n8 += n9;
            }
        }
        return n8;
    }

    public Random a(long l2) {
        return new Random(this.d.s() + (long)(this.j * this.j * 4987142) + (long)(this.j * 5947611) + (long)(this.k * this.k) * 4392871L + (long)(this.k * 389711) ^ l2);
    }

    public boolean h() {
        return false;
    }

    public void i() {
        vi.a(this.b);
    }
}

