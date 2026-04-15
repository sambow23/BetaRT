/*
 * Decompiled with CFR 0.152.
 */
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class kr
extends qe {
    private final Map a;

    kr(Map map) {
        this.a = new HashMap(map);
    }

    public Map c() {
        return new HashMap(this.a);
    }

    public at a() {
        return at.a;
    }

    public String b() {
        throw new IllegalStateException("Attempt to get text on a JsonNode without text.");
    }

    public List d() {
        throw new IllegalStateException("Attempt to get elements on a JsonNode without elements.");
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || this.getClass() != object.getClass()) {
            return false;
        }
        kr kr2 = (kr)object;
        return ((Object)this.a).equals(kr2.a);
    }

    public int hashCode() {
        return ((Object)this.a).hashCode();
    }

    public String toString() {
        return "JsonObject fields:[" + this.a + "]";
    }
}

