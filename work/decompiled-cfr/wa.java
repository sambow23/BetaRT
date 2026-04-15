/*
 * Decompiled with CFR 0.152.
 */
public class wa
extends gm {
    private static final int[] bn = new int[]{3, 8, 6, 3};
    private static final int[] bo = new int[]{11, 16, 15, 13};
    public final int a;
    public final int bk;
    public final int bl;
    public final int bm;

    public wa(int n2, int n3, int n4, int n5) {
        super(n2);
        this.a = n3;
        this.bk = n5;
        this.bm = n4;
        this.bl = bn[n5];
        this.e(bo[n5] * 3 << n3);
        this.bg = 1;
    }
}

