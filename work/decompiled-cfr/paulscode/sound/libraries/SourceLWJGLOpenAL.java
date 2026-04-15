/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.BufferUtils
 *  org.lwjgl.openal.AL10
 */
package paulscode.sound.libraries;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.LinkedList;
import javax.sound.sampled.AudioFormat;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;
import paulscode.sound.Channel;
import paulscode.sound.FilenameURL;
import paulscode.sound.SoundBuffer;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.Source;
import paulscode.sound.libraries.ChannelLWJGLOpenAL;
import paulscode.sound.libraries.LibraryLWJGLOpenAL;

public class SourceLWJGLOpenAL
extends Source {
    private ChannelLWJGLOpenAL channelOpenAL;
    private IntBuffer myBuffer;
    private FloatBuffer listenerPosition;
    private FloatBuffer sourcePosition;
    private FloatBuffer sourceVelocity;

    public SourceLWJGLOpenAL(FloatBuffer floatBuffer, IntBuffer intBuffer, boolean bl2, boolean bl3, boolean bl4, String string, FilenameURL filenameURL, SoundBuffer soundBuffer, float f2, float f3, float f4, int n2, float f5, boolean bl5) {
        super(bl2, bl3, bl4, string, filenameURL, soundBuffer, f2, f3, f4, n2, f5, bl5);
        this.channelOpenAL = (ChannelLWJGLOpenAL)this.channel;
        this.reverseByteOrder = true;
        if (this.codec != null) {
            this.codec.reverseByteOrder(true);
        }
        this.listenerPosition = floatBuffer;
        this.myBuffer = intBuffer;
        this.libraryType = LibraryLWJGLOpenAL.class;
        this.pitch = 1.0f;
        this.resetALInformation();
    }

    public SourceLWJGLOpenAL(FloatBuffer floatBuffer, IntBuffer intBuffer, Source source, SoundBuffer soundBuffer) {
        super(source, soundBuffer);
        this.channelOpenAL = (ChannelLWJGLOpenAL)this.channel;
        this.reverseByteOrder = true;
        if (this.codec != null) {
            this.codec.reverseByteOrder(true);
        }
        this.listenerPosition = floatBuffer;
        this.myBuffer = intBuffer;
        this.libraryType = LibraryLWJGLOpenAL.class;
        this.pitch = 1.0f;
        this.resetALInformation();
    }

    public SourceLWJGLOpenAL(FloatBuffer floatBuffer, AudioFormat audioFormat, boolean bl2, String string, float f2, float f3, float f4, int n2, float f5) {
        super(audioFormat, bl2, string, f2, f3, f4, n2, f5);
        this.channelOpenAL = (ChannelLWJGLOpenAL)this.channel;
        this.reverseByteOrder = true;
        this.listenerPosition = floatBuffer;
        this.libraryType = LibraryLWJGLOpenAL.class;
        this.pitch = 1.0f;
        this.resetALInformation();
    }

    public void cleanup() {
        super.cleanup();
    }

    public void changeSource(FloatBuffer floatBuffer, IntBuffer intBuffer, boolean bl2, boolean bl3, boolean bl4, String string, FilenameURL filenameURL, SoundBuffer soundBuffer, float f2, float f3, float f4, int n2, float f5, boolean bl5) {
        super.changeSource(bl2, bl3, bl4, string, filenameURL, soundBuffer, f2, f3, f4, n2, f5, bl5);
        this.reverseByteOrder = true;
        this.listenerPosition = floatBuffer;
        this.myBuffer = intBuffer;
        this.pitch = 1.0f;
        this.resetALInformation();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public boolean incrementSoundSequence() {
        if (!this.toStream) {
            this.errorMessage("Method 'incrementSoundSequence' may only be used for streaming sources.");
            return false;
        }
        Object object = this.soundSequenceLock;
        synchronized (object) {
            AudioFormat audioFormat;
            if (this.soundSequenceQueue == null) return false;
            if (this.soundSequenceQueue.size() <= 0) return false;
            this.filenameURL = (FilenameURL)this.soundSequenceQueue.remove(0);
            if (this.codec != null) {
                this.codec.cleanup();
            }
            this.codec = SoundSystemConfig.getCodec(this.filenameURL.getFilename());
            if (this.codec == null) return true;
            this.codec.reverseByteOrder(true);
            if (this.codec.getAudioFormat() == null) {
                this.codec.initialize(this.filenameURL.getURL());
            }
            if ((audioFormat = this.codec.getAudioFormat()) == null) {
                this.errorMessage("Audio Format null in method 'incrementSoundSequence'");
                return false;
            }
            int n2 = 0;
            if (audioFormat.getChannels() == 1) {
                if (audioFormat.getSampleSizeInBits() == 8) {
                    n2 = 4352;
                } else {
                    if (audioFormat.getSampleSizeInBits() != 16) {
                        this.errorMessage("Illegal sample size in method 'incrementSoundSequence'");
                        return false;
                    }
                    n2 = 4353;
                }
            } else {
                if (audioFormat.getChannels() != 2) {
                    this.errorMessage("Audio data neither mono nor stereo in method 'incrementSoundSequence'");
                    return false;
                }
                if (audioFormat.getSampleSizeInBits() == 8) {
                    n2 = 4354;
                } else {
                    if (audioFormat.getSampleSizeInBits() != 16) {
                        this.errorMessage("Illegal sample size in method 'incrementSoundSequence'");
                        return false;
                    }
                    n2 = 4355;
                }
            }
            this.channelOpenAL.setFormat(n2, (int)audioFormat.getSampleRate());
            this.preLoad = true;
            return true;
        }
    }

    public void listenerMoved() {
        this.positionChanged();
    }

    public void setPosition(float f2, float f3, float f4) {
        super.setPosition(f2, f3, f4);
        if (this.sourcePosition == null) {
            this.resetALInformation();
        } else {
            this.positionChanged();
        }
        this.sourcePosition.put(0, f2);
        this.sourcePosition.put(1, f3);
        this.sourcePosition.put(2, f4);
        if (this.channel != null && this.channel.attachedSource == this && this.channelOpenAL != null && this.channelOpenAL.ALSource != null) {
            AL10.alSource((int)this.channelOpenAL.ALSource.get(0), (int)4100, (FloatBuffer)this.sourcePosition);
            this.checkALError();
        }
    }

    public void positionChanged() {
        this.calculateDistance();
        this.calculateGain();
        if (this.channel != null && this.channel.attachedSource == this && this.channelOpenAL != null && this.channelOpenAL.ALSource != null) {
            AL10.alSourcef((int)this.channelOpenAL.ALSource.get(0), (int)4106, (float)(this.gain * this.sourceVolume * Math.abs(this.fadeOutGain) * this.fadeInGain));
            this.checkALError();
        }
        this.checkPitch();
    }

    private void checkPitch() {
        if (this.channel != null && this.channel.attachedSource == this && LibraryLWJGLOpenAL.alPitchSupported() && this.channelOpenAL != null && this.channelOpenAL.ALSource != null) {
            AL10.alSourcef((int)this.channelOpenAL.ALSource.get(0), (int)4099, (float)this.pitch);
            this.checkALError();
        }
    }

    public void setLooping(boolean bl2) {
        super.setLooping(bl2);
        if (this.channel != null && this.channel.attachedSource == this && this.channelOpenAL != null && this.channelOpenAL.ALSource != null) {
            if (bl2) {
                AL10.alSourcei((int)this.channelOpenAL.ALSource.get(0), (int)4103, (int)1);
            } else {
                AL10.alSourcei((int)this.channelOpenAL.ALSource.get(0), (int)4103, (int)0);
            }
            this.checkALError();
        }
    }

    public void setAttenuation(int n2) {
        super.setAttenuation(n2);
        if (this.channel != null && this.channel.attachedSource == this && this.channelOpenAL != null && this.channelOpenAL.ALSource != null) {
            if (n2 == 1) {
                AL10.alSourcef((int)this.channelOpenAL.ALSource.get(0), (int)4129, (float)this.distOrRoll);
            } else {
                AL10.alSourcef((int)this.channelOpenAL.ALSource.get(0), (int)4129, (float)0.0f);
            }
            this.checkALError();
        }
    }

    public void setDistOrRoll(float f2) {
        super.setDistOrRoll(f2);
        if (this.channel != null && this.channel.attachedSource == this && this.channelOpenAL != null && this.channelOpenAL.ALSource != null) {
            if (this.attModel == 1) {
                AL10.alSourcef((int)this.channelOpenAL.ALSource.get(0), (int)4129, (float)f2);
            } else {
                AL10.alSourcef((int)this.channelOpenAL.ALSource.get(0), (int)4129, (float)0.0f);
            }
            this.checkALError();
        }
    }

    public void setPitch(float f2) {
        super.setPitch(f2);
        this.checkPitch();
    }

    /*
     * Enabled aggressive block sorting
     */
    public void play(Channel channel) {
        boolean bl2;
        if (!this.active()) {
            if (this.toLoop) {
                this.toPlay = true;
            }
            return;
        }
        if (channel == null) {
            this.errorMessage("Unable to play source, because channel was null");
            return;
        }
        boolean bl3 = bl2 = this.channel != channel;
        if (this.channel != null && this.channel.attachedSource != this) {
            bl2 = true;
        }
        boolean bl4 = this.paused();
        super.play(channel);
        this.channelOpenAL = (ChannelLWJGLOpenAL)this.channel;
        if (bl2) {
            this.setPosition(this.position.x, this.position.y, this.position.z);
            this.checkPitch();
            if (this.channelOpenAL != null && this.channelOpenAL.ALSource != null) {
                if (LibraryLWJGLOpenAL.alPitchSupported()) {
                    AL10.alSourcef((int)this.channelOpenAL.ALSource.get(0), (int)4099, (float)this.pitch);
                    this.checkALError();
                }
                AL10.alSource((int)this.channelOpenAL.ALSource.get(0), (int)4100, (FloatBuffer)this.sourcePosition);
                this.checkALError();
                AL10.alSource((int)this.channelOpenAL.ALSource.get(0), (int)4102, (FloatBuffer)this.sourceVelocity);
                this.checkALError();
                if (this.attModel == 1) {
                    AL10.alSourcef((int)this.channelOpenAL.ALSource.get(0), (int)4129, (float)this.distOrRoll);
                } else {
                    AL10.alSourcef((int)this.channelOpenAL.ALSource.get(0), (int)4129, (float)0.0f);
                }
                this.checkALError();
                if (this.toLoop && !this.toStream) {
                    AL10.alSourcei((int)this.channelOpenAL.ALSource.get(0), (int)4103, (int)1);
                } else {
                    AL10.alSourcei((int)this.channelOpenAL.ALSource.get(0), (int)4103, (int)0);
                }
                this.checkALError();
            }
            if (!this.toStream) {
                if (this.myBuffer == null) {
                    this.errorMessage("No sound buffer to play");
                    return;
                }
                this.channelOpenAL.attachBuffer(this.myBuffer);
            }
        }
        if (!this.playing()) {
            if (this.toStream && !bl4) {
                AudioFormat audioFormat;
                if (this.codec == null) {
                    this.errorMessage("Decoder null in method 'play'");
                    return;
                }
                if (this.codec.getAudioFormat() == null) {
                    this.codec.initialize(this.filenameURL.getURL());
                }
                if ((audioFormat = this.codec.getAudioFormat()) == null) {
                    this.errorMessage("Audio Format null in method 'play'");
                    return;
                }
                int n2 = 0;
                if (audioFormat.getChannels() == 1) {
                    if (audioFormat.getSampleSizeInBits() == 8) {
                        n2 = 4352;
                    } else {
                        if (audioFormat.getSampleSizeInBits() != 16) {
                            this.errorMessage("Illegal sample size in method 'play'");
                            return;
                        }
                        n2 = 4353;
                    }
                } else {
                    if (audioFormat.getChannels() != 2) {
                        this.errorMessage("Audio data neither mono nor stereo in method 'play'");
                        return;
                    }
                    if (audioFormat.getSampleSizeInBits() == 8) {
                        n2 = 4354;
                    } else {
                        if (audioFormat.getSampleSizeInBits() != 16) {
                            this.errorMessage("Illegal sample size in method 'play'");
                            return;
                        }
                        n2 = 4355;
                    }
                }
                this.channelOpenAL.setFormat(n2, (int)audioFormat.getSampleRate());
                this.preLoad = true;
            }
            this.channel.play();
            if (this.pitch != 1.0f) {
                this.checkPitch();
            }
        }
    }

    public boolean preLoad() {
        if (this.codec == null) {
            return false;
        }
        this.codec.initialize(this.filenameURL.getURL());
        LinkedList<byte[]> linkedList = new LinkedList<byte[]>();
        for (int i2 = 0; i2 < SoundSystemConfig.getNumberStreamingBuffers(); ++i2) {
            this.soundBuffer = this.codec.read();
            if (this.soundBuffer == null || this.soundBuffer.audioData == null) break;
            linkedList.add(this.soundBuffer.audioData);
        }
        this.positionChanged();
        this.channel.preLoadBuffers(linkedList);
        this.preLoad = false;
        return true;
    }

    private void resetALInformation() {
        this.sourcePosition = BufferUtils.createFloatBuffer((int)3).put(new float[]{this.position.x, this.position.y, this.position.z});
        this.sourceVelocity = BufferUtils.createFloatBuffer((int)3).put(new float[]{0.0f, 0.0f, 0.0f});
        this.sourcePosition.flip();
        this.sourceVelocity.flip();
        this.positionChanged();
    }

    private void calculateDistance() {
        if (this.listenerPosition != null) {
            double d2 = this.position.x - this.listenerPosition.get(0);
            double d3 = this.position.y - this.listenerPosition.get(1);
            double d4 = this.position.z - this.listenerPosition.get(2);
            this.distanceFromListener = (float)Math.sqrt(d2 * d2 + d3 * d3 + d4 * d4);
        }
    }

    private void calculateGain() {
        if (this.attModel == 2) {
            this.gain = this.distanceFromListener <= 0.0f ? 1.0f : (this.distanceFromListener >= this.distOrRoll ? 0.0f : 1.0f - this.distanceFromListener / this.distOrRoll);
            if (this.gain > 1.0f) {
                this.gain = 1.0f;
            }
            if (this.gain < 0.0f) {
                this.gain = 0.0f;
            }
        } else {
            this.gain = 1.0f;
        }
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

