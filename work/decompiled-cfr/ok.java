/*
 * Decompiled with CFR 0.152.
 */
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ok
implements cl {
    private Set a = new HashSet();
    private lm b;
    private cl c;
    private bf d;
    private Map e = new HashMap();
    private List f = new ArrayList();
    private fd g;

    public ok(fd fd2, bf bf2, cl cl2) {
        this.b = new li(fd2, new byte[32768], 0, 0);
        this.g = fd2;
        this.d = bf2;
        this.c = cl2;
    }

    public boolean a(int n2, int n3) {
        return this.e.containsKey(yy.a(n2, n3));
    }

    public lm c(int n2, int n3) {
        int n4 = yy.a(n2, n3);
        this.a.remove(n4);
        lm lm2 = (lm)this.e.get(n4);
        if (lm2 == null) {
            lm2 = this.d(n2, n3);
            if (lm2 == null) {
                lm2 = this.c == null ? this.b : this.c.b(n2, n3);
            }
            this.e.put(n4, lm2);
            this.f.add(lm2);
            if (lm2 != null) {
                lm2.d();
                lm2.e();
            }
            if (!lm2.n && this.a(n2 + 1, n3 + 1) && this.a(n2, n3 + 1) && this.a(n2 + 1, n3)) {
                this.a(this, n2, n3);
            }
            if (this.a(n2 - 1, n3) && !this.b((int)(n2 - 1), (int)n3).n && this.a(n2 - 1, n3 + 1) && this.a(n2, n3 + 1) && this.a(n2 - 1, n3)) {
                this.a(this, n2 - 1, n3);
            }
            if (this.a(n2, n3 - 1) && !this.b((int)n2, (int)(n3 - 1)).n && this.a(n2 + 1, n3 - 1) && this.a(n2, n3 - 1) && this.a(n2 + 1, n3)) {
                this.a(this, n2, n3 - 1);
            }
            if (this.a(n2 - 1, n3 - 1) && !this.b((int)(n2 - 1), (int)(n3 - 1)).n && this.a(n2 - 1, n3 - 1) && this.a(n2, n3 - 1) && this.a(n2 - 1, n3)) {
                this.a(this, n2 - 1, n3 - 1);
            }
        }
        return lm2;
    }

    public lm b(int n2, int n3) {
        lm lm2 = (lm)this.e.get(yy.a(n2, n3));
        if (lm2 == null) {
            return this.c(n2, n3);
        }
        return lm2;
    }

    private lm d(int n2, int n3) {
        if (this.d == null) {
            return null;
        }
        try {
            lm lm2 = this.d.a(this.g, n2, n3);
            if (lm2 != null) {
                lm2.r = this.g.t();
            }
            return lm2;
        }
        catch (Exception exception) {
            exception.printStackTrace();
            return null;
        }
    }

    private void a(lm lm2) {
        if (this.d == null) {
            return;
        }
        try {
            this.d.b(this.g, lm2);
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void b(lm lm2) {
        if (this.d == null) {
            return;
        }
        try {
            lm2.r = this.g.t();
            this.d.a(this.g, lm2);
        }
        catch (IOException iOException) {
            iOException.printStackTrace();
        }
    }

    public void a(cl cl2, int n2, int n3) {
        lm lm2 = this.b(n2, n3);
        if (!lm2.n) {
            lm2.n = true;
            if (this.c != null) {
                this.c.a(cl2, n2, n3);
                lm2.g();
            }
        }
    }

    public boolean a(boolean bl2, yb yb2) {
        int n2 = 0;
        for (int i2 = 0; i2 < this.f.size(); ++i2) {
            lm lm2 = (lm)this.f.get(i2);
            if (bl2 && !lm2.p) {
                this.a(lm2);
            }
            if (!lm2.a(bl2)) continue;
            this.b(lm2);
            lm2.o = false;
            if (++n2 != 24 || bl2) continue;
            return false;
        }
        if (bl2) {
            if (this.d == null) {
                return true;
            }
            this.d.b();
        }
        return true;
    }

    public boolean a() {
        for (int i2 = 0; i2 < 100; ++i2) {
            if (this.a.isEmpty()) continue;
            Integer n2 = (Integer)this.a.iterator().next();
            lm lm2 = (lm)this.e.get(n2);
            lm2.f();
            this.b(lm2);
            this.a(lm2);
            this.a.remove(n2);
            this.e.remove(n2);
            this.f.remove(lm2);
        }
        if (this.d != null) {
            this.d.a();
        }
        return this.c.a();
    }

    public boolean b() {
        return true;
    }

    public String c() {
        return "ServerChunkCache: " + this.e.size() + " Drop: " + this.a.size();
    }
}

