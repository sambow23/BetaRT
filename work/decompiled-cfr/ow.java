/*
 * Decompiled with CFR 0.152.
 */
import java.util.HashMap;
import java.util.Map;

public class ow {
    private static Map a = new HashMap();
    private static Map b = new HashMap();
    public fd d;
    public int e;
    public int f;
    public int g;
    protected boolean h;

    private static void a(Class clazz, String string) {
        if (b.containsKey(string)) {
            throw new IllegalArgumentException("Duplicate id: " + string);
        }
        a.put(string, clazz);
        b.put(clazz, string);
    }

    public void a(nu nu2) {
        this.e = nu2.e("x");
        this.f = nu2.e("y");
        this.g = nu2.e("z");
    }

    public void b(nu nu2) {
        String string = (String)b.get(this.getClass());
        if (string == null) {
            throw new RuntimeException(this.getClass() + " is missing a mapping! This is a bug!");
        }
        nu2.a("id", string);
        nu2.a("x", this.e);
        nu2.a("y", this.f);
        nu2.a("z", this.g);
    }

    public void n_() {
    }

    public static ow c(nu nu2) {
        ow ow2 = null;
        try {
            Class clazz = (Class)a.get(nu2.i("id"));
            if (clazz != null) {
                ow2 = (ow)clazz.newInstance();
            }
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
        if (ow2 != null) {
            ow2.a(nu2);
        } else {
            System.out.println("Skipping TileEntity with id " + nu2.i("id"));
        }
        return ow2;
    }

    public int e() {
        return this.d.e(this.e, this.f, this.g);
    }

    public void y_() {
        if (this.d != null) {
            this.d.b(this.e, this.f, this.g, this);
        }
    }

    public double a(double d2, double d3, double d4) {
        double d5 = (double)this.e + 0.5 - d2;
        double d6 = (double)this.f + 0.5 - d3;
        double d7 = (double)this.g + 0.5 - d4;
        return d5 * d5 + d6 * d6 + d7 * d7;
    }

    public uu f() {
        return uu.m[this.d.a(this.e, this.f, this.g)];
    }

    public boolean g() {
        return this.h;
    }

    public void i() {
        this.h = true;
    }

    public void j() {
        this.h = false;
    }

    static {
        ow.a(sk.class, "Furnace");
        ow.a(js.class, "Chest");
        ow.a(eg.class, "RecordPlayer");
        ow.a(az.class, "Trap");
        ow.a(yk.class, "Sign");
        ow.a(cy.class, "MobSpawner");
        ow.a(tn.class, "Music");
        ow.a(uk.class, "Piston");
    }
}

