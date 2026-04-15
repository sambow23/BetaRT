/*
 * Decompiled with CFR 0.152.
 */
import java.io.DataInput;
import java.io.DataOutput;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class nu
extends ij {
    private Map a = new HashMap();

    void a(DataOutput dataOutput) {
        for (ij ij2 : this.a.values()) {
            ij.a(ij2, dataOutput);
        }
        dataOutput.writeByte(0);
    }

    void a(DataInput dataInput) {
        ij ij2;
        this.a.clear();
        while ((ij2 = ij.b(dataInput)).a() != 0) {
            this.a.put(ij2.b(), ij2);
        }
    }

    public Collection c() {
        return this.a.values();
    }

    public byte a() {
        return 10;
    }

    public void a(String string, ij ij2) {
        this.a.put(string, ij2.a(string));
    }

    public void a(String string, byte by2) {
        this.a.put(string, new qp(by2).a(string));
    }

    public void a(String string, short s2) {
        this.a.put(string, new ul(s2).a(string));
    }

    public void a(String string, int n2) {
        this.a.put(string, new pp(n2).a(string));
    }

    public void a(String string, long l2) {
        this.a.put(string, new mi(l2).a(string));
    }

    public void a(String string, float f2) {
        this.a.put(string, new p(f2).a(string));
    }

    public void a(String string, double d2) {
        this.a.put(string, new sz(d2).a(string));
    }

    public void a(String string, String string2) {
        this.a.put(string, new xb(string2).a(string));
    }

    public void a(String string, byte[] byArray) {
        this.a.put(string, new hn(byArray).a(string));
    }

    public void a(String string, nu nu2) {
        this.a.put(string, nu2.a(string));
    }

    public void a(String string, boolean bl2) {
        this.a(string, bl2 ? (byte)1 : 0);
    }

    public boolean b(String string) {
        return this.a.containsKey(string);
    }

    public byte c(String string) {
        if (!this.a.containsKey(string)) {
            return 0;
        }
        return ((qp)this.a.get((Object)string)).a;
    }

    public short d(String string) {
        if (!this.a.containsKey(string)) {
            return 0;
        }
        return ((ul)this.a.get((Object)string)).a;
    }

    public int e(String string) {
        if (!this.a.containsKey(string)) {
            return 0;
        }
        return ((pp)this.a.get((Object)string)).a;
    }

    public long f(String string) {
        if (!this.a.containsKey(string)) {
            return 0L;
        }
        return ((mi)this.a.get((Object)string)).a;
    }

    public float g(String string) {
        if (!this.a.containsKey(string)) {
            return 0.0f;
        }
        return ((p)this.a.get((Object)string)).a;
    }

    public double h(String string) {
        if (!this.a.containsKey(string)) {
            return 0.0;
        }
        return ((sz)this.a.get((Object)string)).a;
    }

    public String i(String string) {
        if (!this.a.containsKey(string)) {
            return "";
        }
        return ((xb)this.a.get((Object)string)).a;
    }

    public byte[] j(String string) {
        if (!this.a.containsKey(string)) {
            return new byte[0];
        }
        return ((hn)this.a.get((Object)string)).a;
    }

    public nu k(String string) {
        if (!this.a.containsKey(string)) {
            return new nu();
        }
        return (nu)this.a.get(string);
    }

    public sp l(String string) {
        if (!this.a.containsKey(string)) {
            return new sp();
        }
        return (sp)this.a.get(string);
    }

    public boolean m(String string) {
        return this.c(string) != 0;
    }

    public String toString() {
        return "" + this.a.size() + " entries";
    }
}

