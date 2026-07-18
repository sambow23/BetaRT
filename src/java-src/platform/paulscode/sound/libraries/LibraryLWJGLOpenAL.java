package paulscode.sound.libraries;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import javax.sound.sampled.AudioFormat;
import mcrtx.lwjglshim.LegacyAL10;
import mcrtx.lwjglshim.OpenAlCompat;
import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import paulscode.sound.Channel;
import paulscode.sound.FilenameURL;
import paulscode.sound.ICodec;
import paulscode.sound.Library;
import paulscode.sound.ListenerData;
import paulscode.sound.SoundBuffer;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.SoundSystemException;
import paulscode.sound.Source;

public class LibraryLWJGLOpenAL extends Library {
    private static final boolean GET = false;
    private static final boolean SET = true;
    private static final boolean XXX = false;
    private FloatBuffer listenerPositionAL = null;
    private FloatBuffer listenerOrientation = null;
    private FloatBuffer listenerVelocity = null;
    private HashMap ALBufferMap = null;
    private static boolean alPitchSupported = true;

    public LibraryLWJGLOpenAL() {
        this.ALBufferMap = new HashMap();
    }

    @Override
    public void init() {
        boolean initialError = false;

        try {
            OpenAlCompat.create();
            initialError = this.checkALError();
        } catch (LWJGLException exception) {
            this.errorMessage("Unable to initialize OpenAL.  Probable cause: OpenAL not supported.");
            this.printStackTrace(exception);
            throwUnchecked(new SoundSystemException(exception.getMessage(), 6));
            return;
        }

        if (initialError) {
            this.importantMessage("OpenAL did not initialize properly!");
        } else {
            this.message("OpenAL initialized.");
        }

        this.listenerPositionAL = BufferUtils.createFloatBuffer(3).put(new float[] { this.listener.position.x, this.listener.position.y, this.listener.position.z });
        this.listenerOrientation = BufferUtils.createFloatBuffer(6)
                .put(new float[] { this.listener.lookAt.x, this.listener.lookAt.y, this.listener.lookAt.z, this.listener.up.x, this.listener.up.y, this.listener.up.z });
        this.listenerVelocity = BufferUtils.createFloatBuffer(3).put(new float[] { 0.0F, 0.0F, 0.0F });
        ((Buffer) this.listenerPositionAL).flip();
        ((Buffer) this.listenerOrientation).flip();
        ((Buffer) this.listenerVelocity).flip();
        LegacyAL10.alListener(4100, this.listenerPositionAL);
        initialError = this.checkALError() || initialError;
        LegacyAL10.alListener(4111, this.listenerOrientation);
        initialError = this.checkALError() || initialError;
        LegacyAL10.alListener(4102, this.listenerVelocity);
        initialError = this.checkALError() || initialError;
        if (initialError) {
            this.importantMessage("OpenAL did not initialize properly!");
            throwUnchecked(new SoundSystemException("Problem encountered while loading OpenAL or creating the listener.  Probably cause:  OpenAL not supported", 6));
            return;
        }

        super.init();
        ChannelLWJGLOpenAL normalChannel = (ChannelLWJGLOpenAL) this.normalChannels.get(1);

        try {
            LegacyAL10.alSourcef(normalChannel.ALSource.get(0), 4099, 1.0F);
            if (this.checkALError()) {
                alPitchSupported(true, false);
                throwUnchecked(new SoundSystemException("OpenAL: AL_PITCH not supported.", 13));
                return;
            }
            alPitchSupported(true, true);
        } catch (Exception exception) {
            alPitchSupported(true, false);
            throwUnchecked(new SoundSystemException("OpenAL: AL_PITCH not supported.", 13));
            return;
        }
    }

    public static boolean libraryCompatible() {
        if (OpenAlCompat.isCreated()) {
            return true;
        }

        try {
            OpenAlCompat.create();
        } catch (Exception exception) {
            return false;
        }

        try {
            OpenAlCompat.destroy();
        } catch (Exception exception) {
        }

        return true;
    }

    @Override
    protected Channel createChannel(int channelType) {
        IntBuffer sourceBuffer = BufferUtils.createIntBuffer(1);

        try {
            LegacyAL10.alGenSources(sourceBuffer);
        } catch (Exception exception) {
            LegacyAL10.alGetError();
            return null;
        }

        return LegacyAL10.alGetError() != 0 ? null : new ChannelLWJGLOpenAL(channelType, sourceBuffer);
    }

