/*
 * Decompiled with CFR 0.152.
 */
import java.util.Arrays;

public final class ka {
    private ka() {
    }

    public static jt a(Object ... objectArray) {
        return ka.a(objectArray, new jt(new kk()));
    }

    public static jt b(Object ... objectArray) {
        return ka.a(objectArray, new jt(new kf()));
    }

    public static jt c(Object ... objectArray) {
        return ka.a(objectArray, new jt(new qf()));
    }

    public static jt a(String string) {
        return ka.a(ol.a(string));
    }

    public static jt a(qa qa2) {
        return new jt(new qi(qa2));
    }

    public static jt b(String string) {
        return ka.c(new Object[0]).a(ka.a(string));
    }

    public static jt a(int n2) {
        return new jt(new qg(n2));
    }

    public static jt b(int n2) {
        return ka.b(new Object[0]).a(ka.a(n2));
    }

    private static jt a(Object[] objectArray, jt jt2) {
        jt jt3 = jt2;
        for (int i2 = objectArray.length - 1; i2 >= 0; --i2) {
            if (objectArray[i2] instanceof Integer) {
                jt3 = ka.a(ka.b((Integer)objectArray[i2]), jt3);
                continue;
            }
            if (objectArray[i2] instanceof String) {
                jt3 = ka.a(ka.b((String)objectArray[i2]), jt3);
                continue;
            }
            throw new IllegalArgumentException("Element [" + objectArray[i2] + "] of path elements" + " [" + Arrays.toString(objectArray) + "] was of illegal type [" + objectArray[i2].getClass().getCanonicalName() + "]; only Integer and String are valid.");
        }
        return jt3;
    }

    private static jt a(jt jt2, jt jt3) {
        return new jt(new j(jt2, jt3));
    }
}

