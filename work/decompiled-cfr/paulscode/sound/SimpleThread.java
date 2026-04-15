/*
 * Decompiled with CFR 0.152.
 */
package paulscode.sound;

import paulscode.sound.SimpleThread$1;

public class SimpleThread
extends Thread {
    private static final boolean GET = false;
    private static final boolean SET = true;
    private static final boolean XXX = false;
    private boolean alive = true;
    private boolean kill = false;

    protected void cleanup() {
        this.kill(true, true);
        this.alive(true, false);
    }

    public void run() {
        this.cleanup();
    }

    public void restart() {
        new SimpleThread$1(this).start();
    }

    private void rerun() {
        this.kill(true, true);
        while (this.alive(false, false)) {
            this.snooze(100L);
        }
        this.alive(true, true);
        this.kill(true, false);
        this.run();
    }

    public boolean alive() {
        return this.alive(false, false);
    }

    public void kill() {
        this.kill(true, true);
    }

    protected boolean dying() {
        return this.kill(false, false);
    }

    private synchronized boolean alive(boolean bl2, boolean bl3) {
        if (bl2) {
            this.alive = bl3;
        }
        return this.alive;
    }

    private synchronized boolean kill(boolean bl2, boolean bl3) {
        if (bl2) {
            this.kill = bl3;
        }
        return this.kill;
    }

    protected void snooze(long l2) {
        try {
            Thread.sleep(l2);
        }
        catch (InterruptedException interruptedException) {
            // empty catch block
        }
    }

    static /* synthetic */ void access$000(SimpleThread simpleThread) {
        simpleThread.rerun();
    }
}

