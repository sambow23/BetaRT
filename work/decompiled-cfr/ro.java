/*
 * Decompiled with CFR 0.152.
 */
public class ro
extends ub {
    private final sj c;
    private final int d;
    private final int e;
    private final int f;
    private final int g;
    private String h;
    private int i;
    private int j;
    public boolean a = false;
    public boolean b = true;
    private da l;

    public ro(da da2, sj sj2, int n2, int n3, int n4, int n5, String string) {
        this.l = da2;
        this.c = sj2;
        this.d = n2;
        this.e = n3;
        this.f = n4;
        this.g = n5;
        this.a(string);
    }

    public void a(String string) {
        this.h = string;
    }

    public String a() {
        return this.h;
    }

    public void b() {
        ++this.j;
    }

    public void a(char c2, int n2) {
        if (!this.b || !this.a) {
            return;
        }
        if (c2 == '\t') {
            this.l.j();
        }
        if (c2 == '\u0016') {
            int n3;
            String string = da.d();
            if (string == null) {
                string = "";
            }
            if ((n3 = 32 - this.h.length()) > string.length()) {
                n3 = string.length();
            }
            if (n3 > 0) {
                this.h = this.h + string.substring(0, n3);
            }
        }
        if (n2 == 14 && this.h.length() > 0) {
            this.h = this.h.substring(0, this.h.length() - 1);
        }
        if (fp.a.indexOf(c2) >= 0 && (this.h.length() < this.i || this.i == 0)) {
            this.h = this.h + c2;
        }
    }

    public void a(int n2, int n3, int n4) {
        boolean bl2 = this.b && n2 >= this.d && n2 < this.d + this.f && n3 >= this.e && n3 < this.e + this.g;
        this.a(bl2);
    }

    public void a(boolean bl2) {
        if (bl2 && !this.a) {
            this.j = 0;
        }
        this.a = bl2;
    }

    public void c() {
        this.a(this.d - 1, this.e - 1, this.d + this.f + 1, this.e + this.g + 1, -6250336);
        this.a(this.d, this.e, this.d + this.f, this.e + this.g, -16777216);
        if (this.b) {
            boolean bl2 = this.a && this.j / 6 % 2 == 0;
            this.b(this.c, this.h + (bl2 ? "_" : ""), this.d + 4, this.e + (this.g - 8) / 2, 0xE0E0E0);
        } else {
            this.b(this.c, this.h, this.d + 4, this.e + (this.g - 8) / 2, 0x707070);
        }
    }

    public void a(int n2) {
        this.i = n2;
    }
}

