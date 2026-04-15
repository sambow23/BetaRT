/*
 * Decompiled with CFR 0.152.
 */
import java.util.Map;

class vj
extends Thread {
    final /* synthetic */ Map a;
    final /* synthetic */ cj b;

    vj(cj cj2, Map map) {
        this.b = cj2;
        this.a = map;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void run() {
        try {
            cj.a(this.b, this.a, cj.e(this.b), cj.f(this.b), cj.g(this.b));
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
        finally {
            cj.a(this.b, false);
        }
    }
}

