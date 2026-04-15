/*
 * Decompiled with CFR 0.152.
 */
public class is
implements dt {
    private int b;
    private int c;
    private iz[] d;
    private iz e;
    public final int a;

    public is(int n2, int n3, iz[] izArray, iz iz2) {
        this.a = iz2.c;
        this.b = n2;
        this.c = n3;
        this.d = izArray;
        this.e = iz2;
    }

    public iz b() {
        return this.e;
    }

    public boolean a(mq mq2) {
        for (int i2 = 0; i2 <= 3 - this.b; ++i2) {
            for (int i3 = 0; i3 <= 3 - this.c; ++i3) {
                if (this.a(mq2, i2, i3, true)) {
                    return true;
                }
                if (!this.a(mq2, i2, i3, false)) continue;
                return true;
            }
        }
        return false;
    }

    private boolean a(mq mq2, int n2, int n3, boolean bl2) {
        for (int i2 = 0; i2 < 3; ++i2) {
            for (int i3 = 0; i3 < 3; ++i3) {
                iz iz2;
                int n4 = i2 - n2;
                int n5 = i3 - n3;
                iz iz3 = null;
                if (n4 >= 0 && n5 >= 0 && n4 < this.b && n5 < this.c) {
                    iz3 = bl2 ? this.d[this.b - n4 - 1 + n5 * this.b] : this.d[n4 + n5 * this.b];
                }
                if ((iz2 = mq2.b(i2, i3)) == null && iz3 == null) continue;
                if (iz2 == null && iz3 != null || iz2 != null && iz3 == null) {
                    return false;
                }
                if (iz3.c != iz2.c) {
                    return false;
                }
                if (iz3.i() == -1 || iz3.i() == iz2.i()) continue;
                return false;
            }
        }
        return true;
    }

    public iz b(mq mq2) {
        return new iz(this.e.c, this.e.a, this.e.i());
    }

    public int a() {
        return this.b * this.c;
    }
}

