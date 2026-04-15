/*
 * Decompiled with CFR 0.152.
 */
import java.util.Arrays;
import java.util.Map;

public final class ol {
    private ol() {
    }

    public static gu a() {
        return vn.a;
    }

    public static gu b() {
        return vn.b;
    }

    public static gu c() {
        return vn.c;
    }

    public static qa a(String string) {
        return new qa(string);
    }

    public static gu b(String string) {
        return new ja(string);
    }

    public static qe a(Iterable iterable) {
        return new oe(iterable);
    }

    public static qe a(gu ... guArray) {
        return ol.a(Arrays.asList(guArray));
    }

    public static qe a(Map map) {
        return new kr(map);
    }
}

