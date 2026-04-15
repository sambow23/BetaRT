/*
 * Decompiled with CFR 0.152.
 */
import java.util.Random;

public class ni
extends wb {
    protected ni(int n2, int n3) {
        super(n2, n3);
        this.bm = n3;
        this.b(true);
        float f2 = 0.5f;
        this.a(0.5f - f2, 0.0f, 0.5f - f2, 0.5f + f2, 0.25f, 0.5f + f2);
    }

    protected boolean d(int n2) {
        return n2 == uu.aB.bn;
    }

    public void a(fd fd2, int n2, int n3, int n4, Random random) {
        float f2;
        int n5;
        super.a(fd2, n2, n3, n4, random);
        if (fd2.n(n2, n3 + 1, n4) >= 9 && (n5 = fd2.e(n2, n3, n4)) < 7 && random.nextInt((int)(100.0f / (f2 = this.i(fd2, n2, n3, n4)))) == 0) {
            fd2.d(n2, n3, n4, ++n5);
        }
    }

    public void d_(fd fd2, int n2, int n3, int n4) {
        fd2.d(n2, n3, n4, 7);
    }

    private float i(fd fd2, int n2, int n3, int n4) {
        float f2 = 1.0f;
        int n5 = fd2.a(n2, n3, n4 - 1);
        int n6 = fd2.a(n2, n3, n4 + 1);
        int n7 = fd2.a(n2 - 1, n3, n4);
        int n8 = fd2.a(n2 + 1, n3, n4);
        int n9 = fd2.a(n2 - 1, n3, n4 - 1);
        int n10 = fd2.a(n2 + 1, n3, n4 - 1);
        int n11 = fd2.a(n2 + 1, n3, n4 + 1);
        int n12 = fd2.a(n2 - 1, n3, n4 + 1);
        boolean bl2 = n7 == this.bn || n8 == this.bn;
        boolean bl3 = n5 == this.bn || n6 == this.bn;
        boolean bl4 = n9 == this.bn || n10 == this.bn || n11 == this.bn || n12 == this.bn;
        for (int i2 = n2 - 1; i2 <= n2 + 1; ++i2) {
            for (int i3 = n4 - 1; i3 <= n4 + 1; ++i3) {
                int n13 = fd2.a(i2, n3 - 1, i3);
                float f3 = 0.0f;
                if (n13 == uu.aB.bn) {
                    f3 = 1.0f;
                    if (fd2.e(i2, n3 - 1, i3) > 0) {
                        f3 = 3.0f;
                    }
                }
                if (i2 != n2 || i3 != n4) {
                    f3 /= 4.0f;
                }
                f2 += f3;
            }
        }
        if (bl4 || bl2 && bl3) {
            f2 /= 2.0f;
        }
        return f2;
    }

    public int a(int n2, int n3) {
        if (n3 < 0) {
            n3 = 7;
        }
        return this.bm + n3;
    }

    public int b() {
        return 6;
    }

    public void a(fd fd2, int n2, int n3, int n4, int n5, float f2) {
        super.a(fd2, n2, n3, n4, n5, f2);
        if (fd2.B) {
            return;
        }
        for (int i2 = 0; i2 < 3; ++i2) {
            if (fd2.r.nextInt(15) > n5) continue;
            float f3 = 0.7f;
            float f4 = fd2.r.nextFloat() * f3 + (1.0f - f3) * 0.5f;
            float f5 = fd2.r.nextFloat() * f3 + (1.0f - f3) * 0.5f;
            float f6 = fd2.r.nextFloat() * f3 + (1.0f - f3) * 0.5f;
            hl hl2 = new hl(fd2, (float)n2 + f4, (float)n3 + f5, (float)n4 + f6, new iz(gm.Q));
            hl2.c = 10;
            fd2.b(hl2);
        }
    }

    public int a(int n2, Random random) {
        if (n2 == 7) {
            return gm.R.bf;
        }
        return -1;
    }

    public int a(Random random) {
        return 1;
    }
}

