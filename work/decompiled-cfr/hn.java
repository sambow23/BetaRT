/*
 * Decompiled with CFR 0.152.
 */
import java.io.DataInput;
import java.io.DataOutput;

public class hn
extends ij {
    public byte[] a;

    public hn() {
    }

    public hn(byte[] byArray) {
        this.a = byArray;
    }

    void a(DataOutput dataOutput) {
        dataOutput.writeInt(this.a.length);
        dataOutput.write(this.a);
    }

    void a(DataInput dataInput) {
        int n2 = dataInput.readInt();
        this.a = new byte[n2];
        dataInput.readFully(this.a);
    }

    public byte a() {
        return 7;
    }

    public String toString() {
        return "[" + this.a.length + " bytes]";
    }
}

