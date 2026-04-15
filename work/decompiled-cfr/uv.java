/*
 * Decompiled with CFR 0.152.
 */
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class uv
implements cl {
    private lm a;
    private Map b = new HashMap();
    private List c = new ArrayList();
    private fd d;

    public uv(fd fd2) {
        this.a = new li(fd2, new byte[32768], 0, 0);
        this.d = fd2;
    }

    public boolean a(int n2, int n3) {
        if (this != null) {
            return true;
        }
        yy yy2 = new yy(n2, n3);
        return this.b.containsKey(yy2);
    }

    public void d(int n2, int n3) {
        lm lm2 = this.b(n2, n3);
        if (!lm2.h()) {
            lm2.f();
        }
        this.b.remove(new yy(n2, n3));
        this.c.remove(lm2);
    }

    public lm c(int n2, int n3) {
        yy yy2 = new yy(n2, n3);
        byte[] byArray = new byte[32768];
        lm lm2 = new lm(this.d, byArray, n2, n3);
        Arrays.fill(lm2.f.a, (byte)-1);
        this.b.put(yy2, lm2);
        lm2.c = true;
        return lm2;
    }

    public lm b(int n2, int n3) {
        yy yy2 = new yy(n2, n3);
        lm lm2 = (lm)this.b.get(yy2);
        if (lm2 == null) {
            return this.a;
        }
        return lm2;
    }

    public boolean a(boolean bl2, yb yb2) {
        return true;
    }

    public boolean a() {
        return false;
    }

    public boolean b() {
        return false;
    }

    public void a(cl cl2, int n2, int n3) {
    }

    public String c() {
        return "MultiplayerChunkCache: " + this.b.size();
    }
}

