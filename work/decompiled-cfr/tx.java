/*
 * Decompiled with CFR 0.152.
 */
class tx
extends Thread {
    final /* synthetic */ bd a;

    tx(bd bd2) {
        this.a = bd2;
    }

    public void run() {
        while (bd.a(this.a)) {
            this.a.d();
            try {
                Thread.sleep(1L);
            }
            catch (Exception exception) {}
        }
    }
}

