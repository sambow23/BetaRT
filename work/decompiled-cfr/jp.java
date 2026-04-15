/*
 * Decompiled with CFR 0.152.
 */
public class jp
extends uu {
    private boolean a;

    protected jp(int n2, int n3, ln ln2, boolean bl2) {
        super(n2, n3, ln2);
        this.a = bl2;
    }

    public boolean c() {
        return false;
    }

    public boolean b(xp xp2, int n2, int n3, int n4, int n5) {
        int n6 = xp2.a(n2, n3, n4);
        if (!this.a && n6 == this.bn) {
            return false;
        }
        return super.b(xp2, n2, n3, n4, n5);
    }
}

