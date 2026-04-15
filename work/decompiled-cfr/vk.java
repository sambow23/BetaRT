/*
 * Decompiled with CFR 0.152.
 */
class vk
extends Thread {
    final /* synthetic */ cj a;

    vk(cj cj2) {
        this.a = cj2;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void run() {
        try {
            if (cj.a(this.a) != null) {
                cj.a(this.a, cj.a(this.a), cj.b(this.a), cj.c(this.a), cj.d(this.a));
            } else if (cj.b(this.a).exists()) {
                cj.a(this.a, cj.a(this.a, cj.b(this.a), cj.c(this.a), cj.d(this.a)));
            }
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
        finally {
            cj.a(this.a, false);
        }
    }
}

