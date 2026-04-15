/*
 * Decompiled with CFR 0.152.
 */
package paulscode.sound;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.sound.sampled.AudioFormat;
import paulscode.sound.Channel;
import paulscode.sound.FilenameURL;
import paulscode.sound.ListenerData;
import paulscode.sound.MidiChannel;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.SoundSystemLogger;
import paulscode.sound.Source;
import paulscode.sound.StreamThread;

public class Library {
    private SoundSystemLogger logger = SoundSystemConfig.getLogger();
    protected ListenerData listener;
    protected HashMap bufferMap = new HashMap();
    protected HashMap sourceMap = new HashMap();
    private MidiChannel midiChannel;
    protected List streamingChannels;
    protected List normalChannels;
    private String[] streamingChannelSourceNames;
    private String[] normalChannelSourceNames;
    private int nextStreamingChannel = 0;
    private int nextNormalChannel = 0;
    protected StreamThread streamThread;

    public Library() {
        this.listener = new ListenerData(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f, 1.0f, 0.0f, 0.0f);
        this.streamingChannels = new LinkedList();
        this.normalChannels = new LinkedList();
        this.streamingChannelSourceNames = new String[SoundSystemConfig.getNumberStreamingChannels()];
        this.normalChannelSourceNames = new String[SoundSystemConfig.getNumberNormalChannels()];
        this.streamThread = new StreamThread();
        this.streamThread.start();
    }

