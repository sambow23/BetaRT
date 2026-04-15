/*
 * Decompiled with CFR 0.152.
 */
final class bn
implements wk {
    bn() {
    }

    public String a(int n2) {
        int n3 = n2;
        double d2 = (double)n3 / 100.0;
        double d3 = d2 / 1000.0;
        if (d3 > 0.5) {
            return vr.j().format(d3) + " km";
        }
        if (d2 > 0.5) {
            return vr.j().format(d2) + " m";
        }
        return n2 + " cm";
    }
}

