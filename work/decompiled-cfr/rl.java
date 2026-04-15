/*
 * Decompiled with CFR 0.152.
 */
public class rl
extends gm {
    public int a;

    public rl(int n2, int n3) {
        super(n2);
        this.bg = 1;
        this.a = n3;
    }

    public boolean a(iz iz2, gs gs2, fd fd2, int n2, int n3, int n4, int n5) {
        int n6 = fd2.a(n2, n3, n4);
        if (pc.d(n6)) {
            if (!fd2.B) {
                fd2.b(new yl(fd2, (float)n2 + 0.5f, (float)n3 + 0.5f, (float)n4 + 0.5f, this.a));
            }
            --iz2.a;
            return true;
        }
        return false;
    }
}

