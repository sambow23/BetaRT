/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.BufferUtils
 *  org.lwjgl.openal.AL10
 */
package paulscode.sound.libraries;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.LinkedList;
import javax.sound.sampled.AudioFormat;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;
import paulscode.sound.Channel;
import paulscode.sound.libraries.LibraryLWJGLOpenAL;

public class ChannelLWJGLOpenAL
extends Channel {
    public IntBuffer ALSource;
    public int ALformat;
    public int sampleRate;
    ByteBuffer bufferBuffer = BufferUtils.createByteBuffer((int)0x500000);

    public ChannelLWJGLOpenAL(int n2, IntBuffer intBuffer) {
        super(n2);
        this.libraryType = LibraryLWJGLOpenAL.class;
        this.ALSource = intBuffer;
    }

    public void cleanup() {
        if (this.ALSource != null) {
            try {
                AL10.alSourceStop((IntBuffer)this.ALSource);
                AL10.alGetError();
            }
            catch (Exception exception) {
                // empty catch block
            }
            try {
                AL10.alDeleteSources((IntBuffer)this.ALSource);
                AL10.alGetError();
            }
            catch (Exception exception) {
                // empty catch block
            }
            this.ALSource.clear();
        }
        this.ALSource = null;
        super.cleanup();
    }

    public boolean attachBuffer(IntBuffer intBuffer) {
        if (this.errorCheck(this.channelType != 0, "Sound buffers may only be attached to normal sources.")) {
            return false;
        }
        AL10.alSourcei((int)this.ALSource.get(0), (int)4105, (int)intBuffer.get(0));
        return this.checkALError();
    }

    /*
     * Enabled aggressive block sorting
     */
    public void setAudioFormat(AudioFormat audioFormat) {
        int n2 = 0;
        if (audioFormat.getChannels() == 1) {
            if (audioFormat.getSampleSizeInBits() == 8) {
                n2 = 4352;
            } else {
                if (audioFormat.getSampleSizeInBits() != 16) {
                    this.errorMessage("Illegal sample size in method 'setAudioFormat'");
                    return;
                }
                n2 = 4353;
            }
        } else {
            if (audioFormat.getChannels() != 2) {
                this.errorMessage("Audio data neither mono nor stereo in method 'setAudioFormat'");
                return;
            }
            if (audioFormat.getSampleSizeInBits() == 8) {
                n2 = 4354;
            } else {
                if (audioFormat.getSampleSizeInBits() != 16) {
                    this.errorMessage("Illegal sample size in method 'setAudioFormat'");
                    return;
                }
                n2 = 4355;
            }
        }
        this.ALformat = n2;
        this.sampleRate = (int)audioFormat.getSampleRate();
    }

    public void setFormat(int n2, int n3) {
        this.ALformat = n2;
        this.sampleRate = n3;
    }

    public boolean preLoadBuffers(LinkedList linkedList) {
        IntBuffer intBuffer;
        int n2;
        if (this.errorCheck(this.channelType != 1, "Buffers may only be queued for streaming sources.")) {
            return false;
        }
        if (this.errorCheck(linkedList == null, "Buffer List null in method 'preLoadBuffers'")) {
            return false;
        }
        boolean bl2 = this.playing();
        if (bl2) {
            AL10.alSourceStop((int)this.ALSource.get(0));
            this.checkALError();
        }
        if ((n2 = AL10.alGetSourcei((int)this.ALSource.get(0), (int)4118)) > 0) {
            intBuffer = BufferUtils.createIntBuffer((int)n2);
            AL10.alGenBuffers((IntBuffer)intBuffer);
            if (this.errorCheck(this.checkALError(), "Error clearing stream buffers in method 'preLoadBuffers'")) {
                return false;
            }
            AL10.alSourceUnqueueBuffers((int)this.ALSource.get(0), (IntBuffer)intBuffer);
            if (this.errorCheck(this.checkALError(), "Error unqueuing stream buffers in method 'preLoadBuffers'")) {
                return false;
            }
        }
        if (bl2) {
            AL10.alSourcePlay((int)this.ALSource.get(0));
            this.checkALError();
        }
        intBuffer = BufferUtils.createIntBuffer((int)linkedList.size());
        AL10.alGenBuffers((IntBuffer)intBuffer);
        if (this.errorCheck(this.checkALError(), "Error generating stream buffers in method 'preLoadBuffers'")) {
            return false;
        }
        for (int i2 = 0; i2 < linkedList.size(); ++i2) {
            this.bufferBuffer.clear();
            this.bufferBuffer.put((byte[])linkedList.get(i2), 0, ((byte[])linkedList.get(i2)).length);
            this.bufferBuffer.flip();
            try {
                AL10.alBufferData((int)intBuffer.get(i2), (int)this.ALformat, (ByteBuffer)this.bufferBuffer, (int)this.sampleRate);
            }
            catch (Exception exception) {
                this.errorMessage("Error creating buffers in method 'preLoadBuffers'");
                this.printStackTrace(exception);
                return false;
            }
            if (!this.errorCheck(this.checkALError(), "Error creating buffers in method 'preLoadBuffers'")) continue;
            return false;
        }
        try {
            AL10.alSourceQueueBuffers((int)this.ALSource.get(0), (IntBuffer)intBuffer);
        }
        catch (Exception exception) {
            this.errorMessage("Error queuing buffers in method 'preLoadBuffers'");
            this.printStackTrace(exception);
            return false;
        }
        if (this.errorCheck(this.checkALError(), "Error queuing buffers in method 'preLoadBuffers'")) {
            return false;
        }
        AL10.alSourcePlay((int)this.ALSource.get(0));
        return !this.errorCheck(this.checkALError(), "Error playing source in method 'preLoadBuffers'");
    }

    public boolean queueBuffer(byte[] byArray) {
        if (this.errorCheck(this.channelType != 1, "Buffers may only be queued for streaming sources.")) {
            return false;
        }
        this.bufferBuffer.clear();
        this.bufferBuffer.put(byArray, 0, byArray.length);
        this.bufferBuffer.flip();
        IntBuffer intBuffer = BufferUtils.createIntBuffer((int)1);
        AL10.alSourceUnqueueBuffers((int)this.ALSource.get(0), (IntBuffer)intBuffer);
        if (this.checkALError()) {
            return false;
        }
        AL10.alBufferData((int)intBuffer.get(0), (int)this.ALformat, (ByteBuffer)this.bufferBuffer, (int)this.sampleRate);
        if (this.checkALError()) {
            return false;
        }
        AL10.alSourceQueueBuffers((int)this.ALSource.get(0), (IntBuffer)intBuffer);
        return !this.checkALError();
    }

    public int feedRawAudioData(byte[] byArray) {
        IntBuffer intBuffer;
        if (this.errorCheck(this.channelType != 1, "Raw audio data can only be fed to streaming sources.")) {
            return -1;
        }
        ByteBuffer byteBuffer = ByteBuffer.wrap(byArray, 0, byArray.length);
        int n2 = AL10.alGetSourcei((int)this.ALSource.get(0), (int)4118);
        if (n2 > 0) {
            intBuffer = BufferUtils.createIntBuffer((int)n2);
            AL10.alGenBuffers((IntBuffer)intBuffer);
            if (this.errorCheck(this.checkALError(), "Error clearing stream buffers in method 'feedRawAudioData'")) {
                return -1;
            }
            AL10.alSourceUnqueueBuffers((int)this.ALSource.get(0), (IntBuffer)intBuffer);
            if (this.errorCheck(this.checkALError(), "Error unqueuing stream buffers in method 'feedRawAudioData'")) {
                return -1;
            }
        } else {
            intBuffer = BufferUtils.createIntBuffer((int)1);
            AL10.alGenBuffers((IntBuffer)intBuffer);
            if (this.errorCheck(this.checkALError(), "Error generating stream buffers in method 'preLoadBuffers'")) {
                return -1;
            }
        }
        AL10.alBufferData((int)intBuffer.get(0), (int)this.ALformat, (ByteBuffer)byteBuffer, (int)this.sampleRate);
        if (this.checkALError()) {
            return -1;
        }
        AL10.alSourceQueueBuffers((int)this.ALSource.get(0), (IntBuffer)intBuffer);
        if (this.checkALError()) {
            return -1;
        }
        if (this.attachedSource != null && this.attachedSource.channel == this && this.attachedSource.active() && !this.playing()) {
            AL10.alSourcePlay((int)this.ALSource.get(0));
            this.checkALError();
        }
        return n2;
    }

    public int buffersProcessed() {
        if (this.channelType != 1) {
            return 0;
        }
        int n2 = AL10.alGetSourcei((int)this.ALSource.get(0), (int)4118);
        if (this.checkALError()) {
            return 0;
        }
        return n2;
    }

    public void flush() {
        if (this.channelType != 1) {
            return;
        }
        if (this.checkALError()) {
            return;
        }
        IntBuffer intBuffer = BufferUtils.createIntBuffer((int)1);
        for (int i2 = AL10.alGetSourcei((int)this.ALSource.get(0), (int)4117); i2 > 0; --i2) {
            try {
                AL10.alSourceUnqueueBuffers((int)this.ALSource.get(0), (IntBuffer)intBuffer);
            }
            catch (Exception exception) {
                return;
            }
            if (!this.checkALError()) continue;
            return;
        }
    }

    public void close() {
        try {
            AL10.alSourceStop((int)this.ALSource.get(0));
            AL10.alGetError();
        }
        catch (Exception exception) {
            // empty catch block
        }
        if (this.channelType == 1) {
            this.flush();
        }
    }

    public void play() {
        AL10.alSourcePlay((int)this.ALSource.get(0));
        this.checkALError();
    }

    public void pause() {
        AL10.alSourcePause((int)this.ALSource.get(0));
        this.checkALError();
    }

    public void stop() {
        AL10.alSourceStop((int)this.ALSource.get(0));
        this.checkALError();
    }

    public void rewind() {
        if (this.channelType == 1) {
            return;
        }
        AL10.alSourceRewind((int)this.ALSource.get(0));
        this.checkALError();
    }

    public boolean playing() {
        int n2 = AL10.alGetSourcei((int)this.ALSource.get(0), (int)4112);
        if (this.checkALError()) {
            return false;
        }
        return n2 == 4114;
    }

    private boolean checkALError() {
        switch (AL10.alGetError()) {
            case 0: {
                return false;
            }
            case 40961: {
                this.errorMessage("Invalid name parameter.");
                return true;
            }
            case 40962: {
                this.errorMessage("Invalid parameter.");
                return true;
            }
            case 40963: {
                this.errorMessage("Invalid enumerated parameter value.");
                return true;
            }
            case 40964: {
                this.errorMessage("Illegal call.");
                return true;
            }
            case 40965: {
                this.errorMessage("Unable to allocate memory.");
                return true;
            }
        }
        this.errorMessage("An unrecognized error occurred.");
        return true;
    }
}

