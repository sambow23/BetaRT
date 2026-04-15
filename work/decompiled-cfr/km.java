/*
 * Decompiled with CFR 0.152.
 */
public class km
extends gm {
    public static final String[] a = new String[]{"black", "red", "green", "brown", "blue", "purple", "cyan", "silver", "gray", "pink", "lime", "yellow", "lightBlue", "magenta", "orange", "white"};
    public static final int[] bk = new int[]{0x1E1B1B, 11743532, 3887386, 5320730, 2437522, 8073150, 2651799, 2651799, 0x434343, 14188952, 4312372, 14602026, 6719955, 12801229, 15435844, 0xF0F0F0};

    public km(int n2) {
        super(n2);
        this.a(true);
        this.e(0);
    }

    public int a(int n2) {
        int n3 = n2;
        return this.bh + n3 % 8 * 16 + n3 / 8;
    }

    public String a(iz iz2) {
        return super.a() + "." + a[iz2.i()];
    }

    public boolean a(iz iz2, gs gs2, fd fd2, int n2, int n3, int n4, int n5) {
        if (iz2.i() == 15) {
            int n6 = fd2.a(n2, n3, n4);
            if (n6 == uu.z.bn) {
                if (!fd2.B) {
                    ((he)uu.z).c(fd2, n2, n3, n4, fd2.r);
                    --iz2.a;
                }
                return true;
            }
            if (n6 == uu.aA.bn) {
                if (!fd2.B) {
                    ((ni)uu.aA).d_(fd2, n2, n3, n4);
                    --iz2.a;
                }
                return true;
            }
            if (n6 == uu.v.bn) {
                if (!fd2.B) {
                    --iz2.a;
                    block0: for (int i2 = 0; i2 < 128; ++i2) {
                        int n7 = n2;
                        int n8 = n3 + 1;
                        int n9 = n4;
                        for (int i3 = 0; i3 < i2 / 16; ++i3) {
                            if (fd2.a(n7 += b.nextInt(3) - 1, (n8 += (b.nextInt(3) - 1) * b.nextInt(3) / 2) - 1, n9 += b.nextInt(3) - 1) != uu.v.bn || fd2.h(n7, n8, n9)) continue block0;
                        }
                        if (fd2.a(n7, n8, n9) != 0) continue;
                        if (b.nextInt(10) != 0) {
                            fd2.b(n7, n8, n9, uu.Y.bn, 1);
                            continue;
                        }
                        if (b.nextInt(3) != 0) {
                            fd2.f(n7, n8, n9, uu.ae.bn);
                            continue;
                        }
                        fd2.f(n7, n8, n9, uu.af.bn);
                    }
                }
                return true;
            }
        }
        return false;
    }

    public void a(iz iz2, ls ls2) {
        if (ls2 instanceof dl) {
            dl dl2 = (dl)ls2;
            int n2 = ee.d(iz2.i());
            if (!dl2.s() && dl2.r() != n2) {
                dl2.e_(n2);
                --iz2.a;
            }
        }
    }
}

