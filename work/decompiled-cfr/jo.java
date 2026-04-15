/*
 * Decompiled with CFR 0.152.
 */
import java.util.Comparator;

public class jo
implements Comparator {
    private double a;
    private double b;
    private double c;

    public jo(sn sn2) {
        this.a = -sn2.aM;
        this.b = -sn2.aN;
        this.c = -sn2.aO;
    }

    public int a(dk dk2, dk dk3) {
        double d2 = (double)dk2.q + this.a;
        double d3 = (double)dk2.r + this.b;
        double d4 = (double)dk2.s + this.c;
        double d5 = (double)dk3.q + this.a;
        double d6 = (double)dk3.r + this.b;
        double d7 = (double)dk3.s + this.c;
        return (int)((d2 * d2 + d3 * d3 + d4 * d4 - (d5 * d5 + d6 * d6 + d7 * d7)) * 1024.0);
    }
}

