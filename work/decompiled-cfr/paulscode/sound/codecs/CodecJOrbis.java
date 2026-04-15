/*
 * Decompiled with CFR 0.152.
 */
package paulscode.sound.codecs;

import com.jcraft.jogg.Packet;
import com.jcraft.jogg.Page;
import com.jcraft.jogg.StreamState;
import com.jcraft.jogg.SyncState;
import com.jcraft.jorbis.Block;
import com.jcraft.jorbis.Comment;
import com.jcraft.jorbis.DspState;
import com.jcraft.jorbis.Info;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownServiceException;
import java.nio.ByteOrder;
import javax.sound.sampled.AudioFormat;
import paulscode.sound.ICodec;
import paulscode.sound.SoundBuffer;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.SoundSystemLogger;

public class CodecJOrbis
implements ICodec {
    private static final boolean GET = false;
    private static final boolean SET = true;
    private static final boolean XXX = false;
    protected URL url;
    protected URLConnection urlConnection = null;
    private InputStream inputStream;
    private AudioFormat audioFormat;
    private boolean endOfStream = false;
    private boolean initialized = false;
    private byte[] buffer = null;
    private int bufferSize;
    private int count = 0;
    private int index = 0;
    private int convertedBufferSize;
    private float[][][] pcmInfo;
    private int[] pcmIndex;
    private Packet joggPacket = new Packet();
    private Page joggPage = new Page();
    private StreamState joggStreamState = new StreamState();
    private SyncState joggSyncState = new SyncState();
    private DspState jorbisDspState = new DspState();
    private Block jorbisBlock = new Block(this.jorbisDspState);
    private Comment jorbisComment = new Comment();
    private Info jorbisInfo = new Info();
    private SoundSystemLogger logger = SoundSystemConfig.getLogger();
    private static final boolean LITTLE_ENDIAN = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN;

    public void reverseByteOrder(boolean bl2) {
    }

    public boolean initialize(URL uRL) {
        this.initialized(true, false);
        if (this.joggStreamState != null) {
            this.joggStreamState.clear();
        }
        if (this.jorbisBlock != null) {
            this.jorbisBlock.clear();
        }
        if (this.jorbisDspState != null) {
            this.jorbisDspState.clear();
        }
        if (this.jorbisInfo != null) {
            this.jorbisInfo.clear();
        }
        if (this.joggSyncState != null) {
            this.joggSyncState.clear();
        }
        if (this.inputStream != null) {
            try {
                this.inputStream.close();
            }
            catch (IOException iOException) {
                // empty catch block
            }
        }
        this.url = uRL;
        this.bufferSize = SoundSystemConfig.getStreamingBufferSize() / 2;
        this.buffer = null;
        this.count = 0;
        this.index = 0;
        this.joggStreamState = new StreamState();
        this.jorbisBlock = new Block(this.jorbisDspState);
        this.jorbisDspState = new DspState();
        this.jorbisInfo = new Info();
        this.joggSyncState = new SyncState();
        try {
            this.urlConnection = uRL.openConnection();
        }
        catch (UnknownServiceException unknownServiceException) {
            this.errorMessage("Unable to create a UrlConnection in method 'initialize'.");
            this.printStackTrace(unknownServiceException);
            this.cleanup();
            return false;
        }
        catch (IOException iOException) {
            this.errorMessage("Unable to create a UrlConnection in method 'initialize'.");
            this.printStackTrace(iOException);
            this.cleanup();
            return false;
        }
        if (this.urlConnection != null) {
            try {
                this.inputStream = this.openInputStream();
            }
            catch (IOException iOException) {
                this.errorMessage("Unable to acquire inputstream in method 'initialize'.");
                this.printStackTrace(iOException);
                this.cleanup();
                return false;
            }
        }
        this.endOfStream(true, false);
        this.joggSyncState.init();
        this.joggSyncState.buffer(this.bufferSize);
        this.buffer = this.joggSyncState.data;
        try {
            if (!this.readHeader()) {
                this.errorMessage("Error reading the header");
                return false;
            }
        }
        catch (IOException iOException) {
            this.errorMessage("Error reading the header");
            return false;
        }
        this.convertedBufferSize = this.bufferSize * 2;
        this.jorbisDspState.synthesis_init(this.jorbisInfo);
        this.jorbisBlock.init(this.jorbisDspState);
        int n2 = this.jorbisInfo.channels;
        int n3 = this.jorbisInfo.rate;
        this.audioFormat = new AudioFormat(n3, 16, n2, true, false);
        this.pcmInfo = new float[1][][];
        this.pcmIndex = new int[this.jorbisInfo.channels];
        this.initialized(true, true);
        return true;
    }

    protected InputStream openInputStream() {
        return this.urlConnection.getInputStream();
    }

    public boolean initialized() {
        return this.initialized(false, false);
    }

    public SoundBuffer read() {
        byte[] byArray = this.readBytes();
        if (byArray == null) {
            return null;
        }
        return new SoundBuffer(byArray, this.audioFormat);
    }

    public SoundBuffer readAll() {
        byte[] byArray = this.readBytes();
        while (!(this.endOfStream(false, false) || (byArray = CodecJOrbis.appendByteArrays(byArray, this.readBytes())) != null && byArray.length >= SoundSystemConfig.getMaxFileSize())) {
        }
        return new SoundBuffer(byArray, this.audioFormat);
    }

    public boolean endOfStream() {
        return this.endOfStream(false, false);
    }

    public void cleanup() {
        this.joggStreamState.clear();
        this.jorbisBlock.clear();
        this.jorbisDspState.clear();
        this.jorbisInfo.clear();
        this.joggSyncState.clear();
        if (this.inputStream != null) {
            try {
                this.inputStream.close();
            }
            catch (IOException iOException) {
                // empty catch block
            }
        }
        this.joggStreamState = null;
        this.jorbisBlock = null;
        this.jorbisDspState = null;
        this.jorbisInfo = null;
        this.joggSyncState = null;
        this.inputStream = null;
    }

    public AudioFormat getAudioFormat() {
        return this.audioFormat;
    }

    private boolean readHeader() {
        this.index = this.joggSyncState.buffer(this.bufferSize);
        int n2 = this.inputStream.read(this.joggSyncState.data, this.index, this.bufferSize);
        if (n2 < 0) {
            n2 = 0;
        }
        this.joggSyncState.wrote(n2);
        if (this.joggSyncState.pageout(this.joggPage) != 1) {
            if (n2 < this.bufferSize) {
                return true;
            }
            this.errorMessage("Ogg header not recognized in method 'readHeader'.");
            return false;
        }
        this.joggStreamState.init(this.joggPage.serialno());
        this.jorbisInfo.init();
        this.jorbisComment.init();
        if (this.joggStreamState.pagein(this.joggPage) < 0) {
            this.errorMessage("Problem with first Ogg header page in method 'readHeader'.");
            return false;
        }
        if (this.joggStreamState.packetout(this.joggPacket) != 1) {
            this.errorMessage("Problem with first Ogg header packet in method 'readHeader'.");
            return false;
        }
        if (this.jorbisInfo.synthesis_headerin(this.jorbisComment, this.joggPacket) < 0) {
            this.errorMessage("File does not contain Vorbis header in method 'readHeader'.");
            return false;
        }
        int n3 = 0;
        while (n3 < 2) {
            int n4;
            while (n3 < 2 && (n4 = this.joggSyncState.pageout(this.joggPage)) != 0) {
                if (n4 != 1) continue;
                this.joggStreamState.pagein(this.joggPage);
                while (n3 < 2 && (n4 = this.joggStreamState.packetout(this.joggPacket)) != 0) {
                    if (n4 == -1) {
                        this.errorMessage("Secondary Ogg header corrupt in method 'readHeader'.");
                        return false;
                    }
                    this.jorbisInfo.synthesis_headerin(this.jorbisComment, this.joggPacket);
                    ++n3;
                }
            }
            this.index = this.joggSyncState.buffer(this.bufferSize);
            n2 = this.inputStream.read(this.joggSyncState.data, this.index, this.bufferSize);
            if (n2 < 0) {
                n2 = 0;
            }
            if (n2 == 0 && n3 < 2) {
                this.errorMessage("End of file reached before finished readingOgg header in method 'readHeader'");
                return false;
            }
            this.joggSyncState.wrote(n2);
        }
        this.index = this.joggSyncState.buffer(this.bufferSize);
        this.buffer = this.joggSyncState.data;
        return true;
    }

    private byte[] readBytes() {
        if (!this.initialized(false, false)) {
            return null;
        }
        if (this.endOfStream(false, false)) {
            return null;
        }
        byte[] byArray = null;
        switch (this.joggSyncState.pageout(this.joggPage)) {
            case -1: 
            case 0: {
                this.endOfStream(true, true);
                break;
            }
            case 1: {
                this.joggStreamState.pagein(this.joggPage);
                if (this.joggPage.granulepos() == 0L) {
                    this.endOfStream(true, true);
                    break;
                }
                block10: while (true) {
                    switch (this.joggStreamState.packetout(this.joggPacket)) {
                        case -1: 
                        case 0: {
                            break block10;
                        }
                        case 1: {
                            byArray = CodecJOrbis.appendByteArrays(byArray, this.decodeCurrentPacket());
                        }
                        default: {
                            continue block10;
                        }
                    }
                    break;
                }
                if (this.joggPage.eos() == 0) break;
                this.endOfStream(true, true);
            }
        }
        if (!this.endOfStream(false, false)) {
            this.index = this.joggSyncState.buffer(this.bufferSize);
            if (this.index == -1) {
                this.endOfStream(true, true);
            } else {
                this.buffer = this.joggSyncState.data;
                try {
                    this.count = this.inputStream.read(this.buffer, this.index, this.bufferSize);
                }
                catch (Exception exception) {
                    this.printStackTrace(exception);
                    return byArray;
                }
                this.joggSyncState.wrote(this.count);
                if (this.count == 0) {
                    this.endOfStream(true, true);
                }
            }
        }
        return byArray;
    }

    private byte[] decodeCurrentPacket() {
        int n2;
        int n3;
        int n4;
        byte[] byArray = new byte[this.convertedBufferSize];
        if (this.jorbisBlock.synthesis(this.joggPacket) == 0) {
            this.jorbisDspState.synthesis_blockin(this.jorbisBlock);
        }
        int n5 = this.convertedBufferSize / (this.jorbisInfo.channels * 2);
        for (n3 = 0; n3 < this.convertedBufferSize && (n2 = this.jorbisDspState.synthesis_pcmout(this.pcmInfo, this.pcmIndex)) > 0; n3 += n4 * this.jorbisInfo.channels * 2) {
            n4 = n2 < n5 ? n2 : n5;
            for (int i2 = 0; i2 < this.jorbisInfo.channels; ++i2) {
                int n6 = i2 * 2;
                for (int i3 = 0; i3 < n4; ++i3) {
                    int n7 = (int)(this.pcmInfo[0][i2][this.pcmIndex[i2] + i3] * 32767.0f);
                    if (n7 > Short.MAX_VALUE) {
                        n7 = Short.MAX_VALUE;
                    }
                    if (n7 < Short.MIN_VALUE) {
                        n7 = Short.MIN_VALUE;
                    }
                    if (n7 < 0) {
                        n7 |= 0x8000;
                    }
                    if (LITTLE_ENDIAN) {
                        byArray[n3 + n6] = (byte)n7;
                        byArray[n3 + n6 + 1] = (byte)(n7 >>> 8);
                    } else {
                        byArray[n3 + n6 + 1] = (byte)n7;
                        byArray[n3 + n6] = (byte)(n7 >>> 8);
                    }
                    n6 += 2 * this.jorbisInfo.channels;
                }
            }
            this.jorbisDspState.synthesis_read(n4);
        }
        byArray = CodecJOrbis.trimArray(byArray, n3);
        return byArray;
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

    private static byte[] appendByteArrays(byte[] byArray, byte[] byArray2) {
        byte[] byArray3;
        if (byArray == null && byArray2 == null) {
            return null;
        }
        if (byArray == null) {
            byArray3 = new byte[byArray2.length];
            System.arraycopy(byArray2, 0, byArray3, 0, byArray2.length);
            byArray2 = null;
        } else if (byArray2 == null) {
            byArray3 = new byte[byArray.length];
            System.arraycopy(byArray, 0, byArray3, 0, byArray.length);
            byArray = null;
        } else {
            byArray3 = new byte[byArray.length + byArray2.length];
            System.arraycopy(byArray, 0, byArray3, 0, byArray.length);
            System.arraycopy(byArray2, 0, byArray3, byArray.length, byArray2.length);
            byArray = null;
            byArray2 = null;
        }
        return byArray3;
    }

    private void errorMessage(String string) {
        this.logger.errorMessage("CodecJOrbis", string, 0);
    }

    private void printStackTrace(Exception exception) {
        this.logger.printStackTrace(exception, 1);
    }
}

