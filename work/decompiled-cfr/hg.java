/*
 * Decompiled with CFR 0.152.
 */
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class hg
extends ki {
    public long a;

    public void a(DataInputStream dataInputStream) {
        this.a = dataInputStream.readLong();
    }

    public void a(DataOutputStream dataOutputStream) {
        dataOutputStream.writeLong(this.a);
    }

    public void a(ti ti2) {
        ti2.a(this);
    }

    public int a() {
        return 8;
    }
}

