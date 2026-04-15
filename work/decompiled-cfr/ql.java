/*
 * Decompiled with CFR 0.152.
 */
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class ql
extends ki {
    private float a;
    private float b;
    private boolean c;
    private boolean d;
    private float e;
    private float f;

    public void a(DataInputStream dataInputStream) {
        this.a = dataInputStream.readFloat();
        this.b = dataInputStream.readFloat();
        this.e = dataInputStream.readFloat();
        this.f = dataInputStream.readFloat();
        this.c = dataInputStream.readBoolean();
        this.d = dataInputStream.readBoolean();
    }

    public void a(DataOutputStream dataOutputStream) {
        dataOutputStream.writeFloat(this.a);
        dataOutputStream.writeFloat(this.b);
        dataOutputStream.writeFloat(this.e);
        dataOutputStream.writeFloat(this.f);
        dataOutputStream.writeBoolean(this.c);
        dataOutputStream.writeBoolean(this.d);
    }

    public void a(ti ti2) {
        ti2.a(this);
    }

    public int a() {
        return 18;
    }
}

