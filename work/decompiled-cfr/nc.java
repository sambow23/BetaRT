/*
 * Decompiled with CFR 0.152.
 */
class nc
implements pu {
    final /* synthetic */ pn a;
    final /* synthetic */ lt b;

    nc(lt lt2, pn pn2) {
        this.b = lt2;
        this.a = pn2;
    }

    public void a(lb lb2) {
        this.a.b(lb2);
    }

    public void a(pn pn2) {
        throw new RuntimeException("Coding failure in Argo:  Attempt to add a field to a field.");
    }
}

