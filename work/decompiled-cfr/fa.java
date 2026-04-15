/*
 * Decompiled with CFR 0.152.
 */
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class fa
extends ki {
    public int a;
    public int b;
    public int c;
    public int d;
    public int e;

    public fa() {
    }

    public fa(sn sn2) {
        this.a = sn2.aD;
        this.b = in.b(sn2.aM * 32.0);
        this.c = in.b(sn2.aN * 32.0);
        this.d = in.b(sn2.aO * 32.0);
        if (sn2 instanceof c) {
            this.e = 1;
        }
    }

    public void a(DataInputStream dataInputStream) {
        this.a = dataInputStream.readInt();
        this.e = dataInputStream.readByte();
        this.b = dataInputStream.readInt();
        this.c = dataInputStream.readInt();
        this.d = dataInputStream.readInt();
    }

    public void a(DataOutputStream dataOutputStream) {
        dataOutputStream.writeInt(this.a);
        dataOutputStream.writeByte(this.e);
        dataOutputStream.writeInt(this.b);
        dataOutputStream.writeInt(this.c);
        dataOutputStream.writeInt(this.d);
    }

    public void a(ti ti2) {
        ti2.a(this);
    }

    public int a() {
        return 17;
    }
}

