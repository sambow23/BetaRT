/*
 * Decompiled with CFR 0.152.
 */
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public class fm
implements wt {
    private static final Logger a = Logger.getLogger("Minecraft");
    private final File b;
    private final File c;
    private final File d;
    private final long e = System.currentTimeMillis();

    public fm(File file, String string, boolean bl2) {
        this.b = new File(file, string);
        this.b.mkdirs();
        this.c = new File(this.b, "players");
        this.d = new File(this.b, "data");
        this.d.mkdirs();
        if (bl2) {
            this.c.mkdirs();
        }
        this.d();
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void d() {
        try {
            File file = new File(this.b, "session.lock");
            DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(file));
            try {
                dataOutputStream.writeLong(this.e);
            }
            finally {
                dataOutputStream.close();
            }
        }
        catch (IOException iOException) {
            iOException.printStackTrace();
            throw new RuntimeException("Failed to check session lock, aborting");
        }
    }

    protected File a() {
        return this.b;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void b() {
        try {
            File file = new File(this.b, "session.lock");
            DataInputStream dataInputStream = new DataInputStream(new FileInputStream(file));
            try {
                if (dataInputStream.readLong() != this.e) {
                    throw new us("The save is being accessed from another location, aborting");
                }
            }
            finally {
                dataInputStream.close();
            }
        }
        catch (IOException iOException) {
            throw new us("Failed to check session lock, aborting");
        }
    }

    public bf a(xa xa2) {
        if (xa2 instanceof wd) {
            File file = new File(this.b, "DIM-1");
            file.mkdirs();
            return new to(file, true);
        }
        return new to(this.b, true);
    }

    public ei c() {
        File file = new File(this.b, "level.dat");
        if (file.exists()) {
            try {
                nu nu2 = as.a(new FileInputStream(file));
                nu nu3 = nu2.k("Data");
                return new ei(nu3);
            }
            catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        if ((file = new File(this.b, "level.dat_old")).exists()) {
            try {
                nu nu4 = as.a(new FileInputStream(file));
                nu nu5 = nu4.k("Data");
                return new ei(nu5);
            }
            catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        return null;
    }

    public void a(ei ei2, List list) {
        nu nu2 = ei2.a(list);
        nu nu3 = new nu();
        nu3.a("Data", (ij)nu2);
        try {
            File file = new File(this.b, "level.dat_new");
            File file2 = new File(this.b, "level.dat_old");
            File file3 = new File(this.b, "level.dat");
            as.a(nu3, new FileOutputStream(file));
            if (file2.exists()) {
                file2.delete();
            }
            file3.renameTo(file2);
            if (file3.exists()) {
                file3.delete();
            }
            file.renameTo(file3);
            if (file.exists()) {
                file.delete();
            }
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void a(ei ei2) {
        nu nu2 = ei2.a();
        nu nu3 = new nu();
        nu3.a("Data", (ij)nu2);
        try {
            File file = new File(this.b, "level.dat_new");
            File file2 = new File(this.b, "level.dat_old");
            File file3 = new File(this.b, "level.dat");
            as.a(nu3, new FileOutputStream(file));
            if (file2.exists()) {
                file2.delete();
            }
            file3.renameTo(file2);
            if (file3.exists()) {
                file3.delete();
            }
            file.renameTo(file3);
            if (file.exists()) {
                file.delete();
            }
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public File a(String string) {
        return new File(this.d, string + ".dat");
    }
}