    @Override
    public void cleanup() {
        super.cleanup();

        for (Object key : this.bufferMap.keySet()) {
            IntBuffer buffer = (IntBuffer) this.ALBufferMap.get(key);
            if (buffer != null) {
                LegacyAL10.alDeleteBuffers(buffer);
                this.checkALError();
                ((Buffer) buffer).clear();
            }
        }

        this.bufferMap.clear();
        OpenAlCompat.destroy();
        this.bufferMap = null;
        this.listenerPositionAL = null;
        this.listenerOrientation = null;
        this.listenerVelocity = null;
    }

    @Override
    public boolean loadSound(FilenameURL filenameUrl) {
        if (this.bufferMap == null) {
            this.bufferMap = new HashMap();
            this.importantMessage("Buffer Map was null in method 'loadSound'");
        }

        if (this.ALBufferMap == null) {
            this.ALBufferMap = new HashMap();
            this.importantMessage("Open AL Buffer Map was null in method'loadSound'");
        }

        if (this.errorCheck(filenameUrl == null, "Filename/URL not specified in method 'loadSound'")) {
            return false;
        }
        if (this.bufferMap.get(filenameUrl.getFilename()) != null) {
            return true;
        }

        ICodec codec = SoundSystemConfig.getCodec(filenameUrl.getFilename());
        if (this.errorCheck(codec == null, "No codec found for file '" + filenameUrl.getFilename() + "' in method 'loadSound'")) {
            return false;
        }

        codec.initialize(filenameUrl.getURL());
        SoundBuffer soundBuffer = codec.readAll();
        codec.cleanup();
        if (this.errorCheck(soundBuffer == null, "Sound buffer null in method 'loadSound'")) {
            return false;
        }

        this.bufferMap.put(filenameUrl.getFilename(), soundBuffer);
        AudioFormat audioFormat = soundBuffer.audioFormat;
        short alFormat = 0;
        if (audioFormat.getChannels() == 1) {
            if (audioFormat.getSampleSizeInBits() == 8) {
                alFormat = 4352;
            } else {
                if (audioFormat.getSampleSizeInBits() != 16) {
                    this.errorMessage("Illegal sample size in method 'loadSound'");
                    return false;
                }
                alFormat = 4353;
            }
        } else {
            if (audioFormat.getChannels() != 2) {
                this.errorMessage("File neither mono nor stereo in method 'loadSound'");
                return false;
            }
            if (audioFormat.getSampleSizeInBits() == 8) {
                alFormat = 4354;
            } else {
                if (audioFormat.getSampleSizeInBits() != 16) {
                    this.errorMessage("Illegal sample size in method 'loadSound'");
                    return false;
                }
                alFormat = 4355;
            }
        }

        IntBuffer buffer = BufferUtils.createIntBuffer(1);
        LegacyAL10.alGenBuffers(buffer);
        if (this.errorCheck(LegacyAL10.alGetError() != 0, "alGenBuffers error when loading " + filenameUrl.getFilename())) {
            return false;
        }

        ByteBuffer byteBuffer = BufferUtils.createByteBuffer(soundBuffer.audioData.length);
        ((Buffer) byteBuffer).clear();
        byteBuffer.put(soundBuffer.audioData);
        ((Buffer) byteBuffer).flip();
        LegacyAL10.alBufferData(buffer.get(0), alFormat, byteBuffer, (int) audioFormat.getSampleRate());
        if (this.errorCheck(LegacyAL10.alGetError() != 0, "alBufferData error when loading " + filenameUrl.getFilename())
                && this.errorCheck(buffer == null, "Sound buffer was not created for " + filenameUrl.getFilename())) {
            return false;
        }

        this.ALBufferMap.put(filenameUrl.getFilename(), buffer);
        return true;
    }

    @Override
    public void unloadSound(String filename) {
        this.ALBufferMap.remove(filename);
        super.unloadSound(filename);
    }

    @Override
    public void setMasterVolume(float value) {
        super.setMasterVolume(value);
        LegacyAL10.alListenerf(4106, value);
        this.checkALError();
    }

