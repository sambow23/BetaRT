/*
 * Decompiled with CFR 0.152.
 */
import java.io.DataInput;
import java.io.DataOutput;

public abstract class ij {
    private String a = null;

    abstract void a(DataOutput var1);

    abstract void a(DataInput var1);

    public abstract byte a();

    public String b() {
        if (this.a == null) {
            return "";
        }
        return this.a;
    }

    public ij a(String string) {
        this.a = string;
        return this;
    }

    public static ij b(DataInput dataInput) {
        byte by2 = dataInput.readByte();
        if (by2 == 0) {
            return new lh();
        }
        ij ij2 = ij.a(by2);
        ij2.a = dataInput.readUTF();
        ij2.a(dataInput);
        return ij2;
    }

    public static void a(ij ij2, DataOutput dataOutput) {
        dataOutput.writeByte(ij2.a());
        if (ij2.a() == 0) {
            return;
        }
        dataOutput.writeUTF(ij2.b());
        ij2.a(dataOutput);
    }

    public static ij a(byte by2) {
        switch (by2) {
            case 0: {
                return new lh();
            }
            case 1: {
                return new qp();
            }
            case 2: {
                return new ul();
            }
            case 3: {
                return new pp();
            }
            case 4: {
                return new mi();
            }
            case 5: {
                return new p();
            }
            case 6: {
                return new sz();
            }
            case 7: {
                return new hn();
            }
            case 8: {
                return new xb();
            }
            case 9: {
                return new sp();
            }
            case 10: {
                return new nu();
            }
        }
        return null;
    }

    public static String b(byte by2) {
        switch (by2) {
            case 0: {
                return "TAG_End";
            }
            case 1: {
                return "TAG_Byte";
            }
            case 2: {
                return "TAG_Short";
            }
            case 3: {
                return "TAG_Int";
            }
            case 4: {
                return "TAG_Long";
            }
            case 5: {
                return "TAG_Float";
            }
            case 6: {
                return "TAG_Double";
            }
            case 7: {
                return "TAG_Byte_Array";
            }
            case 8: {
                return "TAG_String";
            }
            case 9: {
                return "TAG_List";
            }
            case 10: {
                return "TAG_Compound";
            }
        }
        return "UNKNOWN";
    }
}

