/*
 * Decompiled with CFR 0.152.
 */
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class af
extends ig {
    public af() {
        this.h = true;
    }

    public af(double d2, double d3, double d4, double d5, boolean bl2) {
        this.a = d2;
        this.b = d3;
        this.d = d4;
        this.c = d5;
        this.g = bl2;
        this.h = true;
    }

    public void a(DataInputStream dataInputStream) {
        this.a = dataInputStream.readDouble();
        this.b = dataInputStream.readDouble();
        this.d = dataInputStream.readDouble();
        this.c = dataInputStream.readDouble();
        super.a(dataInputStream);
    }

    public void a(DataOutputStream dataOutputStream) {
        dataOutputStream.writeDouble(this.a);
        dataOutputStream.writeDouble(this.b);
        dataOutputStream.writeDouble(this.d);
        dataOutputStream.writeDouble(this.c);
        super.a(dataOutputStream);
    }

    public int a() {
        return 33;
    }
}

