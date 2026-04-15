/*
 * Decompiled with CFR 0.152.
 */
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.Minecraft;

public class ik {
    private List b = new ArrayList();
    private i c = new pa();
    public i a;
    private Map d = new HashMap();
    private Minecraft e;
    private File f;
    private String g;

    public ik(Minecraft minecraft, File file) {
        this.e = minecraft;
        this.f = new File(file, "texturepacks");
        if (!this.f.exists()) {
            this.f.mkdirs();
        }
        this.g = minecraft.z.l;
        this.a();
        this.a.a();
    }

    public boolean a(i i2) {
        if (i2 == this.a) {
            return false;
        }
        this.a.b();
        this.g = i2.a;
        this.a = i2;
        this.e.z.l = this.g;
        this.e.z.b();
        this.a.a();
        return true;
    }

    public void a() {
        ArrayList<i> arrayList = new ArrayList<i>();
        this.a = null;
        arrayList.add(this.c);
        if (this.f.exists() && this.f.isDirectory()) {
            Object object;
            for (File file : object = this.f.listFiles()) {
                if (!file.isFile() || !file.getName().toLowerCase().endsWith(".zip")) continue;
                String string = file.getName() + ":" + file.length() + ":" + file.lastModified();
                try {
                    i i2;
                    if (!this.d.containsKey(string)) {
                        i2 = new vp(file);
                        i2.d = string;
                        this.d.put(string, i2);
                        i2.a(this.e);
                    }
                    i2 = (i)this.d.get(string);
                    if (i2.a.equals(this.g)) {
                        this.a = i2;
                    }
                    arrayList.add(i2);
                }
                catch (IOException iOException) {
                    iOException.printStackTrace();
                }
            }
        }
        if (this.a == null) {
            this.a = this.c;
        }
        this.b.removeAll(arrayList);
        for (Object object : this.b) {
            ((i)object).b(this.e);
            this.d.remove(((i)object).d);
        }
        this.b = arrayList;
    }

    public List b() {
        return new ArrayList(this.b);
    }
}

