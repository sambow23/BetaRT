/*
 * Decompiled with CFR 0.152.
 */
import java.util.List;

public class qo
implements lw {
    private String a;
    private int b;
    private iz[] c;
    private List d;

    public qo(String string, int n2) {
        this.a = string;
        this.b = n2;
        this.c = new iz[n2];
    }

    public iz f_(int n2) {
        return this.c[n2];
    }

    public iz a(int n2, int n3) {
        if (this.c[n2] != null) {
            if (this.c[n2].a <= n3) {
                iz iz2 = this.c[n2];
                this.c[n2] = null;
                this.y_();
                return iz2;
            }
            iz iz3 = this.c[n2].a(n3);
            if (this.c[n2].a == 0) {
                this.c[n2] = null;
            }
            this.y_();
            return iz3;
        }
        return null;
    }

    public void a(int n2, iz iz2) {
        this.c[n2] = iz2;
        if (iz2 != null && iz2.a > this.d()) {
            iz2.a = this.d();
        }
        this.y_();
    }

    public int a() {
        return this.b;
    }

    public String c() {
        return this.a;
    }

    public int d() {
        return 64;
    }

    public void y_() {
        if (this.d != null) {
            for (int i2 = 0; i2 < this.d.size(); ++i2) {
                ((hv)this.d.get(i2)).a(this);
            }
        }
    }

    public boolean a_(gs gs2) {
        return true;
    }
}

