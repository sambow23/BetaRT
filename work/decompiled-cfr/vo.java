/*
 * Decompiled with CFR 0.152.
 */
import java.util.ArrayList;
import java.util.List;

class vo {
    private fd b;
    private int c;
    private int d;
    private int e;
    private final boolean f;
    private List g = new ArrayList();
    final /* synthetic */ pc a;

    public vo(pc pc2, fd fd2, int n2, int n3, int n4) {
        this.a = pc2;
        this.b = fd2;
        this.c = n2;
        this.d = n3;
        this.e = n4;
        int n5 = fd2.a(n2, n3, n4);
        int n6 = fd2.e(n2, n3, n4);
        if (pc.a((pc)uu.m[n5])) {
            this.f = true;
            n6 &= 0xFFFFFFF7;
        } else {
            this.f = false;
        }
        this.a(n6);
    }

    private void a(int n2) {
        this.g.clear();
        if (n2 == 0) {
            this.g.add(new wf(this.c, this.d, this.e - 1));
            this.g.add(new wf(this.c, this.d, this.e + 1));
        } else if (n2 == 1) {
            this.g.add(new wf(this.c - 1, this.d, this.e));
            this.g.add(new wf(this.c + 1, this.d, this.e));
        } else if (n2 == 2) {
            this.g.add(new wf(this.c - 1, this.d, this.e));
            this.g.add(new wf(this.c + 1, this.d + 1, this.e));
        } else if (n2 == 3) {
            this.g.add(new wf(this.c - 1, this.d + 1, this.e));
            this.g.add(new wf(this.c + 1, this.d, this.e));
        } else if (n2 == 4) {
            this.g.add(new wf(this.c, this.d + 1, this.e - 1));
            this.g.add(new wf(this.c, this.d, this.e + 1));
        } else if (n2 == 5) {
            this.g.add(new wf(this.c, this.d, this.e - 1));
            this.g.add(new wf(this.c, this.d + 1, this.e + 1));
        } else if (n2 == 6) {
            this.g.add(new wf(this.c + 1, this.d, this.e));
            this.g.add(new wf(this.c, this.d, this.e + 1));
        } else if (n2 == 7) {
            this.g.add(new wf(this.c - 1, this.d, this.e));
            this.g.add(new wf(this.c, this.d, this.e + 1));
        } else if (n2 == 8) {
            this.g.add(new wf(this.c - 1, this.d, this.e));
            this.g.add(new wf(this.c, this.d, this.e - 1));
        } else if (n2 == 9) {
            this.g.add(new wf(this.c + 1, this.d, this.e));
            this.g.add(new wf(this.c, this.d, this.e - 1));
        }
    }

    private void a() {
        for (int i2 = 0; i2 < this.g.size(); ++i2) {
            vo vo2 = this.a((wf)this.g.get(i2));
            if (vo2 == null || !vo2.b(this)) {
                this.g.remove(i2--);
                continue;
            }
            this.g.set(i2, new wf(vo2.c, vo2.d, vo2.e));
        }
    }

    private boolean a(int n2, int n3, int n4) {
        if (pc.h(this.b, n2, n3, n4)) {
            return true;
        }
        if (pc.h(this.b, n2, n3 + 1, n4)) {
            return true;
        }
        return pc.h(this.b, n2, n3 - 1, n4);
    }

    private vo a(wf wf2) {
        if (pc.h(this.b, wf2.a, wf2.b, wf2.c)) {
            return new vo(this.a, this.b, wf2.a, wf2.b, wf2.c);
        }
        if (pc.h(this.b, wf2.a, wf2.b + 1, wf2.c)) {
            return new vo(this.a, this.b, wf2.a, wf2.b + 1, wf2.c);
        }
        if (pc.h(this.b, wf2.a, wf2.b - 1, wf2.c)) {
            return new vo(this.a, this.b, wf2.a, wf2.b - 1, wf2.c);
        }
        return null;
    }

    private boolean b(vo vo2) {
        for (int i2 = 0; i2 < this.g.size(); ++i2) {
            wf wf2 = (wf)this.g.get(i2);
            if (wf2.a != vo2.c || wf2.c != vo2.e) continue;
            return true;
        }
        return false;
    }

    private boolean b(int n2, int n3, int n4) {
        for (int i2 = 0; i2 < this.g.size(); ++i2) {
            wf wf2 = (wf)this.g.get(i2);
            if (wf2.a != n2 || wf2.c != n4) continue;
            return true;
        }
        return false;
    }

    private int b() {
        int n2 = 0;
        if (this.a(this.c, this.d, this.e - 1)) {
            ++n2;
        }
        if (this.a(this.c, this.d, this.e + 1)) {
            ++n2;
        }
        if (this.a(this.c - 1, this.d, this.e)) {
            ++n2;
        }
        if (this.a(this.c + 1, this.d, this.e)) {
            ++n2;
        }
        return n2;
    }

    private boolean c(vo vo2) {
        if (this.b(vo2)) {
            return true;
        }
        if (this.g.size() == 2) {
            return false;
        }
        if (this.g.size() == 0) {
            return true;
        }
        wf wf2 = (wf)this.g.get(0);
        if (vo2.d == this.d && wf2.b == this.d) {
            return true;
        }
        return true;
    }

