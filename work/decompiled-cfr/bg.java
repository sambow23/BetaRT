/*
 * Decompiled with CFR 0.152.
 */
public abstract class bg
extends ii {
    public bg(fd fd2) {
        super(fd2);
    }

    protected float a(int n2, int n3, int n4) {
        if (this.aI.a(n2, n3 - 1, n4) == uu.v.bn) {
            return 10.0f;
        }
        return this.aI.c(n2, n3, n4) - 0.5f;
    }

    public void b(nu nu2) {
        super.b(nu2);
    }

    public void a(nu nu2) {
        super.a(nu2);
    }

    public boolean d() {
        int n2;
        int n3;
        int n4 = in.b(this.aM);
        return this.aI.a(n4, (n3 = in.b(this.aW.b)) - 1, n2 = in.b(this.aO)) == uu.v.bn && this.aI.m(n4, n3, n2) > 8 && super.d();
    }

    public int e() {
        return 120;
    }
}

