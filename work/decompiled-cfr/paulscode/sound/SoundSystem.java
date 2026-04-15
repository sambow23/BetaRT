/*
 * Decompiled with CFR 0.152.
 */
package paulscode.sound;

import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.Set;
import javax.sound.sampled.AudioFormat;
import paulscode.sound.CommandObject;
import paulscode.sound.CommandThread;
import paulscode.sound.FilenameURL;
import paulscode.sound.Library;
import paulscode.sound.ListenerData;
import paulscode.sound.MidiChannel;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.SoundSystemException;
import paulscode.sound.SoundSystemLogger;
import paulscode.sound.Source;

public class SoundSystem {
    private static final boolean GET = false;
    private static final boolean SET = true;
    private static final boolean XXX = false;
    protected SoundSystemLogger logger = SoundSystemConfig.getLogger();
    protected Library soundLibrary;
    protected List commandQueue;
    private List sourcePlayList;
    protected CommandThread commandThread;
    public Random randomNumberGenerator;
    protected String className = "SoundSystem";
    private static Class currentLibrary = null;
    private static boolean initialized = false;
    private static SoundSystemException lastException = null;

    public SoundSystem() {
        if (this.logger == null) {
            this.logger = new SoundSystemLogger();
            SoundSystemConfig.setLogger(this.logger);
        }
        this.linkDefaultLibrariesAndCodecs();
        LinkedList linkedList = SoundSystemConfig.getLibraries();
        if (linkedList != null) {
            ListIterator listIterator = linkedList.listIterator();
            while (listIterator.hasNext()) {
                Class clazz = (Class)listIterator.next();
                try {
                    this.init(clazz);
                    return;
                }
                catch (SoundSystemException soundSystemException) {
                    this.logger.printExceptionMessage(soundSystemException, 1);
                }
            }
        }
        try {
            this.init(Library.class);
            return;
        }
        catch (SoundSystemException soundSystemException) {
            this.logger.printExceptionMessage(soundSystemException, 1);
            return;
        }
    }

    public SoundSystem(Class clazz) {
        if (this.logger == null) {
            this.logger = new SoundSystemLogger();
            SoundSystemConfig.setLogger(this.logger);
        }
        this.linkDefaultLibrariesAndCodecs();
        this.init(clazz);
    }

    protected void linkDefaultLibrariesAndCodecs() {
    }

    protected void init(Class clazz) {
        this.message("", 0);
        this.message("Starting up " + this.className + "...", 0);
        this.randomNumberGenerator = new Random();
        this.commandQueue = new LinkedList();
        this.sourcePlayList = new LinkedList();
        this.commandThread = new CommandThread(this);
        this.commandThread.start();
        SoundSystem.snooze(200L);
        this.newLibrary(clazz);
        this.message("", 0);
    }

    public void cleanup() {
        boolean bl2 = false;
        this.message("", 0);
        this.message(this.className + " shutting down...", 0);
        try {
            this.commandThread.kill();
            this.commandThread.interrupt();
        }
        catch (Exception exception) {
            bl2 = true;
        }
        if (!bl2) {
            for (int i2 = 0; i2 < 50 && this.commandThread.alive(); ++i2) {
                SoundSystem.snooze(100L);
            }
        }
        if (bl2 || this.commandThread.alive()) {
            this.errorMessage("Command thread did not die!", 0);
            this.message("Ignoring errors... continuing clean-up.", 0);
        }
        SoundSystem.initialized(true, false);
        SoundSystem.currentLibrary(true, null);
        try {
            if (this.soundLibrary != null) {
                this.soundLibrary.cleanup();
            }
        }
        catch (Exception exception) {
            this.errorMessage("Problem during Library.cleanup()!", 0);
            this.message("Ignoring errors... continuing clean-up.", 0);
        }
        try {
            if (this.commandQueue != null) {
                this.commandQueue.clear();
            }
        }
        catch (Exception exception) {
            this.errorMessage("Unable to clear the command queue!", 0);
            this.message("Ignoring errors... continuing clean-up.", 0);
        }
        try {
            if (this.sourcePlayList != null) {
                this.sourcePlayList.clear();
            }
        }
        catch (Exception exception) {
            this.errorMessage("Unable to clear the source management list!", 0);
            this.message("Ignoring errors... continuing clean-up.", 0);
        }
        this.randomNumberGenerator = null;
        this.soundLibrary = null;
        this.commandQueue = null;
        this.sourcePlayList = null;
        this.commandThread = null;
        this.importantMessage("Author: Paul Lamb, www.paulscode.com", 1);
        this.message("", 0);
    }

    public void interruptCommandThread() {
        if (this.commandThread == null) {
            this.errorMessage("Command Thread null in method 'interruptCommandThread'", 0);
            return;
        }
        this.commandThread.interrupt();
    }

    public void loadSound(String string) {
        this.CommandQueue(new CommandObject(2, new FilenameURL(string)));
        this.commandThread.interrupt();
    }

    public void loadSound(URL uRL, String string) {
        this.CommandQueue(new CommandObject(2, new FilenameURL(uRL, string)));
        this.commandThread.interrupt();
    }

    public void unloadSound(String string) {
        this.CommandQueue(new CommandObject(4, string));
        this.commandThread.interrupt();
    }

    public void queueSound(String string, String string2) {
        this.CommandQueue(new CommandObject(5, string, new FilenameURL(string2)));
        this.commandThread.interrupt();
    }

