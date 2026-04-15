/*
 * Decompiled with CFR 0.152.
 */
package paulscode.sound;

import paulscode.sound.SimpleThread;
import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.SoundSystemLogger;

public class CommandThread
extends SimpleThread {
    protected SoundSystemLogger logger = SoundSystemConfig.getLogger();
    private SoundSystem soundSystem;
    protected String className = "CommandThread";

    public CommandThread(SoundSystem soundSystem) {
        this.soundSystem = soundSystem;
    }

    protected void cleanup() {
        this.kill();
        this.logger = null;
        this.soundSystem = null;
        super.cleanup();
    }

    public void run() {
        long l2;
        long l3 = l2 = System.currentTimeMillis();
        if (this.soundSystem == null) {
            this.errorMessage("SoundSystem was null in method run().", 0);
            this.cleanup();
            return;
        }
        this.snooze(3600000L);
        while (!this.dying()) {
            this.soundSystem.ManageSources();
            this.soundSystem.CommandQueue(null);
            l3 = System.currentTimeMillis();
            if (!this.dying() && l3 - l2 > 10000L) {
                l2 = l3;
                this.soundSystem.removeTemporarySources();
            }
            if (this.dying()) continue;
            this.snooze(3600000L);
        }
        this.cleanup();
    }

    protected void message(String string, int n2) {
        this.logger.message(string, n2);
    }

    protected void importantMessage(String string, int n2) {
        this.logger.importantMessage(string, n2);
    }

    protected boolean errorCheck(boolean bl2, String string) {
        return this.logger.errorCheck(bl2, this.className, string, 0);
    }

    protected void errorMessage(String string, int n2) {
        this.logger.errorMessage(this.className, string, n2);
    }
}

