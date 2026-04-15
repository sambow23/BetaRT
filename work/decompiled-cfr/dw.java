/*
 * Decompiled with CFR 0.152.
 */
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class dw {
    public List d = new ArrayList();
    public List e = new ArrayList();
    public int f = 0;
    private short a = 0;
    protected List g = new ArrayList();
    private Set b = new HashSet();

    protected void a(gp gp2) {
        gp2.a = this.e.size();
        this.e.add(gp2);
        this.d.add(null);
    }

    public void a() {
        for (int i2 = 0; i2 < this.e.size(); ++i2) {
            iz iz2 = ((gp)this.e.get(i2)).a();
            iz iz3 = (iz)this.d.get(i2);
            if (iz.a(iz3, iz2)) continue;
            iz3 = iz2 == null ? null : iz2.k();
            this.d.set(i2, iz3);
            for (int i3 = 0; i3 < this.g.size(); ++i3) {
                ((ec)this.g.get(i3)).a(this, i2, iz3);
            }
        }
    }

    public gp b(int n2) {
        return (gp)this.e.get(n2);
    }

    public iz a(int n2) {
        gp gp2 = (gp)this.e.get(n2);
        if (gp2 != null) {
            return gp2.a();
        }
        return null;
    }

    public iz a(int n2, int n3, boolean bl2, gs gs2) {
        iz iz2 = null;
        if (n3 == 0 || n3 == 1) {
            ix ix2 = gs2.c;
            if (n2 == -999) {
                if (ix2.i() != null && n2 == -999) {
                    if (n3 == 0) {
                        gs2.a(ix2.i());
                        ix2.b((iz)null);
                    }
                    if (n3 == 1) {
                        gs2.a(ix2.i().a(1));
                        if (ix2.i().a == 0) {
                            ix2.b((iz)null);
                        }
                    }
                }
            } else if (bl2) {
                iz iz3 = this.a(n2);
                if (iz3 != null) {
                    int n4;
                    int n5 = iz3.a;
                    iz2 = iz3.k();
                    gp gp2 = (gp)this.e.get(n2);
                    if (gp2 != null && gp2.a() != null && (n4 = gp2.a().a) < n5) {
                        this.a(n2, n3, bl2, gs2);
                    }
                }
            } else {
                gp gp3 = (gp)this.e.get(n2);
                if (gp3 != null) {
                    int n6;
                    gp3.c();
                    iz iz4 = gp3.a();
                    iz iz5 = ix2.i();
                    if (iz4 != null) {
                        iz2 = iz4.k();
                    }
                    if (iz4 == null) {
                        if (iz5 != null && gp3.b(iz5)) {
                            int n7;
                            int n8 = n7 = n3 == 0 ? iz5.a : 1;
                            if (n7 > gp3.d()) {
                                n7 = gp3.d();
                            }
                            gp3.c(iz5.a(n7));
                            if (iz5.a == 0) {
                                ix2.b((iz)null);
                            }
                        }
                    } else if (iz5 == null) {
                        int n9 = n3 == 0 ? iz4.a : (iz4.a + 1) / 2;
                        iz iz6 = gp3.a(n9);
                        ix2.b(iz6);
                        if (iz4.a == 0) {
                            gp3.c(null);
                        }
                        gp3.a(ix2.i());
                    } else if (gp3.b(iz5)) {
                        if (iz4.c != iz5.c || iz4.f() && iz4.i() != iz5.i()) {
                            if (iz5.a <= gp3.d()) {
                                iz iz7 = iz4;
                                gp3.c(iz5);
                                ix2.b(iz7);
                            }
                        } else {
                            int n10;
                            int n11 = n10 = n3 == 0 ? iz5.a : 1;
                            if (n10 > gp3.d() - iz4.a) {
                                n10 = gp3.d() - iz4.a;
                            }
                            if (n10 > iz5.c() - iz4.a) {
                                n10 = iz5.c() - iz4.a;
                            }
                            iz5.a(n10);
                            if (iz5.a == 0) {
                                ix2.b((iz)null);
                            }
                            iz4.a += n10;
                        }
                    } else if (!(iz4.c != iz5.c || iz5.c() <= 1 || iz4.f() && iz4.i() != iz5.i() || (n6 = iz4.a) <= 0 || n6 + iz5.a > iz5.c())) {
                        iz5.a += n6;
                        iz4.a(n6);
                        if (iz4.a == 0) {
                            gp3.c(null);
                        }
                        gp3.a(ix2.i());
                    }
                }
            }
        }
        return iz2;
    }

    public void a(gs gs2) {
        ix ix2 = gs2.c;
        if (ix2.i() != null) {
            gs2.a(ix2.i());
            ix2.b((iz)null);
        }
    }

    public void a(lw lw2) {
        this.a();
    }

    public void a(int n2, iz iz2) {
        this.b(n2).c(iz2);
    }

    public void a(iz[] izArray) {
        for (int i2 = 0; i2 < izArray.length; ++i2) {
            this.b(i2).c(izArray[i2]);
        }
    }

    public void a(int n2, int n3) {
    }

    public short a(ix ix2) {
        this.a = (short)(this.a + 1);
        return this.a;
    }

    public void a(short s2) {
    }

    public void b(short s2) {
    }

    public abstract boolean b(gs var1);

    protected void a(iz iz2, int n2, int n3, boolean bl2) {
        iz iz3;
        gp gp2;
        int n4 = n2;
        if (bl2) {
            n4 = n3 - 1;
        }
        if (iz2.d()) {
            while (iz2.a > 0 && (!bl2 && n4 < n3 || bl2 && n4 >= n2)) {
                gp2 = (gp)this.e.get(n4);
                iz3 = gp2.a();
                if (!(iz3 == null || iz3.c != iz2.c || iz2.f() && iz2.i() != iz3.i())) {
                    int n5 = iz3.a + iz2.a;
                    if (n5 <= iz2.c()) {
                        iz2.a = 0;
                        iz3.a = n5;
                        gp2.c();
                    } else if (iz3.a < iz2.c()) {
                        iz2.a -= iz2.c() - iz3.a;
                        iz3.a = iz2.c();
                        gp2.c();
                    }
                }
                if (bl2) {
                    --n4;
                    continue;
                }
                ++n4;
            }
        }
        if (iz2.a > 0) {
            n4 = bl2 ? n3 - 1 : n2;
            while (!bl2 && n4 < n3 || bl2 && n4 >= n2) {
                gp2 = (gp)this.e.get(n4);
                iz3 = gp2.a();
                if (iz3 == null) {
                    gp2.c(iz2.k());
                    gp2.c();
                    iz2.a = 0;
                    break;
                }
                if (bl2) {
                    --n4;
                    continue;
                }
                ++n4;
            }
        }
    }
}

