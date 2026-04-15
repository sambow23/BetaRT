/*
 * Decompiled with CFR 0.152.
 */
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.HashSet;
import java.util.Set;

public class rm
extends ki {
    public double a;
    public double b;
    public double c;
    public float d;
    public Set e;

    public void a(DataInputStream dataInputStream) {
        this.a = dataInputStream.readDouble();
        this.b = dataInputStream.readDouble();
        this.c = dataInputStream.readDouble();
        this.d = dataInputStream.readFloat();
        int n2 = dataInputStream.readInt();
        this.e = new HashSet();
        int n3 = (int)this.a;
        int n4 = (int)this.b;
        int n5 = (int)this.c;
        for (int i2 = 0; i2 < n2; ++i2) {
            int n6 = dataInputStream.readByte() + n3;
            int n7 = dataInputStream.readByte() + n4;
            int n8 = dataInputStream.readByte() + n5;
            this.e.add(new wf(n6, n7, n8));
        }
    }

    public void a(DataOutputStream dataOutputStream) {
        dataOutputStream.writeDouble(this.a);
        dataOutputStream.writeDouble(this.b);
        dataOutputStream.writeDouble(this.c);
        dataOutputStream.writeFloat(this.d);
        dataOutputStream.writeInt(this.e.size());
        int n2 = (int)this.a;
        int n3 = (int)this.b;
        int n4 = (int)this.c;
        for (wf wf2 : this.e) {
            int n5 = wf2.a - n2;
            int n6 = wf2.b - n3;
            int n7 = wf2.c - n4;
            dataOutputStream.writeByte(n5);
            dataOutputStream.writeByte(n6);
            dataOutputStream.writeByte(n7);
        }
    }

    public void a(ti ti2) {
        ti2.a(this);
    }

    public int a() {
        return 32 + this.e.size() * 3;
    }
}

