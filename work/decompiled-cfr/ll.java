/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.lwjgl.opengl.GL11
 */
import java.util.HashMap;
import java.util.Map;
import org.lwjgl.opengl.GL11;

public class ll {
    private Map m = new HashMap();
    public static ll a = new ll();
    private sj n;
    public static double b;
    public static double c;
    public static double d;
    public ji e;
    public fd f;
    public ls g;
    public float h;
    public float i;
    public double j;
    public double k;
    public double l;

    private ll() {
        this.m.put(yk.class, new po());
        this.m.put(cy.class, new ag());
        this.m.put(uk.class, new hy());
        for (je je2 : this.m.values()) {
            je2.a(this);
        }
    }

    public je a(Class clazz) {
        je je2 = (je)this.m.get(clazz);
        if (je2 == null && clazz != ow.class) {
            je2 = this.a(clazz.getSuperclass());
            this.m.put(clazz, je2);
        }
        return je2;
    }

    public boolean a(ow ow2) {
        return this.b(ow2) != null;
    }

    public je b(ow ow2) {
        if (ow2 == null) {
            return null;
        }
        return this.a(ow2.getClass());
    }

    public void a(fd fd2, ji ji2, sj sj2, ls ls2, float f2) {
        if (this.f != fd2) {
            this.a(fd2);
        }
        this.e = ji2;
        this.g = ls2;
        this.n = sj2;
        this.h = ls2.aU + (ls2.aS - ls2.aU) * f2;
        this.i = ls2.aV + (ls2.aT - ls2.aV) * f2;
        this.j = ls2.bl + (ls2.aM - ls2.bl) * (double)f2;
        this.k = ls2.bm + (ls2.aN - ls2.bm) * (double)f2;
        this.l = ls2.bn + (ls2.aO - ls2.bn) * (double)f2;
    }

    public void a(ow ow2, float f2) {
        if (ow2.a(this.j, this.k, this.l) < 4096.0) {
            float f3 = this.f.c(ow2.e, ow2.f, ow2.g);
            GL11.glColor3f((float)f3, (float)f3, (float)f3);
            this.a(ow2, (double)ow2.e - b, (double)ow2.f - c, (double)ow2.g - d, f2);
        }
    }

    public void a(ow ow2, double d2, double d3, double d4, float f2) {
        je je2 = this.b(ow2);
        if (je2 != null) {
            je2.a(ow2, d2, d3, d4, f2);
        }
    }

    public void a(fd fd2) {
        this.f = fd2;
        for (je je2 : this.m.values()) {
            if (je2 == null) continue;
            je2.a(fd2);
        }
    }

    public sj a() {
        return this.n;
    }
}

