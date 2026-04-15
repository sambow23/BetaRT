/*
 * Decompiled with CFR 0.152.
 */
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class oe
extends qe {
    private final List a;

    oe(Iterable iterable) {
        this.a = oe.a(iterable);
    }

    public at a() {
        return at.b;
    }

    public List d() {
        return new ArrayList(this.a);
    }

    public String b() {
        throw new IllegalStateException("Attempt to get text on a JsonNode without text.");
    }

    public Map c() {
        throw new IllegalStateException("Attempt to get fields on a JsonNode without fields.");
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        oe oe2 = (oe)object;
        return ((Object)this.a).equals(oe2.a);
    }

    public int hashCode() {
        return ((Object)this.a).hashCode();
    }

    public String toString() {
        return "JsonArray elements:[" + this.a + "]";
    }

    private static List a(Iterable iterable) {
        return new y(iterable);
    }
}

