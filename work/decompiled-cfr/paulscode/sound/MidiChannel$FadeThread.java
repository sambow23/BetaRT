/*
 * Decompiled with CFR 0.152.
 */
package paulscode.sound;

import paulscode.sound.MidiChannel;
import paulscode.sound.MidiChannel$1;
import paulscode.sound.SimpleThread;

class MidiChannel$FadeThread
extends SimpleThread {
    final /* synthetic */ MidiChannel this$0;

    private MidiChannel$FadeThread(MidiChannel midiChannel) {
        this.this$0 = midiChannel;
    }

    public void run() {
        while (!this.dying()) {
            if (this.this$0.fadeOutGain == -1.0f && this.this$0.fadeInGain == 1.0f) {
                this.snooze(3600000L);
            }
            MidiChannel.access$100(this.this$0);
            this.snooze(50L);
        }
        this.cleanup();
    }

    /* synthetic */ MidiChannel$FadeThread(MidiChannel midiChannel, MidiChannel$1 midiChannel$1) {
        this(midiChannel);
    }
}

