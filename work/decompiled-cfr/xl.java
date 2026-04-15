/*
 * Decompiled with CFR 0.152.
 */
import java.io.IOException;

class xl
extends Thread {
    final /* synthetic */ pf a;

    xl(pf pf2, String string) {
        this.a = pf2;
        super(string);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public void run() {
        Object object = pf.a;
        synchronized (object) {
            ++pf.c;
        }
        try {
            while (pf.a(this.a)) {
                while (pf.d(this.a)) {
                }
                try {
                    xl.sleep(100L);
                }
                catch (InterruptedException interruptedException) {
                    // empty catch block
                }
                try {
                    if (pf.e(this.a) == null) continue;
                    pf.e(this.a).flush();
                }
                catch (IOException iOException) {
                    if (!pf.f(this.a)) {
                        pf.a(this.a, iOException);
                    }
                    iOException.printStackTrace();
                }
            }
            return;
        }
        finally {
            object = pf.a;
            synchronized (object) {
                --pf.c;
            }
        }
    }
}