    @Override
    public void newSource(boolean priority, boolean toStream, boolean toLoop, String sourcename, FilenameURL filenameUrl, float x, float y, float z, int attModel, float distOrRoll) {
        IntBuffer buffer = null;
        if (!toStream) {
            buffer = (IntBuffer) this.ALBufferMap.get(filenameUrl.getFilename());
            if (buffer == null && !this.loadSound(filenameUrl)) {
                this.errorMessage("Source '" + sourcename + "' was not created because an error occurred while loading " + filenameUrl.getFilename());
                return;
            }

            buffer = (IntBuffer) this.ALBufferMap.get(filenameUrl.getFilename());
            if (buffer == null) {
                this.errorMessage("Source '" + sourcename + "' was not created because a sound buffer was not found for " + filenameUrl.getFilename());
                return;
            }
        }

        SoundBuffer soundBuffer = null;
        if (!toStream) {
            soundBuffer = (SoundBuffer) this.bufferMap.get(filenameUrl.getFilename());
            if (soundBuffer == null && !this.loadSound(filenameUrl)) {
                this.errorMessage("Source '" + sourcename + "' was not created because an error occurred while loading " + filenameUrl.getFilename());
                return;
            }

            soundBuffer = (SoundBuffer) this.bufferMap.get(filenameUrl.getFilename());
            if (soundBuffer == null) {
                this.errorMessage("Source '" + sourcename + "' was not created because audio data was not found for " + filenameUrl.getFilename());
                return;
            }
        }

        this.sourceMap.put(
                sourcename,
                new SourceLWJGLOpenAL(this.listenerPositionAL, buffer, priority, toStream, toLoop, sourcename, filenameUrl, soundBuffer, x, y, z, attModel, distOrRoll, false));
    }

    @Override
    public void rawDataStream(AudioFormat audioFormat, boolean priority, String sourcename, float x, float y, float z, int attModel, float distOrRoll) {
        this.sourceMap.put(sourcename, new SourceLWJGLOpenAL(this.listenerPositionAL, audioFormat, priority, sourcename, x, y, z, attModel, distOrRoll));
    }

    @Override
    public void quickPlay(
            boolean priority,
            boolean toStream,
            boolean toLoop,
            String sourcename,
            FilenameURL filenameUrl,
            float x,
            float y,
            float z,
            int attModel,
            float distOrRoll,
            boolean temporary) {
        IntBuffer buffer = null;
        if (!toStream) {
            buffer = (IntBuffer) this.ALBufferMap.get(filenameUrl.getFilename());
            if (buffer == null) {
                this.loadSound(filenameUrl);
            }

            buffer = (IntBuffer) this.ALBufferMap.get(filenameUrl.getFilename());
            if (buffer == null) {
                this.errorMessage("Sound buffer was not created for " + filenameUrl.getFilename());
                return;
            }
        }

        SoundBuffer soundBuffer = null;
        if (!toStream) {
            soundBuffer = (SoundBuffer) this.bufferMap.get(filenameUrl.getFilename());
            if (soundBuffer == null && !this.loadSound(filenameUrl)) {
                this.errorMessage("Source '" + sourcename + "' was not created because an error occurred while loading " + filenameUrl.getFilename());
                return;
            }

            soundBuffer = (SoundBuffer) this.bufferMap.get(filenameUrl.getFilename());
            if (soundBuffer == null) {
                this.errorMessage("Source '" + sourcename + "' was not created because audio data was not found for " + filenameUrl.getFilename());
                return;
            }
        }

        SourceLWJGLOpenAL source = new SourceLWJGLOpenAL(this.listenerPositionAL, buffer, priority, toStream, toLoop, sourcename, filenameUrl, soundBuffer, x, y, z, attModel, distOrRoll, false);
        this.sourceMap.put(sourcename, source);
        this.play(source);
        if (temporary) {
            source.setTemporary(true);
        }
    }

