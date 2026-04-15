/*
 * Decompiled with CFR 0.152.
 */
public class jx {
    private transient wm[] a = new wm[16];
    private transient int b;
    private int c = 12;
    private final float d;
    private volatile transient int e;

    public jx() {
        this.d = 0.75f;
    }

    private static int e(int n2) {
        n2 ^= n2 >>> 20 ^ n2 >>> 12;
        return n2 ^ n2 >>> 7 ^ n2 >>> 4;
    }

    private static int a(int n2, int n3) {
        return n2 & n3 - 1;
    }

    public Object a(int n2) {
        int n3 = jx.e(n2);
        wm wm2 = this.a[jx.a(n3, this.a.length)];
        while (wm2 != null) {
            if (wm2.a == n2) {
                return wm2.b;
            }
            wm2 = wm2.c;
        }
        return null;
    }

    public void a(int n2, Object object) {
        int n3 = jx.e(n2);
        int n4 = jx.a(n3, this.a.length);
        wm wm2 = this.a[n4];
        while (wm2 != null) {
            if (wm2.a == n2) {
                wm2.b = object;
            }
            wm2 = wm2.c;
        }
        ++this.e;
        this.a(n3, n2, object, n4);
    }

    private void f(int n2) {
        wm[] wmArray = this.a;
        int n3 = wmArray.length;
        if (n3 == 0x40000000) {
            this.c = Integer.MAX_VALUE;
            return;
        }
        wm[] wmArray2 = new wm[n2];
        this.a(wmArray2);
        this.a = wmArray2;
        this.c = (int)((float)n2 * this.d);
    }

    private void a(wm[] wmArray) {
        wm[] wmArray2 = this.a;
        int n2 = wmArray.length;
        for (int i2 = 0; i2 < wmArray2.length; ++i2) {
            wm wm2;
            wm wm3 = wmArray2[i2];
            if (wm3 == null) continue;
            wmArray2[i2] = null;
            do {
                wm2 = wm3.c;
                int n3 = jx.a(wm3.d, n2);
                wm3.c = wmArray[n3];
                wmArray[n3] = wm3;
            } while ((wm3 = wm2) != null);
        }
    }

    public Object b(int n2) {
        wm wm2 = this.c(n2);
        return wm2 == null ? null : wm2.b;
    }

    final wm c(int n2) {
        wm wm2;
        int n3 = jx.e(n2);
        int n4 = jx.a(n3, this.a.length);
        wm wm3 = wm2 = this.a[n4];
        while (wm3 != null) {
            wm wm4 = wm3.c;
            if (wm3.a == n2) {
                ++this.e;
                --this.b;
                if (wm2 == wm3) {
                    this.a[n4] = wm4;
                } else {
                    wm2.c = wm4;
                }
                return wm3;
            }
            wm2 = wm3;
            wm3 = wm4;
        }
        return wm3;
    }

    public void a() {
        ++this.e;
        wm[] wmArray = this.a;
        for (int i2 = 0; i2 < wmArray.length; ++i2) {
            wmArray[i2] = null;
        }
        this.b = 0;
    }

    private void a(int n2, int n3, Object object, int n4) {
        wm wm2 = this.a[n4];
        this.a[n4] = new wm(n2, n3, object, wm2);
        if (this.b++ >= this.c) {
            this.f(2 * this.a.length);
        }
    }

    static /* synthetic */ int d(int n2) {
        return jx.e(n2);
    }
}

