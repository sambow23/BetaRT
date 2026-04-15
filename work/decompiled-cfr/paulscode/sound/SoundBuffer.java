/*
 * Decompiled with CFR 0.152.
 */
package paulscode.sound;

import javax.sound.sampled.AudioFormat;

public class SoundBuffer {
    public byte[] audioData;
    public AudioFormat audioFormat;

    public SoundBuffer(byte[] byArray, AudioFormat audioFormat) {
        this.audioData = byArray;
        this.audioFormat = audioFormat;
    }

    public void cleanup() {
        this.audioData = null;
        this.audioFormat = null;
    }

    public void trimData(int n2) {
        if (this.audioData == null || n2 == 0) {
            this.audioData = null;
        } else if (this.audioData.length > n2) {
            byte[] byArray = new byte[n2];
            System.arraycopy(this.audioData, 0, byArray, 0, n2);
            this.audioData = byArray;
        }
    }
}

