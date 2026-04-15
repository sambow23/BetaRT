/*
 * Decompiled with CFR 0.152.
 */
public class ab
extends ke {
    private final ht i;

    public ab(int n2, int n3, int n4, String string) {
        this(n2, n3, n4, null, string);
    }

    public ab(int n2, int n3, int n4, int n5, int n6, String string) {
        super(n2, n3, n4, n5, n6, string);
        this.i = null;
    }

    public ab(int n2, int n3, int n4, ht ht2, String string) {
        super(n2, n3, n4, 150, 20, string);
        this.i = ht2;
    }

    public ht a() {
        return this.i;
    }
}