    public void cleanup() {
        this.streamThread.kill();
        this.streamThread.interrupt();
        for (int i2 = 0; i2 < 50 && this.streamThread.alive(); ++i2) {
            try {
                Thread.sleep(100L);
                continue;
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        if (this.streamThread.alive()) {
            this.errorMessage("Stream thread did not die!");
            this.message("Ignoring errors... continuing clean-up.");
        }
        if (this.midiChannel != null) {
            this.midiChannel.cleanup();
            this.midiChannel = null;
        }
        Channel channel = null;
        if (this.streamingChannels != null) {
            while (!this.streamingChannels.isEmpty()) {
                channel = (Channel)this.streamingChannels.remove(0);
                channel.close();
                channel.cleanup();
                channel = null;
            }
            this.streamingChannels.clear();
            this.streamingChannels = null;
        }
        if (this.normalChannels != null) {
            while (!this.normalChannels.isEmpty()) {
                channel = (Channel)this.normalChannels.remove(0);
                channel.close();
                channel.cleanup();
                channel = null;
            }
            this.normalChannels.clear();
            this.normalChannels = null;
        }
        Set set = this.sourceMap.keySet();
        for (String string : set) {
            Source source = (Source)this.sourceMap.get(string);
            if (source == null) continue;
            source.cleanup();
        }
        this.sourceMap.clear();
        this.sourceMap = null;
        this.listener = null;
        this.streamThread = null;
    }

    public void init() {
        int n2;
        Channel channel = null;
        for (n2 = 0; n2 < SoundSystemConfig.getNumberStreamingChannels() && (channel = this.createChannel(1)) != null; ++n2) {
            this.streamingChannels.add(channel);
        }
        for (n2 = 0; n2 < SoundSystemConfig.getNumberNormalChannels() && (channel = this.createChannel(0)) != null; ++n2) {
            this.normalChannels.add(channel);
        }
    }

    public static boolean libraryCompatible() {
        return true;
    }

    protected Channel createChannel(int n2) {
        return null;
    }

    public boolean loadSound(FilenameURL filenameURL) {
        return true;
    }

    public void unloadSound(String string) {
        this.bufferMap.remove(string);
    }

    public void rawDataStream(AudioFormat audioFormat, boolean bl2, String string, float f2, float f3, float f4, int n2, float f5) {
        this.sourceMap.put(string, new Source(audioFormat, bl2, string, f2, f3, f4, n2, f5));
    }

    public void newSource(boolean bl2, boolean bl3, boolean bl4, String string, FilenameURL filenameURL, float f2, float f3, float f4, int n2, float f5) {
        this.sourceMap.put(string, new Source(bl2, bl3, bl4, string, filenameURL, null, f2, f3, f4, n2, f5, false));
    }

    public void quickPlay(boolean bl2, boolean bl3, boolean bl4, String string, FilenameURL filenameURL, float f2, float f3, float f4, int n2, float f5, boolean bl5) {
        this.sourceMap.put(string, new Source(bl2, bl3, bl4, string, filenameURL, null, f2, f3, f4, n2, f5, bl5));
    }

    public void setTemporary(String string, boolean bl2) {
        Source source = (Source)this.sourceMap.get(string);
        if (source != null) {
            source.setTemporary(bl2);
        }
    }

    public void setPosition(String string, float f2, float f3, float f4) {
        Source source = (Source)this.sourceMap.get(string);
        if (source != null) {
            source.setPosition(f2, f3, f4);
        }
    }

    public void setPriority(String string, boolean bl2) {
        Source source = (Source)this.sourceMap.get(string);
        if (source != null) {
            source.setPriority(bl2);
        }
    }

    public void setLooping(String string, boolean bl2) {
        Source source = (Source)this.sourceMap.get(string);
        if (source != null) {
            source.setLooping(bl2);
        }
    }

    public void setAttenuation(String string, int n2) {
        Source source = (Source)this.sourceMap.get(string);
        if (source != null) {
            source.setAttenuation(n2);
        }
    }

    public void setDistOrRoll(String string, float f2) {
        Source source = (Source)this.sourceMap.get(string);
        if (source != null) {
            source.setDistOrRoll(f2);
        }
    }

    public int feedRawAudioData(String string, byte[] byArray) {
        if (string == null || string.equals("")) {
            this.errorMessage("Sourcename not specified in method 'feedRawAudioData'");
            return -1;
        }
        if (this.midiSourcename(string)) {
            this.errorMessage("Raw audio data can not be fed to the MIDI channel.");
            return -1;
        }
        Source source = (Source)this.sourceMap.get(string);
        if (source == null) {
            this.errorMessage("Source '" + string + "' not found in " + "method 'feedRawAudioData'");
        }
        return this.feedRawAudioData(source, byArray);
    }

    public int feedRawAudioData(Source source, byte[] byArray) {
        if (source == null) {
            this.errorMessage("Source parameter null in method 'feedRawAudioData'");
            return -1;
        }
        if (!source.toStream) {
            this.errorMessage("Only a streaming source may be specified in method 'feedRawAudioData'");
            return -1;
        }
        if (!source.rawDataStream) {
            this.errorMessage("Streaming source already associated with a file or URL in method'feedRawAudioData'");
            return -1;
        }
        if (!source.playing() || source.channel == null) {
            Channel channel = source.channel != null && source.channel.attachedSource == source ? source.channel : this.getNextChannel(source);
            int n2 = source.feedRawAudioData(channel, byArray);
            channel.attachedSource = source;
            this.streamThread.watch(source);
            this.streamThread.interrupt();
            return n2;
        }
        return source.feedRawAudioData(source.channel, byArray);
    }

    public void play(String string) {
        if (string == null || string.equals("")) {
            this.errorMessage("Sourcename not specified in method 'play'");
            return;
        }
        if (this.midiSourcename(string)) {
            this.midiChannel.play();
        } else {
            Source source = (Source)this.sourceMap.get(string);
            if (source == null) {
                this.errorMessage("Source '" + string + "' not found in " + "method 'play'");
            }
            this.play(source);
        }
    }

    public void play(Source source) {
        if (source == null) {
            return;
        }
        if (source.rawDataStream) {
            return;
        }
        if (!source.active()) {
            return;
        }
        if (!source.playing()) {
            Channel channel = this.getNextChannel(source);
            if (source != null && channel != null) {
                if (source.channel != null && source.channel.attachedSource != source) {
                    source.channel = null;
                }
                channel.attachedSource = source;
                source.play(channel);
                if (source.toStream) {
                    this.streamThread.watch(source);
                    this.streamThread.interrupt();
                }
            }
        }
    }

    public void stop(String string) {
        if (string == null || string.equals("")) {
            this.errorMessage("Sourcename not specified in method 'stop'");
            return;
        }
        if (this.midiSourcename(string)) {
            this.midiChannel.stop();
        } else {
            Source source = (Source)this.sourceMap.get(string);
            if (source != null) {
                source.stop();
            }
        }
    }

    public void pause(String string) {
        if (string == null || string.equals("")) {
            this.errorMessage("Sourcename not specified in method 'stop'");
            return;
        }
        if (this.midiSourcename(string)) {
            this.midiChannel.pause();
        } else {
            Source source = (Source)this.sourceMap.get(string);
            if (source != null) {
                source.pause();
            }
        }
    }

    public void rewind(String string) {
        if (this.midiSourcename(string)) {
            this.midiChannel.rewind();
        } else {
            Source source = (Source)this.sourceMap.get(string);
            if (source != null) {
                source.rewind();
            }
        }
    }

    public void flush(String string) {
        if (this.midiSourcename(string)) {
            this.errorMessage("You can not flush the MIDI channel");
        } else {
            Source source = (Source)this.sourceMap.get(string);
            if (source != null) {
                source.flush();
            }
        }
    }

    public void cull(String string) {
        Source source = (Source)this.sourceMap.get(string);
        if (source != null) {
            source.cull();
        }
    }

    public void activate(String string) {
        Source source = (Source)this.sourceMap.get(string);
        if (source != null) {
            source.activate();
            if (source.toPlay) {
                this.play(source);
            }
        }
    }

    public void setMasterVolume(float f2) {
        SoundSystemConfig.setMasterGain(f2);
        if (this.midiChannel != null) {
            this.midiChannel.resetGain();
        }
    }

    public void setVolume(String string, float f2) {
        if (this.midiSourcename(string)) {
            this.midiChannel.setVolume(f2);
        } else {
            Source source = (Source)this.sourceMap.get(string);
            if (source != null) {
                float f3 = f2;
                if (f3 < 0.0f) {
                    f3 = 0.0f;
                } else if (f3 > 1.0f) {
                    f3 = 1.0f;
                }
                source.sourceVolume = f3;
                source.positionChanged();
            }
        }
    }

    public float getVolume(String string) {
        if (this.midiSourcename(string)) {
            return this.midiChannel.getVolume();
        }
        Source source = (Source)this.sourceMap.get(string);
        if (source != null) {
            return source.sourceVolume;
        }
        return 0.0f;
    }

    public void setPitch(String string, float f2) {
        Source source;
        if (!this.midiSourcename(string) && (source = (Source)this.sourceMap.get(string)) != null) {
            float f3 = f2;
            if (f3 < 0.5f) {
                f3 = 0.5f;
            } else if (f3 > 2.0f) {
                f3 = 2.0f;
            }
            source.setPitch(f3);
            source.positionChanged();
        }
    }

    public float getPitch(String string) {
        Source source;
        if (!this.midiSourcename(string) && (source = (Source)this.sourceMap.get(string)) != null) {
            return source.getPitch();
        }
        return 1.0f;
    }

    public void moveListener(float f2, float f3, float f4) {
        this.setListenerPosition(this.listener.position.x + f2, this.listener.position.y + f3, this.listener.position.z + f4);
    }

    public void setListenerPosition(float f2, float f3, float f4) {
        this.listener.setPosition(f2, f3, f4);
        Set set = this.sourceMap.keySet();
        for (String string : set) {
            Source source = (Source)this.sourceMap.get(string);
            if (source == null) continue;
            source.positionChanged();
        }
    }

    public void turnListener(float f2) {
        this.setListenerAngle(this.listener.angle + f2);
    }

    public void setListenerAngle(float f2) {
        this.listener.setAngle(f2);
        Set set = this.sourceMap.keySet();
        for (String string : set) {
            Source source = (Source)this.sourceMap.get(string);
            if (source == null) continue;
            source.positionChanged();
        }
    }

    public void setListenerOrientation(float f2, float f3, float f4, float f5, float f6, float f7) {
        this.listener.setOrientation(f2, f3, f4, f5, f6, f7);
        Set set = this.sourceMap.keySet();
        for (String string : set) {
            Source source = (Source)this.sourceMap.get(string);
            if (source == null) continue;
            source.positionChanged();
        }
    }

    public void setListenerData(ListenerData listenerData) {
        this.listener.setData(listenerData);
    }

    public void copySources(HashMap hashMap) {
        if (hashMap == null) {
            return;
        }
        Set set = hashMap.keySet();
        Iterator iterator = set.iterator();
        this.sourceMap.clear();
        while (iterator.hasNext()) {
            String string = (String)iterator.next();
            Source source = (Source)hashMap.get(string);
            if (source == null) continue;
            this.loadSound(source.filenameURL);
            this.sourceMap.put(string, new Source(source, null));
        }
    }

    public void removeSource(String string) {
        Source source = (Source)this.sourceMap.get(string);
        if (source != null) {
            source.cleanup();
        }
        this.sourceMap.remove(string);
    }

    public void removeTemporarySources() {
        Set set = this.sourceMap.keySet();
        Iterator iterator = set.iterator();
        while (iterator.hasNext()) {
            String string = (String)iterator.next();
            Source source = (Source)this.sourceMap.get(string);
            if (source == null || !source.temporary || source.playing()) continue;
            source.cleanup();
            iterator.remove();
        }
    }

    private Channel getNextChannel(Source source) {
        Source source2;
        String string;
        int n2;
        String[] stringArray;
        List list;
        int n3;
        if (source == null) {
            return null;
        }
        String string2 = source.sourcename;
        if (string2 == null) {
            return null;
        }
        if (source.toStream) {
            n3 = this.nextStreamingChannel;
            list = this.streamingChannels;
            stringArray = this.streamingChannelSourceNames;
        } else {
            n3 = this.nextNormalChannel;
            list = this.normalChannels;
            stringArray = this.normalChannelSourceNames;
        }
        int n4 = list.size();
        for (n2 = 0; n2 < n4; ++n2) {
            if (!string2.equals(stringArray[n2])) continue;
            return (Channel)list.get(n2);
        }
        int n5 = n3;
        for (n2 = 0; n2 < n4; ++n2) {
            string = stringArray[n5];
            source2 = string == null ? null : (Source)this.sourceMap.get(string);
            if (source2 == null || !source2.playing()) {
                if (source.toStream) {
                    this.nextStreamingChannel = n5 + 1;
                    if (this.nextStreamingChannel >= n4) {
                        this.nextStreamingChannel = 0;
                    }
                } else {
                    this.nextNormalChannel = n5 + 1;
                    if (this.nextNormalChannel >= n4) {
                        this.nextNormalChannel = 0;
                    }
                }
                stringArray[n5] = string2;
                return (Channel)list.get(n5);
            }
            if (++n5 < n4) continue;
            n5 = 0;
        }
        n5 = n3;
        for (n2 = 0; n2 < n4; ++n2) {
            string = stringArray[n5];
            source2 = string == null ? null : (Source)this.sourceMap.get(string);
            if (source2 == null || !source2.playing() || !source2.priority) {
                if (source.toStream) {
                    this.nextStreamingChannel = n5 + 1;
                    if (this.nextStreamingChannel >= n4) {
                        this.nextStreamingChannel = 0;
                    }
                } else {
                    this.nextNormalChannel = n5 + 1;
                    if (this.nextNormalChannel >= n4) {
                        this.nextNormalChannel = 0;
                    }
                }
                stringArray[n5] = string2;
                return (Channel)list.get(n5);
            }
            if (++n5 < n4) continue;
            n5 = 0;
        }
        return null;
    }

    public void replaySources() {
        Set set = this.sourceMap.keySet();
        for (String string : set) {
            Source source = (Source)this.sourceMap.get(string);
            if (source == null || !source.toPlay || source.playing()) continue;
            this.play(string);
            source.toPlay = false;
        }
    }

    public void queueSound(String string, FilenameURL filenameURL) {
        if (this.midiSourcename(string)) {
            this.midiChannel.queueSound(filenameURL);
        } else {
            Source source = (Source)this.sourceMap.get(string);
            if (source != null) {
                source.queueSound(filenameURL);
            }
        }
    }

    public void dequeueSound(String string, String string2) {
        if (this.midiSourcename(string)) {
            this.midiChannel.dequeueSound(string2);
        } else {
            Source source = (Source)this.sourceMap.get(string);
            if (source != null) {
                source.dequeueSound(string2);
            }
        }
    }

    public void fadeOut(String string, FilenameURL filenameURL, long l2) {
        if (this.midiSourcename(string)) {
            this.midiChannel.fadeOut(filenameURL, l2);
        } else {
            Source source = (Source)this.sourceMap.get(string);
            if (source != null) {
                source.fadeOut(filenameURL, l2);
            }
        }
    }

    public void fadeOutIn(String string, FilenameURL filenameURL, long l2, long l3) {
        if (this.midiSourcename(string)) {
            this.midiChannel.fadeOutIn(filenameURL, l2, l3);
        } else {
            Source source = (Source)this.sourceMap.get(string);
            if (source != null) {
                source.fadeOutIn(filenameURL, l2, l3);
            }
        }
    }

    public void checkFadeVolumes() {
        Source source;
        Channel channel;
        if (this.midiChannel != null) {
            this.midiChannel.resetGain();
        }
        for (int i2 = 0; i2 < this.streamingChannels.size(); ++i2) {
            channel = (Channel)this.streamingChannels.get(i2);
            if (channel == null || (source = channel.attachedSource) == null) continue;
            source.checkFadeOut();
        }
        channel = null;
        source = null;
    }

    public void loadMidi(boolean bl2, String string, FilenameURL filenameURL) {
        if (filenameURL == null) {
            this.errorMessage("Filename/URL not specified in method 'loadMidi'.");
            return;
        }
        if (!filenameURL.getFilename().matches(".*[mM][iI][dD][iI]?$")) {
            this.errorMessage("Filename/identifier doesn't end in '.mid' or'.midi' in method loadMidi.");
            return;
        }
        if (this.midiChannel == null) {
            this.midiChannel = new MidiChannel(bl2, string, filenameURL);
        } else {
            this.midiChannel.switchSource(bl2, string, filenameURL);
        }
    }

    public void unloadMidi() {
        if (this.midiChannel != null) {
            this.midiChannel.cleanup();
        }
        this.midiChannel = null;
    }

    public boolean midiSourcename(String string) {
        if (this.midiChannel == null || string == null) {
            return false;
        }
        if (this.midiChannel.getSourcename() == null || string.equals("")) {
            return false;
        }
        return string.equals(this.midiChannel.getSourcename());
    }

    public Source getSource(String string) {
        return (Source)this.sourceMap.get(string);
    }

    public MidiChannel getMidiChannel() {
        return this.midiChannel;
    }

    public void setMidiChannel(MidiChannel midiChannel) {
        if (this.midiChannel != null && this.midiChannel != midiChannel) {
            this.midiChannel.cleanup();
        }
        this.midiChannel = midiChannel;
    }

    public void listenerMoved() {
        Set set = this.sourceMap.keySet();
        for (String string : set) {
            Source source = (Source)this.sourceMap.get(string);
            if (source == null) continue;
            source.listenerMoved();
        }
    }

    public HashMap getSources() {
        return this.sourceMap;
    }

    public ListenerData getListenerData() {
        return this.listener;
    }

    public static String getTitle() {
        return "No Sound";
    }

    public static String getDescription() {
        return "Silent Mode";
    }

    public String getClassName() {
        return "Library";
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

