/*
 * Decompiled with CFR 0.152.
 */
import java.util.Comparator;

public class md
implements Comparator {
    private ls a;

    public md(ls ls2) {
        this.a = ls2;
    }

    public int a(dk dk2, dk dk3) {
        double d2;
        boolean bl2 = dk2.o;
        boolean bl3 = dk3.o;
        if (bl2 && !bl3) {
            return 1;
        }
        if (bl3 && !bl2) {
            return -1;
        }
        double d3 = dk2.a(this.a);
        if (d3 < (d2 = (double)dk3.a(this.a))) {
            return 1;
        }
        if (d3 > d2) {
            return -1;
        }
        return dk2.w < dk3.w ? 1 : -1;
    }
}

