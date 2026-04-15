/*
 * Decompiled with CFR 0.152.
 */
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class hc {
    private wt a;
    private Map b = new HashMap();
    private List c = new ArrayList();
    private Map d = new HashMap();

    public hc(wt wt2) {
        this.a = wt2;
        this.b();
    }

    public hm a(Class clazz, String string) {
        hm hm2;
        block7: {
            hm2 = (hm)this.b.get(string);
            if (hm2 != null) {
                return hm2;
            }
            if (this.a != null) {
                try {
                    File file = this.a.a(string);
                    if (file == null || !file.exists()) break block7;
                    try {
                        hm2 = (hm)clazz.getConstructor(String.class).newInstance(string);
                    }
                    catch (Exception exception) {
                        throw new RuntimeException("Failed to instantiate " + clazz.toString(), exception);
                    }
                    FileInputStream fileInputStream = new FileInputStream(file);
                    nu nu2 = as.a(fileInputStream);
                    fileInputStream.close();
                    hm2.a(nu2.k("data"));
                }
                catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        }
        if (hm2 != null) {
            this.b.put(string, hm2);
            this.c.add(hm2);
        }
        return hm2;
    }

    public void a(String string, hm hm2) {
        if (hm2 == null) {
            throw new RuntimeException("Can't set null data");
        }
        if (this.b.containsKey(string)) {
            this.c.remove(this.b.remove(string));
        }
        this.b.put(string, hm2);
        this.c.add(hm2);
    }

    public void a() {
        for (int i2 = 0; i2 < this.c.size(); ++i2) {
            hm hm2 = (hm)this.c.get(i2);
            if (!hm2.b()) continue;
            this.a(hm2);
            hm2.a(false);
        }
    }

    private void a(hm hm2) {
        if (this.a == null) {
            return;
        }
        try {
            File file = this.a.a(hm2.a);
            if (file != null) {
                nu nu2 = new nu();
                hm2.b(nu2);
                nu nu3 = new nu();
                nu3.a("data", nu2);
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                as.a(nu3, fileOutputStream);
                fileOutputStream.close();
            }
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void b() {
        try {
            this.d.clear();
            if (this.a == null) {
                return;
            }
            File file = this.a.a("idcounts");
            if (file != null && file.exists()) {
                DataInputStream dataInputStream = new DataInputStream(new FileInputStream(file));
                nu nu2 = as.a(dataInputStream);
                dataInputStream.close();
                for (ij ij2 : nu2.c()) {
                    if (!(ij2 instanceof ul)) continue;
                    ul ul2 = (ul)ij2;
                    String string = ul2.b();
                    short s2 = ul2.a;
                    this.d.put(string, s2);
                }
            }
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public int a(String string) {
        Object object;
        Comparable<Short> comparable;
        Short s2 = (Short)this.d.get(string);
        if (s2 == null) {
            s2 = 0;
        } else {
            comparable = s2;
            s2 = (short)(s2 + 1);
            object = s2;
        }
        this.d.put(string, s2);
        if (this.a == null) {
            return s2.shortValue();
        }
        try {
            comparable = this.a.a("idcounts");
            if (comparable != null) {
                object = new nu();
                for (String string2 : this.d.keySet()) {
                    short s3 = (Short)this.d.get(string2);
                    ((nu)object).a(string2, s3);
                }
                DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream((File)comparable));
                as.a((nu)object, dataOutputStream);
                dataOutputStream.close();
            }
        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
        return s2.shortValue();
    }
}

