/*
 * Decompiled with CFR 0.152.
 */
import java.util.HashMap;
import java.util.Map;

public class jc {
    private static Map a = new HashMap();
    private static Map b = new HashMap();
    private static Map c = new HashMap();
    private static Map d = new HashMap();

    private static void a(Class clazz, String string, int n2) {
        a.put(string, clazz);
        b.put(clazz, string);
        c.put(n2, clazz);
        d.put(clazz, n2);
    }

    public static sn a(String string, fd fd2) {
        sn sn2 = null;
        try {
            Class clazz = (Class)a.get(string);
            if (clazz != null) {
                sn2 = (sn)clazz.getConstructor(fd.class).newInstance(fd2);
            }
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
        return sn2;
    }

    public static sn a(nu nu2, fd fd2) {
        sn sn2 = null;
        try {
            Class clazz = (Class)a.get(nu2.i("id"));
            if (clazz != null) {
                sn2 = (sn)clazz.getConstructor(fd.class).newInstance(fd2);
            }
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
        if (sn2 != null) {
            sn2.e(nu2);
        } else {
            System.out.println("Skipping Entity with id " + nu2.i("id"));
        }
        return sn2;
    }

    public static sn a(int n2, fd fd2) {
        sn sn2 = null;
        try {
            Class clazz = (Class)c.get(n2);
            if (clazz != null) {
                sn2 = (sn)clazz.getConstructor(fd.class).newInstance(fd2);
            }
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
        if (sn2 == null) {
            System.out.println("Skipping Entity with id " + n2);
        }
        return sn2;
    }

    public static int a(sn sn2) {
        return (Integer)d.get(sn2.getClass());
    }

    public static String b(sn sn2) {
        return (String)b.get(sn2.getClass());
    }

    static {
        jc.a(sl.class, "Arrow", 10);
        jc.a(by.class, "Snowball", 11);
        jc.a(hl.class, "Item", 1);
        jc.a(qv.class, "Painting", 9);
        jc.a(ls.class, "Mob", 48);
        jc.a(gz.class, "Monster", 49);
        jc.a(gb.class, "Creeper", 50);
        jc.a(fr.class, "Skeleton", 51);
        jc.a(cn.class, "Spider", 52);
        jc.a(nt.class, "Giant", 53);
        jc.a(uz.class, "Zombie", 54);
        jc.a(uw.class, "Slime", 55);
        jc.a(bp.class, "Ghast", 56);
        jc.a(ya.class, "PigZombie", 57);
        jc.a(wh.class, "Pig", 90);
        jc.a(dl.class, "Sheep", 91);
        jc.a(bx.class, "Cow", 92);
        jc.a(ww.class, "Chicken", 93);
        jc.a(xt.class, "Squid", 94);
        jc.a(gi.class, "Wolf", 95);
        jc.a(qw.class, "PrimedTnt", 20);
        jc.a(ju.class, "FallingSand", 21);
        jc.a(yl.class, "Minecart", 40);
        jc.a(fz.class, "Boat", 41);
    }
}

