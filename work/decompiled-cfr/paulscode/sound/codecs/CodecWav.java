/*
 * Decompiled with CFR 0.152.
 */
package paulscode.sound.codecs;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import paulscode.sound.ICodec;
import paulscode.sound.SoundBuffer;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.SoundSystemLogger;

public class CodecWav
implements ICodec {
    private static final boolean GET = false;
    private static final boolean SET = true;
    private static final boolean XXX = false;
    private boolean endOfStream = false;
    private boolean initialized = false;
    private AudioInputStream myAudioInputStream = null;
    private SoundSystemLogger logger = SoundSystemConfig.getLogger();

    public void reverseByteOrder(boolean bl2) {
    }

    public boolean initialize(URL uRL) {
        this.initialized(true, false);
        this.cleanup();
        if (uRL == null) {
            this.errorMessage("url null in method 'initialize'");
            this.cleanup();
            return false;
        }
        try {
            this.myAudioInputStream = AudioSystem.getAudioInputStream(new BufferedInputStream(uRL.openStream()));
        }
        catch (UnsupportedAudioFileException unsupportedAudioFileException) {
            this.errorMessage("Unsupported audio format in method 'initialize'");
            this.printStackTrace(unsupportedAudioFileException);
            return false;
        }
        catch (IOException iOException) {
            this.errorMessage("Error setting up audio input stream in method 'initialize'");
            this.printStackTrace(iOException);
            return false;
        }
        this.endOfStream(true, false);
        this.initialized(true, true);
        return true;
    }

    public boolean initialized() {
        return this.initialized(false, false);
    }

    public SoundBuffer read() {
        int n2;
        if (this.myAudioInputStream == null) {
            return null;
        }
        AudioFormat audioFormat = this.myAudioInputStream.getFormat();
        if (audioFormat == null) {
            this.errorMessage("Audio Format null in method 'read'");
            return null;
        }
        int n3 = 0;
        byte[] byArray = new byte[SoundSystemConfig.getStreamingBufferSize()];
        try {
            for (n2 = 0; !this.endOfStream(false, false) && n2 < byArray.length; n2 += n3) {
                n3 = this.myAudioInputStream.read(byArray, n2, byArray.length - n2);
                if (n3 > 0) continue;
                this.endOfStream(true, true);
                break;
            }
        }
        catch (IOException iOException) {
            this.endOfStream(true, true);
            return null;
        }
        if (n2 <= 0) {
            return null;
        }
        if (n2 < byArray.length) {
            byArray = CodecWav.trimArray(byArray, n2);
        }
        byte[] byArray2 = CodecWav.convertAudioBytes(byArray, audioFormat.getSampleSizeInBits() == 16);
        SoundBuffer soundBuffer = new SoundBuffer(byArray2, audioFormat);
        return soundBuffer;
    }

    public SoundBuffer readAll() {
        int n2;
        int n3;
        if (this.myAudioInputStream == null) {
            this.errorMessage("Audio input stream null in method 'readAll'");
            return null;
        }
        AudioFormat audioFormat = this.myAudioInputStream.getFormat();
        if (audioFormat == null) {
            this.errorMessage("Audio Format null in method 'readAll'");
            return null;
        }
        byte[] byArray = null;
        int n4 = audioFormat.getChannels() * (int)this.myAudioInputStream.getFrameLength() * audioFormat.getSampleSizeInBits() / 8;
        if (n4 > 0) {
            byArray = new byte[audioFormat.getChannels() * (int)this.myAudioInputStream.getFrameLength() * audioFormat.getSampleSizeInBits() / 8];
            n3 = 0;
            try {
                for (n2 = 0; (n3 = this.myAudioInputStream.read(byArray, n2, byArray.length - n2)) != -1 && n2 < byArray.length; n2 += n3) {
                }
            }
            catch (IOException iOException) {
                this.errorMessage("Exception thrown while reading from the AudioInputStream (location #1).");
                this.printStackTrace(iOException);
                return null;
            }
        }
        n2 = 0;
        int n5 = 0;
        byte[] byArray2 = null;
        byArray2 = new byte[SoundSystemConfig.getFileChunkSize()];
        for (n3 = 0; !this.endOfStream(false, false) && n3 < SoundSystemConfig.getMaxFileSize(); n3 += n2) {
            n5 = 0;
            try {
                for (n2 = 0; n2 < byArray2.length; n2 += n5) {
                    n5 = this.myAudioInputStream.read(byArray2, n2, byArray2.length - n2);
                    if (n5 > 0) continue;
                    this.endOfStream(true, true);
                    break;
                }
            }
            catch (IOException iOException) {
                this.errorMessage("Exception thrown while reading from the AudioInputStream (location #2).");
                this.printStackTrace(iOException);
                return null;
            }
            byArray = CodecWav.appendByteArrays(byArray, byArray2, n2);
        }
        byte[] byArray3 = CodecWav.convertAudioBytes(byArray, audioFormat.getSampleSizeInBits() == 16);
        SoundBuffer soundBuffer = new SoundBuffer(byArray3, audioFormat);
        try {
            this.myAudioInputStream.close();
        }
        catch (IOException iOException) {
            // empty catch block
        }
        return soundBuffer;
    }

    public boolean endOfStream() {
        return this.endOfStream(false, false);
    }

    public void cleanup() {
        if (this.myAudioInputStream != null) {
            try {
                this.myAudioInputStream.close();
            }
            catch (Exception exception) {
                // empty catch block
            }
        }
        this.myAudioInputStream = null;
    }

    public AudioFormat getAudioFormat() {
        if (this.myAudioInputStream == null) {
            return null;
        }
        return this.myAudioInputStream.getFormat();
    }

    private synchronized boolean initialized(boolean bl2, boolean bl3) {
        if (bl2) {
            this.initialized = bl3;
        }
        return this.initialized;
    }

    private synchronized boolean endOfStream(boolean bl2, boolean bl3) {
        if (bl2) {
            this.endOfStream = bl3;
        }
        return this.endOfStream;
    }

    private static byte[] trimArray(byte[] byArray, int n2) {
        byte[] byArray2 = null;
        if (byArray != null && byArray.length > n2) {
            byArray2 = new byte[n2];
            System.arraycopy(byArray, 0, byArray2, 0, n2);
        }
        return byArray2;
    }

    private static byte[] convertAudioBytes(byte[] byArray, boolean bl2) {
        Object object;
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(byArray.length);
        byteBuffer.order(ByteOrder.nativeOrder());
        ByteBuffer byteBuffer2 = ByteBuffer.wrap(byArray);
        byteBuffer2.order(ByteOrder.LITTLE_ENDIAN);
        if (bl2) {
            object = byteBuffer.asShortBuffer();
            ShortBuffer shortBuffer = byteBuffer2.asShortBuffer();
            while (shortBuffer.hasRemaining()) {
                ((ShortBuffer)object).put(shortBuffer.get());
            }
        } else {
            while (byteBuffer2.hasRemaining()) {
                byteBuffer.put(byteBuffer2.get());
            }
        }
        byteBuffer.rewind();
        if (!byteBuffer.hasArray()) {
            object = new byte[byteBuffer.capacity()];
            byteBuffer.get((byte[])object);
            byteBuffer.clear();
            return object;
        }
        return byteBuffer.array();
    }

    private static byte[] appendByteArrays(byte[] byArray, byte[] byArray2, int n2) {
        byte[] byArray3;
        if (byArray == null && byArray2 == null) {
            return null;
        }
        if (byArray == null) {
            byArray3 = new byte[n2];
            System.arraycopy(byArray2, 0, byArray3, 0, n2);
            byArray2 = null;
        } else if (byArray2 == null) {
            byArray3 = new byte[byArray.length];
            System.arraycopy(byArray, 0, byArray3, 0, byArray.length);
            byArray = null;
        } else {
            byArray3 = new byte[byArray.length + n2];
            System.arraycopy(byArray, 0, byArray3, 0, byArray.length);
            System.arraycopy(byArray2, 0, byArray3, byArray.length, n2);
            byArray = null;
            byArray2 = null;
        }
        return byArray3;
    }

    private void errorMessage(String string) {
        this.logger.errorMessage("CodecWav", string, 0);
    }

    private void printStackTrace(Exception exception) {
        this.logger.printStackTrace(exception, 1);
    }
}

