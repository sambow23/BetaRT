/*
 * Decompiled with CFR 0.152.
 */
import java.io.ByteArrayOutputStream;

class hz
extends ByteArrayOutputStream {
    private int b;
    private int c;
    final /* synthetic */ qj a;

    public hz(qj qj2, int n2, int n3) {
        this.a = qj2;
        super(8096);
        this.b = n2;
        this.c = n3;
    }

    public void close() {
        this.a.a(this.b, this.c, this.buf, this.count);
    }
}

