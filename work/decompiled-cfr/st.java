/*
 * Decompiled with CFR 0.152.
 */
public class st {
    public final eb a;
    public int b;
    public int c;
    public int d;
    public int e;
    public int f;
    public int g;

    public st(eb eb2, int n2, int n3, int n4, int n5, int n6, int n7) {
        this.a = eb2;
        this.b = n2;
        this.c = n3;
        this.d = n4;
        this.e = n5;
        this.f = n6;
        this.g = n7;
    }

    public void a(fd fd2) {
        int n2 = this.e - this.b + 1;
        int n3 = this.f - this.c + 1;
        int n4 = this.g - this.d + 1;
        int n5 = n2 * n3 * n4;
        if (n5 > 32768) {
            System.out.println("Light too large, skipping!");
            return;
        }
        int n6 = 0;
        int n7 = 0;
        boolean bl2 = false;
        boolean bl3 = false;
        for (int i2 = this.b; i2 <= this.e; ++i2) {
            for (int i3 = this.d; i3 <= this.g; ++i3) {
                int n8 = i2 >> 4;
                int n9 = i3 >> 4;
                boolean bl4 = false;
                if (bl2 && n8 == n6 && n9 == n7) {
                    bl4 = bl3;
                } else {
                    lm lm2;
                    bl4 = fd2.b(i2, 0, i3, 1);
                    if (bl4 && (lm2 = fd2.c(i2 >> 4, i3 >> 4)).h()) {
                        bl4 = false;
                    }
                    bl3 = bl4;
                    n6 = n8;
                    n7 = n9;
                }
                if (!bl4) continue;
                if (this.c < 0) {
                    this.c = 0;
                }
                if (this.f >= 128) {
                    this.f = 127;
                }
                for (int i4 = this.c; i4 <= this.f; ++i4) {
                    int n10;
                    int n11 = fd2.a(this.a, i2, i4, i3);
                    int n12 = 0;
                    int n13 = fd2.a(i2, i4, i3);
                    int n14 = uu.q[n13];
                    if (n14 == 0) {
                        n14 = 1;
                    }
                    int n15 = 0;
                    if (this.a == eb.a) {
                        if (fd2.o(i2, i4, i3)) {
                            n15 = 15;
                        }
                    } else if (this.a == eb.b) {
                        n15 = uu.s[n13];
                    }
                    if (n14 >= 15 && n15 == 0) {
                        n12 = 0;
                    } else {
                        n10 = fd2.a(this.a, i2 - 1, i4, i3);
                        int n16 = fd2.a(this.a, i2 + 1, i4, i3);
                        int n17 = fd2.a(this.a, i2, i4 - 1, i3);
                        int n18 = fd2.a(this.a, i2, i4 + 1, i3);
                        int n19 = fd2.a(this.a, i2, i4, i3 - 1);
                        int n20 = fd2.a(this.a, i2, i4, i3 + 1);
                        n12 = n10;
                        if (n16 > n12) {
                            n12 = n16;
                        }
                        if (n17 > n12) {
                            n12 = n17;
                        }
                        if (n18 > n12) {
                            n12 = n18;
                        }
                        if (n19 > n12) {
                            n12 = n19;
                        }
                        if (n20 > n12) {
                            n12 = n20;
                        }
                        if ((n12 -= n14) < 0) {
                            n12 = 0;
                        }
                        if (n15 > n12) {
                            n12 = n15;
                        }
                    }
                    if (n11 == n12) continue;
                    fd2.b(this.a, i2, i4, i3, n12);
                    n10 = n12 - 1;
                    if (n10 < 0) {
                        n10 = 0;
                    }
                    fd2.a(this.a, i2 - 1, i4, i3, n10);
                    fd2.a(this.a, i2, i4 - 1, i3, n10);
                    fd2.a(this.a, i2, i4, i3 - 1, n10);
                    if (i2 + 1 >= this.e) {
                        fd2.a(this.a, i2 + 1, i4, i3, n10);
                    }
                    if (i4 + 1 >= this.f) {
                        fd2.a(this.a, i2, i4 + 1, i3, n10);
                    }
                    if (i3 + 1 < this.g) continue;
                    fd2.a(this.a, i2, i4, i3 + 1, n10);
                }
            }
        }
    }

    public boolean a(int n2, int n3, int n4, int n5, int n6, int n7) {
        if (n2 >= this.b && n3 >= this.c && n4 >= this.d && n5 <= this.e && n6 <= this.f && n7 <= this.g) {
            return true;
        }
        int n8 = 1;
        if (n2 >= this.b - n8 && n3 >= this.c - n8 && n4 >= this.d - n8 && n5 <= this.e + n8 && n6 <= this.f + n8 && n7 <= this.g + n8) {
            int n9;
            int n10;
            int n11;
            int n12;
            int n13;
            int n14 = this.e - this.b;
            int n15 = this.f - this.c;
            int n16 = this.g - this.d;
            if (n2 > this.b) {
                n2 = this.b;
            }
            if (n3 > this.c) {
                n3 = this.c;
            }
            if (n4 > this.d) {
                n4 = this.d;
            }
            if (n5 < this.e) {
                n5 = this.e;
            }
            if (n6 < this.f) {
                n6 = this.f;
            }
            if (n7 < this.g) {
                n7 = this.g;
            }
            if ((n13 = (n12 = n5 - n2) * (n11 = n6 - n3) * (n10 = n7 - n4)) - (n9 = n14 * n15 * n16) <= 2) {
                this.b = n2;
                this.c = n3;
                this.d = n4;
                this.e = n5;
                this.f = n6;
                this.g = n7;
                return true;
            }
        }
        return false;
    }
}

