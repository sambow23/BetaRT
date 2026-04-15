/*
 * Decompiled with CFR 0.152.
 */
public abstract class rw
extends uu {
    protected rw(int n2, ln ln2) {
        super(n2, ln2);
        rw.p[n2] = true;
    }

    protected rw(int n2, int n3, ln ln2) {
        super(n2, n3, ln2);
        rw.p[n2] = true;
    }

    public void c(fd fd2, int n2, int n3, int n4) {
        super.c(fd2, n2, n3, n4);
        fd2.a(n2, n3, n4, this.a_());
    }

    public void b(fd fd2, int n2, int n3, int n4) {
        super.b(fd2, n2, n3, n4);
        fd2.p(n2, n3, n4);
    }

    protected abstract ow a_();
}

