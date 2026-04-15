/*
 * Decompiled with CFR 0.152.
 */
import java.io.InputStream;
import paulscode.sound.codecs.CodecJOrbis;

public class io
extends CodecJOrbis {
    protected InputStream openInputStream() {
        return new np(this, this.url, this.urlConnection.getInputStream());
    }
}

