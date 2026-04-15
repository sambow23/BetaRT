/*
 * Decompiled with CFR 0.152.
 */
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class vr {
    public final int e;
    public final String f;
    public boolean g = false;
    public String h;
    private final wk a;
    private static NumberFormat b = NumberFormat.getIntegerInstance(Locale.US);
    public static wk i = new bq();
    private static DecimalFormat c = new DecimalFormat("########0.00");
    public static wk j = new bo();
    public static wk k = new bn();

    public vr(int n2, String string, wk wk2) {
        this.e = n2;
        this.f = string;
        this.a = wk2;
    }

    public vr(int n2, String string) {
        this(n2, string, i);
    }

    public vr h() {
        this.g = true;
        return this;
    }

    public vr g() {
        if (jl.a.containsKey(this.e)) {
            throw new RuntimeException("Duplicate stat id: \"" + ((vr)jl.a.get((Object)Integer.valueOf((int)this.e))).f + "\" and \"" + this.f + "\" at id " + this.e);
        }
        jl.b.add(this);
        jl.a.put(this.e, this);
        this.h = b.a(this.e);
        return this;
    }

    public boolean d() {
        return false;
    }

    public String a(int n2) {
        return this.a.a(n2);
    }

    public String toString() {
        return this.f;
    }

    static /* synthetic */ NumberFormat i() {
        return b;
    }

    static /* synthetic */ DecimalFormat j() {
        return c;
    }
}

