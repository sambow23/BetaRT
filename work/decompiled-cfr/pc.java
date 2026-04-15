/*
 * Decompiled with CFR 0.152.
 */
import java.util.Random;

public class pc
extends uu {
    private final boolean a;

    public static final boolean h(fd fd2, int n2, int n3, int n4) {
        int n5 = fd2.a(n2, n3, n4);
        return n5 == uu.aH.bn || n5 == uu.U.bn || n5 == uu.V.bn;
    }

    public static final boolean d(int n2) {
        return n2 == uu.aH.bn || n2 == uu.U.bn || n2 == uu.V.bn;
    }

    protected pc(int n2, int n3, boolean bl2) {
        super(n2, n3, ln.o);
        this.a = bl2;
        this.a(0.0f, 0.0f, 0.0f, 1.0f, 0.125f, 1.0f);
    }

    public boolean i() {
        return this.a;
    }

    public eq e(fd fd2, int n2, int n3, int n4) {
        return null;
    }

    public boolean c() {
        return false;
    }

    public vf a(fd fd2, int n2, int n3, int n4, bt bt2, bt bt3) {
        this.a((xp)fd2, n2, n3, n4);
        return super.a(fd2, n2, n3, n4, bt2, bt3);
    }

    public void a(xp xp2, int n2, int n3, int n4) {
        int n5 = xp2.e(n2, n3, n4);
        if (n5 >= 2 && n5 <= 5) {
            this.a(0.0f, 0.0f, 0.0f, 1.0f, 0.625f, 1.0f);
        } else {
            this.a(0.0f, 0.0f, 0.0f, 1.0f, 0.125f, 1.0f);
        }
    }

    public int a(int n2, int n3) {
        if (this.a ? this.bn == uu.U.bn && (n3 & 8) == 0 : n3 >= 6) {
            return this.bm - 16;
        }
        return this.bm;
    }

    public boolean d() {
        return false;
    }

    public int b() {
        return 9;
    }

    public int a(Random random) {
        return 1;
    }

    public boolean a(fd fd2, int n2, int n3, int n4) {
        return fd2.h(n2, n3 - 1, n4);
    }

    public void c(fd fd2, int n2, int n3, int n4) {
        if (!fd2.B) {
            this.a(fd2, n2, n3, n4, true);
        }
    }

    public void b(fd fd2, int n2, int n3, int n4, int n5) {
        int n6;
        if (fd2.B) {
            return;
        }
        int n7 = n6 = fd2.e(n2, n3, n4);
        if (this.a) {
            n7 &= 7;
        }
        boolean bl2 = false;
        if (!fd2.h(n2, n3 - 1, n4)) {
            bl2 = true;
        }
        if (n7 == 2 && !fd2.h(n2 + 1, n3, n4)) {
            bl2 = true;
        }
        if (n7 == 3 && !fd2.h(n2 - 1, n3, n4)) {
            bl2 = true;
        }
        if (n7 == 4 && !fd2.h(n2, n3, n4 - 1)) {
            bl2 = true;
        }
        if (n7 == 5 && !fd2.h(n2, n3, n4 + 1)) {
            bl2 = true;
        }
        if (bl2) {
            this.g(fd2, n2, n3, n4, fd2.e(n2, n3, n4));
            fd2.f(n2, n3, n4, 0);
        } else if (this.bn == uu.U.bn) {
            boolean bl3 = fd2.s(n2, n3, n4) || fd2.s(n2, n3 + 1, n4);
            bl3 = bl3 || this.a(fd2, n2, n3, n4, n6, true, 0) || this.a(fd2, n2, n3, n4, n6, false, 0);
            boolean bl4 = false;
            if (bl3 && (n6 & 8) == 0) {
                fd2.d(n2, n3, n4, n7 | 8);
                bl4 = true;
            } else if (!bl3 && (n6 & 8) != 0) {
                fd2.d(n2, n3, n4, n7);
                bl4 = true;
            }
            if (bl4) {
                fd2.i(n2, n3 - 1, n4, this.bn);
                if (n7 == 2 || n7 == 3 || n7 == 4 || n7 == 5) {
                    fd2.i(n2, n3 + 1, n4, this.bn);
                }
            }
        } else if (n5 > 0 && uu.m[n5].f() && !this.a && vo.a(new vo(this, fd2, n2, n3, n4)) == 3) {
            this.a(fd2, n2, n3, n4, false);
        }
    }

    private void a(fd fd2, int n2, int n3, int n4, boolean bl2) {
        if (fd2.B) {
            return;
        }
        new vo(this, fd2, n2, n3, n4).a(fd2.s(n2, n3, n4), bl2);
    }

    private boolean a(fd fd2, int n2, int n3, int n4, int n5, boolean bl2, int n6) {
        if (n6 >= 8) {
            return false;
        }
        int n7 = n5 & 7;
        boolean bl3 = true;
        switch (n7) {
            case 0: {
                if (bl2) {
                    ++n4;
                    break;
                }
                --n4;
                break;
            }
            case 1: {
                if (bl2) {
                    --n2;
                    break;
                }
                ++n2;
                break;
            }
            case 2: {
                if (bl2) {
                    --n2;
                } else {
                    ++n2;
                    ++n3;
                    bl3 = false;
                }
                n7 = 1;
                break;
            }
            case 3: {
                if (bl2) {
                    --n2;
                    ++n3;
                    bl3 = false;
                } else {
                    ++n2;
                }
                n7 = 1;
                break;
            }
            case 4: {
                if (bl2) {
                    ++n4;
                } else {
                    --n4;
                    ++n3;
                    bl3 = false;
                }
                n7 = 0;
                break;
            }
            case 5: {
                if (bl2) {
                    ++n4;
                    ++n3;
                    bl3 = false;
                } else {
                    --n4;
                }
                n7 = 0;
            }
        }
        if (this.a(fd2, n2, n3, n4, bl2, n6, n7)) {
            return true;
        }
        return bl3 && this.a(fd2, n2, n3 - 1, n4, bl2, n6, n7);
    }

    private boolean a(fd fd2, int n2, int n3, int n4, boolean bl2, int n5, int n6) {
        int n7 = fd2.a(n2, n3, n4);
        if (n7 == uu.U.bn) {
            int n8 = fd2.e(n2, n3, n4);
            int n9 = n8 & 7;
            if (n6 == 1 && (n9 == 0 || n9 == 4 || n9 == 5)) {
                return false;
            }
            if (n6 == 0 && (n9 == 1 || n9 == 2 || n9 == 3)) {
                return false;
            }
            if ((n8 & 8) != 0) {
                if (fd2.s(n2, n3, n4) || fd2.s(n2, n3 + 1, n4)) {
                    return true;
                }
                return this.a(fd2, n2, n3, n4, n8, bl2, n5 + 1);
            }
        }
        return false;
    }

    public int h() {
        return 0;
    }

    static /* synthetic */ boolean a(pc pc2) {
        return pc2.a;
    }
}

