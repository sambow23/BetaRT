/*
 * Decompiled with CFR 0.152.
 */
import java.util.List;
import java.util.Map;

final class vn
extends gu {
    static final vn a = new vn(at.g);
    static final vn b = new vn(at.e);
    static final vn c = new vn(at.f);
    private final at d;

    private vn(at at2) {
        this.d = at2;
    }

    public at a() {
        return this.d;
    }

    public String b() {
        throw new IllegalStateException("Attempt to get text on a JsonNode without text.");
    }

    public Map c() {
        throw new IllegalStateException("Attempt to get fields on a JsonNode without fields.");
    }

    public List d() {
        throw new IllegalStateException("Attempt to get elements on a JsonNode without elements.");
    }
}

