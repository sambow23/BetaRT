/*
 * Decompiled with CFR 0.152.
 */
import java.util.Map;

final class qi
extends ba {
    final /* synthetic */ qa a;

    qi(qa qa2) {
        this.a = qa2;
    }

    public boolean a(Map map) {
        return map.containsKey(this.a);
    }

    public String a() {
        return "\"" + this.a.b() + "\"";
    }

    public gu b(Map map) {
        return (gu)map.get(this.a);
    }

    public String toString() {
        return "a field called [\"" + this.a.b() + "\"]";
    }
}

