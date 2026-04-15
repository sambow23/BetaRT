/*
 * Decompiled with CFR 0.152.
 */
import java.util.List;

final class qg
extends ba {
    final /* synthetic */ int a;

    qg(int n2) {
        this.a = n2;
    }

    public boolean a(List list) {
        return list.size() > this.a;
    }

    public String a() {
        return Integer.toString(this.a);
    }

    public gu b(List list) {
        return (gu)list.get(this.a);
    }

    public String toString() {
        return "an element at index [" + this.a + "]";
    }
}

