/*
 * Decompiled with CFR 0.152.
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class tq
implements nl {
    protected final File a;

    public tq(File file) {
        if (!file.exists()) {
            file.mkdirs();
        }
        this.a = file;
    }

    public String a() {
        return "Old Format";
    }

    public List b() {
        ArrayList<vb> arrayList = new ArrayList<vb>();
        for (int i2 = 0; i2 < 5; ++i2) {
            String string = "World" + (i2 + 1);
            ei ei2 = this.b(string);
            if (ei2 == null) continue;
            arrayList.add(new vb(string, "", ei2.l(), ei2.g(), false));
        }
        return arrayList;
    }

    public void c() {
    }

    public ei b(String string) {
        File file = new File(this.a, string);
        if (!file.exists()) {
            return null;
        }
        File file2 = new File(file, "level.dat");
        if (file2.exists()) {
            try {
                nu nu2 = as.a(new FileInputStream(file2));
                nu nu3 = nu2.k("Data");
                return new ei(nu3);
            }
            catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        if ((file2 = new File(file, "level.dat_old")).exists()) {
            try {
                nu nu4 = as.a(new FileInputStream(file2));
                nu nu5 = nu4.k("Data");
                return new ei(nu5);
            }
            catch (Exception exception) {
                exception.printStackTrace();
            }
        }
        return null;
    }

    public void a(String string, String string2) {
        File file = new File(this.a, string);
        if (!file.exists()) {
            return;
        }
        File file2 = new File(file, "level.dat");
        if (file2.exists()) {
            try {
                nu nu2 = as.a(new FileInputStream(file2));
                nu nu3 = nu2.k("Data");
                nu3.a("LevelName", string2);
                as.a(nu2, new FileOutputStream(file2));
            }
            catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }

    public void c(String string) {
        File file = new File(this.a, string);
        if (!file.exists()) {
            return;
        }
        tq.a(file.listFiles());
        file.delete();
    }

    protected static void a(File[] fileArray) {
        for (int i2 = 0; i2 < fileArray.length; ++i2) {
            if (fileArray[i2].isDirectory()) {
                tq.a(fileArray[i2].listFiles());
            }
            fileArray[i2].delete();
        }
    }

    public wt a(String string, boolean bl2) {
        return new fm(this.a, string, bl2);
    }

    public boolean a(String string) {
        return false;
    }

    public boolean a(String string, yb yb2) {
        return false;
    }
}

