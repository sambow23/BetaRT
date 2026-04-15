/*
 * Decompiled with CFR 0.152.
 */
import net.minecraft.client.Minecraft;

public class ob {
    protected final Minecraft a;
    public boolean b = false;

    public ob(Minecraft minecraft) {
        this.a = minecraft;
    }

    public void a(fd fd2) {
    }

    public void a(int n2, int n3, int n4, int n5) {
        this.a.f.a(this.a.h, n2, n3, n4, n5);
        this.b(n2, n3, n4, n5);
    }

    public boolean b(int n2, int n3, int n4, int n5) {
        fd fd2 = this.a.f;
        uu uu2 = uu.m[fd2.a(n2, n3, n4)];
        fd2.e(2001, n2, n3, n4, uu2.bn + fd2.e(n2, n3, n4) * 256);
        int n6 = fd2.e(n2, n3, n4);
        boolean bl2 = fd2.f(n2, n3, n4, 0);
        if (uu2 != null && bl2) {
            uu2.c(fd2, n2, n3, n4, n6);
        }
        return bl2;
    }

    public void c(int n2, int n3, int n4, int n5) {
    }

    public void a() {
    }

    public void a(float f2) {
    }

    public float b() {
        return 5.0f;
    }

    public boolean a(gs gs2, fd fd2, iz iz2) {
        int n2 = iz2.a;
        iz iz3 = iz2.a(fd2, gs2);
        if (iz3 != iz2 || iz3 != null && iz3.a != n2) {
            gs2.c.a[gs2.c.c] = iz3;
            if (iz3.a == 0) {
                gs2.c.a[gs2.c.c] = null;
            }
            return true;
        }
        return false;
    }

    public void a(gs gs2) {
    }

    public void c() {
    }

    public boolean d() {
        return true;
    }

    public void b(gs gs2) {
    }

    public boolean a(gs gs2, fd fd2, iz iz2, int n2, int n3, int n4, int n5) {
        int n6 = fd2.a(n2, n3, n4);
        if (n6 > 0 && uu.m[n6].a(fd2, n2, n3, n4, gs2)) {
            return true;
        }
        if (iz2 == null) {
            return false;
        }
        return iz2.a(gs2, fd2, n2, n3, n4, n5);
    }

    public gs b(fd fd2) {
        return new dc(this.a, fd2, this.a.k, fd2.t.g);
    }

    public void a(gs gs2, sn sn2) {
        gs2.c(sn2);
    }

    public void b(gs gs2, sn sn2) {
        gs2.d(sn2);
    }

    public iz a(int n2, int n3, int n4, boolean bl2, gs gs2) {
        return gs2.e.a(n3, n4, bl2, gs2);
    }

    public void a(int n2, gs gs2) {
        gs2.e.a(gs2);
        gs2.e = gs2.d;
    }
}

