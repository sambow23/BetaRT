/*
 * Decompiled with CFR 0.152.
 */
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ud {
    private static final HashMap a = new HashMap();
    private final Map b = new HashMap();
    private boolean c;

    public void a(int n2, Object object) {
        Integer n3 = (Integer)a.get(object.getClass());
        if (n3 == null) {
            throw new IllegalArgumentException("Unknown data type: " + object.getClass());
        }
        if (n2 > 31) {
            throw new IllegalArgumentException("Data value id is too big with " + n2 + "! (Max is " + 31 + ")");
        }
        if (this.b.containsKey(n2)) {
            throw new IllegalArgumentException("Duplicate id value for " + n2 + "!");
        }
        ma ma2 = new ma(n3, n2, object);
        this.b.put(n2, ma2);
    }

    public byte a(int n2) {
        return (Byte)((ma)this.b.get(n2)).b();
    }

    public int b(int n2) {
        return (Integer)((ma)this.b.get(n2)).b();
    }

    public String c(int n2) {
        return (String)((ma)this.b.get(n2)).b();
    }

    public void b(int n2, Object object) {
        ma ma2 = (ma)this.b.get(n2);
        if (!object.equals(ma2.b())) {
            ma2.a(object);
            ma2.a(true);
            this.c = true;
        }
    }

    public static void a(List list, DataOutputStream dataOutputStream) {
        if (list != null) {
            for (ma ma2 : list) {
                ud.a(dataOutputStream, ma2);
            }
        }
        dataOutputStream.writeByte(127);
    }

    public void a(DataOutputStream dataOutputStream) {
        for (ma ma2 : this.b.values()) {
            ud.a(dataOutputStream, ma2);
        }
        dataOutputStream.writeByte(127);
    }

    private static void a(DataOutputStream dataOutputStream, ma ma2) {
        int n2 = (ma2.c() << 5 | ma2.a() & 0x1F) & 0xFF;
        dataOutputStream.writeByte(n2);
        switch (ma2.c()) {
            case 0: {
                dataOutputStream.writeByte(((Byte)ma2.b()).byteValue());
                break;
            }
            case 1: {
                dataOutputStream.writeShort(((Short)ma2.b()).shortValue());
                break;
            }
            case 2: {
                dataOutputStream.writeInt((Integer)ma2.b());
                break;
            }
            case 3: {
                dataOutputStream.writeFloat(((Float)ma2.b()).floatValue());
                break;
            }
            case 4: {
                ki.a((String)ma2.b(), dataOutputStream);
                break;
            }
            case 5: {
                iz iz2 = (iz)ma2.b();
                dataOutputStream.writeShort(iz2.a().bf);
                dataOutputStream.writeByte(iz2.a);
                dataOutputStream.writeShort(iz2.i());
                break;
            }
            case 6: {
                br br2 = (br)ma2.b();
                dataOutputStream.writeInt(br2.a);
                dataOutputStream.writeInt(br2.b);
                dataOutputStream.writeInt(br2.c);
            }
        }
    }

    public static List a(DataInputStream dataInputStream) {
        ArrayList<ma> arrayList = null;
        byte by2 = dataInputStream.readByte();
        while (by2 != 127) {
            if (arrayList == null) {
                arrayList = new ArrayList<ma>();
            }
            int n2 = (by2 & 0xE0) >> 5;
            int n3 = by2 & 0x1F;
            ma ma2 = null;
            switch (n2) {
                case 0: {
                    ma2 = new ma(n2, n3, dataInputStream.readByte());
                    break;
                }
                case 1: {
                    ma2 = new ma(n2, n3, dataInputStream.readShort());
                    break;
                }
                case 2: {
                    ma2 = new ma(n2, n3, dataInputStream.readInt());
                    break;
                }
                case 3: {
                    ma2 = new ma(n2, n3, Float.valueOf(dataInputStream.readFloat()));
                    break;
                }
                case 4: {
                    ma2 = new ma(n2, n3, ki.a(dataInputStream, 64));
                    break;
                }
                case 5: {
                    int n4 = dataInputStream.readShort();
                    int n5 = dataInputStream.readByte();
                    int n6 = dataInputStream.readShort();
                    ma2 = new ma(n2, n3, new iz(n4, n5, n6));
                    break;
                }
                case 6: {
                    int n4 = dataInputStream.readInt();
                    int n5 = dataInputStream.readInt();
                    int n6 = dataInputStream.readInt();
                    ma2 = new ma(n2, n3, new br(n4, n5, n6));
                }
            }
            arrayList.add(ma2);
            by2 = dataInputStream.readByte();
        }
        return arrayList;
    }

    public void a(List list) {
        for (ma ma2 : list) {
            ma ma3 = (ma)this.b.get(ma2.a());
            if (ma3 == null) continue;
            ma3.a(ma2.b());
        }
    }

    static {
        a.put(Byte.class, 0);
        a.put(Short.class, 1);
        a.put(Integer.class, 2);
        a.put(Float.class, 3);
        a.put(String.class, 4);
        a.put(iz.class, 5);
        a.put(br.class, 6);
    }
}