    public void queueSound(String string, URL uRL, String string2) {
        this.CommandQueue(new CommandObject(5, string, new FilenameURL(uRL, string2)));
        this.commandThread.interrupt();
    }

    public void dequeueSound(String string, String string2) {
        this.CommandQueue(new CommandObject(6, string, string2));
        this.commandThread.interrupt();
    }

    public void fadeOut(String string, String string2, long l2) {
        FilenameURL filenameURL = null;
        if (string2 != null) {
            filenameURL = new FilenameURL(string2);
        }
        this.CommandQueue(new CommandObject(7, string, filenameURL, l2));
        this.commandThread.interrupt();
    }

    public void fadeOut(String string, URL uRL, String string2, long l2) {
        FilenameURL filenameURL = null;
        if (uRL != null && string2 != null) {
            filenameURL = new FilenameURL(uRL, string2);
        }
        this.CommandQueue(new CommandObject(7, string, filenameURL, l2));
        this.commandThread.interrupt();
    }

    public void fadeOutIn(String string, String string2, long l2, long l3) {
        this.CommandQueue(new CommandObject(8, string, new FilenameURL(string2), l2, l3));
        this.commandThread.interrupt();
    }

    public void fadeOutIn(String string, URL uRL, String string2, long l2, long l3) {
        this.CommandQueue(new CommandObject(8, string, new FilenameURL(uRL, string2), l2, l3));
        this.commandThread.interrupt();
    }

    public void checkFadeVolumes() {
        this.CommandQueue(new CommandObject(9));
        this.commandThread.interrupt();
    }

    public void backgroundMusic(String string, String string2, boolean bl2) {
        this.CommandQueue(new CommandObject(12, true, true, bl2, string, new FilenameURL(string2), 0.0f, 0.0f, 0.0f, 0, 0.0f, false));
        this.CommandQueue(new CommandObject(21, string));
        this.commandThread.interrupt();
    }

    public void backgroundMusic(String string, URL uRL, String string2, boolean bl2) {
        this.CommandQueue(new CommandObject(12, true, true, bl2, string, new FilenameURL(uRL, string2), 0.0f, 0.0f, 0.0f, 0, 0.0f, false));
        this.CommandQueue(new CommandObject(21, string));
        this.commandThread.interrupt();
    }

    public void newSource(boolean bl2, String string, String string2, boolean bl3, float f2, float f3, float f4, int n2, float f5) {
        this.CommandQueue(new CommandObject(10, bl2, false, bl3, string, new FilenameURL(string2), f2, f3, f4, n2, f5));
        this.commandThread.interrupt();
    }

    public void newSource(boolean bl2, String string, URL uRL, String string2, boolean bl3, float f2, float f3, float f4, int n2, float f5) {
        this.CommandQueue(new CommandObject(10, bl2, false, bl3, string, new FilenameURL(uRL, string2), f2, f3, f4, n2, f5));
        this.commandThread.interrupt();
    }

    public void newStreamingSource(boolean bl2, String string, String string2, boolean bl3, float f2, float f3, float f4, int n2, float f5) {
        this.CommandQueue(new CommandObject(10, bl2, true, bl3, string, new FilenameURL(string2), f2, f3, f4, n2, f5));
        this.commandThread.interrupt();
    }

    public void newStreamingSource(boolean bl2, String string, URL uRL, String string2, boolean bl3, float f2, float f3, float f4, int n2, float f5) {
        this.CommandQueue(new CommandObject(10, bl2, true, bl3, string, new FilenameURL(uRL, string2), f2, f3, f4, n2, f5));
        this.commandThread.interrupt();
    }

    public void rawDataStream(AudioFormat audioFormat, boolean bl2, String string, float f2, float f3, float f4, int n2, float f5) {
        this.CommandQueue(new CommandObject(11, audioFormat, bl2, string, f2, f3, f4, n2, f5));
        this.commandThread.interrupt();
    }

    public String quickPlay(boolean bl2, String string, boolean bl3, float f2, float f3, float f4, int n2, float f5) {
        String string2 = "Source_" + this.randomNumberGenerator.nextInt() + "_" + this.randomNumberGenerator.nextInt();
        this.CommandQueue(new CommandObject(12, bl2, false, bl3, string2, new FilenameURL(string), f2, f3, f4, n2, f5, true));
        this.CommandQueue(new CommandObject(21, string2));
        this.commandThread.interrupt();
        return string2;
    }

    public String quickPlay(boolean bl2, URL uRL, String string, boolean bl3, float f2, float f3, float f4, int n2, float f5) {
        String string2 = "Source_" + this.randomNumberGenerator.nextInt() + "_" + this.randomNumberGenerator.nextInt();
        this.CommandQueue(new CommandObject(12, bl2, false, bl3, string2, new FilenameURL(uRL, string), f2, f3, f4, n2, f5, true));
        this.CommandQueue(new CommandObject(21, string2));
        this.commandThread.interrupt();
        return string2;
    }

    public String quickStream(boolean bl2, String string, boolean bl3, float f2, float f3, float f4, int n2, float f5) {
        String string2 = "Source_" + this.randomNumberGenerator.nextInt() + "_" + this.randomNumberGenerator.nextInt();
        this.CommandQueue(new CommandObject(12, bl2, true, bl3, string2, new FilenameURL(string), f2, f3, f4, n2, f5, true));
        this.CommandQueue(new CommandObject(21, string2));
        this.commandThread.interrupt();
        return string2;
    }

