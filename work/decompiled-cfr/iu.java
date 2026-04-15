/*
 * Decompiled with CFR 0.152.
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class iu
extends hm {
    public int b;
    public int c;
    public byte d;
    public byte e;
    public byte[] f = new byte[16384];
    public int g;
    public List h = new ArrayList();
    private Map j = new HashMap();
    public List i = new ArrayList();

    public iu(String string) {
        super(string);
    }

    public void a(nu nu2) {
        this.d = nu2.c("dimension");
        this.b = nu2.e("xCenter");
        this.c = nu2.e("zCenter");
        this.e = nu2.c("scale");
        if (this.e < 0) {
            this.e = 0;
        }
        if (this.e > 4) {
            this.e = (byte)4;
        }
        int n2 = nu2.d("width");
        int n3 = nu2.d("height");
        if (n2 == 128 && n3 == 128) {
            this.f = nu2.j("colors");
        } else {
            byte[] byArray = nu2.j("colors");
            this.f = new byte[16384];
            int n4 = (128 - n2) / 2;
            int n5 = (128 - n3) / 2;
            for (int i2 = 0; i2 < n3; ++i2) {
                int n6 = i2 + n5;
                if (n6 < 0 && n6 >= 128) continue;
                for (int i3 = 0; i3 < n2; ++i3) {
                    int n7 = i3 + n4;
                    if (n7 < 0 && n7 >= 128) continue;
                    this.f[n7 + n6 * 128] = byArray[i3 + i2 * n2];
                }
            }
        }
    }

    public void b(nu nu2) {
        nu2.a("dimension", this.d);
        nu2.a("xCenter", this.b);
        nu2.a("zCenter", this.c);
        nu2.a("scale", this.e);
        nu2.a("width", (short)128);
        nu2.a("height", (short)128);
        nu2.a("colors", this.f);
    }

    public void a(gs gs2, iz iz2) {
        if (!this.j.containsKey(gs2)) {
            si si2 = new si(this, gs2);
            this.j.put(gs2, si2);
            this.h.add(si2);
        }
        this.i.clear();
        for (int i2 = 0; i2 < this.h.size(); ++i2) {
            si si3 = (si)this.h.get(i2);
            if (si3.a.be || !si3.a.c.c(iz2)) {
                this.j.remove(si3.a);
                this.h.remove(si3);
                continue;
            }
            float f2 = (float)(si3.a.aM - (double)this.b) / (float)(1 << this.e);
            float f3 = (float)(si3.a.aO - (double)this.c) / (float)(1 << this.e);
            int n2 = 64;
            int n3 = 64;
            if (!(f2 >= (float)(-n2)) || !(f3 >= (float)(-n3)) || !(f2 <= (float)n2) || !(f3 <= (float)n3)) continue;
            byte by2 = 0;
            byte by3 = (byte)((double)(f2 * 2.0f) + 0.5);
            byte by4 = (byte)((double)(f3 * 2.0f) + 0.5);
            byte by5 = (byte)((double)(gs2.aS * 16.0f / 360.0f) + 0.5);
            if (this.d < 0) {
                int n4 = this.g / 10;
                by5 = (byte)(n4 * n4 * 34187121 + n4 * 121 >> 15 & 0xF);
            }
            if (si3.a.m != this.d) continue;
            this.i.add(new ax(this, by2, by3, by4, by5));
        }
    }

    public void a(int n2, int n3, int n4) {
        super.a();
        for (int i2 = 0; i2 < this.h.size(); ++i2) {
            si si2 = (si)this.h.get(i2);
            if (si2.b[n2] < 0 || si2.b[n2] > n3) {
                si2.b[n2] = n3;
            }
            if (si2.c[n2] >= 0 && si2.c[n2] >= n4) continue;
            si2.c[n2] = n4;
        }
    }

    public void a(byte[] byArray) {
        if (byArray[0] == 0) {
            int n2 = byArray[1] & 0xFF;
            int n3 = byArray[2] & 0xFF;
            for (int i2 = 0; i2 < byArray.length - 3; ++i2) {
                this.f[(i2 + n3) * 128 + n2] = byArray[i2 + 3];
            }
            this.a();
        } else if (byArray[0] == 1) {
            this.i.clear();
            for (int i3 = 0; i3 < (byArray.length - 1) / 3; ++i3) {
                byte by2 = (byte)(byArray[i3 * 3 + 1] % 16);
                byte by3 = byArray[i3 * 3 + 2];
                byte by4 = byArray[i3 * 3 + 3];
                byte by5 = (byte)(byArray[i3 * 3 + 1] / 16);
                this.i.add(new ax(this, by2, by3, by4, by5));
            }
        }
    }
}

