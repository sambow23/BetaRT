/*
 * Decompiled with CFR 0.152.
 */
import java.util.List;
import java.util.Map;

public abstract class gu {
    gu() {
    }

    public abstract at a();

    public abstract String b();

    public abstract Map c();

    public abstract List d();

    public final String a(Object ... objectArray) {
        return (String)this.a(ka.a(objectArray), this, objectArray);
    }

    public final List b(Object ... objectArray) {
        return (List)this.a(ka.b(objectArray), this, objectArray);
    }

    private Object a(jt jt2, gu gu2, Object[] objectArray) {
        try {
            return jt2.b(gu2);
        }
        catch (mz mz2) {
            throw ao.a(mz2, objectArray, ol.a(gu2));
        }
    }
}

