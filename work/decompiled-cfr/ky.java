/*
 * Decompiled with CFR 0.152.
 */
import java.util.Comparator;

class ky
implements Comparator {
    final /* synthetic */ hk a;

    ky(hk hk2) {
        this.a = hk2;
    }

    public int a(dt dt2, dt dt3) {
        if (dt2 instanceof tt && dt3 instanceof is) {
            return 1;
        }
        if (dt3 instanceof tt && dt2 instanceof is) {
            return -1;
        }
        if (dt3.a() < dt2.a()) {
            return -1;
        }
        if (dt3.a() > dt2.a()) {
            return 1;
        }
        return 0;
    }
}