    private void d(vo vo2) {
        this.g.add(new wf(vo2.c, vo2.d, vo2.e));
        boolean bl2 = this.b(this.c, this.d, this.e - 1);
        boolean bl3 = this.b(this.c, this.d, this.e + 1);
        boolean bl4 = this.b(this.c - 1, this.d, this.e);
        boolean bl5 = this.b(this.c + 1, this.d, this.e);
        int n2 = -1;
        if (bl2 || bl3) {
            n2 = 0;
        }
        if (bl4 || bl5) {
            n2 = 1;
        }
        if (!this.f) {
            if (bl3 && bl5 && !bl2 && !bl4) {
                n2 = 6;
            }
            if (bl3 && bl4 && !bl2 && !bl5) {
                n2 = 7;
            }
            if (bl2 && bl4 && !bl3 && !bl5) {
                n2 = 8;
            }
            if (bl2 && bl5 && !bl3 && !bl4) {
                n2 = 9;
            }
        }
        if (n2 == 0) {
            if (pc.h(this.b, this.c, this.d + 1, this.e - 1)) {
                n2 = 4;
            }
            if (pc.h(this.b, this.c, this.d + 1, this.e + 1)) {
                n2 = 5;
            }
        }
        if (n2 == 1) {
            if (pc.h(this.b, this.c + 1, this.d + 1, this.e)) {
                n2 = 2;
            }
            if (pc.h(this.b, this.c - 1, this.d + 1, this.e)) {
                n2 = 3;
            }
        }
        if (n2 < 0) {
            n2 = 0;
        }
        int n3 = n2;
        if (this.f) {
            n3 = this.b.e(this.c, this.d, this.e) & 8 | n2;
        }
        this.b.d(this.c, this.d, this.e, n3);
    }

    private boolean c(int n2, int n3, int n4) {
        vo vo2 = this.a(new wf(n2, n3, n4));
        if (vo2 == null) {
            return false;
        }
        vo2.a();
        return vo2.c(this);
    }

    public void a(boolean bl2, boolean bl3) {
        boolean bl4 = this.c(this.c, this.d, this.e - 1);
        boolean bl5 = this.c(this.c, this.d, this.e + 1);
        boolean bl6 = this.c(this.c - 1, this.d, this.e);
        boolean bl7 = this.c(this.c + 1, this.d, this.e);
        int n2 = -1;
        if ((bl4 || bl5) && !bl6 && !bl7) {
            n2 = 0;
        }
        if ((bl6 || bl7) && !bl4 && !bl5) {
            n2 = 1;
        }
        if (!this.f) {
            if (bl5 && bl7 && !bl4 && !bl6) {
                n2 = 6;
            }
            if (bl5 && bl6 && !bl4 && !bl7) {
                n2 = 7;
            }
            if (bl4 && bl6 && !bl5 && !bl7) {
                n2 = 8;
            }
            if (bl4 && bl7 && !bl5 && !bl6) {
                n2 = 9;
            }
        }
        if (n2 == -1) {
            if (bl4 || bl5) {
                n2 = 0;
            }
            if (bl6 || bl7) {
                n2 = 1;
            }
            if (!this.f) {
                if (bl2) {
                    if (bl5 && bl7) {
                        n2 = 6;
                    }
                    if (bl6 && bl5) {
                        n2 = 7;
                    }
                    if (bl7 && bl4) {
                        n2 = 9;
                    }
                    if (bl4 && bl6) {
                        n2 = 8;
                    }
                } else {
                    if (bl4 && bl6) {
                        n2 = 8;
                    }
                    if (bl7 && bl4) {
                        n2 = 9;
                    }
                    if (bl6 && bl5) {
                        n2 = 7;
                    }
                    if (bl5 && bl7) {
                        n2 = 6;
                    }
                }
            }
        }
        if (n2 == 0) {
            if (pc.h(this.b, this.c, this.d + 1, this.e - 1)) {
                n2 = 4;
            }
            if (pc.h(this.b, this.c, this.d + 1, this.e + 1)) {
                n2 = 5;
            }
        }
        if (n2 == 1) {
            if (pc.h(this.b, this.c + 1, this.d + 1, this.e)) {
                n2 = 2;
            }
            if (pc.h(this.b, this.c - 1, this.d + 1, this.e)) {
                n2 = 3;
            }
        }
        if (n2 < 0) {
            n2 = 0;
        }
        this.a(n2);
        int n3 = n2;
        if (this.f) {
            n3 = this.b.e(this.c, this.d, this.e) & 8 | n2;
        }
        if (bl3 || this.b.e(this.c, this.d, this.e) != n3) {
            this.b.d(this.c, this.d, this.e, n3);
            for (int i2 = 0; i2 < this.g.size(); ++i2) {
                vo vo2 = this.a((wf)this.g.get(i2));
                if (vo2 == null) continue;
                vo2.a();
                if (!vo2.c(this)) continue;
                vo2.d(this);
            }
        }
    }

    static /* synthetic */ int a(vo vo2) {
        return vo2.b();
    }
}

