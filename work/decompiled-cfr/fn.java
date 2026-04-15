/*
 * Decompiled with CFR 0.152.
 */
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class fn
extends ki {
    public int a;
    public int b;
    public int c;
    public int d;
    public int e;

    public void a(DataInputStream dataInputStream) {
        this.a = dataInputStream.readInt();
        this.c = dataInputStream.readInt();
        this.d = dataInputStream.readByte();
        this.e = dataInputStream.readInt();
        this.b = dataInputStream.readInt();
    }

    public void a(DataOutputStream dataOutputStream) {
        dataOutputStream.writeInt(this.a);
        dataOutputStream.writeInt(this.c);
        dataOutputStream.writeByte(this.d);
        dataOutputStream.writeInt(this.e);
        dataOutputStream.writeInt(this.b);
    }

    public void a(ti ti2) {
        ti2.a(this);
    }

    public int a() {
        return 20;
    }
}

