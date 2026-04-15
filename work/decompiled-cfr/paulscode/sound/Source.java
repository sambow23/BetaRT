/*
 * Decompiled with CFR 0.152.
 */
package paulscode.sound;

import java.net.URL;
import java.util.LinkedList;
import java.util.ListIterator;
import javax.sound.sampled.AudioFormat;
import paulscode.sound.Channel;
import paulscode.sound.FilenameURL;
import paulscode.sound.ICodec;
import paulscode.sound.Library;
import paulscode.sound.SoundBuffer;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.SoundSystemLogger;
import paulscode.sound.Vector3D;

public class Source {
    protected Class libraryType = Library.class;
    private static final boolean GET = false;
    private static final boolean SET = true;
    private static final boolean XXX = false;
    private SoundSystemLogger logger;
    public boolean rawDataStream = false;
    public AudioFormat rawDataFormat = null;
    public boolean temporary = false;
    public boolean priority = false;
    public boolean toStream = false;
    public boolean toLoop = false;
    public boolean toPlay = false;
    public String sourcename = "";
    public FilenameURL filenameURL = null;
    public Vector3D position;
    public int attModel = 0;
    public float distOrRoll = 0.0f;
    public float gain = 1.0f;
    public float sourceVolume = 1.0f;
    protected float pitch = 1.0f;
    public float distanceFromListener = 0.0f;
    public Channel channel = null;
    private boolean active = true;
    private boolean stopped = true;
    private boolean paused = false;
    protected SoundBuffer soundBuffer = null;
    protected ICodec codec = null;
    protected boolean reverseByteOrder = false;
    protected LinkedList soundSequenceQueue = null;
    protected final Object soundSequenceLock = new Object();
    public boolean preLoad = false;
    protected float fadeOutGain = -1.0f;
    protected float fadeInGain = 1.0f;
    protected long fadeOutMilis = 0L;
    protected long fadeInMilis = 0L;
    protected long lastFadeCheck = 0L;

    public Source(boolean bl2, boolean bl3, boolean bl4, String string, FilenameURL filenameURL, SoundBuffer soundBuffer, float f2, float f3, float f4, int n2, float f5, boolean bl5) {
        this.logger = SoundSystemConfig.getLogger();
        this.priority = bl2;
        this.toStream = bl3;
        this.toLoop = bl4;
        this.sourcename = string;
        this.filenameURL = filenameURL;
        this.soundBuffer = soundBuffer;
        this.position = new Vector3D(f2, f3, f4);
        this.attModel = n2;
        this.distOrRoll = f5;
        this.temporary = bl5;
        if (bl3 && filenameURL != null) {
            this.codec = SoundSystemConfig.getCodec(filenameURL.getFilename());
        }
    }

    public Source(Source source, SoundBuffer soundBuffer) {
        this.logger = SoundSystemConfig.getLogger();
        this.priority = source.priority;
        this.toStream = source.toStream;
        this.toLoop = source.toLoop;
        this.sourcename = source.sourcename;
        this.filenameURL = source.filenameURL;
        this.position = source.position.clone();
        this.attModel = source.attModel;
        this.distOrRoll = source.distOrRoll;
        this.temporary = source.temporary;
        this.sourceVolume = source.sourceVolume;
        this.rawDataStream = source.rawDataStream;
        this.rawDataFormat = source.rawDataFormat;
        this.soundBuffer = soundBuffer;
        if (this.toStream && this.filenameURL != null) {
            this.codec = SoundSystemConfig.getCodec(this.filenameURL.getFilename());
        }
    }

