/*
 * Decompiled with CFR 0.152.
 */
public class sa
extends gm {
    public sa(int n2) {
        super(n2);
        this.bg = 1;
    }

    public void a(iz iz2, ls ls2) {
        wh wh2;
        if (ls2 instanceof wh && !(wh2 = (wh)ls2).v()) {
            wh2.a(true);
            --iz2.a;
        }
    }

    public boolean a(iz iz2, ls ls2, ls ls3) {
        this.a(iz2, ls2);
        return true;
    }
}