    @Override
    public void copySources(HashMap sources) {
        if (sources == null) {
            return;
        }

        Set keys = sources.keySet();
        Iterator iterator = keys.iterator();
        if (this.bufferMap == null) {
            this.bufferMap = new HashMap();
            this.importantMessage("Buffer Map was null in method 'copySources'");
        }

        if (this.ALBufferMap == null) {
            this.ALBufferMap = new HashMap();
            this.importantMessage("Open AL Buffer Map was null in method'copySources'");
        }

        this.sourceMap.clear();
        while (iterator.hasNext()) {
            String sourcename = (String) iterator.next();
            Source source = (Source) sources.get(sourcename);
            if (source != null) {
                SoundBuffer soundBuffer = null;
                if (!source.toStream) {
                    this.loadSound(source.filenameURL);
                    soundBuffer = (SoundBuffer) this.bufferMap.get(source.filenameURL.getFilename());
                }

                if (source.toStream || soundBuffer != null) {
                    this.sourceMap.put(sourcename, new SourceLWJGLOpenAL(this.listenerPositionAL, (IntBuffer) this.ALBufferMap.get(source.filenameURL.getFilename()), source, soundBuffer));
                }
            }
        }
    }

    @Override
    public void setListenerPosition(float x, float y, float z) {
        super.setListenerPosition(x, y, z);
        this.listenerPositionAL.put(0, x);
        this.listenerPositionAL.put(1, y);
        this.listenerPositionAL.put(2, z);
        LegacyAL10.alListener(4100, this.listenerPositionAL);
        this.checkALError();
    }

    @Override
    public void setListenerAngle(float angle) {
        super.setListenerAngle(angle);
        this.listenerOrientation.put(0, this.listener.lookAt.x);
        this.listenerOrientation.put(2, this.listener.lookAt.z);
        LegacyAL10.alListener(4111, this.listenerOrientation);
        this.checkALError();
    }

    @Override
    public void setListenerOrientation(float lookX, float lookY, float lookZ, float upX, float upY, float upZ) {
        super.setListenerOrientation(lookX, lookY, lookZ, upX, upY, upZ);
        this.listenerOrientation.put(0, lookX);
        this.listenerOrientation.put(1, lookY);
        this.listenerOrientation.put(2, lookZ);
        this.listenerOrientation.put(3, upX);
        this.listenerOrientation.put(4, upY);
        this.listenerOrientation.put(5, upZ);
        LegacyAL10.alListener(4111, this.listenerOrientation);
        this.checkALError();
    }

    @Override
    public void setListenerData(ListenerData listenerData) {
        super.setListenerData(listenerData);
        this.listenerPositionAL.put(0, listenerData.position.x);
        this.listenerPositionAL.put(1, listenerData.position.y);
        this.listenerPositionAL.put(2, listenerData.position.z);
        LegacyAL10.alListener(4100, this.listenerPositionAL);
        this.listenerOrientation.put(0, listenerData.lookAt.x);
        this.listenerOrientation.put(1, listenerData.lookAt.y);
        this.listenerOrientation.put(2, listenerData.lookAt.z);
        this.listenerOrientation.put(3, listenerData.up.x);
        this.listenerOrientation.put(4, listenerData.up.y);
        this.listenerOrientation.put(5, listenerData.up.z);
        LegacyAL10.alListener(4111, this.listenerOrientation);
        this.checkALError();
    }

    private boolean checkALError() {
        int errorCode = LegacyAL10.alGetError();
        switch (errorCode) {
            case 0:
                return false;
            case 40961:
                this.errorMessage("Invalid name parameter.");
                return true;
            case 40962:
                this.errorMessage("Invalid parameter.");
                return true;
            case 40963:
                this.errorMessage("Invalid enumerated parameter value.");
                return true;
            case 40964:
                this.errorMessage("Illegal call.");
                return true;
            case 40965:
                this.errorMessage("Unable to allocate memory.");
                return true;
            default:
                this.errorMessage("An unrecognized error occurred.");
                return true;
        }
    }

    public static boolean alPitchSupported() {
        return alPitchSupported(false, false);
    }

    private static synchronized boolean alPitchSupported(boolean action, boolean value) {
        if (action) {
            alPitchSupported = value;
        }

        return alPitchSupported;
    }

    public static String getTitle() {
        return "LWJGL OpenAL";
    }

    public static String getDescription() {
        return "The LWJGL binding of OpenAL.  For more information, see http://www.lwjgl.org";
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> void throwUnchecked(Throwable throwable) throws T {
        throw (T) throwable;
    }

    @Override
    public String getClassName() {
        return "LibraryLWJGLOpenAL";
    }
}