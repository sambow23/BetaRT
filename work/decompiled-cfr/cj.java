/*
 * Decompiled with CFR 0.152.
 */
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Map;

public class cj {
    private volatile boolean a = false;
    private volatile Map b = null;
    private volatile Map c = null;
    private xi d;
    private File e;
    private File f;
    private File g;
    private File h;
    private File i;
    private File j;
    private gr k;
    private int l = 0;
    private int m = 0;

    public cj(gr gr2, xi xi2, File file) {
        this.e = new File(file, "stats_" + gr2.b.toLowerCase() + "_unsent.dat");
        this.f = new File(file, "stats_" + gr2.b.toLowerCase() + ".dat");
        this.i = new File(file, "stats_" + gr2.b.toLowerCase() + "_unsent.old");
        this.j = new File(file, "stats_" + gr2.b.toLowerCase() + ".old");
        this.g = new File(file, "stats_" + gr2.b.toLowerCase() + "_unsent.tmp");
        this.h = new File(file, "stats_" + gr2.b.toLowerCase() + ".tmp");
        if (!gr2.b.toLowerCase().equals(gr2.b)) {
            this.a(file, "stats_" + gr2.b + "_unsent.dat", this.e);
            this.a(file, "stats_" + gr2.b + ".dat", this.f);
            this.a(file, "stats_" + gr2.b + "_unsent.old", this.i);
            this.a(file, "stats_" + gr2.b + ".old", this.j);
            this.a(file, "stats_" + gr2.b + "_unsent.tmp", this.g);
            this.a(file, "stats_" + gr2.b + ".tmp", this.h);
        }
        this.d = xi2;
        this.k = gr2;
        if (this.e.exists()) {
            xi2.a(this.a(this.e, this.g, this.i));
        }
        this.a();
    }

    private void a(File file, String string, File file2) {
        File file3 = new File(file, string);
        if (file3.exists() && !file3.isDirectory() && !file2.exists()) {
            file3.renameTo(file2);
        }
    }

    private Map a(File file, File file2, File file3) {
        if (file.exists()) {
            return this.a(file);
        }
        if (file3.exists()) {
            return this.a(file3);
        }
        if (file2.exists()) {
            return this.a(file2);
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private Map a(File file) {
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(file));
            String string = "";
            StringBuilder stringBuilder = new StringBuilder();
            while ((string = bufferedReader.readLine()) != null) {
                stringBuilder.append(string);
            }
            Map map = xi.a(stringBuilder.toString());
            return map;
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
        finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                }
                catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void a(Map map, File file, File file2, File file3) {
        PrintWriter printWriter = new PrintWriter(new FileWriter(file2, false));
        try {
            printWriter.print(xi.a(this.k.b, "local", map));
        }
        finally {
            printWriter.close();
        }
        if (file3.exists()) {
            file3.delete();
        }
        if (file.exists()) {
            file.renameTo(file3);
        }
        file2.renameTo(file);
    }

    public void a() {
        if (this.a) {
            throw new IllegalStateException("Can't get stats from server while StatsSyncher is busy!");
        }
        this.l = 100;
        this.a = true;
        new vk(this).start();
    }

    public void a(Map map) {
        if (this.a) {
            throw new IllegalStateException("Can't save stats while StatsSyncher is busy!");
        }
        this.l = 100;
        this.a = true;
        new vj(this, map).start();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void b(Map map) {
        int n2 = 30;
        while (this.a && --n2 > 0) {
            try {
                Thread.sleep(100L);
            }
            catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
        }
        this.a = true;
        try {
            this.a(map, this.e, this.g, this.i);
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
        finally {
            this.a = false;
        }
    }

    public boolean b() {
        return this.l <= 0 && !this.a && this.c == null;
    }

    public void c() {
        if (this.l > 0) {
            --this.l;
        }
        if (this.m > 0) {
            --this.m;
        }
        if (this.c != null) {
            this.d.c(this.c);
            this.c = null;
        }
        if (this.b != null) {
            this.d.b(this.b);
            this.b = null;
        }
    }

    static /* synthetic */ Map a(cj cj2) {
        return cj2.b;
    }

    static /* synthetic */ File b(cj cj2) {
        return cj2.f;
    }

    static /* synthetic */ File c(cj cj2) {
        return cj2.h;
    }

    static /* synthetic */ File d(cj cj2) {
        return cj2.j;
    }

    static /* synthetic */ void a(cj cj2, Map map, File file, File file2, File file3) {
        cj2.a(map, file, file2, file3);
    }

    static /* synthetic */ Map a(cj cj2, Map map) {
        cj2.b = map;
        return cj2.b;
    }

    static /* synthetic */ Map a(cj cj2, File file, File file2, File file3) {
        return cj2.a(file, file2, file3);
    }

    static /* synthetic */ boolean a(cj cj2, boolean bl2) {
        cj2.a = bl2;
        return cj2.a;
    }

    static /* synthetic */ File e(cj cj2) {
        return cj2.e;
    }

    static /* synthetic */ File f(cj cj2) {
        return cj2.g;
    }

    static /* synthetic */ File g(cj cj2) {
        return cj2.i;
    }
}

