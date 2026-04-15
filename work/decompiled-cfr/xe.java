/*
 * Decompiled with CFR 0.152.
 */
public final class xe
extends Exception {
    private final int a;
    private final int b;

    xe(String string, et et2) {
        super("At line " + et2.b() + ", column " + et2.a() + ":  " + string);
        this.a = et2.a();
        this.b = et2.b();
    }

    xe(String string, Throwable throwable, et et2) {
        super("At line " + et2.b() + ", column " + et2.a() + ":  " + string, throwable);
        this.a = et2.a();
        this.b = et2.b();
    }
}

