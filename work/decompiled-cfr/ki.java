/*
 * Decompiled with CFR 0.152.
 */
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class ki {
    private static Map a = new HashMap();
    private static Map b = new HashMap();
    private static Set c = new HashSet();
    private static Set d = new HashSet();
    public final long j = System.currentTimeMillis();
    public boolean k = false;
    private static HashMap e;
    private static int f;

    static void a(int n2, boolean bl2, boolean bl3, Class clazz) {
        if (a.containsKey(n2)) {
            throw new IllegalArgumentException("Duplicate packet id:" + n2);
        }
        if (b.containsKey(clazz)) {
            throw new IllegalArgumentException("Duplicate packet class:" + clazz);
        }
        a.put(n2, clazz);
        b.put(clazz, n2);
        if (bl2) {
            c.add(n2);
        }
        if (bl3) {
            d.add(n2);
        }
    }

    public static ki a(int n2) {
        try {
            Class clazz = (Class)a.get(n2);
            if (clazz == null) {
                return null;
            }
            return (ki)clazz.newInstance();
        }
        catch (Exception exception) {
            exception.printStackTrace();
            System.out.println("Skipping packet with id " + n2);
            return null;
        }
    }

    public final int c() {
        return (Integer)b.get(this.getClass());
    }

    public static ki a(DataInputStream dataInputStream, boolean bl2) {
        int n2 = 0;
        ki ki2 = null;
        try {
            n2 = dataInputStream.read();
            if (n2 == -1) {
                return null;
            }
            if (bl2 && !d.contains(n2) || !bl2 && !c.contains(n2)) {
                throw new IOException("Bad packet id " + n2);
            }
            ki2 = ki.a(n2);
            if (ki2 == null) {
                throw new IOException("Bad packet id " + n2);
            }
            ki2.a(dataInputStream);
        }
        catch (EOFException eOFException) {
            System.out.println("Reached end of stream");
            return null;
        }
        nv nv2 = (nv)e.get(n2);
        if (nv2 == null) {
            nv2 = new nv(null);
            e.put(n2, nv2);
        }
        nv2.a(ki2.a());
        if (++f % 1000 == 0) {
            // empty if block
        }
        return ki2;
    }

    public static void a(ki ki2, DataOutputStream dataOutputStream) {
        dataOutputStream.write(ki2.c());
        ki2.a(dataOutputStream);
    }

    public static void a(String string, DataOutputStream dataOutputStream) {
        if (string.length() > Short.MAX_VALUE) {
            throw new IOException("String too big");
        }
        dataOutputStream.writeShort(string.length());
        dataOutputStream.writeChars(string);
    }

    public static String a(DataInputStream dataInputStream, int n2) {
        int n3 = dataInputStream.readShort();
        if (n3 > n2) {
            throw new IOException("Received string length longer than maximum allowed (" + n3 + " > " + n2 + ")");
        }
        if (n3 < 0) {
            throw new IOException("Received string length is less than zero! Weird string!");
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (int i2 = 0; i2 < n3; ++i2) {
            stringBuilder.append(dataInputStream.readChar());
        }
        return stringBuilder.toString();
    }

    public abstract void a(DataInputStream var1);

    public abstract void a(DataOutputStream var1);

    public abstract void a(ti var1);

    public abstract int a();

    static {
        ki.a(0, true, true, lz.class);
        ki.a(1, true, true, nz.class);
        ki.a(2, true, true, mp.class);
        ki.a(3, true, true, pe.class);
        ki.a(4, true, false, hg.class);
        ki.a(5, true, false, s.class);
        ki.a(6, true, false, rc.class);
        ki.a(7, false, true, a.class);
        ki.a(8, true, false, eu.class);
        ki.a(9, true, true, ox.class);
        ki.a(10, true, true, ig.class);
        ki.a(11, true, true, af.class);
        ki.a(12, true, true, vh.class);
        ki.a(13, true, true, ev.class);
        ki.a(14, false, true, jv.class);
        ki.a(15, false, true, gx.class);
        ki.a(16, false, true, ho.class);
        ki.a(17, true, false, jz.class);
        ki.a(18, true, true, nm.class);
        ki.a(19, false, true, ts.class);
        ki.a(20, true, false, mf.class);
        ki.a(21, true, false, nd.class);
        ki.a(22, true, false, di.class);
        ki.a(23, true, false, so.class);
        ki.a(24, true, false, jm.class);
        ki.a(25, true, false, vt.class);
        ki.a(27, false, true, ql.class);
        ki.a(28, true, false, gj.class);
        ki.a(29, true, false, rv.class);
        ki.a(30, true, false, uh.class);
        ki.a(31, true, false, sv.class);
        ki.a(32, true, false, sb.class);
        ki.a(33, true, false, pz.class);
        ki.a(34, true, false, rg.class);
        ki.a(38, true, false, jf.class);
        ki.a(39, true, false, ns.class);
        ki.a(40, true, false, ux.class);
        ki.a(50, true, false, se.class);
        ki.a(51, true, false, ef.class);
        ki.a(52, true, false, wu.class);
        ki.a(53, true, false, tv.class);
        ki.a(54, true, false, vw.class);
        ki.a(60, true, false, rm.class);
        ki.a(61, true, false, fn.class);
        ki.a(70, true, false, ca.class);
        ki.a(71, true, false, fa.class);
        ki.a(100, true, false, iw.class);
        ki.a(101, true, true, mn.class);
        ki.a(102, false, true, qs.class);
        ki.a(103, true, false, hq.class);
        ki.a(104, true, false, kb.class);
        ki.a(105, true, false, mv.class);
        ki.a(106, true, true, oj.class);
        ki.a(130, true, true, ui.class);
        ki.a(131, true, false, ai.class);
        ki.a(200, true, false, of.class);
        ki.a(255, true, true, yr.class);
        e = new HashMap();
        f = 0;
    }
}

