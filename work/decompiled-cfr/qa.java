/*
 * Decompiled with CFR 0.152.
 */
import java.util.List;
import java.util.Map;

public final class qa
extends gu
implements Comparable {
    private final String a;

    qa(String string) {
        if (string == null) {
            throw new NullPointerException("Attempt to construct a JsonString with a null value.");
        }
        this.a = string;
    }

    public at a() {
        return at.c;
    }

    public String b() {
        return this.a;
    }

    public Map c() {
        throw new IllegalStateException("Attempt to get fields on a JsonNode without fields.");
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
        qa qa2 = (qa)object;
        return this.a.equals(qa2.a);
    }

    public int hashCode() {
        return this.a.hashCode();
    }

    public String toString() {
        return "JsonStringNode value:[" + this.a + "]";
    }

    public int a(qa qa2) {
        return this.a.compareTo(qa2.a);
    }
}

