/*
 * Decompiled with CFR 0.152.
 */
import java.io.DataInput;
import java.io.DataOutput;

public class mi
extends ij {
    public long a;

    public mi() {
    }

    public mi(long l2) {
        this.a = l2;
    }

    void a(DataOutput dataOutput) {
        dataOutput.writeLong(this.a);
    }

    void a(DataInput dataInput) {
        this.a = dataInput.readLong();
    }

    public byte a() {
        return 4;
    }

    public String toString() {
        return "" + this.a;
    }
}

