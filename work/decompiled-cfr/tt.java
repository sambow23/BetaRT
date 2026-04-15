/*
 * Decompiled with CFR 0.152.
 */
import java.util.ArrayList;
import java.util.List;

public class tt
implements dt {
    private final iz a;
    private final List b;

    public tt(iz iz2, List list) {
        this.a = iz2;
        this.b = list;
    }

    public iz b() {
        return this.a;
    }

    public boolean a(mq mq2) {
        ArrayList arrayList = new ArrayList(this.b);
        for (int i2 = 0; i2 < 3; ++i2) {
            for (int i3 = 0; i3 < 3; ++i3) {
                iz iz2 = mq2.b(i3, i2);
                if (iz2 == null) continue;
                boolean bl2 = false;
                for (iz iz3 : arrayList) {
                    if (iz2.c != iz3.c || iz3.i() != -1 && iz2.i() != iz3.i()) continue;
                    bl2 = true;
                    arrayList.remove(iz3);
                    break;
                }
                if (bl2) continue;
                return false;
            }
        }
        return arrayList.isEmpty();
    }

    public iz b(mq mq2) {
        return this.a.k();
    }

    public int a() {
        return this.b.size();
    }
}

