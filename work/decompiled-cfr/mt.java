/*
 * Decompiled with CFR 0.152.
 */
import java.util.Random;

public class mt
extends uu {
    public mt(int n2, int n3) {
        super(n2, n3, ln.e);
    }

    public int a(int n2, Random random) {
        if (this.bn == uu.J.bn) {
            return gm.k.bf;
        }
        if (this.bn == uu.ax.bn) {
            return gm.l.bf;
        }
        if (this.bn == uu.O.bn) {
            return gm.aU.bf;
        }
        return this.bn;
    }

    public int a(Random random) {
        if (this.bn == uu.O.bn) {
            return 4 + random.nextInt(5);
        }
        return 1;
    }

    protected int b_(int n2) {
        if (this.bn == uu.O.bn) {
            return 4;
        }
        return 0;
    }
}

