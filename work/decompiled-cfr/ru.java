/*
 * Decompiled with CFR 0.152.
 */
import java.util.Random;

public class ru
extends wb {
    protected ru(int n2, int n3) {
        super(n2, n3);
        float f2 = 0.4f;
        this.a(0.5f - f2, 0.0f, 0.5f - f2, 0.5f + f2, 0.8f, 0.5f + f2);
    }

    public int a(int n2, int n3) {
        if (n3 == 1) {
            return this.bm;
        }
        if (n3 == 2) {
            return this.bm + 16 + 1;
        }
        if (n3 == 0) {
            return this.bm + 16;
        }
        return this.bm;
    }

    public int b(xp xp2, int n2, int n3, int n4) {
        int n5 = xp2.e(n2, n3, n4);
        if (n5 == 0) {
            return 0xFFFFFF;
        }
        long l2 = n2 * 3129871 + n4 * 6129781 + n3;
        l2 = l2 * l2 * 42317861L + l2 * 11L;
        n2 = (int)((long)n2 + (l2 >> 14 & 0x1FL));
        n3 = (int)((long)n3 + (l2 >> 19 & 0x1FL));
        n4 = (int)((long)n4 + (l2 >> 24 & 0x1FL));
        xp2.a().a(n2, n4, 1, 1);
        double d2 = xp2.a().a[0];
        double d3 = xp2.a().b[0];
        return ia.a(d2, d3);
    }

    public int a(int n2, Random random) {
        if (random.nextInt(8) == 0) {
            return gm.Q.bf;
        }
        return -1;
    }
}

