/*
 * Decompiled with CFR 0.152.
 */
class xg
extends Thread {
    final /* synthetic */ pf a;

    xg(pf pf2) {
        this.a = pf2;
    }

    public void run() {
        try {
            Thread.sleep(5000L);
            if (pf.g(this.a).isAlive()) {
                try {
                    pf.g(this.a).stop();
                }
                catch (Throwable throwable) {
                    // empty catch block
                }
            }
            if (pf.h(this.a).isAlive()) {
                try {
                    pf.h(this.a).stop();
                }
                catch (Throwable throwable) {}
            }
        }
        catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        }
    }
}

