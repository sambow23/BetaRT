/*
 * Decompiled with CFR 0.152.
 */
class wm {
    final int a;
    Object b;
    wm c;
    final int d;

    wm(int n2, int n3, Object object, wm wm2) {
        this.b = object;
        this.c = wm2;
        this.a = n3;
        this.d = n2;
    }

    public final int a() {
        return this.a;
    }

    public final Object b() {
        return this.b;
    }

    public final boolean equals(Object object) {
        Object object2;
        Object object3;
        Integer n2;
        if (!(object instanceof wm)) {
            return false;
        }
        wm wm2 = (wm)object;
        Integer n3 = this.a();
        return (n3 == (n2 = Integer.valueOf(wm2.a())) || n3 != null && ((Object)n3).equals(n2)) && ((object3 = this.b()) == (object2 = wm2.b()) || object3 != null && object3.equals(object2));
    }

    public final int hashCode() {
        return jx.d(this.a);
    }

    public final String toString() {
        return this.a() + "=" + this.b();
    }
}

