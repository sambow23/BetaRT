/*
 * Decompiled with CFR 0.152.
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

public class to
implements bf {
    private File a;
    private boolean b;

    public to(File file, boolean bl2) {
        this.a = file;
        this.b = bl2;
    }

    private File a(int n2, int n3) {
        String string = "c." + Integer.toString(n2, 36) + "." + Integer.toString(n3, 36) + ".dat";
        String string2 = Integer.toString(n2 & 0x3F, 36);
        String string3 = Integer.toString(n3 & 0x3F, 36);
        File file = new File(this.a, string2);
        if (!file.exists()) {
            if (this.b) {
                file.mkdir();
            } else {
                return null;
            }
        }
        if (!(file = new File(file, string3)).exists()) {
            if (this.b) {
                file.mkdir();
            } else {
                return null;
            }
        }
        if (!(file = new File(file, string)).exists() && !this.b) {
            return null;
        }
        return file;
    }

    public lm a(fd fd2, int n2, int n3) {
        File file = this.a(n2, n3);
        if (file != null && file.exists()) {
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                nu nu2 = as.a(fileInputStream);
                if (!nu2.b("Level")) {
                    System.out.println("Chunk file at " + n2 + "," + n3 + " is missing level data, skipping");
                    return null;
                }
                if (!nu2.k("Level").b("Blocks")) {
                    System.out.println("Chunk file at " + n2 + "," + n3 + " is missing block data, skipping");
                    return null;
                }
                lm lm2 = to.a(fd2, nu2.k("Level"));
                if (!lm2.a(n2, n3)) {
                    System.out.println("Chunk file at " + n2 + "," + n3 + " is in the wrong location; relocating. (Expected " + n2 + ", " + n3 + ", got " + lm2.j + ", " + lm2.k + ")");
                    nu2.a("xPos", n2);
                    nu2.a("zPos", n3);
                    lm2 = to.a(fd2, nu2.k("Level"));
                }
                lm2.i();
                return lm2;
            }
            catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        return null;
    }

    public void a(fd fd2, lm lm2) {
        Object object;
        fd2.r();
        File file = this.a(lm2.j, lm2.k);
        if (file.exists()) {
            object = fd2.x();
            ((ei)object).b(((ei)object).g() - file.length());
        }
        try {
            object = new File(this.a, "tmp_chunk.dat");
            FileOutputStream fileOutputStream = new FileOutputStream((File)object);
            nu nu2 = new nu();
            nu nu3 = new nu();
            nu2.a("Level", (ij)nu3);
            to.a(lm2, fd2, nu3);
            as.a(nu2, fileOutputStream);
            fileOutputStream.close();
            if (file.exists()) {
                file.delete();
            }
            ((File)object).renameTo(file);
            ei ei2 = fd2.x();
            ei2.b(ei2.g() + file.length());
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public static void a(lm lm2, fd fd2, nu nu2) {
        nu nu3;
        fd2.r();
        nu2.a("xPos", lm2.j);
        nu2.a("zPos", lm2.k);
        nu2.a("LastUpdate", fd2.t());
        nu2.a("Blocks", lm2.b);
        nu2.a("Data", lm2.e.a);
        nu2.a("SkyLight", lm2.f.a);
        nu2.a("BlockLight", lm2.g.a);
        nu2.a("HeightMap", lm2.h);
        nu2.a("TerrainPopulated", lm2.n);
        lm2.q = false;
        sp sp2 = new sp();
        for (int i2 = 0; i2 < lm2.m.length; ++i2) {
            for (Object object : lm2.m[i2]) {
                lm2.q = true;
                nu3 = new nu();
                if (!((sn)object).c(nu3)) continue;
                sp2.a(nu3);
            }
        }
        nu2.a("Entities", sp2);
        sp sp3 = new sp();
        for (Object object : lm2.l.values()) {
            nu3 = new nu();
            ((ow)object).b(nu3);
            sp3.a(nu3);
        }
        nu2.a("TileEntities", sp3);
    }

    public static lm a(fd fd2, nu nu2) {
        sp sp2;
        Object object;
        sp sp3;
        int n2 = nu2.e("xPos");
        int n3 = nu2.e("zPos");
        lm lm2 = new lm(fd2, n2, n3);
        lm2.b = nu2.j("Blocks");
        lm2.e = new wi(nu2.j("Data"));
        lm2.f = new wi(nu2.j("SkyLight"));
        lm2.g = new wi(nu2.j("BlockLight"));
        lm2.h = nu2.j("HeightMap");
        lm2.n = nu2.m("TerrainPopulated");
        if (!lm2.e.a()) {
            lm2.e = new wi(lm2.b.length);
        }
        if (lm2.h == null || !lm2.f.a()) {
            lm2.h = new byte[256];
            lm2.f = new wi(lm2.b.length);
            lm2.c();
        }
        if (!lm2.g.a()) {
            lm2.g = new wi(lm2.b.length);
            lm2.a();
        }
        if ((sp3 = nu2.l("Entities")) != null) {
            for (int i2 = 0; i2 < sp3.c(); ++i2) {
                nu nu3 = (nu)sp3.a(i2);
                object = jc.a(nu3, fd2);
                lm2.q = true;
                if (object == null) continue;
                lm2.a((sn)object);
            }
        }
        if ((sp2 = nu2.l("TileEntities")) != null) {
            for (int i3 = 0; i3 < sp2.c(); ++i3) {
                object = (nu)sp2.a(i3);
                ow ow2 = ow.c((nu)object);
                if (ow2 == null) continue;
                lm2.a(ow2);
            }
        }
        return lm2;
    }

    public void a() {
    }

    public void b() {
    }

    public void b(fd fd2, lm lm2) {
    }
}

