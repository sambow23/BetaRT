/*
 * Decompiled with CFR 0.152.
 */
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;

public class rj {
    private static final Map a = new HashMap();

    private rj() {
    }

    public static synchronized qj a(File file, int n2, int n3) {
        qj qj2;
        File file2 = new File(file, "region");
        File file3 = new File(file2, "r." + (n2 >> 5) + "." + (n3 >> 5) + ".mcr");
        Reference reference = (Reference)a.get(file3);
        if (reference != null && (qj2 = (qj)reference.get()) != null) {
            return qj2;
        }
        if (!file2.exists()) {
            file2.mkdirs();
        }
        if (a.size() >= 256) {
            rj.a();
        }
        qj2 = new qj(file3);
        a.put(file3, new SoftReference<qj>(qj2));
        return qj2;
    }

    public static synchronized void a() {
        for (Reference reference : a.values()) {
            try {
                qj qj2 = (qj)reference.get();
                if (qj2 == null) continue;
                qj2.b();
            }
            catch (IOException iOException) {
                iOException.printStackTrace();
            }
        }
        a.clear();
    }

    public static int b(File file, int n2, int n3) {
        qj qj2 = rj.a(file, n2, n3);
        return qj2.a();
    }

    public static DataInputStream c(File file, int n2, int n3) {
        qj qj2 = rj.a(file, n2, n3);
        return qj2.a(n2 & 0x1F, n3 & 0x1F);
    }

    public static DataOutputStream d(File file, int n2, int n3) {
        qj qj2 = rj.a(file, n2, n3);
        return qj2.b(n2 & 0x1F, n3 & 0x1F);
    }
}

