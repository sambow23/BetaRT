/*
 * Decompiled with CFR 0.152.
 */
import java.util.LinkedList;
import java.util.List;

public final class mz
extends ad {
    final yp a;
    final List b;

    static ad a(yp yp2) {
        return new mz(yp2, new LinkedList());
    }

    static ad a(mz mz2, jt jt2) {
        LinkedList<jt> linkedList = new LinkedList<jt>(mz2.b);
        linkedList.add(jt2);
        return new mz(mz2.a, linkedList);
    }

    static ad b(mz mz2, jt jt2) {
        LinkedList<jt> linkedList = new LinkedList<jt>();
        linkedList.add(jt2);
        return new mz(mz2.a, linkedList);
    }

    private mz(yp yp2, List list) {
        super("Failed to match any JSON node at [" + mz.a(list) + "]");
        this.a = yp2;
        this.b = list;
    }

    static String a(List list) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i2 = list.size() - 1; i2 >= 0; --i2) {
            stringBuilder.append(((jt)list.get(i2)).a());
            if (i2 == 0) continue;
            stringBuilder.append(".");
        }
        return stringBuilder.toString();
    }

    public String toString() {
        return "JsonNodeDoesNotMatchJsonNodeSelectorException{failedNode=" + this.a + ", failPath=" + this.b + '}';
    }
}

