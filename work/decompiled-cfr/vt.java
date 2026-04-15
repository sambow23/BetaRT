/*
 * Decompiled with CFR 0.152.
 */
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class vt
extends ki {
    public int a;
    public int b;
    public int c;
    public int d;
    public int e;
    public String f;

    public vt() {
    }

    public vt(qv qv2) {
        this.a = qv2.aD;
        this.b = qv2.b;
        this.c = qv2.c;
        this.d = qv2.d;
        this.e = qv2.a;
        this.f = qv2.e.A;
    }

    public void a(DataInputStream dataInputStream) {
        this.a = dataInputStream.readInt();
        this.f = vt.a(dataInputStream, iq.z);
        this.b = dataInputStream.readInt();
        this.c = dataInputStream.readInt();
        this.d = dataInputStream.readInt();
        this.e = dataInputStream.readInt();
    }

    public void a(DataOutputStream dataOutputStream) {
        dataOutputStream.writeInt(this.a);
        vt.a(this.f, dataOutputStream);
        dataOutputStream.writeInt(this.b);
        dataOutputStream.writeInt(this.c);
        dataOutputStream.writeInt(this.d);
        dataOutputStream.writeInt(this.e);
    }

    public void a(ti ti2) {
        ti2.a(this);
    }

    public int a() {
        return 24;
    }
}

