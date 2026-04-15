/*
 * Decompiled with CFR 0.152.
 */
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class cq {
    private static Set b = new HashSet();
    protected static final Class[] a = new Class[]{cn.class, uz.class, fr.class};

    protected static wf a(fd fd2, int n2, int n3) {
        int n4 = n2 + fd2.r.nextInt(16);
        int n5 = fd2.r.nextInt(128);
        int n6 = n3 + fd2.r.nextInt(16);
        return new wf(n4, n5, n6);
    }

    public static final int a(fd fd2, boolean bl2, boolean bl3) {
        Object object;
        int n2;
        if (!bl2 && !bl3) {
            return 0;
        }
        b.clear();
        for (n2 = 0; n2 < fd2.d.size(); ++n2) {
            object = (gs)fd2.d.get(n2);
            int n3 = in.b(((gs)object).aM / 16.0);
            int n4 = in.b(((gs)object).aO / 16.0);
            int n5 = 8;
            for (int i2 = -n5; i2 <= n5; ++i2) {
                for (int i3 = -n5; i3 <= n5; ++i3) {
                    b.add(new yy(i2 + n3, i3 + n4));
                }
            }
        }
        n2 = 0;
        object = fd2.u();
        for (lk lk2 : lk.values()) {
            if (lk2.d() && !bl3 || !lk2.d() && !bl2 || fd2.b(lk2.a()) > lk2.b() * b.size() / 256) continue;
            block6: for (yy yy2 : b) {
                bj bj22;
                kd kd2 = fd2.a().a(yy2);
                List list = kd2.a(lk2);
                if (list == null || list.isEmpty()) continue;
                int n6 = 0;
                for (bj bj22 : list) {
                    n6 += bj22.b;
                }
                int n7 = fd2.r.nextInt(n6);
                bj22 = (bj)list.get(0);
                for (bj bj3 : list) {
                    if ((n7 -= bj3.b) >= 0) continue;
                    bj22 = bj3;
                    break;
                }
                wf wf2 = cq.a(fd2, yy2.a * 16, yy2.b * 16);
                int n8 = wf2.a;
                int n9 = wf2.b;
                int n10 = wf2.c;
                if (fd2.h(n8, n9, n10) || fd2.f(n8, n9, n10) != lk2.c()) continue;
                int n11 = 0;
                for (int i4 = 0; i4 < 3; ++i4) {
                    int n12 = n8;
                    int n13 = n9;
                    int n14 = n10;
                    int n15 = 6;
                    for (int i5 = 0; i5 < 4; ++i5) {
                        ls ls2;
                        float f2;
                        float f3;
                        float f4;
                        float f5;
                        float f6;
                        float f7;
                        float f8;
                        if (!cq.a(lk2, fd2, n12 += fd2.r.nextInt(n15) - fd2.r.nextInt(n15), n13 += fd2.r.nextInt(1) - fd2.r.nextInt(1), n14 += fd2.r.nextInt(n15) - fd2.r.nextInt(n15)) || fd2.a(f8 = (float)n12 + 0.5f, f7 = (float)n13, (double)(f6 = (float)n14 + 0.5f), 24.0) != null || (f5 = (f4 = f8 - (float)((br)object).a) * f4 + (f3 = f7 - (float)((br)object).b) * f3 + (f2 = f6 - (float)((br)object).c) * f2) < 576.0f) continue;
                        try {
                            ls2 = (ls)bj22.a.getConstructor(fd.class).newInstance(fd2);
                        }
                        catch (Exception exception) {
                            exception.printStackTrace();
                            return n2;
                        }
                        ls2.c(f8, f7, f6, fd2.r.nextFloat() * 360.0f, 0.0f);
                        if (ls2.d()) {
                            fd2.b(ls2);
                            cq.a(ls2, fd2, f8, f7, f6);
                            if (++n11 >= ls2.l()) continue block6;
                        }
                        n2 += n11;
                    }
                }
            }
        }
        return n2;
    }

    private static boolean a(lk lk2, fd fd2, int n2, int n3, int n4) {
        if (lk2.c() == ln.g) {
            return fd2.f(n2, n3, n4).d() && !fd2.h(n2, n3 + 1, n4);
        }
        return fd2.h(n2, n3 - 1, n4) && !fd2.h(n2, n3, n4) && !fd2.f(n2, n3, n4).d() && !fd2.h(n2, n3 + 1, n4);
    }

    private static void a(ls ls2, fd fd2, float f2, float f3, float f4) {
        if (ls2 instanceof cn && fd2.r.nextInt(100) == 0) {
            fr fr2 = new fr(fd2);
            fr2.c(f2, f3, f4, ls2.aS, 0.0f);
            fd2.b(fr2);
            fr2.i(ls2);
        } else if (ls2 instanceof dl) {
            ((dl)ls2).e_(dl.a(fd2.r));
        }
    }

    public static boolean a(fd fd2, List list) {
        boolean bl2 = false;
        fw fw2 = new fw(fd2);
        for (gs gs2 : list) {
            Class[] classArray = a;
            if (classArray == null || classArray.length == 0) continue;
            boolean bl3 = false;
            for (int i2 = 0; i2 < 20 && !bl3; ++i2) {
                dh dh2;
                ls ls2;
                int n2;
                int n3 = in.b(gs2.aM) + fd2.r.nextInt(32) - fd2.r.nextInt(32);
                int n4 = in.b(gs2.aO) + fd2.r.nextInt(32) - fd2.r.nextInt(32);
                int n5 = in.b(gs2.aN) + fd2.r.nextInt(16) - fd2.r.nextInt(16);
                if (n5 < 1) {
                    n5 = 1;
                } else if (n5 > 128) {
                    n5 = 128;
                }
                int n6 = fd2.r.nextInt(classArray.length);
                for (n2 = n5; n2 > 2 && !fd2.h(n3, n2 - 1, n4); --n2) {
                }
                while (!cq.a(lk.a, fd2, n3, n2, n4) && n2 < n5 + 16 && n2 < 128) {
                    ++n2;
                }
                if (n2 >= n5 + 16 || n2 >= 128) {
                    n2 = n5;
                    continue;
                }
                float f2 = (float)n3 + 0.5f;
                float f3 = n2;
                float f4 = (float)n4 + 0.5f;
                try {
                    ls2 = (ls)classArray[n6].getConstructor(fd.class).newInstance(fd2);
                }
                catch (Exception exception) {
                    exception.printStackTrace();
                    return bl2;
                }
                ls2.c(f2, f3, f4, fd2.r.nextFloat() * 360.0f, 0.0f);
                if (!ls2.d() || (dh2 = fw2.a(ls2, gs2, 32.0f)) == null || dh2.a <= 1) continue;
                d d2 = dh2.c();
                if (!(Math.abs((double)d2.a - gs2.aM) < 1.5) || !(Math.abs((double)d2.c - gs2.aO) < 1.5) || !(Math.abs((double)d2.b - gs2.aN) < 1.5)) continue;
                br br2 = ve.f(fd2, in.b(gs2.aM), in.b(gs2.aN), in.b(gs2.aO), 1);
                if (br2 == null) {
                    br2 = new br(n3, n2 + 1, n4);
                }
                ls2.c((float)br2.a + 0.5f, br2.b, (float)br2.c + 0.5f, 0.0f, 0.0f);
                fd2.b(ls2);
                cq.a(ls2, fd2, (float)br2.a + 0.5f, (float)br2.b, (float)br2.c + 0.5f);
                gs2.a(true, false, false);
                ls2.T();
                bl2 = true;
                bl3 = true;
            }
        }
        return bl2;
    }
}

