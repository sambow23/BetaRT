/*
 * Decompiled with CFR 0.152.
 */
public final class ao
extends ad {
    private static final ml a = new el();

    static ao a(mz mz2, Object[] objectArray, qe qe2) {
        return new ao(mz2, objectArray, qe2);
    }

    private ao(mz mz2, Object[] objectArray, qe qe2) {
        super(ao.b(mz2, objectArray, qe2));
    }

    private static String b(mz mz2, Object[] objectArray, qe qe2) {
        return "Failed to find " + mz2.a.toString() + " at [" + mz.a(mz2.b) + "] while resolving [" + ao.a(objectArray) + "] in " + a.a(qe2) + ".";
    }

    private static String a(Object[] objectArray) {
        StringBuilder stringBuilder = new StringBuilder();
        boolean bl2 = true;
        for (Object object : objectArray) {
            if (!bl2) {
                stringBuilder.append(".");
            }
            bl2 = false;
            if (object instanceof String) {
                stringBuilder.append("\"").append(object).append("\"");
                continue;
            }
            stringBuilder.append(object);
        }
        return stringBuilder.toString();
    }
}

