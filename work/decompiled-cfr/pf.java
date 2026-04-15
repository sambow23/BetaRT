/*
 * Decompiled with CFR 0.152.
 */
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class pf {
    public static final Object a = new Object();
    public static int b;
    public static int c;
    private Object g = new Object();
    private Socket h;
    private final SocketAddress i;
    private DataInputStream j;
    private DataOutputStream k;
    private boolean l = true;
    private List m = Collections.synchronizedList(new ArrayList());
    private List n = Collections.synchronizedList(new ArrayList());
    private List o = Collections.synchronizedList(new ArrayList());
    private ti p;
    private boolean q = false;
    private Thread r;
    private Thread s;
    private boolean t = false;
    private String u = "";
    private Object[] v;
    private int w = 0;
    private int x = 0;
    public static int[] d;
    public static int[] e;
    public int f = 0;
    private int y = 50;

    public pf(Socket socket, String string, ti ti2) {
        this.h = socket;
        this.i = socket.getRemoteSocketAddress();
        this.p = ti2;
        try {
            socket.setSoTimeout(30000);
            socket.setTrafficClass(24);
        }
        catch (SocketException socketException) {
            System.err.println(socketException.getMessage());
        }
        this.j = new DataInputStream(socket.getInputStream());
        this.k = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream(), 5120));
        this.s = new xj(this, string + " read thread");
        this.r = new xl(this, string + " write thread");
        this.s.start();
        this.r.start();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void a(ki ki2) {
        if (this.q) {
            return;
        }
        Object object = this.g;
        synchronized (object) {
            this.x += ki2.a() + 1;
            if (ki2.k) {
                this.o.add(ki2);
            } else {
                this.n.add(ki2);
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private boolean d() {
        boolean bl2;
        block10: {
            bl2 = false;
            try {
                ki ki2;
                Object object;
                if (!(this.n.isEmpty() || this.f != 0 && System.currentTimeMillis() - ((ki)this.n.get((int)0)).j < (long)this.f)) {
                    object = this.g;
                    synchronized (object) {
                        ki2 = (ki)this.n.remove(0);
                        this.x -= ki2.a() + 1;
                    }
                    ki.a(ki2, this.k);
                    int n2 = ki2.c();
                    e[n2] = e[n2] + (ki2.a() + 1);
                    bl2 = true;
                }
                if (this.y-- > 0 || this.o.isEmpty() || this.f != 0 && System.currentTimeMillis() - ((ki)this.o.get((int)0)).j < (long)this.f) break block10;
                object = this.g;
                synchronized (object) {
                    ki2 = (ki)this.o.remove(0);
                    this.x -= ki2.a() + 1;
                }
                ki.a(ki2, this.k);
                int n3 = ki2.c();
                e[n3] = e[n3] + (ki2.a() + 1);
                this.y = 0;
                bl2 = true;
            }
            catch (Exception exception) {
                if (!this.t) {
                    this.a(exception);
                }
                return false;
            }
        }
        return bl2;
    }

    public void a() {
        this.s.interrupt();
        this.r.interrupt();
    }

    private boolean e() {
        boolean bl2 = false;
        try {
            ki ki2 = ki.a(this.j, this.p.c());
            if (ki2 != null) {
                int n2 = ki2.c();
                d[n2] = d[n2] + (ki2.a() + 1);
                this.m.add(ki2);
                bl2 = true;
            } else {
                this.a("disconnect.endOfStream", new Object[0]);
            }
        }
        catch (Exception exception) {
            if (!this.t) {
                this.a(exception);
            }
            return false;
        }
        return bl2;
    }

    private void a(Exception exception) {
        exception.printStackTrace();
        this.a("disconnect.genericReason", "Internal exception: " + exception.toString());
    }

    public void a(String string, Object ... objectArray) {
        if (!this.l) {
            return;
        }
        this.t = true;
        this.u = string;
        this.v = objectArray;
        new xg(this).start();
        this.l = false;
        try {
            this.j.close();
            this.j = null;
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        try {
            this.k.close();
            this.k = null;
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        try {
            this.h.close();
            this.h = null;
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    public void b() {
        if (this.x > 0x100000) {
            this.a("disconnect.overflow", new Object[0]);
        }
        if (this.m.isEmpty()) {
            if (this.w++ == 1200) {
                this.a("disconnect.timeout", new Object[0]);
            }
        } else {
            this.w = 0;
        }
        int n2 = 100;
        while (!this.m.isEmpty() && n2-- >= 0) {
            ki ki2 = (ki)this.m.remove(0);
            ki2.a(this.p);
        }
        this.a();
        if (this.t && this.m.isEmpty()) {
            this.p.a(this.u, this.v);
        }
    }

    public void c() {
        this.a();
        this.q = true;
        this.s.interrupt();
        new xh(this).start();
    }

    static /* synthetic */ boolean a(pf pf2) {
        return pf2.l;
    }

    static /* synthetic */ boolean b(pf pf2) {
        return pf2.q;
    }

    static /* synthetic */ boolean c(pf pf2) {
        return pf2.e();
    }

    static /* synthetic */ boolean d(pf pf2) {
        return pf2.d();
    }

    static /* synthetic */ DataOutputStream e(pf pf2) {
        return pf2.k;
    }

    static /* synthetic */ boolean f(pf pf2) {
        return pf2.t;
    }

    static /* synthetic */ void a(pf pf2, Exception exception) {
        pf2.a(exception);
    }

    static /* synthetic */ Thread g(pf pf2) {
        return pf2.s;
    }

    static /* synthetic */ Thread h(pf pf2) {
        return pf2.r;
    }

    static {
        d = new int[256];
        e = new int[256];
    }
}