    public String quickStream(boolean bl2, URL uRL, String string, boolean bl3, float f2, float f3, float f4, int n2, float f5) {
        String string2 = "Source_" + this.randomNumberGenerator.nextInt() + "_" + this.randomNumberGenerator.nextInt();
        this.CommandQueue(new CommandObject(12, bl2, true, bl3, string2, new FilenameURL(uRL, string), f2, f3, f4, n2, f5, true));
        this.CommandQueue(new CommandObject(21, string2));
        this.commandThread.interrupt();
        return string2;
    }

    public void setPosition(String string, float f2, float f3, float f4) {
        this.CommandQueue(new CommandObject(13, string, f2, f3, f4));
        this.commandThread.interrupt();
    }

    public void setVolume(String string, float f2) {
        this.CommandQueue(new CommandObject(14, string, f2));
        this.commandThread.interrupt();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public float getVolume(String string) {
        Object object = SoundSystemConfig.THREAD_SYNC;
        synchronized (object) {
            if (this.soundLibrary != null) {
                return this.soundLibrary.getVolume(string);
            }
            return 0.0f;
        }
    }

    public void setPitch(String string, float f2) {
        this.CommandQueue(new CommandObject(15, string, f2));
        this.commandThread.interrupt();
    }

    public float getPitch(String string) {
        if (this.soundLibrary != null) {
            return this.soundLibrary.getPitch(string);
        }
        return 1.0f;
    }

    public void setPriority(String string, boolean bl2) {
        this.CommandQueue(new CommandObject(16, string, bl2));
        this.commandThread.interrupt();
    }

    public void setLooping(String string, boolean bl2) {
        this.CommandQueue(new CommandObject(17, string, bl2));
        this.commandThread.interrupt();
    }

    public void setAttenuation(String string, int n2) {
        this.CommandQueue(new CommandObject(18, string, n2));
        this.commandThread.interrupt();
    }

    public void setDistOrRoll(String string, float f2) {
        this.CommandQueue(new CommandObject(19, string, f2));
        this.commandThread.interrupt();
    }

    public void feedRawAudioData(String string, byte[] byArray) {
        this.CommandQueue(new CommandObject(22, string, byArray));
        this.commandThread.interrupt();
    }

    public void play(String string) {
        this.CommandQueue(new CommandObject(21, string));
        this.commandThread.interrupt();
    }

    public void pause(String string) {
        this.CommandQueue(new CommandObject(23, string));
        this.commandThread.interrupt();
    }

    public void stop(String string) {
        this.CommandQueue(new CommandObject(24, string));
        this.commandThread.interrupt();
    }

    public void rewind(String string) {
        this.CommandQueue(new CommandObject(25, string));
        this.commandThread.interrupt();
    }

    public void flush(String string) {
        this.CommandQueue(new CommandObject(26, string));
        this.commandThread.interrupt();
    }

    public void cull(String string) {
        this.CommandQueue(new CommandObject(27, string));
        this.commandThread.interrupt();
    }

    public void activate(String string) {
        this.CommandQueue(new CommandObject(28, string));
        this.commandThread.interrupt();
    }

    public void setTemporary(String string, boolean bl2) {
        this.CommandQueue(new CommandObject(29, string, bl2));
        this.commandThread.interrupt();
    }

    public void removeSource(String string) {
        this.CommandQueue(new CommandObject(30, string));
        this.commandThread.interrupt();
    }

    public void moveListener(float f2, float f3, float f4) {
        this.CommandQueue(new CommandObject(31, f2, f3, f4));
        this.commandThread.interrupt();
    }

    public void setListenerPosition(float f2, float f3, float f4) {
        this.CommandQueue(new CommandObject(32, f2, f3, f4));
        this.commandThread.interrupt();
    }

    public void turnListener(float f2) {
        this.CommandQueue(new CommandObject(33, f2));
        this.commandThread.interrupt();
    }

    public void setListenerAngle(float f2) {
        this.CommandQueue(new CommandObject(34, f2));
        this.commandThread.interrupt();
    }

    public void setListenerOrientation(float f2, float f3, float f4, float f5, float f6, float f7) {
        this.CommandQueue(new CommandObject(35, f2, f3, f4, f5, f6, f7));
        this.commandThread.interrupt();
    }

    public void setMasterVolume(float f2) {
        this.CommandQueue(new CommandObject(36, f2));
        this.commandThread.interrupt();
    }

    public float getMasterVolume() {
        return SoundSystemConfig.getMasterGain();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public ListenerData getListenerData() {
        Object object = SoundSystemConfig.THREAD_SYNC;
        synchronized (object) {
            return this.soundLibrary.getListenerData();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean switchLibrary(Class clazz) {
        Object object = SoundSystemConfig.THREAD_SYNC;
        synchronized (object) {
            SoundSystem.initialized(true, false);
            HashMap hashMap = null;
            ListenerData listenerData = null;
            boolean bl2 = false;
            MidiChannel midiChannel = null;
            FilenameURL filenameURL = null;
            String string = "";
            boolean bl3 = true;
            if (this.soundLibrary != null) {
                SoundSystem.currentLibrary(true, null);
                hashMap = this.copySources(this.soundLibrary.getSources());
                listenerData = this.soundLibrary.getListenerData();
                midiChannel = this.soundLibrary.getMidiChannel();
                if (midiChannel != null) {
                    bl2 = true;
                    bl3 = midiChannel.getLooping();
                    string = midiChannel.getSourcename();
                    filenameURL = midiChannel.getFilenameURL();
                }
                this.soundLibrary.cleanup();
                this.soundLibrary = null;
            }
            this.message("", 0);
            this.message("Switching to " + SoundSystemConfig.getLibraryTitle(clazz), 0);
            this.message("(" + SoundSystemConfig.getLibraryDescription(clazz) + ")", 1);
            try {
                this.soundLibrary = (Library)clazz.newInstance();
            }
            catch (InstantiationException instantiationException) {
                this.errorMessage("The specified library did not load properly", 1);
            }
            catch (IllegalAccessException illegalAccessException) {
                this.errorMessage("The specified library did not load properly", 1);
            }
            catch (ExceptionInInitializerError exceptionInInitializerError) {
                this.errorMessage("The specified library did not load properly", 1);
            }
            catch (SecurityException securityException) {
                this.errorMessage("The specified library did not load properly", 1);
            }
            if (this.errorCheck(this.soundLibrary == null, "Library null after initialization in method 'switchLibrary'", 1)) {
                SoundSystemException soundSystemException = new SoundSystemException(this.className + " did not load properly.  " + "Library was null after initialization.", 4);
                SoundSystem.lastException(true, soundSystemException);
                SoundSystem.initialized(true, true);
                throw soundSystemException;
            }
            try {
                this.soundLibrary.init();
            }
            catch (SoundSystemException soundSystemException) {
                SoundSystem.lastException(true, soundSystemException);
                SoundSystem.initialized(true, true);
                throw soundSystemException;
            }
            this.soundLibrary.setListenerData(listenerData);
            if (bl2) {
                if (midiChannel != null) {
                    midiChannel.cleanup();
                }
                midiChannel = new MidiChannel(bl3, string, filenameURL);
                this.soundLibrary.setMidiChannel(midiChannel);
            }
            this.soundLibrary.copySources(hashMap);
            this.message("", 0);
            SoundSystem.lastException(true, null);
            SoundSystem.initialized(true, true);
            return true;
        }
    }

    public boolean newLibrary(Class clazz) {
        SoundSystem.initialized(true, false);
        this.CommandQueue(new CommandObject(37, clazz));
        this.commandThread.interrupt();
        for (int i2 = 0; !SoundSystem.initialized(false, false) && i2 < 100; ++i2) {
            SoundSystem.snooze(400L);
            this.commandThread.interrupt();
        }
        if (!SoundSystem.initialized(false, false)) {
            SoundSystemException soundSystemException = new SoundSystemException(this.className + " did not load after 30 seconds.", 4);
            SoundSystem.lastException(true, soundSystemException);
            throw soundSystemException;
        }
        SoundSystemException soundSystemException = SoundSystem.lastException(false, null);
        if (soundSystemException != null) {
            throw soundSystemException;
        }
        return true;
    }

    private void CommandNewLibrary(Class clazz) {
        SoundSystem.initialized(true, false);
        String string = "Initializing ";
        if (this.soundLibrary != null) {
            SoundSystem.currentLibrary(true, null);
            string = "Switching to ";
            this.soundLibrary.cleanup();
            this.soundLibrary = null;
        }
        this.message(string + SoundSystemConfig.getLibraryTitle(clazz), 0);
        this.message("(" + SoundSystemConfig.getLibraryDescription(clazz) + ")", 1);
        try {
            this.soundLibrary = (Library)clazz.newInstance();
        }
        catch (InstantiationException instantiationException) {
            this.errorMessage("The specified library did not load properly", 1);
        }
        catch (IllegalAccessException illegalAccessException) {
            this.errorMessage("The specified library did not load properly", 1);
        }
        catch (ExceptionInInitializerError exceptionInInitializerError) {
            this.errorMessage("The specified library did not load properly", 1);
        }
        catch (SecurityException securityException) {
            this.errorMessage("The specified library did not load properly", 1);
        }
        if (this.errorCheck(this.soundLibrary == null, "Library null after initialization in method 'newLibrary'", 1)) {
            SoundSystem.lastException(true, new SoundSystemException(this.className + " did not load properly.  " + "Library was null after initialization.", 4));
            this.importantMessage("Switching to silent mode", 1);
            try {
                this.soundLibrary = new Library();
            }
            catch (SoundSystemException soundSystemException) {
                SoundSystem.lastException(true, new SoundSystemException("Silent mode did not load properly.  Library was null after initialization.", 4));
                SoundSystem.initialized(true, true);
                return;
            }
        }
        try {
            this.soundLibrary.init();
        }
        catch (SoundSystemException soundSystemException) {
            SoundSystem.lastException(true, soundSystemException);
            SoundSystem.initialized(true, true);
            return;
        }
        SoundSystem.lastException(true, null);
        SoundSystem.initialized(true, true);
    }

    private void CommandInitialize() {
        try {
            if (this.errorCheck(this.soundLibrary == null, "Library null after initialization in method 'CommandInitialize'", 1)) {
                SoundSystemException soundSystemException = new SoundSystemException(this.className + " did not load properly.  " + "Library was null after initialization.", 4);
                SoundSystem.lastException(true, soundSystemException);
                throw soundSystemException;
            }
            this.soundLibrary.init();
        }
        catch (SoundSystemException soundSystemException) {
            SoundSystem.lastException(true, soundSystemException);
            SoundSystem.initialized(true, true);
        }
    }

    private void CommandLoadSound(FilenameURL filenameURL) {
        if (this.soundLibrary != null) {
            this.soundLibrary.loadSound(filenameURL);
        } else {
            this.errorMessage("Variable 'soundLibrary' null in method 'CommandLoadSound'", 0);
        }
    }

    private void CommandUnloadSound(String string) {
        if (this.soundLibrary != null) {
            this.soundLibrary.unloadSound(string);
        } else {
            this.errorMessage("Variable 'soundLibrary' null in method 'CommandLoadSound'", 0);
        }
    }

    private void CommandQueueSound(String string, FilenameURL filenameURL) {
        if (this.soundLibrary != null) {
            this.soundLibrary.queueSound(string, filenameURL);
        } else {
            this.errorMessage("Variable 'soundLibrary' null in method 'CommandQueueSound'", 0);
        }
    }

    private void CommandDequeueSound(String string, String string2) {
        if (this.soundLibrary != null) {
            this.soundLibrary.dequeueSound(string, string2);
        } else {
            this.errorMessage("Variable 'soundLibrary' null in method 'CommandDequeueSound'", 0);
        }
    }

    private void CommandFadeOut(String string, FilenameURL filenameURL, long l2) {
        if (this.soundLibrary != null) {
            this.soundLibrary.fadeOut(string, filenameURL, l2);
        } else {
            this.errorMessage("Variable 'soundLibrary' null in method 'CommandFadeOut'", 0);
        }
    }

    private void CommandFadeOutIn(String string, FilenameURL filenameURL, long l2, long l3) {
        if (this.soundLibrary != null) {
            this.soundLibrary.fadeOutIn(string, filenameURL, l2, l3);
        } else {
            this.errorMessage("Variable 'soundLibrary' null in method 'CommandFadeOutIn'", 0);
        }
    }

    private void CommandCheckFadeVolumes() {
        if (this.soundLibrary != null) {
            this.soundLibrary.checkFadeVolumes();
        } else {
            this.errorMessage("Variable 'soundLibrary' null in method 'CommandCheckFadeVolumes'", 0);
        }
    }

    private void CommandNewSource(boolean bl2, boolean bl3, boolean bl4, String string, FilenameURL filenameURL, float f2, float f3, float f4, int n2, float f5) {
        if (this.soundLibrary != null) {
            if (filenameURL.getFilename().matches(".*[mM][iI][dD][iI]?$") && !SoundSystemConfig.midiCodec()) {
                this.soundLibrary.loadMidi(bl4, string, filenameURL);
            } else {
                this.soundLibrary.newSource(bl2, bl3, bl4, string, filenameURL, f2, f3, f4, n2, f5);
            }
        } else {
            this.errorMessage("Variable 'soundLibrary' null in method 'CommandNewSource'", 0);
        }
    }

    private void CommandRawDataStream(AudioFormat audioFormat, boolean bl2, String string, float f2, float f3, float f4, int n2, float f5) {
        if (this.soundLibrary != null) {
            this.soundLibrary.rawDataStream(audioFormat, bl2, string, f2, f3, f4, n2, f5);
        } else {
            this.errorMessage("Variable 'soundLibrary' null in method 'CommandRawDataStream'", 0);
        }
    }

    private void CommandQuickPlay(boolean bl2, boolean bl3, boolean bl4, String string, FilenameURL filenameURL, float f2, float f3, float f4, int n2, float f5, boolean bl5) {
        if (this.soundLibrary != null) {
            if (filenameURL.getFilename().matches(".*[mM][iI][dD][iI]?$") && !SoundSystemConfig.midiCodec()) {
                this.soundLibrary.loadMidi(bl4, string, filenameURL);
            } else {
                this.soundLibrary.quickPlay(bl2, bl3, bl4, string, filenameURL, f2, f3, f4, n2, f5, bl5);
            }
        } else {
            this.errorMessage("Variable 'soundLibrary' null in method 'CommandQuickPlay'", 0);
        }
    }

    private void CommandSetPosition(String string, float f2, float f3, float f4) {
        if (this.soundLibrary != null) {
            this.soundLibrary.setPosition(string, f2, f3, f4);
        } else {
            this.errorMessage("Variable 'soundLibrary' null in method 'CommandMoveSource'", 0);
        }
    }

    private void CommandSetVolume(String string, float f2) {
        if (this.soundLibrary != null) {
            this.soundLibrary.setVolume(string, f2);
        } else {
            this.errorMessage("Variable 'soundLibrary' null in method 'CommandSetVolume'", 0);
        }
    }

    private void CommandSetPitch(String string, float f2) {
        if (this.soundLibrary != null) {
            this.soundLibrary.setPitch(string, f2);
        } else {
            this.errorMessage("Variable 'soundLibrary' null in method 'CommandSetPitch'", 0);
        }
    }

    private void CommandSetPriority(String string, boolean bl2) {
        if (this.soundLibrary != null) {
            this.soundLibrary.setPriority(string, bl2);
        } else {
            this.errorMessage("Variable 'soundLibrary' null in method 'CommandSetPriority'", 0);
        }
    }

    private void CommandSetLooping(String string, boolean bl2) {
        if (this.soundLibrary != null) {
            this.soundLibrary.setLooping(string, bl2);
        } else {
            this.errorMessage("Variable 'soundLibrary' null in method 'CommandSetLooping'", 0);
        }
    }

    private void CommandSetAttenuation(String string, int n2) {
        if (this.soundLibrary != null) {
            this.soundLibrary.setAttenuation(string, n2);
        } else {
            this.errorMessage("Variable 'soundLibrary' null in method 'CommandSetAttenuation'", 0);
        }
    }

    private void CommandSetDistOrRoll(String string, float f2) {
        if (this.soundLibrary != null) {
            this.soundLibrary.setDistOrRoll(string, f2);
        } else {
            this.errorMessage("Variable 'soundLibrary' null in method 'CommandSetDistOrRoll'", 0);
        }
    }

    private void CommandPlay(String string) {
        if (this.soundLibrary != null) {
            this.soundLibrary.play(string);
        } else {
            this.errorMessage("Variable 'soundLibrary' null in method 'CommandPlay'", 0);
        }
    }

    private void CommandFeedRawAudioData(String string, byte[] byArray) {
        if (this.soundLibrary != null) {
            this.soundLibrary.feedRawAudioData(string, byArray);
        } else {
            this.errorMessage("Variable 'soundLibrary' null in method 'CommandFeedRawAudioData'", 0);
        }
    }

    private void CommandPause(String string) {
        if (this.soundLibrary != null) {
            this.soundLibrary.pause(string);
        } else {
            this.errorMessage("Variable 'soundLibrary' null in method 'CommandPause'", 0);
        }
    }

    private void CommandStop(String string) {
        if (this.soundLibrary != null) {
            this.soundLibrary.stop(string);
        } else {
            this.errorMessage("Variable 'soundLibrary' null in method 'CommandStop'", 0);
        }
    }

    private void CommandRewind(String string) {
        if (this.soundLibrary != null) {
            this.soundLibrary.rewind(string);
        } else {
            this.errorMessage("Variable 'soundLibrary' null in method 'CommandRewind'", 0);
        }
    }

    private void CommandFlush(String string) {
        if (this.soundLibrary != null) {
            this.soundLibrary.flush(string);
        } else {
            this.errorMessage("Variable 'soundLibrary' null in method 'CommandFlush'", 0);
        }
    }

    private void CommandSetTemporary(String string, boolean bl2) {
        if (this.soundLibrary != null) {
            this.soundLibrary.setTemporary(string, bl2);
        } else {
            this.errorMessage("Variable 'soundLibrary' null in method 'CommandSetActive'", 0);
        }
    }

    private void CommandRemoveSource(String string) {
        if (this.soundLibrary != null) {
            this.soundLibrary.removeSource(string);
        } else {
            this.errorMessage("Variable 'soundLibrary' null in method 'CommandRemoveSource'", 0);
        }
    }

    private void CommandMoveListener(float f2, float f3, float f4) {
        if (this.soundLibrary != null) {
            this.soundLibrary.moveListener(f2, f3, f4);
        } else {
            this.errorMessage("Variable 'soundLibrary' null in method 'CommandMoveListener'", 0);
        }
    }

    private void CommandSetListenerPosition(float f2, float f3, float f4) {
        if (this.soundLibrary != null) {
            this.soundLibrary.setListenerPosition(f2, f3, f4);
        } else {
            this.errorMessage("Variable 'soundLibrary' null in method 'CommandSetListenerPosition'", 0);
        }
    }

    private void CommandTurnListener(float f2) {
        if (this.soundLibrary != null) {
            this.soundLibrary.turnListener(f2);
        } else {
            this.errorMessage("Variable 'soundLibrary' null in method 'CommandTurnListener'", 0);
        }
    }

    private void CommandSetListenerAngle(float f2) {
        if (this.soundLibrary != null) {
            this.soundLibrary.setListenerAngle(f2);
        } else {
            this.errorMessage("Variable 'soundLibrary' null in method 'CommandSetListenerAngle'", 0);
        }
    }

    private void CommandSetListenerOrientation(float f2, float f3, float f4, float f5, float f6, float f7) {
        if (this.soundLibrary != null) {
            this.soundLibrary.setListenerOrientation(f2, f3, f4, f5, f6, f7);
        } else {
            this.errorMessage("Variable 'soundLibrary' null in method 'CommandSetListenerOrientation'", 0);
        }
    }

    private void CommandCull(String string) {
        if (this.soundLibrary != null) {
            this.soundLibrary.cull(string);
        } else {
            this.errorMessage("Variable 'soundLibrary' null in method 'CommandCull'", 0);
        }
    }

    private void CommandActivate(String string) {
        if (this.soundLibrary != null) {
            this.soundLibrary.activate(string);
        } else {
            this.errorMessage("Variable 'soundLibrary' null in method 'CommandActivate'", 0);
        }
    }

    private void CommandSetMasterVolume(float f2) {
        if (this.soundLibrary != null) {
            this.soundLibrary.setMasterVolume(f2);
        } else {
            this.errorMessage("Variable 'soundLibrary' null in method 'CommandSetMasterVolume'", 0);
        }
    }

    protected void ManageSources() {
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean CommandQueue(CommandObject commandObject) {
        Object object = SoundSystemConfig.THREAD_SYNC;
        synchronized (object) {
            if (commandObject == null) {
                CommandObject commandObject2;
                boolean bl2 = false;
                block44: while (this.commandQueue != null && this.commandQueue.size() > 0) {
                    commandObject2 = (CommandObject)this.commandQueue.remove(0);
                    if (commandObject2 == null) continue;
                    switch (commandObject2.Command) {
                        case 1: {
                            this.CommandInitialize();
                            continue block44;
                        }
                        case 2: {
                            this.CommandLoadSound((FilenameURL)commandObject2.objectArgs[0]);
                            continue block44;
                        }
                        case 4: {
                            this.CommandUnloadSound(commandObject2.stringArgs[0]);
                            continue block44;
                        }
                        case 5: {
                            this.CommandQueueSound(commandObject2.stringArgs[0], (FilenameURL)commandObject2.objectArgs[0]);
                            continue block44;
                        }
                        case 6: {
                            this.CommandDequeueSound(commandObject2.stringArgs[0], commandObject2.stringArgs[1]);
                            continue block44;
                        }
                        case 7: {
                            this.CommandFadeOut(commandObject2.stringArgs[0], (FilenameURL)commandObject2.objectArgs[0], commandObject2.longArgs[0]);
                            continue block44;
                        }
                        case 8: {
                            this.CommandFadeOutIn(commandObject2.stringArgs[0], (FilenameURL)commandObject2.objectArgs[0], commandObject2.longArgs[0], commandObject2.longArgs[1]);
                            continue block44;
                        }
                        case 9: {
                            this.CommandCheckFadeVolumes();
                            continue block44;
                        }
                        case 10: {
                            this.CommandNewSource(commandObject2.boolArgs[0], commandObject2.boolArgs[1], commandObject2.boolArgs[2], commandObject2.stringArgs[0], (FilenameURL)commandObject2.objectArgs[0], commandObject2.floatArgs[0], commandObject2.floatArgs[1], commandObject2.floatArgs[2], commandObject2.intArgs[0], commandObject2.floatArgs[3]);
                            continue block44;
                        }
                        case 11: {
                            this.CommandRawDataStream((AudioFormat)commandObject2.objectArgs[0], commandObject2.boolArgs[0], commandObject2.stringArgs[0], commandObject2.floatArgs[0], commandObject2.floatArgs[1], commandObject2.floatArgs[2], commandObject2.intArgs[0], commandObject2.floatArgs[3]);
                            continue block44;
                        }
                        case 12: {
                            this.CommandQuickPlay(commandObject2.boolArgs[0], commandObject2.boolArgs[1], commandObject2.boolArgs[2], commandObject2.stringArgs[0], (FilenameURL)commandObject2.objectArgs[0], commandObject2.floatArgs[0], commandObject2.floatArgs[1], commandObject2.floatArgs[2], commandObject2.intArgs[0], commandObject2.floatArgs[3], commandObject2.boolArgs[3]);
                            continue block44;
                        }
                        case 13: {
                            this.CommandSetPosition(commandObject2.stringArgs[0], commandObject2.floatArgs[0], commandObject2.floatArgs[1], commandObject2.floatArgs[2]);
                            continue block44;
                        }
                        case 14: {
                            this.CommandSetVolume(commandObject2.stringArgs[0], commandObject2.floatArgs[0]);
                            continue block44;
                        }
                        case 15: {
                            this.CommandSetPitch(commandObject2.stringArgs[0], commandObject2.floatArgs[0]);
                            continue block44;
                        }
                        case 16: {
                            this.CommandSetPriority(commandObject2.stringArgs[0], commandObject2.boolArgs[0]);
                            continue block44;
                        }
                        case 17: {
                            this.CommandSetLooping(commandObject2.stringArgs[0], commandObject2.boolArgs[0]);
                            continue block44;
                        }
                        case 18: {
                            this.CommandSetAttenuation(commandObject2.stringArgs[0], commandObject2.intArgs[0]);
                            continue block44;
                        }
                        case 19: {
                            this.CommandSetDistOrRoll(commandObject2.stringArgs[0], commandObject2.floatArgs[0]);
                            continue block44;
                        }
                        case 21: {
                            this.sourcePlayList.add(commandObject2);
                            continue block44;
                        }
                        case 22: {
                            this.sourcePlayList.add(commandObject2);
                            continue block44;
                        }
                        case 23: {
                            this.CommandPause(commandObject2.stringArgs[0]);
                            continue block44;
                        }
                        case 24: {
                            this.CommandStop(commandObject2.stringArgs[0]);
                            continue block44;
                        }
                        case 25: {
                            this.CommandRewind(commandObject2.stringArgs[0]);
                            continue block44;
                        }
                        case 26: {
                            this.CommandFlush(commandObject2.stringArgs[0]);
                            continue block44;
                        }
                        case 27: {
                            this.CommandCull(commandObject2.stringArgs[0]);
                            continue block44;
                        }
                        case 28: {
                            bl2 = true;
                            this.CommandActivate(commandObject2.stringArgs[0]);
                            continue block44;
                        }
                        case 29: {
                            this.CommandSetTemporary(commandObject2.stringArgs[0], commandObject2.boolArgs[0]);
                            continue block44;
                        }
                        case 30: {
                            this.CommandRemoveSource(commandObject2.stringArgs[0]);
                            continue block44;
                        }
                        case 31: {
                            this.CommandMoveListener(commandObject2.floatArgs[0], commandObject2.floatArgs[1], commandObject2.floatArgs[2]);
                            continue block44;
                        }
                        case 32: {
                            this.CommandSetListenerPosition(commandObject2.floatArgs[0], commandObject2.floatArgs[1], commandObject2.floatArgs[2]);
                            continue block44;
                        }
                        case 33: {
                            this.CommandTurnListener(commandObject2.floatArgs[0]);
                            continue block44;
                        }
                        case 34: {
                            this.CommandSetListenerAngle(commandObject2.floatArgs[0]);
                            continue block44;
                        }
                        case 35: {
                            this.CommandSetListenerOrientation(commandObject2.floatArgs[0], commandObject2.floatArgs[1], commandObject2.floatArgs[2], commandObject2.floatArgs[3], commandObject2.floatArgs[4], commandObject2.floatArgs[5]);
                            continue block44;
                        }
                        case 36: {
                            this.CommandSetMasterVolume(commandObject2.floatArgs[0]);
                            continue block44;
                        }
                        case 37: {
                            this.CommandNewLibrary(commandObject2.classArgs[0]);
                            continue block44;
                        }
                    }
                }
                if (bl2) {
                    this.soundLibrary.replaySources();
                }
                while (this.sourcePlayList != null && this.sourcePlayList.size() > 0) {
                    commandObject2 = (CommandObject)this.sourcePlayList.remove(0);
                    if (commandObject2 == null) continue;
                    switch (commandObject2.Command) {
                        case 21: {
                            this.CommandPlay(commandObject2.stringArgs[0]);
                            break;
                        }
                        case 22: {
                            this.CommandFeedRawAudioData(commandObject2.stringArgs[0], commandObject2.buffer);
                        }
                    }
                }
                return this.commandQueue != null && this.commandQueue.size() > 0;
            }
            if (this.commandQueue == null) {
                return false;
            }
            this.commandQueue.add(commandObject);
            return true;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void removeTemporarySources() {
        Object object = SoundSystemConfig.THREAD_SYNC;
        synchronized (object) {
            if (this.soundLibrary != null) {
                this.soundLibrary.removeTemporarySources();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean playing(String string) {
        Object object = SoundSystemConfig.THREAD_SYNC;
        synchronized (object) {
            if (this.soundLibrary == null) {
                return false;
            }
            Source source = (Source)this.soundLibrary.getSources().get(string);
            if (source == null) {
                return false;
            }
            return source.playing();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean playing() {
        Object object = SoundSystemConfig.THREAD_SYNC;
        synchronized (object) {
            if (this.soundLibrary == null) {
                return false;
            }
            HashMap hashMap = this.soundLibrary.getSources();
            if (hashMap == null) {
                return false;
            }
            Set set = hashMap.keySet();
            for (String string : set) {
                Source source = (Source)hashMap.get(string);
                if (source == null || !source.playing()) continue;
                return true;
            }
            return false;
        }
    }

    private HashMap copySources(HashMap hashMap) {
        Set set = hashMap.keySet();
        Iterator iterator = set.iterator();
        HashMap<String, Source> hashMap2 = new HashMap<String, Source>();
        while (iterator.hasNext()) {
            String string = (String)iterator.next();
            Source source = (Source)hashMap.get(string);
            if (source == null) continue;
            hashMap2.put(string, new Source(source, null));
        }
        return hashMap2;
    }

    public static boolean libraryCompatible(Class clazz) {
        SoundSystemLogger soundSystemLogger = SoundSystemConfig.getLogger();
        if (soundSystemLogger == null) {
            soundSystemLogger = new SoundSystemLogger();
            SoundSystemConfig.setLogger(soundSystemLogger);
        }
        soundSystemLogger.message("", 0);
        soundSystemLogger.message("Checking if " + SoundSystemConfig.getLibraryTitle(clazz) + " is compatible...", 0);
        boolean bl2 = SoundSystemConfig.libraryCompatible(clazz);
        if (bl2) {
            soundSystemLogger.message("...yes", 1);
        } else {
            soundSystemLogger.message("...no", 1);
        }
        return bl2;
    }

    public static Class currentLibrary() {
        return SoundSystem.currentLibrary(false, null);
    }

    public static boolean initialized() {
        return SoundSystem.initialized(false, false);
    }

    public static SoundSystemException getLastException() {
        return SoundSystem.lastException(false, null);
    }

    public static void setException(SoundSystemException soundSystemException) {
        SoundSystem.lastException(true, soundSystemException);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static boolean initialized(boolean bl2, boolean bl3) {
        Object object = SoundSystemConfig.THREAD_SYNC;
        synchronized (object) {
            if (bl2) {
                initialized = bl3;
            }
            return initialized;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static Class currentLibrary(boolean bl2, Class clazz) {
        Object object = SoundSystemConfig.THREAD_SYNC;
        synchronized (object) {
            if (bl2) {
                currentLibrary = clazz;
            }
            return currentLibrary;
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static SoundSystemException lastException(boolean bl2, SoundSystemException soundSystemException) {
        Object object = SoundSystemConfig.THREAD_SYNC;
        synchronized (object) {
            if (bl2) {
                lastException = soundSystemException;
            }
            return lastException;
        }
    }

    protected static void snooze(long l2) {
        try {
            Thread.sleep(l2);
        }
        catch (InterruptedException interruptedException) {
            // empty catch block
        }
    }

    protected void message(String string, int n2) {
        this.logger.message(string, n2);
    }

    protected void importantMessage(String string, int n2) {
        this.logger.importantMessage(string, n2);
    }

    protected boolean errorCheck(boolean bl2, String string, int n2) {
        return this.logger.errorCheck(bl2, this.className, string, n2);
    }

    protected void errorMessage(String string, int n2) {
        this.logger.errorMessage(this.className, string, n2);
    }
}

