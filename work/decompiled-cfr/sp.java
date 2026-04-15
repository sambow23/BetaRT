/*
 * Decompiled with CFR 0.152.
 */
import java.io.DataInput;
import java.io.DataOutput;
import java.util.ArrayList;
import java.util.List;

public class sp
extends ij {
    private List a = new ArrayList();
    private byte b;

    void a(DataOutput dataOutput) {
        this.b = this.a.size() > 0 ? ((ij)this.a.get(0)).a() : (byte)1;
        dataOutput.writeByte(this.b);
        dataOutput.writeInt(this.a.size());
        for (int i2 = 0; i2 < this.a.size(); ++i2) {
            ((ij)this.a.get(i2)).a(dataOutput);
        }
    }

    void a(DataInput dataInput) {
        this.b = dataInput.readByte();
        int n2 = dataInput.readInt();
        this.a = new ArrayList();
        for (int i2 = 0; i2 < n2; ++i2) {
            ij ij2 = ij.a(this.b);
            ij2.a(dataInput);
            this.a.add(ij2);
        }
    }

    public byte a() {
        return 9;
    }

    public String toString() {
        return "" + this.a.size() + " entries of type " + ij.b(this.b);
    }

    public void a(ij ij2) {
        this.b = ij2.a();
        this.a.add(ij2);
    }

    public ij a(int n2) {
        return (ij)this.a.get(n2);
    }

    public int c() {
        return this.a.size();
    }
}