    public Source(AudioFormat audioFormat, boolean bl2, String string, float f2, float f3, float f4, int n2, float f5) {
        this.logger = SoundSystemConfig.getLogger();
        this.priority = bl2;
        this.toStream = true;
        this.toLoop = false;
        this.sourcename = string;
        this.filenameURL = null;
        this.soundBuffer = null;
        this.position = new Vector3D(f2, f3, f4);
        this.attModel = n2;
        this.distOrRoll = f5;
        this.temporary = false;
        this.rawDataStream = true;
        this.rawDataFormat = audioFormat;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void cleanup() {
        if (this.codec != null) {
            this.codec.cleanup();
        }
        Object object = this.soundSequenceLock;
        synchronized (object) {
            if (this.soundSequenceQueue != null) {
                this.soundSequenceQueue.clear();
            }
            this.soundSequenceQueue = null;
        }
        this.sourcename = null;
        this.filenameURL = null;
        this.position = null;
        this.soundBuffer = null;
        this.codec = null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void queueSound(FilenameURL filenameURL) {
        if (!this.toStream) {
            this.errorMessage("Method 'queueSound' may only be used for streaming and MIDI sources.");
            return;
        }
        if (filenameURL == null) {
            this.errorMessage("File not specified in method 'queueSound'");
            return;
        }
        Object object = this.soundSequenceLock;
        synchronized (object) {
            if (this.soundSequenceQueue == null) {
                this.soundSequenceQueue = new LinkedList();
            }
            this.soundSequenceQueue.add(filenameURL);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void dequeueSound(String string) {
        if (!this.toStream) {
            this.errorMessage("Method 'dequeueSound' may only be used for streaming and MIDI sources.");
            return;
        }
        if (string == null || string.equals("")) {
            this.errorMessage("Filename not specified in method 'dequeueSound'");
            return;
        }
        Object object = this.soundSequenceLock;
        synchronized (object) {
            if (this.soundSequenceQueue != null) {
                this.soundSequenceQueue.remove(string);
            }
        }
        object = this.soundSequenceLock;
        synchronized (object) {
            if (this.soundSequenceQueue != null) {
                ListIterator listIterator = this.soundSequenceQueue.listIterator();
                while (listIterator.hasNext()) {
                    if (!((FilenameURL)listIterator.next()).getFilename().equals(string)) continue;
                    listIterator.remove();
                    break;
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void fadeOut(FilenameURL filenameURL, long l2) {
        if (!this.toStream) {
            this.errorMessage("Method 'fadeOut' may only be used for streaming and MIDI sources.");
            return;
        }
        if (l2 < 0L) {
            this.errorMessage("Miliseconds may not be negative in method 'fadeOut'.");
            return;
        }
        this.fadeOutMilis = l2;
        this.fadeInMilis = 0L;
        this.fadeOutGain = 1.0f;
        this.lastFadeCheck = System.currentTimeMillis();
        Object object = this.soundSequenceLock;
        synchronized (object) {
            if (this.soundSequenceQueue != null) {
                this.soundSequenceQueue.clear();
            }
            if (filenameURL != null) {
                if (this.soundSequenceQueue == null) {
                    this.soundSequenceQueue = new LinkedList();
                }
                this.soundSequenceQueue.add(filenameURL);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void fadeOutIn(FilenameURL filenameURL, long l2, long l3) {
        if (!this.toStream) {
            this.errorMessage("Method 'fadeOutIn' may only be used for streaming and MIDI sources.");
            return;
        }
        if (filenameURL == null) {
            this.errorMessage("Filename/URL not specified in method 'fadeOutIn'.");
            return;
        }
        if (l2 < 0L || l3 < 0L) {
            this.errorMessage("Miliseconds may not be negative in method 'fadeOutIn'.");
            return;
        }
        this.fadeOutMilis = l2;
        this.fadeInMilis = l3;
        this.fadeOutGain = 1.0f;
        this.lastFadeCheck = System.currentTimeMillis();
        Object object = this.soundSequenceLock;
        synchronized (object) {
            if (this.soundSequenceQueue == null) {
                this.soundSequenceQueue = new LinkedList();
            }
            this.soundSequenceQueue.clear();
            this.soundSequenceQueue.add(filenameURL);
        }
    }

    public boolean checkFadeOut() {
        if (!this.toStream) {
            return false;
        }
        if (this.fadeOutGain == -1.0f && this.fadeInGain == 1.0f) {
            return false;
        }
        long l2 = System.currentTimeMillis();
        long l3 = l2 - this.lastFadeCheck;
        this.lastFadeCheck = l2;
        if (this.fadeOutGain >= 0.0f) {
            if (this.fadeOutMilis == 0L) {
                this.fadeOutGain = 0.0f;
                this.fadeInGain = 0.0f;
                if (!this.incrementSoundSequence()) {
                    this.stop();
                }
                this.positionChanged();
                this.preLoad = true;
                return false;
            }
            float f2 = (float)l3 / (float)this.fadeOutMilis;
            this.fadeOutGain -= f2;
            if (this.fadeOutGain <= 0.0f) {
                this.fadeOutGain = -1.0f;
                this.fadeInGain = 0.0f;
                if (!this.incrementSoundSequence()) {
                    this.stop();
                }
                this.positionChanged();
                this.preLoad = true;
                return false;
            }
            this.positionChanged();
            return true;
        }
        if (this.fadeInGain < 1.0f) {
            this.fadeOutGain = -1.0f;
            if (this.fadeInMilis == 0L) {
                this.fadeOutGain = -1.0f;
                this.fadeInGain = 1.0f;
            } else {
                float f3 = (float)l3 / (float)this.fadeInMilis;
                this.fadeInGain += f3;
                if (this.fadeInGain >= 1.0f) {
                    this.fadeOutGain = -1.0f;
                    this.fadeInGain = 1.0f;
                }
            }
            this.positionChanged();
            return true;
        }
        return false;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean incrementSoundSequence() {
        if (!this.toStream) {
            this.errorMessage("Method 'incrementSoundSequence' may only be used for streaming and MIDI sources.");
            return false;
        }
        Object object = this.soundSequenceLock;
        synchronized (object) {
            if (this.soundSequenceQueue != null && this.soundSequenceQueue.size() > 0) {
                this.filenameURL = (FilenameURL)this.soundSequenceQueue.remove(0);
                if (this.codec != null) {
                    this.codec.cleanup();
                }
                this.codec = SoundSystemConfig.getCodec(this.filenameURL.getFilename());
                return true;
            }
        }
        return false;
    }

    public void setTemporary(boolean bl2) {
        this.temporary = bl2;
    }

    public void listenerMoved() {
    }

    public void setPosition(float f2, float f3, float f4) {
        this.position.x = f2;
        this.position.y = f3;
        this.position.z = f4;
    }

    public void positionChanged() {
    }

    public void setPriority(boolean bl2) {
        this.priority = bl2;
    }

    public void setLooping(boolean bl2) {
        this.toLoop = bl2;
    }

    public void setAttenuation(int n2) {
        this.attModel = n2;
    }

    public void setDistOrRoll(float f2) {
        this.distOrRoll = f2;
    }

    public float getDistanceFromListener() {
        return this.distanceFromListener;
    }

    public void setPitch(float f2) {
        float f3 = f2;
        if (f3 < 0.5f) {
            f3 = 0.5f;
        } else if (f3 > 2.0f) {
            f3 = 2.0f;
        }
        this.pitch = f3;
    }

    public float getPitch() {
        return this.pitch;
    }

    public boolean reverseByteOrderRequired() {
        return this.reverseByteOrder;
    }

    public void changeSource(boolean bl2, boolean bl3, boolean bl4, String string, FilenameURL filenameURL, SoundBuffer soundBuffer, float f2, float f3, float f4, int n2, float f5, boolean bl5) {
        this.priority = bl2;
        this.toStream = bl3;
        this.toLoop = bl4;
        this.sourcename = string;
        this.filenameURL = filenameURL;
        this.soundBuffer = soundBuffer;
        this.position.x = f2;
        this.position.y = f3;
        this.position.z = f4;
        this.attModel = n2;
        this.distOrRoll = f5;
        this.temporary = bl5;
    }

    public int feedRawAudioData(Channel channel, byte[] byArray) {
        if (!this.active(false, false)) {
            this.toPlay = true;
            return -1;
        }
        if (this.channel != channel) {
            this.channel = channel;
            this.channel.close();
            this.channel.setAudioFormat(this.rawDataFormat);
            this.positionChanged();
        }
        this.stopped(true, false);
        this.paused(true, false);
        return this.channel.feedRawAudioData(byArray);
    }

    public void play(Channel channel) {
        if (!this.active(false, false)) {
            if (this.toLoop) {
                this.toPlay = true;
            }
            return;
        }
        if (this.channel != channel) {
            this.channel = channel;
            this.channel.close();
        }
        this.stopped(true, false);
        this.paused(true, false);
    }

    public boolean stream() {
        if (this.channel == null) {
            return false;
        }
        if (this.preLoad) {
            if (this.rawDataStream) {
                this.preLoad = false;
            } else {
                return this.preLoad();
            }
        }
        if (this.rawDataStream) {
            if (this.stopped() || this.paused()) {
                return true;
            }
            if (this.channel.buffersProcessed() > 0) {
                this.channel.processBuffer();
            }
        } else {
            if (this.codec == null) {
                return false;
            }
            if (this.stopped()) {
                return false;
            }
            if (this.paused()) {
                return true;
            }
            int n2 = this.channel.buffersProcessed();
            SoundBuffer soundBuffer = null;
            for (int i2 = 0; i2 < n2; ++i2) {
                soundBuffer = this.codec.read();
                if (soundBuffer != null) {
                    if (soundBuffer.audioData != null) {
                        this.channel.queueBuffer(soundBuffer.audioData);
                    }
                    soundBuffer.cleanup();
                    soundBuffer = null;
                }
                if (!this.codec.endOfStream()) continue;
                return false;
            }
        }
        return true;
    }

    public boolean preLoad() {
        if (this.channel == null) {
            return false;
        }
        if (this.codec == null) {
            return false;
        }
        URL uRL = this.filenameURL.getURL();
        this.codec.initialize(uRL);
        SoundBuffer soundBuffer = null;
        for (int i2 = 0; i2 < SoundSystemConfig.getNumberStreamingBuffers(); ++i2) {
            soundBuffer = this.codec.read();
            if (soundBuffer == null) continue;
            if (this.soundBuffer.audioData != null) {
                this.channel.queueBuffer(this.soundBuffer.audioData);
            }
            soundBuffer.cleanup();
            soundBuffer = null;
        }
        return true;
    }

    public void pause() {
        this.toPlay = false;
        this.paused(true, true);
        if (this.channel != null) {
            this.channel.pause();
        } else {
            this.errorMessage("Channel null in method 'pause'");
        }
    }

    public void stop() {
        this.toPlay = false;
        this.stopped(true, true);
        this.paused(true, false);
        if (this.channel != null) {
            this.channel.stop();
        } else {
            this.errorMessage("Channel null in method 'stop'");
        }
    }

    public void rewind() {
        if (this.paused(false, false)) {
            this.stop();
        }
        if (this.channel != null) {
            boolean bl2 = this.playing();
            this.channel.rewind();
            if (this.toStream && bl2) {
                this.stop();
                this.play(this.channel);
            }
        } else {
            this.errorMessage("Channel null in method 'rewind'");
        }
    }

    public void flush() {
        if (this.channel != null) {
            this.channel.flush();
        } else {
            this.errorMessage("Channel null in method 'flush'");
        }
    }

    public void cull() {
        if (!this.active(false, false)) {
            return;
        }
        if (this.playing() && this.toLoop) {
            this.toPlay = true;
        }
        if (this.rawDataStream) {
            this.toPlay = true;
        }
        this.active(true, false);
        if (this.channel != null) {
            this.channel.close();
        }
        this.channel = null;
    }

    public void activate() {
        this.active(true, true);
    }

    public boolean active() {
        return this.active(false, false);
    }

    public boolean playing() {
        if (this.channel == null || this.channel.attachedSource != this) {
            return false;
        }
        if (this.paused() || this.stopped()) {
            return false;
        }
        return this.channel.playing();
    }

    public boolean stopped() {
        return this.stopped(false, false);
    }

    public boolean paused() {
        return this.paused(false, false);
    }

    private synchronized boolean active(boolean bl2, boolean bl3) {
        if (bl2) {
            this.active = bl3;
        }
        return this.active;
    }

    private synchronized boolean stopped(boolean bl2, boolean bl3) {
        if (bl2) {
            this.stopped = bl3;
        }
        return this.stopped;
    }

    private synchronized boolean paused(boolean bl2, boolean bl3) {
        if (bl2) {
            this.paused = bl3;
        }
        return this.paused;
    }

    public String getClassName() {
        String string = SoundSystemConfig.getLibraryTitle(this.libraryType);
        if (string.equals("No Sound")) {
            return "Source";
        }
        return "Source" + string;
    }

    protected void message(String string) {
        this.logger.message(string, 0);
    }

    protected void importantMessage(String string) {
        this.logger.importantMessage(string, 0);
    }

    protected boolean errorCheck(boolean bl2, String string) {
        return this.logger.errorCheck(bl2, this.getClassName(), string, 0);
    }

    protected void errorMessage(String string) {
        this.logger.errorMessage(this.getClassName(), string, 0);
    }

    protected void printStackTrace(Exception exception) {
        this.logger.printStackTrace(exception, 1);
    }
}

