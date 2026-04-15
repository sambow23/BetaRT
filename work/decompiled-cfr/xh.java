/*
 * Decompiled with CFR 0.152.
 */
class xh
extends Thread {
    final /* synthetic */ pf a;

    xh(pf pf2) {
        this.a = pf2;
    }

    public void run() {
        try {
            Thread.sleep(2000L);
            if (pf.a(this.a)) {
                pf.h(this.a).interrupt();
                this.a.a("disconnect.closed", new Object[0]);
            }
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
    }
}

