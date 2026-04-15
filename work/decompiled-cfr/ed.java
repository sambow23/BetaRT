/*
 * Decompiled with CFR 0.152.
 */
import java.util.LinkedList;
import java.util.List;

public final class ed
implements lb {
    private final List a = new LinkedList();

    ed() {
    }

    public ed a(lb lb2) {
        this.a.add(lb2);
        return this;
    }

    public qe a() {
        LinkedList<gu> linkedList = new LinkedList<gu>();
        for (lb lb2 : this.a) {
            linkedList.add(lb2.b());
        }
        return ol.a(linkedList);
    }
}

