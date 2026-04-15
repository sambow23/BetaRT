/*
 * Decompiled with CFR 0.152.
 */
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class ef
extends ki {
    public int a;
    public int b;
    public int c;
    public int d;
    public int e;
    public int f;
    public byte[] g;
    private int h;

    public ef() {
        this.k = true;
    }

    public void a(DataInputStream dataInputStream) {
        this.a = dataInputStream.readInt();
        this.b = dataInputStream.readShort();
        this.c = dataInputStream.readInt();
        this.d = dataInputStream.read() + 1;
        this.e = dataInputStream.read() + 1;
        this.f = dataInputStream.read() + 1;
        this.h = dataInputStream.readInt();
        byte[] byArray = new byte[this.h];
        dataInputStream.readFully(byArray);
        this.g = new byte[this.d * this.e * this.f * 5 / 2];
        Inflater inflater = new Inflater();
        inflater.setInput(byArray);
        try {
            inflater.inflate(this.g);
        }
        catch (DataFormatException dataFormatException) {
            throw new IOException("Bad compressed data format");
        }
        finally {
            inflater.end();
        }
    }

    public void a(DataOutputStream dataOutputStream) {
        dataOutputStream.writeInt(this.a);
        dataOutputStream.writeShort(this.b);
        dataOutputStream.writeInt(this.c);
        dataOutputStream.write(this.d - 1);
        dataOutputStream.write(this.e - 1);
        dataOutputStream.write(this.f - 1);
        dataOutputStream.writeInt(this.h);
        dataOutputStream.write(this.g, 0, this.h);
    }

    public void a(ti ti2) {
        ti2.a(this);
    }

    public int a() {
        return 17 + this.h;
    }
}

