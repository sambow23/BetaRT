/*
 * Decompiled with CFR 0.152.
 */
import java.io.DataInput;
import java.io.DataOutput;

public class xb
extends ij {
    public String a;

    public xb() {
    }

    public xb(String string) {
        this.a = string;
        if (string == null) {
            throw new IllegalArgumentException("Empty string not allowed");
        }
    }

    void a(DataOutput dataOutput) {
        dataOutput.writeUTF(this.a);
    }

    void a(DataInput dataInput) {
        this.a = dataInput.readUTF();
    }

    public byte a() {
        return 8;
    }

    public String toString() {
        return "" + this.a;
    }
}

