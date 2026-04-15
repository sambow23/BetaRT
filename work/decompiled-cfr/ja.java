/*
 * Decompiled with CFR 0.152.
 */
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

final class ja
extends gu {
    private static final Pattern a = Pattern.compile("(-?)(0|([1-9]([0-9]*)))(\\.[0-9]+)?((e|E)(\\+|-)?[0-9]+)?");
    private final String b;

    ja(String string) {
        if (string == null) {
            throw new NullPointerException("Attempt to construct a JsonNumber with a null value.");
        }
        if (!a.matcher(string).matches()) {
            throw new IllegalArgumentException("Attempt to construct a JsonNumber with a String [" + string + "] that does not match the JSON number specification.");
        }
        this.b = string;
    }

    public at a() {
        return at.d;
    }

    public String b() {
        return this.b;
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
        ja ja2 = (ja)object;
        return this.b.equals(ja2.b);
    }

    public int hashCode() {
        return this.b.hashCode();
    }

    public String toString() {
        return "JsonNumberNode value:[" + this.b + "]";
    }
}

