/*
 * Decompiled with CFR 0.152.
 */
class xj
extends Thread {
    final /* synthetic */ pf a;

    xj(pf pf2, String string) {
        this.a = pf2;
        super(string);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void run() {
        Object object = pf.a;
        synchronized (object) {
            ++pf.b;
        }
        try {
            while (pf.a(this.a) && !pf.b(this.a)) {
                while (pf.c(this.a)) {
                }
                try {
                    xj.sleep(100L);
                }
                catch (InterruptedException interruptedException) {}
            }
        }
        finally {
            object = pf.a;
            synchronized (object) {
                --pf.b;
            }
        }
    }
}

